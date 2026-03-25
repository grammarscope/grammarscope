/*
 * Copyright (c) 2026. Bernard Bou <1313ou@gmail.com>.
 */

package org.depparse.common

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    /**
     * onCreate
     *
     * @param savedInstanceState saved instance state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // edge to edge
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT) //(DefaultLightScrim, DefaultDarkScrim)
        )
    }
}