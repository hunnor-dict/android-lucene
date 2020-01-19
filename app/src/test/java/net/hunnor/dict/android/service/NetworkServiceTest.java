package net.hunnor.dict.android.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NetworkServiceTest {

    @Mock
    private HttpURLConnection connection;

    @Mock
    private URL url;

    @Test
    public void testConnectionNull() throws IOException {

        when(url.openConnection()).thenReturn(null);

        NetworkService networkService = new NetworkService();
        Map<String, String> stats = networkService.resourceStats(url);

        assertNotNull(stats);
        assertTrue(stats.isEmpty());

    }

    @Test
    public void testConnectionNotFound() throws IOException {

        when(connection.getResponseCode()).thenReturn(404);
        when(url.openConnection()).thenReturn(connection);

        NetworkService networkService = new NetworkService();
        Map<String, String> stats = networkService.resourceStats(url);

        assertNotNull(stats);
        assertEquals(1, stats.size());

        assertEquals("404", stats.get(NetworkService.STATUS));

    }

    @Test
    public void testConnectionOk() throws IOException {

        when(connection.getResponseCode()).thenReturn(200);
        when(connection.getHeaderField("Last-Modified")).thenReturn("LAST MODIFIED");
        when(connection.getHeaderField("Content-Length")).thenReturn("1024");
        when(url.openConnection()).thenReturn(connection);

        NetworkService networkService = new NetworkService();
        Map<String, String> stats = networkService.resourceStats(url);

        assertNotNull(stats);
        assertEquals(3, stats.size());

        assertEquals("200", stats.get(NetworkService.STATUS));
        assertEquals("LAST MODIFIED", stats.get(NetworkService.DATE));
        assertEquals("1024", stats.get(NetworkService.SIZE));

    }

}
