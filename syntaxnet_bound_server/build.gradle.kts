plugins {
    alias(libs.plugins.androidLibrary)
}

android {

    namespace = "org.grammarscope.service.server.bound.syntaxnet"

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileSdk = libs.versions.compileSdk.get().toInt()

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

    implementation(platform(libs.kotlin.bom))
    implementation(kotlin("stdlib"))

    implementation(project(":service_bound_server"))
    implementation(project(":service_iface")) // needed for IParceler
    implementation(project(":data")) // needed for Sentence
    implementation(project(":provider")) // needed for IProvider
    implementation(project(":stub")) // needed  for IEngine, IAsyncLoading
    implementation(project(":result"))
    implementation(project(":syntaxnet_engine"))

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
}
