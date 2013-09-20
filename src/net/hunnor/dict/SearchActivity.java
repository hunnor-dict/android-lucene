package net.hunnor.dict;

import java.util.ArrayList;
import java.util.List;

import net.hunnor.dict.data.Dictionary;
import net.hunnor.dict.data.Entry;
import net.hunnor.dict.util.Device;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class SearchActivity extends Activity implements View.OnClickListener {

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

	private Device device;
	private Dictionary dictionary;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		// Open Dictionary
		if (device == null) {
			device = new Device();
		}
		if (dictionary == null) {
			dictionary = new Dictionary();
		}
		if (!dictionary.open()) {
			if (!dictionary.open(
					device.storage().directory(LuceneConstants.INDEX_DIR))) {
				AlertDialog.Builder alertDialogBuilder =
						new AlertDialog.Builder(this);
				alertDialogBuilder
						.setTitle(R.string.database_alert_title)
						.setMessage(R.string.database_alert_text)
						.setPositiveButton(getResources().getString(
								R.string.database_alert_option_yes),
								new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								try {
									startActivity(new Intent("net.hunnor.dict.ACTIVITY_DATABASE"));
								} catch (ActivityNotFoundException e) {
								}
							}
						})
						.setNegativeButton(getResources().getString(
								R.string.database_alert_option_no),
								new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								finish();
							}
						});
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
			}
		}

		// Set up search UI
		Button searchButton = (Button) findViewById(R.search.search_button);
		searchButton.setOnClickListener(this);
		Button huVoiceButton = (Button) findViewById(R.search.voice_hu_button);
		huVoiceButton.setOnClickListener(this);
		Button noVoiceButton = (Button) findViewById(R.search.voice_no_button);
		noVoiceButton.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.search_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.menu.database:
			try {
				startActivity(new Intent("net.hunnor.dict.ACTIVITY_DATABASE"));
			} catch (ActivityNotFoundException e) {
				return false;
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.search.search_button:
			search();
			break;
		case R.search.voice_hu_button:
			startVoiceRecognition(LuceneConstants.LANG_HU);
			break;
		case R.search.voice_no_button:
			startVoiceRecognition(LuceneConstants.LANG_NO);
			break;
		}
	}

	private void showMessage(String message) {
		TextView tv = (TextView) findViewById(R.search.search_errors);
		tv.setText(Html.fromHtml(message));
	}

	private void search() {
		EditText editText = (EditText) findViewById(R.search.search_input_field);
		String query = editText.getText().toString();
		search(query);
	}

	private void search(String query) {
		search(query, null);
	}

	private void search(String query, String lang) {
		List<Entry> searchResults = dictionary.lookup(query, lang);
		if (searchResults == null) {
			return;
		}
		List<Spanned> results = new ArrayList<Spanned>();
		for (Entry result: searchResults) {
			results.add(Html.fromHtml(result.getText()));
		}
		Spanned[] resultArray = results.toArray(new Spanned[results.size()]);
		ArrayAdapter<Spanned> arrayAdapter = new ArrayAdapter<Spanned>(
				this, android.R.layout.simple_list_item_1, resultArray);
		StringBuilder sb = new StringBuilder();
		if (results.isEmpty()) {
			sb.append(getResources().getString(R.string.search_no_results));
		} else {
			sb.append("<b>");
			sb.append(results.size());
			sb.append("</b> ");
			if (results.size() == 1) {
				sb.append(getResources().getString(R.string.search_num_result));
			} else {
				sb.append(getResources().getString(R.string.search_num_results));
			}
		}
		showMessage(sb.toString());
		ListView listView = (ListView) findViewById(R.search.search_result_list);
		listView.setAdapter(arrayAdapter);
		EditText editText = (EditText) findViewById(R.search.search_input_field);
		editText.setText("");
	}

	private void startVoiceRecognition(String lang) {
		PackageManager packageManager = getPackageManager();
		List<ResolveInfo> activities = packageManager.queryIntentActivities(
				new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.isEmpty()) {
			showMessage(getResources().getString(R.string.voice_not_available));
		} else {
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(
					RecognizerIntent.EXTRA_CALLING_PACKAGE,
					getClass().getPackage().getName());
			intent.putExtra(
					RecognizerIntent.EXTRA_PROMPT,
					getResources().getString(R.string.voice_hint));
			intent.putExtra(
					RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			intent.putExtra(
					RecognizerIntent.EXTRA_MAX_RESULTS,
					1);
			intent.putExtra(
					RecognizerIntent.EXTRA_LANGUAGE,
					lang);
			startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE &&
				resultCode == RESULT_OK) {
			List<String> results =
					data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			search(results.get(0));
		}
	}

}
