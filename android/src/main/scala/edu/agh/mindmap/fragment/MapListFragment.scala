package edu.agh.mindmap.fragment

import com.actionbarsherlock.app.SherlockFragment
import android.os.Bundle
import android.view.{View, ViewGroup, LayoutInflater}
import edu.agh.mindmap.R
import android.widget.{AdapterView, ArrayAdapter, TextView, ListView}
import com.michalrus.helper.ScalaFragment
import edu.agh.mindmap.model.MindMap
import android.widget.AdapterView.OnItemClickListener
import edu.agh.mindmap.activity.MainActivity
import android.content.Context

object MapListFragment {
  val ItemXml = R.layout.recent_list_item
}

class MapListFragment extends SherlockFragment with ScalaFragment {

  def addMaps(maps: Seq[MindMap]) {
    log("adding maps!")
    maps foreach {
      m =>
        log(";  " + m.root.content.getOrElse("?!"))
        adapter add m // FIXME: why doesn't this update the listView?
    }
  }

  private lazy val data = MindMap.findAll.toArray

  private lazy val inflater = getActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]

  private lazy val adapter = new ArrayAdapter(getActivity, MapListFragment.ItemXml, data) {
    override def getView(position: Int, convertView: View, parent: ViewGroup) = {
      val v = Option(convertView) match {
        case Some(x) => x
        case _ => inflater.inflate(MapListFragment.ItemXml, parent, false)
      }

      val map = data(position)

      v.find[TextView](R.id.recent_list_item_name).
        setText(map.root.content.getOrElse(""))
      v.find[TextView](R.id.recent_list_item_detail).
        setText(new java.util.Date(map.lastMod).toString)

      v
    }
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, bundle: Bundle) = {
    val view = inflater.inflate(R.layout.recent_list, container, false)

    val listView = view.find[ListView](R.id.listview)

    val mainActivity = getActivity match {
      case a: MainActivity => Some(a)
      case _ => None
    }

    listView setAdapter adapter

    listView.setOnItemClickListener(new OnItemClickListener {
      override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
        mainActivity foreach (_ viewMindMap data(position))
      }
    })

    view
  }

}
