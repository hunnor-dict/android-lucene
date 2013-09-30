package net.hunnor.dict;

import java.util.List;
import java.util.Map;

import net.hunnor.dict.util.Device;
import net.hunnor.dict.util.Formatter;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

public class DatabaseCheckTask extends AsyncTask<String, Void, Map<String, List<String>>> {

	private Context context;
	private View view;
	private ProgressDialog progressDialog;

	public DatabaseCheckTask(Context context, View view) {
		this.context = context;
		this.view = view;
	}

	@Override
	public void onPreExecute() {
		progressDialog = new ProgressDialog(context);
		progressDialog.setCancelable(true);
		progressDialog.setMessage(context.getResources().getString(R.string.database_check_update));
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.show();		
	}

	@Override
	protected Map<String, List<String>> doInBackground(String... urls) {
		Device device = new Device();
		return device.network().getHttpHeaderFromUrl(urls[0]);
	}

	@Override
	public void onPostExecute(Map<String, List<String>> result) {
		StringBuilder sb = new StringBuilder();
		List<String> dateList = result.get("Last-Modified");
		sb.append("<br>");
		sb.append(context.getResources().getString(R.string.last_modified)).append(": ");
		for (String date: dateList) {
			sb.append(Formatter.date(date, "EEE, dd MMM yyyy H:m:s zzz"));
		}
		sb.append("<br>");
		sb.append(context.getResources().getString(R.string.size)).append(": ");
		List<String> sizeList = result.get("Content-Length");
		for (String size: sizeList) {
			sb.append(Formatter.humanReadableBytes(Double.parseDouble(size)));
		}
		TextView textView = (TextView) view;
		textView.append(Html.fromHtml(sb.toString()));
		progressDialog.dismiss();
	}

}
