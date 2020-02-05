package net.hunnor.dict.android.activity.details;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import net.hunnor.dict.android.R;
import net.hunnor.dict.lucene.model.Entry;
import net.hunnor.dict.lucene.searcher.LuceneSearcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DetailsActivity extends AppCompatActivity {

    private static final String TAG = DetailsActivity.class.getName();

    private static final String DICTIONARY_INDEX_DIRECTORY = "hunnor-lucene-index";

    private static final int SEARCH_MAX_RESULTS = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        showDetails();

    }

    private void showDetails() {

        String query = getQuery();

        if (!isDictionaryOpen()) {
            openDictionary();
        }

        if (isDictionaryOpen()) {
            doSearch(query);
        }

    }

    private String getQuery() {

        Intent intent = getIntent();

        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            return intent.getStringExtra(Intent.EXTRA_TEXT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && Intent.ACTION_PROCESS_TEXT.equals(intent.getAction())) {
            return intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
        } else {
            return intent.getStringExtra("query");
        }

    }

    private boolean isDictionaryOpen() {
        LuceneSearcher luceneSearcher = LuceneSearcher.getInstance();
        return luceneSearcher.isOpen();
    }

    private void openDictionary() {

        LuceneSearcher luceneSearcher = LuceneSearcher.getInstance();

        if (!luceneSearcher.isOpen()) {
            File baseDirectory = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (baseDirectory != null) {
                try {
                    luceneSearcher.open(new File(baseDirectory, DICTIONARY_INDEX_DIRECTORY));
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }

    }

    private void doSearch(String query) {

        if (query != null) {

            LuceneSearcher luceneSearcher = LuceneSearcher.getInstance();

            List<Entry> entries = new ArrayList<>();
            try {
                entries = luceneSearcher.search(query, SEARCH_MAX_RESULTS);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            DetailsArrayAdapter detailsArrayAdapter = new DetailsArrayAdapter(this, entries);

            ListView listView = findViewById(R.id.details_list);
            listView.setEmptyView(findViewById(R.id.search_empty));
            listView.setAdapter(detailsArrayAdapter);

        }

    }

}
