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
import org.squeryl.{Schema, Session, SessionFactory}
import edu.agh.mindmapd.extensions.Settings
import org.squeryl.adapters.PostgreSqlAdapter

private object PostgresStorage extends Schema {

  def init(settings: Settings) {
    if (SessionFactory.concreteFactory.isEmpty)
      SessionFactory.concreteFactory = Some(() => Session create (
        java.sql.DriverManager getConnection(settings.db.url, settings.db.user, settings.db.password),
        new PostgreSqlAdapter
        ))
  }

}

class PostgresStorage(val mindMap: UUID, settings: Settings) extends Storage {
  import PostgresStorage._
  init(settings)

  def existsMindnode(uuid: UUID): Boolean = ???

  def find(uuid: UUID): Option[MindNode] = ???

  def findSince(time: Long): Iterable[MindNode] = ???

  def insertOrReplace(node: MindNode): Unit = ???

  def deleteChildrenOf(node: UUID): Unit = ???

  def touchTimesOfSubtree(parent: MindNode): Unit = ???

  def wasAnyChangedInSubtree(parent: MindNode, since: Long): Boolean = ???

  def hasNoNodesYet: Boolean = ???

}
