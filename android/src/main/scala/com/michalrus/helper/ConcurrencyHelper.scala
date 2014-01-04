/*
 * Copyright 2013 Michał Rus <https://michalrus.com/>
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

package com.michalrus.helper

trait ConcurrencyHelper extends CurrentActivityProvider with MiscHelper {

  def laterOnUiThread(r: () => Unit) = delayOnUiThread(0)(r)

  def delayOnUiThread(ms: Long)(r: () => Unit) {
    import scala.concurrent.{ExecutionContext, future, blocking}
    import ExecutionContext.Implicits.global

    future { blocking {
      Thread sleep ms
      currentActivity runOnUiThread r
    }}
    ()
  }

}
