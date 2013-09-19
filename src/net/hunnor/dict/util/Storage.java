package net.hunnor.dict.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;

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
	 *
	 * <p>Returns a directory in the application directory as File
	 *
	 * @param directory the relative path of the directory
	 * @return the directory as File
	 *
	 */
	public File directory(String directory) {
		return new File(appDirectory() + File.separator + directory);
	}

	/**
	 *
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

	/**
	 *
	 * <p>Creates a directory in the application directory
	 *
	 * @param directory the relative path of the directory to create
	 * @return true or false
	 *
	 */
	public boolean createDirectory(String directory) {
		File file = new File(appDirectory + File.separator + directory);
		return file.mkdirs();
	}

	/**
	 *
	 * <p>Deletes a directory in the application directory recursively
	 *
	 * @param directory the relative path of the directory to delete
	 * @return true if the directory was deleted
	 *
	 */
	public boolean deleteDirectory(String directory) {
		try {
			File file = new File(appDirectory + File.separator + directory);
			FileUtils.deleteDirectory(file);
		} catch (IOException exception) {
			return false;
		}
		return true;
	}

	/**
	 *
	 * <p>Renames a directory in the application directory
	 *
	 * @param directory the relative path of the directory to rename
	 * @return true if the directory was renamed
	 *
	 */
	public boolean renameDirectory(String from, String to) {
		File source = new File(appDirectory + File.separator + from);
		File target = new File(appDirectory + File.separator + to);
		return source.renameTo(target);
	}

	/**
	 *
	 * <p>Extracts a file to the specified directory
	 *
	 * @param from the file to extract
	 * @param to the directory to extract into
	 * @return true or false
	 *
	 */
	public boolean unZip(String from, String to) {
		try {
			byte[] buffer = new byte[256 * 1024];
			ZipInputStream zipInputStream = new ZipInputStream(
					new FileInputStream(appDirectory + File.separator + from));
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			while (zipEntry != null) {
				String fileName = zipEntry.getName();
				File newFile = new File(appDirectory + File.separator + to + File.separator + fileName);
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

}
