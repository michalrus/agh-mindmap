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

import akka.actor.{Props, ActorRef, Actor}
import edu.agh.mindmapd.actors.MindMap
import edu.agh.mindmapd.json.{UpdateResponse, UpdateRequest}

object Updater {

  def props = Props(classOf[Updater])

  case class Process(update: UpdateRequest, mindMap: ActorRef, completer: UpdateResponse => Unit)

}

class Updater extends Actor {
  import Updater._

  def receive = initial

  def initial: Receive = {
    case Process(update, mindMap, completer) =>
      mindMap ! MindMap.Update(update.nodes)
      context become waitingForMap(completer)
  }

  def waitingForMap(completer: UpdateResponse => Unit): Receive = {
    case MindMap.UpdateResult(success) =>
      completer(UpdateResponse(success))
  }

}
