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
import net.hunnor.dict.android.model.Word;

import java.util.List;

public class WordArrayAdapter extends ArrayAdapter<Word> {

    WordArrayAdapter(Context context, List<Word> wordList) {
        super(context, 0, wordList);
    }

    @Override
    public @NonNull
    View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.search_words, parent, false);
        }

        Word word = getItem(position);
        TextView textView = convertView.findViewById(R.id.search_word);

        switch (word.getSource()) {
            case ROOTS:
                textView.setTypeface(null, Typeface.NORMAL);
                textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                break;
            case SPELLING:
                textView.setTypeface(null, Typeface.ITALIC);
                textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                break;
            case HISTORY:
                textView.setTypeface(null, Typeface.NORMAL);
                textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_history_gray_24dp, 0, 0, 0);
                break;
            default:
                break;
        }
        textView.setText(word.getValue());

        return convertView;

    }

}
