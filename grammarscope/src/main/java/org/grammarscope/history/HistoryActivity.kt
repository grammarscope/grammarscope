/*
 * Copyright (c) 2019-2024. Bernard Bou
 */
package org.grammarscope.history

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.SearchRecentSuggestionsProvider
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.CursorAdapter
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import org.grammarscope.MainActivity
import org.grammarscope.common.R
import org.grammarscope.history.History.Companion.recordQuery
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * History activity
 *
 * @author Bernard Bou
 */
class HistoryActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {

    /**
     * List view
     */
    private lateinit var listView: ListView

    /**
     * Cursor adapter
     */
    private lateinit var adapter: CursorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // layout
        setContentView(R.layout.activity_history)

        // toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // set up the action bar
        val actionBar = supportActionBar!!
        actionBar.displayOptions = ActionBar.DISPLAY_USE_LOGO or ActionBar.DISPLAY_SHOW_TITLE or ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_HOME_AS_UP

        // list adapter bound to the cursor
        adapter = SimpleCursorAdapter(
            this,  // context
            R.layout.item_history,  // row template to use android.R.layout.simple_list_item_1
            null,  // empty cursor to bind to
            arrayOf(History.SuggestionColumns.DISPLAY1),  // cursor columns to bind to
            intArrayOf(android.R.id.text1),  // objects to bind to those columns
            0
        )

        // list view
        listView = findViewById(android.R.id.list)

        // bind to adapter
        listView.adapter = adapter

        // click listener
        listView.onItemClickListener = this

        // swipe
        val gestureListener = SwipeGestureListener()
        listView.setOnTouchListener(gestureListener)

        // launchers
        registerLaunchers()

        // initializes the cursor loader
        LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.history, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        when (itemId) {
            R.id.action_history_export -> {
                exportHistory()
            }

            R.id.action_history_import -> {
                importHistory()
            }

            R.id.action_history_clear -> {
                val suggestions = SearchRecentSuggestions(this, History.getAuthority(this), SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES)
                suggestions.clearHistory()
                return true
            }
        }
        return false
    }

    override fun onCreateLoader(loaderID: Int, args: Bundle?): Loader<Cursor> {
        // assert loaderID == LOADER_ID
        val history = History(this, HistoryProvider.MODE)
        return history.cursorLoader()
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor) {
        cursor.moveToFirst()
        adapter.swapCursor(cursor)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        adapter.swapCursor(null).use { }
    }

    // C L I C K

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        Log.d(TAG, "Select $position")
        val cursor = (listView.adapter as SimpleCursorAdapter).cursor
        cursor.moveToPosition(position)
        if (!cursor.isAfterLast) {
            val dataIdx = cursor.getColumnIndex(History.SuggestionColumns.DISPLAY1)
            assert(dataIdx != -1)
            val query = cursor.getString(dataIdx)
            if (null != query) {
                val intent = makeInputIntent(query)
                startActivity(intent)
            }
        }
    }

    private fun makeInputIntent(query: String): Intent {
        val intent = Intent(this, MainActivity::class.java)
        intent.action = Intent.ACTION_SEARCH
        intent.putExtra(SearchManager.QUERY, query)
        return intent
    }

    // S W I P E

    private object SwipeConstants {

        const val SWIPE_MIN_DISTANCE: Int = 120
        const val SWIPE_MAX_OFF_PATH: Int = 250
        const val SWIPE_THRESHOLD_VELOCITY: Int = 200
    }

    inner class SwipeGestureListener : SimpleOnGestureListener(), OnTouchListener {

        private val gestureDetector = GestureDetector(this@HistoryActivity, this)

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (e1 == null) {
                return false
            }

            val position = listView.pointToPosition(e1.x.roundToInt(), e1.y.roundToInt())

            if (abs((e1.y - e2.y).toDouble()) <= SwipeConstants.SWIPE_MAX_OFF_PATH) {
                if (abs(velocityX.toDouble()) >= SwipeConstants.SWIPE_THRESHOLD_VELOCITY) {
                    if (e2.x - e1.x > SwipeConstants.SWIPE_MIN_DISTANCE) {
                        val cursor = adapter.cursor
                        if (!cursor.isAfterLast) {
                            if (cursor.moveToPosition(position)) {
                                val itemIdIdx = cursor.getColumnIndex("_id")
                                assert(itemIdIdx != -1)
                                val itemId = cursor.getString(itemIdIdx)

                                val dataIdx = cursor.getColumnIndex(History.SuggestionColumns.DISPLAY1)
                                assert(dataIdx != -1)
                                val data = cursor.getString(dataIdx)

                                val suggestions = History(this@HistoryActivity, SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES)
                                suggestions.delete(itemId)
                                val cursor2 = suggestions.cursor()
                                adapter.swapCursor(cursor2)

                                Toast.makeText(this@HistoryActivity, resources.getString(R.string.title_history_delete) + ' ' + data, Toast.LENGTH_SHORT).show()
                                return true
                            }
                        }
                        // do not close: cursor.close()
                    }
                }
            }

            return super.onFling(e1, e2, velocityX, velocityY)
        }

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_UP) {
                view.performClick()
            }
            return gestureDetector.onTouchEvent(event)
        }
    }

    /**
     * Export history
     */
    private fun exportHistory() {
        exportLauncher!!.launch(MIME_TYPE)
    }

    /**
     * Import history
     */
    private fun importHistory() {
        importLauncher!!.launch(arrayOf(MIME_TYPE))
    }

    // D O C U M E N T   I N T E R F A C E

    // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
    private var exportLauncher: ActivityResultLauncher<String>? = null

    private var importLauncher: ActivityResultLauncher<Array<String>>? = null

    private fun registerLaunchers() {
        val createContract: ActivityResultContracts.CreateDocument = object : ActivityResultContracts.CreateDocument(MIME_TYPE) {
            override fun createIntent(context: Context, input: String): Intent {
                val intent: Intent = super.createIntent(context, input)
                intent.putExtra(Intent.EXTRA_TITLE, HISTORY_FILE)
                //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
                return intent
            }
        }
        exportLauncher = registerForActivityResult(createContract) { uri: Uri? ->

            // the result data contains a URI for the document or directory that the user selected.
            if (uri != null) {
                doExportHistory(uri)
            }
        }

        val openContract: ActivityResultContracts.OpenDocument = object : ActivityResultContracts.OpenDocument() {
            override fun createIntent(context: Context, input: Array<String>): Intent {
                val intent: Intent = super.createIntent(context, input)
                intent.putExtra(Intent.EXTRA_TITLE, HISTORY_FILE)
                //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
                return intent
            }
        }
        importLauncher = registerForActivityResult(openContract) { uri: Uri? ->

            // the result data contains a URIs for the document or directory that the user selected.
            if (uri != null) {
                doImportHistory(uri)
            }
        }
    }

    /**
     * Export history
     */
    private fun doExportHistory(uri: Uri) {
        Log.d(TAG, "Exporting to $uri")
        try {
            contentResolver.openFileDescriptor(uri, "w").use { fd ->
                FileOutputStream(fd!!.fileDescriptor).use { output ->
                    OutputStreamWriter(output).use { writer ->
                        BufferedWriter(writer).use { bufferedWriter ->
                            val suggestions = History(this, HistoryProvider.MODE)
                            val cursor = suggestions.cursor()!!
                            if (cursor.moveToFirst()) {
                                do {
                                    val dataIdx = cursor.getColumnIndex(History.SuggestionColumns.DISPLAY1)
                                    assert(dataIdx != -1)
                                    val data = cursor.getString(dataIdx)
                                    bufferedWriter.write(data + '\n')
                                } while (cursor.moveToNext())
                            }
                            cursor.close()
                            Log.i(TAG, "Exported to $uri")
                            Toast.makeText(this, resources.getText(R.string.title_history_export).toString() + " " + uri, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "While writing", e)
            Toast.makeText(this, resources.getText(R.string.error_export).toString() + " " + uri, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Import history
     */
    private fun doImportHistory(uri: Uri) {
        Log.d(TAG, "Importing from $uri")
        try {
            contentResolver.openInputStream(uri).use { input ->
                InputStreamReader(input).use { reader ->
                    BufferedReader(reader).use { bufferedReader ->
                        var line: String
                        while ((bufferedReader.readLine().also { line = it }) != null) {
                            recordQuery(this, line.trim { it <= ' ' })
                        }
                        Log.i(TAG, "Imported from $uri")
                        Toast.makeText(this, resources.getText(R.string.title_history_import).toString() + " " + uri, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "While reading", e)
            Toast.makeText(this, resources.getText(R.string.error_import).toString() + " " + uri, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {

        private const val TAG = "History"

        /**
         * Export/import text file
         */
        private const val HISTORY_FILE = "treebolic-wordnet_search_history.txt"

        /**
         * Cursor loader id
         */
        private const val LOADER_ID = 2222

        /**
         * Mime type for import/export
         */
        private const val MIME_TYPE = "text/plain"
    }
}
