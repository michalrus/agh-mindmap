package edu.agh.mindmap.util

import java.io.File
import edu.agh.mindmap.model.{MindNode, MindMap}

object Importer {

  def importFrom(file: File): MindMap = {
    // TODO: open `file`...

    val map = MindMap.create

    // FIXME: parse file

    map.root.content = Some("Beware, I'm the root node!")

    val someChildNode = MindNode.create(map.root)
    someChildNode.content = Some("I'm a child node!")

    // TODO: ...

    map
  }

}
