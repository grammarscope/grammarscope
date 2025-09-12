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
    alias(libs.plugins.kotlinAndroid)
}

private val vCode by lazy { rootProject.extra["versionCode"] as Int }
private val vName by lazy { rootProject.extra["versionName"] as String }
private val vCompileSdk by lazy { rootProject.extra["compileSdk"] as Int }
private val vMinSdk by lazy { rootProject.extra["minSdk"] as Int }
private val vTargetSdk by lazy { rootProject.extra["targetSdk"] as Int }

val keystoreProperties = getProps(rootProject.file("keystore.properties"))

android {

    namespace = "org.grammarscope.udpipe"

    defaultConfig {
        applicationId = "org.grammarscope.udpipe"

        versionCode = vCode
        versionName = vName
        minSdk = vMinSdk
        targetSdk = vTargetSdk

        multiDexEnabled = true

        //ndk {
        //    abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
        //}

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
            assets.srcDirs("src/base/")
        }
        getByName("premium") {
            assets.srcDirs("src/premium/")
        }
        getByName("androidTest") {
            assets.srcDirs("src/main/assets/", "src/debug/assets/")
        }
    }

    packagingOptions.jniLibs {
        useLegacyPackaging = true
    }

    lint {
        abortOnError = false
    }
}

androidComponents.onVariants { variant ->
    if (variant.flavorName?.contains("premiumm") == true) {

        pluginManager.apply(libs.plugins.googleServices.get().pluginId)
        //project.plugins.apply("your.plugin.id")
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget("17")
    }
}

val premiumImplementation by configurations

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":grammarscope"))
    implementation(project(":udpipe_bound_server")) // strictly runtimeOnly but needed for manifest validation
    implementation(project(":common")) // strictly not a dependency but for resources validation
    implementation(project(":annotations")) // strictly not a dependency but for resources validation
    implementation(project(":download")) // strictly not a dependency but for manifest validation
    implementation(project(":download_common")) // strictly not a dependency but for manifest validation
    implementation(project(":others")) // strictly not a dependency but for manifest validation
    implementation(project(":donate")) // strictly not a dependency but for manifest validation

    runtimeOnly(project(":udpipe_jni"))
    runtimeOnly(project(":udpipe_engine"))

    implementation(libs.annotation) // strictly runtimeOnly but needed for resources validation
    implementation(libs.appcompat) // strictly not a dependency but for menu resources validation
    implementation(libs.constraintlayout) // strictly not a dependency but for layout resources validation
    implementation(libs.material) // strictly not a dependency but for layout resources validation

    premiumImplementation(project(":text_getter")) // strictly not a dependency but for manifest validation
    premiumImplementation(libs.text.recognition)

    testImplementation(libs.junit)
    androidTestImplementation(libs.test)
    androidTestImplementation(libs.rules)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.appcompat.resources)
}
