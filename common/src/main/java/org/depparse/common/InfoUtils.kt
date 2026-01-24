/*
 * Copyright (c) 2024. Bernard Bou <1313ou@gmail.com>.
 */

package org.depparse.common

import android.content.Context
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.material.behavior.SwipeDismissBehavior
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.R as MaterialR

@Suppress("unused")
fun showSnackbar(context: Context, parentLayout: View, @StringRes textId: Int, @ColorRes backColor: Int = 0, @ColorRes foreColor: Int = 0, duration: Int = Snackbar.LENGTH_LONG) {
    val text = context.getText(textId)
    showSnackbar(context, parentLayout, text, backColor, foreColor, duration)
}

fun showSnackbar(context: Context, parentLayout: View, text: CharSequence, @ColorRes backColor: Int = 0, @ColorRes foreColor: Int = 0, duration: Int = Snackbar.LENGTH_LONG) {
    val colors: IntArray = if (backColor == 0 || foreColor == 0)
        Colors.getColorAttrs(context, R.style.MyTheme, intArrayOf(MaterialR.attr.colorPrimary, MaterialR.attr.colorOnPrimary)) else
        arrayOf(ContextCompat.getColor(context, backColor), ContextCompat.getColor(context, foreColor)).toIntArray()
    val snackbar = Snackbar.make(parentLayout, text, duration)
    snackbar.setTextMaxLines(8)
        .setBackgroundTint(colors[0])
        .setTextColor(colors[1])
        .show()
}

fun showSwipableSnackbar(context: Context, parentLayout: View, @StringRes textId: Int, @ColorRes backColor: Int = 0, @ColorRes foreColor: Int = 0, @StringRes actionId: Int, listener: View.OnClickListener) {
    val text = context.getText(textId)
    val action = context.getText(actionId)
    showSwipableSnackbar(context, parentLayout, text, backColor, foreColor, action, listener)
}

fun showSwipableSnackbar(context: Context, parentLayout: View, text: CharSequence, @ColorRes backColor: Int = 0, @ColorRes foreColor: Int = 0, action: CharSequence, listener: View.OnClickListener) {
    val colors: IntArray = if (backColor == 0 || foreColor == 0)
        Colors.getColorAttrs(context, R.style.MyTheme, intArrayOf(MaterialR.attr.colorSecondary, MaterialR.attr.colorOnSecondary)) else
        arrayOf(ContextCompat.getColor(context, backColor), ContextCompat.getColor(context, foreColor)).toIntArray()
    val behavior = BaseTransientBottomBar.Behavior()
    behavior.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY)
    val snackbar = Snackbar.make(parentLayout, text, Snackbar.LENGTH_INDEFINITE)
    snackbar.setTextMaxLines(8)
        .setBehavior(behavior)
        .setBackgroundTint(colors[0])
        .setTextColor(colors[1])
        .setAction(action, listener)
        .setActionTextColor(ContextCompat.getColor(context, android.R.color.white))
        .show()
}