package net.hunnor.dict.task;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;

public class DatabaseDialogYes extends Activity implements DialogInterface.OnClickListener {

	@Override
	public void onClick(DialogInterface dialog, int which) {
		try {
			startActivity(new Intent("net.hunnor.dict.ACTIVITY_DATABASE"));
		} catch (ActivityNotFoundException e) {
		}
	}

}
