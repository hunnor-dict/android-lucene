package net.hunnor.dict;

import net.hunnor.dict.util.Device;
import android.content.Context;
import android.os.AsyncTask;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

public class UpdateDownloadTask extends AsyncTask<String, Void, String> {

	private Context context;
	private View view;

	public UpdateDownloadTask(Context context, View view) {
		this.context = context;
		this.view = view;
	}

	@Override
	protected String doInBackground(String... params) {
		Device device = new Device();
		if (device.storage().downloadFile(LuceneConstants.INDEX_URL, LuceneConstants.INDEX_ZIP)) {
			if (device.storage().unZip(LuceneConstants.INDEX_ZIP, "")) {
				if (device.storage().deleteDirectory(LuceneConstants.INDEX_DIR)) {
					if (device.storage().renameDirectory("hunnor-lucene-index", LuceneConstants.INDEX_DIR)) {
						return "OK";
					} else {
						return "MV";
					}
				} else {
					return "DEL";
				}
			} else {
				return "ZP";
			}
		} else {
			return "DL";
		}
	}

	@Override
	public void onPostExecute(String result) {
		StringBuilder stringBuilder = new StringBuilder();
		if ("OK".equals(result)) {
			stringBuilder.append("<font color=\"green\"><b>").append(context.getResources().getString(R.string.ok)).append("</b></font>");
		} else {
			stringBuilder.append("<font color=\"red\"><b>").append(context.getResources().getString(R.string.error)).append("</b></font>");
		}
		TextView textView = (TextView) view;
		textView.append(Html.fromHtml(stringBuilder.toString()));
	}

}
