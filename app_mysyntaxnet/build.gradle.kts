import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

fun getProps(file: File): Properties {
    val props = Properties()
    props.load(FileInputStream(file))
    return props
}

plugins {
    alias(libs.plugins.androidApplication)
}

private val vCode by lazy { rootProject.extra["versionCode"] as Int }
private val vName by lazy { rootProject.extra["versionName"] as String }
private val vCompileSdk by lazy { rootProject.extra["compileSdk"] as Int }
private val vMinSdk by lazy { rootProject.extra["minSdk"] as Int }
private val vTargetSdk by lazy { rootProject.extra["targetSdk"] as Int }

val keystoreProperties = getProps(rootProject.file("keystore.properties"))

android {

    namespace = "org.mysyntaxnet"

    defaultConfig {
        applicationId = "org.mysyntaxnet"

        versionCode = vCode
        versionName = vName
        minSdk = vMinSdk
        targetSdk = vTargetSdk

        multiDexEnabled = true

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileSdk = vCompileSdk

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    testOptions {
    }

    signingConfigs {
        create("mysyntaxnet") {
            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
            storeFile = file(keystoreProperties.getProperty("storeFile"))
            storePassword = keystoreProperties.getProperty("storePassword")
        }
    }

    buildFeatures {
        buildConfig = true
        compose = false
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("mysyntaxnet")
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("mysyntaxnet")
            versionNameSuffix = "signed"
        }
    }

    flavorDimensions += "product"
    productFlavors {
        create("base") {
            dimension = "product"
        }
        create("premium") {
            dimension = "product"
            applicationIdSuffix = ".premium"
            versionNameSuffix = "-premium"
        }
    }

    sourceSets {
        getByName("base") {
            assets.directories.add("src/base/")
        }
        getByName("premium") {
            assets.directories.add("src/premium/")
        }
    }

    packaging.jniLibs {
        useLegacyPackaging = true
    }

    lint {
        abortOnError = false
    }
}

kotlin {
    compilerOptions {
        jvmToolchain(17)
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(platform(libs.kotlin.bom))
    implementation(kotlin("stdlib"))

    implementation(project(":data"))
    implementation(project(":provider"))
    implementation(project(":core"))
    implementation(project(":deploy"))
    implementation(project(":common"))
    implementation(project(":coroutines"))
    implementation(project(":others"))
    implementation(project(":donate"))
    implementation(project(":rate"))

    implementation(project(":syntaxnet1_jni"))

    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    testImplementation(libs.junit)
    androidTestImplementation(libs.test)
    androidTestImplementation(libs.rules)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.appcompat.resources)
}
