package edu.agh.mindmap.model

import scala.collection.mutable
import java.util.UUID

class MindNode private(val uuid: UUID,
                       val map: MindMap,
                       var parent: Option[MindNode],
                       var ordering: Double,
                       var content: Option[String],
                       var hasConflict: Boolean,
                       var cloudTime: Option[Long]) {

  def children = _children.toVector

  private val _children = new mutable.ArrayBuffer[MindNode]

}

object MindNode {

  def findRootOf(map: MindMap): MindNode = ???

  def createRootOf(map: MindMap, ordering: Double) =
    new MindNode(UUID.randomUUID, map, None, ordering, None, false, None)

  def createChildOf(parent: MindNode, ordering: Double) = {
    val child = new MindNode(UUID.randomUUID, parent.map, Some(parent), ordering, None, false, None)
    parent._children += child
    child
  }

}
