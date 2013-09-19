package net.hunnor.dict.task;

import java.io.File;

import net.hunnor.dict.LuceneConstants;
import net.hunnor.dict.util.Device;
import android.os.AsyncTask;

public class GetUpdate extends AsyncTask<String, Void, String> {

	@Override
	protected String doInBackground(String... params) {
		Device device = new Device();
		if (device.downloadFile(LuceneConstants.INDEX_URL, device.storage().appDirectory() + File.separator + LuceneConstants.INDEX_ZIP)) {
			if (device.storage().unZip(LuceneConstants.INDEX_ZIP, "")) {
				if (device.storage().deleteDirectory(LuceneConstants.INDEX_DIR)) {
					if (device.storage().renameDirectory("hunnor-lucene-index", LuceneConstants.INDEX_DIR)) {
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
