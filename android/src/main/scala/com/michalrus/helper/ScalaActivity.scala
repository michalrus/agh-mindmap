package com.michalrus.helper

import android.app.Activity
import scala.util.Try

trait ScalaActivity extends ViewHelper with MiscHelper with ConcurrencyHelper {
  this: Activity =>

  override def currentActivity = this

  def find[T](id: Int) = Try(Option(findViewById(id).asInstanceOf[T])).toOption.flatten

}
