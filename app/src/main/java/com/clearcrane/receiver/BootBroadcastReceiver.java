package com.clearcrane.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.clearcrane.activity.VoDActivity;

import java.util.ArrayList;
import java.util.List;

public class BootBroadcastReceiver extends BroadcastReceiver {

    private PackageManager mPackageManager;
    private final static String TAG = "BootBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        // 开机默认启动launcher
        Log.d(TAG, "onReceive = " + intent.getAction());

        ActivityManager mAm = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String activity_name = mAm.getRunningTasks(1).get(0).topActivity.getClassName();
        Log.d(TAG, "activity name : " + VoDActivity.class.getSimpleName());
        if (activity_name.equals(VoDActivity.class.getSimpleName())) {
            return;
        }
        Intent intent1 = new Intent(context, VoDActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent1);


//        IntentFilter filter = new IntentFilter();
//        filter.addAction("android.intent.action.MAIN");
//        filter.addCategory("android.intent.category.HOME");
//        filter.addCategory("android.intent.category.DEFAULT");
//        ComponentName component = new ComponentName(context.getPackageName(), VoDActivity.class.getName());
//        ComponentName[] components = new ComponentName[]{
//                new ComponentName("com.clearcrane.activity", "com.clearcrane.activity.SplashActivity"), component};
//        mPackageManager = context.getPackageManager();
//        List<String> installedAppList = getInstalledAppList();
//
//        if (installedAppList != null && installedAppList.size() > 0) {
//            clearPackagePreferredActivities(installedAppList);
//        }
//
//
//        mPackageManager.addPreferredActivity(filter, IntentFilter.MATCH_CATEGORY_EMPTY, components, component);

    }


    /**
     * 清除非系统应用开机自启动
     *
     * @param installedAppList
     */
    private final void clearPackagePreferredActivities(List<String> installedAppList) {

        for (String appPackageName : installedAppList)
            mPackageManager.clearPackagePreferredActivities(appPackageName);

    }


    /**
     * 获取系统已经安装app列表
     *
     * @return
     */
    private final List<String> getInstalledAppList() {

        ArrayList<String> resultList = null;

        // Return a List of all packages that are installed on the device.
        List<PackageInfo> packages = mPackageManager.getInstalledPackages(0);

        if (packages.size() > 0) {
            resultList = new ArrayList<String>();
        }

        for (PackageInfo packageInfo : packages) {
            // 判断系统/非系统应用
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) // 非系统应用
            {
                resultList.add(packageInfo.packageName);
            }
        }

        return resultList;
    }
}
