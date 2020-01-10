package net.hunnor.dict.android.activity.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import net.hunnor.dict.android.R;
import net.hunnor.dict.android.activity.ActivityTemplate;
import net.hunnor.dict.android.activity.details.DetailsActivity;
import net.hunnor.dict.android.util.Storage;
import net.hunnor.dict.lucene.searcher.LuceneSearcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActivityTemplate {

    private static final String TAG = MainActivity.class.getName();

    private static final String DICTIONARY_INDEX_DIRECTORY = "hunnor-lucene-index";

    private static final String DICTIONARY_SPELLING_DIRECTORY = "hunnor-lucene-spelling";

    private static final int SEARCH_MAX_SUGGESTIONS = 50;

    private static ExtractTask extractTask;

    private AlertDialog alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setListeners();
        checkDictionary();

    }

    protected void onStop() {

        super.onStop();
        if (alert != null) {
            alert.dismiss();
        }

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
                showSuggestions(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        editText.setOnEditorActionListener((TextView textView, int actionId, KeyEvent keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                showDetails(textView.getText().toString());
            }
            return true;
        });

        ListView listView = findViewById(R.id.search_results);

        listView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            TextView textView = (TextView) view;
            showDetails(textView.getText().toString());
        });

    }

    private void showSuggestions(String query) {

        if (query == null) {
            return;
        }

        LuceneSearcher luceneSearcher = LuceneSearcher.getInstance();

        if (!luceneSearcher.isOpen() || !luceneSearcher.isSpellCheckerOpen()) {
            return;
        }

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        int max = sharedPreferences.getInt(
                "searchMaxSuggestions", SEARCH_MAX_SUGGESTIONS);

        List<String> suggestionList = new ArrayList<>();
        boolean suggestions = false;

        try {
            suggestionList = luceneSearcher.suggestions(query, max);
            if (suggestionList.isEmpty() && !query.isEmpty()) {
                suggestionList = luceneSearcher.spellingSuggestions(query, max);
                suggestions = true;
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        MainArrayAdapter adapter = new MainArrayAdapter(this, suggestionList);
        adapter.setSuggestions(suggestions);

        ListView listView = findViewById(R.id.search_results);
        listView.setAdapter(adapter);

    }

    private void showDetails(String query) {
        Intent intent = new Intent(DetailsActivity.class.getCanonicalName());
        intent.putExtra("query", query);
        startActivity(intent);
    }

    private void checkDictionary() {

        if (isDictionaryNotOpen()) {
            openDictionary();
        }

        if (isDictionaryNotOpen()) {
            startDictionaryDeploy();
        }

    }

    private boolean isDictionaryNotOpen() {
        LuceneSearcher luceneSearcher = LuceneSearcher.getInstance();
        return !luceneSearcher.isOpen() || !luceneSearcher.isSpellCheckerOpen();
    }

    private void openDictionary() {

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

    }

    private void startDictionaryDeploy() {

        if (extractTask == null || !AsyncTask.Status.RUNNING.equals(extractTask.getStatus())) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.search_alert_deploy).setCancelable(false);

            alert = builder.create();
            alert.show();

            extractTask = new ExtractTask(this);
            extractTask.execute();

        }

    }

    public void deployFinished(Storage.Status status) {

        if (!MainActivity.this.isFinishing()
                && alert != null && alert.isShowing()) {
            alert.dismiss();
        }

        if (Storage.Status.OK.equals(status)) {
            openDictionary();
        } else {
            displayError(status);
        }

    }

    protected void displayError(Storage.Status status) {

        String message = "";

        switch (status) {
            case E_DEPLOY_DELETE_DEPLOY_INDEX_DIR:
                message = getString(R.string.database_status_e_deploy_delete_deploy_index_dir);
                break;
            case E_DEPLOY_DELETE_DEPLOY_SPELLING_DIR:
                message = getString(R.string.database_status_e_deploy_delete_deploy_spelling_dir);
                break;
            case E_DEPLOY_DELETE_INDEX_DIR:
                message = getString(R.string.database_status_e_deploy_delete_index_dir);
                break;
            case E_DEPLOY_DELETE_SPELLING_DIR:
                message = getString(R.string.database_status_e_deploy_delete_spelling_dir);
                break;
            case E_DEPLOY_RENAME_INDEX_DIR:
                message = getString(R.string.database_status_e_deploy_rename_index_dir);
                break;
            case E_DEPLOY_RENAME_SPELLING_DIR:
                message = getString(R.string.database_status_e_deploy_rename_spelling_dir);
                break;
            case E_DEPLOY_ZIP_ENTRY_DIR_CREATE:
                message = getString(R.string.database_status_e_deploy_zip_entry_dir_create);
                break;
            case E_EXCEPTION_IO:
                message = getString(R.string.database_status_e_exception_io);
                break;
            default:
                break;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.alert_ok, (DialogInterface dialog, int id) ->
                        dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

}
