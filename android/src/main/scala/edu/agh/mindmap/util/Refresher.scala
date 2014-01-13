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

import java.util.UUID
import edu.agh.mindmap.activity.MainActivity
import com.michalrus.helper.MiscHelper
import edu.agh.mindmap.model.MindMap

object Refresher extends MiscHelper {

  case class State(online: Boolean)

  var mainActivity: Option[MainActivity] = None

  def refresh(mindMapUuid: UUID, refreshDrawing: Boolean) {
    for {
      act <- mainActivity
      mindMap <- MindMap findByUuid mindMapUuid
      rootNode <- mindMap.root
      title <- rootNode.content
    } act onMapChanged (mindMap, title, refreshDrawing)
  }

  def setState(state: State) {
    for (act <- mainActivity) act setState state
  }

}
