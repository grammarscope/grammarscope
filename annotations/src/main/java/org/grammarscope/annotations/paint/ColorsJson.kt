/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope.annotations.paint

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.core.content.edit
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.grammarscope.annotations.AnnotationsSettings.Companion.PREF_RELATION_COLORS
import org.grammarscope.annotations.R


object ColorsJson {

    /**
     * Get color map from resources
     *
     * @param resources resources to read from.
     * @return color map from resources
     */
    fun getColorMapFromResources(resources: Resources): Map<String, Int> {
        val paletteKeys = resources.getStringArray(R.array.relations_keys)
        val paletteValues = resources.getIntArray(R.array.palette_relations_values)
        require(paletteValues.size == paletteKeys.size)
        return paletteKeys.withIndex().associate { (i, k) -> k to paletteValues[i] }
    }

    /**
     * Stores a map of MyObject IDs to colors.
     * @param prefs shared preferences to read from.
     * @param colorMap The map of MyObject IDs to colors to store.
     */
    fun saveColorMap(prefs: SharedPreferences, colorMap: Map<String, Int>) {
        val json = colorMap.colorMapToString()
        prefs.edit {
            putString(PREF_RELATION_COLORS, json)
        }
    }

    /**
     * Retrieves the map of MyObject IDs to colors from shared preferences.
     *
     * @param prefs shared preferences to read from.
     * @return The map of MyObject IDs to colors, or an empty map if no preference is found.
     */
    fun getColorMap(prefs: SharedPreferences?): Map<String, Int>? {
        val json = prefs?.getString(PREF_RELATION_COLORS, null) ?: return null
        return json.getColorMap()
    }

    /**
     * Color map to string.
     */
    fun Map<String, Int>.colorMapToString(): String {
        return Json.encodeToString(MapSerializer(String.serializer(), Int.serializer()), this)
    }

    /**
     * String to color map.
     * @return The map of strings to colors.
     */
    fun String.getColorMap(): Map<String, Int> {
        return Json.decodeFromString(MapSerializer(String.serializer(), Int.serializer()), this)
    }
}
