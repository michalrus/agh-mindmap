package com.michalrus.helper

import android.util.Log

object MiscHelper {

  def log(s: String) = Log d ("com.michalrus.helper", s)

}

trait MiscHelper {

  def log(s: String) = MiscHelper log s

  class Random extends java.util.Random {
    /** Returns pseudo-random integer from range [a;b] */
    def nextInt(a: Int, b: Int): Int = {
      val c = a min b
      val d = a max b
      nextInt(d - c + 1) + c
    }
  }

  val rng = new Random

  import language.implicitConversions

  implicit def blockToRunnable[A](f: => A) = new Runnable {
    def run() = f
  }

}
