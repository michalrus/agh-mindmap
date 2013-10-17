package edu.agh.mindmap.fragment

import com.actionbarsherlock.app.SherlockFragment
import android.os.Bundle
import android.view.{View, ViewGroup, LayoutInflater}
import edu.agh.mindmap.R
import android.widget.{ArrayAdapter, TextView, ListView}
import com.michalrus.helper.ScalaFragment
import edu.agh.mindmap.model.MindMap

class MapListFragment extends SherlockFragment with ScalaFragment {

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, bundle: Bundle) = {
    val view = inflater.inflate(R.layout.recent_list, container, false)

    val data = MindMap.findAll.toArray

    val itemXml = R.layout.recent_list_item

    view.find[ListView](R.id.listview).setAdapter(new ArrayAdapter(getActivity, itemXml, data) {
      override def getView(position: Int, convertView: View, parent: ViewGroup) = {
        val v = Option(convertView) match {
          case Some(x) => x
          case _ => inflater.inflate(itemXml, parent, false)
        }

        val map = data(position)

        v.find[TextView](R.id.recent_list_item_name).
          setText(map.root.content.getOrElse(""))
        v.find[TextView](R.id.recent_list_item_detail).
          setText(new java.util.Date(map.lastMod).toString)

        v
      }
    })

    view
  }

}
