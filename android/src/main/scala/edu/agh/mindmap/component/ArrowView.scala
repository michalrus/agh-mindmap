package edu.agh.mindmap.component

import android.view.View
import android.content.Context
import android.graphics.{Paint, Canvas, Color}

case class Arrow(x0: Int, y0: Int, x1: Int, y1: Int) {

  val boundingX: Int = x0 min x1
  val boundingY: Int = y0 min y1
  val boundingW: Int = (x0 - x1).abs
  val boundingH: Int = (y0 - y1).abs

  val innerX0 = x0 - boundingX
  val innerY0 = y0 - boundingY
  val innerX1 = x1 - boundingX
  val innerY1 = y1 - boundingY

}

class ArrowView(context: Context, var arrow: Arrow)
  extends View(context) {

  setClickable(false)
  setBackgroundColor(Color.TRANSPARENT)

  override def onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    val p = new Paint
    p setColor Color.LTGRAY
    p setAntiAlias true
    p setStrokeWidth 5f

    canvas drawLine(arrow.innerX0.toFloat, arrow.innerY0.toFloat, arrow.innerX1.toFloat, arrow.innerY1.toFloat, p)
  }

}
