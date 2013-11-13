package com.michalrus.helper

import com.actionbarsherlock.app.SherlockFragment

trait ScalaFragment extends SherlockFragment with ViewHelper with MiscHelper with ConcurrencyHelper {

  override protected lazy val currentActivity = getActivity

  override protected lazy val resources = getResources

}
