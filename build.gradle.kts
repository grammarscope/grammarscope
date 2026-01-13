/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>
 */

// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {

    repositories {
        mavenCentral()
        mavenLocal()
        google()
        maven(url = System.getenv("HOME") + "/.m2/repository/")
    }
}

allprojects {

    ext {
        set("versionCode", 6101)
        set("versionName", "6.101")
        set("minSdk", 24)
        set("targetSdk", 36)
        set("compileSdk", 36)
    }

    gradle.projectsEvaluated {
        tasks.withType<JavaCompile> {
            options.compilerArgs.addAll(arrayOf("-Xlint:deprecation", "-Xlint:unchecked"))
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory.get().asFile)
}

plugins {
    // id("idea")
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.googleServices) apply false
}

//idea {
//    module {
//        // println("$project @ " + project.projectDir)
//        excludeDirs.addAll(
//            files(
//                "depparse_messenger_client",
//                "service_messenger_client",
//                "service_messenger_server",
//                "service_client",
//                "service_server",
//                "service_common_server",
//                "syntaxnet_messenger_server",
//                "udpipe_messenger_server",
//                "dist",
//                "gradle",
//                "archive",
//            )
//        )
//
//        project.subprojects.forEach { p ->
//            // println("sub $p @ ${p.projectDir}")
//            listOf("artwork", "artwork2", "artwork-assets", "artwork-assets", "artwork-relations", "data", "dist")
//                .forEach { excluded ->
//                    fileTree("${p.projectDir}").visit {
//                        if (isDirectory && name == excluded) {
//                            // println("EXCLUDE $this")
//                            excludeDirs.add(file(file.absolutePath))
//                        }
//                    }
//                }
//        }
//        //excludeDirs.forEach {
//            // println("- $it")
//        //}
//    }
//}
