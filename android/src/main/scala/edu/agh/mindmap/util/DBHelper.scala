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

import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}
import android.content.Context

object DBHelper {
  val Name = "db"
  val Version = 1

  val TMap = "mindmap"
  val CUuid = "uuid"

  val TNode = "mindnode"
  val CMap = "map"
  val CParent = "parent"
  val COrdering = "ordering"
  val CContent = "content"
  val CHasConflict = "conflict"
  val CCloudTime = "cloudtime"

  val DropQ = s"DROP TABLE $TMap; DROP TABLE $TNode;"
  val CreateQ = s"CREATE TABLE $TMap ($CUuid STRING PRIMARY KEY);" +
    s"CREATE TABLE $TNode ($CUuid STRING PRIMARY KEY, $CMap STRING, $CParent STRING, $COrdering REAL, $CContent STRING, $CHasConflict INTEGER, $CCloudTime INTEGER);" +
    s"CREATE INDEX ${TNode}_$CMap ON $TNode ($CMap);" +
    s"CREATE INDEX ${TNode}_$CParent ON $TNode ($CParent);"
}

class DBHelper(context: Context)
  extends SQLiteOpenHelper(context, DBHelper.Name, null, DBHelper.Version) {
  import DBHelper._

  def onCreate(db: SQLiteDatabase) {
    db execSQL CreateQ
  }

  def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    db execSQL DropQ
    onCreate(db)
  }

}
