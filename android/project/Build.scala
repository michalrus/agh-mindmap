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

import sbt._
import sbt.Keys._

import android.ArbitraryProject
import android.Keys._
import android.Dependencies.{LibraryProject, apklib}

import org.sbtidea.SbtIdeaPlugin._

object Build extends Build {

  // --- common

  val PlatformTarget = "android-17"

  val SupportV4 = "com.android.support" % "support-v4" % "18.0.0"

  val UnwantedSubprojectJars = Seq("android-support-v4.jar")
  def inUnwantedSubprojectJars(file: Attributed[File]) =
    UnwantedSubprojectJars contains file.data.getName
  def inUnwantedSubprojectJars(file: File) =
    UnwantedSubprojectJars contains file.getName

  // --- dload github:iPaulPro/aFileChooser

  val afcGit = uri("https://github.com/iPaulPro/aFileChooser.git#42d10d3bf3bddfb7ed4856c3264fc26ead4625a1")
  val afcBase = (ArbitraryProject git afcGit) / "aFileChooser"
  lazy val afcSettings = android.Plugin.androidBuild ++ Seq(
    platformTarget in Android := PlatformTarget,
    libraryProject in Android := true,

    unmanagedJars in Compile ~= (_ filterNot inUnwantedSubprojectJars),
    libraryDependencies += SupportV4,

    ideaSourcesClassifiers := Seq("src"),
    ideaJavadocsClassifiers := Seq(),
    ideaBasePackage := None,
    ideaPackagePrefix := None
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

      libraryDependencies += SupportV4,
      libraryDependencies += apklib("com.actionbarsherlock" % "actionbarsherlock" % "4.4.0" intransitive()),
      localProjects in Android += LibraryProject(afcBase),

      proguardInputs in Android ~= (p => p.copy(injars = p.injars filterNot inUnwantedSubprojectJars)),
      dexInputs in Android ~= (_ filterNot inUnwantedSubprojectJars),

      useProguard in Android := true,
      proguardOptions in Android += "-keep class android.support.v4.app.** { *; }",
      proguardOptions in Android += "-keep interface android.support.v4.app.** { *; }",
      proguardOptions in Android += "-keep class com.actionbarsherlock.** { *; }",
      proguardOptions in Android += "-keep interface com.actionbarsherlock.** { *; }",
      proguardOptions in Android += "-keepattributes *Annotation*"
    )
  :_*) dependsOn afc aggregate afc

}
