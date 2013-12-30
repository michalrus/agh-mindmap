package edu.agh.mindmapd.actors

import akka.actor.{Actor, Props}
import scala.concurrent.duration.FiniteDuration

object HttpService {
  def props(hostname: String, port: Int, timeout: FiniteDuration): Props =
    Props(classOf[HttpService], hostname, port, timeout)
}

class HttpService(hostname: String, port: Int, timeout: FiniteDuration) extends Actor {

  println(s"h: $hostname")
  println(s"p: $port")
  println(s"t: $timeout")
  context.system.shutdown()

  def receive = Actor.emptyBehavior

}
