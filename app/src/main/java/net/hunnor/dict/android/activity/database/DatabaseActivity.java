package net.hunnor.dict.android.activity.database;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import net.hunnor.dict.android.R;
import net.hunnor.dict.android.activity.ActivityTemplate;
import net.hunnor.dict.android.databinding.ActivityDatabaseBinding;
import net.hunnor.dict.android.service.NetworkService;
import net.hunnor.dict.android.service.StorageService;
import net.hunnor.dict.android.task.CheckForUpdates;
import net.hunnor.dict.android.task.CheckLocalFiles;
import net.hunnor.dict.android.task.ExtractTask;
import net.hunnor.dict.android.task.ExtractTaskStatus;
import net.hunnor.dict.android.util.DateFormatter;
import net.hunnor.dict.android.util.DateFormatterException;

import java.io.File;

public class DatabaseActivity extends ActivityTemplate {

    private static final String TAG = DatabaseActivity.class.getName();

    private static final String INDEX_URL = "https://dict.hunnor.net/databases/HunNor-Lucene.zip";

    private static final String INDEX_FILE = "HunNor-Lucene.zip";

    private static CheckForUpdates checkForUpdates;

    private static CheckLocalFiles checkLocalFiles;

    private static ExtractTask extractTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        ActivityDatabaseBinding binding = DataBindingUtil.setContentView(
                this, R.layout.activity_database);
        binding.setLifecycleOwner(this);

        DatabaseViewModel model = getViewModel();
        binding.setViewModel(model);

        // TODO Check if a previous download is ready

        registerBroadcastReceiver();

        startCheckForUpdates();
        startCheckLocalFiles();

    }

    private DatabaseViewModel getViewModel() {
        return new ViewModelProvider(this).get(DatabaseViewModel.class);
    }

    private void registerBroadcastReceiver() {

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                receiveBroadcast(intent);
            }
        };

        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }

    private void receiveBroadcast(Intent intent) {

        String action = intent.getAction();
        if (!DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            return;
        }

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        if (downloadManager == null) {
            return;
        }

        Long activeDownloadId = getViewModel().getActiveDownloadId().getValue();
        if (activeDownloadId == null || activeDownloadId == 0) {
            return;
        }

        Query query = new Query();
        query.setFilterById(activeDownloadId);
        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(columnIndex)) {
                String uriString = cursor.getString(cursor.getColumnIndex(
                        DownloadManager.COLUMN_LOCAL_URI));
                Uri uri = Uri.parse(uriString);
                startExtractTask(uri);
            }
        }
    }

    private void startExtractTask(Uri uri) {

        if (extractTask != null && AsyncTask.Status.RUNNING.equals(extractTask.getStatus())) {
            return;
        }

        StorageService storageService = new StorageService();
        extractTask = new ExtractTask(this, storageService);
        extractTask.execute(uri);

    }

    public void extractTaskCallback(ExtractTaskStatus status) {

        if (ExtractTaskStatus.OK.equals(status)) {
            startCheckLocalFiles();
        }

        getViewModel().setProgressReport(null);
        getViewModel().setActiveDownloadId(0L);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch (status) {
            case E_DEPLOY_DELETE_DEPLOY_INDEX_DIR:
                builder.setMessage(R.string.database_status_e_deploy_delete_deploy_index_dir);
                break;
            case E_DEPLOY_DELETE_DEPLOY_SPELLING_DIR:
                builder.setMessage(R.string.database_status_e_deploy_delete_deploy_spelling_dir);
                break;
            case E_DEPLOY_DELETE_INDEX_DIR:
                builder.setMessage(R.string.database_status_e_deploy_delete_index_dir);
                break;
            case E_DEPLOY_DELETE_SPELLING_DIR:
                builder.setMessage(R.string.database_status_e_deploy_delete_spelling_dir);
                break;
            case E_DEPLOY_RENAME_INDEX_DIR:
                builder.setMessage(R.string.database_status_e_deploy_rename_index_dir);
                break;
            case E_DEPLOY_RENAME_SPELLING_DIR:
                builder.setMessage(R.string.database_status_e_deploy_rename_spelling_dir);
                break;
            case E_DEPLOY_ZIP_EXTRACT:
                builder.setMessage(R.string.database_status_e_deploy_zip_entry_dir_create);
                break;
            case E_EXCEPTION_IO:
                builder.setMessage(R.string.database_status_e_exception_io);
                break;
            case OK:
                builder.setMessage(R.string.database_extract_completed);
                break;
            default:
                break;
        }

        builder.setCancelable(false).setPositiveButton(
                R.string.alert_ok, (DialogInterface dialog, int id) -> dialog.dismiss());
        AlertDialog alert = builder.create();
        alert.show();

    }

    private void startCheckForUpdates() {

        if (checkForUpdates != null && AsyncTask.Status.RUNNING.equals(checkForUpdates.getStatus())) {
            return;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return;
        }

        DatabaseViewModel model = getViewModel();

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            model.setRemoteStatus(getString(R.string.database_cehck_begin));
            NetworkService networkService = new NetworkService();
            checkForUpdates = new CheckForUpdates(this, networkService);
            checkForUpdates.execute(INDEX_URL);
        } else {
            model.setRemoteStatus(getString(R.string.database_check_offline));
        }

    }

    public void checkForUpdatesCallback(String status, String lastModified, String contentLength) {

        DatabaseViewModel model = getViewModel();
        if (status == null) {
            model.setRemoteStatus(getString(R.string.database_check_error));
        } else if (status.startsWith("2")) {

            long size = 0;
            if (contentLength != null) {
                size = Long.parseLong(contentLength);
            }

            try {
                model.setRemoteDate(DateFormatter.reformatDate(lastModified));
            } catch (DateFormatterException e) {
                Log.e(TAG, e.getMessage() == null ? "null" : e.getMessage());
            }
            model.setRemoteSize(Formatter.formatFileSize(getApplicationContext(), size));
            model.setRemoteStatus(null);

        } else {
            model.setRemoteStatus(getString(R.string.database_check_error_status, status));
        }

    }

    private void startCheckLocalFiles() {

        if (checkLocalFiles != null && AsyncTask.Status.RUNNING.equals(checkLocalFiles.getStatus())) {
            return;
        }

        File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File indexDirectory = new File(externalFilesDir, StorageService.DICTIONARY_INDEX_DIRECTORY);
        File spellingIndexDirectory = new File(externalFilesDir, StorageService.DICTIONARY_SPELLING_DIRECTORY);

        StorageService storageService = new StorageService();
        checkLocalFiles = new CheckLocalFiles(this, storageService);
        checkLocalFiles.execute(indexDirectory, spellingIndexDirectory);

    }

    public void checkLocalFilesCallback(Long localDate, Long localSize) {
        DatabaseViewModel model = getViewModel();
        model.setLocalDate(DateFormatter.formatDate(localDate));
        model.setLocalSize(Formatter.formatFileSize(this, localSize));
    }

    public void doDownload(View view) {

        String externalStorageState = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(externalStorageState)) {
            return;
        }

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        if (downloadManager == null) {
            return;
        }

        DatabaseViewModel model = getViewModel();

        Long activeDownloadId = model.getActiveDownloadId().getValue();
        if (activeDownloadId != null && activeDownloadId != 0) {
            Query query = new Query();
            query.setFilterById(activeDownloadId);
            Cursor cursor = downloadManager.query(query);
            if (cursor.moveToFirst()) {
                model.setProgressReport(getString(R.string.database_download_running));
                return;
            }
        }

        File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File downloadFile = new File(externalFilesDir, INDEX_FILE);
        Uri uri = Uri.fromFile(downloadFile);

        Request request = new Request(Uri.parse(INDEX_URL));
        request.setDestinationUri(uri);
        long queueId = downloadManager.enqueue(request);
        model.setActiveDownloadId(queueId);

        model.setProgressReport(getString(R.string.database_download_notification));

    }

}
