package net.hunnor.dict.android;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.hunnor.dict.lucene.model.Entry;

import java.util.List;

import androidx.annotation.NonNull;

public class DetailsArrayAdapter extends ArrayAdapter<Entry> {

    DetailsArrayAdapter(Context context, List<Entry> entryList) {
        super(context, 0, entryList);
    }

    @Override
    public @NonNull
    View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.search_details, parent, false);
        }

        Entry entry = getItem(position);
        if (entry != null) {
            TextView textView = convertView.findViewById(R.id.search_detail);
            textView.setText(Html.fromHtml(entry.getText()));
        }

        return convertView;

    }

}
