package net.hunnor.dict.android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceFragmentCompat;

import net.hunnor.dict.android.constants.Preferences;

public class SettingsFragment extends PreferenceFragmentCompat {

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.settings, rootKey);

        preferenceChangeListener = (sharedPreferences, key) -> {
            if (Preferences.THEME_KEY.equals(key)) {
                String theme = sharedPreferences.getString(Preferences.THEME_KEY, Preferences.THEME_DAY);
                if (Preferences.THEME_DAY.equals(theme)) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else if (Preferences.THEME_NIGHT.equals(theme)) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
            }
            Activity activity = getActivity();
            if (activity != null) {
                activity.recreate();
            }
        };

    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

}
