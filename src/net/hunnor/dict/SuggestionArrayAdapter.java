package net.hunnor.dict;

import java.util.ArrayList;
import java.util.List;

import net.hunnor.dict.data.Dictionary;
import net.hunnor.dict.data.Entry;
import net.hunnor.dict.util.Device;
import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

public class SuggestionArrayAdapter
		extends ArrayAdapter<Spanned> implements Filterable {

	private ArrayList<Spanned> resultList;

	public SuggestionArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	@Override
	public int getCount() {
		return resultList.size();
	}

	@Override
	public Spanned getItem(int index) {
		return resultList.get(index);
	}

	@Override
	public Filter getFilter() {
		Filter filter = new Filter() {

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults filterResults = new FilterResults();
				if (constraint != null) {
					List<String> suggestionList = autocomplete(constraint.toString());
					resultList = new ArrayList<Spanned>();
					for (String suggestion: suggestionList) {
						resultList.add(Html.fromHtml(suggestion));
					}
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
			dictionary.openSpellChecker(
					device.storage().directory(LuceneConstants.SPELLING_DIR));
			List<Entry> searchResults = dictionary.suggest(query);
			if (searchResults.isEmpty()) {
				String[] suggestions = dictionary.spellCheck(query);
				for (String suggestion: suggestions) {
					list.add("<i>" + suggestion + "</i>");
				}
			} else {
				for (Entry searchResult: searchResults) {
					list.add(searchResult.getRoot());
				}
			}
		}
		return list;
	}

}
