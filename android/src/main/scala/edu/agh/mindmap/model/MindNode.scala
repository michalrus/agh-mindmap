package edu.agh.mindmap.model

import scala.collection.mutable
import java.util.UUID

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

object MindNode {

  def findRootOf(map: MindMap): MindNode = ???

  private[model] def createRootOf(map: MindMap) =
    new MindNode(UUID.randomUUID, map, None, 0, None, false, None)

  def createChildOf(parent: MindNode, ordering: Double) = {
    val child = new MindNode(UUID.randomUUID, parent.map, Some(parent), ordering, None, false, None)
    parent._children += child
    child
  }

}
