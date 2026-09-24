plugins {
    alias(libs.plugins.androidLibrary)
}

android {

    namespace = "com.bbou.download.common"

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileSdk = libs.versions.compileSdk.get().toInt()

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    testOptions {
        targetSdk = libs.versions.targetSdk.get().toInt()
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

    implementation(project(":core"))

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.preference.ktx)
    implementation(libs.constraintlayout)
    implementation(libs.material)
}
