package net.hunnor.dict;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.view.MenuItem;

public class DatabaseActivity extends Activity {

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.menu.search:
			launchActivity("net.hunnor.dict.lucene.ACTIVITY_SEARCH");
			return true;
		case R.menu.database:
			launchActivity("net.hunnor.dict.lucene.ACTIVITY_DATABASE");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected void launchActivity(String activityName) {
		try {
			startActivity(new Intent(activityName));
		} catch (ActivityNotFoundException e) {
		}		
	}

}
