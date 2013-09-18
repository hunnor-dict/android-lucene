package net.hunnor.dict.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
	 * <p>Returns the application's data directory
	 *
	 * @return The absolute path of the data directory as String
	 */
	public String getAppDirectory() {
		return storage.appDirectory();
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

	/**
	 *
	 * <p>Unzips a file to the specified directory
	 *
	 * @param from The file to unzip
	 * @param to The directory to unzip into
	 * @return true if decompression is successful, false otherwise
	 *
	 */
	public boolean unZip(String from, String to) {
		try {
			byte[] buffer = new byte[256 * 1024];
			ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(from));
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			while (zipEntry != null) {
				String fileName = zipEntry.getName();
				File newFile = new File(to + File.separator + fileName);
				new File(newFile.getParent()).mkdirs();
				FileOutputStream fileOutputStream = new FileOutputStream(newFile); 
				int length;
				while ((length = zipInputStream.read(buffer)) > 0) {
					fileOutputStream.write(buffer, 0, length);
				}
				fileOutputStream.close();
				zipEntry = zipInputStream.getNextEntry();
			}
			zipInputStream.closeEntry();
			zipInputStream.close();
		} catch (FileNotFoundException exception) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 *
	 * <p>Deletes a directory recursively
	 *
	 * @param directory The directory to delete
	 * @return true if the directory is deleted, false otherwise
	 *
	 */
	public boolean deleteDirectory(String directory) {
		try {
			FileUtils.deleteDirectory(new File(directory));
		} catch (IOException exception) {
			return false;
		}
		return true;
	}

}
