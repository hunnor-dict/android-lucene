package net.hunnor.dict;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import android.os.Environment;

/**
 *
 * Handles I/O operations and exposes Android's Environment to the application
 *
 * @author Ádám Z. Kövér
 *
 */
public class FileManager {

	public static final String APP_DATA_DIR = "net.hunnor.dict.lucene";

	public enum State {
		STORAGE_NOT_READY,
		STORAGE_MOUNTED_READ_ONLY,
		STORAGE_MOUNTED,

		NET_NOT_AVAILABLE,
		NET_AVAILABLE,

		DOWNLOAD_BAD_URL,
		DOWNLOAD_IO_FAIL,
		DOWNLOAD_SUCCESS;
	}

	/**
	 *
	 * <p>Returns the state of external storage
	 *
	 * @return STORAGE_MOUNTED, STORAGE_MOUNTED_READ_ONLY or STORAGE_NOT_READY
	 *
	 */
	public State getStorageState() {
		String storageState = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(storageState)) {
			return State.STORAGE_MOUNTED;
		}
		if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(storageState)) {
			return State.STORAGE_MOUNTED_READ_ONLY;
		}
		return State.STORAGE_NOT_READY;
	}

	/**
	 *
	 * <p>Returns the application's data directory
	 *
	 * @return The absolute path of the data directory as String
	 */
	public String getAppDirectory() {
		String separator = File.separator;
		StringBuilder appDirectory = new StringBuilder();
		appDirectory.append(Environment.getExternalStorageDirectory().getAbsolutePath());
		appDirectory.append(separator).append("Android").append(separator).append("data");
		appDirectory.append(separator).append(APP_DATA_DIR);
		return appDirectory.toString();
	}

	/**
	 *
	 * <p>Download a file from a URL to the file system
	 *
	 * @param from The URL to download from
	 * @param to The file to save to
	 * @return DOWNLOAD_BAD_URL, DOWNLOAD_IO_FAIL or DOWNLOAD_SUCCESS
	 *
	 */
	public State downloadFile(String from, String to) {
		URL url = null;
		try {
			url = new URL(from);
		} catch (MalformedURLException exception) {
			return State.DOWNLOAD_BAD_URL;
		}
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(to);
			ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
			fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
		} catch (IOException exception) {
			return State.DOWNLOAD_IO_FAIL;
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException exception) {
					return State.DOWNLOAD_IO_FAIL;
				}
			}
		}
		return State.DOWNLOAD_SUCCESS;
	}

}
