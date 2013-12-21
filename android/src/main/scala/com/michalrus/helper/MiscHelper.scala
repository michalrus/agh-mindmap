package com.michalrus.helper

import android.util.Log

object MiscHelper {

  def log(s: String) = Log i ("com.michalrus.helper", s)

  val rng = new MiscHelper.Random

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

  def log(s: String) = MiscHelper log s

  val rng = MiscHelper.rng

  import language.implicitConversions

  implicit def blockToRunnable(f: => Unit) = new Runnable {
    def run() = f
  }

}
