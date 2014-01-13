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
import edu.agh.mindmapd.extensions.Settings
import edu.agh.mindmapd.storage.SquerylStorage

object MindMap {

  case class Update(lastServerTime: Long, nodes: List[MindNode])
  case class UpdateResult(orphanNodes: List[UUID])
  case class Subscribe(whom: ActorRef, since: Long)
  case class Unsubscribe(whom: ActorRef)

  case class Changed(node: MindNode)

  def props(mapUuid: UUID) = Props(classOf[MindMap], mapUuid)

}

class MindMap(mapUuid: UUID) extends Actor {
  import MindMap._

  val storage = SquerylStorage(mapUuid, Settings(context.system))

  var subscribers = Set.empty[ActorRef]

  def receive = {
    case Subscribe(whom, since) =>
      storage findSince (since, limit = 50) foreach (whom ! Changed(_))
      subscribers += whom

    case Unsubscribe(whom) =>
      subscribers -= whom

    case Update(atTime, updates) =>
      orphanNodes(updates) match {
        case orphans if orphans.nonEmpty =>
          sender ! UpdateResult(orphans.toList)
        case _ =>
          mergeIn(updates, atTime)
          sender ! UpdateResult(orphanNodes = Nil)
      }
  }

  def mergeIn(updates: List[MindNode], atTime: Long) =
    updates foreach { suggestion =>
      val update = merged(suggestion, atTime).
        copy(cloudTime = System.currentTimeMillis)

      storage insertOrReplace update
      subscribers foreach (_ ! Changed(update))
    }

  def merged(fromClient: MindNode, atTime: Long): MindNode =
    storage find fromClient.uuid match {
      case None => fromClient // new node creation

      case Some(existing) => // `existing` node update
        (existing.content, fromClient.content) match {

          case (Some(o), Some(n)) => // content change
            if (existing.cloudTime > atTime && o != n) // *** CONFLICT!!! ***
              fromClient.copy(content = Some(o + "\n" + n), hasConflict = true)
            else // normal content change
              fromClient.copy(hasConflict = false)

          case (Some(o), None) => // subtree deletion
            if (storage wasAnyChangedInSubtree (existing, atTime)) {
              storage touchTimesOfSubtree existing.uuid
              existing.copy(hasConflict = false)
            } else {
              storage deleteChildrenOf existing.uuid
              fromClient.copy(hasConflict = false)
            }

          case (None, Some(n)) => // recreation?
            fromClient.copy(hasConflict = false)

          case (None, None) => // can happen rarely, not so important
            fromClient.copy(hasConflict = false)

        }

    }

  def orphanNodes(potentialUpdates: List[MindNode]): Set[UUID] = {
    var orphans = Set.empty[UUID]
    val request = (potentialUpdates map (n => n.uuid -> n)).toMap

    request foreach { case (_, node) =>
      if (storage contains node.uuid)
        () // cool, modifying already existing node
      else node.parent match {
        case Some(parent) =>
          if ((storage find parent exists (_.content.isDefined)) || (request contains parent))
            () // cool, adding a new child to a known and not already deleted parent
          else
            orphans += node.uuid // not cool, no parent known for this node :(
        case None =>
          if (storage.hasNoNodesYet)
            () // cool, new map creation
          else
            orphans += node.uuid // not cool, should not happen (adding a second root?!?!)
      }
    }

    orphans
  }

}
