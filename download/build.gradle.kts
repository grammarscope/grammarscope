import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
}

private val vCompileSdk by lazy { rootProject.extra["compileSdk"] as Int }
private val vMinSdk by lazy { rootProject.extra["minSdk"] as Int }
private val vTargetSdk by lazy { rootProject.extra["targetSdk"] as Int }

android {

    namespace = "com.bbou.download.coroutines"

    defaultConfig {
        minSdk = vMinSdk
    }

    compileSdk = vCompileSdk

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    testOptions {
        targetSdk = vTargetSdk
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
        jvmToolchain(17)
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":download_common"))
    implementation(project(":deploy"))
    implementation(project(":coroutines"))

    implementation(libs.appcompat)
    implementation(libs.core.ktx)
    implementation(libs.fragment)
    implementation(libs.lifecycle)
    implementation(libs.preference.ktx)
    implementation(libs.work)
    implementation(libs.material)
}
