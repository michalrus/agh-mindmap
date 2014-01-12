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

  private implicit class KeyOps(k: String) {
    @inline private def cf = system.settings.config
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
  }; timeout // create now

  object squeryl {
    val driver = "mindmapd.db.driver".cls
    val url = "mindmapd.db.url".str
    val user = "mindmapd.db.user".str
    val password = "mindmapd.db.password".str
  }; squeryl // create now

}
