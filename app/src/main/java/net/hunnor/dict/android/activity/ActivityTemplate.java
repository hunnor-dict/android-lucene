package net.hunnor.dict.android.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import net.hunnor.dict.android.R;
import net.hunnor.dict.android.activity.about.AboutActivity;
import net.hunnor.dict.android.activity.database.DatabaseActivity;
import net.hunnor.dict.android.activity.main.MainActivity;
import net.hunnor.dict.android.activity.settings.SettingsActivity;
import net.hunnor.dict.android.constants.Preferences;

public abstract class ActivityTemplate extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String theme = sharedPreferences.getString(Preferences.THEME_KEY, Preferences.THEME_DAY);
        switch (theme) {
            case Preferences.THEME_DAY:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case Preferences.THEME_NIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case Preferences.THEME_POWER:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                break;
            case Preferences.THEME_SYSTEM:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    public void menuItemMain(MenuItem menuItem) {
        if (!MainActivity.class.equals(this.getClass())) {
            startActivity(new Intent(MainActivity.class.getCanonicalName()));
        }
    }

    public void menuItemDatabase(MenuItem menuItem) {
        if (!DatabaseActivity.class.equals(this.getClass())) {
            startActivity(new Intent(DatabaseActivity.class.getCanonicalName()));
        }
    }

    public void menuItemSettings(MenuItem menuItem) {
        if (!SettingsActivity.class.equals(this.getClass())) {
            startActivity(new Intent(SettingsActivity.class.getCanonicalName()));
        }
    }

    public void menuItemAbout(MenuItem menuItem) {
        if (!AboutActivity.class.equals(this.getClass())) {
            startActivity(new Intent(AboutActivity.class.getCanonicalName()));
        }
    }

}
