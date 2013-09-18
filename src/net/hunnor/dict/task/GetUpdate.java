package net.hunnor.dict.task;

import java.io.File;

import net.hunnor.dict.LuceneConstants;
import net.hunnor.dict.util.Device;
import android.os.AsyncTask;

public class GetUpdate extends AsyncTask<String, Void, String> {

	@Override
	protected String doInBackground(String... params) {
		Device fileManager = new Device();
		if (fileManager.downloadFile(LuceneConstants.INDEX_URL, fileManager.getAppDirectory() + File.separator + LuceneConstants.INDEX_ZIP)) {
			if (fileManager.unZip(fileManager.getAppDirectory() + File.separator + LuceneConstants.INDEX_ZIP, fileManager.getAppDirectory())) {
				if (fileManager.deleteDirectory(fileManager.getAppDirectory() + File.separator + LuceneConstants.INDEX_DIR)) {
					File f = new File(fileManager.getAppDirectory() + File.separator + "hunnor-lucene-index");
					if (f.renameTo(new File(fileManager.getAppDirectory() + File.separator + LuceneConstants.INDEX_DIR))) {
						return "OK";
					} else {
						return "MV";
					}
				} else {
					return "DEL";
				}
			} else {
				return "ZP";
			}
		} else {
			return "DL";
		}
	}

}

