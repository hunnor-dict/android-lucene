package net.hunnor.dict.util;

import java.io.File;

import android.os.Environment;

/**
 *
 * Helper methods for external storage and file system operations
 *
 * @author Ádám Z. Kövér
 *
 */
public class Storage {

	private static final String androidDirectory = "Android";
	private static final String dataDirectory = "data";

	private String appDirectory;

	public void setAppDirectory(String appDirectory) {
		StringBuilder sb = new StringBuilder();
		sb.append(dataDirectory());
		sb.append(File.separator);
		sb.append(appDirectory);
		this.appDirectory = sb.toString();
	}

	public String appDirectory() {
		return this.appDirectory;
	}

	/**
	 *
	 * <p>Returns if the device's external storage is mounted and readable
	 *
	 * @return true or false
	 *
	 */
	public boolean readable() {
		String storageState = Environment.getExternalStorageState();
		return
				Environment.MEDIA_MOUNTED.equals(storageState) ||
				Environment.MEDIA_MOUNTED_READ_ONLY.equals(storageState);
	}

	/**
	 *
	 * <p>Returns if the device's external storage is mounted and writable
	 *
	 * @return true or false
	 *
	 */
	public boolean writable() {
		String storageState = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(storageState);
	}

	/**
	 * <p>Returns the absolute path of the app's data directory
	 *
	 * @return the absolute path of the data directory as String
	 *
	 */
	private String dataDirectory() {
		StringBuilder sb = new StringBuilder();
		sb.append(Environment.getExternalStorageDirectory().getAbsolutePath())
				.append(File.separator).append(androidDirectory)
				.append(File.separator).append(dataDirectory);
		return sb.toString();
	}

}
