/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>.
 */
package com.bbou.textrecog

import com.google.mlkit.vision.text.Text

internal object TextExtractor {

    fun extract(visionText: Text): CharSequence? {
        val sb = StringBuilder()
        val blocks = visionText.textBlocks
        if (blocks.isEmpty()) {
            return null
        }
        var str: String
        for (b in blocks.indices) {
            var inWord = false
            val lines = blocks[b].lines
            for (l in lines.indices) {
                val elements = lines[l].elements
                for (e in elements.indices) {
                    val element = elements[e]

                    // space
                    if (!inWord && sb.isNotEmpty() && sb[sb.length - 1] != '\n') {
                        sb.append(' ')
                    }

                    // what to append
                    str = element.text
                    str = str.trim { it <= ' ' }
                    val hyphenated = str.endsWith("-")
                    inWord = hyphenated
                    if (hyphenated) {
                        str = str.substring(0, str.length - 1)
                    }
                    sb.append(str)
                    // end of element
                }
                // end of line
                // sb.append(' ')
            }
            // end of block
            /*
			if (str != null && !str.endsWith("."))
			{
				sb.append('.')
				str = null
			}
			sb.append('\n')
			*/
        }
        return sb
    }
}
