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
import akka.actor.{PoisonPill, ActorRef, ActorLogging, Props}
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
import spray.http.StatusCodes

object Service {

  def props(mapsSupervisor: ActorRef) =
    Props(classOf[Service], mapsSupervisor)

}

class Service(mapsSupervisor: ActorRef)
  extends HttpServiceActor with ActorLogging with SprayJsonSupport {
  import context.dispatcher

  val settings = Settings(context.system)

  IO(Http)(context.system) ! Http.Bind(self, settings.hostname, settings.port)

  def receive = runRoute {
    path("update") {
      post { entity(as[UpdateRequest]) { req =>
        complete {
          val updater = context actorOf Updater.props
          implicit val tm = Timeout(settings.timeout.update + settings.timeout.internalMessage)
          for {
            map <- mindMapFor(req.mindMap)
            resp <- (updater ? Updater.Process(req, map)).mapTo[UpdateResponse]
          } yield resp
        }
      }}
    } ~
    path("poll" / "since" / LongNumber) { since =>
      get { complete {
        val poller = context actorOf Poller.props(mapsSupervisor)
        implicit val tm = Timeout(settings.timeout.poll + settings.timeout.internalMessage)
        (poller ? Poller.Process(since)).mapTo[PollResponse]
      }}
    } ~
    path("die") { get { complete {
      if (Settings(context.system).isProduction) {
        (StatusCodes.Forbidden, "Won't die at production, u mad? =,=\n")
      } else {
        val _ = context.system.scheduler scheduleOnce (100.millis, self, PoisonPill)
        "Dying...\n"
      }
    }}}
  }

  def mindMapFor(uuid: UUID): Future[ActorRef] = {
    implicit val timeout = Timeout(settings.timeout.internalMessage)
    (mapsSupervisor ? MapsSupervisor.Find(uuid)).mapTo[ActorRef]
  }

}
