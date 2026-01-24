package org.grammarscope

import android.annotation.SuppressLint
import android.app.Activity
import android.app.SearchManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.bbou.coroutines.BaseTask
import com.bbou.deploy.coroutines.Deploy
import com.bbou.deploy.coroutines.Deploy.check
import com.bbou.deploy.coroutines.Deploy.emptyDirectory
import com.bbou.deploy.coroutines.Deploy.redeploy
import com.bbou.donate.DonateActivity
import com.bbou.download.Keys.BROADCAST_ACTION
import com.bbou.download.Keys.BROADCAST_KILL_REQUEST_VALUE
import com.bbou.download.Keys.BROADCAST_NEW_REQUEST_VALUE
import com.bbou.download.Keys.BROADCAST_REQUEST_KEY
import com.bbou.download.coroutines.UpdateStarter
import com.bbou.download.coroutines.utils.ContentDownloader
import com.bbou.download.preference.Settings.unrecordDatapack
import com.bbou.download.preference.Settings.unrecordDatapackSource
import com.bbou.others.OthersActivity
import com.bbou.rate.AppRate
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.depparse.Broadcast
import org.depparse.IProvider
import org.depparse.Storage
import org.depparse.common.AboutActivity
import org.depparse.common.BaseParseActivity.Companion.tryStartParse
import org.depparse.common.Colors
import org.depparse.common.LanguageFlag
import org.depparse.common.ModelInfo
import org.depparse.common.Samples
import org.depparse.common.Settings
import org.depparse.common.Settings.isStripAccentsEnabled
import org.depparse.common.UniqueProvider
import org.depparse.common.WebActivity
import org.depparse.common.showSnackbar
import org.grammarscope.annotations.AnnotatedTextActivity
import org.grammarscope.common.R
import org.grammarscope.history.History.Companion.recordQuery
import org.grammarscope.history.HistoryActivity
import java.util.Locale
import java.util.function.Consumer
import org.depparse.common.R as CommonR
import com.google.android.material.R as MaterialR

abstract class BaseMainActivity : AppCompatActivity() {

    private var autoStart = true
    private lateinit var fabDependencies: FloatingActionButton
    private lateinit var fabSemantics: FloatingActionButton
    private lateinit var loadedIndicator: ImageButton
    private lateinit var langIndicator: TextView
    private var controlMenuItem: MenuItem? = null  // not available before menu is inflated / onCreateOptionsMenu

    /**
     * Broadcast receiver
     */
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            // Log.d(TAG, "Received broadcast message " + action);
            if (Broadcast.BROADCAST_LISTEN == action) {
                val type = Broadcast.EventType.valueOf(intent.getStringExtra(Broadcast.BROADCAST_LISTEN_EVENT)!!)
                onEvent(type)
            }
        }
    }

    // L I F E C Y C L E

    @SuppressLint("CommitTransaction") // BUG
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate()")

        // edge to edge
        enableEdgeToEdge()

        // create an intent filter to listen to the broadcast sent forActivity the action "ENGINE" and map it to the receiver
        ContextCompat.registerReceiver(this, receiver, IntentFilter(Broadcast.BROADCAST_LISTEN), ContextCompat.RECEIVER_NOT_EXPORTED)

        // init model
        initData()

        // layout
        setContentView(R.layout.activity_main)

        // toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        langIndicator = toolbar.findViewById(R.id.lang_indicator)
        setSupportActionBar(toolbar)

        // set up the action bar
        val actionBar = supportActionBar!!
        actionBar.setTitle(R.string.actionbar_title)
        actionBar.setSubtitle(CommonR.string.actionbar_subtitle)
        actionBar.elevation = 0f
        actionBar.displayOptions = ActionBar.DISPLAY_SHOW_TITLE or ActionBar.DISPLAY_USE_LOGO

        // set up the action bar
        setLanguage(null)

        // fragment
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.container, makeFragment())
                .commit()
        } else {
            autoStart = savedInstanceState.getInt("autostart") != 0
        }

        // set up UI
        setupUI()

        // handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.coord_layout)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        // rate
        AppRate.invoke(this)
    }

    public override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy()")
        unregisterReceiver(receiver)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "onSaveInstance()")
        outState.putInt("autostart", if (autoStart) 1 else 0)
    }

    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent) {
        Log.d(TAG, "onNewIntent()")
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent) // handling intent in onResume() will re-launch activity
    }

    public override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        Log.d(TAG, "onPostCreate()")

        // provider
        if (autoStart) {
            ProviderManager.requestNew(applicationContext)
        }
    }

    private fun setupUI() {

        // UI items that are not in the fragment
        fabDependencies = findViewById(R.id.fab_dependencies)
        fabSemantics = findViewById(R.id.fab_semantics)
        loadedIndicator = findViewById(R.id.loaded_indicator)
        // boundIndicator = findViewById(R.id.bound_indicator)

        // listeners
        fabDependencies.setOnClickListener { onClickFABDependencies(false) }
        fabDependencies.setOnLongClickListener { onClickFABDependencies(true); true }
        fabSemantics.setOnClickListener { onClickFABSemantics(false) }
        fabSemantics.setOnLongClickListener { onClickFABSemantics(true); true }
        loadedIndicator.setOnClickListener { info(status()) }
        // boundIndicator.setOnClickListener { info(status()) }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart()")

        // handle intent
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume()")

        // UI
        updateLanguage()
        updateStatus()
    }

    // F R A G M E N T

    private fun makeFragment(): Fragment {
        return MainFragment()
    }

    fun textChanged(s: String) {
        intent.putExtra(SearchManager.QUERY, s)
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class MainFragment : Fragment() {

        private lateinit var queryEdit: EditText

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.fragment_main, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            setupUI(requireActivity())
            if (savedInstanceState != null) {
                queryEdit.setText(savedInstanceState.getString(QUERY_STATE))
            }
        }

        private fun setupUI(activity: Activity) {
            queryEdit = requireActivity().findViewById(R.id.query)
            queryEdit.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    val a = getActivity()
                    if (a is BaseMainActivity) {
                        a.textChanged(s.toString())
                    }
                }
            })

            // clear button
            val btnClear = requireActivity().findViewById<ImageButton>(R.id.clear)
            btnClear.setOnClickListener {
                // data
                queryEdit.setText("")

                // focus
                val focus = activity.currentFocus
                if (focus != null) {
                    val inputManager = (activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                    inputManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                    queryEdit.clearFocus()
                }
            }

            // example button
            val btnExamples = requireActivity().findViewById<ImageButton>(R.id.examples)
            btnExamples.setOnClickListener {
                val sentences = Samples.read(activity)
                if (!sentences.isNullOrEmpty()) {
                    val builder = AlertDialog.Builder(requireContext())
                    builder
                        .setTitle(R.string.title_sentences)
                        .setItems(sentences) { _, which ->
                            val selected = sentences[which]
                            queryEdit.setText(selected)
                        }
                        .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                            dialog.dismiss()
                        }
                    val dialog = builder.create()
                    dialog.show()
                }

                // focus
                val focus = activity.currentFocus
                if (focus != null) {
                    val inputManager = (activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                    inputManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                    queryEdit.clearFocus()
                }
            }
        }

        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            val view = view
            if (view != null) {
                val edit = view.findViewById<EditText>(R.id.query)
                outState.putString(QUERY_STATE, edit.text.toString())
            }
        }

        companion object {

            private const val QUERY_STATE = "main_query_state"
        }
    }

    // I N I T I A L I Z A T I O N

    /**
     * Initialize
     */
    private fun initData() {
        // app storage
        val dir = Storage.getAppStorage(this)

        // deploy
        try {
            check(dir)
        } catch (e: Exception) {
            Log.e(TAG, "Deploy failed", e)
            emptyDirectory(dir)
            warn(getString(R.string.error_deploy) + '\n' + e.message + '\n' + getString(R.string.status_clear_data))
            finish()
        }
    }

    // M E N U

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.d(TAG, "onCreateOptionsMenu()")

        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        controlMenuItem = menu.findItem(R.id.bind)
        updateControl(UniqueProvider.SINGLETON.get() != null)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {

            R.id.bind -> {
                if (UniqueProvider.SINGLETON.get() == null) {
                    autoStart = true
                    ProviderManager.requestNew(applicationContext)
                } else {
                    ProviderManager.requestKill(applicationContext)
                }
                return true
            }

            R.id.status -> {
                info(status())
                return true
            }

            R.id.engine_version -> {
                info(version())
                return true
            }

            R.id.model_status -> {
                Status.modelStatus(this)
                return true
            }

            R.id.settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.builtin_english, R.id.builtin_french -> {
                val deployer = object : BaseTask<Void?, Unit>() {

                    override suspend fun doJob(params: Void?) {
                        // lang
                        val lang = if (item.itemId == R.id.builtin_french) "French" else "English"

                        // dir
                        val dir = Storage.getAppStorage(this@BaseMainActivity)

                        // consume-new bracketed redeploy
                        ProviderManager.requestKill(applicationContext)
                        redeploy(dir, lang) { path: String ->
                            val assetManager = assets
                            assetManager.open(path)
                        }
                        ProviderManager.requestNew(applicationContext)

                        // model info
                        unrecordDatapack(this@BaseMainActivity)
                        unrecordDatapackSource(this@BaseMainActivity)
                    }
                }
                lifecycleScope.launch {
                    deployer.run(Dispatchers.Default, null)
                    updateLanguage()
                }
                return true
            }

            R.id.history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
            }

            R.id.download -> {
                val downloader = object : BaseTask<Void?, Unit>() {

                    override suspend fun doJob(params: Void?) {

                        // consume-new bracketed redeploy
                        ProviderManager.requestKill(applicationContext)

                        // model info
                        unrecordDatapack(this@BaseMainActivity)
                        unrecordDatapackSource(this@BaseMainActivity)
                    }
                }
                lifecycleScope.launch {
                    downloader.run(Dispatchers.Default, null)
                    updateLanguage()
                    ContentDownloader.showContents(this@BaseMainActivity) { context: Context, name: String ->
                        val downloadIntent = DownloadIntentFactory.makeIntent(context)
                        val target = if (name.endsWith(Deploy.ZIP_EXTENSION)) name else name + Deploy.ZIP_EXTENSION
                        ContentDownloader.addTargetToIntent(context, downloadIntent, target)
                        downloadIntent.putExtra(BROADCAST_ACTION, Broadcast.BROADCAST_ACTION)
                        downloadIntent.putExtra(BROADCAST_REQUEST_KEY, Broadcast.BROADCAST_ACTION_REQUEST)
                        downloadIntent.putExtra(BROADCAST_KILL_REQUEST_VALUE, Broadcast.RequestType.KILL.toString())
                        downloadIntent.putExtra(BROADCAST_NEW_REQUEST_VALUE, Broadcast.RequestType.NEW.toString())
                        context.startActivity(downloadIntent)
                    }
                }
                return true
            }

            R.id.update -> {
                try {
                    val downloadIntent = DownloadIntentFactory.makeIntentUpdate(this)
                    downloadIntent.putExtra(BROADCAST_ACTION, Broadcast.BROADCAST_ACTION)
                    downloadIntent.putExtra(BROADCAST_REQUEST_KEY, Broadcast.BROADCAST_ACTION_REQUEST)
                    downloadIntent.putExtra(BROADCAST_KILL_REQUEST_VALUE, Broadcast.RequestType.KILL.toString())
                    downloadIntent.putExtra(BROADCAST_NEW_REQUEST_VALUE, Broadcast.RequestType.NEW.toString())
                    UpdateStarter.start(this, downloadIntent)
                } catch (e: Exception) {
                    warn(e)
                }
                return true
            }

            R.id.strip_accents -> {
                val prefs = PreferenceManager.getDefaultSharedPreferences(this)
                val stripAccents = prefs.getBoolean(Settings.PREF_STRIP_ACCENTS, false)
                val editor = prefs.edit()
                editor.putBoolean(Settings.PREF_STRIP_ACCENTS, !stripAccents) // toggle
                    .apply()
                return true
            }

            R.id.graph -> {
                val prefs = PreferenceManager.getDefaultSharedPreferences(this)
                val asGraph = prefs.getBoolean(GeneralSettings.PREF_AS_GRAPH, false)
                val editor = prefs.edit()
                editor.putBoolean(GeneralSettings.PREF_AS_GRAPH, !asGraph) // toggle
                    .apply()
                return true
            }

            R.id.graphs -> {
                val prefs = PreferenceManager.getDefaultSharedPreferences(this)
                val asGraphs = prefs.getBoolean(GeneralSettings.PREF_AS_GRAPHS, true)
                val editor = prefs.edit()
                editor.putBoolean(GeneralSettings.PREF_AS_GRAPHS, !asGraphs) // toggle
                    .apply()
                return true
            }

            R.id.sentence_detect -> {
                sentenceDetect()
                return true
            }

            R.id.labels -> {
                labels()
                return true
            }

            R.id.about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                return true
            }

            R.id.help -> {
                WebActivity.tryStart(this, "file:///android_asset/help/en/index.html", usesJavaScript = true, local = true)
                return true
            }

            R.id.reference -> {
                WebActivity.tryStart(this, "file:///android_asset/reference/uds.html", usesJavaScript = true, local = true)
                return true
            }

            R.id.others -> {
                startActivity(Intent(this, OthersActivity::class.java))
                return true
            }

            R.id.donate -> {
                startActivity(Intent(this, DonateActivity::class.java))
                return true
            }

            R.id.rate -> {
                AppRate.rate(this)
                return true
            }

            R.id.action_theme_system -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                return true
            }

            R.id.action_theme_night -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                return true
            }

            R.id.action_theme_day -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                return true
            }

            R.id.sentences, R.id.model, R.id.support -> {
                return false // submenu
            }

            else -> if (item.itemId >= R.id.sentences + SENTENCE_ID_OFFSET) {
                val sentIdx = item.itemId - R.id.sentences - SENTENCE_ID_OFFSET
                val sentences = Samples.read(this)
                if (!sentences.isNullOrEmpty() && sentIdx < sentences.size) {
                    query = sentences[sentIdx]
                    return true
                }
            }
        }
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)

        // check asGraph
        val asGraph = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(GeneralSettings.PREF_AS_GRAPH, false)
        menu.findItem(R.id.graph).isChecked = asGraph
        val asGraphs = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(GeneralSettings.PREF_AS_GRAPHS, true)
        menu.findItem(R.id.graphs).isChecked = asGraphs
        val stripAccents = isStripAccentsEnabled(this)
        menu.findItem(R.id.strip_accents).isChecked = stripAccents

        // samples
        val sentences = Samples.read(this)
        if (!sentences.isNullOrEmpty()) {
            // get submenu anchor item
            val sentenceMenuItem = menu.findItem(R.id.sentences)
            // get placeholder submenu
            val subMenu: Menu = sentenceMenuItem.subMenu!!
            subMenu.clear()
            // feed items
            for ((idx, sentence) in sentences.withIndex()) {
                val itemId = R.id.sentences + SENTENCE_ID_OFFSET + idx
                subMenu.add(0, itemId, idx, sentence)
                val item = subMenu.findItem(itemId)
                item.titleCondensed = Samples.ellipsize(sentence, 10)
            }
        }
        return true
    }

    private fun onClickFABDependencies(longClick: Boolean) {
        queryEdit?.let {
            val query: CharSequence = it.text
            recordQuery(this, query.toString())
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
            val sentenceBoundaryDetection = sharedPrefs.getBoolean(GeneralSettings.PREF_SENTENCE_BOUNDARY_DETECTION, true)
            if (sentenceBoundaryDetection) {
                sentenceDetectThenDependencies(query, longClick)
            } else {
                dependencies(query, longClick)
            }
        }
    }

    private fun onClickFABSemantics(longClick: Boolean) {
        queryEdit?.let {
            val query: CharSequence = it.text
            recordQuery(this, query.toString())
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
            val sentenceBoundaryDetection = sharedPrefs.getBoolean(GeneralSettings.PREF_SENTENCE_BOUNDARY_DETECTION, true)
            if (sentenceBoundaryDetection) {
                sentenceDetectThenSemantics(query, longClick)
            } else {
                semantics(query, longClick)
            }
        }
    }

    private fun dependencies(query: CharSequence, longClick: Boolean) {
        // state check
        if (isReady()) {
            var queryStr = query.toString()
            // sanity check
            queryStr = queryStr.trim { it <= ' ' }
            queryStr = queryStr.replace("^\\s+".toRegex(), "")
            queryStr = queryStr.replace("\\s+$".toRegex(), "")
            if (queryStr.isNotEmpty()) {
                val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
                var asGraph = sharedPrefs.getBoolean(GeneralSettings.PREF_AS_GRAPH, false)
                if (longClick) {
                    asGraph = !asGraph
                }
                val asGraphs = sharedPrefs.getBoolean(GeneralSettings.PREF_AS_GRAPHS, true)
                val asAnnotation = sharedPrefs.getBoolean(GeneralSettings.PREF_AS_ANNOTATION, true)
                tryStartParse(
                    this,
                    if (asGraph)
                        (if (asGraphs) DependencyGraphsParseActivity::class.java else DependencyGraphParseActivity::class.java)
                    else
                        (if (asAnnotation) AnnotatedTextActivity::class.java else DependencyParseActivity::class.java),
                    queryStr
                )
            }
        } else
            warn(getString(R.string.provider_not_ready))
    }

    private fun semantics(query: CharSequence, longClick: Boolean) {
        // state check
        if (isReady()) {
            var queryStr = query.toString()
            // sanity check
            queryStr = queryStr.trim { it <= ' ' }
            queryStr = queryStr.replace("^\\s+".toRegex(), "")
            queryStr = queryStr.replace("\\s+$".toRegex(), "")
            if (queryStr.isNotEmpty()) {
                val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
                var asGraph = sharedPrefs.getBoolean(GeneralSettings.PREF_AS_GRAPH, false)
                if (longClick) {
                    asGraph = !asGraph
                }
                val asGraphs = sharedPrefs.getBoolean(GeneralSettings.PREF_AS_GRAPHS, true)
                tryStartParse(
                    this,
                    if (asGraph)
                        (if (asGraphs) SemanticGraphsParseActivity::class.java else SemanticGraphParseActivity::class.java)
                    else
                        SemanticParseActivity::class.java,
                    queryStr
                )
            }
        } else
            warn(getString(R.string.provider_not_ready))
    }

    private fun labels() {
        val intent = Intent(this, LabelsActivity::class.java)
        startActivity(intent)
    }

    // Q U E R Y

    private val queryEdit: EditText?
        get() = findViewById(R.id.query)

    private var query: CharSequence?
        get() = queryEdit?.text.toString()
        set(text) {
            queryEdit?.let {
                it.setText(text)
                it.setSelection(it.text.length)
                val focus = currentFocus
                if (focus != null) {
                    val inputManager = (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                    inputManager.hideSoftInputFromWindow(this.currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                    it.clearFocus()
                }
            }
        }

    private fun setSplitQuery(sentences: Array<String>): CharSequence {
        val newText = join("\n", sentences)
        query = newText
        val message = getResources().getQuantityString(R.plurals.sentences_found, sentences.size, sentences.size)
        info(message)
        return newText
    }

    // S E N T E N C E   D E T E C T

    internal class SentenceDetect(context: Context, lang: String, private val consumer: Consumer<Array<String>>) : BaseSentenceDetect(context, lang) {

        override fun onDone(result: Array<String>) {
            consumer.accept(result)
        }
    }

    @Suppress("unused")
    private fun sentenceDetectSync() {
        val text = query
        if (text.isNullOrEmpty()) {
            return
        }
        try {
            val info = ModelInfo.read(this@BaseMainActivity) ?: throw IllegalArgumentException("Can't read model info.")
            val sentences: Array<String> = SentenceDetector.detect(this, info.lang, text.toString())
            setSplitQuery(sentences)
        } catch (e: Exception) {
            val message = e.message
            warn(message ?: "Failed detecting boundaries of <$text>")
            Log.e(TAG, "Failed detecting boundaries of <$text>", e)
        }
    }

    private fun sentenceDetect() {
        if (query.isNullOrEmpty()) {
            return
        }
        val text = query
        lifecycleScope.launch {
            try {
                val info = ModelInfo.read(this@BaseMainActivity) ?: throw IllegalArgumentException("Can't read model info.")
                val detector = SentenceDetect(this@BaseMainActivity, info.lang) { sentences: Array<String> ->
                    setSplitQuery(sentences)
                }
                detector.runAndCallback(Dispatchers.Default, text.toString())

            } catch (e: UnsupportedOperationException) {
                warn(e)
            } catch (e: Exception) {
                val message = e.message
                warn(message ?: "Failed detecting boundaries of <$text>")
                Log.e(TAG, "Failed detecting boundaries of <$text>", e)
            }
        }
    }

    private fun sentenceDetectThenDependencies(query: CharSequence?, longClick: Boolean) {
        if (query.isNullOrEmpty()) {
            return
        }
        val text = query.toString()
        lifecycleScope.launch {
            try {
                val info = ModelInfo.read(this@BaseMainActivity) ?: throw IllegalArgumentException("Can't read model info.")
                val detector = SentenceDetect(this@BaseMainActivity, info.lang) { sentences: Array<String> ->
                    val joined = setSplitQuery(sentences)
                    dependencies(joined, longClick)
                }
                detector.runAndCallback(Dispatchers.IO, text)

            } catch (e: UnsupportedOperationException) {
                warn(e)
                enableSentenceBoundaryDetection(false)
                val message = getString(R.string.status_disable_sentence_boundary_detect)
                warn(message)
            } catch (e: Exception) {
                val message = e.message
                warn(message ?: "Failed detecting boundaries of <$text>")
                Log.e(TAG, "Failed detecting boundaries of <$text>", e)
            }
        }
    }

    private fun sentenceDetectThenSemantics(query: CharSequence?, longClick: Boolean) {
        if (query.isNullOrEmpty()) {
            return
        }
        val text = query.toString()
        lifecycleScope.launch {
            try {
                val info = ModelInfo.read(this@BaseMainActivity) ?: throw IllegalArgumentException("Can't read model info.")
                val detector = SentenceDetect(this@BaseMainActivity, info.lang) { sentences: Array<String> ->
                    val joined = setSplitQuery(sentences)
                    semantics(joined, longClick)
                }
                detector.runAndCallback(Dispatchers.Default, text)

            } catch (e: UnsupportedOperationException) {
                warn(e)
                enableSentenceBoundaryDetection(false)
                val message = getString(R.string.status_disable_sentence_boundary_detect)
                warn(message)
            } catch (e: Exception) {
                val message = e.message
                warn(message ?: "Failed detecting boundaries of <$text>")
                Log.e(TAG, "Failed detecting boundaries of <$text>", e)
            }
        }
    }

    private fun enableSentenceBoundaryDetection(@Suppress("SameParameterValue") flag: Boolean) {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPrefs.edit {
            putBoolean(GeneralSettings.PREF_SENTENCE_BOUNDARY_DETECTION, flag)
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
            query = text
            return true
        }
        return false
    }

    // E V E N T

    private fun onEvent(e: Broadcast.EventType) {
        Log.d(TAG, "Event $e")
        when (e) {
            Broadcast.EventType.CONNECTED -> {
                updateControl(false, clear = true)
                updateLoading()
            }

            Broadcast.EventType.CONNECTED_FAILURE, Broadcast.EventType.DISCONNECTED -> {
                updateControl(false)
            }

            Broadcast.EventType.BOUND -> {
            }

            Broadcast.EventType.UNBOUND, Broadcast.EventType.BOUND_FAILURE -> {
            }

            Broadcast.EventType.LOADED, Broadcast.EventType.EMBEDDED_LOADED -> {
                updateLoaded(true)
                updateControl(isReady())
            }

            Broadcast.EventType.UNLOADED, Broadcast.EventType.EMBEDDED_UNLOADED, Broadcast.EventType.LOADED_FAILURE, Broadcast.EventType.EMBEDDED_LOADED_FAILURE -> {
                updateLoaded(false)
                updateControl(false)
            }
        }
    }

    // U I

    private fun isReady(): Boolean {
        return UniqueProvider.SINGLETON.get()?.let {
            val status = it.getStatus()
            val isEmbedded = status and IProvider.STATUS_EMBEDDED != 0
            val isBound = status and IProvider.STATUS_BOUND != 0
            val isLoaded = status and IProvider.STATUS_LOADED != 0
            if (isEmbedded) isBound && isLoaded else isLoaded
        } ?: false
    }

    private fun updateStatus() {
        val provider = UniqueProvider.SINGLETON.get()
        if (provider == null) {
            updateLoaded(false)
            updateControl(false)
        } else {
            val status = provider.getStatus()
            val isEmbedded = status and IProvider.STATUS_EMBEDDED != 0
            val isBound = status and IProvider.STATUS_BOUND != 0
            val isLoaded = status and IProvider.STATUS_LOADED != 0
            updateLoaded(isLoaded)
            updateControl(if (isEmbedded) isBound else isLoaded)
        }
    }

    private fun updateControl(bound: Boolean, clear: Boolean = false) {
        if (clear) {
            controlMenuItem?.let {
                it.title = null
                it.icon = null
            }
        } else {
            val title = if (bound) R.string.action_unbind else R.string.action_bind
            val drawable = AppCompatResources.getDrawable(this, if (bound) R.drawable.ic_unbind else R.drawable.ic_bind)!!
            val tints = Colors.getColorAttrs(this, CommonR.style.MyTheme, intArrayOf(MaterialR.attr.colorOnPrimary, MaterialR.attr.colorAccent))
            val tint = tints[if (bound) 0 else 1]
            DrawableCompat.setTint(drawable, tint)
            controlMenuItem?.let {
                it.setTitle(title)
                it.icon = drawable
            }
        }

        if (bound) {
            fabDependencies.show()
            fabSemantics.show()
        } else {
            fabDependencies.hide()
            fabSemantics.hide()
        }
    }

    private fun updateLoading() {
        Log.d(TAG, "UPDATE loading")
        controlMenuItem?.let {
            it.setTitle(R.string.provider_pending)
            val animatedDrawable = AnimatedVectorDrawableCompat.create(this, R.drawable.animated_pending)!!
            val tint = Colors.getColorAttr(this, CommonR.style.MyTheme, MaterialR.attr.colorOnPrimary)
            DrawableCompat.setTint(animatedDrawable, tint)
            it.icon = animatedDrawable
            animatedDrawable.start()
        }
    }

    private fun updateLoaded(loaded: Boolean) {
        Log.d(TAG, "UPDATE loaded: $loaded")
        if (loaded) {
            try {
                val tint = Colors.getColorAttr(this, CommonR.style.MyTheme, MaterialR.attr.colorOnPrimary)
                loadedIndicator.setColorFilter(tint)
                val animatedDrawable = AnimatedVectorDrawableCompat.create(this, R.drawable.animated_bound)!!
                loadedIndicator.setImageDrawable(animatedDrawable)
                animatedDrawable.start()
            } catch (_: Resources.NotFoundException) {
                // BUGFIX for pre 21 Huawei P8
                loadedIndicator.setImageResource(R.drawable.ic_bound)
            }
        } else {
            loadedIndicator.setImageResource(0)
        }
    }

    private fun updateLanguage() {
        val info = ModelInfo.read(this)
        setLanguage(info?.lang)
    }

    private fun setLanguage(language: String?) {
        val shortLanguage = language?.substring(0, 2)?.uppercase(Locale.getDefault()) ?: ""
        langIndicator.text = shortLanguage
        val drawable = LanguageFlag.getDrawable(this)
        if (drawable != null) {
            //DisplayMetrics dm = new DisplayMetrics();
            //getWindowManager().getDefaultDisplay().getMetrics(dm);
            //float density = dm.density;
            val density = getResources().displayMetrics.density
            drawable.setBounds(0, 0, (density * FLAG_WIDTH).toInt(), (density * FLAG_HEIGHT).toInt())
            langIndicator.setCompoundDrawables(drawable, null, null, null)
        } else {
            var iconId = 0
            when (shortLanguage) {
                "EN" -> iconId = R.drawable.english
                "FR" -> iconId = R.drawable.french
                else -> {}
            }
            langIndicator.setCompoundDrawablesWithIntrinsicBounds(iconId, 0, 0, 0)
        }
    }

    private fun status(): String {
        return "${ProviderManager.providerToString(this, UniqueProvider.SINGLETON.get())}\n${ModelInfo.modelToString(this)}"
    }

    protected fun version(): String {
        return ProviderManager.engineVersion()
    }

    // S N A C K B A R

    private fun warn(exception: Exception) {
        val contentView = findViewById<View>(android.R.id.content)
        val message = exception.message ?: exception.toString()
        showSnackbar(this, contentView, message, android.R.color.holo_red_light, android.R.color.white)
    }

    private fun warn(message: String) {
        val contentView = findViewById<View>(android.R.id.content)
        showSnackbar(this, contentView, message, android.R.color.holo_red_light, android.R.color.white)
    }

    private fun info(message: String) {
        val contentView = findViewById<View>(android.R.id.content)
        showSnackbar(this, contentView, message)
    }

    // D A Y

    override fun onNightModeChanged(mode: Int) {
        super.onNightModeChanged(mode)
        val overrideConfig = Application.createOverrideConfigurationForDayNight(this, mode)
        application.onConfigurationChanged(overrideConfig)
    }

    companion object {

        private const val TAG = "Main"
        private const val SENTENCE_ID_OFFSET = 1000
        private const val FLAG_WIDTH = 24
        private const val FLAG_HEIGHT = 16

        private fun join(@Suppress("SameParameterValue") delimiter: String, items: Array<String>): CharSequence {
            val sb = StringBuilder()
            var first = true
            for (item in items) {
                if (first) {
                    first = false
                } else {
                    sb.append(delimiter)
                }
                sb.append(item)
            }
            return sb
        }

        // S T A R T A C T I V I T Y

        fun tryStartWithText(activity: Activity, query: String) {
            val component = ComponentName(activity, "org.grammarscope.MainActivity")
            val intent = Intent()
            intent.component = component
            intent.action = Intent.ACTION_SEARCH
            intent.putExtra(SearchManager.QUERY, query)
            activity.startActivity(intent)
        }
    }
}
