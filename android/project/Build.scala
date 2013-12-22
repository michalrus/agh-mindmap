import sbt._
import sbt.Keys._

import android.ArbitraryProject
import android.Keys._
import android.Dependencies.{LibraryProject, apklib}

object Build extends Build {

  lazy val root = Project(id = "root", base = file(".")).settings(
    android.Plugin.androidBuild ++ Seq(
      name := "agh-mindmap",
      scalaVersion := "2.10.3",

      javacOptions in Compile ++= Seq("-Xlint:deprecation"),
      scalacOptions in Compile ++= Seq("-feature", "-deprecation", "-Yno-adapted-args", "-Ywarn-all", "-Xfatal-warnings",
        "-Xlint", "-Ywarn-value-discard", "-Ywarn-numeric-widen", "-Ywarn-dead-code", "-unchecked"),

      platformTarget in Android := "android-17",

      run <<= run in Android,

      libraryDependencies += "com.android.support" % "support-v4" % "18.0.0",
      libraryDependencies += apklib("com.actionbarsherlock" % "actionbarsherlock" % "4.4.0" intransitive()),

      useProguard in Android := true,
      proguardOptions in Android += "-keep class android.support.v4.app.** { *; }",
      proguardOptions in Android += "-keep interface android.support.v4.app.** { *; }",
      proguardOptions in Android += "-keep class com.actionbarsherlock.** { *; }",
      proguardOptions in Android += "-keep interface com.actionbarsherlock.** { *; }",
      proguardOptions in Android += "-keepattributes *Annotation*"
    )
  :_*)

}
