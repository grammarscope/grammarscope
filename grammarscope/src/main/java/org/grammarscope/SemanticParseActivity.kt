package org.grammarscope

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.depparse.common.R
import org.grammarscope.semantics.SemanticParse
import org.grammarscope.semantics.SemanticRelations

class SemanticParseActivity : TextParseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SemanticRelations.read(this)
    }

    /**
     * Run parsing

     * @param source input
     */
    override fun runParse(source: String?) {
        if (source.isNullOrEmpty()) {
            return
        }
        Log.d(TAG, "Parse run on '$source'")
        lifecycleScope.launch {
            SemanticParse(this@SemanticParseActivity).runAndCallback(Dispatchers.IO, source)
        }
    }

    override fun accept(result: CharSequence?) {
        val textView = findViewById<TextView>(R.id.parsed)
        if (textView != null) {
            textView.text = result ?: ""
        }
    }

    companion object {

        private const val TAG = "SemanticParseA"
    }
}
