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

import android.view.View
import android.widget.Button
import android.view.View.{OnLongClickListener, OnClickListener}
import android.util.TypedValue
import android.graphics.Color
import scala.util.Try

trait ViewHelper extends ViewHelperWithoutContext with CurrentActivityProvider {

  def dp2px (dp: Float): Float = try {
    TypedValue applyDimension (TypedValue.COMPLEX_UNIT_DIP, dp, currentActivity.getResources.getDisplayMetrics)
  } catch {
    case _: Throwable => 0f
  }

  def dp2px (dp: Int): Int = dp2px(dp.toFloat).toInt

}

trait ViewHelperWithoutContext {
  implicit class ScalaView(val v: View) {
    def find[T <: View](id: Int) = MiscHelper safen v.findViewById(id).asInstanceOf[T]

    def onClick(f: => Unit) = v setOnClickListener new OnClickListener {
      def onClick(v: View) = f
    }

    def onLongClick(f: => Boolean) = v setOnLongClickListener new OnLongClickListener {
      def onLongClick(v: View) = f
    }
  }

  def randomColor = {
    val hue = MiscHelper.rng.nextFloat * 360
    val s = .6f
    val v = 1f
    Color.HSVToColor(Array(hue, s, v))
  }

}
