package com.michalrus.helper

import com.actionbarsherlock.app.SherlockFragment

trait ScalaFragment extends SherlockFragment with ViewHelper {

  override protected lazy val resources = getResources

}
