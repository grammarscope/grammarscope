/*
 * Copyright (c) 2026. Bernard Bou <1313ou@gmail.com>.
 */

package com.bbou.textgetter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.bbou.textrecog.R

class ActionBottomSheet(val sentence: String, val select: (sentence: String)->Unit) : BottomSheetDialogFragment() {

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.modal_bottom_sheet, container, false)
            view.findViewById<TextView>(R.id.sentence).setText(sentence)
            view.findViewById<Button>(R.id.btn_select).setOnClickListener {
                dismiss()
                select.invoke(sentence)
            }
            //view.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            //    dismiss()
            //}
            return view
        }
}