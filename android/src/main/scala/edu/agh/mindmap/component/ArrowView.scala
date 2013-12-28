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

package edu.agh.mindmap.component

import android.view.View
import android.content.Context
import android.graphics.{Paint, Canvas, Color}

object Arrow {
  // how much should the bounding box be bigger than the (x0,y0,x1,y1) rectangle?
  val SafetyMargin = 10 // [px]
  val DisplayBoundingBox = false
}

case class Arrow(x0: Int, y0: Int, throughX: Option[Int], x1: Int, y1: Int) {
  import Arrow.{SafetyMargin => M}

  val boundingX = -M + (x0 min x1)
  val boundingY = -M + (y0 min y1)
  val boundingW = 2 * M + (x0 - x1).abs
  val boundingH = 2 * M + (y0 - y1).abs

  def innerX(x: Int) = x - boundingX
  def innerY(y: Int) = y - boundingY

}

class ArrowView(context: Context, var arrow: Arrow)
  extends View(context) {

  setClickable(false)
  setBackgroundColor(if (Arrow.DisplayBoundingBox) Color.argb(0x11, 128, 0, 0) else Color.TRANSPARENT)

  override def onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    val p = new Paint
    p setColor Color.LTGRAY
    p setAntiAlias true
    p setStrokeWidth 5f

    def polyline(ps: (Int, Int)*) = if (ps.nonEmpty) {
      ps zip ps.tail foreach { case ((x0, y0), (x1, y1)) =>
        canvas drawLine(
          arrow.innerX(x0).toFloat,
          arrow.innerY(y0).toFloat,
          arrow.innerX(x1).toFloat,
          arrow.innerY(y1).toFloat, p)
      }
    }

    arrow.throughX match {
      case Some(throughX) => polyline (
        (arrow.x0, arrow.y0),
        (throughX, arrow.y0),
        (throughX, arrow.y1),
        (arrow.x1, arrow.y1)
      )
      case None => polyline(
        (arrow.x0, arrow.y0),
        (arrow.x1, arrow.y1)
      )
    }
  }

}
