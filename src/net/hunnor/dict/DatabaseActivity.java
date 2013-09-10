package net.hunnor.dict;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
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
import android.widget.TextView;

public class DatabaseActivity extends Activity {

	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_database);

		// TODO Set up layout elements

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
					listBuilder.append("<br>  ").append(getResources().getString(R.string.database_index_last_modified)).append(": ").append(simpleDateFormat.format(date));
					stringBuilder.append(listBuilder);
				} catch (IOException e) {
					stringBuilder.append("<font color=\"red\"><b>").append(getResources().getString(R.string.error)).append("</b></font> (").append(getResources().getString(R.string.database_index_corrupt)).append(")");
				}
			}
		}

		TextView textView = (TextView) findViewById(R.database.databaseInfo);
		textView.setText(Html.fromHtml(stringBuilder.toString()));
	}

/*
		stringBuilder.append("<b>Remote data:</b>");
		try {
			URL url = new URL("http://dict.hunnor.net/port/lucene/hunnor-lucene-3.6.zip");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("HEAD");
			stringBuilder.append("Size: ").append(connection.getHeaderField("Content-Length")).append("<br>");
			stringBuilder.append("Date: ").append(connection.getHeaderField("Last-Modified")).append("<br>");
		} catch (Exception e) {
			stringBuilder.append("Net error.");
		}
		
		stringBuilder.append("<b>Download:</b>");
		boolean state = fileManager.downloadFile(LuceneConstants.INDEX_URL, fileManager.getAppDirectory() + File.separator + LuceneConstants.INDEX_DIR + File.separator + LuceneConstants.INDEX_ZIP);
		stringBuilder.append(state).append("<br>");

		stringBuilder.append("<b>Extract:</b>");
		boolean unzipState = fileManager.unZip(
				fileManager.getAppDirectory() + File.separator + LuceneConstants.INDEX_DIR + File.separator + LuceneConstants.INDEX_ZIP,
				fileManager.getAppDirectory());
		if (unzipState) {
			stringBuilder.append("Unzipped<br>");
		} else {
			stringBuilder.append("Failed to unzip<br>");
		}

		stringBuilder.append("<b>Listing:</b>");
		try {
			File file = new File(fileManager.getAppDirectory() + File.separator + LuceneConstants.INDEX_DIR);
			stringBuilder.append("For: ").append(file.getAbsolutePath() + ":<br>");
			//for (String f: file.list()) {
				//stringBuilder.append(f).append("<br>");
			//}
		} catch (Exception exception) {
			stringBuilder.append("Error.<br>");
		}
		fileManager.deleteDirectory(fileManager.getAppDirectory() + File.separator + LuceneConstants.INDEX_DIR);
		File f = new File(fileManager.getAppDirectory() + File.separator + "hunnor-lucene-index");
		f.renameTo(new File(fileManager.getAppDirectory() + File.separator + LuceneConstants.INDEX_DIR));
		stringBuilder.append("<br>Done.");
*/

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
