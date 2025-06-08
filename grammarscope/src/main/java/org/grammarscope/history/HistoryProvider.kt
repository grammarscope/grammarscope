/*
 * Copyright (c) 2019-2024. Bernard Bou
 */
package org.grammarscope.history

import android.content.Context
import android.content.SearchRecentSuggestionsProvider
import org.grammarscope.common.R

/**
 * Recent suggestion provider
 *
 * @author Bernard Bou
 */
class HistoryProvider : SearchRecentSuggestionsProvider() {

    override fun onCreate(): Boolean {
        val context = context!!
        val authority = getAuthority(context)
        setupSuggestions(authority, MODE)
        return super.onCreate()
    }

    companion object {

        /**
         * Mode
         */
        internal const val MODE = DATABASE_MODE_QUERIES

         /**
         * Authority
         */
       @JvmStatic
        fun getAuthority(context: Context): String {
            return context.getString(R.string.history_provider_authority)
        }
    }
}
