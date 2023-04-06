package net.hunnor.dict.android.activity.main;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.hunnor.dict.android.R;
import net.hunnor.dict.lucene.model.Entry;

import java.util.List;

public class EntryArrayAdapter extends ArrayAdapter<Entry> {

    EntryArrayAdapter(Context context, List<Entry> entryList) {
        super(context, 0, entryList);
    }

    @Override
    public @NonNull
    View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.search_entries, parent, false);
        }

        Entry entry = getItem(position);
        if (entry != null) {
            TextView textView = convertView.findViewById(R.id.search_entry);
            textView.setText(Html.fromHtml(entry.getText(), Html.FROM_HTML_MODE_LEGACY));
        }

        return convertView;

    }

}
