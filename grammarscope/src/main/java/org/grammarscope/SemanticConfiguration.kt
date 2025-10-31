/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope

import edu.uci.ics.jung.graph.Network
import edu.uci.ics.jung.settings.BaseSettings.Companion.PREF_VERTEX_COLOR
import edu.uci.ics.jung.settings.BaseSettings.Companion.PREF_VERTEX_ICON
import edu.uci.ics.jung.settings.BaseSettings.Companion.PREF_VERTEX_LABEL_BACK_COLOR
import edu.uci.ics.jung.settings.BaseSettings.Companion.PREF_VERTEX_LABEL_COLOR
import edu.uci.ics.jung.settings.BaseSettings.Companion.PREF_VERTEX_SHAPE
import edu.uci.ics.jung.settings.BaseSettings.Companion.PREF_VERTEX_SIZE
import edu.uci.ics.jung.settings.Configuration
import edu.uci.ics.jung.settings.Configurator
import edu.uci.ics.jung.settings.Settings
import edu.uci.ics.jung.visualization.decorators.IDecorator
import edu.uci.ics.jung.visualization.decorators.vertex.VertexShapeFactory
import glue.Color
import glue.Icon
import org.depparse.Token
import org.grammarscope.ColorSettings.Companion.PREF_OBJECT_COLOR
import org.grammarscope.ColorSettings.Companion.PREF_PREDICATE_COLOR
import org.grammarscope.ColorSettings.Companion.PREF_PREDICATE_MODIFYING_SUBPREDICATE_COLOR
import org.grammarscope.ColorSettings.Companion.PREF_SUBJECT_COLOR
import org.grammarscope.ColorSettings.Companion.PREF_TERM_MODIFYING_SUBPREDICATE_COLOR
import org.grammarscope.graph.CommonSettings.Companion.PREF_ROOT_VERTEX_COLOR
import org.grammarscope.graph.CommonSettings.Companion.PREF_ROOT_VERTEX_ICON
import org.grammarscope.graph.CommonSettings.Companion.PREF_ROOT_VERTEX_LABEL_BACK_COLOR
import org.grammarscope.graph.CommonSettings.Companion.PREF_ROOT_VERTEX_LABEL_COLOR
import org.grammarscope.graph.CommonSettings.Companion.PREF_ROOT_VERTEX_SHAPE
import org.grammarscope.graph.GraphColors
import org.grammarscope.semantics.PO
import org.grammarscope.semantics.PP
import org.grammarscope.semantics.PS
import org.grammarscope.semantics.Relation
import org.grammarscope.semantics.SemanticRelations
import org.grammarscope.semantics.TP

class SemanticGraphConfiguration(settings: Settings) :
    Configuration<Token, Relation>(settings) {

    val nodeShape: VertexShapeFactory = settings.makeVertexShape(PREF_VERTEX_SHAPE)
    val nodeIcon: Icon? = settings.makeIcon(PREF_VERTEX_ICON)
    val nodeColor: Color = settings.makeColor(PREF_VERTEX_COLOR, GraphColors.vertexColor)
    val nodeSize = settings.makeInt(PREF_VERTEX_SIZE, settings.defaultVertexSize)
    val nodeLabelFillColor: Color? = settings.makeColorOrNull(PREF_VERTEX_LABEL_BACK_COLOR)
    val nodeLabelDrawColor: Color? = settings.makeColorOrNull(PREF_VERTEX_LABEL_COLOR)
    val nodeLabelSize = vertexLabelSize

    val rootShape: VertexShapeFactory = settings.makeVertexShape(PREF_ROOT_VERTEX_SHAPE)
    val rootIcon: Icon? = settings.makeIcon(PREF_ROOT_VERTEX_ICON)
    val rootColor: Color = settings.makeColor(PREF_ROOT_VERTEX_COLOR, GraphColors.rootVertexColor)
    val rootSize = (vertexSize * 1.5f).toInt()
    val rootLabelSize = (nodeLabelSize * 1.5f).toInt()
    val rootLabelDrawColor: Color? = settings.makeColorOrNull(PREF_ROOT_VERTEX_LABEL_COLOR)
    val rootLabelFillColor: Color? = settings.makeColorOrNull(PREF_ROOT_VERTEX_LABEL_BACK_COLOR)

    val predicateColor: Color = settings.makeColor(PREF_PREDICATE_COLOR, GraphColors.predicateColor)
    val subjectColor: Color = settings.makeColor(PREF_SUBJECT_COLOR, GraphColors.subjectColor)
    val objectColor: Color = settings.makeColor(PREF_OBJECT_COLOR, GraphColors.objectColor)
    val termModifyingPredicateColor: Color = settings.makeColor(PREF_TERM_MODIFYING_SUBPREDICATE_COLOR, GraphColors.termModifyingPredicateColor)
    val predicateModifyingPredicateColor: Color = settings.makeColor(PREF_PREDICATE_MODIFYING_SUBPREDICATE_COLOR, GraphColors.predicateModifyingPredicateColor)

    init {
        layoutAlgorithm = settings.makeLayoutAlgorithm()
        edgeShapeFactory = settings.makeEdgeShape()
    }
}

class SemanticGraphConfigurator(semanticConfig: SemanticGraphConfiguration) : Configurator<Token, Relation>(semanticConfig) {

    override fun readFunctions(decorator: IDecorator<Token, Relation>) {
        super.readFunctions(decorator)

        val semanticConfig = config as SemanticGraphConfiguration
        val vertexToShape = { v: Token, _: Network<Token, Relation> -> if (isRoot(v)) semanticConfig.rootShape else semanticConfig.nodeShape }
        val vertexToIcon = { v: Token, _: Network<Token, Relation> -> if (isRoot(v)) semanticConfig.rootIcon else semanticConfig.nodeIcon }
        val vertexToColor = { v: Token, _: Network<Token, Relation> -> if (isRoot(v)) semanticConfig.rootColor else if (isPredicate(v)) semanticConfig.predicateColor else semanticConfig.nodeColor }
        val vertexToSize = { v: Token, _: Network<Token, Relation> -> if (isRoot(v)) semanticConfig.rootSize else semanticConfig.nodeSize }
        val vertexToLabelSize = { v: Token, _: Network<Token, Relation> -> if (isRoot(v)) semanticConfig.rootLabelSize else semanticConfig.nodeLabelSize }
        val vertexToLabelFillColor = { v: Token, _: Network<Token, Relation> -> if (isRoot(v)) semanticConfig.rootLabelFillColor else semanticConfig.nodeLabelFillColor }
        val vertexToLabelDrawColor = { v: Token, _: Network<Token, Relation> -> if (isRoot(v)) semanticConfig.rootLabelDrawColor else if (isPredicate(v)) semanticConfig.predicateColor else semanticConfig.nodeLabelDrawColor }
        val edgeToColor = { e: Relation, _: Network<Token, Relation> -> relationToColor(e) }
        val vertexToLabel = { v: Token, _: Network<Token, Relation> -> v.toString() }
        val edgeToLabel = { e: Relation, _: Network<Token, Relation> -> e.label }

        decorator.vertexToIcon = vertexToIcon
        decorator.vertexToShape = vertexToShape
        decorator.vertexToSize = vertexToSize
        decorator.vertexToFillColor = vertexToColor
        decorator.vertexToLabelSize = vertexToLabelSize
        decorator.vertexToLabelFillColor = vertexToLabelFillColor
        decorator.vertexToLabelDrawColor = vertexToLabelDrawColor
        decorator.edgeToDrawColor = edgeToColor
        decorator.edgeToLabelDrawColor = edgeToColor
        decorator.edgeToArrowDrawColor = edgeToColor
        decorator.edgeToArrowFillColor = edgeToColor
        decorator.vertexToLabel = vertexToLabel
        decorator.edgeToLabel = edgeToLabel
    }

    private fun isRoot(t: Token): Boolean {
        return "root" == t.label
    }

    private fun isPredicate(t: Token): Boolean {
        val label = t.label
        return SemanticRelations.PredicateRelations.contains(label) ||
                SemanticRelations.PredicateModifierRelations.contains(label) ||
                SemanticRelations.TermModifierRelations.contains(label)
    }

    private fun relationToColor(r: Relation): Color? {
        val semanticConfig = config as SemanticGraphConfiguration
        return when (r) {
            is PS -> semanticConfig.subjectColor
            is PO -> semanticConfig.objectColor
            is TP -> semanticConfig.termModifyingPredicateColor
            is PP -> semanticConfig.predicateModifyingPredicateColor
            else -> semanticConfig.edgeDrawColor
        }
    }
}
