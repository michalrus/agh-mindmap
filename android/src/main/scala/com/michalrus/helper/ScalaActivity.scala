package com.michalrus.helper

import android.app.Activity
import scala.concurrent.{ExecutionContext, future}
import ExecutionContext.Implicits.global

trait ScalaActivity extends Activity with ViewHelper {

  def find[T](id: Int) = findViewById(id).asInstanceOf[T]

  def laterOnUiThread(r: Runnable) {
    future {
      runOnUiThread(r)
    }
  }

}
