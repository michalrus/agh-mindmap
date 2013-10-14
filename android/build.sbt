androidDefaults

name := "agh-mindmap"

version := "0.1"

versionCode := 0

scalaVersion := "2.10.1"

platformName := "android-14"

libraryDependencies += "com.android.support" % "support-v4" % "18.0.0"

libraryDependencies += apklib("com.actionbarsherlock" % "actionbarsherlock" % "4.4.0" intransitive())

proguardOptions += "-keep class android.support.v4.app.** { *; }"

proguardOptions += "-keep interface android.support.v4.app.** { *; }"

proguardOptions += "-keep class com.actionbarsherlock.** { *; }"

proguardOptions += "-keep interface com.actionbarsherlock.** { *; }"

proguardOptions += "-keepattributes *Annotation*"
