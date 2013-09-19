package net.hunnor.dict.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 *
 * Handles the device's hardware and operating system
 *
 * @author Ádám Z. Kövér
 *
 */
public class Device {

	public static final String APP_DATA_DIR = "net.hunnor.dict.lucene";

	private Storage storage;

	public Device() {
		storage = new Storage();
		if (storage.readable()) {
			storage.setAppDirectory(APP_DATA_DIR);
		}
	}

	public Storage storage() {
		return storage;
	}

	public boolean online(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager)
				context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}

	/**
	 *
	 * <p>Download a file from a URL to the file system
	 *
	 * @param from The URL to download from
	 * @param to The file to save to
	 * @return true if file download was successful, false otherwise
	 *
	 */
	public boolean downloadFile(String from, String to) {
		try {
			URL url = new URL(from);
			File destination = new File(to);
			FileUtils.copyURLToFile(url, destination);
		} catch (MalformedURLException exception) {
			return false;
		} catch (IOException exception) {
			return false;
		}
		return true;
	}

}
