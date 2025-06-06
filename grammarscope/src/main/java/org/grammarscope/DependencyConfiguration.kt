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
import org.grammarscope.graph.CommonSettings.Companion.PREF_ROOT_VERTEX_COLOR
import org.grammarscope.graph.CommonSettings.Companion.PREF_ROOT_VERTEX_ICON
import org.grammarscope.graph.CommonSettings.Companion.PREF_ROOT_VERTEX_LABEL_BACK_COLOR
import org.grammarscope.graph.CommonSettings.Companion.PREF_ROOT_VERTEX_LABEL_COLOR
import org.grammarscope.graph.CommonSettings.Companion.PREF_ROOT_VERTEX_SHAPE
import org.grammarscope.graph.GraphColors

class DependencyGraphConfiguration(settings: Settings) :
    Configuration<Token, Token>(settings) {

    val nodeShape: VertexShapeFactory = settings.makeVertexShape(PREF_VERTEX_SHAPE)
    val nodeIcon: Icon? = settings.makeIcon(PREF_VERTEX_ICON)
    val nodeColor: Color? = settings.makeColor(PREF_VERTEX_COLOR, GraphColors.vertexColor)
    val nodeSize = settings.makeInt(PREF_VERTEX_SIZE, settings.defaultVertexSize)
    val nodeLabelFillColor: Color? = settings.makeColorOrNull(PREF_VERTEX_LABEL_BACK_COLOR)
    val nodeLabelDrawColor: Color? = settings.makeColor(PREF_VERTEX_LABEL_COLOR, GraphColors.vertexLabelColor)
    val nodeLabelSize = vertexLabelSize

    val edgeColor: Color? = edgeDrawColor

    val rootShape: VertexShapeFactory = settings.makeVertexShape(PREF_ROOT_VERTEX_SHAPE)
    val rootIcon: Icon? = settings.makeIcon(PREF_ROOT_VERTEX_ICON)
    val rootColor: Color? = settings.makeColor(PREF_ROOT_VERTEX_COLOR, GraphColors.rootVertexColor)
    val rootSize = (nodeSize * 1.5f).toInt()
    val rootLabelSize = (nodeLabelSize * 1.5f).toInt()
    val rootLabelDrawColor: Color? = settings.makeColor(PREF_ROOT_VERTEX_LABEL_COLOR, GraphColors.vertexLabelColor)
    val rootLabelFillColor: Color? = settings.makeColorOrNull(PREF_ROOT_VERTEX_LABEL_BACK_COLOR)

    init {
        layoutAlgorithm = settings.makeLayoutAlgorithm()
        edgeShapeFactory = settings.makeEdgeShape()
    }
}

class DependencyGraphConfigurator(depConfig: DependencyGraphConfiguration) : Configurator<Token, Token>(depConfig) {

    override fun readFunctions(decorator: IDecorator<Token, Token>) {
        super.readFunctions(decorator)

        val depConfig = config as DependencyGraphConfiguration
        val vertexToShape = { v: Token, _: Network<Token, Token> -> if (isRoot(v)) depConfig.rootShape else depConfig.nodeShape }
        val vertexToIcon = { v: Token, _: Network<Token, Token> -> if (isRoot(v)) depConfig.rootIcon else depConfig.nodeIcon }
        val vertexToColor = { v: Token, _: Network<Token, Token> -> if (isRoot(v)) depConfig.rootColor else depConfig.nodeColor }
        val vertexToSize = { v: Token, _: Network<Token, Token> -> if (isRoot(v)) depConfig.rootSize else depConfig.nodeSize }
        val vertexToLabelSize = { v: Token, _: Network<Token, Token> -> if (isRoot(v)) depConfig.rootLabelSize else depConfig.nodeLabelSize }
        val vertexToLabelFillColor = { v: Token, _: Network<Token, Token> -> if (isRoot(v)) depConfig.rootLabelFillColor else depConfig.nodeLabelFillColor }
        val vertexToLabelDrawColor = { v: Token, _: Network<Token, Token>? -> if (isRoot(v)) depConfig.rootLabelDrawColor else depConfig.nodeLabelDrawColor }
        val edgeToColor = { e: Token, _: Network<Token, Token> -> depConfig.edgeColor }
        val vertexToLabel = { v: Token, _: Network<Token, Token> -> v.toString() }
        val edgeToLabel = { e: Token, _: Network<Token, Token> -> e.label }

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
}
