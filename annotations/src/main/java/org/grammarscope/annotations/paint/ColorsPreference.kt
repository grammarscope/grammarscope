/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope.annotations.paint

import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.preference.DialogPreference
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.grammarscope.annotations.R
import org.grammarscope.annotations.paint.ColorsJson.getColorMapFromResources
import org.jung.colors.ColorPadView
import org.jung.colors.chooser.ColorChooserDialog
import org.jung.colors.chooser.ColorChooserView.OnColorChangedListener

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
        val builder = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_colors, null)
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Set up the builder
        builder.setView(view)
            .setTitle("Color Map")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                // Save the color map to preferences
                ColorsJson.saveColorMap(preferenceManager.sharedPreferences!!, adapter.items.associate { it.id to it.color })
                dialog = null
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                dialog = null
            }
        // Show the dialog
        dialog = builder.create()
        dialog?.show()
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
                // Open a color picker dialog (you'll need to implement this part)
                openColorPickerDialog(holder, item)
            }
        }

        override fun getItemCount(): Int = items.size

        private fun openColorPickerDialog(holder: ViewHolder, item: ColorItem) {

            var newColor = item.color

            val colorChooserDialog = ColorChooserDialog(holder.itemView.context, item.color, object : OnColorChangedListener {
                override fun onColorChanged(color: Int) {
                    newColor = color
                }
            })
            colorChooserDialog.setButton(AlertDialog.BUTTON_NEGATIVE, holder.itemView.context.getString(android.R.string.cancel), object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, p: Int) {
                }
            })
            colorChooserDialog.setButton(AlertDialog.BUTTON_POSITIVE, holder.itemView.context.getString(android.R.string.ok), object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, p: Int) {
                    item.color = newColor
                    holder.colorView.setValue(newColor)
                }
            })
            colorChooserDialog.show()
        }
    }
}