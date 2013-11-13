package com.michalrus.helper

trait MiscHelper {

  val rng = new java.util.Random {
    /** Returns pseudo-random integer from range [a;b] */
    def nextInt(a: Int, b: Int): Int = {
      val c = a min b
      val d = a max b
      nextInt(d - c + 1) + c
    }
  }

  import language.implicitConversions

  implicit def blockToRunnable[A](f: => A) = new Runnable {
    def run() = f
  }

}
