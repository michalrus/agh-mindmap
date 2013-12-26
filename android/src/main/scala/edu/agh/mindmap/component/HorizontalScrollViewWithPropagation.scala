package edu.agh.mindmap.component

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import android.support.v4.view.MotionEventCompat
import android.graphics.Matrix

class HorizontalScrollViewWithPropagation(context: Context, attrs: AttributeSet)
  extends HorizontalScrollView(context, attrs) {

  override def dispatchTouchEvent(ev: MotionEvent) = {
    if (inner.isDefined) {
      val copy = MotionEvent obtain ev
      val m = new Matrix
      m setTranslate (getScrollX.toFloat, 0)
      copy transform m
      inner.get dispatchTouchEvent copy
    }
    onTouchEvent(ev)
    true
  }

  var inner: Option[ScrollView] = None

}
