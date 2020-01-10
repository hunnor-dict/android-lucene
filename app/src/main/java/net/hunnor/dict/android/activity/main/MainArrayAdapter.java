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

public class MainArrayAdapter extends ArrayAdapter<String> {

    private boolean suggestions;

    MainArrayAdapter(Context context, List<String> entryList) {
        super(context, 0, entryList);
    }

    @Override
    public @NonNull
    View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.search_result, parent, false);
        }

        String str = getItem(position);
        TextView textView = convertView.findViewById(R.id.search_result);
        if (suggestions) {
            textView.setTypeface(null, Typeface.ITALIC);
        }
        textView.setText(str);

        return convertView;

    }

    void setSuggestions(boolean suggestions) {
        this.suggestions = suggestions;
    }

}