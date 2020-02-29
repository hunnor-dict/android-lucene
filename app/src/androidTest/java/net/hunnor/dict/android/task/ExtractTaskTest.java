package net.hunnor.dict.android.task;

import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;

import androidx.test.core.app.ApplicationProvider;

import net.hunnor.dict.android.activity.database.DatabaseActivity;
import net.hunnor.dict.android.activity.main.MainActivity;
import net.hunnor.dict.android.service.StorageService;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExtractTaskTest {

    @Mock
    private StorageService storageService;

    @Mock
    private DatabaseActivity databaseActivity;

    @Mock
    private MainActivity mainActivity;

    @Test
    public void testDatabaseActivity() throws IOException {

        File baseDir = ApplicationProvider.getApplicationContext()
                .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

        File file = new File(baseDir, "file.txt");
        FileUtils.writeByteArrayToFile(file, "foo".getBytes());

        Uri uri = Uri.fromFile(file);

        when(storageService.rename(any(), any())).thenReturn(true);

        doAnswer(answer -> {
            ExtractTaskStatus status = answer.getArgument(0);
            assertEquals(ExtractTaskStatus.OK, status);
            return null;
        }).when(databaseActivity).extractTaskCallback(any());

        doAnswer(answer -> {
            fail();
            return null;
        }).when(mainActivity).extractTaskCallback(any());

        ExtractTask task = new ExtractTask(databaseActivity, storageService);
        task.execute(uri);

    }

    @Test
    public void testDatabaseActivityPathNull() {

        when(storageService.rename(any(), any())).thenReturn(true);

        doAnswer(answer -> {
            ExtractTaskStatus status = answer.getArgument(0);
            assertEquals(ExtractTaskStatus.E_EXCEPTION_IO, status);
            return null;
        }).when(databaseActivity).extractTaskCallback(any());

        doAnswer(answer -> {
            fail();
            return null;
        }).when(mainActivity).extractTaskCallback(any());

        ExtractTask task = new ExtractTask(databaseActivity, storageService);
        Uri uri = Uri.parse("mailto:user@localhost");
        task.execute(uri);

    }

    @Test
    public void testMainActivity() {

        AssetManager assetManager = ApplicationProvider.getApplicationContext().getAssets();
        when(mainActivity.getAssets()).thenReturn(assetManager);

        when(storageService.rename(any(), any())).thenReturn(true);

        doAnswer(answer -> {
            fail();
            return null;
        }).when(databaseActivity).extractTaskCallback(any());

        doAnswer(answer -> {
            ExtractTaskStatus status = answer.getArgument(0);
            assertEquals(ExtractTaskStatus.OK, status);
            return null;
        }).when(mainActivity).extractTaskCallback(any());

        ExtractTask task = new ExtractTask(mainActivity, storageService);
        task.execute();

    }

}
