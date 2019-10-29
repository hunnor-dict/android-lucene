package net.hunnor.dict.android.activity.main;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Environment;

import net.hunnor.dict.android.util.Storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class ExtractTask extends AsyncTask<Void, Void, Storage.Status> {

    private static final String DICTIONARY_ASSET = "lucene-index.zip";

    private WeakReference<Activity> activityWeakReference;

    ExtractTask(Activity activity) {
        activityWeakReference = new WeakReference<>(activity);
    }

    @Override
    protected Storage.Status doInBackground(Void... voids) {

        Storage.Status status = null;

        if (activityWeakReference != null) {

            Activity activity = activityWeakReference.get();
            AssetManager assetManager = activity.getAssets();

            File baseDirectory = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

            try {
                InputStream inputStream = assetManager.open(DICTIONARY_ASSET);
                status = Storage.deployDictionary(inputStream, baseDirectory);
            } catch (IOException e) {
                status = Storage.Status.E_EXCEPTION_IO;
            }

        }

        return status;

    }

    protected void onPostExecute(Storage.Status status) {

        final Activity activity = activityWeakReference.get();

        if (!MainActivity.class.equals(activity.getClass())) {
            return;
        }

        MainActivity mainActivity = (MainActivity) activity;

        mainActivity.deployFinished(status);

    }

}
