package net.hunnor.dict;

import java.util.ArrayList;
import java.util.List;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		Button button = (Button) findViewById(R.search.search_button);
		button.setOnClickListener(this);
		Dictionary dictionaryProvider = new Dictionary();
		if (!dictionaryProvider.ready()) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder
					.setTitle(R.string.database_alert_title)
					.setMessage(R.string.database_alert_text)
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							try {
								startActivity(new Intent("net.hunnor.dict.ACTIVITY_DATABASE"));
							} catch (ActivityNotFoundException e) {
							}		
						}
					})
					.setNegativeButton("No", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}
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
	public void onClick(View view) {
		switch (view.getId()) {
		case R.search.search_button:
			search();
			break;
		case R.search.voice_hu_button:
			startVoiceRecognition("hu");
			break;
		case R.search.voice_no_button:
			startVoiceRecognition("no");
			break;
		}
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

	private void searchError(String s) {
		TextView tv = (TextView) findViewById(R.search.search_errors);
		tv.setText(s);
	}

	private void search() {
		EditText editText = (EditText) findViewById(R.search.searchInputField);
		String query = editText.getText().toString();
		searchFor(query);
	}

	private void searchFor(String query) {
		Dictionary dictionaryProvider = new Dictionary();
		List<IndexObject> searchResults = dictionaryProvider.search(query);
		if (searchResults == null) {
			return;
		}
		List<Spanned> results = new ArrayList<Spanned>();
		for (IndexObject result: searchResults) {
			results.add(Html.fromHtml(result.getText()));
		}
		Spanned[] resultArray = results.toArray(new Spanned[results.size()]);
		ArrayAdapter<Spanned> arrayAdapter = new ArrayAdapter<Spanned>(this, android.R.layout.simple_list_item_1, resultArray);
		ListView listView = (ListView) findViewById(R.search.searchResults);
		listView.setAdapter(arrayAdapter);
		clearSearchField();
	}

	private void startVoiceRecognition(String lang) {
		PackageManager packageManager = getPackageManager();
		List<ResolveInfo> activities = packageManager.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.isEmpty()) {
			searchError("No recognizer");
		} else {
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
			intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getResources().getString(R.string.voice_hint));
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang);
			startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
		}
	}

	private void clearSearchField() {
		EditText editText = (EditText) findViewById(R.search.searchInputField);
		editText.setText("");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
			List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			searchFor(results.get(0));
		}
	}

}
