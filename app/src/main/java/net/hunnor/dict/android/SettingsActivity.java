package net.hunnor.dict.android;

import android.os.Bundle;

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
