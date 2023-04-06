package net.hunnor.dict.android.task;

import android.app.Activity;
import android.os.AsyncTask;

import net.hunnor.dict.android.activity.database.DatabaseActivity;
import net.hunnor.dict.android.service.StorageService;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Map;

public class CheckLocalFiles extends AsyncTask<File, Void, Map<String, Long>> {

    private final WeakReference<Activity> activityWeakReference;

    private final StorageService storageService;

    public CheckLocalFiles(Activity activity, StorageService storageService) {
        this.activityWeakReference = new WeakReference<>(activity);
        this.storageService = storageService;
    }

    @Override
    protected Map<String, Long> doInBackground(File... directories) {
        return storageService.fileStats(directories);
    }

    @Override
    protected void onPostExecute(Map<String, Long> result) {

        final Activity activity = activityWeakReference.get();

        if (!DatabaseActivity.class.isAssignableFrom(activity.getClass())) {
            return;
        }

        DatabaseActivity databaseActivity = (DatabaseActivity) activity;
        databaseActivity.checkLocalFilesCallback(
                result.get(StorageService.DATE), result.get(StorageService.SIZE));

    }

}
