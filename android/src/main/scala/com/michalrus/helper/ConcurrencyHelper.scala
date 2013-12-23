package com.michalrus.helper

trait ConcurrencyHelper extends CurrentActivityProvider {

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
