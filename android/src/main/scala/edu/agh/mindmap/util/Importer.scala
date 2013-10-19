package edu.agh.mindmap.util

import java.io.{BufferedReader, InputStreamReader, File}
import edu.agh.mindmap.model.{MindNode, MindMap}
import java.util.zip.ZipFile
import scala.xml.{Node, XML}

class ImporterException extends Exception

object Test {

  def sum(a: Int)(b: Int) = a + b

  def sumN(a: Int, b: Int) = sum(a)(b)


}

object Importer {

  def importFrom(file: File): Seq[MindMap] = try {
    val zip = new ZipFile(file)
    val is = zip.getInputStream(zip.getEntry("content.xml"))
    val br = new BufferedReader(new InputStreamReader(is))
    val sb = new StringBuilder

    def loop {
      br.readLine match {
        case line if line != null => sb append line; loop
        case _ =>
      }
    }

    val xml = XML loadString sb.result

    (xml \ "sheet").map(sheet => {
      val map = MindMap.create

      val root = (sheet \ "topic").head

      map.root.content = Some((root \ "title").head.text)

      def extractor(topic: Node, parent: MindNode) {
        (topic \ "topic").view.zipWithIndex.foreach {
          case (childXml, i) =>
            val child = MindNode.createChildOf(parent, i)
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
