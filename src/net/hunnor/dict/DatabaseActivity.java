package net.hunnor.dict;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class DatabaseActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_database);
		StringBuilder stringBuilder = new StringBuilder();
		FileManager fileManager = new FileManager();
		FileManager.StorageState storageState = fileManager.getStorageState();
		if (FileManager.StorageState.STORAGE_MOUNTED.equals(storageState)) {
			stringBuilder.append("<font color=\"green\">");
			stringBuilder.append("Mounted");
			stringBuilder.append("</font>");
		} else {
			stringBuilder.append("Yay!");
		}
		TextView textView = (TextView) findViewById(R.database.databaseInfo);
		textView.setText(Html.fromHtml(stringBuilder.toString()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.database_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.menu.search:
			launchActivity("net.hunnor.dict.ACTIVITY_SEARCH");
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
