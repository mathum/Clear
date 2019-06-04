
/**
 * Like c:
 * L.i("int %d, str %s", num, str)
 * 
 * Like java:
 * L.i("int " + num + ", str " + str);
 * 
 * TODO, FIXME:
 * 1. send log/mes to server by http post 
 */

package com.clearcrane.log;

import android.util.Log;

import java.util.MissingFormatArgumentException;

public class L {
	public static final String TAG = "ClearPrisonVoD";
	
	public static String version = null;
	public static String clientID = null;
	
	public static boolean init(String ver, String cid) {
		version = ver;
		clientID = cid;
		return true;
	}

	public static void i(String msg, Object... args) {
		try {
			//if (BuildConfig.DEBUG) 
				Log.i(TAG, String.format(msg, args));
		} catch (MissingFormatArgumentException e) {
			Log.e(TAG, "ClearVoD Log Format Fail", e);
			Log.i(TAG, msg);
		}
	}

	public static void d(String msg, Object... args) {
		try {
			//if (BuildConfig.DEBUG)
				Log.d(TAG, String.format(msg, args));
		} catch (MissingFormatArgumentException e) {
			Log.e(TAG, "ClearVoD Log Format Fail", e);
			Log.d(TAG, msg);
		}
	}

	public static void w(String msg, Object... args) {
		try {
			//if (BuildConfig.DEBUG)
				Log.w(TAG, String.format(msg, args));
		} catch (MissingFormatArgumentException e) {
			Log.e(TAG, "ClearVoD Log Format Fail", e);
			Log.w(TAG, msg);
		}
	}


	public static void e(String msg, Object... args) {
		try {
				Log.e(TAG, String.format(msg, args));
		} catch (MissingFormatArgumentException e) {
			Log.e(TAG, "ClearVoD Log Format Fail", e);
			Log.e(TAG, msg);
		}
	}

	public static void e(String msg, Throwable t) {
		Log.e(TAG, msg, t);
	}

	/**
	 * @param tag
	 *            PLAYER
	 *            APP
	 *            SHOP
	 *            ...
	 *
	 */
	public static void v(String tag, String msg, Object... args) {
		String prefix = version + "," + clientID + "," + tag + ",";
		try {
			//if (BuildConfig.DEBUG)
			Log.i(TAG, prefix + String.format(msg, args));

		} catch (MissingFormatArgumentException e) {
			Log.e(TAG, "ClearVoD Log Format Fail", e);
			Log.i(TAG, msg);
		}
	}
	
	public static String getStackTraceString(Exception e) {
        return Log.getStackTraceString(e);
    }
}
