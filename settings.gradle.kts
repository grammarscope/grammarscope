pluginManagement {
    repositories {
        gradlePluginPortal()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenLocal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenLocal()
        mavenCentral()
    }
}

rootProject.name = "GrammarScope"

include(":data")
include(":provider")
include(":core")
include(":deploy")
include(":semantics")
include(":sentence_detector")
include(":download")
include(":download_common")
include(":others")
include(":donate")
include(":rate")
include(":graph")
include(":annotations")
include(":capture")
include(":common")
include(":grammarscope")
include(":text_getter")
include(":livedata")
include(":coroutines")

// JNI

include(":syntaxnet1_jni")
include(":syntaxnet2_jni")
include(":udpipe_jni")
include(":corenlp")

// APP

include(":app_mysyntaxnet")
include(":app_grammarscope_syntaxnet")
include("app_grammarscope_udpipe")
include("app_grammarscope_corenlp")

// SERVICE

include(":service_iface")
include(":service_client_iface")
//include ":service_common_client")

include(":service_bound_server")
include(":service_bound_client")

//include (":service_common_server")
//include (":service_messenger_server")
//include (":service_messenger_client")

include(":depparse_bound_client")
// include ":depparse_messenger_client")

include(":result")

// UDPIPE

include(":udpipe_engine")
include(":udpipe_bound_server")
//include (":udpipe_messenger_server")

// SYNTAXNET

include(":syntaxnet_engine")
include(":syntaxnet_bound_server")
//include (":syntaxnet_messenger_server")

// CORENLP

include(":corenlp_engine")
include(":corenlp_bound_server")
//include (":corenlp_messenger_server")
