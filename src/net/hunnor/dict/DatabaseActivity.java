package net.hunnor.dict;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

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

		// Check external storage state
		StringBuilder stringBuilder = new StringBuilder();
		FileManager fileManager = new FileManager();
		stringBuilder.append(getResources().getString(R.string.database_storage_check)).append("... ");
		if (fileManager.storageWriteable()) {
			stringBuilder.append("<font color=\"green\"><b>").append(getResources().getString(R.string.ok)).append("</b></font> (").append(getResources().getString(R.string.database_read_write)).append(")");
		} else if (fileManager.storageReadable()) {
			stringBuilder.append("<font color=\"yellow\"><b>").append(getResources().getString(R.string.ok)).append("</b></font> (").append(getResources().getString(R.string.database_read_only)).append(")");
		} else {
			stringBuilder.append("<font color=\"red\"><b>").append(getResources().getString(R.string.error)).append("</b></font> (").append(getResources().getString(R.string.database_not_ready)).append(")");
			check = false;
		}

		// Check application directory
		if (check) {
			stringBuilder.append("<br>");
			stringBuilder.append(getResources().getString(R.string.database_directory_check)).append("... ");
			File appDirectory = new File(fileManager.getAppDirectory());
			if (appDirectory.exists()) {
				if (appDirectory.canRead()) {
					if (appDirectory.canWrite()) {
						stringBuilder.append("<font color=\"green\"><b>").append(getResources().getString(R.string.ok)).append("</b></font> (").append(getResources().getString(R.string.database_read_write)).append(")");
					} else {
						stringBuilder.append("<font color=\"yellow\"><b>").append(getResources().getString(R.string.ok)).append("</b></font> (").append(getResources().getString(R.string.database_read_only)).append(")");
					}
				} else {
					stringBuilder.append("<font color=\"red\"><b>").append(getResources().getString(R.string.error)).append("</b></font> (").append(getResources().getString(R.string.database_not_ready)).append(")");
					check = false;
				}
			} else {
				if (appDirectory.mkdirs()) {
					stringBuilder.append("<font color=\"green\"><b>").append(getResources().getString(R.string.ok)).append("</b></font> (").append(getResources().getString(R.string.database_directory_created)).append(")");
				} else {
					stringBuilder.append("<font color=\"red\"><b>").append(getResources().getString(R.string.error)).append("</b></font> (").append(getResources().getString(R.string.database_directory_create_failed)).append(")");
					check = false;
				}
			}
		}

		// Check index directory
		File indexDirectory = null;
		if (check) {
			stringBuilder.append("<br>");
			stringBuilder.append(getResources().getString(R.string.database_index_directory_check)).append("... ");
			indexDirectory = new File(fileManager.getAppDirectory() + File.separator + LuceneConstants.INDEX_DIR);
			if (indexDirectory.exists()) {
				if (indexDirectory.canRead()) {
					if (indexDirectory.canWrite()) {
						stringBuilder.append("<font color=\"green\"><b>").append(getResources().getString(R.string.ok)).append("</b></font> (").append(getResources().getString(R.string.database_read_write)).append(")");
					} else {
						stringBuilder.append("<font color=\"yellow\"><b>").append(getResources().getString(R.string.ok)).append("</b></font> (").append(getResources().getString(R.string.database_read_only)).append(")");
					}
				} else {
					stringBuilder.append("<font color=\"red\"><b>").append(getResources().getString(R.string.error)).append("</b></font> (").append(getResources().getString(R.string.database_not_ready)).append(")");
					check = false;
				}
			} else {
				if (indexDirectory.mkdirs()) {
					stringBuilder.append("<font color=\"green\"><b>").append(getResources().getString(R.string.ok)).append("</b></font> (").append(getResources().getString(R.string.database_directory_created)).append(")");
				} else {
					stringBuilder.append("<font color=\"red\"><b>").append(getResources().getString(R.string.error)).append("</b></font> (").append(getResources().getString(R.string.database_directory_create_failed)).append(")");
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
				stringBuilder.append(getResources().getString(R.string.database_index_check)).append("... ");
				try {
					StringBuilder listBuilder = new StringBuilder();
					Directory directory;
					directory = new NIOFSDirectory(indexDirectory);
					@SuppressWarnings("deprecation")
					long lastMod = IndexReader.lastModified(directory);
					Date date = new Date(lastMod);
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd");
					listBuilder.append("<br>").append(getResources().getString(R.string.database_last_modified)).append(": ").append(simpleDateFormat.format(date));
					stringBuilder.append(listBuilder);
				} catch (IOException e) {
					stringBuilder.append("<font color=\"red\"><b>").append(getResources().getString(R.string.error)).append("</b></font> (").append(getResources().getString(R.string.database_index_corrupt)).append(")");
				}
			}
		}

		TextView textView = (TextView) findViewById(R.database.databaseInfo);
		textView.setText(Html.fromHtml(stringBuilder.toString()));
	}

	private void checkRemote() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(getResources().getString(R.string.database_update_check)).append("... ");
		try {
			URL url = new URL(LuceneConstants.INDEX_URL);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("HEAD");
			stringBuilder.append("<br>").append(getResources().getString(R.string.database_last_modified)).append(": ").append(connection.getHeaderField("Last-Modified"));
			stringBuilder.append("<br>").append(getResources().getString(R.string.database_size)).append(": ").append(connection.getHeaderField("Content-Length"));
		} catch (MalformedURLException exception) {
			stringBuilder.append("<font color=\"red\"><b>").append(getResources().getString(R.string.error)).append("</b></font> (").append(getResources().getString(R.string.database_update_malformed_url)).append(")");
		} catch (ProtocolException exception) {
			stringBuilder.append("<font color=\"red\"><b>").append(getResources().getString(R.string.error)).append("</b></font> (").append(getResources().getString(R.string.database_update_protocol_exception)).append(")");
		} catch (IOException exception) {
			stringBuilder.append("<font color=\"red\"><b>").append(getResources().getString(R.string.error)).append("</b></font> (").append(getResources().getString(R.string.database_update_io_exception)).append(")");
		}
		
		TextView textView = (TextView) findViewById(R.database.update_status);
		textView.setText(Html.fromHtml(stringBuilder.toString()));
	}

	private void getRemote() {

		boolean check = true;

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(getResources().getString(R.string.database_download_begin)).append("... ");
		FileManager fileManager = new FileManager();
		if (fileManager.downloadFile(LuceneConstants.INDEX_URL, fileManager.getAppDirectory() + File.separator + LuceneConstants.INDEX_ZIP)) {
			stringBuilder.append("<font color=\"green\"><b>").append(getResources().getString(R.string.ok)).append("</b></font>");
		} else {
			stringBuilder.append("<font color=\"red\"><b>").append(getResources().getString(R.string.error)).append("</b></font> (").append(getResources().getString(R.string.database_download_fail)).append(")");
			check = false;
		}

		if (check) {
			stringBuilder.append("<br>");
			stringBuilder.append(getResources().getString(R.string.database_extract_begin)).append("... ");
			if (fileManager.unZip(fileManager.getAppDirectory() + File.separator + LuceneConstants.INDEX_ZIP, fileManager.getAppDirectory())) {
				stringBuilder.append("<font color=\"green\"><b>").append(getResources().getString(R.string.ok)).append("</b></font>");
			} else {
				stringBuilder.append("<font color=\"red\"><b>").append(getResources().getString(R.string.error)).append("</b></font> (").append(getResources().getString(R.string.database_extract_fail)).append(")");
				check = false;
			}
		}

		if (check) {
			stringBuilder.append("<br>");
			stringBuilder.append(getResources().getString(R.string.database_install_begin)).append("... ");
			if (fileManager.deleteDirectory(fileManager.getAppDirectory() + File.separator + LuceneConstants.INDEX_DIR)) {
				File f = new File(fileManager.getAppDirectory() + File.separator + "hunnor-lucene-index");
				if (f.renameTo(new File(fileManager.getAppDirectory() + File.separator + LuceneConstants.INDEX_DIR))) {
					stringBuilder.append("<font color=\"green\"><b>").append(getResources().getString(R.string.ok)).append("</b></font>");
				} else {
					stringBuilder.append("<font color=\"red\"><b>").append(getResources().getString(R.string.error)).append("</b></font> (").append(getResources().getString(R.string.database_install_rename_fail)).append(")");
				}
			} else {
				stringBuilder.append("<font color=\"red\"><b>").append(getResources().getString(R.string.error)).append("</b></font> (").append(getResources().getString(R.string.database_install_delete_fail)).append(")");
			}
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
