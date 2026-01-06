/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.depparse.common

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import org.depparse.common.AppMode.isNightMode

abstract class BaseSettingsActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    /**
     * Make initial fragment
     *
     * @return initial fragment
     */
    protected abstract fun makeFragment(): Fragment

    @SuppressLint("CommitTransaction") // BUG
    override fun onCreate(savedInstanceState: Bundle?) {
        // super
        super.onCreate(savedInstanceState)

        // statusbar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val isNightMode = isNightMode(this)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = !isNightMode

        // content view
        setContentView(R.layout.activity_settings)

        // fragment
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, makeFragment())
                .commit()
            setTitle(R.string.title_settings)
        } else {
            title = savedInstanceState.getCharSequence(TITLE_TAG)
        }
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                setTitle(R.string.title_settings)
            } else {
                var title: CharSequence? = null
                val fragments = supportFragmentManager.fragments
                if (fragments.isNotEmpty()) {
                    val fragment = fragments[0] // only one at a time
                    val preferenceFragment = fragment as PreferenceFragmentCompat
                    title = preferenceFragment.preferenceScreen.title
                }
                if (title.isNullOrEmpty()) {
                    setTitle(R.string.title_settings)
                } else {
                    setTitle(title)
                }
            }
            val actionBar = supportActionBar
            if (actionBar != null) {
                actionBar.subtitle = title
            }
        }

        // toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // set up the action bar
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setTitle(R.string.app_name)
            actionBar.subtitle = title
            actionBar.displayOptions = ActionBar.DISPLAY_USE_LOGO or ActionBar.DISPLAY_SHOW_TITLE or ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_HOME_AS_UP
        }
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, title)
    }

    // M E N U

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.action_clear_settings) {
            resetSettings()
            restart()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // U P

    override fun onSupportNavigateUp(): Boolean {
        val fm = supportFragmentManager
        return if (fm.popBackStackImmediate()) {
            true
        } else super.onSupportNavigateUp()
    }

    // F A C T O R Y

    @SuppressLint("CommitTransaction") // BUG
    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        // Instantiate the new Fragment
        val manager = supportFragmentManager
        val args = pref.getExtras()
        val fragmentName = pref.fragment!!
        val fragment = manager.fragmentFactory.instantiate(classLoader, fragmentName)
        fragment.arguments = args

        // Replace the existing Fragment forActivity the new Fragment
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, fragment)
            .addToBackStack(null)
            .commit()
        return true
    }

    // U T I L S

    /**
     * Reset settings
     */
    protected open fun resetSettings() {
        PreferenceManager.getDefaultSharedPreferences(this).edit { clear() }
    }

    /**
     * Restart app
     */
    private fun restart() {
        val restartIntent = packageManager.getLaunchIntentForPackage(packageName)!!
        restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(restartIntent)
    }

    companion object {

        private const val TITLE_TAG = "settingsActivityTitle"
    }
}