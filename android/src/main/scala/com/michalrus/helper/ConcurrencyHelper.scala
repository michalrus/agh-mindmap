package com.michalrus.helper

import android.app.Activity

trait ConcurrencyHelper {

  protected def currentActivity: Activity

  def laterOnUiThread(r: Runnable) = delayOnUiThread(0)(r)

  def delayOnUiThread(ms: Long)(r: Runnable) {
    import scala.concurrent.{ExecutionContext, future}
    import ExecutionContext.Implicits.global

    future {
      Thread sleep ms
      currentActivity runOnUiThread r
    }
    ()
  }

}
