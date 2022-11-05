package net.hunnor.dict.android.activity.settings

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import net.hunnor.dict.android.R
import net.hunnor.dict.android.constants.Preferences

class SettingsFragment : PreferenceFragmentCompat() {

    private var preferenceChangeListener: OnSharedPreferenceChangeListener? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        setPreferencesFromResource(R.xml.settings, rootKey)

        preferenceChangeListener = OnSharedPreferenceChangeListener { sharedPreferences: SharedPreferences, key: String ->
            if (Preferences.THEME_KEY == key) {
                when (sharedPreferences.getString(Preferences.THEME_KEY, Preferences.THEME_DAY)) {
                    Preferences.THEME_DAY -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    Preferences.THEME_NIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    Preferences.THEME_POWER -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                    Preferences.THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences
                ?.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences
                ?.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

}
