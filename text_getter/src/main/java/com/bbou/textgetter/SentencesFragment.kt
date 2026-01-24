/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>.
 */
package com.bbou.textgetter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bbou.textgetter.SentencesFragment.SentencesAdapter.SentenceViewHolder
import com.bbou.textrecog.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.R as MaterialR

/**
 * A placeholder fragment containing a simple view.
 */
class SentencesFragment : Fragment() {

    private var receiver: ResultReceiver? = null
    private var code = 0
    private var resultKey: String? = null
    private lateinit var listView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_gettext_sentences, container, false)!!
        listView = root.findViewById(R.id.list)

        // Use this setting to improve performance if you know that changes in content do not change the layout size of the RecyclerView
        listView.setHasFixedSize(true)

        // Use a linear layout manager
        val layoutManager = LinearLayoutManager(requireContext())
        val orientation = layoutManager.orientation
        listView.setLayoutManager(layoutManager)

        // Decoration
        val dividerItemDecoration = DividerItemDecoration(requireContext(), orientation)
        listView.addItemDecoration(dividerItemDecoration)

        // Adapter
        val adapter: RecyclerView.Adapter<SentenceViewHolder> = SentencesAdapter()
        listView.setAdapter(adapter)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = activity as GetTextActivity
        val sentenceData = activity.sentencesModel
        sentenceData?.output?.observe(viewLifecycleOwner) { sentences: Array<String> ->
            val adapter = listView.adapter as SentencesAdapter
            adapter.setData(sentences)
        }
    }

    internal inner class SentencesAdapter : RecyclerView.Adapter<SentenceViewHolder>() {

        private var dataset: Array<String> = arrayOf()

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and you provide access to all the views for a data item in a view holder
        // Each data item is just a string in this case
        inner class SentenceViewHolder internal constructor(val textView: TextView) : RecyclerView.ViewHolder(textView) {

            init {
                itemView.setOnClickListener { view: View ->

                    // get the position on recyclerview.
                    val pos = layoutPosition
                    Log.d(TAG, "position " + pos + " " + dataset[pos])
                    val colors: IntArray = getColorAttrs(requireContext(), R.style.MyTheme, intArrayOf(MaterialR.attr.colorSecondary, MaterialR.attr.colorOnSecondary, MaterialR.attr.colorOnPrimary))
                    Snackbar.make(view, dataset[pos], Snackbar.LENGTH_LONG)
                        .setAction("Select") {
                            if (receiver != null) {
                                val result = Bundle()
                                result.putString(resultKey, dataset[pos])
                                receiver!!.send(code, result)
                            }
                        }
                        .setTextMaxLines(8)
                        .setBackgroundTint(colors[0])
                        .setTextColor(colors[1])
                        .setActionTextColor(colors[2])
                        .show()
                }
            }
        }

        /**
         * Set data
         *
         * @param dataset new dataset
         */
        @SuppressLint("NotifyDataSetChanged")
        fun setData(dataset: Array<String>) {
            this.dataset = dataset
            notifyDataSetChanged()
        }

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SentenceViewHolder {
            // create a new view
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_sentence, parent, false) as TextView
            return SentenceViewHolder(v)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(holder: SentenceViewHolder, position: Int) {
            // Get element from your dataset at this position, Replace the contents of the view with that element
            holder.textView.text = dataset[position]
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount(): Int {
            return dataset.size
        }
    }

    companion object {

        private const val TAG = "SentencesF"

        fun newInstance(receiver: ResultReceiver?, code: Int, resultKey: String?): SentencesFragment {
            val f = SentencesFragment()
            f.receiver = receiver
            f.code = code
            f.resultKey = resultKey
            return f
        }

        fun getColorAttrs(context: Context, @StyleRes themeId: Int, @StyleableRes resIds: IntArray): IntArray {
            val result: IntArray
            context.theme.obtainStyledAttributes(themeId, resIds).let {
                result = IntArray(resIds.size)
                for (i in resIds.indices) {
                    result[i] = it.getColor(i, -1)
                }
                it.recycle()
            }
            return result
        }
    }
}