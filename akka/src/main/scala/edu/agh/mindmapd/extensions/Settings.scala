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

package edu.agh.mindmapd.extensions

import scala.concurrent.duration._
import akka.actor.{Extension, ExtendedActorSystem, ExtensionKey}

object Settings extends ExtensionKey[Settings]

class Settings(system: ExtendedActorSystem) extends Extension {

  @inline private def cf = system.settings.config

  private def boolean(k: String) = cf getBoolean k
  private def string(k: String) = cf getString k
  private def int(k: String) = cf getInt k
  private def duration(k: String) = FiniteDuration(cf getMilliseconds k, MILLISECONDS)

  val isProduction = boolean("mindmapd.is-production")

  val hostname = string("mindmapd.hostname")
  val port     = int("mindmapd.port")

  object timeout {
    val poll = duration("mindmapd.timeout.poll")
    val mapResponse = duration("mindmapd.timeout.maps-response")
    val update = duration("mindmapd.timeout.update")
    val internalMessage = duration("mindmapd.timeout.internal-message")
  }

}
