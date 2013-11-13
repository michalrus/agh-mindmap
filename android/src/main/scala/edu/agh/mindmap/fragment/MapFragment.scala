package edu.agh.mindmap.fragment

import com.michalrus.helper.ScalaFragment
import com.actionbarsherlock.app.SherlockFragment
import android.view.{View, ViewGroup, LayoutInflater}
import android.os.Bundle
import edu.agh.mindmap.R
import java.util.UUID
import edu.agh.mindmap.model.{MindNode, MindMap}
import edu.agh.mindmap.component.HorizontalScrollViewWithPropagation
import android.widget.{RelativeLayout, ScrollView}

class MapFragment extends SherlockFragment with ScalaFragment {

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, bundle: Bundle) = {
    val view = inflater.inflate(R.layout.map, container, false)

    val hScroll = view.find[HorizontalScrollViewWithPropagation](R.id.hscroll)
    val vScroll = view.find[ScrollView](R.id.vscroll)
    hScroll.inner = vScroll

    val uuid = try {
      UUID.fromString(bundle getString "uuid")
    } catch {
      case _: Exception => new UUID(0, 0)
    }

    MindMap findByUuid uuid match {
      case Some(m) => paintMap(m)
      case _ =>
    }

    view
  }

  def paintMap(map: MindMap)(implicit view: View) {
    val paper = view.find[RelativeLayout](R.id.paper)

    // FIXME
  }

}
