package net.hunnor.dict.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import net.hunnor.dict.lucene.searcher.LuceneSearcher;

import java.io.File;
import java.io.IOException;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

public class MainActivity extends ActivityTemplate {

    private static final String TAG = MainActivity.class.getName();

    private static final String DICTIONARY_INDEX_DIRECTORY = "hunnor-lucene-index";

    private static final String DICTIONARY_SPELLING_DIRECTORY = "hunnor-lucene-spelling";

    private static final int SEARCH_MAX_SUGGESTIONS = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setListeners();
        checkDictionary();

    }

    private void setListeners() {

        EditText editText = findViewById(R.id.search_input);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence charSequence, int start, int before, int count) {
                doSearch(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        editText.setOnEditorActionListener((TextView textView, int actionId, KeyEvent keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                submitSearch(textView.getText().toString());
            }
            return true;
        });

        ListView listView = findViewById(R.id.search_results);

        listView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            TextView textView = (TextView) view;
            submitSearch(textView.getText().toString());
        });

    }

    private void doSearch(String query) {

        LuceneSearcher luceneSearcher = LuceneSearcher.getInstance();

        if (luceneSearcher.isOpen() && luceneSearcher.isSpellCheckerOpen()) {

            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(this);
            int max = sharedPreferences.getInt(
                    "searchMaxSuggestions", SEARCH_MAX_SUGGESTIONS);

            List<String> suggestionList = luceneSearcher.suggestions(query, max);
            boolean suggestions = false;

            if (suggestionList.isEmpty() && query != null && !query.isEmpty()) {
                suggestionList = luceneSearcher.spellingSuggestions(query, max);
                suggestions = true;
            }

            MainArrayAdapter adapter = new MainArrayAdapter(this, suggestionList);
            adapter.setSuggestions(suggestions);

            ListView listView = findViewById(R.id.search_results);
            listView.setAdapter(adapter);

        }

    }

    private void submitSearch(String query) {
        Intent intent = new Intent(DetailsActivity.class.getCanonicalName());
        intent.putExtra("query", query);
        startActivity(intent);
    }

    private void checkDictionary() {

        LuceneSearcher luceneSearcher = LuceneSearcher.getInstance();

        if (!luceneSearcher.isOpen()) {
            File baseDirectory = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (baseDirectory != null) {
                try {
                    luceneSearcher.open(new File(
                            baseDirectory, DICTIONARY_INDEX_DIRECTORY));
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }

        if (!luceneSearcher.isSpellCheckerOpen()) {
            File baseDirectory = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (baseDirectory != null) {
                try {
                    luceneSearcher.openSpellChecker(new File(
                            baseDirectory, DICTIONARY_SPELLING_DIRECTORY));
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }

        if (!luceneSearcher.isOpen() || !luceneSearcher.isSpellCheckerOpen()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.search_alert_deploy).setCancelable(false)
                    .setPositiveButton(R.string.alert_ok, (DialogInterface dialog, int id) -> {
                        Intent intent = new Intent(DatabaseActivity.class.getCanonicalName());
                        intent.putExtra("deploy", true);
                        startActivity(intent);
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

    }

}
