/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope.annotations.paint

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.core.graphics.withSave

object Utils {

    /**
     * Draw triangle
     *
     * @param x0      x coordinate
     * @param y0      y coordinate
     * @param w       arrow width
     * @param h2      arrow half height
     * @param reverse reverse
     * @param rotation rotation
     * @param paint   paint
     */
    @JvmStatic
    fun Canvas.drawTriangle(x0: Float, y0: Float, w: Float, h2: Float, reverse: Boolean, rotation: Float, paint: Paint) {
        val trianglePath = horizontalArrow(x0, y0, w, h2, reverse)
        withSave {
            translate(x0, y0)
            rotate(rotation)
            translate(-x0, -y0)
            drawPath(trianglePath, paint)
        }
    }

    /**
     * Draw horizontal arrow
     *
     * @param x0      x coordinate of tip
     * @param y0      y coordinate of tip
     * @param w       arrow width
     * @param h2      arrow half height
     * @param paint   paint
     * @param leftward points leftward
     */
    @JvmStatic
    fun Canvas.drawHorizontalArrow(x0: Float, y0: Float, w: Float, h2: Float, paint: Paint, leftward: Boolean = false) {
        val trianglePath = horizontalArrow(x0, y0, w, h2, leftward = leftward)
        drawPath(trianglePath, paint)
    }

    /**
     * Draw vertical arrow
     *
     * @param x0      x coordinate of tip
     * @param y0      y coordinate of tip
     * @param w2      half arrow width
     * @param h       arrow height
     * @param paint   paint
     * @param downward points downward
     */
    @JvmStatic
    fun Canvas.drawVerticalArrow(x0: Float, y0: Float, w2: Float, h: Float, paint: Paint, downward: Boolean = false) {
        val trianglePath = verticalArrow(x0, y0, w2, h, downward = downward)
        drawPath(trianglePath, paint)
    }

    /**
     * Draw dot
     *
     * @param x0      x coordinate
     * @param y0      y coordinate
     * @param r       radius
     * @param paint   paint
     */
    @JvmStatic
    fun Canvas.drawDot(x0: Float, y0: Float, r: Float, paint: Paint) {
        if (r == 0.5F) {
            drawPoint(x0, y0, paint)
        }
        val x1 = x0 - r
        val y1 = y0 - r
        val x2 = x0 + r
        val y2 = y0 + r
        drawOval(x1, y1, x2, y2, paint)
    }

    /**
     * Draw diamond
     *
     * @param x0    x coordinate
     * @param y0    y coordinate
     * @param r     radius
     * @param color color
     */
    @JvmStatic
    fun Canvas.drawDiamond(x0: Float, y0: Float, r: Float, paint: Paint) {
        if (r == .5F) {
            drawPoint(x0, y0, paint)
        }
        val x = (x0 - r + .5F)
        val y = (y0 - r + .5F)
        val w = 2F * r
        val h = 2F * r
        val diamond = pathOf(arrayOf(x, x0, x + w, x0), arrayOf(y0, y, y0, y + h))
        drawPath(diamond, paint)
    }

    /**
     * Draw elliptical mark
     *
     * @param x0    x coordinate
     * @param y0    y coordinate
     * @param color color
     */
    @JvmStatic
    fun Canvas.drawEllipticalMark(x0: Float, y0: Float, paint: Paint) {
        drawDiamond(x0, y0, 3F, paint)
    }

    // PATHS

    /**
     * Make horizontal arrow
     * @param x0  tip x coordinate
     * @param y0  tip y coordinate
     * @param w   width
     * @param h2  half height
     * @param leftward leftward
     * @return path
     */
    private fun horizontalArrow(x0: Float, y0: Float, w: Float, h2: Float, leftward: Boolean = false): Path {
        val x1 = x0 + (if (leftward) w else -w)
        return pathOf(arrayOf(x0, x1, x1), arrayOf(y0, y0 - h2, y0 + h2))
    }

    /**
     * Make vertical arrow
     * @param x0  tip x coordinate
     * @param y0  tip y coordinate
     * @param w2  half width
     * @param h   half height
     * @param reverse reverse
     * @return path
     */
    private fun verticalArrow(x0: Float, y0: Float, w2: Float, h: Float, downward: Boolean = false): Path {
        val y1 = y0 + (if (downward) h else -h)
        return pathOf(arrayOf(x0, x0 - w2, x0 + w2), arrayOf(y0, y1, y1))
    }

    /**
     * Path
     *
     * @param x0 x vertices
     * @param y0 y vertices
     * @return path
     */
    private fun pathOf(x0: Array<Float>, y0: Array<Float>): Path {
        return Path().apply {
            moveTo(x0[0], y0[0])
            for (i in 1 until x0.size) {
                lineTo(x0[i], y0[i])
            }
            close()
        }
    }
}