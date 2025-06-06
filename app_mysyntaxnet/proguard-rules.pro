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

# J N I   I N V O K E D

-keep class org.depparse.Sentence { *; }
-keep class org.depparse.Token { *; }

# D Y N A M I C   O R   B Y   N A M E

-keep class org.syntaxnet1.Syntaxnet1Engine { *; }
-keep class org.depparse.common.AboutFragment { *; }

# O T H E R
-dontwarn com.google.android.material.snackbar.**

#-dontwarn **
