package net.hunnor.dict;

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
import android.os.Environment;

/**
 *
 * Handles I/O operations and exposes Android's Environment to the application
 *
 * @author Ádám Z. Kövér
 *
 */
public class Device {

	public static final String APP_DATA_DIR = "net.hunnor.dict.lucene";

	/**
	 *
	 * <p>Returns if external storage is readable
	 *
	 * @return true if external storage is readable, false otherwise
	 *
	 */
	public boolean storageReadable() {
		String storageState = Environment.getExternalStorageState();
		return (Environment.MEDIA_MOUNTED.equals(storageState) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(storageState));
	}

	public boolean deviceOnline(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}

	/**
	 *
	 * <p>Returns if external storage is writable
	 *
	 * @return true if external storage is writable, false otherwise
	 *
	 */
	public boolean storageWriteable() {
		String storageState = Environment.getExternalStorageState();
		return (Environment.MEDIA_MOUNTED.equals(storageState));
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
