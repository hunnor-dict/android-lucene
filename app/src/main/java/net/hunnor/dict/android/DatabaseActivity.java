package net.hunnor.dict.android;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
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
import android.widget.TextView;

import net.hunnor.dict.android.task.CheckTask;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import androidx.appcompat.app.AlertDialog;

public class DatabaseActivity extends ActivityTemplate {

    private static final String TAG = DatabaseActivity.class.getName();

    public enum Status {

        E_DEPLOY_DELETE_DEPLOY_INDEX_DIR,

        E_DEPLOY_DELETE_DEPLOY_SPELLING_DIR,

        E_DEPLOY_DELETE_INDEX_DIR,

        E_DEPLOY_DELETE_SPELLING_DIR,

        E_DEPLOY_RENAME_INDEX_DIR,

        E_DEPLOY_RENAME_SPELLING_DIR,

        E_DEPLOY_ZIP_ENTRY_DIR_CREATE,

        E_EXCEPTION_IO,

        OK

    }

    private static final String INDEX_URL = "https://dict.hunnor.net/databases/HunNor-Lucene.zip";

    private static final String DICTIONARY_INDEX_DIRECTORY = "hunnor-lucene-index";

    private static final String DICTIONARY_SPELLING_DIRECTORY = "hunnor-lucene-spelling";

    private static CheckTask checkTask;

    private DownloadManager downloadManager;

    private long queueId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        processDeployRequest();

        setListeners();

        displayInstalledDictionary();
        displayAvailableUpdate();

    }

    private void processDeployRequest() {

        Intent intent = getIntent();
        boolean deploy = intent.getBooleanExtra("deploy", false);
        if (!deploy) {
            return;
        }

        try {

            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open("lucene-index.zip");

            Status status = deployDictionary(inputStream);

            if (Status.OK.equals(status)) {
                returnToSearch();
            } else {
                displayError(status);
            }

        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            displayError(Status.E_EXCEPTION_IO);
        }

    }

    private Status deployDictionary(InputStream inputStream) {

        File baseDirectory = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

        String suffix = "-deploy";

        // Delete files to make names available for extraction

        File indexDirectory = new File(baseDirectory,
                DICTIONARY_INDEX_DIRECTORY + suffix);
        if (recursiveDeleteFails(indexDirectory)) {
            return Status.E_DEPLOY_DELETE_DEPLOY_INDEX_DIR;
        }

        File spellingDirectory = new File(baseDirectory,
                DICTIONARY_SPELLING_DIRECTORY + suffix);
        if (recursiveDeleteFails(spellingDirectory)) {
            return Status.E_DEPLOY_DELETE_DEPLOY_SPELLING_DIR;
        }

        // Extract with temporary directory name

        try {

            byte[] buffer = new byte[256 * 1024];

            ZipInputStream zipInputStream = new ZipInputStream(inputStream);
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                String fileName = zipEntry.getName();
                fileName = fileName.replace(DICTIONARY_INDEX_DIRECTORY,
                        DICTIONARY_INDEX_DIRECTORY + suffix);
                fileName = fileName.replace(DICTIONARY_SPELLING_DIRECTORY,
                        DICTIONARY_SPELLING_DIRECTORY + suffix);
                File entryFile = new File(baseDirectory, fileName);
                File entryDirectory = new File(entryFile.getParent());
                if (!entryDirectory.isDirectory()) {
                    if (!entryDirectory.mkdirs()) {
                        return Status.E_DEPLOY_ZIP_ENTRY_DIR_CREATE;
                    }
                }
                FileOutputStream fileOutputStream = new FileOutputStream(entryFile);
                int length;
                while ((length = zipInputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, length);
                }
                fileOutputStream.close();
                zipEntry = zipInputStream.getNextEntry();
            }
            zipInputStream.closeEntry();
            zipInputStream.close();

        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            return Status.E_EXCEPTION_IO;
        }

        // Switch to new directories

        indexDirectory = new File(baseDirectory, DICTIONARY_INDEX_DIRECTORY);
        if (recursiveDeleteFails(indexDirectory)) {
            return Status.E_DEPLOY_DELETE_INDEX_DIR;
        }
        spellingDirectory = new File(baseDirectory, DICTIONARY_SPELLING_DIRECTORY);
        if (recursiveDeleteFails(spellingDirectory)) {
            return Status.E_DEPLOY_DELETE_SPELLING_DIR;
        }
        File newIndexDirectory = new File(
                baseDirectory, DICTIONARY_INDEX_DIRECTORY + suffix);
        File newSpellingDirectory = new File(
                baseDirectory, DICTIONARY_SPELLING_DIRECTORY + suffix);
        if (!newIndexDirectory.renameTo(indexDirectory)) {
            return Status.E_DEPLOY_RENAME_INDEX_DIR;
        }
        if (!newSpellingDirectory.renameTo(spellingDirectory)) {
            return Status.E_DEPLOY_RENAME_SPELLING_DIR;
        }

        return Status.OK;

    }

    private boolean recursiveDeleteFails(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                try {
                    FileUtils.deleteDirectory(file);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                    return true;
                }
            } else {
                return !file.delete();
            }
        }
        return false;
    }

    private void returnToSearch() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.database_alert_deploy).setCancelable(false)
                .setPositiveButton(R.string.alert_ok, (DialogInterface dialog, int id) -> {
                    Intent returnIntent = new Intent(MainActivity.class.getCanonicalName());
                    startActivity(returnIntent);
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void displayError(Status status) {

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

    private void setListeners() {
        BroadcastReceiver receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                if (!DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    return;
                }

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
            Status status = deployDictionary(inputStream);
            if (!Status.OK.equals(status)) {
                displayError(status);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            displayError(Status.E_EXCEPTION_IO);
        }

        if (!file.delete()) {
            Log.e(TAG, "Error deleting downloaded file");
        }

    }

    public void displayInstalledDictionary() {

        long size = 0;
        long timestamp = 0;

        File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

        File indexDirectory = new File(externalFilesDir, DICTIONARY_INDEX_DIRECTORY);
        if (indexDirectory.canRead() && indexDirectory.isDirectory()) {
            File[] files = indexDirectory.listFiles();
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

        File spellingIndexDirectory = new File(externalFilesDir, DICTIONARY_SPELLING_DIRECTORY);
        if (spellingIndexDirectory.canRead() && spellingIndexDirectory.isDirectory()) {
            File[] files = spellingIndexDirectory.listFiles();
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
            File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            File downloadFile = new File(externalFilesDir, "HunNor-Lucene.zip");
            Uri uri = Uri.fromFile(downloadFile);
            downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            Request request = new Request(
                    Uri.parse(INDEX_URL));
            request.setDestinationUri(uri);
            queueId = downloadManager.enqueue(request);
        }
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
            Log.e(TAG, e.getMessage());
        }
        return result;
    }

}
