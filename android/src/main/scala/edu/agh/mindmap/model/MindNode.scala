package edu.agh.mindmap.model

import java.util.UUID

class MindNode private(val uuid: UUID,
                       val map: MindMap,
                       var parent: Option[MindNode],
                       var content: Option[String],
                       var hasConflict: Boolean,
                       var cloudTime: Option[Long]) {
}

object MindNode {

  def findRootOf(map: MindMap): MindNode = ???

  def create(map: MindMap) =
    new MindNode(UUID.randomUUID, map, None, None, false, None)

  def create(parent: MindNode) =
    new MindNode(UUID.randomUUID, parent.map, Some(parent), None, false, None)

}
