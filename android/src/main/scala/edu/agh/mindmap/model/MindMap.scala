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
import edu.agh.mindmap.util.{DBHelper, Importer}

class MindMap private(val uuid: UUID,
                      val lastMod: Long,
                      isNew: Boolean) {

  val root = if (isNew) MindNode.createRootOf(this)
  else MindNode.findRootOf(this)

  private def commit() {
    // FIXME: save to local DB
    // FIXME: sync
  }

}

object MindMap extends DBUser {

  def create = {
    val map = new MindMap(UUID.randomUUID, (new java.util.Date).getTime, true)
    map commit()
    map
  }

  def findAll: Seq[MindMap] = {
    // FIXME
    Seq.empty
  }

  def findByUuid(uuid: UUID): Option[MindMap] = {
    // FIXME
    None
  }

  def importFrom(file: File): Seq[MindMap] = Importer importFrom file

}
