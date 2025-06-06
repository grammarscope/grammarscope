package org.grammarscope

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.grammarscope.BaseMainActivity.Companion.tryStartWithText

class ShareActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
    }

    @SuppressLint("MissingSuperCall")
    /* BUG IN KOTLIN INSPECTION */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    /**
     * Handle search intent
     *
     * @param intent intent
     * @return true if handled
     */
    private fun handleIntent(intent: Intent): Boolean {
        val action = intent.action
        if (Intent.ACTION_SEND == action) {
            val type = intent.type
            if ("text/plain" == type) {
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (!sharedText.isNullOrEmpty()) {
                    tryStartWithText(this, sharedText)
                    return true
                }
            }
        }
        return false
    }
}
