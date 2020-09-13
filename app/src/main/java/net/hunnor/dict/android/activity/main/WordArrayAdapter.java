package net.hunnor.dict.android.activity.main;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.hunnor.dict.android.R;

import java.util.List;

public class WordArrayAdapter extends ArrayAdapter<String> {

    private boolean suggestions;

    WordArrayAdapter(Context context, List<String> wordList) {
        super(context, 0, wordList);
    }

    @Override
    public @NonNull
    View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.search_words, parent, false);
        }

        String word = getItem(position);
        TextView textView = convertView.findViewById(R.id.search_word);
        if (suggestions) {
            textView.setTypeface(null, Typeface.ITALIC);
        }
        textView.setText(word);

        return convertView;

    }

    void setSuggestions(boolean suggestions) {
        this.suggestions = suggestions;
    }

}
