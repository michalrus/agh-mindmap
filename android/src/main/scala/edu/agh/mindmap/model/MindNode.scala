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
import edu.agh.mindmap.util._
import com.michalrus.android.helper.Helper.safen
import android.database.sqlite.SQLiteDatabase
import android.content.ContentValues
import scala.Some

class MindNode private(val uuid: UUID,
                       val map: MindMap,
                       initialParent: Option[UUID],
                       initialOrdering: Double,
                       initialContent: Option[String],
                       initialHasConflict: Boolean,
                       initialCloudTime: Option[Long]) extends Ordered[MindNode] {

  def isRoot = parent.isEmpty // rly? sufficient?
  def isRemoved = content.isEmpty

  def compare(that: MindNode): Int = {
    val ord = this.ordering compare that.ordering
    if (ord != 0) ord else this.uuid compareTo that.uuid
  }

  private var _ordering = initialOrdering
  def ordering = _ordering

  private var _hasConflict = initialHasConflict
  def hasConflict = _hasConflict

  private var _cloudTime = initialCloudTime
  def cloudTime = _cloudTime

  private var _parent = initialParent
  def parent = _parent
  def parent_=(v: Option[UUID]) = _parent.synchronized {
    if (_parent != v) {
      _parent = v
      touch()
    }
  }

  def touch() {
    _cloudTime = None
    commit()
  }

  private var _content = initialContent
  def content = _content.synchronized(_content)
  def content_=(v: Option[String]) = _content.synchronized {
    if (_content != v) {
      _content = v
      touch()
    }
  }

//  private val _children = new mutable.TreeSet[MindNode]
//  private var childrenRead = false // FIXME: I'm turning this off to always reread the local DB
  def childrenIncludingDeleted: Vector[MindNode] = { //_children.synchronized {
//    if (!childrenRead) {
      import DBHelper._

      val _children = new mutable.TreeSet[MindNode]

      val cur = MindNode.dbr query (TNode, Array(CUuid),
        s"$CParent = ?", Array(uuid.toString), ExplicitNull.String, ExplicitNull.String, ExplicitNull.String)
      val _ = cur moveToFirst()
      while (!cur.isAfterLast) {
        for {
          uuid <- safen(UUID fromString (cur getString 0))
          node <- MindNode findByUuid uuid
        } _children += node
        val _ = cur moveToNext()
      }

//      childrenRead = true
//    }
    _children.toVector
  }
  def children = childrenIncludingDeleted filterNot (_.isRemoved)

  def remove() = if (!isRoot) {
    MindNode deleteChildrenOf this

//    _children clear()
    content = None
    Synchronizer.update()
  }

  private def commit() {
    import DBHelper._

    val v = new ContentValues
    v put (CUuid, uuid.toString)
    v put (CMap, map.uuid.toString)
    v put (CParent, parent map (_.toString) getOrElse ExplicitNull.String)
    v put (COrdering, ordering)
    v put (CContent, content getOrElse ExplicitNull.String)
    v put (CHasConflict, Long box (if (hasConflict) 1L else 0L))
    v put (CCloudTime, cloudTime map Long.box getOrElse ExplicitNull.Long)

    val _ = MindNode.dbw insertWithOnConflict (TNode, CUuid, v, SQLiteDatabase.CONFLICT_REPLACE)
    MindNode.memo += uuid -> this
    Synchronizer.update()
  }

}

object MindNode extends DBUser {
  import DBHelper._

  private var memo = Map.empty[UUID, MindNode]
  def findByUuid(uuid: UUID): Option[MindNode] = memo get uuid orElse {
    val cur = dbr query (TNode, Array(CMap, CParent, COrdering, CContent, CHasConflict, CCloudTime),
      s"$CUuid = ?", Array(uuid.toString), ExplicitNull.String, ExplicitNull.String, ExplicitNull.String)
    val _ = cur moveToFirst()
    val candidate = for {
      map <- safen(UUID fromString (cur getString 0))
      mindMap <- MindMap findByUuid map
      parent = safen(UUID fromString (cur getString 1))
      ordering <- safen(cur getDouble 2)
      content = safen(cur getString 3)
      hasConflict <- safen(cur getLong 4)
      cloudTime = safen(cur getLong 5)
    } yield new MindNode(uuid, mindMap, parent, ordering, content, hasConflict != 0L, cloudTime)

    candidate foreach (n => memo += n.uuid -> n)

    candidate
  }

  def findRootOf(map: MindMap): Option[MindNode] = {
    val cur = dbr query (TNode, Array(CUuid),
      s"$CMap = ? AND $CParent IS NULL", Array(map.uuid.toString), ExplicitNull.String, ExplicitNull.String, ExplicitNull.String)
    val _ = cur moveToFirst()
    for {
      uuid <- safen(UUID fromString (cur getString 0))
      node <- findByUuid(uuid)
    } yield node
  }

  def createRootOf(map: MindMap, initialContent: String): MindNode = {
    findRootOf(map) getOrElse {
      val root = new MindNode(UUID.randomUUID, map, None, 0, Some(initialContent), false, None)
      root commit()
      root
    }
  }

  def findModified: Set[MindNode] = {
    var ns = Set.empty[MindNode]
    val cur = dbr query (TNode, Array(CUuid),
      s"$CCloudTime IS NULL", Array(), ExplicitNull.String, ExplicitNull.String, ExplicitNull.String)
    val _ = cur moveToFirst()
    while (!cur.isAfterLast) {
      for {
        uuid <- safen(UUID fromString (cur getString 0))
        node <- findByUuid(uuid)
      } ns += node
      val _ = cur moveToNext()
    }
    ns
  }

  def lastTimeWithAkka: Long = {
    val cur = dbr query (TPrefs, Array(CVal), s"$CKey = ?", Array(FLatestAkka), ExplicitNull.String, ExplicitNull.String, ExplicitNull.String)
    val _ = cur moveToFirst()
    safen(cur getLong 0) getOrElse 0L
  }

  private def setLastTimeWithAkka(to: Long) {
    if (to > lastTimeWithAkka) {
      val v = new ContentValues
      v put (CKey, FLatestAkka)
      v put (CVal, Long box to)
      val _ = dbw insertWithOnConflict (TPrefs, CKey, v, SQLiteDatabase.CONFLICT_REPLACE)
    }
  }

  def createChildOf(parent: MindNode, ordering: Double) = {
    val child = new MindNode(UUID.randomUUID, parent.map, Some(parent.uuid), ordering, Some(""), false, None)
    child commit()
//    parent._children += child
    child
  }

  private def deleteChildrenOf(node: MindNode) {
    import DBHelper._
    node.children foreach { ch =>
      deleteChildrenOf(ch)
      val _ = MindNode.dbw delete (TNode, s"$CUuid = ?", Array(ch.uuid.toString))
      MindNode.memo -= ch.uuid
    }
  }

  def touchAllOfTree(treeRoot: MindNode) {
    treeRoot.touch()
    treeRoot.children foreach touchAllOfTree
  }

  def mergeIn(mapUuid: UUID, jss: List[JsMindNode]) {
    setLastTimeWithAkka(jss.map(_.cloudTime).max)

    for (js <- jss) findByUuid(js.uuid) match {
      case Some(existing) =>
        if ((existing._cloudTime getOrElse 0L) < js.cloudTime) { // if it is a real update! _IMPORTANT_!
          val allowed: Boolean = (existing.content, js.content) match {
            case (Some(_), None) => // if akka wants to delete...
              // ... check if any children were updated and not yet synchronized
              def anyUpdatedInTree(r: MindNode): Boolean = {
                if (r.cloudTime.isEmpty) true
                else r.children exists anyUpdatedInTree
              }
              val isUpd = anyUpdatedInTree(existing)
              !isUpd
            case _ => true
          }

          if (allowed) {
            existing._cloudTime = Some(js.cloudTime)
            existing._parent = js.parent
            existing._hasConflict = js.hasConflict
            existing._ordering = js.ordering

            existing._content = js.content
            if (js.content.isEmpty) deleteChildrenOf(existing)

            existing commit()
          }
        }

      case None =>
        val map = MindMap findByUuid mapUuid getOrElse {
          MindMap createWith mapUuid
        }

        val node = new MindNode(js.uuid, map, js.parent, js.ordering,
          js.content, js.hasConflict, Some(js.cloudTime))

        node commit()
    }

    Refresher.refresh(mapUuid, refreshDrawing = true)
  }

}
