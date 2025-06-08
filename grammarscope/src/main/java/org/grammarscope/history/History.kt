/*
 * Copyright (c) 2019-2024. Bernard Bou
 */
package org.grammarscope.history

import android.content.Context
import android.content.SearchRecentSuggestionsProvider
import android.database.Cursor
import android.net.Uri
import android.provider.BaseColumns
import android.util.Log
import androidx.loader.content.CursorLoader
import androidx.preference.PreferenceManager
import org.grammarscope.common.R
import androidx.core.net.toUri

/**
 * Search recent suggestion.
 * Access to suggestions provider, after standard SearchRecentSuggestions which has private members and can hardly be extended.
 *
 * @property context context
 * @param mode    item_mode
 *
 * @author Bernard Bou
 */
class History(private val context: Context, mode: Int) {

    // a superset of all possible column names (need not all be in table)
    object SuggestionColumns : BaseColumns {

        const val DISPLAY1: String = "display1"
        const val DATE: String = "date"
    }

    // client-provided configuration values
    private val suggestionsUri: Uri

    // Q U E R Y

    /**
     * Get cursor
     *
     * @return cursor
     */
    fun cursor(): Cursor? {
        val contentResolver = context.contentResolver
        try {
            val projection = arrayOf( /*"DISTINCT " +*/SuggestionColumns.DISPLAY1, "_id")
            val sortOrder = sortOrder
            return contentResolver.query(suggestionsUri, projection, null, null, sortOrder)
        } catch (e: RuntimeException) {
            Log.e(TAG, "While getting cursor", e)
        }
        return null
    }

    /**
     * Get cursor loader
     *
     * @return cursor loader
     */
    fun cursorLoader(): CursorLoader {
        val projection = arrayOf( /* "DISTINCT " +*/SuggestionColumns.DISPLAY1, "_id")
        val sortOrder = sortOrder
        return CursorLoader(context, suggestionsUri, projection, null, null, sortOrder)
    }

    /**
     * Constructor
     */
    init {
        require((mode and SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES) != 0)

        // derived values
        suggestionsUri = "content://$authority/suggestions".toUri()
    }

    private val sortOrder: String
        get() {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val byDate = prefs.getBoolean(PREF_KEY_HISTORY_SORT_BY_DATE, true)
            return if (byDate) SuggestionColumns.DATE + " DESC" else SuggestionColumns.DISPLAY1 + " ASC"
        }

    // D E L E T E

    /**
     * Delete item by _id
     */
    fun delete(id: String) {
        val selection = "_id = ?"
        val selectArgs = arrayOf(id)
        val cr = context.contentResolver
        try {
            cr.delete(suggestionsUri, selection, selectArgs)
        } catch (e: RuntimeException) {
            Log.e(TAG, "While deleting suggestion", e)
        }
    }

    /**
     * Authority
     */
    private val authority: String
        get() = getAuthority(context)

    companion object {

        private const val TAG = "History"

        private const val PREF_KEY_HISTORY_SORT_BY_DATE = "pref_history_sort_by_date"

        @JvmStatic
        fun getAuthority(context: Context): String {
            return context.getString(R.string.history_provider_authority)
        }

        /**
         * Record query
         *
         * @param context context
         * @param query   query
         */
        @JvmStatic
        fun recordQuery(context: Context, query: String?) {
            val suggestions = android.provider.SearchRecentSuggestions(context, getAuthority(context), SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES)
            suggestions.saveRecentQuery(query, null)
        }

    }
}
