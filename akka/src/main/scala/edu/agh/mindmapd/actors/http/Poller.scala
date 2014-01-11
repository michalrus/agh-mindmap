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

import akka.actor.{Cancellable, ActorRef, Props, Actor}
import edu.agh.mindmapd.json.PollResponse
import edu.agh.mindmapd.actors.{MindMap, MapsSupervisor}
import concurrent.duration._
import edu.agh.mindmapd.model.NodePlusMap
import edu.agh.mindmapd.extensions.Settings

object Poller {

  def props(mapsSupervisor: ActorRef) = Props(classOf[Poller], mapsSupervisor)

  case class Process(since: Long)

  private case object PollTimeout
  private case object MapsTimeout

}

class Poller(mapsSupervisor: ActorRef) extends Actor {
  import Poller._
  import context.dispatcher

  def receive = initial

  val settings = Settings(context.system)

  var requester: Option[ActorRef] = None
  var cancellables = List.empty[Cancellable]

  def initial: Receive = {
    case Process(since) =>
      mapsSupervisor ! MapsSupervisor.Subscribe(self, since)
      requester = Some(sender)
      cancellables ::= context.system.scheduler scheduleOnce (settings.timeout.poll, self, PollTimeout)
      context become waitingForFirst
  }

  def waitingForFirst: Receive = {
    case PollTimeout => complete(Nil)

    case MindMap.Changed(node) =>
      cancellables ::= context.system.scheduler scheduleOnce (settings.timeout.mapResponse, self, MapsTimeout)
      context become waitingForRest(node :: Nil)
  }

  def waitingForRest(data: List[NodePlusMap]): Receive = {
    case PollTimeout | MapsTimeout => complete(data)

    case MindMap.Changed(node) =>
      context become waitingForRest(node :: data)
  }

  def complete(data: List[NodePlusMap]) {
    cancellables foreach (_.cancel())
    requester foreach (_ ! PollResponse(data))
    mapsSupervisor ! MapsSupervisor.Unsubscribe(self)
    context stop self
  }

}
