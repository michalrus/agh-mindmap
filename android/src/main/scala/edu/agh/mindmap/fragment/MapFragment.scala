package edu.agh.mindmap.fragment

import com.michalrus.helper.ScalaFragment
import com.actionbarsherlock.app.SherlockFragment
import android.view.{ViewGroup, LayoutInflater}
import android.os.Bundle
import edu.agh.mindmap.R
import java.util.UUID
import edu.agh.mindmap.model.MindMap
import edu.agh.mindmap.component.HorizontalScrollViewWithPropagation
import android.widget.{ImageView, RelativeLayout, ScrollView}

class MapFragment extends SherlockFragment with ScalaFragment {

  private var inflater: Option[LayoutInflater] = None
  private var map: Option[MindMap] = None

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

    map = MindMap findByUuid uuid

    laterOnUiThread {
      paintMap()
    }

    view
  }

  private def paintMap() {
    val paper = getView.find[RelativeLayout](R.id.paper)

    // FIXME really

    val n = rng nextInt (4, 7)

    case class Rect(w: Int, h: Int) {
      var x, y = 0
    }

    def drawRect(r: Rect) {
      val iv = new ImageView(currentActivity)
      iv setBackgroundColor randomColor

      val rp = new RelativeLayout.LayoutParams(r.w, r.h)
      rp.leftMargin = dp2px(r.x).toInt
      rp.topMargin = dp2px(r.y).toInt

      paper addView (iv, rp)
    }

    def randomRect(minW: Int, maxW: Int, minH: Int, maxH: Int) = {
      val w = rng nextInt (dp2px(minW).toInt, dp2px(maxW).toInt)
      val h = rng nextInt (dp2px(minH).toInt, dp2px(maxH).toInt)
      Rect(w, h)
    }

    val root = randomRect(70, 70, 30, 30)

    // TODO: simulate positions
    root.x = 20
    root.y = 40

    drawRect(root)
  }

}
