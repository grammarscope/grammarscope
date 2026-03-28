/*
 * Copyright (c) 2019-2024. Bernard Bou
 */
package org.grammarscope.history

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import org.depparse.BaseActivity
import org.grammarscope.BaseMainActivity.Companion.startMainWithQuery
import org.grammarscope.common.R

/**
 * History activity
 *
 * @author Bernard Bou
 */
class HistoryActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // layout
        setContentView(R.layout.activity_history)

        // fragment
        if (savedInstanceState == null) {
            val fragment = HistoryFragment()
                .apply {
                    itemLayoutId = R.layout.item_history
                    selectListener = { query ->
                        startMainWithQuery(query, requireActivity())
                    }
                }
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_history, fragment)
                .commit()
        }

        // toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // set up the action bar
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.displayOptions = ActionBar.DISPLAY_USE_LOGO or ActionBar.DISPLAY_SHOW_TITLE or ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_HOME_AS_UP
        }
    }
}
