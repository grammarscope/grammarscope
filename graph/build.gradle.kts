plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

private val vCompileSdk by lazy { rootProject.extra["compileSdk"] as Int }
private val vMinSdk by lazy { rootProject.extra["minSdk"] as Int }

android {

    namespace = "org.grammarscope.graph"

    defaultConfig {
        minSdk = vMinSdk
    }

    compileSdk = vCompileSdk

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
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

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":data"))
    implementation(project(":core"))
    implementation(project(":common"))
    implementation(project(":coroutines"))
    implementation(project(":semantics"))

    implementation(libs.jung.api)
    implementation(libs.jung.layout)
    implementation(libs.jung.visualization)
    implementation(libs.jung.factory)
    implementation(libs.jung.settings)
    implementation(libs.jung.colors)
    implementation(libs.jung.glue.geom)
    implementation(libs.jung.glue.event)
    implementation(libs.jung.glue.visualization)

    implementation(libs.guava)
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.preference.ktx)
}
