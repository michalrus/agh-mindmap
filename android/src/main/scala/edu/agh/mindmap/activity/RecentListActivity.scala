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

package edu.agh.mindmap.activity

import android.app.Activity
import android.os.Bundle
import edu.agh.mindmap.R

import android.widget.{ListView, TextView}
import edu.agh.mindmap.components.FunctionalListAdapter
import edu.agh.mindmap.helper.ScalaActivity

class RecentListActivity extends Activity with ScalaActivity {
  implicit val activity = this

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.recent_list)

    val data = ("abc" :: "def" :: "ghi" :: "jkl" :: "abc" :: "def" :: "ghi" :: "jkl" :: "abc" :: "def" :: "ghi" :: "jkl" :: Nil).toArray

    find[ListView](R.id.listview).setAdapter(new FunctionalListAdapter(this, R.layout.recent_list_item, data)((v, d: String) => {
      v.find[TextView](R.id.recent_list_item_name).setText(d)
      v.find[TextView](R.id.recent_list_item_detail).setText("Last mod: 1990/01/09 17:38:00")
    }))
  }
}
