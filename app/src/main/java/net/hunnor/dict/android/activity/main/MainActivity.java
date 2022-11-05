package net.hunnor.dict.android.activity.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
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
import net.hunnor.dict.android.constants.Preferences;
import net.hunnor.dict.android.constants.Resources;
import net.hunnor.dict.android.model.Word;
import net.hunnor.dict.android.service.HistoryService;
import net.hunnor.dict.android.service.StorageService;
import net.hunnor.dict.android.task.ExtractTask;
import net.hunnor.dict.android.task.ExtractTaskStatus;
import net.hunnor.dict.lucene.model.Entry;
import net.hunnor.dict.lucene.searcher.LuceneSearcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActivityTemplate {

    private static final String TAG = MainActivity.class.getName();

    private static final int SEARCH_MAX_ENTRIES = 25;

    private static final int SEARCH_MAX_WORDS = 50;

    private static final int HISTORY_MAX_WORDS = 10;

    private static ExtractTask extractTask;

    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViews();

        checkDictionary();
        checkQuery();

    }

    @Override
    protected void onStop() {

        super.onStop();

        if (alertDialog != null) {
            alertDialog.dismiss();
        }

    }

    private void setupViews() {

        EditText editText = findViewById(R.id.search_input);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence charSequence, int start, int before, int count) {
                showWords(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        editText.setOnEditorActionListener((TextView textView, int actionId, KeyEvent keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                saveToHistory(textView.getText().toString());
                showEntries(textView.getText().toString());
            }
            return true;
        });

        ListView listView = findViewById(R.id.search_list);

        listView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            TextView textView = (TextView) view;
            saveToHistory(textView.getText().toString());
            showEntries(textView.getText().toString());
        });

    }

    private boolean isHistoryEnabled() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean(Preferences.HISTORY_ENABLED, true);
    }

    private void saveToHistory(String query) {

        if (query == null || query.isEmpty()) {
            return;
        }

        if (!isHistoryEnabled()) {
            return;
        }

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String wordsInHistory = sharedPreferences.getString(Preferences.HISTORY_WORDS, "");

        HistoryService historyService = new HistoryService();
        String newHistoryStr = historyService.append(wordsInHistory, query, HISTORY_MAX_WORDS);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Preferences.HISTORY_WORDS, newHistoryStr);
        editor.apply();

    }

    private void showWords(String query) {

        List<Word> words;

        if (query == null || query.isEmpty()) {
            words = loadWordsFromHistory();
        } else {
            words = loadWordsFromDictionary(query);
        }

        WordArrayAdapter adapter = new WordArrayAdapter(this, words);

        ListView listView = findViewById(R.id.search_list);
        listView.setAdapter(adapter);

    }

    private List<Word> loadWordsFromHistory() {

        List<Word> words = new ArrayList<>();

        if (!isHistoryEnabled()) {
            return words;
        }

        HistoryService historyService = new HistoryService();

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String wordChain = sharedPreferences.getString(Preferences.HISTORY_WORDS, "");
        List<String> historyWords = historyService.readToList(wordChain);
        if (!historyWords.isEmpty()) {
            for (String historyWord : historyWords) {
                Word word = new Word(historyWord, Word.Source.HISTORY);
                words.add(word);
            }
        }
        return words;
    }

    private List<Word> loadWordsFromDictionary(String query) {
        List<Word> words = new ArrayList<>();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        int max = sharedPreferences.getInt(
                Preferences.SEARCH_MAX_WORDS, SEARCH_MAX_WORDS);

        LuceneSearcher luceneSearcher = LuceneSearcher.getInstance();

        if (isDictionaryNotOpen()) {
            return words;
        }

        try {
            List<String> roots = luceneSearcher.suggestions(query, max);
            for (String root : roots) {
                Word word = new Word(root, Word.Source.ROOTS);
                words.add(word);
            }
            if (roots.isEmpty() && !query.isEmpty()) {
                List<String> spellingSuggestions = luceneSearcher.spellingSuggestions(query, max);
                for (String spellingSuggestion : spellingSuggestions) {
                    Word word = new Word(spellingSuggestion, Word.Source.SPELLING);
                    words.add(word);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return words;
    }

    private void showEntries(String query) {

        if (query == null || isDictionaryNotOpen()) {
            return;
        }

        EditText editText = findViewById(R.id.search_input);
        editText.setText("");

        LuceneSearcher luceneSearcher = LuceneSearcher.getInstance();
        List<Entry> entries = new ArrayList<>();

        try {
            entries = luceneSearcher.search(query, SEARCH_MAX_ENTRIES);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        EntryArrayAdapter entryArrayAdapter = new EntryArrayAdapter(this, entries);

        ListView listView = findViewById(R.id.search_list);
        listView.setEmptyView(findViewById(R.id.search_empty));
        listView.setAdapter(entryArrayAdapter);

    }

    private void checkQuery() {

        String query = null;

        Intent intent = getIntent();

        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            query = intent.getStringExtra(Intent.EXTRA_TEXT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && Intent.ACTION_PROCESS_TEXT.equals(intent.getAction())) {
            query = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
        }

        if (query == null || query.isEmpty()) {
            showWords(query);
        } else {
            showEntries(query);
        }

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
                            baseDirectory, Resources.DICTIONARY_INDEX_DIRECTORY));
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
                            baseDirectory, Resources.DICTIONARY_SPELLING_DIRECTORY));
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

            alertDialog = builder.create();
            alertDialog.show();

            StorageService storageService = new StorageService();
            extractTask = new ExtractTask(this, storageService);
            extractTask.execute();

        }

    }

    public void extractTaskCallback(ExtractTaskStatus status) {

        if (!MainActivity.this.isFinishing()
                && alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }

        if (ExtractTaskStatus.OK.equals(status)) {
            openDictionary();
            checkQuery();
        } else {
            displayError(status);
        }

    }

    protected void displayError(ExtractTaskStatus status) {

        String message = status.toString();

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
            case E_DEPLOY_ZIP_EXTRACT:
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
