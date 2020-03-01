package net.hunnor.dict.android.task;

import android.app.Activity;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import net.hunnor.dict.android.activity.database.DatabaseActivity;
import net.hunnor.dict.android.activity.details.DetailsActivity;
import net.hunnor.dict.android.activity.main.MainActivity;
import net.hunnor.dict.android.service.StorageService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class ExtractTask extends AsyncTask<Uri, Void, ExtractTaskStatus> {

    private static final String DICTIONARY_ASSET = "lucene-index.zip";

    private WeakReference<Activity> activityWeakReference;

    private StorageService storageService;

    public ExtractTask(Activity activity, StorageService storageService) {
        this.activityWeakReference = new WeakReference<>(activity);
        this.storageService = storageService;
    }

    @Override
    protected ExtractTaskStatus doInBackground(Uri... uris) {

        ExtractTaskStatus status = ExtractTaskStatus.E_EXCEPTION_IO;

        if (uris == null || uris.length == 0) {

            // MainActivity

            if (activityWeakReference != null) {
                Activity activity = activityWeakReference.get();
                AssetManager assetManager = activity.getAssets();
                File baseDirectory = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                status = deployAsset(assetManager, baseDirectory);
            }

        } else {

            // DatabaseActivity

            Uri uri = uris[0];
            String path = uri.getPath();
            if (path != null) {
                File file = new File(path);
                if (activityWeakReference != null) {
                    Activity activity = activityWeakReference.get();
                    File baseDirectory = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                    status = deployFile(file, baseDirectory);
                }
            }

        }

        return status;

    }

    @Override
    protected void onPostExecute(ExtractTaskStatus status) {

        final Activity activity = activityWeakReference.get();

        if (MainActivity.class.isAssignableFrom(activity.getClass())) {
            MainActivity mainActivity = (MainActivity) activity;
            mainActivity.extractTaskCallback(status);
        } else if (DetailsActivity.class.isAssignableFrom(activity.getClass())) {
            DetailsActivity detailsActivity = (DetailsActivity) activity;
            detailsActivity.extractTaskCallback(status);
        } else if (DatabaseActivity.class.isAssignableFrom(activity.getClass())) {
            DatabaseActivity databaseActivity = (DatabaseActivity) activity;
            databaseActivity.extractTaskCallback(status);
        }

    }

    private ExtractTaskStatus deployAsset(AssetManager assetManager, File baseDirectory) {
        try {
            InputStream inputStream = assetManager.open(DICTIONARY_ASSET);
            return deployStream(inputStream, baseDirectory);
        } catch (IOException e) {
            return ExtractTaskStatus.E_STREAM_ASSET_NOT_FOUND;
        }
    }

    private ExtractTaskStatus deployFile(File file, File baseDirectory) {
        try {
            InputStream inputStream = new FileInputStream(file);
            return deployStream(inputStream, baseDirectory);
        } catch (FileNotFoundException e) {
            return ExtractTaskStatus.E_STREAM_FILE_NOT_FOUND;
        }

    }

    private ExtractTaskStatus deployStream(InputStream inputStream, File baseDirectory) {

        String suffix = "-deploy";

        try {
            File indexDirectory = new File(baseDirectory,
                    StorageService.DICTIONARY_INDEX_DIRECTORY + suffix);
            storageService.deleteRecursively(indexDirectory);
        } catch (IOException e) {
            return ExtractTaskStatus.E_DEPLOY_DELETE_DEPLOY_INDEX_DIR;
        }

        try {
            File spellingDirectory = new File(baseDirectory,
                    StorageService.DICTIONARY_SPELLING_DIRECTORY + suffix);
            storageService.deleteRecursively(spellingDirectory);
        } catch (IOException e) {
            return ExtractTaskStatus.E_DEPLOY_DELETE_DEPLOY_SPELLING_DIR;
        }

        try {
            storageService.extract(inputStream, baseDirectory, suffix);
        } catch (IOException e) {
            return ExtractTaskStatus.E_DEPLOY_ZIP_EXTRACT;
        }

        try {
            File indexDirectory = new File(baseDirectory,
                    StorageService.DICTIONARY_INDEX_DIRECTORY);
            storageService.deleteRecursively(indexDirectory);
        } catch (IOException e) {
            return ExtractTaskStatus.E_DEPLOY_DELETE_INDEX_DIR;
        }

        try {
            File spellingDirectory = new File(baseDirectory,
                    StorageService.DICTIONARY_SPELLING_DIRECTORY);
            storageService.deleteRecursively(spellingDirectory);
        } catch (IOException e) {
            return ExtractTaskStatus.E_DEPLOY_DELETE_SPELLING_DIR;
        }

        File indexDirectory = new File(baseDirectory,
                StorageService.DICTIONARY_INDEX_DIRECTORY);
        File newIndexDirectory = new File(
                baseDirectory, StorageService.DICTIONARY_INDEX_DIRECTORY + suffix);
        if (!storageService.rename(newIndexDirectory, indexDirectory)) {
            return ExtractTaskStatus.E_DEPLOY_RENAME_INDEX_DIR;
        }

        File spellingDirectory = new File(baseDirectory,
                StorageService.DICTIONARY_SPELLING_DIRECTORY);
        File newSpellingDirectory = new File(
                baseDirectory, StorageService.DICTIONARY_SPELLING_DIRECTORY + suffix);
        if (!storageService.rename(newSpellingDirectory, spellingDirectory)) {
            return ExtractTaskStatus.E_DEPLOY_RENAME_SPELLING_DIR;
        }

        return ExtractTaskStatus.OK;

    }

}
