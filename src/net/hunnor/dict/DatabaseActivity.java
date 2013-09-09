package net.hunnor.dict;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

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
		FileManager.State storageState = fileManager.getStorageState();
		stringBuilder.append("Storage: ");
		if (FileManager.State.STORAGE_MOUNTED.equals(storageState)) {
			stringBuilder.append("<font color=\"green\">");
			stringBuilder.append("Mounted");
			stringBuilder.append("</font>");
		} else if (FileManager.State.STORAGE_MOUNTED_READ_ONLY.equals(storageState)) {
			stringBuilder.append("<font color=\"yellow\">");
			stringBuilder.append("Mounted read-only");
			stringBuilder.append("</font>");
		} else {
			stringBuilder.append("<font color=\"red\">");
			stringBuilder.append("Not ready");
			stringBuilder.append("</font>");
			return;
		}
		stringBuilder.append("<br>");
		stringBuilder.append("AppDir: ");
		File appDirectory = new File(fileManager.getAppDirectory());
		if (appDirectory.exists()) {
			if (appDirectory.canRead()) {
				if (appDirectory.canWrite()) {
					stringBuilder.append("<font color=\"green\">");
					stringBuilder.append("Read/Write");
					stringBuilder.append("</font>");
					// TODO Check API level:
					// android.os.Build.VERSION.SDK_INT
					// stringBuilder.append(" " + appDirectory.getFreeSpace() + " free");
				} else {
					stringBuilder.append("<font color=\"yellow\">");
					stringBuilder.append("Read only");
					stringBuilder.append("</font>");
				}
			} else {
				stringBuilder.append("<font color=\"red\">");
				stringBuilder.append("Can't read");
				stringBuilder.append("</font>");
				return;
			}
		} else {
			stringBuilder.append(" <font color=\"red\">");
			stringBuilder.append("Doesn't exist");
			stringBuilder.append("</font>");
			if (appDirectory.mkdirs()) {
				stringBuilder.append(" Created");
			} else {
				stringBuilder.append(" <font color=\"red\">");
				stringBuilder.append("Failed to create");
				stringBuilder.append("</font>");
			}
		}
		stringBuilder.append("<br>");

		stringBuilder.append("IndexDir: ");
		File indexDirectory = new File(fileManager.getAppDirectory() + File.separator + LuceneConstants.INDEX_DIR);
		if (indexDirectory.exists()) {
			if (indexDirectory.canRead()) {
				if (indexDirectory.canWrite()) {
					stringBuilder.append("<font color=\"green\">");
					stringBuilder.append("Read/Write");
					stringBuilder.append("</font>");
				} else {
					stringBuilder.append(("<font color=\"yellow\">"));
					stringBuilder.append("Read only");
					stringBuilder.append("</font>");
				}
			} else {
				stringBuilder.append(("<font color=\"red\">"));
				stringBuilder.append("Can't read");
				stringBuilder.append("</font>");
				return;
			}
		} else {
			if (indexDirectory.mkdir()) {
				stringBuilder.append(" <font color=\"green\">");
				stringBuilder.append("Created");
				stringBuilder.append("</font>");				
			} else {
				stringBuilder.append(" <font color=\"red\">");
				stringBuilder.append("Failed to create");
				stringBuilder.append("</font>");
			}
		}
		stringBuilder.append("<br>");

		stringBuilder.append("Index info: ");
		// Try to use as Lucene index
		Directory directory;
		try {
			directory = new NIOFSDirectory(indexDirectory);
			IndexReader indexReader = IndexReader.open(directory);
			long lastMod = indexReader.lastModified(directory);
			Date date = new Date(lastMod);
			stringBuilder.append("<br> LastMod: " + date);
		} catch (IOException e) {
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
