package org.grammarscope

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import org.depparse.Storage.getAppStorage
import org.depparse.common.BaseSpanner.SpanFactory
import org.depparse.common.BaseSpanner.append
import org.depparse.common.Colors.getColorAttrs
import org.depparse.common.ModelInfo.Companion.read
import org.grammarscope.common.R
import java.io.File
import java.util.Locale
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import kotlin.math.ln
import kotlin.math.pow

object Status {

    private fun format(text: CharSequence, vararg factories: SpanFactory): SpannableStringBuilder {
        val sb = SpannableStringBuilder()
        sb.append(text, *factories)
        return sb
    }

    private fun humanReadableByteCount(bytes: Long, @Suppress("SameParameterValue") si: Boolean): String {
        val unit = if (si) 1000 else 1024
        if (bytes < unit) {
            return "$bytes B"
        }
        val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1].toString() + if (si) "" else "i"
        return String.format(Locale.ENGLISH, "%.1f %sB", bytes / unit.toDouble().pow(exp.toDouble()), pre)
    }

    private fun sizeOf(file: File): CharSequence {
        val bytes = file.length()
        return humanReadableByteCount(bytes, false)
    }

    fun modelStatus(activity: Activity) {
        val dir = getAppStorage(activity)
        val info = read(activity)
        val alert = AlertDialog.Builder(activity) // unguarded, level 1
        alert.setTitle(R.string.model)
        if (info == null) {
            alert.setIcon(R.drawable.ic_error)
            alert.setMessage(R.string.model_none)
        } else {
            val colors = getColorAttrs(activity, R.style.MyTheme, intArrayOf(R.attr.colorAccent, R.attr.colorOnPrimary, R.attr.colorOnSecondary))
            val langFactory = SpanFactory { arrayOf<Any>(ForegroundColorSpan(colors[0]), StyleSpan(Typeface.BOLD)) }
            val nameFactory = SpanFactory { arrayOf<Any>(ForegroundColorSpan(colors[0]), StyleSpan(Typeface.BOLD), StyleSpan(Typeface.ITALIC)) }
            val moreFactory = SpanFactory { arrayOf<Any>(ForegroundColorSpan(colors[0]), StyleSpan(Typeface.ITALIC)) }
            val coreFileFactory = SpanFactory { arrayOf<Any>(ForegroundColorSpan(colors[1]), StyleSpan(Typeface.BOLD)) }
            val fileFactory = SpanFactory { arrayOf<Any>(ForegroundColorSpan(colors[2]), StyleSpan(Typeface.ITALIC)) }
            val items: MutableList<CharSequence> = ArrayList()
            items.add(format(info.lang, langFactory))
            if (info.lang != info.name)
                items.add(format(info.name, nameFactory))
            if (info.more1 != null)
                items.add(format(info.more1!!, moreFactory))
            if (info.more2 != null)
                items.add(format(info.more2!!, moreFactory))
            val coreModelFiles = activity.resources.getStringArray(R.array.core_model_files)
            for (modelFile in expandRegEx(dir, coreModelFiles)) {
                val file = File(modelFile)
                items.add(format(file.name, coreFileFactory)
                    .append(" (")
                    .append(sizeOf(file))
                    .append(')'))
            }
            val modelFiles = activity.resources.getStringArray(R.array.model_files)
            for (modelFile in expandRegEx(dir, modelFiles)) {
                val file = File(modelFile)
                items.add(format(file.name, fileFactory))
            }
            alert.setItems(items.toTypedArray<CharSequence>(), null)
            alert.setNegativeButton(R.string.action_cancel) { d: DialogInterface, _: Int -> d.cancel() }
        }
        alert.show()
    }

    private fun expandRegEx(dir: File, array: Array<String>): List<String> {
        return array
            .flatMap {
                findFilesMatching(it, dir)
            }
            .toList()
    }

    /**
     * Attempts to find and list files recursively based on a regex pattern
     * that might implicitly define a starting path relative to a set of predefined root directories.
     *
     * The regex pattern should be crafted such that its beginning can match a known root
     * or a significant portion of a path.
     *
     * @param complexRegexPattern A regex where the initial part might match a directory structure.
     * @param root A File object representing starting point for the search.
     * @return A list of absolute paths of matching files.
     */
    private fun findFilesMatching(complexRegexPattern: String, root: File): List<String> {
        val compiledPattern: Pattern = try {
            Pattern.compile(complexRegexPattern)
        } catch (e: PatternSyntaxException) {
            Log.e(TAG, "Invalid regex pattern: $complexRegexPattern - ${e.message}")
            return emptyList()
        }

        val allMatchingFiles = mutableSetOf<String>() // Use Set to avoid duplicates if roots overlap

        if (!root.exists() || !root.isDirectory) {
            return emptyList()
        }

        // This is the tricky part: we need to see if the root itself or any of its children could be a starting point for the full regex.
        // For simplicity, this example will start the recursive search from each root and let the full regex match against the absolute paths.
        // A more sophisticated approach would try to "anchor" the regex.
        val filesToProcess = ArrayDeque<File>()
        filesToProcess.add(root)

        while (filesToProcess.isNotEmpty()) {
            val currentFile = filesToProcess.removeFirst()

            if (currentFile.isDirectory) {
                // Check if the directory path itself matches the regex (useful for patterns like "/usr/local/.*")
                // This allows the regex to match directories as well, if the pattern is designed for it.
                // However, we are primarily interested in files.
                // if (compiledPattern.matcher(currentFile.absolutePath).matches()) {
                //     // It's a directory that matches, decide if you want to add it or its contents
                // }
                val children = currentFile.listFiles()
                if (children != null) {
                    for (child in children) {
                        filesToProcess.add(child)
                    }
                }
            } else {
                // It's a file, check if its ABSOLUTE PATH matches the regex
                if (compiledPattern.matcher(currentFile.absolutePath).matches()) {
                    allMatchingFiles.add(currentFile.absolutePath)
                }
            }
        }
        return allMatchingFiles.toList()
    }

    private const val TAG = "Status"
}
