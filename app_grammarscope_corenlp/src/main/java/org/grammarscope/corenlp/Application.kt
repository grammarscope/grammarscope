/*
 * Copyright (c) 2026. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope.corenlp

import org.grammarscope.AbstractApplication

class Application : AbstractApplication() {

    /**
     * Build time
     *
     * @return build time
     */
    override fun buildTime(): String {
        return BuildConfig.BUILD_TIME
    }

    /**
     * Git hash
     *
     * @return git hasj
     */
    override fun gitHash(): String {
        return BuildConfig.GIT_HASH
    }
}
