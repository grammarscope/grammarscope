/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>.
 */
package com.bbou.textgetter

import android.os.ResultReceiver
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * A FragmentStateAdapter that returns a fragment corresponding to one of the sections/tabs/pages.
 */
class PagerAdapter(activity: FragmentActivity, private val receiver: ResultReceiver, private val code: Int, private val resultKey: String) : FragmentStateAdapter(activity) {

    /** Called to instantiate the fragment for the given page */
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TextFragment()
            1 -> SentencesFragment.newInstance(receiver, code, resultKey)
            else -> throw RuntimeException("Unexpected tab position")
        }
    }

    override fun getItemCount(): Int {
        return 2
    }
}