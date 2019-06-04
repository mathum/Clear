package com.clearcrane.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.clearcrane.view.MainPageView;
import com.clearcrane.view.VoDBaseView;
import com.clearcrane.view.VoDViewManager;

/**
 * 监听应用的安装和卸载
 *
 * @author SlientLeaves 2016年5月18日 上午10:41:50
 */
public class InStalledReceiver extends BroadcastReceiver {

    public InStalledReceiver() {
        super();
    }

    public InStalledReceiver(Context context) {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) { // install
                String packageName = intent.getDataString().trim();
                packageName = packageName.substring(8);
                Log.i("InStalledReceiver", "install broadcast:" + packageName);

                VoDBaseView topView = VoDViewManager.getInstance().getTopView();
                if (topView instanceof MainPageView) {
                    ((MainPageView) topView).afterInstll2Reresh(packageName);
                }

            }

            if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) { // uninstall
                String packageName = intent.getDataString().trim();
                packageName = packageName.substring(8);
                Log.i("InStalledReceiver", "unstall broadcast:" + packageName);

//			Intent _intent = new Intent();
//			_intent.setClassName("com.clearcrane.vod", "VoDActivity");
//
//			if (context.getPackageManager().resolveActivity(intent, 0) == null) {
//				// 说明系统中不存在这个activity
//				DBHelper.getInstance(context).deleteByPakageName(packageName);
//			} else {
//				VoDBaseView topView = VoDViewManager.getInstance().getTopView();
//				if (topView instanceof MainPageView) {
//					((MainPageView) topView).afterUninstll2Reresh(packageName);
//				}
//			}

                VoDBaseView topView = VoDViewManager.getInstance().getTopView();
                if (topView instanceof MainPageView) {
                    ((MainPageView) topView).afterUninstll2Reresh(packageName);
                }


            }
        } catch (Exception e) {

        }


    }

}
