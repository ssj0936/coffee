// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}
buildscript {
    extra["kotlinVersion"] = "1.4.32"

    repositories {
        google()
        jcenter()
        mavenCentral()

    }
    dependencies {
        classpath(Dependencies.androidGradlePlugin)
        classpath(Dependencies.Kotlin.gradlePlugin)

        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.7.1")


        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven( "https://jitpack.io")
    }
}