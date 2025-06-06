package org.depparse.common

import android.graphics.Typeface
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import org.depparse.common.BaseSpanner.SpanFactory

/**
 * Span factories
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
object Factories {

    private val boldFactory = SpanFactory { arrayOf<Any>(StyleSpan(Typeface.BOLD)) }
    val textFactory = boldFactory
    val rootLabelFactory = SpanFactory {
        if (Colors.rootBackColor and -0x1000000 == 0) arrayOf<Any>(ForegroundColorSpan(Colors.rootColor), StyleSpan(Typeface.BOLD)) else arrayOf<Any>(
            BackgroundColorSpan(Colors.rootBackColor),
            ForegroundColorSpan(Colors.rootColor),
            StyleSpan(Typeface.BOLD)
        )
    }
    val labelFactory = SpanFactory {
        if (Colors.labelBackColor and -0x1000000 == 0) arrayOf<Any>(ForegroundColorSpan(Colors.labelColor), StyleSpan(Typeface.BOLD)) else arrayOf<Any>(
            BackgroundColorSpan(Colors.labelBackColor),
            ForegroundColorSpan(Colors.labelColor),
            StyleSpan(Typeface.BOLD)
        )
    }
    val enhancedLabelFactory = SpanFactory {
        if (Colors.enhancedLabelBackColor and -0x1000000 == 0) arrayOf<Any>(ForegroundColorSpan(Colors.enhancedLabelBackColor), StyleSpan(Typeface.BOLD)) else arrayOf<Any>(
            BackgroundColorSpan(Colors.enhancedLabelBackColor),
            ForegroundColorSpan(Colors.enhancedLabelColor),
            StyleSpan(Typeface.BOLD)
        )
    }
    val headFactory = SpanFactory {
        if (Colors.headBackColor and -0x1000000 == 0) arrayOf<Any>(ForegroundColorSpan(Colors.headColor), StyleSpan(Typeface.BOLD_ITALIC)) else arrayOf<Any>(
            BackgroundColorSpan(Colors.headBackColor),
            ForegroundColorSpan(Colors.headColor),
            StyleSpan(Typeface.BOLD_ITALIC)
        )
    }
    val dependentFactory = SpanFactory {
        if (Colors.dependentBackColor and -0x1000000 == 0) arrayOf<Any>(ForegroundColorSpan(Colors.dependentColor), StyleSpan(Typeface.NORMAL)) else arrayOf<Any>(
            BackgroundColorSpan(Colors.dependentBackColor), ForegroundColorSpan(
                Colors.dependentColor
            ), StyleSpan(Typeface.NORMAL)
        )
    }
    val predicateFactory = SpanFactory {
        if (Colors.predicateBackColor and -0x1000000 == 0) arrayOf<Any>(ForegroundColorSpan(Colors.predicateColor), StyleSpan(Typeface.BOLD)) else arrayOf<Any>(
            BackgroundColorSpan(Colors.predicateBackColor), ForegroundColorSpan(
                Colors.predicateColor
            ), StyleSpan(Typeface.BOLD)
        )
    }
    val subjectFactory = SpanFactory {
        if (Colors.subjectBackColor and -0x1000000 == 0) arrayOf<Any>(ForegroundColorSpan(Colors.subjectColor), StyleSpan(Typeface.BOLD)) else arrayOf<Any>(
            BackgroundColorSpan(Colors.subjectBackColor), ForegroundColorSpan(
                Colors.subjectColor
            ), StyleSpan(Typeface.BOLD)
        )
    }
    val objectFactory = SpanFactory {
        if (Colors.objectBackColor and -0x1000000 == 0) arrayOf<Any>(ForegroundColorSpan(Colors.objectColor), StyleSpan(Typeface.BOLD)) else arrayOf<Any>(
            BackgroundColorSpan(Colors.objectBackColor),
            ForegroundColorSpan(Colors.objectColor),
            StyleSpan(Typeface.BOLD)
        )
    }
    val termFactory = SpanFactory {
        if (Colors.termModifierPredicateBackColor and -0x1000000 == 0) arrayOf<Any>(ForegroundColorSpan(Colors.termModifierPredicateColor), StyleSpan(Typeface.BOLD)) else arrayOf<Any>(
            BackgroundColorSpan(Colors.termModifierPredicateBackColor), ForegroundColorSpan(
                Colors.termModifierPredicateColor
            ), StyleSpan(Typeface.BOLD)
        )
    }
    val predicate2Factory = SpanFactory {
        if (Colors.predicateModifierPredicateBackColor and -0x1000000 == 0) arrayOf<Any>(ForegroundColorSpan(Colors.predicateModifierPredicateColor), StyleSpan(Typeface.BOLD)) else arrayOf<Any>(
            BackgroundColorSpan(Colors.predicateModifierPredicateBackColor), ForegroundColorSpan(
                Colors.predicateModifierPredicateColor
            ), StyleSpan(Typeface.BOLD)
        )
    }
}
