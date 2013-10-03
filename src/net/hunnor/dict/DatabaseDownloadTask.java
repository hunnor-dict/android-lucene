package net.hunnor.dict;

import net.hunnor.dict.util.Device;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

public class DatabaseDownloadTask extends AsyncTask<String, Void, String> {

	private Context context;
	private View view;
	private ProgressDialog progressDialog;

	private static final String OK = "OK";
	private static final String ERROR_DOWNLOAD = "DL";
	private static final String ERROR_EXTRACT = "EX";
	private static final String ERROR_DELETE = "RM";
	private static final String ERROR_MOVE = "MV";

	public DatabaseDownloadTask(Context context, View view) {
		this.context = context;
		this.view = view;
	}

	@Override
	public void onPreExecute() {
		progressDialog = new ProgressDialog(context);
		progressDialog.setCancelable(true);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		// Requires API 11
		// progressDialog.setProgressNumberFormat(null);
		progressDialog.setMessage(context.getResources().getString(
				R.string.database_download_preparing));
		progressDialog.show();
	}

	@Override
	protected String doInBackground(String... params) {
		Device device = new Device();

		// Connection
		setMessage(progressDialog, context.getResources().getString(
				R.string.database_download_downloading) + "...");
		if (!device.network().online(context)) {
			return ERROR_DOWNLOAD;
		}

		// Download
		if (!device.storage()
				.downloadFileWithReporting(params[0], progressDialog, context)) {
			return ERROR_DOWNLOAD;
		}

		// Extract
		setMessage(progressDialog, context.getResources().getString(
				R.string.database_download_extracting) + "...");
		if (!device.storage().extractWithReporting(
				LuceneConstants.INDEX_ZIP, "",
				progressDialog, context)) {
			return ERROR_EXTRACT;
		}

		// Install
		setMessage(progressDialog, context.getResources().getString(
				R.string.database_download_installing) + "...");
		if (!device.storage().deleteDirectory(LuceneConstants.INDEX_DIR)) {
			return ERROR_DELETE;
		}
		if (!device.storage().renameDirectory(
				LuceneConstants.INDEX_ZIP_DIR, LuceneConstants.INDEX_DIR)) {
			return ERROR_MOVE;
		}

		return OK;
	}

	@Override
	public void onPostExecute(String result) {
		progressDialog.dismiss();
		StringBuilder sb = new StringBuilder();
		if (OK.equals(result)) {
			sb.append(context.getResources().getString(
					R.string.database_download_finished));
		} else {
			sb.append("<font color=\"red\"><b>");
			sb.append(context.getResources().getString(R.string.error));
			sb.append("</b></font>: ");
			if (ERROR_DOWNLOAD.equals(result)) {
				sb.append(context.getResources().getString(
						R.string.database_download_error_download));
			} else if (ERROR_EXTRACT.equals(result)) {
				sb.append(context.getResources().getString(
						R.string.database_download_error_extract));
			} else if (ERROR_DELETE.equals(result)) {
				sb.append(context.getResources().getString(
						R.string.database_download_error_delete));
			} else if (ERROR_MOVE.equals(result)) {
				sb.append(context.getResources().getString(
						R.string.database_download_error_move));
			}
		}
		TextView textView = (TextView) view;
		textView.setText(Html.fromHtml(sb.toString()));
	}

	protected void setMessage(ProgressDialog progressDialog, String message) {
	}

}
