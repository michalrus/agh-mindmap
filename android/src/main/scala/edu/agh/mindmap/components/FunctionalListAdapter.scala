/*
 *   Copyright 2013 Katarzyna Szawan <kat.szwn@gmail.com>
 *       and Michał Rus <m@michalrus.com>
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package edu.agh.mindmap.components

import android.view.{LayoutInflater, ViewGroup, View}
import android.content.Context
import android.widget.{TextView, ArrayAdapter, ListAdapter}
import edu.agh.mindmap.R

class FunctionalListAdapter[T <: AnyRef](val context: Context, val itemLayout: Int, vals: Array[T])(val updateItem: (View, T) => Unit)
  extends ArrayAdapter[T](context, itemLayout, vals) {

  override def getView(position: Int, convertView: View, parent: ViewGroup) = {
    val view = Option(convertView) match {
      case Some(v) => v
      case _ => {
        val inf = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
        inf.inflate(R.layout.recent_list_item, parent, false)
      }
    }

    updateItem(view, vals(position))

    view
  }

}
