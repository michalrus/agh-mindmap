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

import akka.actor.{Terminated, Actor}
import edu.agh.mindmapd.extensions.Settings

class Supervisor extends Actor {

  val s = Settings(context.system)

  val mapsSupervisor = context actorOf (MapsSupervisor.props, "maps")

  val httpService = context actorOf (http.Service.props(mapsSupervisor), "http-service")
  context watch httpService

  def receive = {
    case Terminated(`httpService`) => context stop self
  }

}
