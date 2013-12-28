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

import android.app.Activity
import scala.util.Try
import android.view.View

trait ScalaActivity extends ViewHelper with MiscHelper with ConcurrencyHelper {
  this: Activity =>

  override def currentActivity = this

  def find[T <: View](id: Int) = safen(findViewById(id).asInstanceOf[T])

}
