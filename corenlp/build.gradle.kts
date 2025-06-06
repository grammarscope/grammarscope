plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

private val vCompileSdk by lazy { rootProject.extra["compileSdk"] as Int }
private val vMinSdk by lazy { rootProject.extra["minSdk"] as Int }

android {

    namespace = "org.depparse.corenlp"

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
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":data"))

    implementation(libs.core.ktx)
    implementation(libs.annotation)

    implementation(libs.coreNlp) {

        exclude(group = "com.apple", module = "AppleJavaExtensions")
        exclude(group = "de.jollyday", module = "jollyday")
        exclude(group = "org.apache.commons", module = "commons-lang3")
        exclude(group = "xom", module = "xom")
        exclude(group = "xalan", module = "xalan")
        exclude(group = "xalan", module = "serializer")
        exclude(group = "com.google.protobuf", module = "protobuf-java")
        exclude(group = "joda-time", module = "joda-time")
        exclude(group = "org.slf4j", module = "slf4j-api")

        exclude(group = "org.apache.lucene", module = "lucene-queryparser")
        exclude(group = "org.apache.lucene", module = "lucene-analyzers-common")
        exclude(group = "org.apache.lucene", module = "lucene-core")
        exclude(group = "jakarta.servlet", module = "jakarta.servlet-api")
        exclude(group = "org.ejml", module = "ejml-core")
        exclude(group = "org.ejml", module = "ejml-ddense")
        exclude(group = "org.ejml", module = "ejml-simple")
        exclude(group = "org.glassfish", module = "jakarta.json")

        exclude(group = "javax.activation", module = "javax.activation-api")
        exclude(group = "javax.xml.bind", module = "jaxb-api")
        exclude(group = "com.sun.istack", module = "istack-commons-runtime")
        exclude(group = "com.sun.xml.bind", module = "jaxb-impl")

        exclude(group = "junit", module = "junit")
        exclude(group = "com.pholser", module = "junit-quickcheck-core")
        exclude(group = "com.pholser", module = "junit-quickcheck-generators")
    }

    // implementation("edu.stanford.nlp:stanford-corenlp:${libs.versions.coreNlp}:models")
    // implementation("edu.stanford.nlp:stanford-corenlp:${libs.versions.coreNlp}:models-english")
    // implementation("edu.stanford.nlp:stanford-corenlp:${libs.versions.coreNlp}:models-english-kbp")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(libs.slf4j.api)
    implementation(libs.logback.android)

    androidTestImplementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.stdlib.jdk8)
}
