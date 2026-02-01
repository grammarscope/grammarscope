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
    alias(libs.plugins.googleServices)
}

private val vCode by lazy { rootProject.extra["versionCode"] as Int }
private val vName by lazy { rootProject.extra["versionName"] as String }
private val vCompileSdk by lazy { rootProject.extra["compileSdk"] as Int }
private val vMinSdk by lazy { rootProject.extra["minSdk"] as Int }
private val vTargetSdk by lazy { rootProject.extra["targetSdk"] as Int }

val keystoreProperties = getProps(rootProject.file("keystore_upload.properties"))

android {

    namespace = "org.grammarscope.corenlp"

    defaultConfig {
        applicationId = "org.grammarscope.corenlp"

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
        create("upload") {
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
            signingConfig = signingConfigs.getByName("debug")
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("upload")
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
        getByName("androidTest") {
            assets.directories.addAll(listOf("src/main/assets/", "src/debug/assets/"))
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/license.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/notice.txt"
            excludes += "META-INF/ASL2.0"
            excludes += "META-INF/*.kotlin_module"
        }
    }

    lint {
        abortOnError = false
    }
}

androidComponents.onVariants { variant ->
    if (variant.flavorName == "base") {
        val taskName = "process${variant.name.replaceFirstChar { it.uppercase() }}GoogleServices"
        project.tasks.named(taskName).configure {
            enabled = false
        }
    }
}

kotlin {
    compilerOptions {
        jvmToolchain(17)
    }
}

val premiumImplementation by configurations

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(platform(libs.kotlin.bom))
    implementation(kotlin("stdlib"))

    implementation(project(":grammarscope"))
    implementation(project(":corenlp_bound_server")) // strictly runtimeOnly but needed for manifest validation
    implementation(project(":common")) // strictly not a dependency but for resources validation
    implementation(project(":annotations")) // strictly not a dependency but for resources validation
    implementation(project(":download")) // strictly not a dependency but for manifest validation
    implementation(project(":download_common")) // strictly not a dependency but for manifest validation
    implementation(project(":others")) // strictly not a dependency but for manifest validation
    implementation(project(":donate")) // strictly not a dependency but for manifest validation

    runtimeOnly(project(":corenlp"))
    runtimeOnly(project(":corenlp_engine"))

    implementation(libs.preference.ktx)
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
