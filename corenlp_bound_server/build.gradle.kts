import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

private val vCompileSdk by lazy { rootProject.extra["compileSdk"] as Int }
private val vMinSdk by lazy { rootProject.extra["minSdk"] as Int }

android {

    namespace = "org.grammarscope.service.server.bound.corenlp"

    defaultConfig {
        minSdk = vMinSdk
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
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget("17")
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":service_bound_server"))
    implementation(project(":service_iface")) // needed for IParceler
    implementation(project(":data")) // needed for Sentence
    implementation(project(":provider")) // needed for IProvider
    implementation(project(":core")) // needed  for IEngine, IAsyncLoading
    implementation(project(":result"))
    implementation(project(":corenlp_engine"))

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
}
