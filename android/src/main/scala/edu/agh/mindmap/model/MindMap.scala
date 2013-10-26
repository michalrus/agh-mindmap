package edu.agh.mindmap.model

import java.util.UUID
import java.io.File
import edu.agh.mindmap.util.Importer

class MindMap private(val uuid: UUID,
                      val lastMod: Long,
                      isNew: Boolean) {

  val root = if (isNew) MindNode.createRootOf(this)
  else MindNode.findRootOf(this)

}

object MindMap {

  import collection.mutable

  val db = new mutable.HashMap[UUID, MindMap]  // FIXME: this should be a real database

  def create = {
    val map = new MindMap(UUID.randomUUID, (new java.util.Date).getTime, true)
    db += map.uuid -> map
    map
  }

  def findAll: List[MindMap] = {
    val a = create; a.root.content = Some("mapa A")
    val b = create; b.root.content = Some("mapa B")
    val c = create; c.root.content = Some("mapa C")
    val d = create; d.root.content = Some("mapa D")

    a :: b :: c :: d :: Nil
  }

  def findByUuid(uuid: UUID) = db get uuid

  def importFrom(file: File): Seq[MindMap] = {
    val maps = Importer importFrom file

    maps foreach {
      m => db += m.uuid -> m
    }

    maps
  }

}
