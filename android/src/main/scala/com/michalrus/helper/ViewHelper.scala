package com.michalrus.helper

import android.view.{ViewGroup, View}
import android.widget.{RelativeLayout, ImageView, Button}
import android.view.View.OnClickListener
import android.util.{TypedValue, Log}
import android.content.res.Resources
import android.graphics.Color
import android.app.Activity

object ViewHelper {

  def log(s: String) = Log.d("com.michalrus.helper", s)

}

trait ViewHelper {

  def rng: MiscHelper#Random

  protected def currentActivity: Activity

  protected def resources: Resources

  def dp2px (dp: Float) =
    TypedValue applyDimension (TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics)

  import language.implicitConversions

  def log(s: String) = ViewHelper.log(s)

  implicit def scalaizeView(v: View) = new ScalaView(v)
  class ScalaView(val v: View) {
    def find[T](id: Int) = v.findViewById(id).asInstanceOf[T]
  }

  implicit def scalaizeButton(b: Button) = new ScalaButton(b)
  class ScalaButton(val b: Button) {
    def onClick[A](f: => A) {
      b.setOnClickListener(new OnClickListener {
        def onClick(v: View) = f
      })
    }
  }

  def randomColor = {
    val hue = rng.nextFloat * 360
    val s, v = 1f
    Color.HSVToColor(Array(hue, s, v))
  }

  def randomRect(minW: Int, maxW: Int, minH: Int, maxH: Int) = {
    val w = rng nextInt (dp2px(minW).toInt, dp2px(maxW).toInt)
    val h = rng nextInt (dp2px(minH).toInt, dp2px(maxH).toInt)

    val params = new ViewGroup.MarginLayoutParams(w, h)

    val iv = new ImageView(currentActivity)
    iv setBackgroundColor randomColor
    iv setLayoutParams params
    iv
  }

}
