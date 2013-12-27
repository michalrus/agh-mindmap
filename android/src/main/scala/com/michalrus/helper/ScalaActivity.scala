package com.michalrus.helper

import android.app.Activity
import scala.util.Try
import android.view.View

trait ScalaActivity extends ViewHelper with MiscHelper with ConcurrencyHelper {
  this: Activity =>

  override def currentActivity = this

  def find[T <: View](id: Int) = Try(Option(findViewById(id).asInstanceOf[T])).toOption.flatten

}
