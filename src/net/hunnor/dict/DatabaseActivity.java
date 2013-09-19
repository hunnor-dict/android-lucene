package net.hunnor.dict;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.hunnor.dict.task.CheckUpdate;
import net.hunnor.dict.task.GetUpdate;
import net.hunnor.dict.util.Device;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DatabaseActivity extends Activity implements View.OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_database);

		Button button1 = (Button) findViewById(R.database.update_download_button);
		button1.setOnClickListener(this);
		Button button2 = (Button) findViewById(R.database.update_check_button);
		button2.setOnClickListener(this);

		checkLocals();
	}

	@SuppressLint("SimpleDateFormat")
	private void checkLocals() {

		boolean check = true;

		StringBuilder stringBuilder = new StringBuilder();
		Device device = new Device();
		stringBuilder.append(getResources().getString(R.string.database_check_external_storage)).append("... ");
		if (device.storage().writable()) {
			stringBuilder.append("<font color=\"green\"><b>").append(getResources().getString(R.string.ok)).append("</b></font> (").append(getResources().getString(R.string.io_read_write)).append(")");
		} else if (device.storage().readable()) {
			stringBuilder.append("<font color=\"yellow\"><b>").append(getResources().getString(R.string.ok)).append("</b></font> (").append(getResources().getString(R.string.io_read_only)).append(")");
		} else {
			stringBuilder.append("<font color=\"red\"><b>").append(getResources().getString(R.string.error)).append("</b></font> (").append(getResources().getString(R.string.io_not_ready)).append(")");
			check = false;
		}

		if (check) {
			stringBuilder.append("<br>");
			stringBuilder.append(getResources().getString(R.string.database_check_app_directory)).append("... ");
			File appDirectory = device.storage().directory("");
			if (appDirectory.exists()) {
				if (appDirectory.canRead()) {
					if (appDirectory.canWrite()) {
						stringBuilder.append("<font color=\"green\"><b>").append(getResources().getString(R.string.ok)).append("</b></font> (").append(getResources().getString(R.string.io_read_write)).append(")");
					} else {
						stringBuilder.append("<font color=\"yellow\"><b>").append(getResources().getString(R.string.ok)).append("</b></font> (").append(getResources().getString(R.string.io_read_only)).append(")");
					}
				} else {
					stringBuilder.append("<font color=\"red\"><b>").append(getResources().getString(R.string.error)).append("</b></font> (").append(getResources().getString(R.string.io_not_ready)).append(")");
					check = false;
				}
			} else {
				if (appDirectory.mkdirs()) {
					stringBuilder.append("<font color=\"green\"><b>").append(getResources().getString(R.string.ok)).append("</b></font> (").append(getResources().getString(R.string.io_create_ok)).append(")");
				} else {
					stringBuilder.append("<font color=\"red\"><b>").append(getResources().getString(R.string.error)).append("</b></font> (").append(getResources().getString(R.string.io_create_error)).append(")");
					check = false;
				}
			}
		}

		File indexDirectory = null;
		if (check) {
			stringBuilder.append("<br>");
			stringBuilder.append(getResources().getString(R.string.database_check_index_directory)).append("... ");
			indexDirectory = device.storage().directory(LuceneConstants.INDEX_DIR);
			if (indexDirectory.exists()) {
				if (indexDirectory.canRead()) {
					if (indexDirectory.canWrite()) {
						stringBuilder.append("<font color=\"green\"><b>").append(getResources().getString(R.string.ok)).append("</b></font> (").append(getResources().getString(R.string.io_read_write)).append(")");
					} else {
						stringBuilder.append("<font color=\"yellow\"><b>").append(getResources().getString(R.string.ok)).append("</b></font> (").append(getResources().getString(R.string.io_read_only)).append(")");
					}
				} else {
					stringBuilder.append("<font color=\"red\"><b>").append(getResources().getString(R.string.error)).append("</b></font> (").append(getResources().getString(R.string.io_not_ready)).append(")");
					check = false;
				}
			} else {
				if (indexDirectory.mkdirs()) {
					stringBuilder.append("<font color=\"green\"><b>").append(getResources().getString(R.string.ok)).append("</b></font> (").append(getResources().getString(R.string.io_create_ok)).append(")");
				} else {
					stringBuilder.append("<font color=\"red\"><b>").append(getResources().getString(R.string.error)).append("</b></font> (").append(getResources().getString(R.string.io_create_error)).append(")");
					check = false;
				}
			}
		}

		if (check) {
			stringBuilder.append("<br>");
			String[] files = indexDirectory.list();
			if (files.length == 0) {
				stringBuilder.append(getResources().getString(R.string.database_index_directory_empty));
			} else {
				stringBuilder.append(getResources().getString(R.string.database_check_index_integrity)).append("... ");
				try {
					StringBuilder listBuilder = new StringBuilder();
					Directory directory;
					directory = new NIOFSDirectory(indexDirectory);
					@SuppressWarnings("deprecation")
					long lastMod = IndexReader.lastModified(directory);
					Date date = new Date(lastMod);
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
					listBuilder.append("<br>").append(getResources().getString(R.string.last_modified)).append(": ").append(simpleDateFormat.format(date));
					stringBuilder.append(listBuilder);
				} catch (IOException e) {
					stringBuilder.append("<font color=\"red\"><b>").append(getResources().getString(R.string.error)).append("</b></font> (").append(getResources().getString(R.string.index_corrupt)).append(")");
				}
			}
		}

		TextView textView = (TextView) findViewById(R.database.databaseInfo);
		textView.setText(Html.fromHtml(stringBuilder.toString()));
	}

	private void checkRemote() {

		boolean check = true;

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(getResources().getString(R.string.database_check_internet_connection)).append("... ");
		Device device = new Device();
		if (device.network().online(this)) {
			stringBuilder.append("<font color=\"green\"><b>").append(getResources().getString(R.string.ok)).append("</b></font> (").append(getResources().getString(R.string.net_online)).append(")");			
		} else {
			stringBuilder.append("<font color=\"red\"><b>").append(getResources().getString(R.string.error)).append("</b></font> (").append(getResources().getString(R.string.net_offline)).append(")");
			check = false;
		}

		if (check) {
			stringBuilder.append("<br>");
			stringBuilder.append(getResources().getString(R.string.database_check_update_url)).append("... ");
			new CheckUpdate() {
				@Override
				public void onPostExecute(String result) {
					StringBuilder stringBuilder = new StringBuilder();
					stringBuilder.append(result);
					TextView textView = (TextView) findViewById(R.database.update_status);
					textView.append(Html.fromHtml(stringBuilder.toString()));
				}
			}.execute(LuceneConstants.INDEX_URL);
		}

		TextView textView = (TextView) findViewById(R.database.update_status);
		textView.setText(Html.fromHtml(stringBuilder.toString()));
	}

	private void getRemote() {

		boolean check = true;

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(getResources().getString(R.string.database_check_internet_connection)).append("... ");
		Device fileManager = new Device();
		if (fileManager.network().online(this)) {
			stringBuilder.append("<font color=\"green\"><b>").append(getResources().getString(R.string.ok)).append("</b></font> (").append(getResources().getString(R.string.net_online)).append(")");
		} else {
			stringBuilder.append("<font color=\"red\"><b>").append(getResources().getString(R.string.error)).append("</b></font> (").append(getResources().getString(R.string.net_offline)).append(")");
			check = false;
		}

		if (check) {
			stringBuilder.append("<br>");
			stringBuilder.append(getResources().getString(R.string.database_download)).append("... ");
			new GetUpdate() {
				@Override
				public void onPostExecute(String result) {
					StringBuilder stringBuilder = new StringBuilder();
					if ("OK".equals(result)) {
						stringBuilder.append("<font color=\"green\"><b>").append(getResources().getString(R.string.ok)).append("</b></font>");
					} else {
						stringBuilder.append("<font color=\"red\"><b>").append(getResources().getString(R.string.error)).append("</b></font>");
					}
					TextView textView = (TextView) findViewById(R.database.update_status);
					textView.append(Html.fromHtml(stringBuilder.toString()));
				}
			}.execute(LuceneConstants.INDEX_URL);
		}

		TextView textView = (TextView) findViewById(R.database.update_status);
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
			try {
				startActivity(new Intent("net.hunnor.dict.ACTIVITY_SEARCH"));
				return true;
			} catch (ActivityNotFoundException exception) {
				return false;
			}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.database.update_check_button:
			checkRemote();
			break;
		case R.database.update_download_button:
			getRemote();
			break;
		}
	}

}
