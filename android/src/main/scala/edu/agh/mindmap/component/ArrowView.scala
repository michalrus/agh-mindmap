package edu.agh.mindmap.component

import android.view.View
import android.content.Context
import android.graphics.{Paint, Canvas, Color}

object Arrow {
  // how much should the bounding box be bigger than the (x0,y0,x1,y1) rectangle?
  val SafetyMargin = 10 // [px]
  val DisplayBoundingBox = false
}

case class Arrow(x0: Int, y0: Int, x1: Int, y1: Int) {
  import Arrow.{SafetyMargin => M}

  val boundingX = -M + (x0 min x1)
  val boundingY = -M + (y0 min y1)
  val boundingW = 2 * M + (x0 - x1).abs
  val boundingH = 2 * M + (y0 - y1).abs

  val innerX0 = x0 - boundingX
  val innerY0 = y0 - boundingY
  val innerX1 = x1 - boundingX
  val innerY1 = y1 - boundingY

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

    canvas drawLine(arrow.innerX0.toFloat, arrow.innerY0.toFloat, arrow.innerX1.toFloat, arrow.innerY1.toFloat, p)
  }

}
