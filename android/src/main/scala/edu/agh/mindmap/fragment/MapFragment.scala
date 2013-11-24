package edu.agh.mindmap.fragment

import com.michalrus.helper.ScalaFragment
import com.actionbarsherlock.app.SherlockFragment
import android.view.{View, ViewGroup, LayoutInflater}
import android.os.Bundle
import edu.agh.mindmap.R
import java.util.UUID
import edu.agh.mindmap.model.{MindNode, MindMap}
import edu.agh.mindmap.component.HorizontalScrollViewWithPropagation
import android.widget.{TextView, ImageView, RelativeLayout, ScrollView}

object MapFragment {
  val ArcShortRadius = 200
  val PaperPadding = 50
  val SubtreeMargin = 1

  val NodeH = 30
  val NodeW = 80
}

class MapFragment extends SherlockFragment with ScalaFragment {

  private var inflater: Option[LayoutInflater] = None
  private var map: Option[MindMap] = None

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, bundle: Bundle) = {
    this.inflater = Some(inflater)

    val view = inflater.inflate(R.layout.map, container, false)

    val hScroll = view.find[HorizontalScrollViewWithPropagation](R.id.hscroll)
    val vScroll = view.find[ScrollView](R.id.vscroll)
    hScroll.inner = vScroll

    val uuid = try {
      UUID.fromString(getArguments getString "uuid")
    } catch {
      case e: Exception => {
        log(e.toString)
        new UUID(0, 0)
      }
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

    def isRoot = node.map.root == node
    def w = _w
    def h = _h
    def folded = _folded
    def folded_=(v: Boolean) = {
      _folded = v
      recalculate()
    }

    private def recalculate() =
      if (isRoot) {
        _w = MapFragment.NodeW
        _h = MapFragment.NodeH
      } else {
        // TODO: use Wrapper(node.children. ...)
        // TODO: recalculate node's parents? isn't that a cycle?
        _w = MapFragment.NodeW // FIXME
        _h = MapFragment.NodeH
      }

    private var view: Option[View] = None

    def drawOn(vg: ViewGroup, color: Int = randomColor) {
      def updateRp(rp: RelativeLayout.LayoutParams) {
        rp.width = dp2px(w)
        rp.height = dp2px(h)
        rp.leftMargin = dp2px(x)
        rp.topMargin = dp2px(y)
      }
      def updateV(v: View) {
        val tf = v.find[TextView](R.id.content)
        tf.setBackgroundColor(color)
        node.content foreach (tf setText _)
        v setBackgroundColor color
      }

      view match {
        case Some(v) =>
          v.getLayoutParams match {
            case rp: RelativeLayout.LayoutParams =>
              updateRp(rp)
              v requestLayout()
              updateV(v)
            case _ =>
          }
        case _ => inflater foreach { inflater =>
          val v = inflater inflate (R.layout.mind_node, null)
          view = Some(v)

          val rp = new RelativeLayout.LayoutParams(dp2px(w), dp2px(h))
          updateRp(rp)
          vg addView (v, rp)
          updateV(v)
        }
      }

    }
  }

  private def paintMap() {
    val paper = getView.find[RelativeLayout](R.id.paper)

    map foreach { map =>
      val root = Wrapper(map.root)

      val trees = map.root.children map (Wrapper(_))

      val (rtrees, ltrees) = if (trees.isEmpty) (Vector.empty, Vector.empty) else {
        val hsum = (0 /: trees)(_ + _.h) / 2.0
        val accH = (trees.tail scanLeft trees.head.h)(_ + _.h)
        val idx = ((accH map (h => (h - hsum).abs)).zipWithIndex minBy(_._1))._2
        trees splitAt (idx + 1)
      }

      def hei(ts: Vector[Wrapper]) = (0 /: ts)(_ + _.h + 2 * MapFragment.SubtreeMargin)
      val rheight = hei(rtrees)
      val lheight = hei(ltrees)
      val height = rheight max lheight max root.h

      def pad(th: Int) = (height - th) / 2
      val rpad = pad(rheight)
      val lpad = pad(lheight)

      def position(trees: Vector[Wrapper], left: Boolean) {
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

      val minx = if (ltrees.isEmpty) 0 else (ltrees map (_.x)).min
      val maxx = if (rtrees.isEmpty) 0 else (rtrees map (t => t.x + t.w)).max

      val paperW = -minx + maxx + root.w
      val paperH = height

      def setPaperSize(w: Int, h: Int) {
        val lp = paper.getLayoutParams
        lp.width = dp2px(w)
        lp.height = dp2px(h)
        paper requestLayout()
      }

      ltrees foreach (_.x += -minx)
      rtrees foreach (_.x += -minx + root.w)

      root.x = -minx
      root.y = paperH / 2 - root.h / 2

      val rects = root +: ltrees ++: rtrees

      // paper size and padding
      val pp = MapFragment.PaperPadding
      setPaperSize(paperW + 2 * pp, paperH + 2 * pp)
      rects foreach { t => t.x += pp; t.y += pp }

      rects foreach (_ drawOn paper)
    }
  }

}
