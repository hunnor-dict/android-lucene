package net.hunnor.dict.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
*
* Helper methods for networking operations
*
* @author Ádám Z. Kövér
*
*/
public class Network {

	/**
	 *
	 * <p>Returns if Internet connection is available
	 *
	 * @param context
	 * @return true or false
	 *
	 */
	public boolean online(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager)
				context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}

	/**
	 *
	 * <p>Returns HTTP headers for a URL
	 *
	 * @param url
	 * @return a Map of HTTP header fields
	 *
	 */
	public Map<String, List<String>> getHttpHeaderFromUrl(
			Context context, String resource) {
		Map<String, List<String>> result = null;
		if (online(context)) {
			try {
				URL url = new URL(resource);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				if (connection != null) {
					connection.setRequestMethod("HEAD");
					result = connection.getHeaderFields();
					connection.disconnect();
				}
			} catch (MalformedURLException exception) {
			} catch (ProtocolException exception) {
			} catch (IOException exception) {
			}
		}
		return result;
	}

}
