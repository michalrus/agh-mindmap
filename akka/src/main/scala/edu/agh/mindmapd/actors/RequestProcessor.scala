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

import akka.actor.{Props, ActorRef, Actor}
import java.util.UUID
import edu.agh.mindmapd.model.{MindNode, UpdateRequest}

object RequestProcessor {

  def props = Props(classOf[RequestProcessor])

  case class Request(msgId: UUID, update: UpdateRequest)
  case class Response(msgId: UUID, updates: List[MindNode])

}

class RequestProcessor extends Actor {
  import RequestProcessor._

  def receive = initial

  def initial: Receive = {
    case Request(id, update) =>
      context become waitingForMaps(id, sender)
  }

  // TODO: TIMEOUT & LONG POLLING!

  def waitingForMaps(requestId: UUID, requester: ActorRef): Receive = {
    case _ =>
      ???
      requester ! Response(requestId, ???)
      context stop self
  }

}
