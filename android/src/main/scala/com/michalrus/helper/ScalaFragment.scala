package com.michalrus.helper

import com.actionbarsherlock.app.SherlockFragment

trait ScalaFragment extends ViewHelper with MiscHelper with ConcurrencyHelper {
  this: SherlockFragment =>

  override def currentActivity = getActivity

}
