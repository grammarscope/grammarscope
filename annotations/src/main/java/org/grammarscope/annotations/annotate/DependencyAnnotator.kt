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
import org.depparse.Segment
import org.depparse.Token
import org.grammarscope.annotations.AnnotatedTextView.Companion.foreHighlightWord
import org.grammarscope.annotations.allocator.Allocator
import org.grammarscope.annotations.annotate.AnnotationManager.Companion.EDGE_TAG_TEXT_SIZE
import org.grammarscope.annotations.annotate.AnnotationManager.Type
import org.grammarscope.annotations.annotation.Annotation
import org.grammarscope.annotations.annotation.Annotation.BoxAnnotation
import org.grammarscope.annotations.annotation.Annotation.EdgeAnnotation
import org.grammarscope.annotations.annotation.AnnotationType
import org.grammarscope.annotations.annotation.Edge
import org.grammarscope.annotations.annotation.Edge.Companion.makeEdge
import org.grammarscope.annotations.document.Document
import org.grammarscope.annotations.document.Graph
import org.grammarscope.annotations.document.SegmentUtils
import org.grammarscope.annotations.modelToViewF
import org.grammarscope.annotations.paint.Metrics.height
import org.grammarscope.annotations.paint.Palette
import org.grammarscope.annotations.paint.Palette.setAlpha
import org.grammarscope.annotations.segmentToViewRectF

/**
 * Dependency annotator
 *
 * @param textView textView
 *
 * @author Bernard Bou
 */
class DependencyAnnotator<N>(
    val textView: TextView,
    val manager: AnnotationManager,
    val boxWords: Boolean = true,
    val boxEdges: Boolean = true,
    val ignoreRelations: Set<String> = setOf(),

    ) where N : HasIndex, N : HasSegment {

    /**
     * Paint for tag
     */
    private val tagPaint: Paint = Paint().apply { textSize = EDGE_TAG_TEXT_SIZE }

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

        val wordSegments = document.wordSegments

        // dumpLines()
        Log.d(TAG, "Annotating")

        // space allocation
        val padTopOffset = manager.allocate(Type.DEP)

        // annotations
        val edges: MutableCollection<EdgeAnnotation> = ArrayList()
        val boxes: MutableCollection<BoxAnnotation> = ArrayList()

        // space height
        val padHeight = textView.lineSpacingExtra
        Log.d(TAG, "-padHeight $padHeight")
        val tagFontHeight: Float = tagPaint.fontMetrics.height()
        val tagHeight: Float = tagFontHeight + 2 * LABEL_INFLATE
        val tagSpace: Float = tagHeight + LABEL_TOP_INSET + LABEL_BOTTOM_INSET
        Log.d(TAG, "-edge space $tagSpace")

        // where edges start (slot 0)
        val firstEdgeBase: Float = PAD_TOP_INSET + EDGES_TOP_INSET + tagSpace // relative to pad topOffset
        if (firstEdgeBase > padHeight) return null
        val lastEdgeBase: Float = padHeight - PAD_BOTTOM_INSET - EDGES_BOTTOM_INSET // relative to pad
        Log.d(TAG, "-from $firstEdgeBase to $lastEdgeBase")

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

                // data
                val color: Int = Palette.invoke(token.label).setAlpha(if ("root" == token.label) Palette.ALPHA_ROOT else Palette.ALPHA)

                // location
                val textSegment = document.getTextSegment(sentenceIdx, node.segment)
                val wordRect = textView.segmentToViewRectF(textSegment)

                // box word
                if (boxWords) {
                    boxes.add(BoxAnnotation(wordRect, color.setAlpha(0x7F), true))
                }

                // box edge space
                if (boxEdges) {
                    val annotationTop = wordRect.top + manager.lineHeight
                    val annotationBottom = annotationTop + padHeight
                    val annotationBox = RectF(wordRect.left, annotationTop + padTopOffset + PAD_TOP_INSET, wordRect.right, annotationBottom - PAD_BOTTOM_INSET)
                    boxes.add(BoxAnnotation(annotationBox, color.setAlpha(0xCF)))
                }

                // pos
                val tagMap = Token.TokenTagProcessor.splitTag(token.tag)
                Log.d(TAG, "token $token $tagMap")
                val pos = tagMap["fPOS"]?.split("++")
                Log.d(TAG, "pos $token ${pos?.get(0)} ${pos?.get(1)}")

                // root
                if ("root" == token.label) {
                    val textSegment = document.getTextSegment(sentenceIdx, token.segment)
                    val wordStart: Int = textSegment.first
                    val wordEnd: Int = textSegment.second + 1
                    //textView.backHighlightWord(wordStart, wordEnd, Palette.rootBackColor)
                    textView.foreHighlightWord(wordStart, wordEnd, Palette.rootColor)
                }
            }

            // EDGES

            // height and anchor allocator
            val allocator = Allocator<N>(graph.nodes, graph.edges)

            // build edges
            for (gEdge in graph.edges) {
                // relation that labels edge
                val label: String? = gEdge.label
                if (label != null && ignoreRelations.contains(label))
                    continue

                // segment
                val fromWord = document.getTextSegment(sentenceIdx, gEdge.source.segment)
                val toWord = document.getTextSegment(sentenceIdx, gEdge.target.segment)

                val isBackwards = fromWord.first > toWord.first
                val leftSegment = if (isBackwards) toWord else fromWord
                val rightSegment = if (isBackwards) fromWord else toWord

                // location
                val leftRectangle = textView.segmentToViewRectF(leftSegment)
                val rightRectangle = textView.segmentToViewRectF(rightSegment)

                // data
                val color: Int = Palette.invoke(gEdge.label)
                val slot = allocator.getSlot(gEdge)
                val edgeYOffset = slot * tagSpace // relative to first slot
                val bottom = manager.lineHeight + padHeight
                val isVisible = firstEdgeBase + edgeYOffset < lastEdgeBase
                Log.d(TAG, "edge $label at slot=$slot ofs=$edgeYOffset y=${leftRectangle.top + manager.lineHeight + padTopOffset + firstEdgeBase + edgeYOffset} visible=$isVisible")

                // if it fits on one line
                if (leftRectangle.top == rightRectangle.top) {
                    // compute edge
                    val xEdge1 = leftRectangle.left + leftRectangle.width() / 2F
                    val xEdge2 = rightRectangle.left + rightRectangle.width() / 2F
                    val yEdge = leftRectangle.top + manager.lineHeight + padTopOffset + firstEdgeBase + edgeYOffset
                    val xAnchor1 = (allocator.getLeftAnchor(gEdge) * X_SHIFT)
                    val xAnchor2 = (allocator.getRightAnchor(gEdge) * X_SHIFT)
                    val yAnchor: Float = leftRectangle.top + manager.lineHeight + padTopOffset + PAD_TOP_INSET
                    val yBottom = leftRectangle.top + bottom

                    val edge: Edge = makeEdge(xEdge1, xEdge2, yEdge, xAnchor1, xAnchor2, yAnchor, tagHeight, yBottom, label, isBackwards, isLeftTerminal = true, isRightTerminal = true, isVisible, color, tagPaint = tagPaint)
                    edges.add(EdgeAnnotation(edge))

                } else {
                    // edge does not fit on line : make one edge per line

                    // get text word segments in span
                    val words: MutableList<Segment> = SegmentUtils.split(leftSegment, rightSegment, wordSegments)
                    if (words.isEmpty()) {
                        continue
                    }
                    val lastSegmentIndex = words.size // last

                    // cursor
                    var xRight = leftRectangle.left + leftRectangle.width()
                    var xLeft = leftRectangle.left
                    var xLeftOfs = leftRectangle.width() / 2
                    var y = leftRectangle.top

                    // first segment in line
                    var isFirst = true

                    // iterate over word segments
                    var currentSegmentIndex = 0
                    for (word in words) {
                        currentSegmentIndex++

                        // if is not visible
                        val textBox = textView.segmentToViewRectF(word)

                        // if line skipped (the current segment is on the current line)
                        if (textBox.top != y) {
                            // line break before this segment
                            val xEdge1 = xLeft + xLeftOfs - (if (isFirst) 0 else X_MARGIN)
                            val xEdge2 = xRight + X_MARGIN
                            val xAnchor1 = (allocator.getLeftAnchor(gEdge) * X_SHIFT)
                            val xAnchor2 = (allocator.getRightAnchor(gEdge) * X_SHIFT)
                            val yEdge = y + manager.lineHeight + padTopOffset + firstEdgeBase + edgeYOffset
                            val yAnchor: Float = y + manager.lineHeight + padTopOffset + PAD_TOP_INSET
                            val yBottom = y + bottom

                            val edge: Edge = makeEdge(xEdge1, xEdge2, yEdge, xAnchor1, xAnchor2, yAnchor, tagHeight, yBottom, label, isBackwards, isLeftTerminal = isFirst, isRightTerminal = false, isVisible, color, tagPaint = tagPaint)
                            edges.add(EdgeAnnotation(edge))

                            // move ahead cursor1
                            val rectangle2 = textView.modelToViewF(word.first)
                            xLeft = rectangle2.left
                            xLeftOfs = rectangle2.width() / 2
                            y = rectangle2.top
                            isFirst = false
                        }

                        // move ahead cursor2
                        xRight = textBox.left + textBox.width()
                        val xRightOfs = textBox.width() / 2

                        // finish it off if this is the last segment
                        if (currentSegmentIndex == lastSegmentIndex) {
                            // last segment
                            val xEdge1 = xLeft + xLeftOfs - (if (isFirst) 0 else X_MARGIN)
                            val xEdge2 = xRight - xRightOfs
                            val yEdge = y + manager.lineHeight + padTopOffset + firstEdgeBase + edgeYOffset
                            val xAnchor1 = (allocator.getLeftAnchor(gEdge) * X_SHIFT)
                            val xAnchor2 = (allocator.getRightAnchor(gEdge) * X_SHIFT)
                            val yAnchor: Float = y + manager.lineHeight + padTopOffset + PAD_TOP_INSET
                            val yBottom = y + bottom

                            val edge: Edge = makeEdge(xEdge1, xEdge2, yEdge, xAnchor1, xAnchor2, yAnchor, tagHeight, yBottom, label, isBackwards, isLeftTerminal = isFirst, isRightTerminal = true, isVisible, color, tagPaint = tagPaint)
                            edges.add(EdgeAnnotation(edge))
                        }
                    }
                }
            }
        }
        return mapOf(AnnotationType.EDGE to edges, AnnotationType.BOX to boxes)
    }

    companion object {
        const val TAG = "DepAnnotator"

        private const val PAD_TOP_INSET = 4

        private const val PAD_BOTTOM_INSET = 10

        private const val EDGES_TOP_INSET = 10

        private const val EDGES_BOTTOM_INSET = 60

        private const val LABEL_TOP_INSET = 15

        private const val LABEL_BOTTOM_INSET = 15

        private const val LABEL_INFLATE = 1

        private const val X_MARGIN = 80

        private const val X_SHIFT = 20
    }
}