package net.hunnor.dict;

import net.hunnor.dict.data.Entry;
import android.content.Context;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SearchArrayAdapter extends ArrayAdapter<Entry> {

	private final Context context;
	private final int resourceId;
	private final Entry[] resultArray;

	public SearchArrayAdapter(
			Context context,
			int resourceId,
			Entry[] resultArray) {
		super(context, resourceId, resultArray);
		this.context = context;
		this.resourceId = resourceId;
		this.resultArray = resultArray;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(resourceId, parent, false);
		Entry result = resultArray[position];
		TextView textView = (TextView)
				rowView.findViewById(R.id.search_result_text);
		textView.setText(Html.fromHtml(result.getText()));
		/*
		ImageSpan flag = null;
		if (LuceneConstants.LANG_HU.equals(result.getLang())) {
			flag = new ImageSpan(this.context,
					R.drawable.flag_hu, ImageSpan.ALIGN_BASELINE);
		} else if (LuceneConstants.LANG_NO.equals(result.getLang())) {
			flag = new ImageSpan(this.context,
					R.drawable.flag_no, ImageSpan.ALIGN_BASELINE);
		}
		*/
		// TODO Fix flag for higher API levels
		// SpannableString s1 = new SpannableString(".");
		SpannableString s2 = new SpannableString(Html.fromHtml(result.getText()));
		// s1.setSpan(flag, 0, 0, 0);
		SpannableStringBuilder sb = new SpannableStringBuilder();
		// sb.append(s1).append(s2);
		sb.append(s2);
		textView.setText(sb);
		return rowView;
	}

}
