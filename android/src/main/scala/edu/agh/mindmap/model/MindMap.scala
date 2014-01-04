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
                      val lastMod: Long) {

  def root: Option[MindNode] = MindNode findRootOf this

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

  def create = {
    val map = createWith(uuid = UUID.randomUUID)
    MindNode createRootOf map
    map
  }

  private[model] def createWith(uuid: UUID) = memo.synchronized {
    val map = new MindMap(uuid, (new java.util.Date).getTime)
    map commit()
    memo += map.uuid -> map
    map
  }

  def findAll: Vector[MindMap] = {
    val cur = dbr query (TMap, Array(CUuid), null, null, null, null, null)

    cur moveToFirst()
    var uuids = Vector.empty[UUID]
    while (!cur.isAfterLast) {
      for {
        uuid <- safen(UUID fromString (cur getString 0))
      } uuids :+= uuid
      cur moveToNext()
    }

    uuids map { uuid =>
      findByUuid(uuid) getOrElse {
        val m = new MindMap(uuid, 0)
        memo += m.uuid -> m
        m
      }
    }
  }

  private var memo = Map.empty[UUID, MindMap]
  def findByUuid(uuid: UUID): Option[MindMap] = memo.synchronized { memo get uuid orElse {
    val cur = dbr query (TMap, Array(CUuid), s"$CUuid = ?", Array(uuid.toString), null, null, null)
    cur moveToFirst()

    val candidate = for {
      uuid <- safen(UUID fromString (cur getString 0))
    } yield new MindMap(uuid, 0)

    candidate foreach (m => memo += m.uuid -> m)

    candidate
  }}

  def importFrom(file: File): Seq[MindMap] = Importer importFrom file

}
