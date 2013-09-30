package net.hunnor.dict.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.hunnor.dict.LuceneConstants;

import org.apache.commons.io.FileUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

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
		sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
		sb.append(File.separator).append(androidDirectory);
		sb.append(File.separator).append(dataDirectory);
		sb.append(File.separator).append(appDirectory);
		this.appDirectory = sb.toString();
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
	 * <p>Returns the amount of free space in Bytes
	 *
	 * @return the number of free Bytes
	 *
	 */
	public double freeSpace() {
		double free = 0;
		StatFs statFs = new StatFs(
				Environment.getExternalStorageDirectory().getAbsolutePath());
		free = statFs.getBlockSize() * statFs.getAvailableBlocks();
		return free;
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
		return new File(appDirectory + File.separator + directory);
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
	 * @param progressDialog the ProgressDialog to update during extraction
	 * @param context the application context
	 * @return true or false
	 *
	 */
	public boolean extractWithReporting(
			String from,
			String to,
			ProgressDialog progressDialog,
			Context context) {

		long total = 0;
		try {
			ZipInputStream zipInputStream = new ZipInputStream(
					new FileInputStream(appDirectory + File.separator + from));
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			while (zipEntry != null) {
				total = total + zipEntry.getSize();
				zipEntry = zipInputStream.getNextEntry();
			}
			zipInputStream.close();
		} catch (FileNotFoundException exception) {
			return false;
		} catch (IOException e) {
			return false;
		}
		progressDialog.setProgress(0);
		int zipSize = (int) total;
		progressDialog.setMax(zipSize);

		try {
			byte[] buffer = new byte[256 * 1024];
			int extracted = 0;
			ZipInputStream zipInputStream = new ZipInputStream(
					new FileInputStream(appDirectory + File.separator + from));
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			while (zipEntry != null) {
				String fileName = zipEntry.getName();
				File newFile = new File(
						appDirectory + File.separator
						+ to + File.separator + fileName);
				new File(newFile.getParent()).mkdirs();
				FileOutputStream fileOutputStream =
						new FileOutputStream(newFile);
				int length;
				while ((length = zipInputStream.read(buffer)) > 0) {
					fileOutputStream.write(buffer, 0, length);
				}
				fileOutputStream.close();
				extracted = extracted + (int) zipEntry.getSize();
				progressDialog.setProgress(extracted);
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
	 * <p>Downloads a file to the specified directory
	 *
	 * @param resource the remote resource to download
	 * @param progressDialog the ProgressDialog to update during download
	 * @param context the application context
	 * @return true or false
	 *
	 */
	public boolean downloadFileWithReporting(
			String resource,
			ProgressDialog progressDialog,
			Context context) {

		progressDialog.setProgress(0);
		progressDialog.setMax(100);

		HttpURLConnection connection = null;
		InputStream inputStream = null;
		FileOutputStream outputStream = null;
		byte[] buffer = new byte[10240];

		try {
			URL url = new URL(resource);
			connection = (HttpURLConnection) url.openConnection();
			String sizeAsString = connection.getHeaderField("Content-Length");
			int size = Integer.parseInt(sizeAsString);
			progressDialog.setMax(size);
			inputStream = connection.getInputStream();
			File outputFile = directory(LuceneConstants.INDEX_ZIP);
			File outputDirectory = outputFile.getParentFile();
			outputDirectory.mkdirs();
			outputStream = new FileOutputStream(outputFile);
			int part;
			int total = 0;
			while ((part = inputStream.read(buffer, 0, 10240)) != -1) {
				total = total + part;
				outputStream.write(buffer, 0, part);
				progressDialog.setProgress(total);
			}
		} catch (MalformedURLException exception) {
			return false;
		} catch (IOException exception) {
			return false;
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException exception) {
					return false;
				}
			}
		}
		return true;
	}

}
