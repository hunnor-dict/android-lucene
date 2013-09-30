package net.hunnor.dict;

import java.io.File;
import java.io.IOException;

import net.hunnor.dict.util.Device;
import net.hunnor.dict.util.Formatter;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class DatabaseActivity extends Activity implements View.OnClickListener {

	private Device device;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_database);

		ImageButton searchButton = (ImageButton)
				findViewById(R.id.database_head_button_back);
		searchButton.setOnClickListener(this);

		ImageButton checkUpdateButton = (ImageButton)
				findViewById(R.id.database_button_check_for_updates);
		checkUpdateButton.setOnClickListener(this);

		ImageButton downloadUpdateButton = (ImageButton)
				findViewById(R.id.database_button_download_updates);
		downloadUpdateButton.setOnClickListener(this);

		checkLocals();
	}

	private void checkLocals() {
		if (device == null) {
			device = new Device();
		}

		StringBuilder result = new StringBuilder();
		result.append(getResources().getString(R.string.database_local_title));
		result.append(": ");
		result.append(checkLocal());

		TextView textView = (TextView) findViewById(R.id.database_text_local);
		textView.setText(Html.fromHtml(result.toString()));
	}

	private StringBuilder checkLocal() {

		StringBuilder sb = new StringBuilder();

		if (device.storage().readable()) {
			File indexDirectory = null;
			indexDirectory = device.storage().directory(LuceneConstants.INDEX_DIR);
			if (indexDirectory.exists() && indexDirectory.canRead()) {
				String[] files = indexDirectory.list();
				if (files.length > 0) {
					try {
						Directory directory;
						directory = new NIOFSDirectory(indexDirectory);
						@SuppressWarnings("deprecation")
						long lastMod = IndexReader.lastModified(directory);
						sb.append(getResources().getString(R.string.last_modified));
						sb.append(": ").append(Formatter.date(lastMod));
					} catch (IOException exception) {
						sb.append("<font color=\"red\"><b>");
						sb.append(getResources().getString(R.string.error));
						sb.append("</b></font>: ");
						sb.append(getResources().getString(R.string.index_corrupt));
					}
				} else {
					sb.append("<font color=\"red\"><b>");
					sb.append(getResources().getString(R.string.error));
					sb.append("</b></font>: ");
					sb.append(getResources().getString(R.string.database_local_index_directory_empty));
				}
			} else {
				sb.append("<font color=\"red\"><b>");
				sb.append(getResources().getString(R.string.error));
				sb.append("</b></font>: ");
				sb.append(getResources().getString(R.string.io_not_ready));
			}
		} else {
			sb.append("<font color=\"red\"><b>");
			sb.append(getResources().getString(R.string.error));
			sb.append("</b></font>: ");
			sb.append(getResources().getString(R.string.io_not_ready));
		}
		return sb;
	}

	private void checkRemotes() {
		if (device == null) {
			device = new Device();
		}
		
		StringBuilder result = new StringBuilder();
		result.append(getResources().getString(R.string.database_check_title));
		result.append("... ");
		result.append(checkRemote());

		TextView textView = (TextView) findViewById(R.id.database_text_remote);
		textView.setText(Html.fromHtml(result.toString()));
	}

	private StringBuilder checkRemote() {
		StringBuilder sb = new StringBuilder();
		if (!device.network().online(this)) {
			sb.append("<font color=\"red\"><b>");
			sb.append(getResources().getString(R.string.error));
			sb.append("</b></font>");
			sb.append("<br><small>");
			sb.append(getResources().getString(R.string.net_offline));
			sb.append("</small>");
			return sb;
		}

		new DatabaseCheckTask(this, findViewById(R.id.database_text_remote))
				.execute(LuceneConstants.INDEX_URL);

		return sb;
	}

	private void getRemotes() {
		if (device == null) {
			device = new Device();
		}

		StringBuilder result = new StringBuilder();
		result.append(getResources().getString(R.string.database_download_title));
		result.append("... ");
		result.append(getRemote());

		TextView textView = (TextView) findViewById(R.id.database_text_remote);
		textView.setText(Html.fromHtml(result.toString()));
	}

	private StringBuilder getRemote() {
		StringBuilder sb = new StringBuilder();
		if (!device.network().online(this)) {
			sb.append("<font color=\"red\"><b>");
			sb.append(getResources().getString(R.string.error));
			sb.append("</b></font>");
			sb.append("<br><small>");
			sb.append(getResources().getString(R.string.net_offline));
			sb.append("</small>");
			return sb;
		}

		new DatabaseDownloadTask(this, findViewById(R.id.database_text_remote)) {
			@Override
			public void setMessage(final ProgressDialog progressDialog, final String message) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						progressDialog.setMessage(message);
					}
				});
			}
		}.execute(LuceneConstants.INDEX_URL);

		return sb;
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
		case R.id.database_head_button_back:
			startActivity("net.hunnor.dict.ACTIVITY_SEARCH");
			break;
		case R.id.database_button_check_for_updates:
			checkRemotes();
			break;
		case R.id.database_button_download_updates:
			getRemotes();
			break;
		}
	}

	private boolean startActivity(String activity) {
		try {
			Intent intent = new Intent(activity);
			startActivity(intent);
		} catch (ActivityNotFoundException exception) {
			return false;
		}
		return true;
	}

}
