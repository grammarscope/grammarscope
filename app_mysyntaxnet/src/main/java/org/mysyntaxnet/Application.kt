/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.mysyntaxnet

import android.app.Application
import org.depparse.common.AppContext

class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        AppContext.init(this)
    }
}