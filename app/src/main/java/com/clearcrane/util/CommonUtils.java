///**
// * Created on 2015-11-23
// *
// * @author: wgq
// */
//package com.clearcrane.util;
//
//import java.io.BufferedReader;
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.lang.reflect.Method;
//import java.net.Inet4Address;
//import java.net.InetAddress;
//import java.net.MalformedURLException;
//import java.net.NetworkInterface;
//import java.net.SocketException;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.Enumeration;
//import java.util.List;
//import java.util.Locale;
//import java.util.Random;
//import java.util.zip.GZIPInputStream;
//import java.util.zip.GZIPOutputStream;
//
//import org.apache.http.util.EncodingUtils;
//
//import android.Manifest.permission;
//import android.annotation.TargetApi;
//import android.app.AlarmManager;
//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager;
//import android.content.pm.PackageManager.NameNotFoundException;
//import android.graphics.Color;
//import android.net.wifi.WifiInfo;
//import android.net.wifi.WifiManager;
//import android.os.Build;
//import android.os.Environment;
//import android.os.StatFs;
//import android.text.TextUtils;
//import android.util.Log;
//
//import com.clearcrane.log.L;
//import com.lidroid.xutils.util.LogUtils;
//
//public class CommonUtils {
//
//	private static final String TAG = "CommonUtils";
//
//	/**
//	 * TODO, FIXME, check if cache dir will be cleared by system ...
//	 *
//	 * TODO, FIXME, api 19 以上，会提供 getExternalCacheDirs 返回多个外部存储
//	 *
//	 * @param context
//	 * @param dirName
//	 *            Only the folder name, not full path.
//	 * @return app_cache_path/dirName
//	 */
//	public static String getDiskCacheDir(Context context, String dirName) {
//		String cachePath = null;
//		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
//			File externalCacheDir = context.getExternalCacheDir();
//			if (externalCacheDir != null) {
//				cachePath = externalCacheDir.getPath();
//			}
//		}
//		if (cachePath == null) {
//			File cacheDir = context.getCacheDir();
//			if (cacheDir != null && cacheDir.exists()) {
//				cachePath = cacheDir.getPath();
//			}
//		}
//
//		return cachePath + File.separator + dirName;
//	}
//
//	/**
//	 *
//	 * TODO, FIXME, api 19 以上，会提供 getExternalFilesDirs 返回多个外部存储
//	 *
//	 * @param context
//	 * @param dirName
//	 *            Only the folder name, not full path.
//	 * @return app_cache_path/dirName
//	 */
//	public static String getDiskFilesDir(Context context, String dirName) {
//		String filesPath = null;
//		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
//			File externalFilesDir = context.getExternalFilesDir(null);
//			if (externalFilesDir != null) {
//				filesPath = externalFilesDir.getPath();
//			}
//		}
//		if (filesPath == null) {
//			File filesDir = context.getFilesDir();
//			if (filesDir != null && filesDir.exists()) {
//				filesPath = filesDir.getPath();
//			}
//		}
//
//		return filesPath + File.separator + dirName;
//	}
//
//	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
//	public static long getAvailableSpace(String dir) {
//		File directory = new File(dir);
//		try {
//			final StatFs stats = new StatFs(directory.getPath());
//			int curApiVersion = Build.VERSION.SDK_INT;
//			if (curApiVersion >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//				return (long) stats.getBlockSizeLong() * (long) stats.getAvailableBlocksLong();
//			} else {
//				return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
//			}
//		} catch (Throwable e) {
//			LogUtils.e(e.getMessage(), e);
//			return -1;
//		}
//	}
//
//	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
//	public static long getTotalSpace(String dir) {
//		File directory = new File(dir);
//		try {
//			final StatFs stats = new StatFs(directory.getPath());
//			int curApiVersion = Build.VERSION.SDK_INT;
//			if (curApiVersion >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//				return (long) stats.getBlockSizeLong() * (long) stats.getBlockCountLong();
//			} else {
//				return (long) stats.getBlockSize() * (long) stats.getBlockCount();
//			}
//		} catch (Throwable e) {
//			LogUtils.e(e.getMessage(), e);
//			return -1;
//		}
//	}
//
//	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
//	public static int getAvailableSpacePercent(String dir) {
//		File directory = new File(dir);
//		try {
//			final StatFs stats = new StatFs(directory.getPath());
//			int curApiVersion = Build.VERSION.SDK_INT;
//			if (curApiVersion >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//				return (int) (stats.getAvailableBlocksLong() * 100 / stats.getBlockCountLong());
//			} else {
//				return stats.getAvailableBlocks() * 100 / stats.getBlockCount();
//			}
//		} catch (Throwable e) {
//			LogUtils.e(e.getMessage(), e);
//			return -1;
//		}
//	}
//
//	public static String getID(Context ctx) {
//		/* first check mac */
//		String ID = null;
//		WifiManager wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
//		/* TODO, FIXME, waiting wifi enabled? */
//		if (!wifi.isWifiEnabled()) {
//			wifi.setWifiEnabled(true);
//		}
//
//		WifiInfo info = wifi.getConnectionInfo();
//		ID = info.getMacAddress();
//		if (ID != null && ID.length() > 0)
//			return ID;
//
//		ID = getMacByBusybox();
//		if (ID != null && ID.length() > 0)
//			return ID;
//
//		/* TODO, FIXME, if mac is not available, use some other, SN... */
//
//		return "Unknown";
//	}
//
//	public static String getMacByBusybox() {
//		String result = null;
//		String Mac = null;
//		result = callCmd("busybox ifconfig", "HWaddr");
//
//		if (result == null) {
//			return "0.0.0.0";
//		}
//
//		if (result.length() > 0 && result.contains("HWaddr") == true) {
//			Mac = result.substring(result.indexOf("HWaddr") + 6, result.length() - 1);
//			L.i(TAG, "Mac:" + Mac + " Mac.length: " + Mac.length());
//			Mac = Mac.replaceAll(" ", "");
//			result = Mac;
//		} else {
//			return "0.0.0.0";
//		}
//
//		return result;
//	}
//
//	public static String callCmd(String cmd, String filter) {
//		String result = null;
//		String line = "";
//		try {
//			Process proc = Runtime.getRuntime().exec(cmd);
//			InputStreamReader is = new InputStreamReader(proc.getInputStream());
//			BufferedReader br = new BufferedReader(is);
//
//			while ((line = br.readLine()) != null && line.contains(filter) == false) {
//				// result += line;
//				// L.d(TAG,"cmd res line: "+line);
//			}
//
//			result = line;
//			// L.i(TAG,"result: "+result);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return result;
//	}
//
//	/**
//	 * GZIP decompress
//	 *
//	 * @param data
//	 * @return
//	 * @throws Exception
//	 */
//	public static byte[] gzipDecompress(byte[] data) throws Exception {
////		ByteArrayInputStream bais = new ByteArrayInputStream(data);
////		ByteArrayOutputStream baos = new ByteArrayOutputStream();
////		GZIPInputStream gis = new GZIPInputStream(bais);
////
////		int count;
////		byte buf[] = new byte[Constant.TEMP_BUF_LEN];
////
////		while ((count = gis.read(buf, 0, Constant.TEMP_BUF_LEN)) != -1) {
////			baos.write(buf, 0, count);
////		}
////
////		byte result[] = baos.toByteArray();
////
////		baos.flush();
////		baos.close();
////		gis.close();
////		bais.close();
////
////		return result;
//	}
//
////	public static byte[] gzipDecompress(InputStream is) throws Exception {
////		ByteArrayOutputStream baos = new ByteArrayOutputStream();
////		GZIPInputStream gis = new GZIPInputStream(is);
////
////		int count;
////		byte buf[] = new byte[Constant.TEMP_BUF_LEN];
////
////		while ((count = gis.read(buf, 0, Constant.TEMP_BUF_LEN)) != -1) {
////			baos.write(buf, 0, count);
////		}
////
////		byte result[] = baos.toByteArray();
////
////		baos.flush();
////		baos.close();
////		gis.close();
////
////		return result;
////	}
//
//	public static byte[] gzipCompress(byte[] data) throws Exception {
//		ByteArrayInputStream bais = new ByteArrayInputStream(data);
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
//		gzipCompress(bais, baos);
//
//		byte[] output = baos.toByteArray();
//
//		baos.flush();
//		baos.close();
//		bais.close();
//
//		return output;
//	}
//
////	public static void gzipCompress(InputStream is, OutputStream os) throws Exception {
////
////		GZIPOutputStream gos = new GZIPOutputStream(os);
////
////		int count;
////		byte data[] = new byte[Constant.TEMP_BUF_LEN];
////		while ((count = is.read(data, 0, Constant.TEMP_BUF_LEN)) != -1) {
////			gos.write(data, 0, count);
////		}
////
////		gos.finish();
////
////		gos.flush();
////		gos.close();
////	}
//
//	/**
//	 * 返回的值区间，不包括 maxNum
//	 *
//	 * TODO, FIXME, 是否随机分布够均匀
//	 */
//	public static int getRandomInt(int maxNum) {
//		Random r = new Random();
//		return r.nextInt(maxNum);
//	}
//
//	/* colorStr #ARGB, eg. #FF112233 */
//	public static int getColorFromStr(String colorStr) {
//		int c = Color.WHITE;
//
//		if (colorStr.length() > 8) {
//			try {
//				c = Color.argb(Integer.parseInt(colorStr.substring(1, 3), 16),
//						Integer.parseInt(colorStr.substring(3, 5), 16), Integer.parseInt(colorStr.substring(5, 7), 16),
//						Integer.parseInt(colorStr.substring(7), 16));
//			} catch (NumberFormatException e) {
//				L.w(TAG, "Parse color hex string fail " + colorStr);
//			}
//		} else {
//			try {
//				c = Color.rgb(Integer.parseInt(colorStr.substring(1, 3), 16),
//						Integer.parseInt(colorStr.substring(3, 5), 16), Integer.parseInt(colorStr.substring(5), 16));
//			} catch (NumberFormatException e) {
//				L.w(TAG, "Parse color hex string fail " + colorStr);
//			}
//		}
//		return c;
//
//	}
//
//	public static String getLocalIPAddres() {
//
//		try {
//			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
//				NetworkInterface intf = en.nextElement();
//				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
//					InetAddress inetAddress = enumIpAddr.nextElement();
//					if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
//						return inetAddress.getHostAddress().toString();
//					}
//				}
//			}
//		} catch (SocketException e) {
//			Log.i("iperr", "555555" + e.getMessage());
//		}
//
//		return "0.0.0.0";
//	}
//
//	public static String getVersionName(Context ctx) {
//		PackageManager packageManager = ctx.getPackageManager();
//		PackageInfo packInfo = null;
//		try {
//			packInfo = packageManager.getPackageInfo(ctx.getPackageName(), 0);
//		} catch (NameNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return "Unknown";
//		}
//		return packInfo.versionName;
//
//	}
//
//	public static int getVersionCode(Context ctx) {
//		PackageManager packageManager = ctx.getPackageManager();
//		PackageInfo packInfo = null;
//		try {
//			packInfo = packageManager.getPackageInfo(ctx.getPackageName(), 0);
//		} catch (NameNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			L.e(TAG, "get version code error!");
//			return -1;
//		}
//		return packInfo.versionCode;
//	}
//
//	public static int getVersionCodeForPackageName(Context ctx, String packageName) {
//		PackageManager packageManager = ctx.getPackageManager();
//		PackageInfo packInfo = null;
//		try {
//			packInfo = packageManager.getPackageInfo(packageName, 0);
//		} catch (NameNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			L.e(TAG, "get version code error!");
//			return -1;
//		}
//		return packInfo.versionCode;
//	}
//
//	public static String getVersionNameAndCode(Context ctx) {
//		return getVersionName(ctx) + "(" + getVersionCode(ctx) + ")";
//	}
//
//	public static boolean checkHttpFormat(String url) {
//		if (url == null || url.length() <= "http://".length()) {
//			return false;
//		}
//
//		try {
//			URL tUrl = new URL(url);
//			if (tUrl.getProtocol().equalsIgnoreCase("http")) {
//				return true;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			L.e(TAG, "url format fail: " + url);
//		}
//
//		return false;
//	}
//
//	public static long getCPUInfo() {
//		String cpu1 = readCPUInfo();
//		if (cpu1 == null) {
//			return 0;
//		}
//		cpu1 = cpu1.replaceAll("\\s+ ", " ");
//
//		String[] toks = cpu1.split(" ");
//		if (toks == null || toks.length < 8) {
//			return 0;
//		}
//		long totalCputime1 = Long.parseLong(toks[1]) + Long.parseLong(toks[2]) + Long.parseLong(toks[3])
//				+ Long.parseLong(toks[4]) + Long.parseLong(toks[5]) + Long.parseLong(toks[6]) + Long.parseLong(toks[7]);
//		long idle1 = Long.parseLong(toks[4]);
//
//		try {
//			Thread.sleep(100);
//		} catch (Exception e) {
//		}
//
//		String cpu2 = readCPUInfo();
//		if (cpu2 == null) {
//			return 0;
//		}
//		cpu2 = cpu2.replaceAll("\\s+ ", " ");
//
//		toks = cpu2.split(" ");
//		if (toks == null || toks.length < 8) {
//			return 0;
//		}
//		long totalCputime2 = Long.parseLong(toks[1]) + Long.parseLong(toks[2]) + Long.parseLong(toks[3])
//				+ Long.parseLong(toks[4]) + Long.parseLong(toks[5]) + Long.parseLong(toks[6]) + Long.parseLong(toks[7]);
//		long idle2 = Long.parseLong(toks[4]);
//
//		if (totalCputime2 == totalCputime1) {
//			return 0;
//		}
//
//		long useage = 100 * ((totalCputime2 - totalCputime1) - (idle2 - idle1)) / (totalCputime2 - totalCputime1);
//
//		return useage;
//	}
//
//	private static String readCPUInfo() {
//		FileReader fr = null;
//		BufferedReader buf = null;
//		String cpu = "";
//
//		try {
//			fr = new FileReader("/proc/stat");
//			buf = new BufferedReader(fr);
//
//			try {
//				String line = buf.readLine();
//				while (line != null) {
//					boolean ret = line.startsWith("cpu");
//					if (ret) {
//						cpu = line;
//						break;
//					}
//
//					line = buf.readLine();
//				}
//			} catch (Exception e) {
//				L.e(TAG, "[" + L.getStackTraceString(e) + "]");
//			}
//
//		} catch (Exception e) {
//			L.e(TAG, "[" + L.getStackTraceString(e) + "]");
//		}
//
//		try {
//			buf.close();
//			fr.close();
//		} catch (Exception ex2) {
//			L.e(TAG, "[" + L.getStackTraceString(ex2) + "]");
//		}
//
//		return cpu;
//	}
//
//	public static long getMemTotal() {
//		FileReader fr = null;
//		BufferedReader buf = null;
//		long total = 0;
//
//		try {
//			fr = new FileReader("/proc/meminfo");
//			buf = new BufferedReader(fr);
//
//			try {
//				String line = buf.readLine();
//				while (line != null) {
//					boolean ret = line.startsWith("MemTotal");
//					if (ret) {
//						String[] list = line.split(" ");
//						String kb = list[list.length - 1];
//						String value = list[list.length - 2];
//
//						total = Long.parseLong(value);
//						if (kb.equalsIgnoreCase("kB")) {
//							total = total * 1024;
//						} else if (kb.equalsIgnoreCase("mB")) {
//							total = total * 1024 * 1024;
//						}
//
//						break;
//					}
//
//					line = buf.readLine();
//				}
//			} catch (Exception e) {
//				L.e(TAG, "[" + L.getStackTraceString(e) + "]");
//			}
//
//		} catch (Exception e) {
//			L.e(TAG, "[" + L.getStackTraceString(e) + "]");
//		}
//
//		try {
//			buf.close();
//			fr.close();
//		} catch (Exception ex2) {
//			L.e(TAG, "[" + L.getStackTraceString(ex2) + "]");
//		}
//
//		return total;
//	}
//
//	public static long getMemFree() {
//		FileReader fr = null;
//		BufferedReader buf = null;
//		long total = 0;
//
//		try {
//			fr = new FileReader("/proc/meminfo");
//			buf = new BufferedReader(fr);
//
//			try {
//				String line = buf.readLine();
//				while (line != null) {
//					boolean ret = line.startsWith("MemFree");
//					if (ret) {
//						String[] list = line.split(" ");
//						String kb = list[list.length - 1];
//						String value = list[list.length - 2];
//
//						total = Long.parseLong(value);
//						if (kb.equalsIgnoreCase("kB")) {
//							total = total * 1024;
//						} else if (kb.equalsIgnoreCase("mB")) {
//							total = total * 1024 * 1024;
//						}
//
//						break;
//					}
//
//					line = buf.readLine();
//				}
//			} catch (Exception e) {
//				L.e(TAG, "[" + L.getStackTraceString(e) + "]");
//			}
//
//		} catch (Exception e) {
//			L.e(TAG, "[" + L.getStackTraceString(e) + "]");
//		}
//
//		try {
//			buf.close();
//			fr.close();
//		} catch (Exception ex2) {
//			L.e(TAG, "[" + L.getStackTraceString(ex2) + "]");
//		}
//
//		return total;
//	}
//
//	public static boolean checkPermission(Context context, String permission) {
//		boolean result = false;
//		if (Build.VERSION.SDK_INT >= 23) {
//			try {
//				Class<?> clazz = Class.forName("android.content.Context");
//				Method method = clazz.getMethod("checkSelfPermission", String.class);
//				int rest = (Integer) method.invoke(context, permission);
//				if (rest == PackageManager.PERMISSION_GRANTED) {
//					result = true;
//				} else {
//					result = false;
//				}
//			} catch (Exception e) {
//				result = false;
//			}
//		} else {
//			PackageManager pm = context.getPackageManager();
//			if (pm.checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
//				result = true;
//			}
//		}
//		return result;
//	}
//
//	public static String getDeviceInfo(Context context) {
//		try {
//			org.json.JSONObject json = new org.json.JSONObject();
//			android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
//					.getSystemService(Context.TELEPHONY_SERVICE);
//			String device_id = null;
//			if (checkPermission(context, permission.READ_PHONE_STATE)) {
//				device_id = tm.getDeviceId();
//			}
//			String mac = null;
//			FileReader fstream = null;
//			try {
//				fstream = new FileReader("/sys/class/net/wlan0/address");
//			} catch (FileNotFoundException e) {
//				fstream = new FileReader("/sys/class/net/eth0/address");
//			}
//			BufferedReader in = null;
//			if (fstream != null) {
//				try {
//					in = new BufferedReader(fstream, 1024);
//					mac = in.readLine();
//				} catch (IOException e) {
//				} finally {
//					if (fstream != null) {
//						try {
//							fstream.close();
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					}
//					if (in != null) {
//						try {
//							in.close();
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					}
//				}
//			}
//			json.put("mac", mac);
//			if (TextUtils.isEmpty(device_id)) {
//				device_id = mac;
//			}
//			if (TextUtils.isEmpty(device_id)) {
//				device_id = android.provider.Settings.Secure.getString(context.getContentResolver(),
//						android.provider.Settings.Secure.ANDROID_ID);
//			}
//			json.put("device_id", device_id);
//			return json.toString();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	public static void reStartApp(Context context) {
//		Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
//		PendingIntent restartIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
//		AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//		mgr.set(AlarmManager.RTC, System.currentTimeMillis(), restartIntent); // 定时重启应用，这里时间设置太长可能会导致重启时会跳过SplashActivity，直接启动MainActivity
//		System.exit(0);
//	}
////50
////public static void reStartAppNew(Context context) {
////	Intent intent = new Intent(VoDApplication.getInstance(), SplashActivity.class);
////	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////	VoDApplication.getInstance().startActivity(intent);
////	android.os.Process.killProcess(android.os.Process.myPid());
////}
//
//	public static String getExternalStorageDirectory() {
//		String dir = new String();
//		try {
//			Runtime runtime = Runtime.getRuntime();
//			Process proc = runtime.exec("mount");
//			InputStream is = proc.getInputStream();
//			InputStreamReader isr = new InputStreamReader(is);
//			String line;
//			BufferedReader br = new BufferedReader(isr);
//			while ((line = br.readLine()) != null) {
//				if (line.contains("secure"))
//					continue;
//				if (line.contains("asec"))
//					continue;
//
//				if (line.contains("fat")) {
//					String columns[] = line.split(" ");
//					if (columns != null && columns.length > 1) {
//						dir = dir.concat(columns[1] + "\n");
//					}
//				} else if (line.contains("fuse")) {
//					String columns[] = line.split(" ");
//					if (columns != null && columns.length > 1) {
//						dir = dir.concat(columns[1] + "\n");
//					}
//				}
//			}
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return dir;
//	}
//	  public static String[] getExternalStorageDirectoryNew() {
//	        String dir = "";
//	        try {
//	            Runtime runtime = Runtime.getRuntime();
//	            Process proc = runtime.exec("mount");
//	            InputStream is = proc.getInputStream();
//	            InputStreamReader isr = new InputStreamReader(is);
//	            String line;
//	            BufferedReader br = new BufferedReader(isr);
//
//	            while ((line = br.readLine()) != null) {
//	                Log.v("udisk", "line0 = " + line);
//	                if (line.contains("secure")  && !Platforms.isPhilips5358())
//	                    continue;
//	                if (line.contains("asec")) continue;
//	                if (line.contains("fat") && !( Platforms.isHisense() || Platforms.isHaier()
//							|| Platforms.isPhilips5952()  || Platforms.isPhilips5358())) {
//	                    Log.i("udisk", "line1 = " + line);
//	                    String columns[] = line.split(" ");
//	                    if (columns.length > 1) {
//	                        dir = dir.concat(columns[1] + "\n");
////	                        if (Platforms.isSony_4K_GB()) {
////	                            dir = "/storage/3267-8E6F";
////	                        }
//	                    }
//	                } else if (line.contains("fuse") && line.contains("mnt") && (Platforms.isSkyworth366()
//	                    || Platforms.isPhilips5952() || Platforms.isPhilips5358())) {
//	                    Log.e(TAG, "366w usb");
//	                    Log.i("udisk", "line2 = " + line);
//	                    String columns[] = line.split(" ");
//	                    if (columns.length > 1) {
//	                        dir = columns[1];
//	                        L.i(TAG, "366 dir:" + dir);
//	                    }
//	                } else if (line.contains("vfat") && !(Platforms.isHaier() || Platforms.isPhilips5952()  || Platforms.isPhilips5358())) {
//	                    Log.i("udisk", "line3 = " + line);
//	                    String columns[] = line.split(" ");
//	                    if (columns.length > 1) {
//	                        dir = dir.concat(columns[1] + "\n");
//	                    }
//	                } else if (line.contains("fuse") && line.contains("udisk") && !Platforms.isZhaoge()
//	                        && !Platforms.isSkyworth366()
//	                        && !Platforms.isHisense()) {
//	                    Log.i("udisk", "line4 = " + line);
//	                    String columns[] = line.split(" ");
//	                    if (columns.length > 1) {
//	                        dir = dir.concat(columns[1] + "\n");
//	                    }
//	                }
//
//
//	            }
//	        } catch (IOException e) {
//	            e.printStackTrace();
//	        }
//	        Log.i("udisk", "line5 = " + dir);
//	        return dir.split("\n");
//	    }
//	public static int dip2px(Context context, int dp) {
//		return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
//	}
//
//	public static int px2dip(Context context, int px) {
//		return (int) (px / context.getResources().getDisplayMetrics().density + 0.5f);
//	}
//
//	public static String getStringFromAssets(Context context, String fileName) {
//		try {
//			InputStream in = context.getAssets().open(fileName);
//			// 获取文件的字节数
//			int lenght = in.available();
//			// 创建byte数组
//			byte[] buffer = new byte[lenght];
//			// 将文件中的数据读到byte数组中
//			in.read(buffer);
//			return new String(buffer, "utf-8");// 你的文件的编码
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	/**
//	 * 获取优盘；路径
//	 * @return
//	 */
//	public static List<String> getAllExternalSdcardPath() {
//		List<String> PathList = new ArrayList<String>();
//
//		String firstPath = Environment.getExternalStorageDirectory().getPath();
//		Log.d(TAG,"getAllExternalSdcardPath , firstPath = "+firstPath);
//
//		try {
//			// 运行mount命令，获取命令的输出，得到系统中挂载的所有目录
//			Runtime runtime = Runtime.getRuntime();
//			Process proc = runtime.exec("mount");
//			InputStream is = proc.getInputStream();
//			InputStreamReader isr = new InputStreamReader(is);
//			String line;
//			BufferedReader br = new BufferedReader(isr);
//			while ((line = br.readLine()) != null) {
//				// 将常见的linux分区过滤掉
//				if (line.contains("proc") || line.contains("tmpfs") || line.contains("media") || line.contains("asec") || line.contains("secure") || line.contains("system") || line.contains("cache")
//						|| line.contains("sys") || line.contains("data") || line.contains("shell") || line.contains("root") || line.contains("acct") || line.contains("misc") || line.contains("obb")) {
//					continue;
//				}
//				Log.i(TAG,"line>> :"+ line);
//				// 下面这些分区是我们需要的
//				if (line.contains("fat") || line.contains("fuse") || (line.contains("ntfs"))){
//					// 将mount命令获取的列表分割，items[0]为设备名，items[1]为挂载路径
//					String items[] = line.split(" ");
//					if (items != null && items.length > 1){
//						String path = items[1].toLowerCase(Locale.getDefault());
//						// 添加一些判断，确保是sd卡，如果是otg等挂载方式，可以具体分析并添加判断条件
//						if (path != null && !PathList.contains(path))
//							PathList.add(items[1]);
//					}
//				}
//			}
//		} catch (Exception e){
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		if (!PathList.contains(firstPath)) {
//			PathList.add(firstPath);
//		}
//		return PathList;
//	}
//}
