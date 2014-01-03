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

import spray.json._
import java.util.UUID

trait CustomJsonFormats {

  implicit object UuidFormat extends JsonFormat[UUID] {
    def write(obj: UUID): JsValue = JsString(obj.toString)

    def read(json: JsValue): UUID = json match {
      case JsString(x) => try { UUID fromString x }
      catch { case e: Throwable => deserializationError(s"UUID could not be parsed from `$x'", e) }
      case x => deserializationError(s"Expected UUID as JsString, but got $x")
    }
  }

}

object JsMindNode extends DefaultJsonProtocol with CustomJsonFormats {
  implicit val format = jsonFormat(apply, "uuid", "parent", "ordering", "content", "hasConflict", "cloudTime")
}

case class JsMindNode(uuid: UUID,
                      parent: Option[UUID],
                      ordering: Double,
                      content: Option[String],
                      hasConflict: Boolean,
                      cloudTime: Long)

object PollResponse extends DefaultJsonProtocol {
  implicit val format = jsonFormat(apply _, "nodes")
}

case class PollResponse(nodes: List[JsMindNode])

object UpdateRequest extends DefaultJsonProtocol with CustomJsonFormats {
  implicit val format = jsonFormat(apply, "mindMap", "lastServerTime", "nodes")
}

case class UpdateRequest(mindMap: UUID, lastServerTime: Long, nodes: List[JsMindNode])

object UpdateResponse extends DefaultJsonProtocol with CustomJsonFormats {
  implicit val format = jsonFormat(apply _, "unknownParents")
}

case class UpdateResponse(unknownParents: List[UUID])
