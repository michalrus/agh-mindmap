package edu.agh.mindmapd.extensions

import scala.concurrent.duration._
import akka.actor.{Extension, ExtendedActorSystem, ExtensionKey}

object Settings extends ExtensionKey[Settings]

class Settings(system: ExtendedActorSystem) extends Extension {

  @inline private def cf = system.settings.config

  val hostname = cf getString "mindmapd.hostname"
  val port     = cf getInt "mindmapd.port"
  val timeout  = FiniteDuration(cf getMilliseconds "mindmapd.timeout", MILLISECONDS)

}
