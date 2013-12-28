package edu.agh.mindmap.model

import java.util.UUID
import java.io.File
import edu.agh.mindmap.util.Importer

class MindMap private(val uuid: UUID,
                      val lastMod: Long,
                      isNew: Boolean) {

  val root = if (isNew) MindNode.createRootOf(this)
  else MindNode.findRootOf(this)

  private def commit() {
    // FIXME: save to local DB
    // FIXME: sync
    MindMap.db += uuid -> this
    ()
  }

}

object MindMap {

  import collection.mutable

  private val db = new mutable.HashMap[UUID, MindMap]  // FIXME: this should be a real database

  def create = {
    val map = new MindMap(UUID.randomUUID, (new java.util.Date).getTime, true)
    map commit()
    map
  }

  private def fillWithRandom() {
    var ord = 1
    def addRandomChildren(parent: MindNode, num: Int) = for (i <- 1 to num) {
      val n = MindNode createChildOf (parent, ord.toDouble)
      n.content = parent.content map (_ + "." + i)
      ord += 1
    }

    val a = create; a.root.content = Some("A")
    addRandomChildren(a.root, 9)
    a.root.children foreach { c => addRandomChildren(c, 5); c.children foreach { cc => addRandomChildren(cc, 2) } }

    val b = create; b.root.content = Some("B")
    addRandomChildren(b.root, 4)

    val c = create; c.root.content = Some("C")
    addRandomChildren(c.root, 6)

    val d = create; d.root.content = Some("D")
    addRandomChildren(d.root, 8)
  }

  def findAll: List[MindMap] = {
    if (db.isEmpty) fillWithRandom()

    db.values.toList
  }

  def findByUuid(uuid: UUID) = db get uuid

  def importFrom(file: File): Seq[MindMap] = Importer importFrom file

}
