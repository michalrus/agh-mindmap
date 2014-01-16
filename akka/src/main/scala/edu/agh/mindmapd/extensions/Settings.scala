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
import akka.actor.{ExtensionIdProvider, ExtensionId, Extension, ExtendedActorSystem}
import com.typesafe.config.Config

class Settings(cf: Config) extends Extension {

  private implicit class KeyOps(k: String) {
    def boo = cf getBoolean k
    def dur = FiniteDuration(cf getMilliseconds k, MILLISECONDS)
    def int = cf getInt k
    def str = cf getString k
    def cls = Class forName (cf getString k)
  }

  val isProduction = "mindmapd.is-production".boo

  val hostname = "mindmapd.hostname".str
  val port = "mindmapd.port".int

  object timeout {
    val poll = "mindmapd.timeout.poll".dur
    val mapResponse = "mindmapd.timeout.maps-response".dur
    val update = "mindmapd.timeout.update".dur
    val internalMessage = "mindmapd.timeout.internal-message".dur
  }
  val _ = timeout // create now

  object squeryl {
    val driver = "mindmapd.squeryl.driver".cls
    val url = "mindmapd.squeryl.url".str
    val user = "mindmapd.squeryl.user".str
    val password = "mindmapd.squeryl.password".str
  }
  val __ = squeryl // create now

}

object Settings extends ExtensionId[Settings] with ExtensionIdProvider {
  def lookup() = Settings
  def createExtension(system: ExtendedActorSystem) = new Settings(system.settings.config)
}
