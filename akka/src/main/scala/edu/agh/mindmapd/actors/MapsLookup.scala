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

import akka.actor.{ActorRef, Props, Actor}
import java.util.UUID

object MapsLookup {

  case class Find(uuids: List[UUID])

  def props = Props(classOf[MapsLookup])

}

class MapsLookup extends Actor {
  import MapsLookup._

  def receive = {
    case Find(uuids) =>
      sender ! uuids.map(u => (u, refFor(u))).toMap
  }

  def refFor(uuid: UUID): ActorRef = {
    val s = uuid.toString
    context child s match {
      case Some(ref) => ref
      case _ => context actorOf (MindMap.props(uuid), s)
    }
  }

}
