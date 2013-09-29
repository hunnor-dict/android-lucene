package net.hunnor.dict;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import net.hunnor.dict.util.Device;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

public class UpdateDownloadTask extends AsyncTask<String, Void, String> {

	private Context context;
	private View view;
	private ProgressDialog progressDialog;

	public UpdateDownloadTask(Context context, View view) {
		this.context = context;
		this.view = view;
	}

	@Override
	public void onPreExecute() {
		progressDialog = new ProgressDialog(context);
		progressDialog.setCancelable(true);
		progressDialog.setMessage(context.getResources().getString(R.string.database_check_update));
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setProgress(0);
		progressDialog.setMax(100);
		progressDialog.show();		
	}

	@Override
	protected String doInBackground(String... params) {
		Device device = new Device();
		HttpURLConnection connection = null;
		FileOutputStream outputStream = null;
		try {
			URL url = new URL(params[0]);
			connection = (HttpURLConnection) url.openConnection();
			String sizeAsString = connection.getHeaderField("Content-Length");
			int size = Integer.parseInt(sizeAsString);
			progressDialog.setMax(size);
			InputStream inputStream = connection.getInputStream();
			File outFile = device.storage().directory(LuceneConstants.INDEX_ZIP);
			outputStream = new FileOutputStream(outFile);
			byte[] buffer = new byte[10240];
			int count;
			int absCount = 0;
			while ((count = inputStream.read(buffer, 0, 10240)) != -1) {
				absCount = absCount + count;
				outputStream.write(buffer, 0, count);
				progressDialog.setProgress(absCount);
			}
		} catch (MalformedURLException exception) {
		} catch (IOException exception) {
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException exception) {
					
				}
			}
		}
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
	}

	@Override
	public void onPostExecute(String result) {
		progressDialog.dismiss();
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
