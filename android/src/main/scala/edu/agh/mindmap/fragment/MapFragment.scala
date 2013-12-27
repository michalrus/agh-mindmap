package edu.agh.mindmap.fragment

import com.michalrus.helper.ScalaFragment
import com.actionbarsherlock.app.SherlockFragment
import android.view._
import android.os.Bundle
import edu.agh.mindmap.R
import java.util.UUID
import edu.agh.mindmap.model.{MindNode, MindMap}
import edu.agh.mindmap.component.{NodeView, HorizontalScrollViewWithPropagation}
import android.widget._
import edu.agh.mindmap.util.MapPainter
import scala.util.Try
import android.content.Context
import android.view.inputmethod.{EditorInfo, InputMethodManager}
import android.widget.TextView.OnEditorActionListener

class MapFragment extends SherlockFragment with ScalaFragment {
  private val painter = new MapPainter(
    dp2px,
    nodeLayoutId = R.layout.mind_node,
    paperPadding = 20, // [dp]
    subtreeMargin = 5, // [dp]
    childHorizontalDistance = 50, // [dp]
    arcShortRadius,
    nodeViewSize,
    initializeNodeView,
    updateNodeView
  )

  private lazy val dummyFocus = getView.find[View](R.id.dummy_focus)
  private lazy val inputManager = safen(getActivity.getSystemService(Context.INPUT_METHOD_SERVICE).asInstanceOf[InputMethodManager])

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
      paper onClick defocus
      painter paint (map, paper, inflater)
    }

    view
  }

  /**
   * Initialize inflated `R.layout.mind_node` view (event listeners and stuff).
   * @param node A node from the model.
   * @param v An inflated `R.layout.mind_node`.
   */
  def initializeNodeView(node: MindNode, v: NodeView) {
    v.addButton onClick addChildTo(node)
    v.content onLongClick removeNode(node)
    v.content setBackgroundColor randomColor

    v.content setOnEditorActionListener new OnEditorActionListener {
      def onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean =
        actionId == EditorInfo.IME_ACTION_DONE && { defocus(); true }
    }
  }

  /**
   * Update `R.layout.mind_node` view with accordance to the model.
   * @param node A node from the model.
   * @param v An inflated `R.layout.mind_node`.
   */
  def updateNodeView(node: MindNode, v: NodeView) = {
    val cnt = node.content getOrElse ""
    if (v.content.getText.toString != cnt) v.content setText cnt
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

  def focusOn(t: EditText) {
    t requestFocus()
    for (imm <- inputManager) laterOnUiThread { imm showSoftInput (t, 0); () }
  }

  def defocus() = for (dummy <- dummyFocus) {
    dummy requestFocus()
    for (imm <- inputManager) laterOnUiThread { imm hideSoftInputFromWindow (dummy.getWindowToken, 0); () }
  }

  def addChildTo(node: MindNode) {
    val ord = if (node.children.isEmpty) 0 else (node.children map (_.ordering)).max
    val newNode = MindNode createChildOf (node, ord + 10)
    painter repaint()

    for {
      v <- painter viewFor newNode
      text <- v.find[EditText](R.id.content)
    } focusOn(text)
  }

  def removeNode(node: MindNode) = {
    node remove()
    painter repaint()
    true
  }

}
