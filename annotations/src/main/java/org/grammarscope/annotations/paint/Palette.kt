/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope.annotations.paint

import android.content.Context
import android.graphics.Color
import android.graphics.Color.argb
import android.graphics.Color.blue
import android.graphics.Color.green
import android.graphics.Color.red
import android.graphics.Color.rgb
import android.util.Log
import org.grammarscope.annotations.AnnotationsSettings.Companion.PREF_EDGE_COLOR
import org.grammarscope.annotations.AnnotationsSettings.Companion.PREF_LABEL_COLOR
import org.grammarscope.annotations.AnnotationsSettings.Companion.PREF_META_COLOR
import org.grammarscope.annotations.AnnotationsSettings.Companion.PREF_POS_COLOR
import org.grammarscope.annotations.AnnotationsSettings.Companion.PREF_ROOT_COLOR
import org.grammarscope.annotations.AnnotationsSettings.Companion.getSharedPreferences
import org.grammarscope.annotations.R
import org.grammarscope.annotations.paint.ColorsJson.getColorMap
import java.util.Collections.enumeration
import java.util.Enumeration
import java.util.Properties
import kotlin.random.Random

/**
 * Palette
 *
 * @author Bernard Bou
 */
object Palette : (String?) -> Int {

    const val ALPHA = 0x40

    const val ALPHA_ROOT = 0x80

    /**
     * Relation id to color map
     */
    internal val colorMap: MutableMap<String, Int> = HashMap() //makeColorMap().toMutableMap()

    /**
     * Random
     */
    private val random = Random.Default

    private val randomColor
        get() = rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255))


    // P A L E T T E

    override fun invoke(id: String?): Int {
        if (id == null)
            return randomColor
        var color: Int? = colorMap[id]
        if (color != null)
            return color
        if (id.contains(':')) {
            // subrelation
            val id2 = id.substring(0, id.indexOf(':'))
            color = colorMap[id2]
        }
        if (color == null) {
            Log.d(TAG, "No color: $id")
            color = randomColor
            colorMap[id] = color
        }
        return color
    }

    fun setColorsFromResources(context: Context) {
        Log.d(TAG, "setColorsFromResources")

        val resources = context.resources
        edgeColor = resources.getColor(R.color.edge, context.theme)
        arrowTipColor = resources.getColor(R.color.arrowTip, context.theme)
        arrowStartColor = resources.getColor(R.color.arrowStart, context.theme)
        labelColor = resources.getColor(R.color.label, context.theme)
        posColor = resources.getColor(R.color.pos, context.theme)
        overflowColor = resources.getColor(R.color.overflow, context.theme)
        spanColor = resources.getColor(R.color.span, context.theme)
        rootColor = resources.getColor(R.color.root, context.theme)

        val paletteKeys = resources.getStringArray(R.array.relations_keys)
        val paletteValues = resources.getIntArray(R.array.palette_relations_values)
        require(paletteValues.size == paletteKeys.size)
        for (i in 0 until paletteKeys.size) {
            val k = paletteKeys[i]
            val v = paletteValues[i]
            colorMap[k] = v
        }
    }

    fun setColorsFromPreferences(context: Context) {
        Log.d(TAG, "setColorsFromPreferences")

        val sharedPrefs = getSharedPreferences(context)
        if (sharedPrefs.contains(PREF_EDGE_COLOR)) {
            val color = sharedPrefs.getInt(PREF_EDGE_COLOR, -1)
            if (color != -1) {
                edgeColor = color
                arrowTipColor = color
                arrowStartColor = color
            }
        }
        if (sharedPrefs.contains(PREF_LABEL_COLOR)) {
            val color = sharedPrefs.getInt(PREF_LABEL_COLOR, -1)
            if (color != -1) {
                labelColor = color
            }
        }
        if (sharedPrefs.contains(PREF_ROOT_COLOR)) {
            val color = sharedPrefs.getInt(PREF_ROOT_COLOR, -1)
            if (color != -1) {
                rootColor = color
            }
        }
        if (sharedPrefs.contains(PREF_POS_COLOR)) {
            val color = sharedPrefs.getInt(PREF_POS_COLOR, -1)
            if (color != -1) {
                posColor = color
            }
        }
        if (sharedPrefs.contains(PREF_META_COLOR)) {
            val color = sharedPrefs.getInt(PREF_META_COLOR, -1)
            if (color != -1) {
                overflowColor = color
                spanColor = color
            }
        }
        val preferenceMap: Map<String, Int>? = getColorMap(sharedPrefs)
        if (preferenceMap != null)
            colorMap.putAll(preferenceMap)
    }

    // S T R I N G

    override fun toString(): String {
        val sb = StringBuilder()
        for (id in colorMap.keys.sorted()) {
            val color: Int? = colorMap[id]
            if (color == null) {
                continue
            }
            val key = "color-$id"
            val value = "${red(color)}, ${green(color)}, ${blue(color)}"
            sb.append(key)
            sb.append("=")
            sb.append(value)
            sb.append("\n")
        }
        return sb.toString()
    }

    // P E R S I S T

    /**
     * To properties
     *
     * @return properties
     */
    fun toProperties(): Properties {
        val properties: Properties = object : Properties() {
            override fun keys(): Enumeration<Any> {
                return enumeration<Any>(colorMap.keys.sorted())
            }
        }
        for (id in colorMap.keys) {
            val color: Int? = colorMap[id]
            if (color == null) {
                continue
            }
            val key = "color-$id"
            val value = "${red(color)}, ${green(color)}, ${blue(color)}"
            properties.setProperty(key, value)
        }
        return properties
    }

    /**
     * Load properties into maps
     *
     * @param properties properties to read from
     */
    fun fromProperties(properties: Properties) {
        for (key in properties.stringPropertyNames()) {
            val subKeys: Array<String> = key.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (subKeys.size < 2) {
                continue
            }
            val id = subKeys[1]
            if (subKeys[0] == "color") {
                val values: Array<String?> = properties.getProperty(key).split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val rValue = values[0]!!.toInt()
                val gValue = values[1]!!.toInt()
                val bValue = values[2]!!.toInt()
                val color: Int = rgb(rValue, gValue, bValue)
                colorMap[id] = color
            }
        }
    }

    const val TAG = "Palette"

    const val DEFAULT_FORE_DAY = Color.DKGRAY
    const val DEFAULT_FORE_NIGHT = Color.GRAY
    const val DEFAULT_FORE = DEFAULT_FORE_DAY

    const val DEFAULT_OVERFLOW_COLOR: Int = DEFAULT_FORE
    const val DEFAULT_SPAN_COLOR: Int = DEFAULT_FORE
    const val DEFAULT_EDGE_COLOR: Int = DEFAULT_FORE
    const val DEFAULT_ARROW_TIP_COLOR: Int = DEFAULT_FORE
    const val DEFAULT_ARROW_START_COLOR: Int = DEFAULT_FORE
    const val DEFAULT_LABEL_COLOR: Int = DEFAULT_FORE
    const val DEFAULT_POS_COLOR: Int = DEFAULT_FORE
    const val DEFAULT_ROOT_COLOR: Int = Color.RED

    var edgeColor = DEFAULT_EDGE_COLOR
    var arrowTipColor = DEFAULT_ARROW_TIP_COLOR
    var arrowStartColor = DEFAULT_ARROW_START_COLOR
    var labelColor = DEFAULT_LABEL_COLOR
    var overflowColor: Int = DEFAULT_OVERFLOW_COLOR
    var spanColor: Int = DEFAULT_SPAN_COLOR
    var posColor: Int = DEFAULT_POS_COLOR
    var rootColor: Int = DEFAULT_ROOT_COLOR

    /**
     * Set edge color
     *
     * @param color color
     */
    fun setEdgeColor(color: Int?) {
        edgeColor = color ?: DEFAULT_EDGE_COLOR
        arrowTipColor = color ?: DEFAULT_ARROW_TIP_COLOR
        arrowStartColor = color ?: DEFAULT_ARROW_START_COLOR
    }

    /**
     * Set label color
     *
     * @param color color
     */
    fun setLabelColor(color: Int?) {
        labelColor = color ?: DEFAULT_LABEL_COLOR
    }

    /**
     * Set alpha
     *
     * @param alpha
     * @return color with transparency
     */
    fun Int.setAlpha(alpha: Int): Int {
        return argb(alpha, red(this), green(this), blue(this))
    }
}
