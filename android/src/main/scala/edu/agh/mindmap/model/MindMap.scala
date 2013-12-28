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

import java.util.UUID
import java.io.File
import edu.agh.mindmap.util.Importer

class MindMap private(val uuid: UUID,
                      val lastMod: Long,
                      isNew: Boolean) {

  val root = if (isNew) MindNode.createRootOf(this)
  else MindNode.findRootOf(this)

  private def commit() {
    // FIXME: save to local DB
    // FIXME: sync
    MindMap.db += uuid -> this
    ()
  }

}

object MindMap {

  import collection.mutable

  private val db = new mutable.HashMap[UUID, MindMap]  // FIXME: this should be a real database

  def create = {
    val map = new MindMap(UUID.randomUUID, (new java.util.Date).getTime, true)
    map commit()
    map
  }

  private def fillWithRandom() {
    var ord = 1
    def addRandomChildren(parent: MindNode, num: Int) = for (i <- 1 to num) {
      val n = MindNode createChildOf (parent, ord.toDouble)
      n.content = parent.content map (_ + "." + i)
      ord += 1
    }

    val a = create; a.root.content = Some("A")
    addRandomChildren(a.root, 9)
    a.root.children foreach { c => addRandomChildren(c, 5); c.children foreach { cc => addRandomChildren(cc, 2) } }

    val b = create; b.root.content = Some("B")
    addRandomChildren(b.root, 4)

    val c = create; c.root.content = Some("C")
    addRandomChildren(c.root, 6)

    val d = create; d.root.content = Some("D")
    addRandomChildren(d.root, 8)
  }

  def findAll: List[MindMap] = {
    if (db.isEmpty) fillWithRandom()

    db.values.toList
  }

  def findByUuid(uuid: UUID) = db get uuid

  def importFrom(file: File): Seq[MindMap] = Importer importFrom file

}
