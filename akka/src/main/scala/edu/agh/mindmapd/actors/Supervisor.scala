package edu.agh.mindmapd.actors

import akka.actor.Actor
import concurrent.duration._
import edu.agh.mindmapd.extensions.Settings

class Supervisor extends Actor {

  @inline def s = Settings(context.system)

  val http = context actorOf HttpService.props(s.hostname, s.port, s.timeout)

  def receive = Actor.emptyBehavior

}
