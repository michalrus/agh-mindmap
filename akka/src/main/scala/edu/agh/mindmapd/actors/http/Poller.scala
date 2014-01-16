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
import edu.agh.mindmapd.model.MindNode
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

  private case class State(requester: ActorRef, cancellables: List[Cancellable], data: List[MindNode])

  def initial: Receive = {
    case Process(since) =>
      mapsSupervisor ! MapsSupervisor.Subscribe(self, since)
      val tm = context.system.scheduler scheduleOnce (settings.timeout.poll, self, PollTimeout)
      context become waitingForFirst(State(
        requester = sender,
        cancellables = tm :: Nil,
        data = Nil))
  }

  def waitingForFirst(state: State): Receive = {
    case PollTimeout => complete(state)

    case MindMap.Changed(node) =>
      val tm = context.system.scheduler scheduleOnce(settings.timeout.mapResponse, self, MapsTimeout)
      context become waitingForRest(state copy(
        cancellables = tm :: state.cancellables,
        data = node :: state.data))
  }

  def waitingForRest(state: State): Receive = {
    case PollTimeout | MapsTimeout => complete(state)

    case MindMap.Changed(node) =>
      context become waitingForRest(state copy (
        data = node :: state.data))
  }

  def complete(state: State) {
    import state._
    cancellables foreach (_.cancel())
    requester ! PollResponse(data)
    mapsSupervisor ! MapsSupervisor.Unsubscribe(self)
    context stop self
  }

}
