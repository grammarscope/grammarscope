/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>.
 */
package com.bbou.textgetter

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.bbou.textrecog.R

/**
 * A Text fragment.
 */
class TextFragment : Fragment() {

    private lateinit var editText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window = requireActivity().window
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_gettext_text, container, false)
        editText = root.findViewById(R.id.text)
        editText.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val activity = (activity as GetTextActivity?)!!
                val sentenceData = activity.sentencesModel!!
                sentenceData.input.value = s.toString()
            }

            override fun afterTextChanged(s: Editable) {}
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = (activity as GetTextActivity?)!!
        val recogData = activity.textModel
        recogData?.output!!.observe(viewLifecycleOwner) { text: CharSequence? -> editText.setText(text) }

        val fileData = activity.textFromFileModel
        fileData?.output!!.observe(viewLifecycleOwner) { text: CharSequence? -> editText.setText(text) }
    }
}