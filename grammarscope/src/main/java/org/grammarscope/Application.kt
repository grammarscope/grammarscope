package org.grammarscope

import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Process
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.bbou.deploy.coroutines.Deploy.deploy
import com.bbou.deploy.coroutines.Deploy.emptyDirectory
import com.bbou.download.preference.Settings
import com.bbou.download.preference.Settings.getCachePref
import com.bbou.download.preference.Settings.getDatapackDir
import com.bbou.download.preference.Settings.setCachePref
import com.bbou.download.preference.Settings.setDatapackDir
import com.bbou.download.preference.Settings.setRepoPref
import org.depparse.Broadcast
import org.depparse.Storage.getAppStorage
import org.depparse.common.Colors
import org.grammarscope.common.BuildConfig
import org.grammarscope.common.R
import org.grammarscope.graph.DependencySettings
import org.grammarscope.graph.GraphColors
import org.grammarscope.graph.SemanticSettings
import kotlin.system.exitProcess
import androidx.core.content.edit
import org.depparse.common.AppValues
import org.grammarscope.annotations.AnnotationsSettings
import org.grammarscope.annotations.paint.Palette

class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Application $this")
        if (BuildConfig.DEBUG) {
            StrictMode.setVmPolicy(VmPolicy.Builder().detectLeakedClosableObjects().penaltyLog().build())
            //StrictMode.enableDefaults();
        }
        initialize()
        ContextCompat.registerReceiver(this, ProviderManager, IntentFilter(Broadcast.BROADCAST_ACTION), ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onTerminate() {
        super.onTerminate()
        unregisterReceiver(ProviderManager)
    }

    // I N I T I A L I Z A T I O N

    /**
     * Initialize
     */
    private fun initialize() {
        /*
		// permissions
		Permissions.check(this);
		*/

        // version of this code
        var verCode = -1
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            @Suppress("DEPRECATION")
            verCode = pInfo.versionCode
        } catch (_: PackageManager.NameNotFoundException) {
            // ignore
        }

        // initialize prefs (mainly for download mode)
        val deviceSharedPref = getSharedPreferences(Settings.PREFERENCES_DEVICE, MODE_PRIVATE)
        val savedVerCode = deviceSharedPref.getInt(PREF_INITIALIZED_APP, -1)
        if (savedVerCode == -1 || verCode == -1 || savedVerCode < verCode) {
            // clear on update
            clearSettings(this)

            // minimal
            initSettings(this)

            // flag as initialized
            deviceSharedPref.edit { putInt(PREF_INITIALIZED_APP, verCode) }
        }

        // app values
        AppValues.posKey = resources.getString(R.string.poskey)
        AppValues.xposKey = resources.getString(R.string.xposkey)

        // theming
        Log.d(TAG, "Night mode: " + getNightModeString(this))
        setAllColorsFromResources(this)
        setAllColorsFromPreferences(this)

        // deploy
        val dir = getAppStorage(this)
        try {
            deploy(dir, "English") { path: String ->
                val assetManager = assets
                assetManager.open(path)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Deploy failed", e)
            Toast.makeText(this, getString(R.string.error_deploy) + e.message, Toast.LENGTH_LONG).show()
            emptyDirectory(dir)
            Toast.makeText(this, R.string.status_clear_data, Toast.LENGTH_LONG).show()
            Process.killProcess(Process.myPid())
            exitProcess(1)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val newContext = wrapContext(this, newConfig)
        Log.d(TAG, "onConfigurationChanged: " + getNightModeString(this) + " -> " + getNightModeString(newContext))
        setAllColorsFromResources(newContext)
        setAllColorsFromPreferences(newContext)
    }

    private fun setAllColorsFromResources(context: Context) {
        Colors.setColorsFromResources(context)
        GraphColors.setColorsFromResources(context)
        Palette.setColorsFromResources(context)
    }

    private fun setAllColorsFromPreferences(context: Context) {
        ColorSettings.setColorsFromPreferences(context)
        Palette.setColorsFromPreferences(context)
     }

    companion object {

        private const val TAG = "App"
        private const val PREF_INITIALIZED_APP = "pref_initialized_app"

        fun clearSettings(context: Context) {
            context.getSharedPreferences(Settings.PREFERENCES_DEVICE, MODE_PRIVATE).edit { clear() }
            context.getSharedPreferences(Settings.PREFERENCES_DATAPACK, MODE_PRIVATE).edit { clear() }
            PreferenceManager.getDefaultSharedPreferences(context).edit { clear() }
            ColorSettings(context, true).reset()
            ColorSettings(context, false).reset()
            DependencySettings(context, true).reset()
            DependencySettings(context, false).reset()
            SemanticSettings(context, true).reset()
            SemanticSettings(context, false).reset()
            AnnotationsSettings(context, true).reset()
            AnnotationsSettings(context, false).reset()
        }

        fun initSettings(context: Context) {
            // app storage
            val dir = getAppStorage(context)

            // model dir
            var dest = getDatapackDir(context)
            if (dest == null) {
                dest = dir.absolutePath
                setDatapackDir(context, dest)
            }

            // cache
            var cache = getCachePref(context)
            if (cache == null) {
                cache = context.cacheDir.absolutePath
                setCachePref(context, cache)
            }

            // download repo
            val defaultSource = context.getString(R.string.default_download_repo)
            setRepoPref(context, defaultSource)
        }

        private fun wrapContext(context: Context, newConfig: Configuration): Context {
            //Context themedContext = new ContextThemeWrapper(context, R.style.MyTheme);
            //return themedContext;

            //Configuration newConfig = context.getResources().getConfiguration();
            //newConfig.uiMode &= ~Configuration.UI_MODE_NIGHT_MASK; // clear
            //newConfig.uiMode |= toConfigurationUiMode(mode) & Configuration.UI_MODE_NIGHT_MASK; // set
            val newContext = context.createConfigurationContext(newConfig)
            return ContextThemeWrapper(newContext, R.style.MyTheme)
        }

        fun createOverrideConfigurationForDayNight(context: Context, mode: Int): Configuration {
            val newNightMode = when (mode) {
                AppCompatDelegate.MODE_NIGHT_YES -> Configuration.UI_MODE_NIGHT_YES
                AppCompatDelegate.MODE_NIGHT_NO -> Configuration.UI_MODE_NIGHT_NO
                else -> {
                    // If we're following the system, we just use the system default from the application context
                    val appConfig = context.applicationContext.resources.configuration
                    appConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
                }
            }

            // If we're here then we can try and apply an override configuration on the Context.
            val overrideConf = Configuration()
            overrideConf.fontScale = 0f
            overrideConf.uiMode = newNightMode or (overrideConf.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv())
            return overrideConf
        }

        /**
         * Test whether in night mode.
         *
         * @param context context
         * @return true if in night mode, false otherwise
         */
        fun isNightMode(context: Context): Boolean {
            val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return when (nightModeFlags) {
                Configuration.UI_MODE_NIGHT_YES -> true
                Configuration.UI_MODE_NIGHT_NO -> false
                else -> false
            }
        }

        /**
         * Get night mode.
         *
         * @param context context
         * @return mode to string
         */
        fun getNightModeString(context: Context): String {
            val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return when (nightModeFlags) {
                Configuration.UI_MODE_NIGHT_YES -> "night"
                Configuration.UI_MODE_NIGHT_NO -> "day"
                else -> "unknown"
            }
        }
    }
}
