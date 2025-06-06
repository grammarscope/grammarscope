package org.grammarscope

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.depparse.Storage.getAppStorage
import org.depparse.common.WebActivity.Companion.tryStart
import org.grammarscope.common.R
import org.grammarscope.semantics.SemanticRelations.ObjectRelations
import org.grammarscope.semantics.SemanticRelations.PredicateModifierRelations
import org.grammarscope.semantics.SemanticRelations.PredicateRelations
import org.grammarscope.semantics.SemanticRelations.SubjectRelations
import org.grammarscope.semantics.SemanticRelations.TermModifierRelations
import org.grammarscope.semantics.SemanticRelations.read
import org.grammarscope.semantics.SemanticRelations.unregister
import org.grammarscope.semantics.SemanticRelations.write
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

/**
 * Labels activity
 *
 * @author Bernard Bou
 */
class LabelsActivity : AppCompatActivity() {

    /**
     * List adapter
     */
    private var adapter: ListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // layout
        setContentView(R.layout.activity_labels)

        // toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // buttons
        val btnOk = findViewById<Button>(R.id.ok)
        btnOk.setOnClickListener { onOkClick() }
        val btnCancel = findViewById<Button>(R.id.cancel)
        btnCancel.setOnClickListener { onCancelClick() }

        // show the Up button in the action bar.
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.displayOptions = ActionBar.DISPLAY_USE_LOGO or ActionBar.DISPLAY_SHOW_TITLE or ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_HOME_AS_UP
        }
    }

    override fun onResume() {
        super.onResume()

        // list adapter bound to the cursor
        val labels: Array<String> = try {
            this.labels
        } catch (e: IOException) {
            finish()
            return
        }
        read(this)
        val data: MutableList<Map<String, Any>> = ArrayList()
        for (label in labels) {
            val row: MutableMap<String, Any> = HashMap()
            row[KEY_LABEL] = label
            row[KEY_PREDICATE] = PredicateRelations.contains(label)
            row[KEY_SUBJECT] = SubjectRelations.contains(label)
            row[KEY_OBJECT] = ObjectRelations.contains(label)
            row[KEY_TERM_MODIFIER_PREDICATE] = TermModifierRelations.contains(label)
            row[KEY_PREDICATE_MODIFIER_PREDICATE] = PredicateModifierRelations.contains(label)
            data.add(row)
        }
        val from = arrayOf(KEY_LABEL, KEY_PREDICATE, KEY_SUBJECT, KEY_OBJECT, KEY_TERM_MODIFIER_PREDICATE, KEY_PREDICATE_MODIFIER_PREDICATE)
        val to = intArrayOf(android.R.id.text1, R.id.predicate, R.id.subject, R.id.`object`, R.id.term_modifier_predicate, R.id.predicate_modifier_predicate)
        adapter = object : SimpleAdapter(this, data, R.layout.item_semantic_labels, from, to) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)

                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setOnClickListener { v: View -> onFollow(v as TextView) }
                val cb1 = view.findViewById<CheckBox>(R.id.predicate)
                cb1.tag = position
                cb1.contentDescription = getString(R.string.description_p_check, labels[position])
                cb1.setOnClickListener { view2: View -> onCheck(view2) }
                val cb2 = view.findViewById<CheckBox>(R.id.subject)
                cb2.tag = position
                cb2.contentDescription = getString(R.string.description_s_check, labels[position])
                cb2.setOnClickListener { view2: View -> onCheck(view2) }
                val cb3 = view.findViewById<CheckBox>(R.id.`object`)
                cb3.tag = position
                cb3.contentDescription = getString(R.string.description_o_check, labels[position])
                cb3.setOnClickListener { view2: View -> onCheck(view2) }
                val cb4 = view.findViewById<CheckBox>(R.id.term_modifier_predicate)
                cb4.tag = position
                cb4.contentDescription = getString(R.string.description_tp_check, labels[position])
                cb4.setOnClickListener { view2: View -> onCheck(view2) }
                val cb5 = view.findViewById<CheckBox>(R.id.predicate_modifier_predicate)
                cb5.tag = position
                cb5.contentDescription = getString(R.string.description_pp_check, labels[position])
                cb5.setOnClickListener { view2: View -> onCheck(view2) }
                return view
            }
        }

        // list view
        val listView = findViewById<ListView>(android.R.id.list)

        // bind to adapter
        listView.adapter = adapter

        // focus
        listView.itemsCanFocus = true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.labels, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val i = item.itemId
        if (i == R.id.semantics_reset) {
            unregister(this)
            finish()
            return true
        }
        return false
    }

    @get:Throws(IOException::class)
    private val labels: Array<String>
        get() {
            val labelsFile = getString(R.string.labels_file)
            return if (labelsFile.isNotEmpty()) {
                getLabels(labelsFile)
            } else defaultLabels
        }

    @get:Throws(IOException::class)
    private val defaultLabels: Array<String>
        get() {
            val assetManager = this.assets
            val list: MutableList<String> = ArrayList()
            assetManager.open(ASSET_LABELS).use { `is` ->
                BufferedReader(InputStreamReader(`is`, StandardCharsets.UTF_8)).use { br ->
                    var line = br.readLine() // first line = count
                    if (line != null) {
                        val count = line.toInt()
                        var i = 0
                        while (null != br.readLine().also { line = it }) {
                            val fields = line!!.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                            list.add(fields[0])
                            i++
                        }
                        if (i != count) {
                            throw RuntimeException("Error in count")
                        }
                    }
                }
            }
            list.sort()
            return list.toTypedArray<String>()
        }

    @Throws(IOException::class)
    private fun getLabels(labelsFile: String): Array<String> {
        val list: MutableList<String> = ArrayList()
        BufferedReader(FileReader(File(getAppStorage(this), labelsFile))).use { br ->
            var line = br.readLine() // first line = count
            if (line != null) {
                val count = line.toInt()
                var i = 0
                while (null != br.readLine().also { line = it }) {
                    val fields = line!!.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    list.add(fields[0])
                    i++
                }
                if (i != count) {
                    throw RuntimeException("Error in count")
                }
            }
        }
        list.sort()
        return list.toTypedArray<String>()
    }

    // C L I C K

    private fun onFollow(view: TextView) {
        var label = view.text.toString()
        val split = label.lastIndexOf(':')
        if (split != -1) {
            label = label.substring(0, split)
        }
        val pathInAssets = "reference/$label.html"
        // check if openable
        try {
            assets.open(pathInAssets).use { }
        } catch (e: IOException) {
            return
        }
        tryStart(this, "file:///android_asset/$pathInAssets", usesJavaScript = false, local = true)
    }

    fun onCheck(view: View) {
        val cb = view as CheckBox
        val position = cb.tag as Int
        val id = cb.id.toLong()
        val checked = cb.isChecked
        Log.d(TAG, "Check $position id=$id")
        @Suppress("UNCHECKED_CAST")
        val row = adapter!!.getItem(position) as MutableMap<String, Any>
        when (id) {
            R.id.predicate.toLong() -> {
                row[KEY_PREDICATE] = checked
            }

            R.id.subject.toLong() -> {
                row[KEY_SUBJECT] = checked
            }

            R.id.`object`.toLong() -> {
                row[KEY_OBJECT] = checked
            }

            R.id.term_modifier_predicate.toLong() -> {
                row[KEY_TERM_MODIFIER_PREDICATE] = checked
            }

            R.id.predicate_modifier_predicate.toLong() -> {
                row[KEY_PREDICATE_MODIFIER_PREDICATE] = checked
            }
        }
    }

    private fun onOkClick() {
        PredicateRelations.clear()
        SubjectRelations.clear()
        ObjectRelations.clear()
        TermModifierRelations.clear()
        PredicateModifierRelations.clear()
        val n = adapter!!.count
        for (position in 0 until n) {
            val row = adapter!!.getItem(position) as Map<*, *>
            Log.d(TAG, "At $position$row")
            val label = row[KEY_LABEL] as String
            var b = row[KEY_PREDICATE] as Boolean?
            if (b != null && b) {
                PredicateRelations.add(label)
            }
            b = row[KEY_SUBJECT] as Boolean?
            if (b != null && b) {
                SubjectRelations.add(label)
            }
            b = row[KEY_OBJECT] as Boolean?
            if (b != null && b) {
                ObjectRelations.add(label)
            }
            b = row[KEY_TERM_MODIFIER_PREDICATE] as Boolean?
            if (b != null && b) {
                TermModifierRelations.add(label)
            }
            b = row[KEY_PREDICATE_MODIFIER_PREDICATE] as Boolean?
            if (b != null && b) {
                PredicateModifierRelations.add(label)
            }
        }
        write(this)
        finish()
    }

    private fun onCancelClick() {
        Log.d(TAG, "Cancel")
        finish()
    }

    companion object {

        private const val TAG = "LabelsActivity"
        private const val KEY_LABEL = "l"
        private const val KEY_PREDICATE = "p"
        private const val KEY_SUBJECT = "s"
        private const val KEY_OBJECT = "o"
        private const val KEY_TERM_MODIFIER_PREDICATE = "tp"
        private const val KEY_PREDICATE_MODIFIER_PREDICATE = "pp"

        // G E T   L A B E L S

        private const val ASSET_LABELS = "labels.txt" //"file:///android_asset/labels.txt";
    }
}
