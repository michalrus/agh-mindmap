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
import scala.util.Try

object DBHelper {
  val Name = "db"
  val Version = 5

  val TPrefs = "prefs"
  val CKey = "key"
  val CVal = "val"
  val FLatestAkka = "latest_akka"

  val TMap = "mindmap"
  val CUuid = "uuid"

  val TNode = "mindnode"
  val CMap = "map"
  val CParent = "parent"
  val COrdering = "ordering"
  val CContent = "content"
  val CHasConflict = "conflict"
  val CCloudTime = "cloudtime"

  val DropQs =
    s"DROP TABLE $TPrefs;" ::
    s"DROP TABLE $TMap;" ::
    s"DROP TABLE $TNode;" ::
    Nil
  val CreateQs =
    s"CREATE TABLE $TPrefs ($CKey STRING PRIMARY KEY, $CVal STRING);" ::
    s"CREATE INDEX ${TPrefs}_$CKey ON $TPrefs ($CKey);" ::
    s"CREATE TABLE $TMap ($CUuid STRING PRIMARY KEY);" ::
    s"CREATE TABLE $TNode ($CUuid STRING PRIMARY KEY, $CMap STRING, $CParent STRING, $COrdering REAL, $CContent STRING, $CHasConflict INTEGER, $CCloudTime INTEGER);" ::
    s"CREATE INDEX ${TNode}_$CMap ON $TNode ($CMap);" ::
    s"CREATE INDEX ${TNode}_$CParent ON $TNode ($CParent);" ::
    Nil
}

class DBHelper(context: Context)
  extends SQLiteOpenHelper(context, DBHelper.Name, ExplicitNull.CursorFactory, DBHelper.Version) {
  import DBHelper._

  val _ = getWritableDatabase // wtf, Android? it won't call DBHelper#onCreate if you delete this =,=

  def onCreate(db: SQLiteDatabase) {
    CreateQs foreach (db execSQL _)
  }

  def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    DropQs foreach (q => Try(db execSQL q))
    onCreate(db)
  }

}
