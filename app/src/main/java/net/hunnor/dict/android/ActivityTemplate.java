package net.hunnor.dict.android;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

public abstract class ActivityTemplate extends AppCompatActivity {

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
