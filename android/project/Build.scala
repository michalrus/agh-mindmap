import sbt._
import sbt.Keys._

import android.ArbitraryProject
import android.Keys._
import android.Dependencies.{LibraryProject, apklib}

import org.sbtidea.SbtIdeaPlugin._

object Build extends Build {

  // --- common

  val PlatformTarget = "android-17"

  // --- dload github:iPaulPro/aFileChooser

  val afcGit = uri("https://github.com/iPaulPro/aFileChooser.git#5da7ca6f69ed3a6e4c07f6d1ce0a76453ea435ef")
  val afcBase = (ArbitraryProject git afcGit) / "aFileChooser"
  lazy val afcSettings = android.Plugin.androidBuild ++ Seq(
    platformTarget in Android := PlatformTarget,
    libraryProject in Android := true,
    ideaSourcesClassifiers := Seq("src"),
    ideaJavadocsClassifiers := Seq(),
    ideaBasePackage := Some("com.ipaulpro.afilechooser"),
    ideaPackagePrefix := Some("com.ipaulpro.afilechooser")
  )
  lazy val afc = RootProject(afcBase)

  // --- build loaders

  override def buildLoaders = ArbitraryProject settingsLoader Map(
    afcBase -> afcSettings
  )

  // --- root

  lazy val root = Project(id = "root", base = file(".")).settings(
    android.Plugin.androidBuild ++ Seq(
      name := "agh-mindmap",
      scalaVersion := "2.10.3",

      javacOptions in Compile ++= Seq("-Xlint:deprecation"),
      scalacOptions in Compile ++= Seq("-feature", "-deprecation", "-Yno-adapted-args", "-Ywarn-all", "-Xfatal-warnings",
        "-Xlint", "-Ywarn-value-discard", "-Ywarn-numeric-widen", "-Ywarn-dead-code", "-unchecked"),

      platformTarget in Android := PlatformTarget,

      run <<= run in Android,

      libraryDependencies += "com.android.support" % "support-v4" % "18.0.0",
      libraryDependencies += apklib("com.actionbarsherlock" % "actionbarsherlock" % "4.4.0" intransitive()),
      localProjects in Android += LibraryProject(afcBase),

      useProguard in Android := true,
      proguardOptions in Android += "-keep class android.support.v4.app.** { *; }",
      proguardOptions in Android += "-keep interface android.support.v4.app.** { *; }",
      proguardOptions in Android += "-keep class com.actionbarsherlock.** { *; }",
      proguardOptions in Android += "-keep interface com.actionbarsherlock.** { *; }",
      proguardOptions in Android += "-keepattributes *Annotation*"
    )
  :_*) dependsOn afc aggregate afc

}
