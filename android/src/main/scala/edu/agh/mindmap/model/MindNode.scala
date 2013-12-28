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

package edu.agh.mindmap.model

import scala.collection.mutable
import java.util.UUID
import edu.agh.mindmap.util.DBHelper

class MindNode private(val uuid: UUID,
                       val map: MindMap,
                       val parent: Option[MindNode],
                       val ordering: Double,
                       initialContent: Option[String],
                       val hasConflict: Boolean,
                       val cloudTime: Option[Long]) {

  private var _content = initialContent
  def content = _content
  def content_=(v: Option[String]) = { _content = v; commit() }

  private val _children = new mutable.ArrayBuffer[MindNode]
  def children = _children.toVector

  def remove() = for (p <- parent) p._children -= this

  private def commit() {
    // FIXME: save to local db
    // FIXME: sync
  }

}

object MindNode extends DBUser {

  def findRootOf(map: MindMap): MindNode = {
    // FIXME
    ???
  }

  private[model] def createRootOf(map: MindMap) = {
    val root = new MindNode(UUID.randomUUID, map, None, 0, None, false, None)
    root commit()
    root
  }

  def createChildOf(parent: MindNode, ordering: Double) = {
    val child = new MindNode(UUID.randomUUID, parent.map, Some(parent), ordering, None, false, None)
    child commit()
    parent._children += child
    child
  }

}
