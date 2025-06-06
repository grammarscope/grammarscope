package org.depparse.common

import android.content.Context
import android.graphics.Color
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import androidx.core.content.ContextCompat

/**
 * Color values
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
object Colors {

    // only the back colors are saved, the fore color is computed, except root
    private const val NOT_DEFINED = -0x55555556

    var rootBackColor = Color.BLACK

    var rootColor = Color.WHITE

    var labelBackColor = Color.WHITE

    var labelColor = Color.BLACK

    var enhancedLabelBackColor = Color.WHITE

    var enhancedLabelColor = Color.BLACK

    var headBackColor = Color.WHITE

    var headColor = Color.BLACK

    var dependentBackColor = Color.WHITE

    var dependentColor = Color.BLACK

    var predicateBackColor = Color.BLACK

    var predicateColor = Color.WHITE

    var subjectBackColor = Color.WHITE

    var subjectColor = Color.BLACK

    var objectBackColor = Color.WHITE

    var objectColor = Color.BLACK

    var termModifierPredicateBackColor = Color.WHITE

    var termModifierPredicateColor = Color.BLACK

    var predicateModifierPredicateBackColor = Color.WHITE

    var predicateModifierPredicateColor = Color.BLACK

    @ColorInt
    fun computeForeColor(@ColorInt backColor: Int): Int {
        val brightness = Color.red(backColor) * 0.299 + Color.green(backColor) * 0.587 + Color.blue(backColor) * 0.114
        return if (brightness < 160) Color.WHITE else Color.BLACK
    }

    fun setColorsFromResources(context: Context) {
        val palette = context.resources.getIntArray(R.array.palette)
        var i = 0
        rootBackColor = palette[i++]
        rootColor = palette[i++]
        labelBackColor = palette[i++]
        labelColor = palette[i++]
        enhancedLabelBackColor = palette[i++]
        enhancedLabelColor = palette[i++]
        headBackColor = palette[i++]
        headColor = palette[i++]
        dependentBackColor = palette[i++]
        dependentColor = palette[i++]
        predicateBackColor = palette[i++]
        predicateColor = palette[i++]
        subjectBackColor = palette[i++]
        subjectColor = palette[i++]
        objectBackColor = palette[i++]
        objectColor = palette[i++]
        termModifierPredicateBackColor = palette[i++]
        termModifierPredicateColor = palette[i++]
        predicateModifierPredicateBackColor = palette[i++]
        predicateModifierPredicateColor = palette[i++]
        if (BuildConfig.DEBUG && i != palette.size) {
            throw AssertionError("Assertion failed")
        }
    }

    fun getColors(context: Context, @ColorRes vararg colorIds: Int): IntArray {
        val result = IntArray(colorIds.size)
        for (i in colorIds.indices) {
            result[i] = ContextCompat.getColor(context, colorIds[i])
        }
        return result
    }

    fun getColorAttrs(context: Context, @StyleRes themeId: Int, @StyleableRes resIds: IntArray): IntArray {
        val result: IntArray
        context.theme.obtainStyledAttributes(themeId, resIds).let {
            result = IntArray(resIds.size)
            for (i in resIds.indices) {
                result[i] = it.getColor(i, NOT_DEFINED)
            }
            it.recycle()
        }
        return result
    }

    fun getColorAttr(context: Context, @StyleRes themeId: Int, @AttrRes resId: Int): Int {
        val result: Int
        context.theme.obtainStyledAttributes(themeId, intArrayOf(resId)).let {
            result = it.getColor(0, NOT_DEFINED)
            it.recycle()
        }
        return result
    }
}