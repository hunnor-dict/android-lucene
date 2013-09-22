package net.hunnor.dict;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SearchArrayAdapter extends ArrayAdapter<Spanned> {

	private final Context context;
	private final int resourceId;
	private final Spanned[] resultArray;

	public SearchArrayAdapter(Context context, int resourceId, Spanned[] resultArray) {
		super(context, resourceId, resultArray);
		this.context = context;
		this.resourceId = resourceId;
		this.resultArray = resultArray;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(resourceId, parent, false);
		Spanned result = resultArray[position];
		ImageView imageView = (ImageView) rowView.findViewById(R.search.search_result_flag);
		TextView textView = (TextView) rowView.findViewById(R.search.search_result_text);
		textView.setText(result);
		String lang = "hu";
		InputStream inputStream = null;
		try {
			inputStream = context.getAssets().open("flag_" + lang + ".png");
		} catch (IOException exception) {
		}
		Drawable drawable = Drawable.createFromStream(inputStream, null);
		imageView.setImageDrawable(drawable);
		return rowView;
	}
}
