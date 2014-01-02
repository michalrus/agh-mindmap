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
import edu.agh.mindmapd.model.MindNode

object MindMap {

  case class Update(lastServerTime: Long, nodes: List[MindNode])
  case class UpdateResult(unknownParents: List[UUID])
  case class Subscribe(whom: ActorRef, since: Long)
  case class Unsubscribe(whom: ActorRef)

  case class Changed(node: MindNode)

  def props(mapUuid: UUID) = Props(classOf[MindMap], mapUuid)

}

class MindMap(mapUuid: UUID) extends Actor {
  import MindMap._

  import collection.immutable.TreeMap

  var times = TreeMap.empty[Long, UUID]
  var nodes = Map.empty[UUID, MindNode]
  var subscribers = Set.empty[ActorRef]

  def receive = {
    case Subscribe(whom, since) =>
      for {
        (_, uuid) <- times from since
        node <- nodes get uuid
      } whom ! Changed(node)
      subscribers += whom

    case Unsubscribe(whom) =>
      subscribers -= whom

    case Update(atTime, updates) =>
      // FIXME: save the update
      // FIXME: send the updates to all `subscribers`
      sender ! UpdateResult(Nil)
  }

}
