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
import scala.util.{Failure, Try, Success}
import spray.json._
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.{HttpPost, HttpUriRequest, HttpGet}
import edu.agh.mindmap.model
import org.apache.http.util.EntityUtils
import concurrent.duration._
import org.apache.http.params.HttpConnectionParams
import org.apache.http.entity.StringEntity
import org.apache.http.protocol.HTTP

object Synchronizer {
  private var baseUrl = "http://undefined"
  private def urlForPoll(since: Long) = s"$baseUrl/poll/since/$since"
  private def urlForUpdate = s"$baseUrl/update"

  private val HttpTimeout = 30.seconds
  private val MinIntervalBetweenFailures = 5.seconds

  private val pollShouldRun = new AtomicBoolean(false)
  def pause() = pollShouldRun set false

  def resume(baseUrl: String) {
    this.baseUrl = baseUrl
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

  private def request[T: JsonReader](req: HttpUriRequest): Try[T] = {
    val startedAt = System.currentTimeMillis

    val response = Try {
      val http = new DefaultHttpClient
      val params = http.getParams
      HttpConnectionParams setConnectionTimeout (params, HttpTimeout.toMillis.toInt)
      HttpConnectionParams setSoTimeout (params, HttpTimeout.toMillis.toInt)
      http setParams params
      val resp = http execute req
      val raw = EntityUtils toString resp.getEntity
      raw.asJson.convertTo[T]
    }

    if (response.isFailure) {
      val dur = System.currentTimeMillis - startedAt
      val diff = MinIntervalBetweenFailures.toMillis - dur
      if (diff > 0)
        Thread sleep diff
    }

    response
  }

  private def realUpdate: Future[Boolean] = future { blocking {
    val url = urlForUpdate

    val lastSeenAkkaAt = model.MindNode.lastTimeWithAkka

    val msgs = model.MindNode.findModified groupBy (_.map.uuid) map { case (mapUuid, nodes) =>
      val jsNodes = nodes map { n =>
        JsMindNode(n.uuid, n.parent, n.ordering, n.content, n.hasConflict, 0)
      }

      UpdateRequest(mapUuid, lastSeenAkkaAt, jsNodes.toList)
    }

    msgs foreach { msg =>
      val req = new HttpPost(url)
      val entity = new StringEntity(msg.toJson.compactPrint, HTTP.UTF_8)
      entity setContentType "application/json"
      req setEntity entity

      request[UpdateResponse](req) match {
        case Success(UpdateResponse(orphanNodes)) =>
          if (orphanNodes.nonEmpty) {
            for {
              orphUuid <- orphanNodes
              orph <- model.MindNode.findByUuid(orphUuid)
              parentUuid <- orph.parent
              parent <- model.MindNode.findByUuid(parentUuid)
            } model.MindNode.touchAllOfTree(parent) // ... to have them resent in the next `UpdateRequest`

            update() // primitively "schedule" a new update
          }

        case Failure(cause) =>
          log(s"UPDATE: ${cause.getMessage}")
          throw cause
      }
    }

    true
  }}

  private def realPoll: Future[Unit] = future { blocking {
    val url = urlForPoll(since = model.MindNode.lastTimeWithAkka)
    request[PollResponse](new HttpGet(url)) match {
      case Success(PollResponse(updates)) =>
        val grp = updates groupBy { case JsNodePlusMap(map, node) => map } mapValues { xs => xs map (_.node) }
        grp foreach { case (map, nodes) =>
          model.MindNode.mergeIn(map, nodes)
        }

      case Failure(cause) =>
        log(s"POLL: ${cause.getMessage}")
    }
  }}

}
