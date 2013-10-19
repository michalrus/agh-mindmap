package edu.agh.mindmap.model

import scala.collection.mutable
import java.util.UUID

class MindNode private(val uuid: UUID,
                       val map: MindMap,
                       var parent: Option[MindNode],
                       var content: Option[String],
                       var hasConflict: Boolean,
                       var cloudTime: Option[Long]) {

  val children = new mutable.ArrayBuffer[MindNode]

}

object MindNode {

  def findRootOf(map: MindMap): MindNode = ???

  def create(map: MindMap) =
    new MindNode(UUID.randomUUID, map, None, None, false, None)

  def create(parent: MindNode) = {
    val child = new MindNode(UUID.randomUUID, parent.map, Some(parent), None, false, None)
    parent.children += child
    child
  }

}
