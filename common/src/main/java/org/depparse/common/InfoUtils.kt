/*
 * Copyright (c) 2024. Bernard Bou <1313ou@gmail.com>.
 */

package org.depparse.common

import android.content.Context
import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import com.google.android.material.behavior.SwipeDismissBehavior
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.R as MaterialR

private fun Snackbar.setTextMinLines(min: Int): Snackbar {
    val snackText = view.findViewById<TextView>(MaterialR.id.snackbar_text)
    snackText.minLines = min
    return this
}

fun makeSnackbar(
    context: Context,
    view: View,
    @StringRes textId: Int,
    minLines: Int = 3,
    @AttrRes backColorAttr: Int = MaterialR.attr.colorTertiary,
    @AttrRes foreColorAttr: Int = MaterialR.attr.colorOnTertiary,
    duration: Int = Snackbar.LENGTH_LONG,
): Snackbar {
    val formattedText = Html.fromHtml(context.getString(textId), Html.FROM_HTML_MODE_LEGACY)
    return makeSnackbar(context, view, formattedText, minLines = minLines, backColorAttr = backColorAttr, foreColorAttr = foreColorAttr, duration = duration)
}

fun makeAnchoredSnackbar(
    context: Context,
    anchorView: View,
    @StringRes textId: Int,
    minLines: Int = 3,
    @AttrRes backColorAttr: Int = MaterialR.attr.colorTertiary,
    @AttrRes foreColorAttr: Int = MaterialR.attr.colorOnTertiary,
    duration: Int = Snackbar.LENGTH_LONG,
): Snackbar {
    val formattedText = Html.fromHtml(context.getString(textId), Html.FROM_HTML_MODE_LEGACY)
    return makeAnchoredSnackbar(context, anchorView, formattedText, minLines = minLines, backColorAttr = backColorAttr, foreColorAttr = foreColorAttr, duration = duration)
}

fun makeSnackbar(
    context: Context,
    view: View,
    text: CharSequence,
    minLines: Int = 3,
    @AttrRes backColorAttr: Int = MaterialR.attr.colorTertiary,
    @AttrRes foreColorAttr: Int = MaterialR.attr.colorOnTertiary,
    duration: Int = Snackbar.LENGTH_LONG,
): Snackbar {
    val colors: IntArray = Colors.getColorAttrs(context, R.style.MyTheme, intArrayOf(backColorAttr, foreColorAttr))
    return Snackbar
        .make(view, text, duration)
        .setTextMaxLines(8)
        .setTextMinLines(minLines)
        .setBackgroundTint(colors[0])
        .setTextColor(colors[1])
}

fun makeActionSnackbar(
    context: Context,
    anchorView: View,
    text: CharSequence,
    minLines: Int = 3,
    @AttrRes backColorAttr: Int = MaterialR.attr.colorTertiary,
    @AttrRes foreColorAttr: Int = MaterialR.attr.colorOnTertiary,
    duration: Int = Snackbar.LENGTH_LONG,
    action: (View) -> Unit,
): Snackbar {
    return makeSnackbar(context, anchorView, text, minLines = minLines, backColorAttr = backColorAttr, foreColorAttr = foreColorAttr, duration = duration)
         .setAction(android.R.string.ok, action)
}

fun makeAnchoredSnackbar(
    context: Context,
    anchorView: View,
    text: CharSequence,
    minLines: Int = 3,
    @AttrRes backColorAttr: Int = MaterialR.attr.colorTertiary,
    @AttrRes foreColorAttr: Int = MaterialR.attr.colorOnTertiary,
    duration: Int = Snackbar.LENGTH_LONG,
): Snackbar {
    return makeActionSnackbar(context, anchorView, text, minLines = minLines, backColorAttr = backColorAttr, foreColorAttr = foreColorAttr, duration = duration) {}
        .setAnchorView(anchorView)
}

fun Snackbar.makeSwipable(
     @StringRes actionId: Int,
    listener: View.OnClickListener
): Snackbar {
     val action = context.getText(actionId)
    return makeSwipable(action, listener)
}

fun Snackbar.makeSwipable(
    action: CharSequence,
    listener: View.OnClickListener
): Snackbar {
    val behavior = BaseTransientBottomBar.Behavior()
    behavior.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY)
    return this
        .setBehavior(behavior)
        .setAction(action, listener)
        .setActionTextColor(ContextCompat.getColor(context, android.R.color.white))
}

fun showTooltip(context: Context, view: View, @StringRes textId: Int) {
    val formattedText = Html.fromHtml(context.getString(textId), Html.FROM_HTML_MODE_LEGACY)
    showTooltip(view, formattedText)
}

fun showTooltip(view: View, text: CharSequence) {
    // Using TooltipCompat ensures the most "Material 3" compliant behavior
    // for the View system across all API levels.
    TooltipCompat.setTooltipText(view, text)

    // To force the tooltip to show immediately (since it's a 'Primer'),
    // we simulate a long click which triggers the system tooltip.
    // If you prefer the Snackbar look (which is often used for M3 'Plain Tooltips'
    // that contain more than one word), the Snackbar code is actually more M3-standard.

    // However, if you want the strictly visual "PlainTooltip" anchor:
    view.performLongClick()

    // Since performLongClick might trigger your LongPress listener logic,
    // a safer Material 3 approach for a persistent primer is actually
    // the Snackbar anchored to the FAB (which you already had),
    // but styled specifically for M3.
}
