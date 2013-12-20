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

  private val db = new mutable.HashMap[UUID, MindMap]  // FIXME: this should be a real database

  def create = {
    val map = new MindMap(UUID.randomUUID, (new java.util.Date).getTime, true)
    db += map.uuid -> map
    map
  }

  private def fillWithRandom() {
    var ord = 1.0
    def cr(m: MindMap, s: String) = {
      val n = MindNode createChildOf (m.root, ord)
      n.content = Some(s)
      ord += 1
    }

    val a = create; a.root.content = Some("mapa A")
    val num = 4
    1 to num map ("a" + _) foreach (cr(a, _))

    val b = create; b.root.content = Some("mapa B")
    val c = create; c.root.content = Some("mapa C")
    val d = create; d.root.content = Some("mapa D")
  }

  def findAll: List[MindMap] = {
    if (db.isEmpty) fillWithRandom()

    db.values.toList
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
