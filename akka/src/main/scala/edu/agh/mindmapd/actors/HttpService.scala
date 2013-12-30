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

package edu.agh.mindmapd.actors

import akka.actor.{ActorLogging, Props}
import concurrent.duration._
import scala.concurrent.duration.FiniteDuration
import spray.routing.HttpServiceActor
import spray.httpx.SprayJsonSupport
import akka.io.IO
import spray.can.Http
import edu.agh.mindmapd.extensions.Settings

object HttpService {
  def props(hostname: String, port: Int, timeout: FiniteDuration): Props =
    Props(classOf[HttpService], hostname, port, timeout)
}

class HttpService(hostname: String, port: Int, timeout: FiniteDuration)
  extends HttpServiceActor with ActorLogging with SprayJsonSupport {
  import HttpService._
  import context.dispatcher

  IO(Http)(context.system) ! Http.Bind(self, hostname, port)

  def receive = runRoute(pathPrefix("api") {
    path("test") {
      get { complete(<i>xoxoxo</i>) }
    } ~
    path("die")(get { complete {
      if (Settings(context.system).isProduction) {
        "Won't die at production, u mad? =,=\n"
      } else {
        val sys = context.system
        sys.scheduler.scheduleOnce(Duration.Zero) { sys.shutdown() }
        "Dying...\n"
      }
    }})
  })

}
