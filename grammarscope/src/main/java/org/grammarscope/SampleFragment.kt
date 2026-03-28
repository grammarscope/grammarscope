/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>
 */
package org.grammarscope

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import org.depparse.common.Samples
import org.grammarscope.common.R

open class SampleFragment : Fragment() {

    lateinit var appContext: Context

    var values: Array<String>? = null
        set(values) {
            adapter.values = values
            field = values
        }

    var selectListener: ((String) -> Unit)? = null
        set(selectListener) {
            adapter.onSelect = selectListener
            field = selectListener
        }

    var itemLayoutId: Int? = null
        set(itemLayoutId) {
            adapter.itemLayoutId = itemLayoutId ?: R.layout.item_sample
            field = itemLayoutId
        }

    val adapter = SampleAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContext = requireContext().applicationContext
        values = Samples.read(appContext)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        recyclerView.adapter = adapter
    }

    inner class SampleAdapter : RecyclerView.Adapter<SampleAdapter.ViewHolder>() {
        var values: Array<String>? = null

        var onSelect: ((query: String) -> Unit)? = null

        var itemLayoutId: Int = R.layout.item_sample

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(itemLayoutId, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val query = values?.get(position)
            if (query != null) {
                holder.textView.text = query
                holder.itemView.setOnClickListener {
                    onSelect?.invoke(query)
                }
            }
        }

        override fun getItemCount(): Int {
            return values?.size ?: 0
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(android.R.id.text1)
        }
    }

    companion object {

        private const val TAG = "SampleF"
    }
}
