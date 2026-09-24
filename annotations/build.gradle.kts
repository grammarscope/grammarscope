plugins {
    alias(libs.plugins.androidLibrary)
}

android {
    namespace = "org.grammarscope.annotations"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

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
        jvmToolchain(17)
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(platform(libs.kotlin.bom))
    implementation(kotlin("stdlib"))

    implementation(project(":core"))
    implementation(project(":data"))
    implementation(project(":stub"))
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
