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

import akka.actor.{ActorLogging, Actor, Props}
import scala.concurrent.duration.FiniteDuration

object HttpService {
  def props(hostname: String, port: Int, timeout: FiniteDuration): Props =
    Props(classOf[HttpService], hostname, port, timeout)
}

class HttpService(hostname: String, port: Int, timeout: FiniteDuration) extends Actor with ActorLogging {

  log info s"h: $hostname"
  log info s"p: $port"
  log info s"t: $timeout"

  context.system.shutdown()

  def receive = Actor.emptyBehavior

}
