package edu.agh.mindmap.util

import java.io.File
import edu.agh.mindmap.model.{MindNode, MindMap}
import java.util.zip.ZipFile
import scala.xml.{Node, XML}

class ImporterException extends Exception

object Importer {

  def importFrom(file: File): Seq[MindMap] = try {
    val zip = new ZipFile(file)
    val is = zip.getInputStream(zip.getEntry("content.xml"))

    val xml = XML load is

    (xml \ "sheet").map(sheet => {
      val map = MindMap.create

      val root = (sheet \ "topic").head

      map.root.content = Some((root \ "title").head.text)

      def extractor(topic: Node, parent: MindNode) {
        (topic \ "children" \ "topics" \ "topic").zipWithIndex.foreach {
          case (childXml, i) =>
            val child = MindNode.createChildOf(parent, i.toDouble)
            child.content = Some((childXml \ "title").head.text)
            extractor(childXml, child)
        }
      }

      extractor(root, map.root)

      map
    })
  } catch {
    case _: Exception => throw new ImporterException
  }

}
