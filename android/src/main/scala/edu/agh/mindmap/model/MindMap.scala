package edu.agh.mindmap.model

import java.util.UUID

class MindMap(val uuid: UUID = UUID.randomUUID,
              val root: MindNode = new MindNode(this, UUID.randomUUID, None, Some(""), false, None))

object MindMap {

  def findAll: List[MindMap] = ???

}
