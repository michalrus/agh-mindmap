/*
 * Copyright 2013 Katarzyna Szawan <kat.szwn@gmail.com>
 *     and Michał Rus <https://michalrus.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

      map.root foreach (_.content = Some((root \ "title").head.text))

      def extractor(topic: Node, parent: MindNode) {
        (topic \ "children" \ "topics" \ "topic").zipWithIndex.foreach {
          case (childXml, i) =>
            val child = MindNode.createChildOf(parent, i.toDouble)
            child.content = Some((childXml \ "title").head.text)
            extractor(childXml, child)
        }
      }

      map.root foreach (extractor(root, _))

      map
    })
  } catch {
    case _: Exception => throw new ImporterException
  }

}
