package edu.agh.mindmap.fragment

import com.michalrus.helper.ScalaFragment
import com.actionbarsherlock.app.SherlockFragment
import android.view.{ViewGroup, LayoutInflater}
import android.os.Bundle
import edu.agh.mindmap.R
import java.util.UUID
import edu.agh.mindmap.model.{MindNode, MindMap}
import edu.agh.mindmap.component.HorizontalScrollViewWithPropagation
import android.widget.{ImageView, RelativeLayout, ScrollView}

object MapFragment {
  val ArcShortRadius = 200
  val SubtreeMargin = 1
}

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

    laterOnUiThread { // Why later?...
      paintMap()
    }

    view
  }

  object Wrapper {
    import collection.mutable

    private val memo = new mutable.HashMap[UUID, Wrapper]

    def apply(node: MindNode): Wrapper = memo get node.uuid match {
      case Some(wr) => wr
      case _ =>
        val wr = new Wrapper(node)
        memo += node.uuid -> wr
        wr
    }
  }

  class Wrapper private(node: MindNode) {
    var x, y = 0
    private var _w, _h = 0
    private var _folded = false
    recalculate()

    def w = _w
    def h = _h
    def folded = _folded
    def folded_=(v: Boolean) = {
      _folded = v
      recalculate()
    }

    private def recalculate() {
      // TODO: use Wrapper(node.children. ...)
      // TODO: recalculate node's parents? isn't that a cycle?
      _w = ???
      _h = ???
    }
  }

  private def paintMap() {
    val paper = getView.find[RelativeLayout](R.id.paper)

    map foreach { map =>
      // TODO: Wrapper(map.root).folded = ...
    }

    object Rect {
      def random(minW: Int, maxW: Int, minH: Int, maxH: Int) = {
        val w = rng nextInt (dp2px(minW), dp2px(maxW))
        val h = rng nextInt (dp2px(minH), dp2px(maxH))
        Rect(w, h)
      }
    }
    case class Rect (w: Int, h: Int) {
      var x, y = 0
      def drawOn(vg: ViewGroup, color: Int = randomColor) {
        val iv = new ImageView(currentActivity)
        iv setBackgroundColor color
        val rp = new RelativeLayout.LayoutParams(dp2px(w), dp2px(h))
        rp.leftMargin = dp2px(x)
        rp.topMargin = dp2px(y)
        vg addView (iv, rp)
      }
    }

    val root = Rect random (70, 70, 30, 30)

    val n = rng nextInt (4, 6)
    val trees = (Vector fill n)(Rect random (50, 200, 30, 200))

    val (rtrees, ltrees) = if (trees.isEmpty) (Vector.empty, Vector.empty) else {
      val hsum = (0 /: trees)(_ + _.h) / 2.0
      val accH = (trees.tail scanLeft trees.head.h)(_ + _.h)
      val idx = ((accH map (h => (h - hsum).abs)).zipWithIndex minBy(_._1))._2
      trees splitAt idx
      // FIXME: why you always on the left :< why you not work
    }

    def hei(ts: Vector[Rect]) = (0 /: ts)(_ + _.h + 2 * MapFragment.SubtreeMargin)
    val rheight = hei(rtrees)
    val lheight = hei(ltrees)
    val height = rheight max lheight

    def pad(th: Int) = (height - th) / 2
    val rpad = pad(rheight)
    val lpad = pad(lheight)

    def position(trees: Vector[Rect], left: Boolean) {
      val sgn = if (left) -1 else 1
      val pad = if (left) lpad else rpad
      val x0, y0 = 0

      var y = y0 + pad
      trees foreach { t =>
        y += MapFragment.SubtreeMargin
        t.y = y
        y += t.h + MapFragment.SubtreeMargin

        val middleY = t.y + t.h / 2
        t.x = x0 - (if (left) t.w else 0)
        t.x += (sgn * math.sin(math.Pi * (middleY - y0) / height) * MapFragment.ArcShortRadius).toInt
      }
    }

    position(rtrees, left = false)
    position(ltrees, left = true)

    val minx = (ltrees map (_.x)).min
    val maxx = (rtrees map (t => t.x + t.w)).max

    val paperW = -minx + maxx + root.w
    val paperH = height

    def setPaperSize(w: Int, h: Int) {
      val lp = paper.getLayoutParams
      lp.width = dp2px(w)
      lp.height = dp2px(h)
      paper requestLayout()
    }

    setPaperSize(paperW, paperH)

    ltrees foreach (_.x += -minx)
    rtrees foreach (_.x += -minx + root.w)

    root.x = -minx
    root.y = paperH / 2 - root.h / 2
    root drawOn paper

    rtrees foreach (_ drawOn (paper, 0xffff0000))
    ltrees foreach (_ drawOn (paper, 0xff00ff00))

    paper setBackgroundColor 0xff0000ff
  }

}
