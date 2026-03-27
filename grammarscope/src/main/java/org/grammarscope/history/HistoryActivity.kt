/*
 * Copyright (c) 2019-2024. Bernard Bou
 */
package org.grammarscope.history

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import org.depparse.BaseActivity
import org.grammarscope.MainActivity
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
                        select(query)
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

    fun select(query: String) {
        val intent = makeInputIntent(query)
        startActivity(intent)
    }

    fun makeInputIntent(query: String): Intent {
        val intent = Intent(this, MainActivity::class.java)
        intent.action = Intent.ACTION_SEARCH
        intent.putExtra(SearchManager.QUERY, query)
        return intent
    }
}
