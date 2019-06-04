package com.clearcrane.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

import com.clearcrane.activity.ClearApplication;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class InstallApkUtils {
	private static final String TAG = "InstallApkUtils";

	public static void installAndStartApk(final Context context, final String apkPath) {
		if ((apkPath == null) || (context == null)) {
			return;
		}

		File file = new File(apkPath);
		if (file.exists() == false) {
			return;
		}

		new Thread() {
			public void run() {
				String packageName = getUninstallApkPackageName(context, apkPath);
				if (silentInstall(apkPath)) {
					List<ResolveInfo> matches = findActivitiesForPackage(context, packageName);
					if ((matches != null) && (matches.size() > 0)) {
						ResolveInfo resolveInfo = matches.get(0);
						ActivityInfo activityInfo = resolveInfo.activityInfo;
						startApk(activityInfo.packageName, activityInfo.name);
					}
				}
			};
		}.start();

	}

	public static String getUninstallApkPackageName(Context context, String apkPath) {
		String packageName = null;
		if (apkPath == null) {
			return packageName;
		}

		PackageManager pm = context.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
		if (info == null) {
			return packageName;
		}

		packageName = info.packageName;
		return packageName;
	}

	public static List<ResolveInfo> findActivitiesForPackage(Context context, String packageName) {
		final PackageManager pm = context.getPackageManager();

		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		mainIntent.setPackage(packageName);

		final List<ResolveInfo> apps = pm.queryIntentActivities(mainIntent, 0);
		return apps != null ? apps : new ArrayList<ResolveInfo>();
	}

	public static boolean silentInstall(String apkPath) {
		String cmd1 = "chmod 777 " + apkPath + " \n";
		String cmd2 = "LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install -r " + apkPath + " \n";
		return execWithSID(cmd1, cmd2);
	}

	private static boolean execWithSID(String... args) {
		boolean isSuccess = false;
		Process process = null;
		OutputStream out = null;
		try {
			process = Runtime.getRuntime().exec("su");
			out = process.getOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(out);

			for (String tmp : args) {
				dataOutputStream.writeBytes(tmp);
			}

			dataOutputStream.flush(); // 提交命令
			dataOutputStream.close(); // 关闭流操作
			out.close();

			isSuccess = waitForProcess(process);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return isSuccess;
	}

	public static boolean startApk(String packageName, String activityName) {
		boolean isSuccess = false;

		String cmd = "am start -n " + packageName + "/" + activityName + " \n";
		try {
			Process process = Runtime.getRuntime().exec(cmd);

			isSuccess = waitForProcess(process);
		} catch (IOException e) {
			Log.i(TAG, e.getMessage());
			e.printStackTrace();
		}
		return isSuccess;
	}

	private static boolean waitForProcess(Process p) {
		boolean isSuccess = false;
		int returnCode;
		try {
			returnCode = p.waitFor();
			switch (returnCode) {
			case 0:
				isSuccess = true;
				break;

			case 1:
				break;

			default:
				break;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return isSuccess;
	}

	/**
	 * 通过包名启动app
	 * 
	 * @param packagename
	 */
	public static void doStartApplicationWithPackageName(String packagename,Context context) {

		// 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
		PackageInfo packageinfo = null;
		try {
			packageinfo = context.getPackageManager().getPackageInfo(packagename, 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (packageinfo == null) {
			return;
		}

		// 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
		Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resolveIntent.setPackage(packageinfo.packageName);

		// 通过getPackageManager()的queryIntentActivities方法遍历
		List<ResolveInfo> resolveinfoList = context.getPackageManager().queryIntentActivities(resolveIntent, 0);

		ResolveInfo resolveinfo = resolveinfoList.iterator().next();
		if (resolveinfo != null) {
			// packagename = 参数packname
			String packageName = resolveinfo.activityInfo.packageName;
			// 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
			String className = resolveinfo.activityInfo.name;
			// LAUNCHER Intent
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);

			// 设置ComponentName参数1:packagename参数2:MainActivity路径
			ComponentName cn = new ComponentName(packageName, className);

			intent.setComponent(cn);
			context.startActivity(intent);
		}
	}
	public static boolean installApk2(String apkLocation){
//	        Log.d(TAG, "install apk: " + filepath);
	        try {
	            Intent intent = ClearApplication.instance().getPackageManager()
	                    .getLaunchIntentForPackage("com.clearcrane.install");
	            intent.putExtra("packageName", "com.clearcrane.ims");
	            intent.putExtra("packageUri", "file://mnt/sdcard/smart.apk");
	            ClearApplication.instance().startActivity(intent);
	            ClearApplication.instance().isUpdateApp = false;
	            return true;
	        } catch (Exception e) {
	            Log.w(TAG, "ClearInstall does not exist");
	            try {
	                Intent installIntent = new Intent(Intent.ACTION_VIEW);
	                installIntent.setDataAndType(Uri.parse("file://" + apkLocation), "applicationnd.android.package-archive");
	                installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	                ClearApplication.instance().startActivity(installIntent);
	                ClearApplication.instance().isUpdateApp = false;
	                return true;
	            } catch (Exception e2) {
	                Log.w(TAG, "[" + Log.getStackTraceString(e2) + "]");
	            }
	        }
	        return false;

	}
	
	public static int installApk(String apkLocation) {

		int result = -1;
		
        if (apkLocation == null || apkLocation.trim().length() <= 0) {
            Log.i(TAG, " file is NULL");
            return result;
        }

        String[] progArray = { "pm", "install", "-r", apkLocation };
        Runtime runtime = Runtime.getRuntime();

        Process proc = null;

        try {
            runtime.exec("sh -c cd /sdcard/.yasmin");
            proc = runtime.exec(progArray);

            result = proc.waitFor();
            Log.d(TAG, "pm waitFor=" + result);
            
            
        } catch (Exception e) {
            Log.d(TAG, "[" + Log.getStackTraceString(e) + "]");
        } 


        Log.i(TAG, "end install apk");
        return result;
    }
	/**
	 * 从一个apk文件去获取该文件的版本信息 
	 * 
	 * @param context
	 * @param archiveFilePath
	 * @return
	 */
	public static int getVersionNameFromApk(Context context, String archiveFilePath) {  
	    PackageManager pm = context.getPackageManager();  
	    PackageInfo packInfo = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);  
	    return packInfo.versionCode;  
	}  
}
