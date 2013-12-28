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
import com.michalrus.helper.MiscHelper.safen
import android.database.sqlite.SQLiteDatabase
import android.content.ContentValues

class MindNode private(val uuid: UUID,
                       val map: MindMap,
                       val parent: Option[MindNode],
                       val ordering: Double,
                       initialContent: Option[String],
                       val hasConflict: Boolean,
                       val cloudTime: Option[Long]) extends Ordered[MindNode] {

  def compare(that: MindNode): Int = {
    val ord = this.ordering compare that.ordering
    if (ord != 0) ord else this.uuid compareTo that.uuid
  }

  private var _content = initialContent
  def content = _content
  def content_=(v: Option[String]) = { _content = v; commit() }

  private val _children = new mutable.TreeSet[MindNode]
  private var childrenRead = false
  def childrenIncludingDeleted = {
    if (!childrenRead) {
      import DBHelper._

      val cur = MindNode.dbr query (TNode, Array(CUuid, COrdering, CContent, CHasConflict, CCloudTime),
        s"$CParent = ?", Array(uuid.toString), null, null, null)
      cur moveToFirst()
      while (!cur.isAfterLast) {
        for {
          uuid <- safen(UUID fromString (cur getString 0))
          ordering <- safen(cur getDouble 1)
          content = safen(cur getString 2)
          hasConflict <- safen(cur getLong 3)
          cloudTime = safen(cur getLong 4)
        } _children += new MindNode(uuid, map, Some(this), ordering, content, hasConflict != 0L, cloudTime)
        cur moveToNext()
      }

      childrenRead = true
    }
    _children.toVector
  }
  def children = childrenIncludingDeleted filter (_.content.isDefined)

  def remove() = if (map.root != this) {
    def deleteChildrenOf(node: MindNode) {
      import DBHelper._
      node.children foreach { ch =>
        deleteChildrenOf(ch)
        MindNode.dbw delete (TNode, s"$CUuid = ?", Array(ch.uuid.toString))
      }
    }
    deleteChildrenOf(this)

    _children clear()
    content = None
  }

  private def commit() {
    import DBHelper._

    val v = new ContentValues
    v put (CUuid, uuid.toString)
    v put (CMap, map.uuid.toString)
    v put (CParent, parent map (_.uuid.toString) getOrElse null)
    v put (COrdering, ordering)
    v put (CContent, content getOrElse null)
    v put (CHasConflict, Long box (if (hasConflict) 1L else 0L))
    cloudTime foreach (ct => v put (CCloudTime, Long box ct))

    MindNode.dbw insertWithOnConflict (TNode, null, v, SQLiteDatabase.CONFLICT_REPLACE)

    // FIXME: sync
    ()
  }

}

object MindNode extends DBUser {
  import DBHelper._

  def findRootOf(map: MindMap): Option[MindNode] = {
    val cur = dbr query (TNode, Array(CUuid, COrdering, CContent, CHasConflict, CCloudTime),
      s"$CMap = ? AND $CParent IS NULL", Array(map.uuid.toString), null, null, null)
    cur moveToFirst()
    for {
      uuid <- safen(UUID fromString (cur getString 0))
      ordering <- safen(cur getDouble 1)
      content = safen(cur getString 2)
      hasConflict <- safen(cur getLong 3)
      cloudTime = safen(cur getLong 4)
    } yield new MindNode(uuid, map, None, ordering, content, hasConflict != 0L, cloudTime)
  }

  private[model] def createRootOf(map: MindMap) = {
    val root = new MindNode(UUID.randomUUID, map, None, 0, None, false, None)
    root commit()
    root
  }

  def createChildOf(parent: MindNode, ordering: Double) = {
    val child = new MindNode(UUID.randomUUID, parent.map, Some(parent), ordering, Some(""), false, None)
    child commit()
    parent._children += child
    child
  }

}
