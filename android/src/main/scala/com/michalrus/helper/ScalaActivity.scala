package com.michalrus.helper

import android.app.Activity

trait ScalaActivity extends Activity with ViewHelper with MiscHelper with ConcurrencyHelper {

  override protected lazy val currentActivity = this

  def find[T](id: Int) = findViewById(id).asInstanceOf[T]

}
