
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
