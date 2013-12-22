package edu.agh.mindmap.fragment

import com.michalrus.helper.ScalaFragment
import com.actionbarsherlock.app.SherlockFragment
import android.view.{View, ViewGroup, LayoutInflater}
import android.os.Bundle
import edu.agh.mindmap.R
import java.util.UUID
import edu.agh.mindmap.model.{MindNode, MindMap}
import edu.agh.mindmap.component.{Arrow, ArrowView, HorizontalScrollViewWithPropagation}
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

  trait Size { def w: Int; def h: Int }
  trait Position { def x: Int; def y: Int }

  object SubtreeWrapper {
    import collection.mutable

    private val memo = new mutable.HashMap[UUID, SubtreeWrapper]

    def apply(node: MindNode): SubtreeWrapper = memo get node.uuid getOrElse {
      val wr = new SubtreeWrapper(node)
      memo += node.uuid -> wr
      wr
    }

    def recalculateAllSizes(map: MindMap) = SubtreeWrapper(map.root).sizes recalculate()
  }

  class SubtreeWrapper private(mindNode: MindNode) {
    private var nodeView: Option[View] = None
    private var arrowView: Option[ArrowView] = None

    def isRoot = mindNode.map.root == mindNode

    private var _folded = false
    def folded = _folded
    def folded_=(v: Boolean) = {
      _folded = v
      SubtreeWrapper recalculateAllSizes mindNode.map
    }

    object positions {
      private var sy, sx, ny, nx = 0
      val subtree = new Position { def y = sy; def x = sx }
      val node    = new Position { def y = ny; def x = nx }

      def positionAt(x0: Int, y0: Int, left: Boolean) {
        sy = y0
        sx = x0 - (if (left) sizes.subtree.w else 0)
        ny = subtree.y + (sizes.subtree.h - sizes.node.h) / 2
        nx = subtree.x + (if (left) sizes.subtree.w - sizes.node.w else 0)

        // FIXME: position children
      }

      def repositionBy(dx: Int, dy: Int) {
        sx += dx
        sy += dy
        nx += dx
        ny += dy

        if (!isRoot) mindNode.children map (SubtreeWrapper(_)) foreach
          (_.positions repositionBy(dx, dy))
      }
    }

    object sizes {
      private var sw, sh, nw, nh = 0
      val subtree = new Size { def w = sw; def h = sh }
      val node    = new Size { def w = nw; def h = nh }

      private[SubtreeWrapper] def recalculate() {
        val kids = mindNode.children map (SubtreeWrapper(_))
        kids foreach (_.sizes.recalculate())

        nw = MapFragment.NodeW
        nh = MapFragment.NodeH

        if (isRoot) {
          sw = node.w
          sh = node.h
        } else {
          if (kids.nonEmpty) {
            val kidsH = (0 /: kids)(_ + _.sizes.subtree.h + 2 * MapFragment.NodeMarginTB)
            val kidsW = kids maxBy (_.sizes.subtree.w)

            // FIXME
          }

          sw = node.w // FIXME
          sh = node.h
        }
      }
    }

    def drawOn(vg: ViewGroup, color: Int = randomColor) {
      def updateLP(v: View, fun: RelativeLayout.LayoutParams => Unit) {
        v.getLayoutParams match {
          case rp: RelativeLayout.LayoutParams =>
            fun(rp)
            v requestLayout()
          case _ =>
        }
      }

      def updateNodeViewParams(rp: RelativeLayout.LayoutParams) {
        rp.width = dp2px(sizes.node.w)
        rp.height = dp2px(sizes.node.h)
        rp.leftMargin = dp2px(positions.node.x)
        rp.topMargin = dp2px(positions.node.y)
      }
      def updateNodeView(v: View) {
        val tf = v.find[TextView](R.id.content)
        tf.setBackgroundColor(color)
        mindNode.content foreach (tf setText _)
        v setBackgroundColor color
      }
      def updateArrowViewParams(a: Arrow)(rp: RelativeLayout.LayoutParams) {
        rp.width = a.boundingW
        rp.height = a.boundingH
        rp.leftMargin = a.boundingX
        rp.topMargin = a.boundingY
      }
      def properArrow: Option[Arrow] = mindNode.parent map (SubtreeWrapper(_)) map { parent =>
        val x0 = parent.positions.node.x + parent.sizes.node.w / 2
        val y0 = parent.positions.node.y + parent.sizes.node.h / 2
        val x1 = positions.node.x + sizes.node.h / 2
        val y1 = positions.node.y + sizes.node.h / 2
        Arrow(dp2px(x0), dp2px(y0), dp2px(x1), dp2px(y1))
      }

      nodeView match {
        case Some(nv) if !redrawEverything =>
          updateLP(nv, updateNodeViewParams)
          updateNodeView(nv)
          for (av <- arrowView; a <- properArrow) {
            av.arrow = a
            updateLP(av, updateArrowViewParams(a))
          }
        case _ => inflater foreach { inflater =>
          val nv = inflater inflate (R.layout.mind_node, null)

          nodeView foreach vg.removeView
          nodeView = Some(nv)

          val rp = new RelativeLayout.LayoutParams(0, 0)
          updateNodeViewParams(rp)
          vg addView (nv, rp)
          updateNodeView(nv)

          arrowView foreach vg.removeView
          arrowView = None
          for (a <- properArrow) {
            val av = new ArrowView(inflater.getContext, a)
            arrowView = Some(av)
            val rp = new RelativeLayout.LayoutParams(0, 0)
            updateArrowViewParams(a)(rp)
            vg addView (av, 0, rp)
          }
        }
      }

      // FIXME: draw children
    }
  }

  private def paintMap(paper: RelativeLayout) {
    map foreach { map =>
      val root = SubtreeWrapper(map.root)
      root.positions.positionAt(0, 0, left = false)

      SubtreeWrapper recalculateAllSizes map

      val trees = map.root.children map (SubtreeWrapper(_))

      val (ltrees, rtrees) = if (trees.isEmpty) (Vector.empty, Vector.empty) else {
        val hsum = (0 /: trees)(_ + _.sizes.subtree.h) / 2.0
        val accH = (trees.tail scanLeft trees.head.sizes.subtree.h)(_ + _.sizes.subtree.h)
        val idx = ((accH map (h => (h - hsum).abs)).zipWithIndex minBy(_._1))._2
        val (r, l) = trees splitAt (idx + 1)
        (l.reverse, r)
      }

      def hei(ts: Vector[SubtreeWrapper]) = (0 /: ts)(_ + _.sizes.subtree.h + 2 * MapFragment.SubtreeMargin)
      val rheight = hei(rtrees)
      val lheight = hei(ltrees)
      val height = List(rheight, lheight, root.sizes.subtree.h).max

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
          y += t.sizes.subtree.h + MapFragment.SubtreeMargin

          val middleY = ty + t.sizes.subtree.h / 2
          val tx = x0 + (sgn * math.sin(math.Pi * (middleY - y0) / height) * MapFragment.ArcShortRadius).toInt

          t.positions positionAt (tx, ty, left)
        }
      }

      position(rtrees, left = false)
      position(ltrees, left = true)

      val minx = if (ltrees.isEmpty) 0 else (ltrees map (_.positions.subtree.x)).min
      val maxx = if (rtrees.isEmpty) 0 else (rtrees map (t => t.positions.subtree.x + t.sizes.subtree.w)).max

      val paperW = -minx + maxx + root.sizes.subtree.w
      val paperH = height

      def setPaperSize(w: Int, h: Int) {
        val lp = paper.getLayoutParams
        lp.width = dp2px(w)
        lp.height = dp2px(h)
        paper requestLayout()
      }

      ltrees foreach (_.positions repositionBy (-minx, 0))
      rtrees foreach (_.positions repositionBy (-minx + root.sizes.subtree.w, 0))

      root.positions repositionBy (-minx, paperH / 2 - root.sizes.subtree.h / 2)

      val rects = root +: ltrees ++: rtrees

      // paper size and padding
      val pp = MapFragment.PaperPadding
      setPaperSize(paperW + 2 * pp, paperH + 2 * pp)
      rects foreach (_.positions repositionBy (pp, pp))

      rects foreach (_ drawOn paper)

      redrawEverything = false
    }
  }

}
