/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>
 */
package org.grammarscope.history

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.recyclerview.widget.RecyclerView
import org.grammarscope.common.R

open class HistoryFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor> {

    lateinit var appContext: Context

    val adapter = HistoryAdapter()

    var selectListener: ((String) -> Unit)? = null
        set(selectListener) {
            adapter.onSelect = selectListener
            field = selectListener
        }

    var itemLayoutId: Int? = null
        set(itemLayoutId) {
            adapter.itemLayoutId = itemLayoutId ?: R.layout.item_history
            field = itemLayoutId
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContext = requireContext().applicationContext
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        recyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this)
    }

    override fun onCreateLoader(loaderID: Int, args: Bundle?): Loader<Cursor> {
        return History(appContext, HistoryProvider.MODE).cursorLoader()
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor) {
        adapter.changeCursor(cursor)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        adapter.changeCursor(null)
    }

    inner class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

        var onSelect: ((query: String) -> Unit)? = null

        var itemLayoutId: Int = R.layout.item_history

        private var cursor: Cursor? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(itemLayoutId, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (cursor?.moveToPosition(position) == true) {
                val dataIdx = cursor!!.getColumnIndex(History.SuggestionColumns.DISPLAY1)
                val query = cursor!!.getString(dataIdx)
                holder.textView.text = query
                holder.itemView.setOnClickListener {
                    onSelect?.invoke(query)
                }
            }
        }

        override fun getItemCount(): Int {
            return cursor?.count ?: 0
        }

        fun getCursor(): Cursor? {
            return cursor
        }

        @SuppressLint("NotifyDataSetChanged")
        fun changeCursor(newCursor: Cursor?) {
            if (cursor === newCursor) {
                return
            }
            cursor = newCursor
            notifyDataSetChanged()
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(android.R.id.text1)
        }
    }

    companion object {

        private const val TAG = "HistoryF"

        private const val LOADER_ID = 2222
    }
}
