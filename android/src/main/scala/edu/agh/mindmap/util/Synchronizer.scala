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

object Synchronizer {
  log(s"Hello, from $Synchronizer!")

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
      realUpdate andThen { case _ =>
        updating set false
        if (updateOnceMore.get) update()
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

  private def realUpdate: Future[Unit] = future {
    log(s"UPDATE: starting...")

    // FIXME: get & send pending updates from `model.MindMap`
    Thread sleep 500

    log(s"UPDATE:    ... done")
  }

  private def realPoll: Future[Unit] = future {
    log(s"POLL: starting...")

    // FIXME: pull & merge updates from Akka
    Thread sleep 1000

    log(s"POLL:    ... done")
  }

}
