import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

private val vCompileSdk by lazy { rootProject.extra["compileSdk"] as Int }
private val vMinSdk by lazy { rootProject.extra["minSdk"] as Int }

android {

    namespace = "org.depparse.syntaxnet1"

    defaultConfig {
        minSdk = vMinSdk

        externalNativeBuild {
            cmake {
                cppFlags("-frtti -fexceptions")
                arguments("-DCMAKE_VERBOSE_MAKEFILE=1")
            }
        }

        //ndk {
        //    abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
        //}
    }

    compileSdk = vCompileSdk

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            isJniDebuggable = false
        }
        debug {
            isJniDebuggable = true
        }
    }

    externalNativeBuild {
        cmake {
            path("CMakeLists.txt")
        }
    }

    //sourceSets {
    //    maybeCreate("main").apply {
    //        jniLibs {
    //            srcDirs("src/main/jniLibs")
    //        }
    //    }
    //}

    ndkVersion = "25.0.8775105"
}

kotlin {
    compilerOptions {
        jvmToolchain(17)
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":data"))
    implementation(project(":core"))
    implementation(project(":provider"))

    implementation(libs.core.ktx)
    implementation(libs.annotation)

    androidTestImplementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.stdlib.jdk8)
}
