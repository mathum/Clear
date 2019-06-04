package com.clearcrane.tool;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.clearcrane.activity.ClearApplication;
import com.clearcrane.activity.VoDActivity;
import com.clearcrane.util.PlatformSettings;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Create by Tao on 2018-08-06
 */
public class RebootTool {
    public static final String TAG = "SettingsTool";

    public static void doReboot() {
        switch (PlatformSettings.getPlatform()) {
            // 创维设置
            case BaoFeng_50F1:
            case TCL6800:
            case skyworth_368W:
            case skyworth_368:
            case skyworth_368hs:
            case skyworth_32d5:
            case skyworth_388:
            case tcl_tv338:
            case CH_3500:
            case HAIER_43:
            case BAOFENG_65R4:
                try {
                    execCommand("reboot");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;
            case TCL_A260:
            case TCL_A360:
                Log.e(TAG, "doreboot tcl!");
                if (ClearApplication.instance().myCustomerApi != null) {
                    try {
                        ClearApplication.instance().myCustomerApi.rebootSystem();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                break;
            default:
                qrcodeReboot();
                break;
        }
        try {
            execCommand("reboot");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void execCommand(String command) throws IOException {

        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec(command);
        try {
            if (proc.waitFor() != 0) {
                System.err.println("exit value = " + proc.exitValue());
            }
        } catch (InterruptedException e) {
            System.err.println(e);
        }
    }

    public static void qrcodeReboot() {
        Log.e(TAG, "winter qrcodereboot!");
        String uri = "http://127.0.0.1:19003/index.html?op=reboot";
        try {
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "text/xml");
            conn.setRequestProperty("charset", "utf-8");
            conn.setConnectTimeout(10000);
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                //重启成功
            } else {
                Log.e(TAG, "response code=" + conn.getResponseCode());
            }
        } catch (Exception e) {
            Log.e(TAG, "[" + Log.getStackTraceString(e) + "]");
        }
    }

    public static void rebootApp() {
        //这种重启在应用挂了重启没问题，如果真要重启应用，应该用hardRebootApp
        Intent intent = new Intent(ClearApplication.instance(),
                VoDActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        ClearApplication.instance().startActivity(intent);
        System.exit(0);

    }

    public static void hardRebootApp() {
        Intent intent = ClearApplication.instance().getPackageManager()
                .getLaunchIntentForPackage(ClearApplication.instance().getPackageName());
        PendingIntent restartIntent = PendingIntent.getActivity(ClearApplication.instance(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager) ClearApplication.instance().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // 1秒钟后重启应用
        System.exit(0);
    }

}
