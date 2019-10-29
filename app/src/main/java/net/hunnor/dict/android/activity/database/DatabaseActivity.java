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
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import net.hunnor.dict.android.R;
import net.hunnor.dict.android.activity.ActivityTemplate;
import net.hunnor.dict.android.util.Storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseActivity extends ActivityTemplate {

    private static final String TAG = DatabaseActivity.class.getName();

    private static final String INDEX_URL = "https://dict.hunnor.net/databases/HunNor-Lucene.zip";

    private static CheckTask checkTask;

    private DownloadManager downloadManager;

    private long queueId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        setListeners();

        displayInstalledDictionary();
        displayAvailableUpdate();

    }

    private void setListeners() {

        BroadcastReceiver receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                if (!DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    return;
                }

                downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

                Query query = new Query();
                query.setFilterById(queueId);
                Cursor cursor = downloadManager.query(query);
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(columnIndex)) {

                        String uri = cursor.getString(cursor.getColumnIndex(
                                DownloadManager.COLUMN_LOCAL_URI));
                        startExtractTask(Uri.parse(uri));

                    }
                }

            }

        };

        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }

    private void startExtractTask(Uri uri) {

        String path = uri.getPath();

        if (path == null) {
            return;
        }

        File file = new File(path);

        InputStream inputStream;
        try {
            inputStream = new FileInputStream(file);
            File baseDirectory = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            Storage.Status status = Storage.deployDictionary(inputStream, baseDirectory);
            if (Storage.Status.OK.equals(status)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.database_extract_completed).setCancelable(false)
                        .setPositiveButton(R.string.alert_ok, (DialogInterface dialog, int id) ->
                                dialog.dismiss());
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                displayError(status);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            displayError(Storage.Status.E_EXCEPTION_IO);
        }

        if (!file.delete()) {
            Log.e(TAG, "Error deleting downloaded file");
        }

        displayInstalledDictionary();

    }

    public void displayInstalledDictionary() {

        long size = 0;
        long timestamp = 0;

        File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

        File indexDirectory = new File(externalFilesDir, Storage.DICTIONARY_INDEX_DIRECTORY);
        if (indexDirectory.canRead() && indexDirectory.isDirectory()) {
            File[] files = indexDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.canRead() && file.isFile()) {
                        size = size + file.length();
                        long lastModified = file.lastModified();
                        if (lastModified > timestamp) {
                            timestamp = lastModified;
                        }
                    }
                }
            }
        }

        File spellingIndexDirectory = new File(externalFilesDir, Storage.DICTIONARY_SPELLING_DIRECTORY);
        if (spellingIndexDirectory.canRead() && spellingIndexDirectory.isDirectory()) {
            File[] files = spellingIndexDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.canRead() && file.isFile()) {
                        size = size + file.length();
                        long lastModified = file.lastModified();
                        if (lastModified > timestamp) {
                            timestamp = lastModified;
                        }
                    }
                }
            }
        }

        setLocalSizeAndDate(size, timestamp);

    }

    private void setLocalSizeAndDate(long size, long date) {

        TextView textView = findViewById(R.id.database_local_title);
        textView.setText(R.string.database_local_title);

        textView = findViewById(R.id.database_local_date_label);
        textView.setText(R.string.database_label_date);
        textView = findViewById(R.id.database_local_date);
        textView.setText(formatDate(date));

        textView = findViewById(R.id.database_local_size_label);
        textView.setText(R.string.database_label_size);
        textView = findViewById(R.id.database_local_size);
        textView.setText(Formatter.formatFileSize(this, size));

    }

    public void displayAvailableUpdate() {

        if (checkTask != null && AsyncTask.Status.RUNNING.equals(checkTask.getStatus())) {
            return;
        }

        TextView textView = findViewById(R.id.database_available_update_title);
        textView.setText(R.string.database_cehck_begin);

        Context context = getApplicationContext();
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                checkTask = new CheckTask(this);
                checkTask.execute(INDEX_URL);
                return;
            }
        }

        textView.setText(R.string.database_check_offline);

    }

    public void setRemoteSizeAndDate(String status, String contentLength, String lastModified) {

        TextView textView = findViewById(R.id.database_available_update_title);

        if (status == null) {
            textView.setText(R.string.database_check_error);
            return;
        }

        if (status.startsWith("2")) {

            textView.setText(R.string.database_remote_title);

            textView = findViewById(R.id.database_remote_date_label);
            textView.setText(R.string.database_label_date);
            textView = findViewById(R.id.database_remote_date);
            textView.setText(reformatDate(lastModified));

            textView = findViewById(R.id.database_remote_size_label);
            textView.setText(R.string.database_label_size);
            textView = findViewById(R.id.database_remote_size);
            long size = 0;
            if (contentLength != null) {
                size = Long.parseLong(contentLength);
            }
            textView.setText(Formatter.formatFileSize(getApplicationContext(), size));

        } else {
            String text = getString(R.string.database_check_error_status, status);
            textView.setText(text);
        }

    }

    public void doDownload(View view) {
        String externalStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(externalStorageState)) {

            downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

            Query query = new Query();
            query.setFilterById(queueId);
            Cursor cursor = downloadManager.query(query);
            if (cursor.moveToFirst()) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.database_download_running, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }

            File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            File downloadFile = new File(externalFilesDir, "HunNor-Lucene.zip");
            Uri uri = Uri.fromFile(downloadFile);

            Request request = new Request(
                    Uri.parse(INDEX_URL));
            request.setDestinationUri(uri);
            queueId = downloadManager.enqueue(request);

            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.database_download_notification, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();

        }
    }

    protected void displayError(Storage.Status status) {

        String message = "";

        switch (status) {
            case E_DEPLOY_DELETE_DEPLOY_INDEX_DIR:
                message = getString(R.string.database_status_e_deploy_delete_deploy_index_dir);
                break;
            case E_DEPLOY_DELETE_DEPLOY_SPELLING_DIR:
                message = getString(R.string.database_status_e_deploy_delete_deploy_spelling_dir);
                break;
            case E_DEPLOY_DELETE_INDEX_DIR:
                message = getString(R.string.database_status_e_deploy_delete_index_dir);
                break;
            case E_DEPLOY_DELETE_SPELLING_DIR:
                message = getString(R.string.database_status_e_deploy_delete_spelling_dir);
                break;
            case E_DEPLOY_RENAME_INDEX_DIR:
                message = getString(R.string.database_status_e_deploy_rename_index_dir);
                break;
            case E_DEPLOY_RENAME_SPELLING_DIR:
                message = getString(R.string.database_status_e_deploy_rename_spelling_dir);
                break;
            case E_DEPLOY_ZIP_ENTRY_DIR_CREATE:
                message = getString(R.string.database_status_e_deploy_zip_entry_dir_create);
                break;
            case E_EXCEPTION_IO:
                message = getString(R.string.database_status_e_exception_io);
                break;
            default:
                break;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setCancelable(false)
                .setPositiveButton(R.string.alert_ok, (DialogInterface dialog, int id) ->
                        dialog.dismiss());
        AlertDialog alert = builder.create();
        alert.show();

    }

    // Utility methods for formatting dates

    private String formatDate(long timestamp) {
        return formatDate(new Date(timestamp));
    }

    private String formatDate(Date date) {
        SimpleDateFormat appFormat =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return appFormat.format(date);
    }

    private String reformatDate(String original) {
        SimpleDateFormat httpHeaderFormat =
                new SimpleDateFormat("EEE, dd MMM yyyy H:m:s zzz", Locale.getDefault());
        String result = null;
        try {
            Date date = httpHeaderFormat.parse(original);
            result = formatDate(date);
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage() == null ? "null" : e.getMessage());
        }
        return result;
    }

}
