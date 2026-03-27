package org.grammarscope

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import com.bbou.textgetter.GetTextActivity
import org.depparse.common.ModelInfo
import org.grammarscope.common.R
import org.grammarscope.history.HistoryBottomSheet
import java.util.Locale

class MainActivity : BaseMainActivity() {
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // get button
        findViewById<ImageButton>(R.id.text_get).setOnClickListener { getText() }

        // get button
        findViewById<ImageButton>(R.id.text_history).setOnClickListener { getTextFromHistory() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.text_get -> {
                getText()
                true
            }

            R.id.text_history -> {
                getTextFromHistory()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getText() {
        val intent = Intent(this, GetTextActivity::class.java)
        val info = ModelInfo.read(this)
        if (info != null) {
            intent.putExtra(GetTextActivity.ARG_LANG, info.lang.take(2).lowercase(Locale.getDefault()))
        }
        startActivity(intent)
    }

    private fun getTextFromHistory() {
        HistoryBottomSheet { sentence: String ->
            select(sentence)
        }.show(supportFragmentManager, "HistoryBottomSheet")
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
