package net.hunnor.dict.android.task;

import net.hunnor.dict.android.activity.database.DatabaseActivity;
import net.hunnor.dict.android.service.StorageService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CheckLocalFilesTest {

    @Mock
    private StorageService storageService;

    @Mock
    private DatabaseActivity databaseActivity;

    @Test
    public void testCheckForLocalFiles() {

        Map<String, Long> fileStats = new HashMap<>();
        fileStats.put(StorageService.DATE, (long) 1);
        fileStats.put(StorageService.SIZE, (long) 2);
        when(storageService.fileStats(any())).thenReturn(fileStats);

        doAnswer(answer -> {
            long date = answer.getArgument(0);
            assertEquals(1, date);
            long size = answer.getArgument(1);
            assertEquals(2, size);
            return null;
        }).when(databaseActivity).checkLocalFilesCallback(any(), any());

        CheckLocalFiles task = new CheckLocalFiles(databaseActivity, storageService);
        task.execute();

    }

}
