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
import edu.agh.mindmapd.storage.SquerylStorage
import edu.agh.mindmapd.extensions.Settings

object MapsSupervisor {

  case class Find(uuid: UUID)
  case class Subscribe(whom: ActorRef, since: Long)
  case class Unsubscribe(whom: ActorRef)

  def props = Props(classOf[MapsSupervisor])

}

class MapsSupervisor extends Actor {
  import MapsSupervisor._

  var subscribers = Map.empty[ActorRef, Long]

  // create MindMap actors for mind maps already existing in Storage
  SquerylStorage allMaps Settings(context.system) map refFor

  def receive = {
    case Find(uuid) => sender ! refFor(uuid)

    case Subscribe(whom, since) =>
      subscribers += whom -> since
      context.children foreach (_ ! MindMap.Subscribe(whom, since))

    case Unsubscribe(whom) =>
      subscribers -= whom
      context.children foreach (_ ! MindMap.Unsubscribe(whom))
  }

  def refFor(uuid: UUID): ActorRef = {
    val s = uuid.toString
    context child s match {
      case Some(ref) => ref
      case _ =>
        val ref = context actorOf (MindMap.props(uuid), s)
        subscribers foreach { case (whom, since) =>
          ref ! MindMap.Subscribe(whom, since)
        }
        ref
    }
  }

}
