package net.hunnor.dict;

import java.util.ArrayList;
import java.util.List;

import net.hunnor.dict.data.Dictionary;
import net.hunnor.dict.data.Entry;
import net.hunnor.dict.util.Device;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

public class SuggestionArrayAdapter
		extends ArrayAdapter<String> implements Filterable {

	private ArrayList<String> resultList;

	public SuggestionArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	@Override
	public int getCount() {
		return resultList.size();
	}

	@Override
	public String getItem(int index) {
		return resultList.get(index);
	}

	@Override
	public Filter getFilter() {
		Filter filter = new Filter() {

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults filterResults = new FilterResults();
				if (constraint != null) {
					resultList = autocomplete(constraint.toString());
					filterResults.values = resultList;
					filterResults.count = resultList.size();
				}
				return filterResults;
			}

			@Override
			protected void publishResults(
					CharSequence constraint, FilterResults results) {
				if (results != null && results.count > 0) {
					notifyDataSetChanged();
				} else {
					notifyDataSetInvalidated();
				}
			}

		};
		return filter;
	}

	private ArrayList<String> autocomplete(String query) {
		ArrayList<String> list = new ArrayList<String>();
		if (query.length() > 4) {
			Dictionary dictionary = new Dictionary();
			Device device = new Device();
			dictionary.open(
					device.storage().directory(LuceneConstants.INDEX_DIR));
			List<Entry> searchResults = dictionary.suggest(query);
			for (Entry searchResult: searchResults) {
				list.add(searchResult.getRoot());
			}
		}
		return list;
	}

}
