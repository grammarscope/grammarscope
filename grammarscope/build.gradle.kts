plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

private val vCompileSdk by lazy { rootProject.extra["compileSdk"] as Int }
private val vMinSdk by lazy { rootProject.extra["minSdk"] as Int }

android {

    namespace = "org.grammarscope.common"

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

    flavorDimensions += "product"
    productFlavors {
        create("base") {
            dimension = "product"
        }
        create("premium") {
            dimension = "product"
        }
    }

    sourceSets {
        getByName("base") {
            assets.srcDirs("src/base/")
        }
        getByName("premium") {
            assets.srcDirs("src/premium/")
        }
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

val premiumImplementation by configurations
dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":data"))
    implementation(project(":provider"))
    implementation(project(":core"))
    implementation(project(":deploy"))
    implementation(project(":common"))
    implementation(project(":coroutines"))
    implementation(project(":download"))
    implementation(project(":download_common"))
    implementation(project(":semantics"))
    implementation(project(":graph"))
    implementation(project(":annotations"))
    implementation(project(":capture"))
    implementation(project(":others"))
    implementation(project(":donate"))
    implementation(project(":rate"))
    implementation(project(":sentence_detector"))
    implementation(project(":service_iface"))
    implementation(project(":service_client_iface"))
    implementation(project(":service_bound_client"))
    implementation(project(":depparse_bound_client"))

    implementation(libs.core.ktx)
    implementation(libs.kotlin.reflect)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.preference.ktx)
    implementation(libs.material)
    implementation(libs.guava)
    implementation(libs.jung.api)
    implementation(libs.jung.layout)
    implementation(libs.jung.visualization)
    implementation(libs.jung.factory)
    implementation(libs.jung.settings)
    implementation(libs.jung.colors)
    implementation(libs.jung.glue.geom)
    implementation(libs.jung.glue.event)
    implementation(libs.jung.glue.visualization)

    premiumImplementation(project(":text_getter"))
}
