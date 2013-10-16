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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class SearchActivity extends Activity {

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

	private static final String ACTIVITY_DATABASE =
			"net.hunnor.dict.ACTIVITY_DATABASE";

	private Device device;
	private Dictionary dictionary;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

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
									startActivity(new Intent(ACTIVITY_DATABASE));
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

		ImageButton searchButton = (ImageButton) findViewById(R.id.search_button);
		searchButton.setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View view) {
						search();
					}
				});
		ImageButton huVoiceButton = (ImageButton) findViewById(R.id.voice_hu_button);
		huVoiceButton.setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View view) {
						startVoiceRecognition(LuceneConstants.LANG_HU);
					}
				});
		ImageButton noVoiceButton = (ImageButton) findViewById(R.id.voice_no_button);
		noVoiceButton.setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View view) {
						startVoiceRecognition(LuceneConstants.LANG_NO);
					}
				});

		AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView)
				findViewById(R.id.autocomplete);
		autoCompleteTextView.setOnEditorActionListener(
				new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					search();
					return true;
				}
				return false;
			}
		});
		autoCompleteTextView.setAdapter(
				new SuggestionArrayAdapter(this, R.layout.autocomplete_item));
		autoCompleteTextView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(
					AdapterView<?> adapterView,
					View view,
					int position, long id) {
				Spanned spanned = (Spanned)
						adapterView.getItemAtPosition(position);
				String string = spanned.toString();
				search(string);
			}
		});
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
				startActivity(new Intent(ACTIVITY_DATABASE));
			} catch (ActivityNotFoundException e) {
				return false;
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(
			int requestCode, int resultCode, Intent intent) {
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE &&
				resultCode == RESULT_OK) {
			List<String> results =
					intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			search(results.get(0));
		}
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

	private void search() {
		EditText editText = (EditText) findViewById(R.id.autocomplete);
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
		Entry[] resultArray =
				searchResults.toArray(new Entry[searchResults.size()]);
		ArrayAdapter<Entry> arrayAdapter = new SearchArrayAdapter(
				this, R.layout.search_result, resultArray);
		StringBuilder sb = new StringBuilder();
		if (results.isEmpty()) {
			sb.append(getResources().getString(R.string.search_no_results));
		} else {
			sb.append(results.size());
			sb.append(" ");
			if (results.size() == 1) {
				sb.append(getResources().getString(R.string.search_num_result));
			} else {
				sb.append(getResources().getString(R.string.search_num_results));
			}
		}
		showMessage(sb.toString());
		ListView listView = (ListView) findViewById(R.id.search_result_list);
		listView.setAdapter(arrayAdapter);
		EditText editText = (EditText) findViewById(R.id.autocomplete);
		editText.setText("");
	}

	private void showMessage(String message) {
		TextView textView = (TextView) findViewById(R.id.search_errors);
		textView.setText(Html.fromHtml(message));
	}

}
