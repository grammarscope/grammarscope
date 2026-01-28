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

#noinspection ShrinkerUnresolvedReference
-keep class org.depparse.Sentence { *; }
#noinspection ShrinkerUnresolvedReference
-keep class org.depparse.Token { *; }

# D Y N A M I C   O R   B Y   N A M E

-keep class org.grammarscope.corenlp.CoreNlpEngine { *; }
-keep class org.grammarscope.service.server.bound.corenlp.CoreNlpBoundService { *; }
-keep class org.grammarscope.service.client.DepParseBoundClient { *; }
-keep class org.depparse.common.AboutFragment { *; }
-keep class com.bbou.download.UpdateFragment { *; }

# P R E F E R E N C E

-keep public class * extends androidx.preference.PreferenceFragmentCompat

# G U A V A

# static analysis annotation frameworks
-dontwarn afu.org.checkerframework.**
-dontwarn org.checkerframework.**
-dontwarn com.google.errorprone.**

#unavailable classes on Android which are protected by runtime checks in Guava
# -dontwarn sun.misc.Unsafe
# -dontwarn java.lang.ClassValue

-dontwarn com.google.j2objc.annotations.ReflectionSupport$Level
-dontwarn com.google.j2objc.annotations.ReflectionSupport
-dontwarn com.google.j2objc.annotations.RetainedWith
-dontwarn com.google.j2objc.annotations.Weak
-dontwarn org.slf4j.impl.StaticLoggerBinder

# C O R E N L P

#corenlp
-keep class edu.stanford.nlp.classify.** { *; }
#-keep class edu.stanford.nlp.coref.** { *; }
#-keep class edu.stanford.nlp.dcoref.** { *; }
-keep class edu.stanford.nlp.fsm.** { *; }
-keep class edu.stanford.nlp.graph.** { *; }
#-keep class edu.stanford.nlp.ie.** { *; }
-keep class edu.stanford.nlp.international.** { *; }
-keep class edu.stanford.nlp.io.** { *; }
-keep class edu.stanford.nlp.ling.** { *; }
-keep class edu.stanford.nlp.math.** { *; }
-keep class edu.stanford.nlp.maxent.** { *; }
#-keep class edu.stanford.nlp.naturalli.** { *; }
-keep class edu.stanford.nlp.net.** { *; }
-keep class edu.stanford.nlp.neural.** { *; }
#-keep class edu.stanford.nlp.objectbank.** { *; }
-keep class edu.stanford.nlp.optimization.** { *; }
-keep class edu.stanford.nlp.paragraphs.** { *; }
-keep class edu.stanford.nlp.parser.** { *; }
-keep class edu.stanford.nlp.patterns.** { *; }
-keep class edu.stanford.nlp.pipeline.** { *; }
-keep class edu.stanford.nlp.process.** { *; }
#-keep class edu.stanford.nlp.quoteattribution.** { *; }
#-keep class edu.stanford.nlp.scenegraph.** { *; }
-keep class edu.stanford.nlp.semgraph.** { *; }
#-keep class edu.stanford.nlp.sentiment.** { *; }
-keep class edu.stanford.nlp.sequences.** { *; }
-keep class edu.stanford.nlp.simple.** { *; }
-keep class edu.stanford.nlp.stats.** { *; }
#-keep class edu.stanford.nlp.swing.** { *; }
-keep class edu.stanford.nlp.tagger.** { *; }
#-keep class edu.stanford.nlp.time.** { *; }
-keep class edu.stanford.nlp.trees.** { *; }
-keep class edu.stanford.nlp.util.** { *; }
-keep class edu.stanford.nlp.wordseg.** { *; }

#unused
-dontwarn java.awt.**
-dontwarn javax.swing.**
-dontwarn javax.json.**
-dontwarn javax.imageio.**
-dontwarn com.google.protobuf.**
-dontwarn org.ejml.**
-dontwarn org.joda.time.**
-dontwarn de.jollyday.**
-dontwarn com.sun.net.httpserver.**
-dontwarn nu.xom.**

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

# O T H E R

-dontwarn com.google.android.material.snackbar.**

# F I R E B A S E

-keep class com.google.android.gms.** { *; }
-keep class com.google.firebase.** { *; }

# A N D R O I D X   S T A R T U P   /   W O R K M A N A G E R

-keep class androidx.startup.InitializationProvider { *; }
-keep class androidx.work.impl.WorkDatabase_Impl { *; }
-keep class androidx.work.impl.** { *; }

# M L  K I T

-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_* { *; }
-keep class com.google.android.odml.image.** { *; }

#-dontwarn **
