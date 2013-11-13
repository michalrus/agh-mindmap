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
    val s = .6f
    val v = 1f
    Color.HSVToColor(Array(hue, s, v))
  }

}
