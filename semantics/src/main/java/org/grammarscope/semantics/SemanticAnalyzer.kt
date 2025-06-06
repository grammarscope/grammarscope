package org.grammarscope.semantics

import android.util.Log
import org.depparse.Sentence
import org.depparse.Token

/**
 * Semantic pred-subject-object analyzer
 *
 * @author Bernard Bou
 */
class SemanticAnalyzer {

    /**
     * To avoid visit cycles
     */
    private val visitedNodes: MutableSet<Token> = HashSet()

    // F E E D

    private fun getRoots(sentence: Sentence): Collection<Token> {
        val result: MutableList<Token> = ArrayList()
        for (token in sentence.tokens) {
            if (token.head == -1 && "root" == token.label) {
                result.add(token)
            }
        }
        return result
    }

    private fun getDependencies(sentence: Sentence, sentenceIdx: Int, @Suppress("SameParameterValue") label2Dependencies: MutableMap<String, Dependency>?): Array<Dependency> {
        return sentence.tokens
            .withIndex()
            .map { (i, token) ->
                val dependency = Dependency(if (token.head == -1) null else sentence.tokens[token.head], token, i, sentenceIdx)
                if (label2Dependencies != null) {
                    label2Dependencies[token.label] = dependency
                }
                dependency
            }
            .toTypedArray<Dependency>()
    }

    // A N A L Y Z E

    fun analyze(vararg sentences: Sentence): Array<Analysis> {

        val n = sentences.size
        val analyses = Array(n) {
            Log.d(TAG, "sentence: " + (it + 1))
            val sentence = sentences[it]
            val analysis = Analysis(sentence.text)

            // clear visits
            visitedNodes.clear()

            // dependencies
            val dependencies = getDependencies(sentence, it, null)

            // filter
            val filteredDependencies = filter(dependencies)

            // roots
            val roots = getRoots(sentence)
            if (roots.isNotEmpty()) {

                // explore
                for (root in roots) {
                    visitedNodes.add(root)
                    analyzePredicate(it, root, filteredDependencies, analysis)
                }
            }
            // else System.err.printf("sentence: %d no roots\n", thisSentenceIdx + 1);

            analysis
        }
        return analyses
    }

    /**
     * Analyze predicate
     *
     * @param sentenceIdx  sentence index
     * @param predicate    predicate
     * @param dependencies input dependencies
     * @param relations    result relations
     */
    private fun analyzePredicate(sentenceIdx: Int, predicate: Token, dependencies: Collection<Dependency>, relations: MutableList<Relation>) {

        // predicate-subject
        val pss = makePSs(sentenceIdx, dependencies, predicate)
        if (pss != null) {
            for (ps in pss) {
                // add
                relations.add(ps)

                // recurse
                if (!visitedNodes.contains(ps.term)) {
                    visitedNodes.add(ps.term)
                    analyzeTerm(sentenceIdx, ps.term, dependencies, relations)
                }
            }
        }

        // predicate-object
        val pos = makePOs(sentenceIdx, dependencies, predicate)
        if (pos != null) {
            for (po in pos) {
                // add
                relations.add(po)

                // recurse
                if (!visitedNodes.contains(po.term)) {
                    visitedNodes.add(po.term)
                    analyzeTerm(sentenceIdx, po.term, dependencies, relations)
                }
            }
        }

        // explore predicate-predicate
        val pps = makePPs(sentenceIdx, dependencies, predicate)
        if (pps.isNotEmpty()) {
            relations.addAll(pps)

            // recurse
            for (pp in pps) {
                val predicate2 = pp.term
                if (!visitedNodes.contains(predicate2)) {
                    visitedNodes.add(predicate2)
                    analyzePredicate(sentenceIdx, predicate2, dependencies, relations)
                }
            }
        }

        // explore term-predicate
        val tps = makeTPs(sentenceIdx, dependencies, predicate)
        if (tps.isNotEmpty()) {
            relations.addAll(tps)

            // recurse
            for (tp in tps) {
                val predicate2 = tp.predicate
                if (!visitedNodes.contains(predicate2)) {
                    visitedNodes.add(predicate2)
                    analyzePredicate(sentenceIdx, predicate2, dependencies, relations)
                }
            }
        }
    }

    /**
     * Analyze term
     *
     * @param sentenceIdx  sentence index
     * @param term         term
     * @param dependencies input dependencies
     * @param relations    result relations
     */
    private fun analyzeTerm(sentenceIdx: Int, term: Token?, dependencies: Collection<Dependency>, relations: MutableList<Relation>) {
        if (term == null) {
            return
        }

        // explore predicate-predicate
        val pps = makePPs(sentenceIdx, dependencies, term)
        if (pps.isNotEmpty()) {
            relations.addAll(pps)

            // recurse
            for (pp in pps) {
                val predicate2 = pp.term
                if (!visitedNodes.contains(predicate2)) {
                    visitedNodes.add(predicate2)
                    analyzePredicate(sentenceIdx, predicate2, dependencies, relations)
                }
            }
        }

        // explore term-predicate
        val tps = makeTPs(sentenceIdx, dependencies, term)
        if (tps.isNotEmpty()) {
            relations.addAll(tps)

            // recurse
            for (tp in tps) {
                val predicate2 = tp.predicate
                if (!visitedNodes.contains(predicate2)) {
                    visitedNodes.add(predicate2)
                    analyzePredicate(sentenceIdx, predicate2, dependencies, relations)
                }
            }
        }
    }

    /**
     * Make predicate-subject relation
     *
     * @param sentenceIdx  sentence index
     * @param dependencies input dependencies
     * @param predicate    predicate
     * @return predicate-subject relation
     */
    private fun makePS(sentenceIdx: Int, dependencies: Collection<Dependency>, predicate: Token): PS? {
        // subject
        val subject = findOneOf(dependencies, SemanticRelations.SubjectRelations, predicate)
        if (subject == null) {
            Log.d(TAG, "no subject")
            return null
        }

        // PS
        val ps = PS(sentenceIdx + 1, predicate, subject.dependent, subject.label)
        Log.d(TAG, "\tPS: $ps")
        return ps
    }

    /**
     * Make predicate-subject relations
     *
     * @param sentenceIdx  sentence index
     * @param dependencies input dependencies
     * @param predicate    predicate
     * @return predicate-subject relations
     */
    private fun makePSs(sentenceIdx: Int, dependencies: Collection<Dependency>, predicate: Token): List<PS>? {
        // subject
        val subjects = findAllOf(dependencies, SemanticRelations.SubjectRelations, predicate)
        if (subjects.isEmpty()) {
            Log.d(TAG, "no subjects")
            return null
        }

        // PS
        val pss: MutableList<PS> = ArrayList()
        for (subject in subjects) {
            val ps = PS(sentenceIdx + 1, predicate, subject.dependent, subject.label)
            pss.add(ps)
            Log.d(TAG, "\tPS: $ps")
        }
        return pss
    }

    /**
     * Make predicate-object relation
     *
     * @param sentenceIdx  sentence index
     * @param dependencies input dependencies
     * @param predicate    predicate
     * @return predicate-object relation
     */
    private fun makePO(sentenceIdx: Int, dependencies: Collection<Dependency>, predicate: Token): PO? {
        // object
        val `object` = findOneOf(dependencies, SemanticRelations.ObjectRelations, predicate)
        if (`object` == null) {
            Log.d(TAG, "no object")
            return null
        }

        // PO
        val po = PO(sentenceIdx + 1, predicate, `object`.dependent, `object`.label)
        Log.d(TAG, "\tPO: $po")
        return po
    }

    /**
     * Make predicate-object relations
     *
     * @param sentenceIdx  sentence index
     * @param dependencies input dependencies
     * @param predicate    predicate
     * @return predicate-object relations
     */
    private fun makePOs(sentenceIdx: Int, dependencies: Collection<Dependency>, predicate: Token): List<PO>? {
        // object
        val objects = findAllOf(dependencies, SemanticRelations.ObjectRelations, predicate)
        if (objects.isEmpty()) {
            Log.d(TAG, "no objects")
            return null
        }

        // PO
        val pos: MutableList<PO> = ArrayList()
        for (`object` in objects) {
            val po = PO(sentenceIdx + 1, predicate, `object`.dependent, `object`.label)
            pos.add(po)
            Log.d(TAG, "\tPO: $po")
        }
        return pos
    }

    /**
     * Make predicate-predicate (ex:clausal complement)
     *
     * @param sentenceIdx  sentence index
     * @param dependencies input dependencies
     * @param predicate    predicate
     * @return predicate-predicate
     */
    private fun makePPs(sentenceIdx: Int, dependencies: Collection<Dependency>, predicate: Token): List<PP> {
        val pps: MutableList<PP> = ArrayList()

        // predicate
        val predicates2 = findAllOf(dependencies, SemanticRelations.PredicateModifierRelations, predicate)
        for (predicate2 in predicates2) {
            Log.d(TAG, "\tfound predicate (" + predicate.word + ") to predicate2: " + predicate2.dependent.word)

            // PP
            val pp = PP(sentenceIdx + 1, predicate, predicate2.dependent, predicate2.label)
            Log.d(TAG, "\tPP: $pp")
            pps.add(pp)
        }
        return pps
    }

    /**
     * Make term predicate (ex:relative clause)
     *
     * @param sentenceIdx  sentence index
     * @param dependencies dependencies
     * @param terms        terms
     * @return list of term-predicate
     */
    private fun makeTPs(sentenceIdx: Int, dependencies: Collection<Dependency>, vararg terms: Token): List<TP> {
        val tps: MutableList<TP> = ArrayList()
        for (term in terms) {

            // predicate
            val predicates2 = findAllOf(dependencies, SemanticRelations.TermModifierRelations, term)
            for (predicate2 in predicates2) {
                Log.d(TAG, "\tfound term (" + term.word + ") to predicate2: " + predicate2.dependent.word)

                // TP
                val tp = TP(sentenceIdx + 1, term, predicate2.dependent, predicate2.label)
                Log.d(TAG, "\tTP: $tp")
                tps.add(tp)
            }
        }
        return tps
    }

    /**
     * Find first dependency with acceptable label and specific governor
     *
     * @param dependencies     dependencies
     * @param acceptableLabels acceptable labels
     * @param governor         governor
     * @return first relation with acceptable label and specific governor
     */
    private fun findOneOf(dependencies: Collection<Dependency>, acceptableLabels: Set<String>, governor: Token): Dependency? {
        for (d in dependencies) {
            val label = d.label
            val sourceToken = d.governor
            if (acceptableLabels.contains(label) && sourceToken == governor) {
                return d
            }
        }
        return null
    }

    /**
     * Find all dependencies with acceptable label and specific governor
     *
     * @param dependencies     dependencies
     * @param acceptableLabels acceptable labels
     * @param governor         governor
     * @return list of dependencies with acceptable label and specific governor
     */
    private fun findAllOf(dependencies: Collection<Dependency>, acceptableLabels: Set<String>, governor: Token): List<Dependency> {
        val foundDependencies: MutableList<Dependency> = ArrayList()
        for (d in dependencies) {
            val label = d.label
            val sourceToken = d.governor
            if (acceptableLabels.contains(label) && sourceToken == governor) {
                foundDependencies.add(d)
            }
        }
        return foundDependencies
    }

    /**
     * Filter out irrelevant dependencies
     *
     * @param dependencies dependencies
     * @return relevant dependencies
     */
    private fun filter(dependencies: Array<Dependency>): Collection<Dependency> {
        val filteredDependencies: MutableCollection<Dependency> = ArrayList()
        for (d in dependencies) {
            if (accept(d)) {
                filteredDependencies.add(d)
            }
        }
        return filteredDependencies
    }

    /**
     * Accept dependency relevant to semantics
     *
     * @param d tested dependency
     * @return whether this relation is relevant to semantics
     */
    private fun accept(d: Dependency): Boolean {
        val testLabel = d.label
        for (labelSet in SemanticRelations.ALL_GRAMMATICAL_RELATIONS) {
            if (labelSet.contains(testLabel)) {
                return true
            }
        }
        return false
    }

    companion object {

        private const val TAG = "SemanticAnalyzer"
    }
}
