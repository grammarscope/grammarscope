import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties

val buildTime: String = SimpleDateFormat("yyyy-MM-dd_HH:mm").format(Date())

fun getGitHash(workingDir: File = File(".")): String? {
    return try {
        val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD")
            .directory(workingDir)
            .redirectErrorStream(true)
            .start()
        val result = process.inputStream.bufferedReader().use { it.readText() }.trim()
        val exitCode = process.waitFor()
        if (exitCode == 0) result else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun getProps(file: File): Properties {
    val props = Properties()
    props.load(FileInputStream(file))
    return props
}

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.googleServices)
}

val keystoreProperties = getProps(rootProject.file("keystore_udpipe.properties"))

android {

    namespace = "org.grammarscope.udpipe"

    defaultConfig {
        applicationId = "org.grammarscope.udpipe"

        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get() as String?
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()

        multiDexEnabled = true

        //ndk {
        //    abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
        //}

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // BuildConfig fields
        buildConfigField("int", "VERSION_CODE", "${libs.versions.versionCode.get().toInt()}")
        buildConfigField("String", "VERSION_NAME", "\"${libs.versions.versionName.get()}\"")
        buildConfigField("boolean", "DROP_DATA", "false")
        buildConfigField("String", "BUILD_TIME", "\"$buildTime\"")
        buildConfigField("String", "GIT_HASH", "\"${getGitHash()}\"")
    }

    compileSdk = libs.versions.compileSdk.get().toInt()

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    testOptions {
    }

    signingConfigs {
        create("release") {
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
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
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

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(platform(libs.kotlin.bom))
    implementation(kotlin("stdlib"))

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

    "premiumImplementation"(project(":text_getter")) // strictly not a dependency but for manifest validation
    "premiumImplementation"(libs.text.recognition)

    testImplementation(libs.junit)
    androidTestImplementation(libs.test)
    androidTestImplementation(libs.rules)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.appcompat.resources)
}
