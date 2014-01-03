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
import com.michalrus.helper.MiscHelper
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

class MindMap private(val uuid: UUID,
                      val lastMod: Long,
                      isNew: Boolean) {

  val root: MindNode = (if (isNew) None else MindNode findRootOf this) getOrElse
    (MindNode createRootOf this)

  private def commit() {
    import DBHelper._

    val v = new ContentValues
    v put (CUuid, uuid.toString)

    MindMap.dbw insertWithOnConflict (TMap, null, v, SQLiteDatabase.CONFLICT_REPLACE)

    // FIXME: sync? But what?
    ()
  }

}

object MindMap extends DBUser {
  import DBHelper._
  import MiscHelper.safen

  def create = memo.synchronized {
    val map = new MindMap(UUID.randomUUID, (new java.util.Date).getTime, true)
    map commit()
    memo += map.uuid -> map
    map
  }

  def findAll: Vector[MindMap] = {
    val cur = dbr query (TMap, Array(CUuid), null, null, null, null, null)

    cur moveToFirst()
    var r = Vector.empty[MindMap]
    while (!cur.isAfterLast) {
      for {
        uuid <- safen(UUID fromString (cur getString 0))
      } r :+= new MindMap(uuid, 0, false)
      cur moveToNext()
    }

    r foreach (m => memo += m.uuid -> m)

    r
  }

  private var memo = Map.empty[UUID, MindMap]
  def findByUuid(uuid: UUID): Option[MindMap] = memo get uuid orElse {
    val cur = dbr query (TMap, Array(CUuid), s"$CUuid = ?", Array(uuid.toString), null, null, null)
    cur moveToFirst()

    val candidate = for {
      uuid <- safen(UUID fromString (cur getString 0))
    } yield new MindMap(uuid, 0, false)

    candidate foreach (m => memo += m.uuid -> m)

    candidate
  }

  def importFrom(file: File): Seq[MindMap] = Importer importFrom file

}
