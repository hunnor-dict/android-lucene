package net.hunnor.dict;

import java.util.List;
import java.util.Map;

import net.hunnor.dict.util.Device;
import net.hunnor.dict.util.Formatter;
import net.hunnor.dict.util.Request;
import net.hunnor.dict.util.RequestStatus;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

public class DatabaseCheckTask extends
		AsyncTask<String, Void, Request> {

	private Context context;
	private Device device;
	private ProgressDialog progressDialog;
	private View view;

	public DatabaseCheckTask(Context context, View view) {
		this.context = context;
		this.view = view;
	}

	@Override
	public void onPreExecute() {
		device = new Device();
		if (!device.network().online(context)) {
			return;
		}
		progressDialog = new ProgressDialog(context);
		progressDialog.setCancelable(true);
		progressDialog.setMessage(context.getResources().getString(
				R.string.database_check_dialog));
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.show();
	}

	@Override
	protected Request doInBackground(String... urls) {
		return device.network().getHttpHeaderFromUrl(context, urls[0]);
	}

	@Override
	public void onPostExecute(Request request) {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}

		RequestStatus status = request.status();
		@SuppressWarnings("unchecked")
		Map<String, List<String>> result =
				(Map<String, List<String>>) request.response();
		TextView textView = (TextView) view;
		StringBuilder sb = new StringBuilder();

		switch (status) {
		case OK:
			if (result != null) {
				sb.append(context.getResources().getString(
						R.string.database_check_details));
				sb.append(":<br>");
				List<String> dateList = result.get("Last-Modified");
				sb.append(context.getResources().getString(
						R.string.last_modified)).append(": ");
				for (String date: dateList) {
					sb.append(Formatter.date(date, "EEE, dd MMM yyyy H:m:s zzz"));
				}
				sb.append("<br>");
				sb.append(context.getResources().getString(
						R.string.size)).append(": ");
				List<String> sizeList = result.get("Content-Length");
				for (String size: sizeList) {
					sb.append(Formatter.humanReadableBytes(Double.parseDouble(size)));
				}
			}
			break;
		case IO_EXCEPTION_NETWORK:
			sb.append(context.getResources().getString(R.string.error));
			sb.append(": ");
			sb.append(context.getResources().getString(
					R.string.database_check_error_io_net));
			break;
		case MALFORMED_URL_EXCEPTION:
			sb.append(context.getResources().getString(R.string.error));
			sb.append(": ");
			sb.append(context.getResources().getString(
					R.string.database_check_error_malformed_url));
			break;
		case NET_NOT_READY:
			sb.append(context.getResources().getString(
					R.string.database_check_error_net_not_ready));
			break;
		case PROTOCOL_EXCEPTION:
			sb.append(context.getResources().getString(R.string.error));
			sb.append(": ");
			sb.append(context.getResources().getString(
					R.string.database_check_error_protocol));
			break;
		}
		textView.setText(Html.fromHtml(sb.toString()));
	}

}
