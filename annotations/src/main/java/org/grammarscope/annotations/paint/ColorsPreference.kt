/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope.annotations.paint

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.preference.DialogPreference
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.grammarscope.annotations.R
import org.grammarscope.annotations.paint.ColorsJson.getColorMapFromResources
import org.jung.colors.ColorPadView
import org.jung.colors.chooser.ColorChooserDialog
import org.depparse.common.R as CommonR

// import org.jung.colors.R as JungR

class ColorMapPreference(context: Context, attrs: AttributeSet?) : DialogPreference(context, attrs) {

    private lateinit var adapter: ColorMapAdapter

    private lateinit var colorMap: Map<String, Int>

    private var dialog: AlertDialog? = null

    init {
        dialogLayoutResource = R.layout.dialog_colors
        setPositiveButtonText(android.R.string.ok)
        setNegativeButtonText(android.R.string.cancel)
    }

    /**
     * Called when the preference is clicked
     */
    override fun onClick() {
        super.onClick()
        showDialog()
    }

    /**
     * Show the dialog
     */
    private fun showDialog() {
        // Load the color map from preferences or resources
        val prefsColorMap = ColorsJson.getColorMap(preferenceManager.sharedPreferences!!)
        colorMap = prefsColorMap ?: getColorMapFromResources(context.resources)
        adapter = ColorMapAdapter(colorMap.map { ColorMapAdapter.ColorItem(it.key, it.value) })

        // Build the dialog
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_colors, null)
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Set up the builder
        dialog = MaterialAlertDialogBuilder(context, CommonR.style.MyM3AlertDialogOverlay)
            .setView(view)
            .setTitle("Color Map")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                // Save the color map to preferences
                ColorsJson.saveColorMap(preferenceManager.sharedPreferences!!, adapter.items.associate { it.id to it.color })
                dialog = null
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                dialog = null
            }
            .create()
        dialog!!.show()
    }

    class ColorMapAdapter(val items: List<ColorItem>) : RecyclerView.Adapter<ColorMapAdapter.ViewHolder>() {

        data class ColorItem(val id: String, var color: Int)

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val colorView: ColorPadView = view.findViewById(R.id.color_view)
            val idView: TextView = view.findViewById(R.id.id_view)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_relation_color, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.idView.text = item.id
            holder.colorView.setValue(item.color)
            holder.colorView.setOnClickListener {
                // Open a color picker dialog
                openColorPickerDialog(holder, item)
            }
        }

        override fun getItemCount(): Int = items.size

        private fun openColorPickerDialog(holder: ViewHolder, item: ColorItem) {

            ColorChooserDialog.Builder(holder.itemView.context)
                .setColor(item.color)
                .setPositiveButton { newColor ->
                    if (newColor != null) {
                        item.color = newColor
                        holder.colorView.setValue(newColor)
                    }
                }
                //.setNeutralButton(textId = JungR.string.dialog_button_title_none) { ->
                //    item.color = null
                //    holder.colorView.setValue(null)
                //}
                .show()
        }
    }
}
