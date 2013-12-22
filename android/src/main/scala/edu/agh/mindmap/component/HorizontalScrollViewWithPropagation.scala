package edu.agh.mindmap.component

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView
import android.widget.ScrollView

class HorizontalScrollViewWithPropagation(context: Context, attrs: AttributeSet)
  extends HorizontalScrollView(context, attrs) {

  override def onTouchEvent(event: MotionEvent) =
    super.onTouchEvent(event) | (inner != null && (inner onTouchEvent event))

  override def onInterceptTouchEvent(event: MotionEvent) =
    super.onInterceptTouchEvent(event) | (inner != null && (inner onTouchEvent event))

  var inner: ScrollView = null

}
