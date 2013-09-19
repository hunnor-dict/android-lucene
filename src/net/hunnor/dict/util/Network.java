package net.hunnor.dict.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
*
* Helper methods for networking operations
*
* @author Ádám Z. Kövér
*
*/
public class Network {

	/**
	 *
	 * <p>Returns if Internet connection is available
	 *
	 * @param context
	 *
	 * @return true or false
	 *
	 */
	public boolean online(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager)
				context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}

}
