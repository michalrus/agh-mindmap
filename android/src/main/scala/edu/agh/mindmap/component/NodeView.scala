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

import android.widget.{Button, EditText, FrameLayout}
import android.content.Context
import android.util.AttributeSet
import edu.agh.mindmap.R
import com.michalrus.android.helper.ViewHelperWithoutContext

class NodeView(context: Context, attrs: AttributeSet)
  extends FrameLayout(context, attrs) with ViewHelperWithoutContext {

  // Ad. `Option#get`: safe to throw here, crucial functionality
  lazy val content = this.find[EditText](R.id.content).fold { (throw new NoSuchElementException): EditText } { x => x }
  lazy val addButton = this.find[Button](R.id.add_button).fold { (throw new NoSuchElementException): Button } { x => x }

}
