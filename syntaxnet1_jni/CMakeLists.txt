# Documentation: https://d.android.com/studio/projects/add-native-code.html

project(SYNTAXNET_JNI)

# Sets the minimum version of CMake required to build the native library.
cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_VERBOSE_MAKEFILE on)

# Sets compile flags
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DSTANDALONE_JNI_LIB -std=c++11 -fno-exceptions -fno-rtti -O2 -Wno-narrowing -fPIE")
if (ANDROID_ABI MATCHES "^armeabi-v7a$")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -mfloat-abi=softfp -mfpu=neon")
elseif (ANDROID_ABI MATCHES "^arm64-v8a")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O2 -ftree-vectorize")
endif ()

# Sets link flags
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,--allow-multiple-definition -Wl,--whole-archive -fPIE -v")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
get_filename_component(CPP_DIR ${CMAKE_SOURCE_DIR}/src/main/cpp ABSOLUTE)
add_library(
        syntaxnet_jni    # name of the library.
        SHARED      # as a shared library.
        ${CPP_DIR}/syntaxnet_jni.cpp # path to source file(s).
)

get_filename_component(INCLUDE_DIR ${CMAKE_SOURCE_DIR}/src/main/include ABSOLUTE)
get_filename_component(TOP_DIR ${CMAKE_SOURCE_DIR}/.. ABSOLUTE)
target_include_directories(
        syntaxnet_jni       # name of the library.
        PRIVATE
        ${INCLUDE_DIR}              # first level dir
        ${TOP_DIR}                  # second level dir
        ${CMAKE_SOURCE_DIR}         # third level dir
)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
find_library(
        log-lib      # the name of the path variable.
        log       # the name of the NDK library that CMake is to locate.
)

add_library(syntaxnet_inference SHARED IMPORTED)
set_target_properties(syntaxnet_inference PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libsyntaxnet_inference.so)
#set_target_properties(syntaxnet_inference PROPERTIES IMPORTED_NO_SONAME 1)
set_property(TARGET syntaxnet_inference PROPERTY IMPORTED_NO_SONAME 1)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.
target_link_libraries(
        syntaxnet_jni    # the target library.
        syntaxnet_inference   # the imported library to link to.
        android
        log
        m
        atomic
        z
        #${log-lib}                 # the library included in the NDK to link the target library to
)
