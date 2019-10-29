package net.hunnor.dict.android.activity.settings;

import android.os.Bundle;

import net.hunnor.dict.android.R;
import net.hunnor.dict.android.activity.ActivityTemplate;

public class SettingsActivity extends ActivityTemplate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_panel, new SettingsFragment())
                .commit();
        setContentView(R.layout.activity_settings);
    }

}
