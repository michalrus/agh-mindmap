package com.michalrus.helper

import android.app.Activity

trait ScalaActivity extends Activity with ViewHelper with MiscHelper {

  override protected lazy val resources = getResources

  def find[T](id: Int) = findViewById(id).asInstanceOf[T]

  def laterOnUiThread(r: Runnable) = delayOnUiThread(0)(r)

  def delayOnUiThread(ms: Long)(r: Runnable) {
    import scala.concurrent.{ExecutionContext, future}
    import ExecutionContext.Implicits.global

    future {
      Thread sleep ms
      runOnUiThread(r)
    }
  }

}
