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

package edu.agh.mindmapd.storage

import java.util.UUID
import edu.agh.mindmapd.model.MindNode
import org.squeryl.{Schema, Session, SessionFactory, PrimitiveTypeMode}
import PrimitiveTypeMode._
import edu.agh.mindmapd.extensions.Settings
import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl.dsl.ast.LogicalBoolean

object SquerylStorage extends Schema {

  def allMaps(settings: Settings): Vector[UUID] = {
    init(settings)
    inTransaction { from(mindNodes)(n => select(&(n.mindMap))).distinct.toVector }
  }

  def apply(mindMap: UUID, settings: Settings): Storage = {
    init(settings)
    new SquerylStorage(mindMap)
  }

  private[this] def init(settings: Settings) {
    init(settings.squeryl.url, settings.squeryl.user, settings.squeryl.password)
  }

  private[this] def init(url: String, user: String, password: String) {
    if (SessionFactory.concreteFactory.isEmpty)
      SessionFactory.concreteFactory = Some(() => Session create (
        java.sql.DriverManager getConnection(url, user, password),
        new PostgreSqlAdapter
        ))
  }

  private val mindNodes = table[MindNode]

  on(mindNodes)(n => declare(
    columns(n.mindMap, n.parent) are indexed,
    n.content is dbType("text"),
    columns(n.mindMap, n.cloudTime) are indexed
  ))

}

class SquerylStorage private(val mindMap: UUID) extends Storage {
  import SquerylStorage._

  def contains(node: UUID): Boolean = find(node).isDefined

  def find(node: UUID): Option[MindNode] = inTransaction {
    from(mindNodes)(n =>
      where(n.mindMap.~ === mindMap and n.uuid.~ === node)
        select n).headOption
  }

  def findSince(time: Long, limit: Int): Vector[MindNode] = inTransaction {
    val q = from(mindNodes)(n =>
      where(n.mindMap.~ === mindMap and n.cloudTime.~ >= time)
        select n orderBy n.cloudTime.asc) page (0, limit)
    q.toVector
  }

  def insertOrReplace(node: MindNode) = inTransaction {
    if (contains(node.uuid)) mindNodes update node
    else { val _ = mindNodes insert node }
  }

  private def children(parent: UUID): Iterable[MindNode] = inTransaction {
    from(mindNodes)(n =>
      where(n.mindMap.~ === mindMap and n.parent.~ === Some(parent))
        select n
    )
  }

  def deleteChildrenOf(parent: UUID) = inTransaction {
    for (ch <- children(parent)) deleteChildrenOf(ch.uuid)
    // *** DFS, DO NOT CHANGE ORDER! ***
    val _ = mindNodes deleteWhere (n => n.mindMap.~ === mindMap and n.parent.~ === Some(parent))
  }

  def touchTimesOfSubtree(parent: UUID) = inTransaction {
    val now = System.currentTimeMillis
    def upd(f: MindNode => LogicalBoolean) {
      val _ = update(mindNodes)(n => where(n.mindMap.~ === mindMap and f(n)) set(n.cloudTime := now))
    }

    def touchChildrenOf(parent: UUID) {
      for (ch <- children(parent)) touchChildrenOf(ch.uuid)
      upd(_.parent.~ === Some(parent))
    }

    touchChildrenOf(parent)
    upd(_.uuid.~ === parent)
  }

  def wasAnyChangedInSubtree(parent: MindNode, since: Long): Boolean = inTransaction {
    if (parent.cloudTime > since) true
    else children(parent.uuid) exists (wasAnyChangedInSubtree(_, since))
  }

  def hasNoNodesYet: Boolean = inTransaction {
    val num: Long = from(mindNodes)(n => where(n.mindMap.~ === mindMap) compute count).single.measures
    num == 0
  }

}
