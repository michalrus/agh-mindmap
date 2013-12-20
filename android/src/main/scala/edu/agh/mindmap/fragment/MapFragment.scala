package edu.agh.mindmap.fragment

import com.michalrus.helper.ScalaFragment
import com.actionbarsherlock.app.SherlockFragment
import android.view.{View, ViewGroup, LayoutInflater}
import android.os.Bundle
import edu.agh.mindmap.R
import java.util.UUID
import edu.agh.mindmap.model.{MindNode, MindMap}
import edu.agh.mindmap.component.HorizontalScrollViewWithPropagation
import android.widget.{TextView, RelativeLayout, ScrollView}

object MapFragment {
  val ArcShortRadius = 200
  val PaperPadding = 50
  val SubtreeMargin = 1
  val NodeMarginTB = 5
  val NodeMarginLR = 5

  val NodeH = 30
  val NodeW = 80
}

class MapFragment extends SherlockFragment with ScalaFragment {

  private var inflater: Option[LayoutInflater] = None
  private var map: Option[MindMap] = None

  private var redrawEverything = false

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, bundle: Bundle) = {
    this.inflater = Some(inflater)

    val view = inflater.inflate(R.layout.map, container, false)

    val hScroll = view.find[HorizontalScrollViewWithPropagation](R.id.hscroll)
    val vScroll = view.find[ScrollView](R.id.vscroll)
    hScroll.inner = vScroll

    val uuid = try {
      UUID.fromString(getArguments getString "uuid")
    } catch {
      case _: Exception => new UUID(0, 0)
    }

    map = MindMap findByUuid uuid

    redrawEverything = true // <m> I hate you, Android, for all this mutability!
    paintMap(view.find[RelativeLayout](R.id.paper))

    view
  }

  object SubtreeWrapper {
    import collection.mutable

    private val memo = new mutable.HashMap[UUID, SubtreeWrapper]

    def apply(node: MindNode): SubtreeWrapper = memo get node.uuid getOrElse {
      val wr = new SubtreeWrapper(node)
      memo += node.uuid -> wr
      wr
    }

    def recalculateAllSizes(map: MindMap) = SubtreeWrapper(map.root) recalculate()
  }

  class SubtreeWrapper private(node: MindNode) {
    private var _x, _y, _w, _h = 0
    private var _folded = false

    def isRoot = node.map.root == node

    def x = _x
    def y = _y
    def w = _w
    def h = _h

    def folded = _folded
    def folded_=(v: Boolean) = {
      _folded = v
      SubtreeWrapper recalculateAllSizes node.map
    }

    private def recalculate() {
      val kids = node.children map (SubtreeWrapper(_))
      kids foreach (_.recalculate())

      if (isRoot) {
        _w = MapFragment.NodeW
        _h = MapFragment.NodeH
      } else {
        if (kids.nonEmpty) {
          val kidsH = (0 /: kids)(_ + _.h + 2 * MapFragment.NodeMarginTB)
          val kidsW = kids maxBy (_.w)

//          ??? // FIXME
        }

        _w = MapFragment.NodeW// FIXME
        _h = MapFragment.NodeH
      }
    }

    def positionAt(x0: Int, y0: Int, left: Boolean) {
      _y = y0
      _x = x0 - (if (left) w else 0)

      // FIXME: position children
    }

    def repositionBy(dx: Int, dy: Int) {
      _x += dx
      _y += dy
      if (!isRoot) node.children map (SubtreeWrapper(_)) foreach (_ repositionBy(dx, dy))
    }

    private var nodeView: Option[View] = None

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

      nodeView match {
        case Some(v) if !redrawEverything =>
          v.getLayoutParams match {
            case rp: RelativeLayout.LayoutParams =>
              updateRp(rp)
              v requestLayout()
              updateV(v)
            case _ =>
          }
        case _ => inflater foreach { inflater =>
          val v = inflater inflate (R.layout.mind_node, null)
          nodeView = Some(v)

          val rp = new RelativeLayout.LayoutParams(dp2px(w), dp2px(h))
          updateRp(rp)
          vg addView (v, rp)
          updateV(v)
        }
      }

      // FIXME: draw children
    }
  }

  private def paintMap(paper: RelativeLayout) {
    map foreach { map =>
      val root = SubtreeWrapper(map.root)
      SubtreeWrapper recalculateAllSizes map

      val trees = map.root.children map (SubtreeWrapper(_))

      val (ltrees, rtrees) = if (trees.isEmpty) (Vector.empty, Vector.empty) else {
        val hsum = (0 /: trees)(_ + _.h) / 2.0
        val accH = (trees.tail scanLeft trees.head.h)(_ + _.h)
        val idx = ((accH map (h => (h - hsum).abs)).zipWithIndex minBy(_._1))._2
        val (r, l) = trees splitAt (idx + 1)
        (l.reverse, r)
      }

      def hei(ts: Vector[SubtreeWrapper]) = (0 /: ts)(_ + _.h + 2 * MapFragment.SubtreeMargin)
      val rheight = hei(rtrees)
      val lheight = hei(ltrees)
      val height = List(rheight, lheight, root.h).max

      def pad(th: Int) = (height - th) / 2
      val rpad = pad(rheight)
      val lpad = pad(lheight)

      def position(trees: Vector[SubtreeWrapper], left: Boolean) {
        val sgn = if (left) -1 else 1
        val pad = if (left) lpad else rpad
        val x0, y0 = 0

        var y = y0 + pad
        trees foreach { t =>
          y += MapFragment.SubtreeMargin
          val ty = y
          y += t.h + MapFragment.SubtreeMargin

          val middleY = ty + t.h / 2
          val tx = x0 + (sgn * math.sin(math.Pi * (middleY - y0) / height) * MapFragment.ArcShortRadius).toInt

          t positionAt (tx, ty, left)
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

      ltrees foreach (_ repositionBy (-minx, 0))
      rtrees foreach (_ repositionBy (-minx + root.w, 0))

      root repositionBy (-minx, paperH / 2 - root.h / 2)

      val rects = root +: ltrees ++: rtrees

      // paper size and padding
      val pp = MapFragment.PaperPadding
      setPaperSize(paperW + 2 * pp, paperH + 2 * pp)
      rects foreach (_ repositionBy (pp, pp))

      rects foreach (_ drawOn paper)

      redrawEverything = false
    }
  }

}
