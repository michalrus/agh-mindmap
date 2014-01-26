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

import edu.agh.mindmap.model.{MindNode, MindMap}
import android.content.Context
import android.widget.Toast
import edu.agh.mindmap.R
import android.os.Environment
import java.io.{FileOutputStream, BufferedOutputStream, File}
import scala.xml._
import java.util.zip.{ZipEntry, ZipOutputStream}
import java.nio.charset.Charset

object Exporter {
  import com.michalrus.android.helper.Helper.{rng, log}

  def export(context: Context, map: MindMap) {
    val ctx = context.getApplicationContext
    val title = map.root flatMap (_.content) getOrElse SanitizeWith
    val file = fileFor(title)

    def toast(id: Int) =
      Toast makeText (ctx, ctx getString (id, file.getAbsolutePath), Toast.LENGTH_SHORT) show()
    def error() = toast(R.string.export_error)
    try {
      map.root.fold { error() } { root: MindNode =>
        exportImpl(file, root, ctx getString R.string.export_sheet_title)
        toast(R.string.export_success)
      }
    } catch {
      case e: Throwable =>
        error()
        log(s"$e\n${e.getStackTraceString}\n")
    }
  }

  private def exportImpl(file: File, root: MindNode, sheetTitle: String) {
    val timestamp = System.currentTimeMillis.toString
    val xmap = <xmap-content
    xmlns="urn:xmind:xmap:xmlns:content:2.0"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    xmlns:svg="http://www.w3.org/2000/svg"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    timestamp={timestamp}
    version="2.0">
      <sheet id={randomId} timestamp={timestamp}>
        {topic2Xml(root, timestamp)}
        <title>{sheetTitle}</title>
      </sheet>
    </xmap-content>
    val content = """<?xml version="1.0" encoding="UTF-8" standalone="no"?>""" +
      "\n" + xmap.mkString

    val os = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)))
    try {
      os putNextEntry new ZipEntry("content.xml")
      os write (content getBytes (Charset forName "UTF-8"))
    } finally {
      os close()
    }
  }

  private def topic2Xml(node: MindNode, timestamp: String): Node = {
    val children = node.children
    val t = <topic id={randomId} timestamp={timestamp}>
      <title>{node.content getOrElse ""}</title>
      {if (children.nonEmpty) <children>
        <topics type="attached">
          {children map (topic2Xml(_, timestamp))}
        </topics>
      </children>}
    </topic>

    if (node.isRoot) t % Attribute(None, "structure-class", Text("org.xmind.ui.map.clockwise"), Null)
    else t
  }

  private val RandomIdInput = "qwertyuiopasdfghjklzxcvbnm1234567890"
  private def randomId = (Array fill 26)(RandomIdInput(rng nextInt RandomIdInput.length)).mkString

  private val SanitizeRegex = """[^A-Za-z0-9]+""".r
  private val SanitizeWith = "_"
  private def fileFor(title: String): File = {
    val clean = SanitizeRegex replaceAllIn (title, SanitizeWith) stripPrefix SanitizeWith stripSuffix SanitizeWith
    val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    new File(dir.getAbsolutePath + "/" + clean + ".xmind")
  }

}
