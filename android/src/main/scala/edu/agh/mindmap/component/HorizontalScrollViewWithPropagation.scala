/*
 * Copyright 2013 Katarzyna Szawan <kat.szwn@gmail.com>
 *     and Michał Rus <https://michalrus.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.agh.mindmap.component

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import android.graphics.Matrix

class HorizontalScrollViewWithPropagation(context: Context, attrs: AttributeSet)
  extends HorizontalScrollView(context, attrs) {

  override def dispatchTouchEvent(ev: MotionEvent) = {
    for (inner <- inner) {
      val copy = MotionEvent obtain ev
      val m = new Matrix
      m setTranslate (getScrollX.toFloat, 0)
      copy transform m
      inner dispatchTouchEvent copy
    }
    onTouchEvent(ev)
    true
  }

  var inner: Option[ScrollView] = None

}
