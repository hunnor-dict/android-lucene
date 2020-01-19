package net.hunnor.dict.android.task;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import net.hunnor.dict.android.activity.database.DatabaseActivity;
import net.hunnor.dict.android.service.NetworkService;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CheckForUpdates extends AsyncTask<String, Void, Map<String, String>> {

    private static final String TAG = CheckForUpdates.class.getName();

    private static final String STATUS = "status";

    private static final String DATE = "date";

    private static final String SIZE = "size";

    private WeakReference<Activity> activityWeakReference;

    private NetworkService networkService;

    public CheckForUpdates(Activity activity, NetworkService networkService) {
        this.activityWeakReference = new WeakReference<>(activity);
        this.networkService = networkService;
    }

    @Override
    protected Map<String, String> doInBackground(String... strings) {

        if (strings.length == 0) {
            throw new IllegalArgumentException("CheckForUpdates expects the download URL as parameter");
        }

        String fromUrl = strings[0];

        URL url;

        try {
            url = new URL(fromUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("The URL parameter must be a valid URL", e);
        }

        Map<String, String> result = new HashMap<>();

        try {
            result = networkService.resourceStats(url);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return result;

    }

    @Override
    protected void onPostExecute(Map<String, String> result) {

        final Activity activity = activityWeakReference.get();

        if (!DatabaseActivity.class.isAssignableFrom((activity.getClass()))) {
            return;
        }

        DatabaseActivity databaseActivity = (DatabaseActivity) activity;
        databaseActivity.checkForUpdatesCallback(
                result.get(STATUS), result.get(DATE), result.get(SIZE));

    }

}
