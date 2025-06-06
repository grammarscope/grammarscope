package org.grammarscope

import android.util.Log
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.depparse.common.Parse
import org.depparse.common.R

class DependencyParseActivity : TextParseActivity() {

    /**
     * Run parsing
     *
     * @param source input
     */
    override fun runParse(source: String?) {
        if (source.isNullOrEmpty()) {
            return
        }
        Log.d(TAG, "Parse run on '$source'")
        lifecycleScope.launch {
            Parse(this@DependencyParseActivity).runAndCallback(Dispatchers.Default, source)
        }
    }

    override fun accept(result: CharSequence?) {
        val textView = findViewById<TextView>(R.id.parsed)
        if (textView != null) {
            textView.text = result ?: ""
        }
    }

    companion object {

        private const val TAG = "DependencyParseA"
    }
}
