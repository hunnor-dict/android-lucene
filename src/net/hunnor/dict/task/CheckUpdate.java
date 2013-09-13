package net.hunnor.dict.task;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;

public class CheckUpdate extends AsyncTask<String, Void, String> {

	@Override
	protected String doInBackground(String... urls) {
		HttpURLConnection connection = null;
		try {
			URL url = new URL(urls[0]);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("HEAD");
		} catch (MalformedURLException exception) {
		} catch (IOException exception) {
		}
		return connection.getHeaderField("Last-Modified");
		// TODO Add 'Content-Length'
	}

}
