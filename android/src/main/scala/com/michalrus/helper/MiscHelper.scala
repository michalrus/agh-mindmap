package com.michalrus.helper

import android.util.Log
import scala.util.Try

object MiscHelper {

  def log(s: String) { Log i ("com.michalrus.helper", s); () }

  val rng = new MiscHelper.Random

  def safen[T](block: => T): Option[T] = Try(Option(block)).toOption.flatten

  class Random extends java.util.Random {
    /** Returns pseudo-random integer from range [a;b] */
    def nextInt(a: Int, b: Int): Int = {
      val c = a min b
      val d = a max b
      nextInt(d - c + 1) + c
    }
  }

}

trait MiscHelper {

  @inline def log(s: String) = MiscHelper log s

  @inline def safen[T](block: => T) = MiscHelper safen block

  val rng = MiscHelper.rng

  import language.implicitConversions

  implicit def blockToRunnable(f: => Unit) = new Runnable {
    def run() = f
  }

}
