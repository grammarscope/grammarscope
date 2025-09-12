import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

private val vCompileSdk by lazy { rootProject.extra["compileSdk"] as Int }
private val vMinSdk by lazy { rootProject.extra["minSdk"] as Int }

android {
    namespace = "org.grammarscope.annotations"
    compileSdk = vCompileSdk

    defaultConfig {
        minSdk = vMinSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget("17")
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":data"))
    implementation(project(":core"))
    implementation(project(":common"))
    implementation(project(":coroutines"))
    implementation(project(":capture"))

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.preference.ktx)
    implementation(libs.jung.colors)
    implementation(libs.kotlin.json)

    testImplementation(libs.junit)
    androidTestImplementation(libs.test)
    androidTestImplementation(libs.espresso.core)
}