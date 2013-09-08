package net.hunnor.dict;

import android.os.Environment;

public class FileManager {

	public enum StorageState {
		STORAGE_OTHER,
		STORAGE_MOUNTED_READ_ONLY,
		STORAGE_MOUNTED;
	}

	public StorageState getStorageState() {
		String storageState = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(storageState)) {
			return StorageState.STORAGE_MOUNTED;
		}
		if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(storageState)) {
			return StorageState.STORAGE_MOUNTED_READ_ONLY;
		}
		return StorageState.STORAGE_OTHER;
	}

}
