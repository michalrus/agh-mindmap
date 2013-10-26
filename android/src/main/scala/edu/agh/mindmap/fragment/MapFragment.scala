package edu.agh.mindmap.fragment

import com.michalrus.helper.ScalaFragment
import com.actionbarsherlock.app.SherlockFragment
import android.view.{ViewGroup, LayoutInflater}
import android.os.Bundle
import edu.agh.mindmap.R

class MapFragment extends SherlockFragment with ScalaFragment {

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, bundle: Bundle) = {
    val view = inflater.inflate(R.layout.map, container, false)

    val uuid = bundle getString "uuid"
    // FIXME

    view
  }

}