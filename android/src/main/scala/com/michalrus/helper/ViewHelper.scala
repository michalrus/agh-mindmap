package com.michalrus.helper

import android.view.View
import android.widget.Button
import android.view.View.OnClickListener
import android.util.TypedValue
import android.graphics.Color
import scala.util.Try

trait ViewHelper extends ViewHelperWithoutContext with CurrentActivityProvider {

  def dp2px (dp: Float): Float =
    TypedValue applyDimension (TypedValue.COMPLEX_UNIT_DIP, dp, currentActivity.getResources.getDisplayMetrics)

  def dp2px (dp: Int): Int = dp2px(dp.toFloat).toInt

}

trait ViewHelperWithoutContext {
  import language.implicitConversions

  implicit def scalaizeView(v: View) = new ScalaView(v)
  class ScalaView(val v: View) {
    def find[T](id: Int) = Try(Option(v.findViewById(id).asInstanceOf[T])).toOption.flatten
  }

  implicit def scalaizeButton(b: Button) = new ScalaButton(b)
  class ScalaButton(val b: Button) {
    def onClick(f: => Unit) {
      b.setOnClickListener(new OnClickListener {
        def onClick(v: View) = f
      })
    }
  }

  def randomColor = {
    val hue = MiscHelper.rng.nextFloat * 360
    val s = .6f
    val v = 1f
    Color.HSVToColor(Array(hue, s, v))
  }

}
