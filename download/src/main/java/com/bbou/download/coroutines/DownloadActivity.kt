/*
 * Copyright (c) 2023. Bernard Bou
 */
package com.bbou.download.coroutines

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import com.bbou.download.CompletionListener
import com.bbou.download.Keys
import com.bbou.download.common.R
import com.bbou.download.preference.Settings

/**
 * Download activity
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
class DownloadActivity : AppCompatActivity(), CompletionListener {

    /**
     * onCreate
     *
     * @param savedInstanceState saved instance state
     */
    @SuppressLint("CommitTransaction") // BUG
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // download mode to downloader
        val overriddenMode = intent.getStringExtra(Keys.DOWNLOAD_MODE_ARG)
        val mode = (if (overriddenMode == null) Settings.Mode.getModePref(this) else Settings.Mode.valueOf(overriddenMode))!!
        val downloader = mode.toDownloader()

        // content
        setContentView(R.layout.activity_download)

        // toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // set up the action bar
        val actionBar = supportActionBar!!
        actionBar.setDisplayShowTitleEnabled(true)

        // fragment
        if (savedInstanceState == null) {

            val dummy = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_dummy", false)

            // fragment
            val downloadFragment = if (dummy) DummyFragment() else toFragment(downloader)

            // pass arguments over to fragment
            val args = intent.extras
            downloadFragment.arguments = args
            if (args != null) {
                val broadcastAction = args.getString(Keys.BROADCAST_ACTION)
                val broadcastRequestKey = args.getString(Keys.BROADCAST_REQUEST_KEY)
                if (!broadcastAction.isNullOrEmpty() && !broadcastRequestKey.isNullOrEmpty()) {
                    val broadcastKillRequestValue = args.getString(Keys.BROADCAST_KILL_REQUEST_VALUE)
                    if (!broadcastKillRequestValue.isNullOrEmpty()) {
                        downloadFragment.requestKill = Runnable { broadcastRequest(this, broadcastAction, broadcastRequestKey, broadcastKillRequestValue) }
                    }
                    val broadcastNewRequestValue = args.getString(Keys.BROADCAST_NEW_REQUEST_VALUE)
                    if (!broadcastNewRequestValue.isNullOrEmpty()) {
                        downloadFragment.requestNew = Runnable { broadcastRequest(this, broadcastAction, broadcastRequestKey, broadcastNewRequestValue) }
                    }
                }
            }
            supportFragmentManager
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.container, downloadFragment)
                .commit()
        }
    }

    /**
     * On complete callback from fragment
     */
    override fun onComplete(success: Boolean) {
        Log.d(TAG, "OnComplete succeeded=$success $this")

        // finish activity
        if (success) {
            finish()
        }
    }

    companion object {

        private const val TAG = "DownloadA"

        /**
         * Broadcast request
         *
         * @param context               context
         * @param broadcastAction       broadcast action
         * @param broadcastRequestKey   broadcast request arg key
         * @param broadcastRequestValue broadcast request arg value
         */
        private fun broadcastRequest(context: Context, broadcastAction: String, broadcastRequestKey: String, broadcastRequestValue: String) {
            Log.d(TAG, "Send broadcast request $broadcastRequestValue")
            val intent = Intent()
            intent.setPackage(context.packageName)
            intent.action = broadcastAction
            intent.putExtra(broadcastRequestKey, broadcastRequestValue)
            context.sendBroadcast(intent)
        }
    }
}
