/**
 * @author xujifu
 * @copyright clear
 * @date 2014-06-17
 * @description 获取应用信息类
 */

package com.clearcrane.util;
//AppInfoProvider.java

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

public class AppInfoProvider {
	private PackageManager packageManager;
	//获取一个包管理器
	public AppInfoProvider(Context context){
		packageManager = context.getPackageManager();
	}
	/**
	 *获取系统中所有应用信息，
	 *并将应用软件信息保存到list列表中。
	 **/ 
	public List<AppInfo> getAllApps(){
		List<AppInfo> list = new ArrayList<AppInfo>();
		AppInfo myAppInfo;
		 //获取到所有安装了的应用程序的信息，包括那些卸载了的，但没有清除数据的应用程序 
		List<PackageInfo> packageInfos = packageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		for(PackageInfo info:packageInfos){
			myAppInfo = new AppInfo();
			//拿到包名
			String packageName = info.packageName;
			 //拿到应用程序的信息 
			ApplicationInfo appInfo = info.applicationInfo;
			//拿到应用程序的图标
			Drawable icon = appInfo.loadIcon(packageManager);
			//拿到应用程序的大小
			//long codesize = packageStats.codeSize;
			//Log.i("info", "-->"+codesize);
			//拿到应用程序的程序名
			String appName = appInfo.loadLabel(packageManager).toString();
			myAppInfo.setPackageName(packageName);
			myAppInfo.setAppName(appName);
			myAppInfo.setIcon(icon);
			
			if(filterApp(appInfo)){
				myAppInfo.setSystemApp(false);
				list.add(myAppInfo);
			}else{
				myAppInfo.setSystemApp(true);
			}
			
		}
		return list;
	}
	/**
	 *判断某一个应用程序是不是系统的应用程序，
	 *如果是返回true，否则返回false。
	 */ 
	public boolean filterApp(ApplicationInfo info){
		//有些系统应用是可以更新的，如果用户自己下载了一个系统的应用来更新了原来的，它还是系统应用，这个就是判断这种情况的
		if((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0){
			return true;
		}else if((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0){//判断是不是系统应用
			return true;
		}
		return false;
	}
}