# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView forActivity JS through addJavascriptInterface(),
# uncomment the following and specify the fully qualified class name
# to the JavaScript interface class:
# -keepclassmembers class fqcn.of.javascript.interface.for.webview { public *; }

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# G U A V A

# static analysis annotation frameworks
-dontwarn afu.org.checkerframework.**
-dontwarn org.checkerframework.**
-dontwarn com.google.errorprone.**

#unavailable classes on Android which are protected by runtime checks in Guava
# -dontwarn sun.misc.Unsafe
# -dontwarn java.lang.ClassValue

# O P E N N L P

# -dontwarn org.osgi.framework.BundleActivator
# -dontwarn org.osgi.framework.BundleContext
# -dontwarn org.osgi.framework.Filter
# -dontwarn org.osgi.framework.FrameworkUtil
# -dontwarn org.osgi.framework.InvalidSyntaxException
# -dontwarn org.osgi.util.tracker.ServiceTracker
