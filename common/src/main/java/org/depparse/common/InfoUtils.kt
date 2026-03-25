/*
 * Copyright (c) 2024. Bernard Bou <1313ou@gmail.com>.
 */

package org.depparse.common

import android.content.Context
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.material.behavior.SwipeDismissBehavior
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.R as MaterialR

fun showSnackbar(context: Context, parentLayout: View, text: CharSequence, @AttrRes backColorAttr: Int = 0, @AttrRes foreColorAttr: Int = 0, duration: Int = Snackbar.LENGTH_LONG) {
    val colorAttrs: IntArray = if (backColorAttr != 0 || foreColorAttr != 0) intArrayOf(backColorAttr, foreColorAttr) else intArrayOf(MaterialR.attr.colorTertiary, MaterialR.attr.colorOnTertiary)
    val colors: IntArray = Colors.getColorAttrs(context, R.style.MyTheme, colorAttrs)
    Snackbar.make(parentLayout, text, duration)
        .setTextMaxLines(8)
        .setBackgroundTint(colors[0])
        .setTextColor(colors[1])
        .show()
}

fun showSwipableSnackbar(context: Context, parentLayout: View, @StringRes textId: Int, @AttrRes backColorAttr: Int = 0, @AttrRes foreColorAttr: Int = 0, @StringRes actionId: Int, listener: View.OnClickListener) {
    val text = context.getText(textId)
    val action = context.getText(actionId)
    showSwipableSnackbar(context, parentLayout, text, backColorAttr, foreColorAttr, action, listener)
}

fun showSwipableSnackbar(context: Context, parentLayout: View, text: CharSequence, @AttrRes backColorAttr: Int = 0, @AttrRes foreColorAttr: Int = 0, action: CharSequence, listener: View.OnClickListener) {
    val colorAttrs: IntArray = if (backColorAttr != 0 || foreColorAttr != 0) intArrayOf(backColorAttr, foreColorAttr) else intArrayOf(MaterialR.attr.colorTertiary, MaterialR.attr.colorOnTertiary)
    val colors: IntArray = Colors.getColorAttrs(context, R.style.MyTheme, colorAttrs)
    val behavior = BaseTransientBottomBar.Behavior()
    behavior.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY)
    Snackbar.make(parentLayout, text, Snackbar.LENGTH_INDEFINITE)
        .setTextMaxLines(8)
        .setBehavior(behavior)
        .setBackgroundTint(colors[0])
        .setTextColor(colors[1])
        .setAction(action, listener)
        .setActionTextColor(ContextCompat.getColor(context, android.R.color.white))
        .show()
}