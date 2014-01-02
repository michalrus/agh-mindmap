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
  case class UpdateResult(orphanNodes: List[UUID])
  case class Subscribe(whom: ActorRef, since: Long)
  case class Unsubscribe(whom: ActorRef)

  case class Changed(node: MindNode)

  def props(mapUuid: UUID) = Props(classOf[MindMap], mapUuid)

}

class MindMap(mapUuid: UUID) extends Actor {
  import MindMap._

  import collection.immutable.TreeMap

  var subscribers = Set.empty[ActorRef]

  object DB {
    // FIXME: this should be a real database ;-)

    object TimesIdx {
      private var times = TreeMap.empty[Long, Set[UUID]]
      private var timesInv = TreeMap.empty[UUID, Long]

      def findSince(time: Long): Iterable[MindNode] = (for {
        (_, uuids) <- times from time
        uuid <- uuids
      } yield nodes get uuid).flatten

      def remove(node: MindNode) {
        for {
          time <- timesInv get node.uuid
          oldSet <- times get time
        } times += time -> (oldSet - node.uuid)
        timesInv -= node.uuid
      }

      def add(node: MindNode) {
        times += node.cloudTime -> (times get node.cloudTime match {
          case Some(set) => set + node.uuid
          case None => Set(node.uuid)
        })
        timesInv += node.uuid -> node.cloudTime
      }
    }

    private var nodes = Map.empty[UUID, MindNode]

    def find(uuid: UUID): Option[MindNode] = nodes get uuid

    def touchChildrenOf(node: UUID) {
      ???
      ???
    }

    def wasAnyChildChanged(parent: UUID, since: Long): Boolean = {
      ???
      ???
    }

    def contains(uuid: UUID): Boolean = nodes contains uuid

    def isEmpty: Boolean = nodes.isEmpty

    def findSince(time: Long): Iterable[MindNode] = TimesIdx findSince time

    def insertOrReplace(node: MindNode) {
      TimesIdx remove node
      TimesIdx add node
      nodes += node.uuid -> node
    }

    def deleteChildrenOf(node: UUID) {
      // FIXME: inefficient `children` lookup
      val children = nodes.values filter (_.parent exists node.==)

      children foreach { child =>
        // *** DFS, DO NOT CHANGE ORDER! ***
        deleteChildrenOf(child.uuid)

        TimesIdx remove child
        nodes -= node
      }
    }
  }

  def receive = {
    case Subscribe(whom, since) =>
      DB findSince since foreach (whom ! Changed(_))
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

      DB insertOrReplace update
      subscribers foreach (_ ! Changed(update))
    }

  def merged(fromClient: MindNode, atTime: Long): MindNode =
    DB find fromClient.uuid match {
      case None => fromClient // new node creation

      case Some(existing) => // `existing` node update
        (existing.content, fromClient.content) match {

          case (Some(o), Some(n)) => // content change
            if (existing.cloudTime > atTime) // *** CONFLICT!!! ***
              fromClient.copy(content = Some(o + "\n" + n), hasConflict = true)
            else // normal content change
              fromClient.copy(hasConflict = false)

          case (Some(o), None) => // subtree deletion
            if (DB wasAnyChildChanged (existing.uuid, atTime)) {
              DB touchChildrenOf existing.uuid
              existing.copy(hasConflict = false)
            } else {
              DB deleteChildrenOf existing.uuid
              fromClient.copy(hasConflict = false)
            }

          case (None, Some(n)) => // recreation?
            // can happen if I haz conflict
            ???
            ???

          case (None, None) => // can happen rarely, not so important
            fromClient.copy(hasConflict = false)

        }

    }

  def orphanNodes(potentialUpdates: List[MindNode]): Set[UUID] = {
    var orphans = Set.empty[UUID]
    val request = (potentialUpdates map (n => n.uuid -> n)).toMap

    request foreach { case (_, node) =>
      if (DB contains node.uuid)
        () // cool, modifying already existing node
      else node.parent match {
        case Some(parent) =>
          if ((DB contains parent) || (request contains parent))
            () // cool, adding a new child to a known parent
          else
            orphans += node.uuid // not cool, no parent known for this node :(
        case None =>
          if (DB.isEmpty)
            () // cool, new map creation
          else
            orphans += node.uuid // not cool, should not happen (adding a second root?!?!)
      }
    }

    orphans
  }

}
