package org.corenlp

import android.util.Log
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.ling.IndexedWord
import edu.stanford.nlp.pipeline.CoreDocument
import edu.stanford.nlp.pipeline.CoreSentence
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.semgraph.SemanticGraph
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations
import edu.stanford.nlp.semgraph.SemanticGraphEdge
import edu.stanford.nlp.trees.GrammaticalRelation
import edu.stanford.nlp.trees.GrammaticalStructure
import edu.stanford.nlp.trees.GrammaticalStructureFactory
import edu.stanford.nlp.trees.PennTreebankLanguagePack
import edu.stanford.nlp.trees.TreebankLanguagePack
import edu.stanford.nlp.trees.TypedDependency
import org.depparse.Sentence
import org.depparse.Token
import org.depparse.Token.BreakLevel
import java.io.File
import java.util.Properties

typealias CoreNlpSentence = CoreSentence
typealias CoreNlpToken = CoreLabel
typealias CoreNlpNeuralDependencies = SemanticGraph
typealias CoreNlpConstituencyDependency = TypedDependency

object CoreNlp {

    var pipeline: StanfordCoreNLP? = null

    var neural = true

    fun version(): Int {
        return (4 shl 24) or (5 shl 16) or (9 shl 8) or 1
    }

    private fun loadProperties(home: String): Properties {
        val props = Properties()
        val propertiesFile = File("$home/StanfordCoreNLP.properties")
        propertiesFile.reader().use { reader ->
            props.load(reader)
        }
        for (key in props.keys) {
            if (key.toString().endsWith(".model"))
                props[key] = "$home/${props[key]}"
        }
        return props
    }

    fun load(modelPath: String, neural: Boolean): Long {
        val hash = if (pipeline == null) {

            // neural flag
            this.neural = neural

            // set up pipeline properties
            val propertiesPath = modelPath
            val props = loadProperties(propertiesPath)
            props.setProperty("annotators", if (neural) "tokenize,pos,depparse" else "tokenize,pos,lemma,parse") // drop "lemma"

            // build pipeline
            Log.d(TAG, "Loading new pipeline (annotators are pooled and cached")
            pipeline = StanfordCoreNLP(props)
            Log.d(TAG, "Loaded new pipeline")
            pipeline.hashCode().toLong()
        } else {
            Log.d(TAG, "Pipeline reused")
            pipeline.hashCode().toLong()
        }
        Log.d(TAG, "Pipeline $hash is ready")
        return hash
    }

    fun unload(handle: Long) {
        if (pipeline.hashCode().toLong() == handle) {
            pipeline = null
            System.gc()
        }
    }

    private fun CoreNlpSentence.basicDeps(): SemanticGraph {
        return coreMap().get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation::class.java)
    }

    private fun CoreNlpSentence.enhancedDeps(): SemanticGraph {
        return coreMap().get(SemanticGraphCoreAnnotations.EnhancedDependenciesAnnotation::class.java)
    }

    // = dependencyParse()
    private fun CoreNlpSentence.enhancedPlusPlusDeps(): SemanticGraph {
        return coreMap().get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation::class.java)
    }

    private fun tokens(sentence: CoreNlpSentence): String {
        return sentence.tokens().joinToString(separator = "\n") { token: CoreNlpToken ->
            val word = token.word()
            val start = token.beginPosition()
            val end = token.endPosition()
            val lemma = token.lemma()
            "$word $lemma ($start-$end)"
        }
    }

    private fun merge(basic: Iterable<SemanticGraphEdge>, enhanced: Iterable<SemanticGraphEdge>): Iterable<SemanticGraphEdge> {
        val merged = (basic.asSequence() + enhanced.asSequence())
            .distinctBy { listOf(it.relation, it.source, it.target) }
            .toList()
        return merged
    }

    private fun enhancedEdge(enhanced: Iterable<SemanticGraphEdge>, index: Int): String {
        return enhanced
            .asSequence()
            .filter { index == it.target.index() }
            .map { edge: SemanticGraphEdge ->
                val relation: GrammaticalRelation = edge.relation
                val label: String = relation.shortName
                val source: IndexedWord = edge.source
                val head = source.index() - 1
                "$head:$label"
            }
            .joinToString(separator = "|")
    }

    private fun enhancedDependency(enhanced: Iterable<TypedDependency>, index: Int): String {
        return enhanced
            .asSequence()
            .filter { index == it.dep().index() }
            .map { edge: TypedDependency ->
                val relation: GrammaticalRelation = edge.reln()
                val label: String = relation.shortName
                val gov: IndexedWord = edge.gov()
                val head = gov.index() - 1
                "$head:$label"
            }
            .joinToString(separator = "|")
    }

    fun parse(inputTexts: Array<String>): Array<Sentence> {
        val doc = pipeline?.processToCoreDocument(inputTexts.joinToString("\n"))
        if (doc != null)
            return if (neural) parseNeural(doc) else parseConstituency(doc)
        return emptyArray()
    }

    fun parseConstituency(doc: CoreDocument): Array<Sentence> {
        val charIndices: IntArray = getCharIndices(doc.text())
        val tlp: TreebankLanguagePack = PennTreebankLanguagePack()
        val gsf: GrammaticalStructureFactory = tlp.grammaticalStructureFactory()
        return doc.sentences()
            .withIndex()
            .map { (sentenceIndex: Int, sentence: CoreNlpSentence) ->

                val offsets = sentence.charOffsets()
                val sentenceStart = charIndices[offsets.first]
                val sentenceEnd = charIndices[offsets.second - 1]

                val parseTree = sentence.constituencyParse()
                val gs: GrammaticalStructure = gsf.newGrammaticalStructure(parseTree)
                val constituencyDeps: Collection<CoreNlpConstituencyDependency> = gs.typedDependencies()
                val constituencyEnhancedDeps: Collection<CoreNlpConstituencyDependency> = gs.typedDependenciesEnhancedPlusPlus() - constituencyDeps
                val tokens = constituencyDeps
                    .map { dependency: CoreNlpConstituencyDependency ->
                        val head = dependency.gov()
                        val headIndex = head.index() - 1
                        val dependent = dependency.dep()
                        val dependentIndex = dependent.index() - 1
                        val dependentWord = dependent.word()
                        val dependentPos = "name: 'postag' value: '${dependent.tag()}'"
                        val label = dependency.reln().toString()

                        // offsets
                        val dependentStart = charIndices[dependent.beginPosition() - sentenceStart] // sentence relative offset
                        val dependentEnd = charIndices[dependent.endPosition() - 1 - sentenceStart] // sentence relative offset

                        // breaklevel
                        val breakLevel = dependent.breakLevel()

                        val deps: String = enhancedDependency(constituencyEnhancedDeps, dependentIndex + 1)

                        val token = Token(sentenceIndex, dependentIndex, dependentWord, dependentStart, dependentEnd, "", if (dependentPos.isEmpty()) "?" else dependentPos, head = headIndex, label, breakLevel = breakLevel, deps = deps)
                        token
                    }
                    .sortedBy { it.index }
                    .toTypedArray()

                val text = sentence.text()
                val docid = sentence.document().docID() ?: ""
                Sentence(text, sentenceStart, sentenceEnd, tokens, docid)
            }
            .toTypedArray()
    }

    fun parseNeural(doc: CoreDocument): Array<Sentence> {
        val charIndices: IntArray = getCharIndices(doc.text())
        return doc.sentences()
            .withIndex()
            .map { (sentenceIndex: Int, sentence: CoreNlpSentence) ->
                val offsets = sentence.charOffsets()
                val sentenceStart = charIndices[offsets.first]
                val sentenceEnd = charIndices[offsets.second - 1]

                val neuralDeps: CoreNlpNeuralDependencies = sentence.basicDeps()
                val neuralEnhancedDeps = sentence.enhancedPlusPlusDeps().edgeIterable().filter { it.isExtra }

                val deps: Sequence<Token> = neuralDeps.edgeIterable()
                    .asSequence()
                    .map { edge: SemanticGraphEdge ->

                        val relation: GrammaticalRelation = edge.relation
                        val label: String = relation.shortName

                        // source is head / governor
                        val source: IndexedWord = edge.source
                        val sourceIndex = source.index() - 1

                        // target is dependent token
                        val target: IndexedWord = edge.target
                        val targetIndex = target.index() - 1
                        val targetWord = target.word()
                        val targetPos = "name: 'postag' value: '${target.get(CoreAnnotations.PartOfSpeechAnnotation::class.java)}'"

                        // offsets
                        val targetStart = charIndices[target.beginPosition() - sentenceStart] // sentence relative offset
                        val targetEnd = charIndices[target.endPosition() - 1 - sentenceStart] // sentence relative offset

                        // breaklevel
                        val breakLevel = target.breakLevel()

                        val deps: String = enhancedEdge(neuralEnhancedDeps, targetIndex + 1)

                        val token = Token(sentenceIndex, targetIndex, targetWord, targetStart, targetEnd, "", targetPos, head = sourceIndex, label, breakLevel = breakLevel, deps = deps)
                        token
                    }

                val root: IndexedWord = neuralDeps.roots.first()
                val rootIndex = root.index() - 1
                val rootStart = charIndices[root.beginPosition() - sentenceStart]
                val rootEnd = charIndices[root.endPosition() - 1 - sentenceStart]
                val rootWord = root.word()
                val rootPos = "name: 'postag' value: '${root.get(CoreAnnotations.PartOfSpeechAnnotation::class.java)}'"
                val rootBreakLevel = root.breakLevel()

                val tokens: Sequence<Token> = sequenceOf(Token(sentenceIndex, rootIndex, rootWord, rootStart, rootEnd, "", rootPos, head = -1, "root", breakLevel = rootBreakLevel, deps = null)) + deps
                val tokenArray: Array<Token> = tokens.sortedBy { it.index }.toList().toTypedArray()

                val text = sentence.text()
                val docid = sentence.document().docID() ?: ""
                Sentence(text, sentenceStart, sentenceEnd, tokenArray, docid)
            }
            .toTypedArray()
    }

    private fun IndexedWord.breakLevel(): Int {
        val after: String = this.get(CoreAnnotations.AfterAnnotation::class.java)
        return when (after) {
            "" -> BreakLevel.NO_BREAK.ordinal /* No separation between tokens */
            " " -> BreakLevel.SPACE_BREAK.ordinal /* Tokens separated by space */
            "\n" -> BreakLevel.LINE_BREAK.ordinal /* Tokens separated by line break */
            "\n\n" -> BreakLevel.SENTENCE_BREAK.ordinal /* Tokens separated by sentence break. New sentence. */
            else -> -1
        }
    }

    /**
     * Creates a mapping from UTF-8 byte positions to character indices.
     *
     * @param text The UTF-8 encoded string to process
     * @return An IntArray where each index represents a byte position, and the value is the corresponding character index
     */
    fun getCharIndices(text: String): IntArray {
        // Get the byte size of the UTF-8 string
        val textBytes = text.toByteArray(Charsets.UTF_8)
        val byteSize = textBytes.size

        // Create array for byte-to-char index mapping (size + 1 to include position after last byte)
        val byteToCharIndex = IntArray(byteSize + 1)

        // Iterate through each Unicode character
        var bytePos = 0
        var charIndex = 0

        for (codePoint in text.codePoints2()) {
            // Get the UTF-8 byte representation of this code point
            val charBytes = String(intArrayOf(codePoint), 0, 1).toByteArray(Charsets.UTF_8)
            val charByteCount = charBytes.size

            // Fill in the byte-to-char mapping for each byte in this character
            for (j in 0 until charByteCount) {
                if (bytePos + j < byteToCharIndex.size) {
                    byteToCharIndex[bytePos + j] = charIndex
                }
            }

            bytePos += charByteCount
            charIndex++
        }

        // Set the final position
        if (bytePos < byteToCharIndex.size) {
            byteToCharIndex[bytePos] = charIndex
        }

        return byteToCharIndex
    }

    // Extension function to get code points as a sequence (similar to Java's codePoints())
    fun String.codePoints2(): Sequence<Int> = sequence {
        var i = 0
        while (i < length) {
            val codePoint = Character.codePointAt(this@codePoints2, i)
            yield(codePoint)
            i += Character.charCount(codePoint)
        }
    }

    private const val TAG = "CoreNlp"
}
