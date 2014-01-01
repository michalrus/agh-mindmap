/*
 * Copyright 2013 Katarzyna Szawan <kat.szwn@gmail.com>
 *     and Michał Rus <https://michalrus.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.agh.mindmapd.actors.http

import akka.pattern.ask
import akka.actor.{ActorRef, ActorLogging, Props}
import concurrent.duration._
import spray.routing.HttpServiceActor
import spray.httpx.SprayJsonSupport
import akka.io.IO
import spray.can.Http
import edu.agh.mindmapd.extensions.Settings
import java.util.UUID
import edu.agh.mindmapd.actors.MapsSupervisor
import edu.agh.mindmapd.json.{UpdateResponse, UpdateRequest, PollResponse}
import akka.util.Timeout
import scala.concurrent.Future

object Service {

  def props(hostname: String, port: Int, timeout: FiniteDuration, mapsSupervisor: ActorRef) =
    Props(classOf[Service], hostname, port, timeout, mapsSupervisor)

}

class Service(hostname: String, port: Int, timeout: FiniteDuration, mapsSupervisor: ActorRef)
  extends HttpServiceActor with ActorLogging with SprayJsonSupport {
  import context.dispatcher

  IO(Http)(context.system) ! Http.Bind(self, hostname, port)

  def receive = runRoute {
    path("update") {
      post { entity(as[UpdateRequest]) { req =>
        produce(instanceOf[UpdateResponse]) { completer => _ =>
          val updater = context actorOf Updater.props // *** this Actor has to be local!
          for {
            map <- mindMapFor(req.mindMap)
          } updater ! Updater.Process(req, map, completer)
        }
      }}
    } ~
    path("poll" / "since" / LongNumber) { since =>
      get { produce(instanceOf[PollResponse]) { completer => _ =>
        val poller = context actorOf Poller.props(mapsSupervisor) // *** this Actor has to be local!
        poller ! Poller.Process(since, completer)
      }}
    } ~
    path("die") { get { complete {
      if (Settings(context.system).isProduction) {
        "Won't die at production, u mad? =,=\n"
      } else {
        context.system.shutdown()
        "Dying...\n"
      }
    }}}
  }

  def mindMapFor(uuid: UUID): Future[ActorRef] = {
    implicit val timeout = Timeout(5.seconds)
    (mapsSupervisor ? MapsSupervisor.Find(uuid)).mapTo[ActorRef]
  }

}
