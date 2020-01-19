package net.hunnor.dict.android.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class NetworkService {

    public static final String STATUS = "status";

    public static final String DATE = "date";

    public static final String SIZE = "size";

    public Map<String, String> resourceStats(URL url) throws IOException {

        Map<String, String> result = new HashMap<>();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        if (connection != null) {

            connection.setRequestMethod("HEAD");
            int status = connection.getResponseCode();
            result.put(STATUS, Integer.toString(status));

            if (HttpURLConnection.HTTP_OK == status) {
                result.put(DATE, connection.getHeaderField("Last-Modified"));
                result.put(SIZE, connection.getHeaderField("Content-Length"));
                connection.disconnect();
            }

        }

        return result;

    }

}
