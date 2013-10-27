package com.michalrus.helper

import android.view.View
import android.widget.Button
import android.view.View.OnClickListener
import android.util.Log

object ViewHelper {

  def log(s: String) = Log.d("com.michalrus.helper", s)

}

trait ViewHelper {

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

  implicit def blockToRunnable[A](f: => A) = new Runnable {
    def run() = f
  }

}
