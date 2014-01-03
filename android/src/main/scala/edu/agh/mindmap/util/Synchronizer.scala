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

import concurrent._
import ExecutionContext.Implicits.global
import java.util.concurrent.atomic.AtomicBoolean
import com.michalrus.helper.MiscHelper.log
import scala.util.Success
import java.util.UUID
import spray.json._

object Synchronizer {
  private val pollShouldRun = new AtomicBoolean(false)
  def pause() = pollShouldRun set false

  def resume() {
    pollShouldRun set true

    update()
    poll()
  }

  private val updateOnceMore, updating = new AtomicBoolean(false)
  def update() {
    if (updating compareAndSet (false, true)) {
      updateOnceMore set false
      realUpdate andThen {
        case Success(true) =>
          updating set false
          if (updateOnceMore.get) update()
        case _ =>
          updating set false
          update()
      }
      ()
    } else {
      updateOnceMore set true
    }
  }

  private val polling = new AtomicBoolean(false)
  private def poll() {
    if (polling compareAndSet (false, true)) {
      realPoll andThen { case _ =>
        polling set false
        if (pollShouldRun.get) poll()
      }
      ()
    }
  }

  private def realUpdate: Future[Boolean] = future {
    log(s"UPDATE: starting...")

    // FIXME: get & send pending updates from `model.MindMap`
    Thread sleep 500
    val connectionSucceeded = true

    log(s"UPDATE:    ... done")

    connectionSucceeded
  }

  private def realPoll: Future[Unit] = future {
    log(s"POLL: starting...")

    // FIXME: pull & merge updates from Akka
    Thread sleep 1000

    log(s"POLL:    ... done")
  }

}

object Js {

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

  object MindNode extends DefaultJsonProtocol with CustomJsonFormats {
    implicit val format = jsonFormat6(apply)
  }

  case class MindNode(uuid: UUID,
                      parent: Option[UUID],
                      ordering: Double,
                      content: Option[String],
                      hasConflict: Boolean,
                      cloudTime: Long)

  object PollResponse extends DefaultJsonProtocol {
    implicit val format = jsonFormat1(apply)
  }

  case class PollResponse(nodes: List[MindNode])

  object UpdateRequest extends DefaultJsonProtocol with CustomJsonFormats {
    implicit val format = jsonFormat3(apply)
  }

  case class UpdateRequest(mindMap: UUID, lastServerTime: Long, nodes: List[MindNode])

  object UpdateResponse extends DefaultJsonProtocol with CustomJsonFormats {
    implicit val format = jsonFormat1(apply)
  }

  case class UpdateResponse(unknownParents: List[UUID])

}
