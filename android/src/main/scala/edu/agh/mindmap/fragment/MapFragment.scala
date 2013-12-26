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
import edu.agh.mindmap.util.MapPainter
import scala.util.Try

class MapFragment extends SherlockFragment with ScalaFragment {
  private val painter = new MapPainter(
    dp2px,
    nodeLayoutId = R.layout.mind_node,
    paperPadding = 20, // [dp]
    subtreeMargin = 5, // [dp]
    childHorizontalDistance = 50, // [dp]
    arcShortRadius,
    nodeViewSize,
    updateNodeView
  )

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, bundle: Bundle) = {
    val view = inflater.inflate(R.layout.map, container, false)

    for {
      hScroll <- view.find[HorizontalScrollViewWithPropagation](R.id.hscroll)
      vScroll = view.find[ScrollView](R.id.vscroll)
      uuid <- Try(UUID fromString (getArguments getString "uuid"))
      map <- MindMap findByUuid uuid
      paper <- view.find[RelativeLayout](R.id.paper)
    } {
      hScroll.inner = vScroll
      painter paintMap (map, paper, inflater)
    }

    view
  }

  /**
   * Update `R.layout.mind_node` view with accordance to the model.
   * @param node A node from the model.
   * @param view An inflated `R.layout.mind_node`.
   */
  def updateNodeView(node: MindNode, view: View) =
    for {
      tf <- view.find[TextView](R.id.content)
    } {
      tf setBackgroundColor randomColor
      tf setText (node.content getOrElse "")
    }

  def arcShortRadius(numChildren: Int): Int = // [dp]
    if (numChildren <= 3) 50
    else if (numChildren <= 8) 100
    else 150

  /**
   * Provide the size of a to-be-inflated `R.layout.mind_node`.
   * @param node A node from the model.
   * @return `(width, height)` in DP
   */
  def nodeViewSize(node: MindNode): (Int, Int) =
    (120, 30)

}
