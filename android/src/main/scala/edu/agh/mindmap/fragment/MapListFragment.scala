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
    maps foreach (m => adapter add m)
    adapter notifyDataSetChanged()
  }

  private lazy val adapter = new ArrayAdapter(getActivity, MapListFragment.ItemXml, collection.JavaConversions.bufferAsJavaList(MindMap.findAll.toBuffer)) {
    private lazy val inflater = getActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]

    override def getView(position: Int, convertView: View, parent: ViewGroup) = {
      val v = Option(convertView) match {
        case Some(x) => x
        case _ => inflater.inflate(MapListFragment.ItemXml, parent, false)
      }

      val map = getItem(position)

      v.find[TextView](R.id.recent_list_item_name).
        setText(map.root.content.getOrElse(""))
      v.find[TextView](R.id.recent_list_item_detail).
        setText(new java.util.Date(map.lastMod).toString)

      v
    }
  }

  def withMainActivity(f: MainActivity => Unit) {
    getActivity match {
      case a: MainActivity => f(a)
      case _ =>
    }
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, bundle: Bundle) = {
    val view = inflater.inflate(R.layout.recent_list, container, false)

    val listView = view.find[ListView](R.id.listview)

    listView setAdapter adapter

    listView setOnItemClickListener new OnItemClickListener {
      override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
        val map = adapter getItem position
        withMainActivity (_ viewMindMap map)
      }
    }

    view
  }

}
