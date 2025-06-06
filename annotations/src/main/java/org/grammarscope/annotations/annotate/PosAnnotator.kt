/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope.annotations.annotate

import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import android.widget.TextView
import org.depparse.HasIndex
import org.depparse.HasSegment
import org.depparse.Token
import org.depparse.common.AppValues
import org.grammarscope.annotations.annotate.AnnotationManager.Type
import org.grammarscope.annotations.annotation.Annotation
import org.grammarscope.annotations.annotation.Annotation.LabelAnnotation
import org.grammarscope.annotations.annotation.AnnotationType
import org.grammarscope.annotations.document.Document
import org.grammarscope.annotations.document.Graph
import org.grammarscope.annotations.paint.Metrics.height
import org.grammarscope.annotations.paint.Palette
import org.grammarscope.annotations.segmentToViewRectF

/**
 * Semantic graph renderer
 *
 * @param textView textView
 *
 * @author Bernard Bou
 */
class PosAnnotator<N>(
    val textView: TextView,
    val manager: AnnotationManager,
    val ignoreRelations: Set<String> = setOf(),
) where N : HasIndex, N : HasSegment {

    /**
     * Paint for tag
     */
    private val posPaint: Paint = Paint().apply { textSize = AnnotationManager.LABEL_TEXT_SIZE }

    /**
     * Annotate
     *
     * @param document document to annotate from
     * @return annotations per type
     */
    fun annotate(
        document: Document<N>,
    ): Map<AnnotationType, Collection<Annotation>>? {
        if (document.sentenceCount == 0) return null

        // dumpLines()
        Log.d(TAG, "Annotating")

        // space allocation
        val padTopOffset = manager.allocate(Type.POS)

        // annotations
        val boxes: MutableCollection<LabelAnnotation> = ArrayList()

        // space height
        val padHeight = textView.lineSpacingExtra
        Log.d(TAG, "-padHeight $padHeight")
        val tagFontHeight: Float = posPaint.fontMetrics.height()
        val tagHeight: Float = tagFontHeight + 2 * LABEL_INFLATE
        val tagSpace: Float = tagHeight + LABEL_TOP_INSET + LABEL_BOTTOM_INSET
        Log.d(TAG, "-pos space $tagSpace")

        // keys
        val posKeyFields = AppValues.posKey.split("++")
        val posKey = posKeyFields[0]
        val posIndex = if (posKeyFields.size > 1) posKeyFields[1].toInt() else null

        // iterate over sentences
        val n: Int = document.sentenceCount
        for (sentenceIdx in 0..<n) {
            val graph: Graph<N> = document.getGraph(sentenceIdx)

            // NODES
            for (node in graph.nodes) {
                val token = node as Token

                // relation that labels edge
                val label: String? = node.label
                if (label != null && ignoreRelations.contains(label))
                    continue

                // pos
                val tagMap = Token.TokenTagProcessor.splitTag(token.tag)
                Log.d(TAG, "token $token $tagMap")
                val pos = if (posIndex == null) tagMap[posKey]!! else tagMap[posKey]?.split("++")!![posIndex]
                Log.d(TAG, "pos $token $pos}")

                // location
                val segment = document.getTextSegment(sentenceIdx, node.segment)
                val wordBox = textView.segmentToViewRectF(segment)

                val annotationTop = wordBox.top + manager.lineHeight
                val annotationBottom = annotationTop + padHeight
                val annotationBox = RectF(wordBox.left, annotationTop + padTopOffset + PAD_TOP_INSET, wordBox.right, annotationBottom - PAD_BOTTOM_INSET)
                boxes.add(LabelAnnotation(annotationBox, pos.toString(), backColor = null, foreColor = Palette.posColor))
            }
        }
        return mapOf(AnnotationType.LABEL to boxes)
    }

    companion object {
        const val TAG = "Annotator"

        private const val PAD_TOP_INSET = 4

        private const val PAD_BOTTOM_INSET = 2

        private const val LABEL_TOP_INSET = 15

        private const val LABEL_BOTTOM_INSET = 15

        private const val LABEL_INFLATE = 1
    }
}