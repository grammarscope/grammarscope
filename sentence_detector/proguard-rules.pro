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

# O P E N N L P

#component
-keep class opennlp.tools.sentdetect.SentenceDetectorME { *; }

# factory
-keep class opennlp.tools.sentdetect.SentenceDetectorFactory { *; }

# model
-keep class opennlp.tools.sentdetect.SentenceModel { *; }

#serializers
-keep class opennlp.tools.util.model.ByteArraySerializer { *; }
-keep class opennlp.tools.util.model.GenericModelSerializer { *; }
-keep class opennlp.tools.util.model.PropertiesSerializer { *; }
-keep class opennlp.tools.util.model.DictionarySerializer { *; }

# -dontwarn org.osgi.framework.BundleActivator
# -dontwarn org.osgi.framework.BundleContext
# -dontwarn org.osgi.framework.Filter
# -dontwarn org.osgi.framework.FrameworkUtil
# -dontwarn org.osgi.framework.InvalidSyntaxException
# -dontwarn org.osgi.util.tracker.ServiceTracker
