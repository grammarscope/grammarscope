package org.grammarscope.semantics

import android.content.Context
import androidx.preference.PreferenceManager
import java.util.Collections
import java.util.TreeSet
import androidx.core.content.edit

/**
 * Relevant relations for semantics
 *
 * @author Bernard Bou
 */
object SemanticRelations {

    private const val PREF_PREDICATE_LABELS = "pref_predicates"
    private const val PREF_SUBJECT_LABELS = "pref_subjects"
    private const val PREF_OBJECT_LABELS = "pref_objects"
    private const val PREF_TERM_MODIFIER_PREDICATE_LABELS = "pref_term_modifier_predicates"
    private const val PREF_PREDICATE_MODIFIER_PREDICATE_LABELS = "pref_predicate_modifier_predicates"

    /**
     * Semantic predicate
     */
    private val PREDICATE_RELATIONS = arrayOf("root",  /* more */"pred") //, "attr", "acomp"
    private val PREDICATE_RELATION_DEFAULT_SET: Set<String> = Collections.unmodifiableSortedSet(TreeSet(listOf(*PREDICATE_RELATIONS)))

    val PredicateRelations: MutableSet<String> = TreeSet(PREDICATE_RELATION_DEFAULT_SET)

    /**
     * Semantic subject
     */
    private val SUBJECT_RELATIONS = arrayOf("nsubj", "obl:agent",  /* more */"xsubj", "agent")
    private val SUBJECT_RELATION_DEFAULT_SET: Set<String> = Collections.unmodifiableSortedSet(TreeSet(listOf(*SUBJECT_RELATIONS)))

    val SubjectRelations: MutableSet<String> = TreeSet(SUBJECT_RELATION_DEFAULT_SET)

    /**
     * Semantic object
     */
    private val OBJECT_RELATIONS = arrayOf("obj", "iobj", "obl", "obl:npmod", "obl:tmod", "nsubj:pass",  /* more */"dobj", "pobj", "nsubjpass")
    private val OBJECT_RELATION_DEFAULT_SET: Set<String> = Collections.unmodifiableSortedSet(TreeSet(listOf(*OBJECT_RELATIONS)))

    val ObjectRelations: MutableSet<String> = TreeSet(OBJECT_RELATION_DEFAULT_SET)

    /**
     * Semantic term modifier predicate
     */
    private val TERM_MODIFIER_RELATIONS = arrayOf("acl", "acl:relcl",  /* more */"rcmod", "vmod")
    private val TERM_MODIFIER_PREDICATE_RELATION_DEFAULT_SET: Set<String> = Collections.unmodifiableSortedSet(TreeSet(listOf(*TERM_MODIFIER_RELATIONS)))

    val TermModifierRelations: MutableSet<String> = TreeSet(TERM_MODIFIER_PREDICATE_RELATION_DEFAULT_SET)

    /**
     * Semantic predicate modifier predicate
     */
    private val PREDICATE_MODIFIER_RELATIONS = arrayOf("ccomp", "xcomp", "csubj", "csubj:pass", "advcl",  /* more */"purpcl", "csubjpass")
    private val PREDICATE_MODIFIER_PREDICATE_RELATION_DEFAULT_SET: Set<String> = Collections.unmodifiableSortedSet(TreeSet(listOf(*PREDICATE_MODIFIER_RELATIONS)))

    val PredicateModifierRelations: MutableSet<String> = TreeSet(PREDICATE_MODIFIER_PREDICATE_RELATION_DEFAULT_SET)

    /**
     * All
     */
    val ALL_GRAMMATICAL_RELATIONS: Array<Set<String>> = arrayOf(
        PredicateRelations,
        SubjectRelations, ObjectRelations,
        TermModifierRelations, PredicateModifierRelations
    )

    fun read(context: Context) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val predicateSet = sharedPref.getStringSet(PREF_PREDICATE_LABELS, PREDICATE_RELATION_DEFAULT_SET)
        PredicateRelations.clear()
        PredicateRelations.addAll(predicateSet!!)

        val subjectSet = sharedPref.getStringSet(PREF_SUBJECT_LABELS, SUBJECT_RELATION_DEFAULT_SET)
        SubjectRelations.clear()
        SubjectRelations.addAll(subjectSet!!)

        val objectSet = sharedPref.getStringSet(PREF_OBJECT_LABELS, OBJECT_RELATION_DEFAULT_SET)
        ObjectRelations.clear()
        ObjectRelations.addAll(objectSet!!)

        val termModifierPredicateSet = sharedPref.getStringSet(PREF_TERM_MODIFIER_PREDICATE_LABELS, TERM_MODIFIER_PREDICATE_RELATION_DEFAULT_SET)
        TermModifierRelations.clear()
        TermModifierRelations.addAll(termModifierPredicateSet!!)

        val predicateModifierPredicateSet = sharedPref.getStringSet(PREF_PREDICATE_MODIFIER_PREDICATE_LABELS, PREDICATE_MODIFIER_PREDICATE_RELATION_DEFAULT_SET)
        PredicateModifierRelations.clear()
        PredicateModifierRelations.addAll(predicateModifierPredicateSet!!)
    }

    fun write(context: Context) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPref.edit {
            putStringSet(PREF_PREDICATE_LABELS, PredicateRelations)
                .putStringSet(PREF_SUBJECT_LABELS, SubjectRelations)
                .putStringSet(PREF_OBJECT_LABELS, ObjectRelations)
                .putStringSet(PREF_TERM_MODIFIER_PREDICATE_LABELS, TermModifierRelations)
                .putStringSet(PREF_PREDICATE_MODIFIER_PREDICATE_LABELS, PredicateModifierRelations)
        }
    }

    fun unregister(context: Context) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPref.edit {
            remove(PREF_PREDICATE_LABELS)
                .remove(PREF_SUBJECT_LABELS)
                .remove(PREF_OBJECT_LABELS)
                .remove(PREF_TERM_MODIFIER_PREDICATE_LABELS)
                .remove(PREF_PREDICATE_MODIFIER_PREDICATE_LABELS)
        }
    }
}
