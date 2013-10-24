import android.Keys._

import android.Dependencies.{apklib,aar}

android.Plugin.androidBuild

name := "agh-mindmap"

scalaVersion := "2.10.1"

scalacOptions in Compile += "-feature"

scalacOptions in Compile += "-deprecation"

platformTarget in Android := "android-17"

run <<= run in Android

install <<= install in Android

libraryDependencies += "com.android.support" % "support-v4" % "18.0.0"

libraryDependencies += apklib("com.actionbarsherlock" % "actionbarsherlock" % "4.4.0" intransitive())

useProguard in Android := true

proguardOptions in Android += "-keep class android.support.v4.app.** { *; }"

proguardOptions in Android += "-keep interface android.support.v4.app.** { *; }"

proguardOptions in Android += "-keep class com.actionbarsherlock.** { *; }"

proguardOptions in Android += "-keep interface com.actionbarsherlock.** { *; }"

proguardOptions in Android += "-keepattributes *Annotation*"
