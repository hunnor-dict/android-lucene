package net.hunnor.dict.android.task;

import net.hunnor.dict.android.activity.database.DatabaseActivity;
import net.hunnor.dict.android.service.NetworkService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CheckForUpdatesTest {

    @Mock
    private NetworkService networkService;

    @Mock
    private DatabaseActivity databaseActivity;

    @Test
    public void testCheckForUpdates() {

        try {
            Map<String, String> resourceStats = new HashMap<>();
            resourceStats.put(NetworkService.STATUS, "200");
            resourceStats.put(NetworkService.DATE, "Sat, 02 Jan 2010 12:14:15 GMT");
            resourceStats.put(NetworkService.SIZE, "1024");
            when(networkService.resourceStats(any())).thenReturn(resourceStats);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        doAnswer(answer -> {
            assertEquals("200", answer.getArgument(0));
            assertEquals("Sat, 02 Jan 2010 12:14:15 GMT", answer.getArgument(1));
            assertEquals("1024", answer.getArgument(2));
            return null;
        }).when(databaseActivity).checkForUpdatesCallback(anyString(), anyString(), anyString());

        CheckForUpdates task = new CheckForUpdates(databaseActivity, networkService);
        task.execute("http://localhost");

    }

}
