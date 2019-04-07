package net.hunnor.dict.android.task;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import net.hunnor.dict.android.DatabaseActivity;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CheckTask extends AsyncTask<String, Void, Map<String, String>> {

    private static final String TAG = CheckTask.class.getName();

    private WeakReference<Activity> activityWeakReference;

    public CheckTask(Activity activity) {
        activityWeakReference = new WeakReference<>(activity);
    }

    @Override
    protected Map<String, String> doInBackground(String... strings) {

        if (strings.length == 0) {
            throw new IllegalArgumentException("CheckTask expects the download URL as parameter");
        }

        String fromUrl = strings[0];

        URL url;

        try {
            url = new URL(fromUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("The URL parameter must be a valid URL", e);
        }

        Map<String, String> result = new HashMap<>();;

        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (connection != null) {

                connection.setRequestMethod("HEAD");
                int status = connection.getResponseCode();

                result.put("status", Integer.toString(status));
                if (HttpURLConnection.HTTP_OK == status) {
                    result.put("size", connection.getHeaderField("Content-Length"));
                    result.put("date", connection.getHeaderField("Last-Modified"));
                    connection.disconnect();
                }

            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return result;

    }

    @Override
    protected void onPostExecute(Map<String, String> result) {

        final Activity activity = activityWeakReference.get();

        if (!DatabaseActivity.class.equals(activity.getClass())) {
            Log.w(TAG, "Task called by unexpected Activity "
                    + activity.getClass().getCanonicalName());
            return;
        }

        DatabaseActivity databaseActivity = (DatabaseActivity) activity;
        databaseActivity.setRemoteSizeAndDate(
                result.get("status"), result.get("size"), result.get("date"));

    }

}
