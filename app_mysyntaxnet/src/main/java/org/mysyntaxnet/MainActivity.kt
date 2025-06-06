package org.mysyntaxnet

import android.annotation.SuppressLint
import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.bbou.deploy.coroutines.Deploy.InputStreamGetter
import com.bbou.deploy.coroutines.Deploy.deploy
import com.bbou.deploy.coroutines.Deploy.redeploy
import com.bbou.donate.DonateActivity
import com.bbou.others.OthersActivity
import com.bbou.others.OthersActivity.Companion.install
import com.bbou.rate.AppRate.Companion.invoke
import com.bbou.rate.AppRate.Companion.rate
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.depparse.IProvider
import org.depparse.Sentence
import org.depparse.Storage.getAppStorage
import org.depparse.Unique
import org.depparse.common.AboutActivity
import org.depparse.common.BaseParseActivity.Companion.tryStartParse
import org.depparse.common.ModelInfo.Companion.read
import org.depparse.common.UniqueProvider
import org.depparse.common.showSwipableSnackbar
import org.syntaxnet1.Syntaxnet1Engine
import java.io.File

class MainActivity : AppCompatActivity() {

    // input getter
    private var getter: InputStreamGetter? = null

    // L I F E C Y C L E

    @SuppressLint("CommitTransaction") // BUG
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // input getter
        getter = InputStreamGetter { path: String? ->
            val assetManager = assets
            assetManager.open(path!!)
        }

        // init
        initialize()

        // layout
        setContentView(R.layout.activity_main)

        // fab listener
        val fab = findViewById<FloatingActionButton>(R.id.on_dependencies)
        fab.setOnClickListener { onClickDependencies() }

        // warning
        val parentLayout = findViewById<View>(R.id.coord_layout)
        showSwipableSnackbar(this, parentLayout, R.string.obsolete_app, android.R.color.holo_red_light, android.R.color.white, R.string.obsolete_get_grammarscope) {
            install(this.getString(org.depparse.common.R.string.grammarscope_uri), this)
        }

        // toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // set up the action bar
        @SuppressLint("InflateParams") val actionBarView = layoutInflater.inflate(R.layout.actionbar_custom, null)
        val actionBar = supportActionBar!!
        actionBar.setTitle(R.string.actionbar_title)
        actionBar.setSubtitle(R.string.actionbar_subtitle)
        actionBar.elevation = 0f
        actionBar.customView = actionBarView
        actionBar.displayOptions = ActionBar.DISPLAY_SHOW_TITLE or ActionBar.DISPLAY_SHOW_CUSTOM or ActionBar.DISPLAY_USE_LOGO

        // fragment
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.container, makeFragment())
                .commit()
        }

        // rate
        invoke(this)
    }

    @SuppressLint("MissingSuperCall")
    /* BUG IN KOTLIN INSPECTION */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent) // handling intent in onResume() will re-launch activity
    }

    override fun onStart() {
        super.onStart()

        // handle intent
        handleIntent(intent) // handling intent in onResume() will re-launch activity
    }

    // F R A G M E N T

    private fun makeFragment(): Fragment {
        return MainFragment()
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class MainFragment
    /**
     * Constructor
     */
        : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.fragment_main, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val activity = requireActivity()

            // edit text
            val edit = activity.findViewById<EditText>(R.id.query)
            edit.imeOptions = EditorInfo.IME_ACTION_DONE
            edit.setRawInputType(InputType.TYPE_CLASS_TEXT)
            edit.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val query = edit.text
                    tryStartParse(activity, ParseActivity::class.java, query.toString())
                    return@setOnEditorActionListener true
                }
                false
            }

            // main
            val main = activity.findViewById<View>(android.R.id.content)
            main.setOnTouchListener { v: View, event: MotionEvent ->
                if (event.action == MotionEvent.ACTION_UP) {
                    v.performClick()
                }
                //searchView.clearFocus();
                edit.clearFocus()
                false
            }
        }
    }

    // I N I T I A L I Z A T I O N

    /**
     * Initialize
     */
    private fun initialize() {
        // app storage
        val dir = getAppStorage(this)

        // deploy
        try {
            deploy(dir, "English", getter!!)
        } catch (e: Exception) {
            Toast.makeText(this, "Redeploying: " + e.message, Toast.LENGTH_LONG).show()
            redeploy(dir, "English", getter!!)
        }

        // new provider
        newProvider(dir)
    }

    private fun newProvider(dir: File) {
        // provider factory
        val factory = Unique.Factory<IProvider<Array<Sentence>>> {
            val engine = Syntaxnet1Engine()
            engine.load(dir.absolutePath)
            engine
        }
        // provider
        val made = UniqueProvider.SINGLETON.make(factory)
        Log.d(TAG, "Provider made $made")
    }

    // M E N U

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.english, R.id.french -> {
                val lang = if (item.itemId == R.id.french) "French" else "English"
                val dir = getAppStorage(this)
                // consume provider
                UniqueProvider.SINGLETON.kill()
                // deploy
                redeploy(dir, lang, getter!!)
                // new provider
                newProvider(dir)
                true
            }

            R.id.sample1 -> {
                setQuery(0)
                true
            }

            R.id.sample2 -> {
                setQuery(1)
                true
            }

            R.id.sample3 -> {
                setQuery(2)
                true
            }

            R.id.sample4 -> {
                setQuery(3)
                true
            }

            R.id.sample5 -> {
                setQuery(4)
                true
            }

            R.id.about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }

            R.id.others -> {
                startActivity(Intent(this, OthersActivity::class.java))
                true
            }

            R.id.donate -> {
                startActivity(Intent(this, DonateActivity::class.java))
                true
            }

            R.id.rate -> {
                rate(this)
                true
            }

            else -> false
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        var isEnglish = false
        var isFrench = false
        val info = read(this)
        if (info != null) {
            isEnglish = info.isEnglish
            isFrench = info.isFrench
        }
        menu.findItem(R.id.english).isChecked = isEnglish
        menu.findItem(R.id.french).isChecked = isFrench
        return true
    }

    private fun onClickDependencies() {
        val edit = findViewById<EditText>(R.id.query)
        val query = edit.text
        tryStartParse(this, ParseActivity::class.java, query.toString())
    }

    private fun setQuery(index: Int) {
        var isEnglish = false
        var isFrench = false
        val info = read(this)
        if (info != null) {
            isEnglish = info.isEnglish
            isFrench = info.isFrench
        }
        val id: Int = if (isEnglish) {
            R.array.samples_english
        } else if (isFrench) {
            R.array.samples_french
        } else {
            return
        }

        // english + french
        val samples = getResources().getStringArray(id)
        setQuery(samples[index])
    }

    private fun setQuery(text: CharSequence?) {
        val edit = findViewById<EditText>(R.id.query)
        edit.setText(text)
        edit.setSelection(edit.text.length)
        val focus = currentFocus
        if (focus != null) {
            val inputManager = (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            inputManager.hideSoftInputFromWindow(this.currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            edit.clearFocus()
        }
    }

    // R E Q U E S T S

    /**
     * Handle search intent
     *
     * @param intent intent
     * @return true if handled
     */
    private fun handleIntent(intent: Intent): Boolean {
        val action = intent.action
        Log.d(TAG, intent.toString())
        if (Intent.ACTION_SEARCH == action) {
            val text = intent.getStringExtra(SearchManager.QUERY)
            setQuery(text)
            return true
        }
        return false
    }

    companion object {

        private const val TAG = "MainActivity"

        // S T A R T A C T I V I T Y

        fun tryStartWithText(activity: Activity, sharedText: String) {
            val intent = Intent(activity, MainActivity::class.java)
            intent.action = Intent.ACTION_SEARCH
            intent.putExtra(SearchManager.QUERY, sharedText)
            activity.startActivity(intent)
        }
    }
}
