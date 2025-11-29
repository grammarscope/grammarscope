import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

private val vCompileSdk by lazy { rootProject.extra["compileSdk"] as Int }
private val vMinSdk by lazy { rootProject.extra["minSdk"] as Int }

android {

    namespace = "org.depparse.common"

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
        jvmToolchain(17)
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":data"))
    implementation(project(":provider"))
    implementation(project(":core"))
    implementation(project(":coroutines"))
    implementation(project(":deploy"))
    implementation(project(":others"))
    implementation(project(":donate"))

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.fragment)
    implementation(libs.webkit)
    implementation(libs.constraintlayout)
    implementation(libs.preference.ktx)
    implementation(libs.material)
}
