/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope.annotations

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bbou.capture.Capture.captureAndSave
import com.bbou.capture.Capture.captureAndShare
import com.bbou.capture.Capture.getBackgroundFromTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.depparse.Token
import org.depparse.common.BaseParseActivity
import org.grammarscope.annotations.AnnotationsSettings.Companion.PREF_BOX_EDGES
import org.grammarscope.annotations.AnnotationsSettings.Companion.PREF_BOX_WORDS
import org.grammarscope.annotations.AnnotationsSettings.Companion.PREF_IGNORE_RELATIONS
import org.grammarscope.annotations.annotate.AnnotationManager
import org.grammarscope.annotations.annotate.DependencyAnnotator
import org.grammarscope.annotations.annotate.PosAnnotator
import org.grammarscope.annotations.document.AnnotationParse
import org.grammarscope.annotations.document.Document

class AnnotatedTextActivity : BaseParseActivity<Document<Token>?>() {

    lateinit var textView: AnnotatedTextView

    lateinit var query: String

    private var document: Document<Token>? = null

    override val layout: Int
        get() = R.layout.activity_annotated

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // retrieve text from intent
        query = intent.getStringExtra(ARG_SOURCE)!!

        // access text view
        val textControl = findViewById<AnnotatedTextControl>(R.id.annotated_text_control)
        textView = findViewById(textControl.textView.id)

        // set text
        textView.text = query + '\n'

        // observe layout change events
        textView.changed.observe(this) {
            Log.d(TAG, "Intercepted layout change")
            runAnnotations()
        }
    }

    // T E X T   T O   P A R S E D   D O C U M E N T

    override fun runParse(source: String?) {
        if (source.isNullOrEmpty()) {
            return
        }
        Log.d(TAG, "Parse run on '$source'")
        lifecycleScope.launch {
            AnnotationParse(this@AnnotatedTextActivity).runAndCallback(Dispatchers.IO, source)
        }
    }

    override fun accept(result: Document<Token>?) {
        Log.d(TAG, "Accept document")
        if (result != null) {
            this.document = result

            runAnnotations()
        }
    }

    // D O C U M E N T   T O   A N N O T A T I O N S

    private fun runAnnotations() {
        Log.d(TAG, "Annotate from document")
        if (document != null) {
            textView.post {
                this.lifecycleScope.launch {
                    val sharedPrefs = AnnotationsSettings(this@AnnotatedTextActivity).sharedPrefs
                    val boxWords = sharedPrefs.getBoolean(PREF_BOX_WORDS, false)
                    val boxEdges = sharedPrefs.getBoolean(PREF_BOX_EDGES, false)
                    var ignoreRelations = sharedPrefs.getStringSet(PREF_IGNORE_RELATIONS, null) ?: resources.getStringArray(R.array.default_ignored_relations_keys).toSet()
                    val manager = AnnotationManager(textView)
                    val depAnnotator = DependencyAnnotator<Token>(textView, manager, boxWords = boxWords, boxEdges = boxEdges, ignoreRelations = ignoreRelations)
                    val depAnnotations = depAnnotator.annotate(document!!)!!
                    val posAnnotator = PosAnnotator<Token>(textView, manager, ignoreRelations = ignoreRelations)
                    val posAnnotations = posAnnotator.annotate(document!!)!!
                    textView.annotations = depAnnotations + posAnnotations
                    textView.invalidate()
                }
            }
        }
    }

    override fun pending() {
        Toast.makeText(this, R.string.status_processing, Toast.LENGTH_SHORT).show()
    }

    // M E N U

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.annotation, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.annotation_settings -> {
                startActivity(Intent(this, AnnotationsSettingsActivity::class.java))
                return true
            }

            R.id.action_capture -> {
                val view = capturedView(this)
                if (view != null) {
                    val bg = getBackgroundFromTheme(this)
                    captureAndSave(view, this, backGround = bg)
                }
                return true
            }

            R.id.action_share_capture -> {
                val view = capturedView(this)
                if (view != null) {
                    val bg = getBackgroundFromTheme(this)
                    captureAndShare(view, this, backGround = bg)
                }
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun capturedView(activity: AppCompatActivity): View? {
        var view = activity.findViewById<View>(textView.id)
        if (view != null && view.width > 0 && view.height > 0) {
            return view
        }
        Toast.makeText(activity, R.string.status_capture_no_view, Toast.LENGTH_SHORT).show()
        return null
    }

    companion object {
        private const val TAG = "AnnotatedParseA"
    }
}
