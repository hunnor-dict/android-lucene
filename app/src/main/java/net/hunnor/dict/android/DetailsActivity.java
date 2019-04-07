package net.hunnor.dict.android;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ListView;

import net.hunnor.dict.lucene.model.Entry;
import net.hunnor.dict.lucene.searcher.LuceneSearcher;

import java.io.File;
import java.io.IOException;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class DetailsActivity extends AppCompatActivity {

    private static final String TAG = DetailsActivity.class.getName();

    private static final String DICTIONARY_INDEX_DIRECTORY = "hunnor-lucene-index";

    private static final int SEARCH_MAX_RESULTS = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        doSearch();

    }

    private void doSearch() {

        Intent intent = getIntent();
        String query = intent.getStringExtra("query");

        if (query == null) {
            return;
        }

        LuceneSearcher luceneSearcher = LuceneSearcher.getInstance();

        if (!luceneSearcher.isOpen()) {

            File baseDirectory = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (baseDirectory == null) {
                return;
            }

            try {
                luceneSearcher.open(new File(baseDirectory, DICTIONARY_INDEX_DIRECTORY));
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
                return;
            }

        }

        List<Entry> entries = luceneSearcher.search(query, SEARCH_MAX_RESULTS);
        DetailsArrayAdapter detailsArrayAdapter = new DetailsArrayAdapter(this, entries);

        ListView listView = findViewById(R.id.details_list);
        listView.setAdapter(detailsArrayAdapter);

    }

}
