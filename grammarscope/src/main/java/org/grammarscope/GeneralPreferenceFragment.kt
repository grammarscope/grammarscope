package org.grammarscope

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.bbou.download.preference.Settings.getDatapackDir
import com.bbou.download.preference.Settings.setDatapackDir
import org.depparse.Storage.getAppStorage
import org.depparse.common.AppContext
import org.grammarscope.common.R

/**
 * This fragment shows general preferences only.
 */
class GeneralPreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_general)
        val prefModelDir = findPreference<ResettablePreference>(PREF_MODEL_DIR)!!
        prefModelDir.setSummary(getDatapackDir(prefModelDir.context))
        prefModelDir.setClickListener {
            val newValue = getAppStorage(AppContext.context).absolutePath
            setDatapackDir(AppContext.context, newValue)
            prefModelDir.setSummary(newValue)
        }
    }

    companion object {

        private const val PREF_MODEL_DIR = "pref_model_dir"
    }
}