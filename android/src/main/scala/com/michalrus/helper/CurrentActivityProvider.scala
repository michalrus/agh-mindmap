package com.michalrus.helper

import android.app.Activity

trait CurrentActivityProvider {
  
  def currentActivity: Activity

}
