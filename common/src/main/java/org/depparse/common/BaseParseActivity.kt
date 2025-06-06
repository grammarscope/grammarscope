package org.depparse.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bbou.deploy.coroutines.Deploy.fastCheck
import org.depparse.Storage
import java.text.Normalizer
import java.util.function.Consumer

abstract class BaseParseActivity<T> : AppCompatActivity(), Consumer<T> {

    protected abstract val layout: Int
    protected abstract fun pending()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)

        // init
        fastCheck(Storage.getAppStorage(this))

        // view
        setContentView(layout)

        // toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // action bar
        @SuppressLint("InflateParams") val actionBarView = layoutInflater.inflate(R.layout.actionbar_custom, null)
        val actionBar = supportActionBar!!
        actionBar.setTitle(R.string.actionbar_title)
        actionBar.setSubtitle(R.string.actionbar_subtitle)
        actionBar.elevation = 0f
        actionBar.customView = actionBarView
        actionBar.displayOptions = ActionBar.DISPLAY_SHOW_TITLE or ActionBar.DISPLAY_SHOW_CUSTOM or ActionBar.DISPLAY_USE_LOGO
    }

    /**
     * An activity will always be paused before receiving a new intent,
     * so you can count on onResume() being called after this method
     */
    @SuppressLint("MissingSuperCall")
    /* BUG IN KOTLIN INSPECTION */
    override fun onNewIntent(intent: Intent) {
        Log.d(TAG, "onNewIntent action=" + intent.action)
        super.onNewIntent(intent)

        // getIntent() still returns the original Intent, use setIntent(Intent) to update it to this new Intent.
        setIntent(intent)
    }

    override fun onResume() {
        Log.d(TAG, "Resume")

        // super
        super.onResume()

        // pending UI
        pending()
        if (Intent.ACTION_VIEW == intent.action) {
            // retrieve arguments
            val source = unmarshalArgs(intent)

            // update UI
            update(source)

            // sanity check
            if (source == null) {
                Toast.makeText(this, R.string.error_null_data, Toast.LENGTH_LONG).show()
                finish()
                return
            }

            // accents
            val stripAccents = Settings.isStripAccentsEnabled(this)
            val source2 = if (stripAccents) source.stripAccents() else source

            // run parsing
            Log.d(TAG, "Parse")
            runParse(source2)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Colors.setColorsFromResources(this)
    }

    // P A R A M E T E R S - A R G U M E N T S

    /**
     * Get model and parameters from intent (onResume)
     *
     * @param intent intent
     * @return source text
     */
    private fun unmarshalArgs(intent: Intent): String? {
        val params = intent.extras!!
        return params.getString(ARG_SOURCE)
    }

    /**
     * Strip accents
     *
     * @return string with accents removed
     */
    private fun String.stripAccents(): String {
        // 1. Normalize to NFD (decomposes characters into base characters and combining diacritical marks)
        val normalizedText = Normalizer.normalize(this, Normalizer.Form.NFD)

        // 2. Remove combining diacritical marks (Unicode category Mn)
        // The regex "\\p{Mn}" matches any character with the Unicode property "Mark, Nonspacing".
        return normalizedText.replace("\\p{Mn}".toRegex(), "")
    }

    // P A R S E

    /**
     * Run parsing
     */
    protected abstract fun runParse(source: String?)

    // U I

    private fun update(source: String?) {
        // action bar  subtitle
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.subtitle = source
        }
    }

    // C O N S U M E R

    abstract override fun accept(result: T)

    // M E N U

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.parse, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val itemId = item.itemId
        if (itemId == R.id.reference_uds) {
            WebActivity.tryStart(this, "file:///android_asset/reference/uds.html", usesJavaScript = true, local = true)
            return true
        }
        return false
    }

    companion object {

        private const val TAG = "ParseActivity"
        const val ARG_SOURCE = "org.depparse.SOURCE"

        // L A U N C H   I N T E N T

        /**
         * Try to start parse activity from source
         *
         * @param context context
         * @param query   source
         */
        private fun makeParseIntent(context: Context, clazz: Class<out BaseParseActivity<*>>, query: String): Intent? {
            if (query.isEmpty()) {
                Toast.makeText(context, R.string.error_null_source, Toast.LENGTH_SHORT).show()
                return null
            }
            val intent = Intent(context, clazz)
            intent.action = Intent.ACTION_VIEW
            intent.putExtra(ARG_SOURCE, query)
            Log.d(TAG, "Start parse from source: '$query'")
            return intent
        }

        /**
         * Try to start parse activity from source
         *
         * @param query source
         */
        fun tryStartParse(context: Context, clazz: Class<out BaseParseActivity<*>>, query: String) {
            val intent = makeParseIntent(context, clazz, query)
            if (intent != null) {
                context.startActivity(intent)
            }
        }
    }
}
