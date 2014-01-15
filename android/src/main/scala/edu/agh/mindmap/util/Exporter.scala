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

package edu.agh.mindmap.util

import edu.agh.mindmap.model.MindMap
import android.content.Context
import android.widget.Toast
import edu.agh.mindmap.R

object Exporter {
  import com.michalrus.helper.MiscHelper.log

  def export(context: Context, map: MindMap) {
    val ctx = context.getApplicationContext
    val title = map.root flatMap (_.content) getOrElse ""
    log(s"exporting mind map ${map.uuid}") // FIXME
    Toast makeText (ctx, ctx getString (R.string.export_error, title), Toast.LENGTH_SHORT) show()
  }

}
