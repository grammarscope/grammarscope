/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope.annotations

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.TextUtils.TruncateAt
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.ScrollView
import androidx.annotation.AttrRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.grammarscope.annotations.annotate.AnnotationManager.Companion.INITIAL_LINESPACING
import org.grammarscope.annotations.annotate.AnnotationManager.Companion.INITIAL_TEXTSIZE
import org.grammarscope.annotations.annotate.AnnotationManager.Companion.INITIAL_WORDSPACING
import org.grammarscope.annotations.annotate.AnnotationManager.Companion.LINE_SPACING_MULTIPLIER

class AnnotatedTextControl(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {


    private val minTextSize = 12f
    private val maxTextSize = 50f
    private val textSizeIncrement = 2f

    private val minLineSpacing = 100f
    private val maxLineSpacing = 1500f
    private val lineSpacingIncrement = 50f

    private val minWordSpacing = 0f
    private val maxWordSpacing = 50f
    private val wordSpacingIncrement = 2f

    private var currentTextSize = INITIAL_TEXTSIZE
    private var currentLineSpacing = INITIAL_LINESPACING
    private var currentWordSpacing = INITIAL_WORDSPACING

    private val padding = 16

    private val scrollView: ScrollView = ScrollView(context).apply {
        id = generateViewId()
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        isFillViewport = true
        isVerticalScrollBarEnabled = true
    }

    internal val textView: AppCompatTextView = AnnotatedTextView(context).apply {
        id = generateViewId()
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        textSize = INITIAL_TEXTSIZE
        setLineSpacing(INITIAL_LINESPACING, lineSpacingMultiplier)
        setPadding(padding)
        ellipsize = TruncateAt.END
    }

    private val increaseLineSpacingButton: FloatingActionButton = createTransparentFab(context, R.drawable.btn_plus).apply {
        id = generateViewId()
    }

    private val decreaseLineSpacingButton: FloatingActionButton = createTransparentFab(context, R.drawable.btn_minus).apply {
        id = generateViewId()
    }

    private val increaseTextSizeButton: FloatingActionButton = createTransparentFab(context, R.drawable.btn_expand).apply {
        id = generateViewId()
    }

    private val decreaseTextSizeButton: FloatingActionButton = createTransparentFab(context, R.drawable.btn_collapse).apply {
        id = generateViewId()
    }

    private val increaseLetterSpacingButton: FloatingActionButton = createTransparentFab(context, R.drawable.btn_widen).apply {
        id = generateViewId()
    }

    private val decreaseLetterSpacingButton: FloatingActionButton = createTransparentFab(context, R.drawable.btn_shrink).apply {
        id = generateViewId()
    }

    init {
        textView.setPadding(LEFT_PADDING, 0, RIGHT_PADDING, 0)

        scrollView.addView(textView)

        addView(scrollView) //, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        addView(increaseLineSpacingButton, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        addView(decreaseLineSpacingButton, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        addView(increaseTextSizeButton, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        addView(decreaseTextSizeButton, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        addView(increaseLetterSpacingButton, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        addView(decreaseLetterSpacingButton, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))

        val constraintSet = ConstraintSet()
        constraintSet.apply {
            clone(this@AnnotatedTextControl)

            // TextView
            connect(scrollView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            connect(scrollView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            connect(scrollView.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT)
            connect(scrollView.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT)

            // From bottom upwards
            // Increase Line Spacing Button
            connect(increaseLineSpacingButton.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, MARGIN_BOTTOM)
            connect(increaseLineSpacingButton.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, MARGIN_END)

            // Decrease Line Spacing Button
            connect(decreaseLineSpacingButton.id, ConstraintSet.BOTTOM, increaseLineSpacingButton.id, ConstraintSet.TOP, MARGIN_BOTTOM)
            connect(decreaseLineSpacingButton.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, MARGIN_END)

            // Increase TextSize Button
            connect(increaseTextSizeButton.id, ConstraintSet.BOTTOM, decreaseLineSpacingButton.id, ConstraintSet.TOP, MARGIN_BOTTOM)
            connect(increaseTextSizeButton.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, MARGIN_END)

            // Decrease TextSize Button
            connect(decreaseTextSizeButton.id, ConstraintSet.BOTTOM, increaseTextSizeButton.id, ConstraintSet.TOP, MARGIN_BOTTOM)
            connect(decreaseTextSizeButton.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, MARGIN_END)

            // Increase Letter spacing Button
            connect(increaseLetterSpacingButton.id, ConstraintSet.BOTTOM, decreaseTextSizeButton.id, ConstraintSet.TOP, MARGIN_BOTTOM)
            connect(increaseLetterSpacingButton.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, MARGIN_END)

            // Decrease Letter Button
            connect(decreaseLetterSpacingButton.id, ConstraintSet.BOTTOM, increaseLetterSpacingButton.id, ConstraintSet.TOP, MARGIN_BOTTOM)
            connect(decreaseLetterSpacingButton.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, MARGIN_END)

            applyTo(this@AnnotatedTextControl)
        }

        increaseLineSpacingButton.setOnClickListener {
            increaseSpacing()
        }

        decreaseLineSpacingButton.setOnClickListener {
            decreaseSpacing()
        }

        increaseTextSizeButton.setOnClickListener {
            increaseTextSize()
        }

        decreaseTextSizeButton.setOnClickListener {
            decreaseTextSize()
        }

        increaseLetterSpacingButton.setOnClickListener {
            increaseSpace()
        }

        decreaseLetterSpacingButton.setOnClickListener {
            decreaseSpace()
        }
    }

    fun createTransparentFab(context: Context, icon: Int): FloatingActionButton {

        // Create a ContextThemeWrapper with your style
        val fab = FloatingActionButton(context).apply {

            // Set transparent background
            backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)

            // Setting elevation to 0 often solves the "square shadow" artifact
            // If you want a shadow, it's better to use a standard ImageButton with a ripple
            elevation = 0f
            compatElevation = 0f

            // Ensure the icon is centered
            scaleType = android.widget.ImageView.ScaleType.CENTER

            // Set icon
            setImageResource(icon)

            // Apply the theme color to the icon
            imageTintList = ColorStateList.valueOf(context.getColorFromAttr(android.R.attr.colorPrimary))

            // Optional: Adjust size to mini to make it less intrusive
            size = FloatingActionButton.SIZE_MINI

            // Ensure there's no background shadow casting at all
            outlineProvider = null
        }
        return fab
    }

    private fun Context.getColorFromAttr(
        @Suppress("SameParameterValue") @AttrRes attrColor: Int,
        typedValue: TypedValue = TypedValue(),
        resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return ContextCompat.getColor(this, typedValue.resourceId)
    }

    private fun increaseSpacing() {
        if (currentLineSpacing < maxLineSpacing) {
            currentLineSpacing += lineSpacingIncrement
            textView.setLineSpacing(currentLineSpacing, LINE_SPACING_MULTIPLIER)
        }
    }

    private fun decreaseSpacing() {
        if (currentLineSpacing > minLineSpacing) {
            currentLineSpacing -= lineSpacingIncrement
            textView.setLineSpacing(currentLineSpacing, LINE_SPACING_MULTIPLIER)
        }
    }

    private fun increaseTextSize() {
        if (currentTextSize < maxTextSize) {
            currentTextSize += textSizeIncrement
            textView.textSize = currentTextSize
        }
    }

    private fun decreaseTextSize() {
        if (currentTextSize > minTextSize) {
            currentTextSize -= textSizeIncrement
            textView.textSize = currentTextSize
        }
    }

    private fun increaseSpace() {
        if (currentWordSpacing < maxWordSpacing) {
            currentWordSpacing += wordSpacingIncrement
            // Apply letter spacing.
            textView.letterSpacing = currentWordSpacing / 10
        }
    }

    private fun decreaseSpace() {
        if (currentWordSpacing > minWordSpacing) {
            currentWordSpacing -= wordSpacingIncrement
            textView.letterSpacing = currentWordSpacing / 10
        }
    }

    companion object {
        const val LEFT_PADDING = 50
        const val RIGHT_PADDING = 30
        const val MARGIN_BOTTOM = 3
        const val MARGIN_END = 0
    }
}