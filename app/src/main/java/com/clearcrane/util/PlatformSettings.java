package com.clearcrane.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.KeyEvent;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Hashtable;

public class PlatformSettings {

    private final static String TAG = "Platform";
    public final static String COSHIP = "A3000";
    public final static String LETV = "LeTV";
    public final static String SKYWORTH = "Skyworth";
    public final static String SKYWORTH_3RT84 = "Skyworth3RT84";
    public final static String SKYWORTH_368W = "skyworth_368W";
    public final static String SKYWORTH_368 = "skyworth_368";
    public final static String SKYWORTH_368HS = "skyworth_368hs";
    public final static String SKYWORTH_362 = "skyworth_362";
    public final static String SKYWORTH_388 = "Skyworth_388";
    public final static String SKYWORTH_32D5 = "skyworth_32d5";
    public final static String HIMEDIA = "A3000H";
    public final static String S805 = "MBX_S805";
    public final static String S905X = "DS912";
    public final static String JIUUNION = "Hi3798MV100";
    public final static String TCL6800 = "6800";
    public final static String TCLICESCREEN = "TCLIceScreen";
    public final static String HISENSE = "Hisense";
    public final static String PHILIPS = "Philips";
    public final static String KONKA32 = "konka32";
    public final static String BAOFENG_50F1 = "BaoFeng_50F1";
    public final static String BAOFENG_65R4 = "amlogic_BAOFENG_TV AML_T962";
    public final static String TCL = "tcl";
    public final static String TCL_TV338 = "tcl_tv338";
    public final static String A3000H = "Hisilicon_Hi3798MV100";
    public final static String CH_3500 = "CH_3500";
    public final static String KR_905 = "Amlogic_S905X_X9PRO";
    public final static String HAIER_43 = "haier_HRA962_1G_QINGHE";
    public final static String TCL_49 = "TCL Multimedia_TCL Android TV";
    public final static String TCL_A360 = "amlogic_AOSP on p32a6";
    public final static String COSHIP_SETTINGS = "com.android.settings";
    public final static String LETV_SETTINGS = "com.letv.t2.globalsetting";
    public final static String SKYWORTH_SETTINGS = "com.android.settings";
    public final static String CLEAR_SETTINGS = "com.android.settings";
    public final static String MBX_SETTINGS = "com.mbx.settingsmbox";
    public final static String COSHIP_BROWSER = "com.android.browser";
    public final static String LETV_BROWSER = "com.android.letv.browser";
    public final static String SKYWORTH_BROWSER = "com.skyworth.tv_browser";
    public final static String CLEAR_BROWSER = "com.hrtvbic.browser";
    public final static String REMOTE_DESKTOP = "com.splashtop.remote.pad";
    public final static String SKYWORTH_FACTORY = "com.skyworth.tvos.factory";
    public final static String SKYWORTH_3RT84_FWUPDATE = "com.tcLog.update";

    private static String[] musiclist = {"aac", "amr", "mp3", "wav", "wma", "ogg", "flac", "ac3", "mka", "m4a", "mp2",
            "aiff", "ra", "au"};

    private static Hashtable<String, String> musicMap = null;

    public static Platform platform = Platform.uninit;
	public static String platformStr = "";

    public enum Platform{
        unknown, coship, letv, skyworth, skyworth_368W, skyworth_362, skyworth_388,skyworth_32d5,
		skyworth_3RT84, himedia, s805, tcl_icescreen, hisense, uninit, philips, konka32, BaoFeng_50F1,
		tcl, tcl_tv338, s905x, JiuUnion, TCL6800, skyworth_368hs, skyworth_368, CH_3500, A3000H, KR_905,
		HAIER_43,TCL_A260,TCL_A360,BAOFENG_65R4
    }

    public static String getPlatformString(){
        return platformStr;
    }

	public static Platform getPlatform() {
		if (platform != Platform.uninit) {
			return platform;
		}

		String manufacturer = android.os.Build.MANUFACTURER;
		String model = android.os.Build.MODEL;
		String platformID = manufacturer + "_" + model;

		Log.d(TAG, "manufacturer: " + manufacturer + "\n" + "model: " + model);

		if (manufacturer.equalsIgnoreCase("coship") && model.equalsIgnoreCase("n9085i")) {
			platform = Platform.coship;
			platformStr = COSHIP;
			return Platform.coship;
		}

		if (manufacturer.equalsIgnoreCase("MBX") && model.equalsIgnoreCase("AMLOGIC8726MX")) {
			platform = Platform.letv;
			platformStr = LETV;
			return Platform.letv;
		}

		if ((manufacturer.equalsIgnoreCase("MBX") && model.equalsIgnoreCase("m201_512m"))
				|| (manufacturer.equalsIgnoreCase("FONPO"))) {
			platform = Platform.s805;
			platformStr = S805;
			return Platform.s805;
		}

		if (model.toUpperCase().contains("HIMEDIA") || manufacturer.toUpperCase().contains("HIMEDIA")) {
			platform = Platform.himedia;
			platformStr = HIMEDIA;
			return Platform.himedia;
		}

        if (model.toUpperCase().contains("HISENSE") || manufacturer.toUpperCase().contains("HISENSE")) {
            platform = Platform.hisense;
            platformStr = HISENSE;
            return Platform.hisense;
        }
        if (manufacturer.equals("Skyworth")) {
            if (model.equals("Skyworth 8R96 E660E") ||
                    model.equals("Skyworth 8R79 E362W")) {
                platform = Platform.skyworth_362;
                platformStr = SKYWORTH_362;
                return Platform.skyworth_362;
            }

            if (model.contains("8H12Z_E388G")) {
                platform = Platform.skyworth_388;
                platformStr = SKYWORTH_388;
                return Platform.skyworth_388;
            }

            if (model.equals("Skyworth 9R49 E368W")) {
                platform = Platform.skyworth_368W;
                platformStr = SKYWORTH_368W;
                return Platform.skyworth_368W;
            }
            if (model.equals("Skyworth 8H21 D5")){
            	platform = Platform.skyworth_32d5;
            	platformStr = SKYWORTH_32D5;
            	return Platform.skyworth_32d5;
            }
            	
            platform = Platform.skyworth;
            platformStr = SKYWORTH;
            return Platform.skyworth;
        }
        if (model.contains("BUSSINESS") && manufacturer.contains("TALENTS")) {
            platform = Platform.skyworth_3RT84;
            platformStr = SKYWORTH_3RT84;
            return Platform.skyworth_3RT84;
        }

		if (model.contains("Tcl") && model.contains("Amber3")) {
			platform = Platform.tcl_icescreen;
			platformStr = TCLICESCREEN;
			return Platform.tcl_icescreen;
		}
		if (model.equalsIgnoreCase("tv338")&&manufacturer.equals("TCL_PRISON")){
			platform = Platform.tcl_tv338;
			platformStr = TCL_TV338;
			return Platform.tcl_tv338;
		}
		if (model.contains("Konka Android TV 2992") && manufacturer.contains("Konka")) {
			platform = Platform.konka32;
			platformStr = KONKA32;
			Log.d(TAG, "konka32 type");
			return Platform.konka32;
		}
		if (model.contains("BAOFENG_TV")){// MST_6A338") && manufacturer.contains("MStar Semiconductor, Inc.")) {
			Log.e("xb", "BAOFENG_TV MST_6A338");
			platform = Platform.BaoFeng_50F1;
			platformStr = BAOFENG_50F1;
			Log.d("xb", "BAOFENG type");
			return Platform.BaoFeng_50F1;
		}
		if (model.contains("TV628") && manufacturer.contains("KTC")) {
			Log.i(TAG, "is philips");
			platform = Platform.philips;
			platformStr = PHILIPS;
			return Platform.philips;
		}
		if (model.equals("Generic Android on mt5882") && manufacturer.equals("MTK")) {
			platform = Platform.tcl;
			platformStr = TCL;
			return Platform.tcl;
		}
		if(model.equalsIgnoreCase(S905X) && manufacturer.equalsIgnoreCase("Amlogic")){
			platform = Platform.s905x;
			platformStr = S905X;
			return Platform.s905x;
		}
		
		if(manufacturer.equalsIgnoreCase("Unionman")){
			platform = Platform.JiuUnion;
			platformStr = JIUUNION;
			return Platform.JiuUnion;
		}

		if(model.contains(TCL6800)){
			platform = Platform.TCL6800;
			platformStr = TCL6800;
			return Platform.TCL6800;
		}

        if (platformID.equals(SKYWORTH_368)) {
            platform = Platform.skyworth_368;
            platformStr = SKYWORTH_368;
            return Platform.skyworth_368;
        }
        if (platformID.equals(SKYWORTH_368HS)) {
            platform = Platform.skyworth_368hs;
            platformStr = SKYWORTH_368HS;
            return Platform.skyworth_368hs;
        }
        if (model.equalsIgnoreCase("ChangHong Android TV") && manufacturer.equals("ChangHong")) {
            platform = Platform.CH_3500;
            platformStr = CH_3500;
            return Platform.CH_3500;
        }
        if(manufacturer.contains("Hisilicon") && model.contains("Hi3798MV100")){
            platform = Platform.A3000H;
            platformStr = A3000H;
            return Platform.A3000H;
        }
        if (platformID.equals(KR_905)) {
            platform = Platform.KR_905;
            platformStr = KR_905;
            return Platform.KR_905;
        }
        if (platformID.equals(HAIER_43)){
            platform = Platform.HAIER_43;
            platformStr = HAIER_43;
            return  Platform.HAIER_43;
        }
        if(platformID.equals(TCL_49)){
            platform = Platform.TCL_A260;
            platformStr = TCL_49;
            return  Platform.TCL_A260;
        }
        if(platformID.equals(TCL_A360)){
        	platform = Platform.TCL_A360;
        	platformStr = TCL_A360;
        	return Platform.TCL_A360;
        	
        }
        if(platformID.equals(BAOFENG_65R4)){
            platform = Platform.BAOFENG_65R4;
            platformStr = BAOFENG_65R4;
            return  Platform.BAOFENG_65R4;
        }

        platform = Platform.unknown;
        platformStr = model + "_" + manufacturer;
        platformStr = platformStr.replace(" ", "");
        return Platform.unknown;
    }

    public static String getPlatformName() {
        switch (platform) {
            case coship:
                return COSHIP;
            case himedia:
                return HIMEDIA;
            case letv:
                return LETV;
            case skyworth_368W:
                return SKYWORTH_368W;
            case skyworth:
                return SKYWORTH;
            case skyworth_388:
                return SKYWORTH_388;
            case s805:
                return S805;
            case tcl_icescreen:
                return TCLICESCREEN;
            case philips:
                return PHILIPS;
            case konka32:
                return KONKA32;
            case BaoFeng_50F1:
                return BAOFENG_50F1;
            case tcl:
                return TCL;
            case s905x:
                return S905X;
            case JiuUnion:
                return JIUUNION;
            default:
                return "unknown";
        }
    }

	public static void init(Context ctx) {
		if (musicMap == null) {
			musicMap = new Hashtable<String, String>();
			for (int i = 0; i < musiclist.length; i++) {
				musicMap.put(musiclist[i], "1");
			}
		}

		if (platform != Platform.uninit) {
			Log.d(TAG, "inited");
			return;
		}

		if (platform == Platform.uninit) {
			getPlatform();
		}
	}

	public static String getSettingsPackage() {
		if (platform == Platform.coship) {
			return COSHIP_SETTINGS;
		} else if (platform == Platform.letv) {
			return LETV_SETTINGS;
		} else if (platform == Platform.skyworth) {
			return SKYWORTH_SETTINGS;
		} else if (platform == Platform.s805) {
			return MBX_SETTINGS;
		} else {
			return CLEAR_SETTINGS;
		}
	}

	public static boolean isMusic(String extention) {
		if (extention == null || extention.length() == 0) {
			Log.w(TAG, "wrong extention");
			return false;
		}
		if (musicMap == null) {
			musicMap = new Hashtable<String, String>();
			for (int i = 0; i < musiclist.length; i++) {
				musicMap.put(musiclist[i], "1");
			}
		}
		String res = musicMap.get(extention);
		if (res != null && res.equals("1")) {
			return true;
		}
		return false;
	}

	public static String getLocalIPAddres() throws SocketException {
		for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
			NetworkInterface intf = en.nextElement();
			for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
				InetAddress inetAddress = enumIpAddr.nextElement();
				if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
					return inetAddress.getHostAddress().toString();
				}
			}
		}
		return "0.0.0.0";
	}

	public static boolean launchApp(Context ctx, String packageName) {
		if(getPlatform() == Platform.BaoFeng_50F1 && packageName.endsWith("com.bftv.usermanual")){
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setComponent(new ComponentName("com.baofengtv.customersetting","com.baofengtv.customersetting.MainActivity"));
			ctx.startActivity(intent);
			return true;
		}
		PackageManager packageManager = ctx.getPackageManager();
		Intent intent = null;

		intent = packageManager.getLaunchIntentForPackage(packageName);

		if (intent == null) {
			Log.w(TAG, "no such " + packageName + " found");
			return false;
		}
		ctx.startActivity(intent);

		if (REMOTE_DESKTOP.equals(packageName)) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					Log.i(TAG, "wait 3.6s");
					try {
						Thread.sleep(3600);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Common.readContentFromGet(
							"http://127.0.0.1:19003/index.html?keycode=" + KeyEvent.KEYCODE_DPAD_CENTER);
				}

			}).start();
		}

		return true;
	}
}
