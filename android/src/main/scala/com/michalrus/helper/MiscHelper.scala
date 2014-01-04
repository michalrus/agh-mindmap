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

import android.util.Log
import scala.util.Try

object MiscHelper {

  def log(s: String) { Log i ("com.michalrus.helper", s); () }

  val rng = new MiscHelper.Random

  def safen[T](block: => T): Option[T] = Try(Option(block)).toOption.flatten

  class Random extends java.util.Random {
    /** Returns pseudo-random integer from range [a;b] */
    def nextInt(a: Int, b: Int): Int = {
      val c = a min b
      val d = a max b
      nextInt(d - c + 1) + c
    }
  }

}

trait MiscHelper {

  @inline def log(s: String) = MiscHelper log s

  @inline def safen[T](block: => T) = MiscHelper safen block

  val rng = MiscHelper.rng

  import language.implicitConversions

  implicit def quasiblockToRunnable(f: () => Unit) =
    new Runnable {
      def run() = f()
    }

}
