/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>
 */
package org.grammarscope.history

import android.content.SearchRecentSuggestionsProvider
import android.net.Uri
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.loader.app.LoaderManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import org.grammarscope.common.R
import org.grammarscope.history.History.Companion.getAuthority
import org.grammarscope.history.History.Companion.recordQuery
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class HistoryEditFragment : HistoryFragment() {

    /**
     * Fragment menu provider
     */
    private val fragmentMenuProvider = object : MenuProvider {

        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.history, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.action_history_export -> {
                    exportHistory()
                    true
                }

                R.id.action_history_import -> {
                    importHistory()
                    true
                }

                R.id.action_history_clear -> {
                    val suggestions = SearchRecentSuggestions(requireContext(), getAuthority(appContext), SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES)
                    suggestions.clearHistory()
                    true
                }

                else -> false
            }
        }
    }

    private val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.bindingAdapterPosition
            val cursor = adapter.getCursor()
            if (cursor != null && cursor.moveToPosition(position)) {
                val itemIdIdx = cursor.getColumnIndex("_id")
                val itemId = cursor.getString(itemIdIdx)
                val dataIdx = cursor.getColumnIndex(History.SuggestionColumns.DISPLAY1)
                val data = cursor.getString(dataIdx)
                val suggestions = History(requireContext(), SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES)
                suggestions.delete(itemId)
                // Restart the loader to get the updated cursor
                LoaderManager.getInstance(this@HistoryEditFragment).restartLoader(LOADER_ID, null, this@HistoryEditFragment)
                Toast.makeText(requireContext(), resources.getString(R.string.status_deleted) + ' ' + data, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLaunchers()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.search_list)

        val touchHelper = ItemTouchHelper(swipeCallback)
        touchHelper.attachToRecyclerView(recyclerView)

        requireActivity().addMenuProvider(fragmentMenuProvider, getViewLifecycleOwner(), Lifecycle.State.RESUMED)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::exportLauncher.isInitialized) {
            exportLauncher.unregister()
        }
        if (::importLauncher.isInitialized) {
            importLauncher.unregister()
        }
    }

    // E X P O R T

    private fun exportHistory() {
        exportLauncher.launch(MIME_TYPE)
    }

    private fun importHistory() {
        importLauncher.launch(arrayOf(MIME_TYPE))
    }

    private lateinit var exportLauncher: ActivityResultLauncher<String>

    private lateinit var importLauncher: ActivityResultLauncher<Array<String>>

    private fun registerLaunchers() {
        exportLauncher = registerForActivityResult(CreateDocument(MIME_TYPE)) { uri: Uri? ->
            uri?.let { doExportHistory(it) }
        }
        importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let { doImportHistory(it) }
        }
    }

    private fun doExportHistory(uri: Uri) {
        Log.d(TAG, "Exporting to $uri")
        try {
            appContext.contentResolver.openFileDescriptor(uri, "w").use { pfd ->
                FileOutputStream(pfd!!.fileDescriptor).use { fileOutputStream ->
                    OutputStreamWriter(fileOutputStream).use { writer ->
                        BufferedWriter(writer).use { bw ->
                            val suggestions = History(appContext, SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES)
                            suggestions.cursor().use { cursor ->
                                if (cursor?.moveToFirst() == true) {
                                    do {
                                        val dataIdx = cursor.getColumnIndex(History.SuggestionColumns.DISPLAY1)
                                        val data = cursor.getString(dataIdx)
                                        bw.write(data + '\n')
                                    } while (cursor.moveToNext())
                                }
                            }
                            Log.i(TAG, "Exported to $uri")
                            Toast.makeText(appContext, resources.getText(R.string.title_history_export).toString() + " " + uri, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "While writing", e)
            Toast.makeText(appContext, resources.getText(R.string.error_export).toString() + " " + uri, Toast.LENGTH_SHORT).show()
        }
    }

    private fun doImportHistory(uri: Uri) {
        Log.d(TAG, "Importing from $uri")
        try {
            appContext.contentResolver.openInputStream(uri).use { `is` ->
                InputStreamReader(`is`).use { reader ->
                    BufferedReader(reader).use { br ->
                        var line: String?
                        while (br.readLine().also { line = it } != null) {
                            recordQuery(appContext, line!!.trim { it <= ' ' })
                        }
                        Log.i(TAG, "Imported from $uri")
                        Toast.makeText(appContext, resources.getText(R.string.title_history_import).toString() + " " + uri, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "While reading", e)
            Toast.makeText(appContext, resources.getText(R.string.error_import).toString() + " " + uri, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {

        private const val TAG = "HistoryEF"

        private const val LOADER_ID = 2222

        private const val MIME_TYPE = "text/plain"

        // private const val HISTORY_FILE = "semantikos_search_history.txt"
    }
}