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

public class DatabaseActivity extends Activity {

	private static final String ACTIVITY_SEARCH =
			"net.hunnor.dict.ACTIVITY_SEARCH";

	private Device device;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_database);

		ImageButton searchButton = (ImageButton)
				findViewById(R.id.database_head_button_back);
		searchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivity(ACTIVITY_SEARCH);
			}
		});

		ImageButton checkUpdateButton = (ImageButton)
				findViewById(R.id.database_button_check_for_updates);
		checkUpdateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				checkRemotes();
			}
		});

		ImageButton downloadUpdateButton = (ImageButton)
				findViewById(R.id.database_button_download_updates);
		downloadUpdateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getRemotes();
			}
		});

		checkLocals();
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
				startActivity(new Intent(ACTIVITY_SEARCH));
				return true;
			} catch (ActivityNotFoundException exception) {
				return false;
			}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void checkLocals() {
		if (device == null) {
			device = new Device();
		}

		TextView textView = (TextView) findViewById(R.id.database_text_local);
		StringBuilder result = new StringBuilder();

		result.append(getResources().getString(R.string.database_local_begin));
		result.append("...");
		textView.setText(Html.fromHtml(result.toString()));

		result = checkLocal();
		textView.setText(Html.fromHtml(result.toString()));
	}

	private StringBuilder checkLocal() {

		StringBuilder sb = new StringBuilder();

		if (!device.storage().readable()) {
			sb.append("<font color=\"red\"><b>");
			sb.append(getResources().getString(R.string.error));
			sb.append("</b></font>: ");
			sb.append(getResources().getString(R.string.database_local_storage_not_ready));
			return sb;
		}

		File indexDirectory = null;
		indexDirectory = device.storage().directory(LuceneConstants.INDEX_DIR);
		if (!indexDirectory.exists()) {
			sb.append("<font color=\"red\"><b>");
			sb.append(getResources().getString(R.string.error));
			sb.append("</b></font>: ");
			sb.append(getResources().getString(R.string.database_local_dir_missing));
			return sb;
		}

		if (!indexDirectory.canRead()) {
			sb.append("<font color=\"red\"><b>");
			sb.append(getResources().getString(R.string.error));
			sb.append("</b></font>: ");
			sb.append(getResources().getString(R.string.database_local_dir_missing));
			return sb;
		}

		String[] files = indexDirectory.list();
		if (files == null || files.length == 0) {
			sb.append("<font color=\"red\"><b>");
			sb.append(getResources().getString(R.string.error));
			sb.append("</b></font>: ");
			sb.append(getResources().getString(R.string.database_local_dir_empty));
			return sb;
		}

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
			sb.append(getResources().getString(
					R.string.database_local_index_corrupt));
		}
		return sb;
	}

	private void checkRemotes() {
		if (device == null) {
			device = new Device();
		}

		TextView textView = (TextView) findViewById(R.id.database_text_remote);
		StringBuilder result = new StringBuilder();

		result.append(getResources().getString(R.string.database_check_begin));
		result.append("... ");
		textView.setText(Html.fromHtml(result.toString()));

		checkRemote();
	}

	private void checkRemote() {
		new DatabaseCheckTask(this, findViewById(R.id.database_text_remote))
				.execute(LuceneConstants.INDEX_URL);
	}

	private void getRemotes() {
		if (device == null) {
			device = new Device();
		}

		TextView textView = (TextView) findViewById(R.id.database_text_remote);
		StringBuilder result = new StringBuilder();

		result.append(getResources().getString(R.string.database_download_begin));
		result.append("... ");
		textView.setText(Html.fromHtml(result.toString()));

		getRemote();
	}

	private void getRemote() {
		new DatabaseDownloadTask(this, findViewById(R.id.database_text_remote)) {
			@Override
			public void setMessage(
					final ProgressDialog progressDialog, final String message) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						progressDialog.setMessage(message);
					}
				});
			}
		}.execute(LuceneConstants.INDEX_URL);
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
