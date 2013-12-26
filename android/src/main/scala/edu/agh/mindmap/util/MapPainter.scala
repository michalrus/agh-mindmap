package edu.agh.mindmap.util

import edu.agh.mindmap.model.{MindMap, MindNode}
import android.view.{LayoutInflater, ViewGroup, View}
import java.util.UUID
import edu.agh.mindmap.component.{Arrow, ArrowView}
import android.widget.RelativeLayout

class MapPainter(dp2px: Int => Int,
                 nodeLayoutId: Int,
                 paperPadding: Int,
                 subtreeMargin: Int,
                 childHorizontalDistance: Int,
                 arcShortRadius: Int => Int,
                 nodeViewSize: MindNode => (Int, Int),
                 initializeNodeView: (MindNode, View) => Unit,
                 updateNodeView: (MindNode, View) => Unit) {

  private object SubtreeWrapper {
    import collection.mutable

    private val memo = new mutable.HashMap[UUID, SubtreeWrapper]

    def apply(node: MindNode): SubtreeWrapper = memo get node.uuid getOrElse {
      val wr = new SubtreeWrapper(node)
      memo += node.uuid -> wr
      wr
    }

    def recalculateAllSizes(map: MindMap) = SubtreeWrapper(map.root).sizes recalculate()
  }

  private class SubtreeWrapper private(val mindNode: MindNode) {
    trait Size { def w: Int; def h: Int }
    trait Position { def x: Int; def y: Int }

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
      private var tx: Option[Int] = None
      val subtree = new Position { def y = sy; def x = sx }
      val node    = new Position { def y = ny; def x = nx }
      def arrowThroughX = tx

      def positionAt(x0: Int, y0: Int, left: Boolean) {
        sy = y0
        sx = x0 - (if (!isRoot && left) sizes.subtree.w else 0)
        ny = subtree.y + (sizes.subtree.h - sizes.node.h) / 2
        nx = subtree.x + (if (!isRoot && left) sizes.subtree.w - sizes.node.w else 0)

        tx = None

        if (!isRoot) {
          val kids = mindNode.children map (SubtreeWrapper(_))

          val cx = if (left) node.x - childHorizontalDistance
          else node.x + sizes.node.w + childHorizontalDistance

          var dy = subtreeMargin
          kids foreach { child =>
            val cy = subtree.y + dy
            dy += child.sizes.subtree.h + subtreeMargin

            child.positions positionAt(cx, cy, left)

            child.positions.tx = Some((if (left) Array(node.x, child.positions.node.x + child.sizes.node.w)
            else Array(node.x + sizes.node.w, child.positions.node.x)).sum / 2)
          }
        }
      }

      def repositionBy(dx: Int, dy: Int) {
        sx += dx
        sy += dy
        nx += dx
        ny += dy
        tx = tx map (_ + dx)

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

        val tmp = nodeViewSize(mindNode)
        nw = tmp._1
        nh = tmp._2

        if (isRoot) {
          sw = node.w
          sh = node.h
        } else if (kids.nonEmpty) {
          val kidsH = (0 /: kids)(_ + _.sizes.subtree.h + subtreeMargin) + subtreeMargin
          val kidsW = (kids map (_.sizes.subtree.w)).max

          sw = kidsW + childHorizontalDistance + node.w
          sh = List(node.h, kidsH).max
        }
        else {
          sw = node.w
          sh = node.h
        }
      }
    }

    def drawOn(vg: ViewGroup, redrawEverything: Boolean, inflater: LayoutInflater) {
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
      def updateArrowViewParams(a: Arrow)(rp: RelativeLayout.LayoutParams) {
        rp.width = a.boundingW
        rp.height = a.boundingH
        rp.leftMargin = a.boundingX
        rp.topMargin = a.boundingY
      }
      def properArrow: Option[Arrow] = mindNode.parent map (SubtreeWrapper(_)) map { parent =>
        val x0 = parent.positions.node.x + parent.sizes.node.w / 2
        val y0 = parent.positions.node.y + parent.sizes.node.h / 2
        val x1 = positions.node.x + sizes.node.w / 2
        val y1 = positions.node.y + sizes.node.h / 2
        Arrow(dp2px(x0), dp2px(y0), positions.arrowThroughX map dp2px, dp2px(x1), dp2px(y1))
      }

      nodeView match {
        case Some(nv) if !redrawEverything =>
          updateLP(nv, updateNodeViewParams)
          updateNodeView(mindNode, nv)
          for (av <- arrowView; a <- properArrow) {
            av.arrow = a
            updateLP(av, updateArrowViewParams(a))
          }
        case _ =>
          val nv = inflater inflate (nodeLayoutId, null)

          nodeView foreach vg.removeView
          nodeView = Some(nv)

          val rp = new RelativeLayout.LayoutParams(0, 0)
          updateNodeViewParams(rp)
          vg addView (nv, rp)
          initializeNodeView(mindNode, nv)
          updateNodeView(mindNode, nv)

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

      mindNode.children map (SubtreeWrapper(_)) foreach (_ drawOn (vg, redrawEverything, inflater))
    }
  }

  private var cachedMap: Option[MindMap] = None
  private var cachedInflater: Option[LayoutInflater] = None
  private var cachedPaper: Option[RelativeLayout] = None

  def paintMap(map: MindMap, paper: RelativeLayout, inflater: LayoutInflater) {
    cachedMap = Some(map)
    cachedInflater = Some(inflater)
    cachedPaper = Some(paper)

    repaintMap(redrawEverything = true)
  }

  private def repaintMap(redrawEverything: Boolean) = for {
    map <- cachedMap
    inflater <- cachedInflater
    paper <- cachedPaper
  } {

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

    def hei(ts: Vector[SubtreeWrapper]) = (0 /: ts)(_ + _.sizes.subtree.h + 2 * subtreeMargin)
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
        y += subtreeMargin
        val ty = y
        y += t.sizes.subtree.h + subtreeMargin

        val middleY = ty + t.sizes.subtree.h / 2
        val tx = x0 + (sgn * math.sin(math.Pi * (middleY - y0) / height) *
          arcShortRadius(root.mindNode.children.length)).toInt

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
    val pp = paperPadding
    setPaperSize(paperW + 2 * pp, paperH + 2 * pp)
    rects foreach (_.positions repositionBy (pp, pp))

    rects foreach (_ drawOn (paper, redrawEverything, inflater))
  }

}
