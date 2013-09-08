package net.hunnor.dict;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class SearchActivity extends Activity implements View.OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		Button button = (Button) findViewById(R.search.searchButton);
		button.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.search_menu, menu);
		return true;
	}

	@Override
	public void onClick(View view) {
		// Get query String
		EditText editText = (EditText) findViewById(R.search.searchInputField);
		String query = editText.getText().toString();

		// Get search results
		DictionaryProvider dictionaryProvider = new DictionaryProvider();
		List<IndexObject> searchResults = dictionaryProvider.search(query);
		if (searchResults == null) {
			return;
		}

		// Get results as Array
		List<Spanned> results = new ArrayList<Spanned>();
		for (IndexObject result: searchResults) {
			results.add(Html.fromHtml(result.getText()));
		}
		Spanned[] resultArray = results.toArray(new Spanned[results.size()]);

		// Adapter
		ArrayAdapter<Spanned> arrayAdapter = new ArrayAdapter<Spanned>(this, android.R.layout.simple_list_item_1, resultArray);
		ListView listView = (ListView) findViewById(R.search.searchResults);
		listView.setAdapter(arrayAdapter);
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.menu.database:
			launchActivity("net.hunnor.dict.ACTIVITY_DATABASE");
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
