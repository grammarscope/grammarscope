package org.mysyntaxnet

import android.util.Log
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.depparse.common.Parse
import org.depparse.common.TextBaseParseActivity
import org.depparse.common.R as CommonR

class ParseActivity : TextBaseParseActivity() {

    /**
     * Run parsing
     */
    override fun runParse(source: String?) {
        if (source.isNullOrEmpty()) {
            return
        }
        Log.d(TAG, "Parse run on '$source'")
        lifecycleScope.launch {
            Parse(this@ParseActivity).runAndCallback(Dispatchers.IO, source)
        }
    }

    override fun accept(result: CharSequence?) {
        val textView = findViewById<TextView>(CommonR.id.parsed)
        if (textView != null) {
            textView.text = result ?: ""
        }
    }

    companion object {

        private const val TAG = "ParseA"
    }
}
