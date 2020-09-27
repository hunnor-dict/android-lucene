package net.hunnor.dict.android.activity.settings

import android.os.Bundle
import net.hunnor.dict.android.R
import net.hunnor.dict.android.activity.ActivityTemplate

class SettingsActivity : ActivityTemplate() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_panel, SettingsFragment())
                .commit()
        setContentView(R.layout.activity_settings)
    }

}
