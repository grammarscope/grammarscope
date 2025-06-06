package org.depparse.common

import android.widget.TextView

abstract class TextBaseParseActivity : BaseParseActivity<CharSequence?>() {

    override val layout: Int
        get() = R.layout.activity_parse

    override fun pending() {
        val textView = findViewById<TextView>(R.id.parsed)
        textView.setText(R.string.pending)
    }
}
