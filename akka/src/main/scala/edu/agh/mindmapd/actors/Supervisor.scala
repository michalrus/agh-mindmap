package edu.agh.mindmapd.actors

import akka.actor.Actor
import concurrent.duration._

object Supervisor {
  case object Bye
}

class Supervisor extends Actor {
  import Supervisor._
  import context.dispatcher

  override def preStart() {
    println("Hello!")
    context.system.scheduler scheduleOnce (3.seconds, self, Bye)
    ()
  }

  def receive = {
    case Bye =>
      println("Bye!")
      context stop self
  }

}
