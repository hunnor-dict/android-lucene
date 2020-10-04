package net.hunnor.dict.android.activity.settings

import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceManager
import net.hunnor.dict.android.R
import net.hunnor.dict.android.activity.ActivityTemplate
import net.hunnor.dict.android.constants.Preferences

class SettingsActivity : ActivityTemplate() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_panel, SettingsFragment())
                .commit()
        setContentView(R.layout.activity_settings)
    }

    fun doHistoryDelete(view: View) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPreferences.edit()
        editor.putString(Preferences.HISTORY_WORDS, "")
        editor.apply()
    }

}
