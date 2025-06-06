/*
 * Copyright (c) 2021. Bernard Bou <1313ou@gmail.com>.
 */
package org.grammarscope

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import org.grammarscope.common.R

class ResettablePreference : Preference {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    private var listener: View.OnClickListener? = null
    fun setClickListener(listener: View.OnClickListener?) {
        this.listener = listener
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val button = holder.findViewById(R.id.bn_reset) as Button
        button.setOnClickListener(listener)
    }
}
