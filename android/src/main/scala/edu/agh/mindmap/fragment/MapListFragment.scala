package edu.agh.mindmap.fragment

import com.actionbarsherlock.app.SherlockFragment
import android.os.Bundle
import android.view.{ViewGroup, LayoutInflater}
import edu.agh.mindmap.R
import android.widget.{TextView, ListView}
import edu.agh.mindmap.components.FunctionalListAdapter
import com.michalrus.helper.ScalaFragment
import edu.agh.mindmap.model.MindMap

class MapListFragment extends SherlockFragment with ScalaFragment {

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, bundle: Bundle) = {
    val view = inflater.inflate(R.layout.recent_list, container, false)

    val data = MindMap.findAll.toArray

    view.find[ListView](R.id.listview).setAdapter(new FunctionalListAdapter(getActivity, R.layout.recent_list_item, data)((v, map: MindMap) => {
      v.find[TextView](R.id.recent_list_item_name).setText(map.root.content.getOrElse(""))
      v.find[TextView](R.id.recent_list_item_detail).setText(new java.util.Date(map.lastMod).toString)
    }))

    view
  }

}
