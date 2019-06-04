package com.clearcrane.tool;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;

import com.clearcrane.platform.skyworthSetPreference;
import com.clearcrane.util.PlatformSettings;

/**
 * Create by Tao on 2018-07-27
 */
public class SettingsTool {
    public static final String TAG = "SettingsTool";

    public static void startSettingsApp(Context context) {

        Intent intent;
        PackageManager packageManager;
        ComponentName componentName;
        switch (PlatformSettings.getPlatform()) {
            // 创维设置
            case skyworth:
            case skyworth_3RT84:
                intent = new Intent(context, skyworthSetPreference.class);
                context.startActivity(intent);
                break;
            case himedia:
                // 海美迪设置
                packageManager = context.getPackageManager();
                intent = packageManager.getLaunchIntentForPackage("com.android.settings");
                context.startActivity(intent);
            case konka32:
                packageManager = context.getPackageManager();
                intent = packageManager.getLaunchIntentForPackage("com.konka.systemsetting");
                context.startActivity(intent);
                break;
            case BaoFeng_50F1:
            case BAOFENG_65R4:
                packageManager = context.getPackageManager();
                intent = packageManager.getLaunchIntentForPackage("com.baofengtv.settings");
                context.startActivity(intent);
                break;
            case tcl:
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setComponent(new ComponentName("com.tcl.settings", "com.tcl.settings.MainActivity"));
                context.startActivity(intent);
                break;
            case tcl_tv338:
            case philips:
                intent = new Intent(Settings.ACTION_SETTINGS);
                context.startActivity(intent);
                break;
            case s905x:
                intent = new Intent();
                componentName = new ComponentName("com.android.tv.settings", "com.android.tv.settings.MainSettings");
                intent.setComponent(componentName);
                context.startActivity(intent);
                break;
            case skyworth_368W:
                intent = new Intent();
                componentName = new ComponentName("com.tianci.setting", "com.tianci.setting.TianciSetting");
                intent.setComponent(componentName);
                context.startActivity(intent);
                break;
            case skyworth_388:
                intent = new Intent();
                componentName = new ComponentName("com.tianci.setting", "com.tianci.setting.TianciSetting");
                intent.setComponent(componentName);
                context.startActivity(intent);
                break;
            case skyworth_32d5:
            	intent = new Intent();
            	componentName = new ComponentName("com.tianci.setting", "com.tianci.setting.TianciSetting");
                intent.setComponent(componentName);
                context.startActivity(intent);
                break;
            case CH_3500:
                intent = new Intent("Changhong.EasySetting");
                intent.setPackage("com.changhong.easysetting");
                context.startService(intent);
                break;
            case A3000H:
                packageManager = context.getPackageManager();
                intent = packageManager.getLaunchIntentForPackage("com.android.hisiliconsetting");
                context.startActivity(intent);
                break;
            case KR_905:
                packageManager = context.getPackageManager();
                intent = packageManager.getLaunchIntentForPackage("com.android.tv.settings");
                context.startActivity(intent);
                break;
            case HAIER_43:
                intent = new Intent();
                intent.setAction("com.haier.settings.intent.action.SettingsService");
                intent.putExtra("command", 0);
                context.startService(intent);
                break;
            case TCL_A260:
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setComponent(new ComponentName("com.tcl.settings", "com.tcl.settings.MainActivity"));
                context.startActivity(intent);
                break;
            default:
                packageManager = context.getPackageManager();
                intent = packageManager.getLaunchIntentForPackage("com.android.settings");
                context.startActivity(intent);
                break;
        }
    }
}
