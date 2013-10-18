package edu.agh.mindmap.model

import java.util.UUID

class MindMap private(val uuid: UUID,
                      val lastMod: Long,
                      isNew: Boolean) {

  val root = if (isNew) MindNode.create(this)
  else MindNode.findRootOf(this)

}

object MindMap {

  def create = {
    new MindMap(UUID.randomUUID, (new java.util.Date).getTime, true)
  }

  def findAll: List[MindMap] = {
    val a = create; a.root.content = Some("mapa A")
    val b = create; b.root.content = Some("mapa B")
    val c = create; c.root.content = Some("mapa C")

    a :: b :: c :: Nil
  }

  def importFrom(file: Nothing): MindMap = ???

}
