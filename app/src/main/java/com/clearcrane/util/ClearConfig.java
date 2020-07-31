/**
 * @author xujifu
 * @copyright clear
 * @date 2014-06-17
 * @description 配置信息
 */
package com.clearcrane.util;

import android.app.TvManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.clearcrane.constant.ClearConstant;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Map;

public class ClearConfig {

    //public static final String STR_NETWORK = "networkXj";
    public static final String STR_NETWORK = "network";

    // 更新时间
    public static final int UPDATE_TIME = 10 * 1000;

    /**
     * 异步加载的数据类型
     */
    public static final int TYPE_IMAGE   = 0;
    public static final int TYPE_JSON = 1;
    public static final int TYPE_IMAGE_SUB_ICON = 3;
    public static final int TYPE_IMAGE_BG = 2;
    public static final int TYPE_IMAGE_BG_SCALE = 4;
    public static final int TYPE_NETWORK_STB = 0;
    public static final int TYPE_LOCAL_STB = 1;
    public static final String SDCARD_URI = Environment.getExternalStorageDirectory().toString();

    public static final String SDCARD_CACHE = Environment.getExternalStorageDirectory() + "/imagecache";
    public static final int MIN_SD_SPACE_NEED = 10;

    public static final int MB = 1024 * 1024;

    public static final String WHOLESALE_CONV = ".cach";

    private static DisplayMetrics mDisplayMetrics = new DisplayMetrics();

    public static final String DEFAULT_MAIN_URI = "http://34.73.89.1/nativevod/now/main.json";
    public static final String BOOT_ADVERTISEMENT_URL = ":8000/backend/getBootAnimation";
    private static final String TAG = "clearconfig";

    public static String MAIN_URI, LOCAL_UPDATE_SERVER, SERVER_URI, WEATHER_URI, CLOUD_UPDATE_SERVER,
            BACKGROUND_VIDEO_URL;
    public static String BACKUP_URI = "/data/local/nativevod/now/main.json";
    public static String serverIp = null;
    public static String SERVER_URI_PREFIX;

    public static int LanguageID = 1;
    public static String roomId = "A101";

    private static int ScreenWidth;

    private static int ScreenHeight;
    public static Context ctx;
    public static boolean autoUpdate = true;

    private static int videoWidth;
    private static int videoHeight;


    private static MyEthernetManager myEthernetManager;

    public static Map<String, Boolean> connectedServerMap = null;
    public static String MAINSERVER_IP;
    public static int mainServerPort;
    public static boolean isNormal = true; //用于判断是盒子端设置ip，还是远程设置ip。默认是盒子。

    public static void init(Context context) {
        // load material from net or local
        isLocalSTB(context, false);
        //add by winter
        //for config mainserver ip
        configFromUdisk(context);
        initdatas(context);
        ctx = context;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        ScreenWidth = wm.getDefaultDisplay().getWidth();
        ScreenHeight = wm.getDefaultDisplay().getHeight();

        videoWidth = ScreenWidth;
        videoHeight = ScreenHeight;

        //add 2018
        //zhe shi zuo shenme yong de !!
//		if (PlatformSettings.getPlatform() == Platform.skyworth
//				|| PlatformSettings.getPlatform() == Platform.skyworth_3RT84) {
//			TvManager mTvManager = (TvManager) context.getSystemService("tv");
//			if (mTvManager != null) {
//				String videoSizeInfo = mTvManager.getVideoSize();
//				String[] infos = videoSizeInfo.split("\\\n");
//				if (infos.length == 4) {
//					videoWidth = Integer.parseInt(infos[2]);
//					videoHeight = Integer.parseInt(infos[3]);
//				}
//			}
//		}
//		
        //add by winter for jiulian
//		myEthernetManager = new MyEthernetManager(ctx);
    }

    public static boolean isEthernetModeDhcp() {
        return myEthernetManager.isEthernetModeDhcp();
    }

    public static boolean changeDhcp2Manual() {
        return myEthernetManager.changeDhcpToManual();
    }

    public static boolean changeToDhcp() {
        return myEthernetManager.changeToDhcp();
    }

    private static void configFromUdisk(Context context) {
        Log.e(TAG, "configFromUdisk");
        String mainUri = initMainUriFromUsb();
        if (mainUri == null || mainUri.equals("")) return;
        SharedPreferences sp = context.getSharedPreferences(STR_NETWORK, Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putString(ClearConstant.MAIN_SERVER, mainUri);
        editor.commit();
        Log.e(TAG, "configFromUdisk2");
    }

    public static String[] WEEK = {"MONDAY", "TUESDAY", "WEDNESDAY", "THRUSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};

    public static String[] MONTH = {"January", "February", "March", "April", "May", "June", "July", "August",
            "September", "October", "November", "December"};

    /**
     * 获取屏幕高度
     */
    // public static int getScreenWidth(Activity activity){
    // activity.getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
    // return mDisplayMetrics.widthPixels;
    // }
    //
    // public static int getScreenHeight(Activity activity){
    // activity.getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
    // return mDisplayMetrics.heightPixels;
    // }
    public static int getScreenWidth() {
        return ScreenWidth;
    }

    public static int getScreenHeight() {
        return ScreenHeight;
    }

    /**
     * 检查是否存在SDCARD
     */
    public static boolean checkSDCard() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    public static void isLocalSTB(Context context, boolean islocal) {
        SharedPreferences settings = context.getSharedPreferences(ClearConfig.STR_NETWORK, Context.MODE_PRIVATE);
        Editor editor = settings.edit();
        editor.putBoolean("localSTB", islocal);
        editor.commit();
    }

    /**
     * 检查是否联网
     */
    public static int checkNetwork(Context context) {
        SharedPreferences settings = context.getSharedPreferences(STR_NETWORK, Context.MODE_PRIVATE);
        if (settings.getBoolean("localSTB", false)) {
            return TYPE_LOCAL_STB;
        }
        return TYPE_NETWORK_STB;
    }

    public static String getJsonUrl(Context context, String url) {
        if (context != null) {
            SharedPreferences settings = context.getSharedPreferences(STR_NETWORK, Context.MODE_PRIVATE);
        }
        url = SERVER_URI_PREFIX + url;
        return url;

    }

    public static void setLanguageIdByIconName(String name) {
        if (name.equalsIgnoreCase("CHZ")) {
            LanguageID = 1;
        } else if (name.equalsIgnoreCase("ENG")) {
            LanguageID = 2;
        }
    }

    public static String getStringByLanguageId(String stringCH, String stringENG) {
        if (LanguageID == 1) {
            return stringCH;
        }
        return stringENG;

    }

    public static String getVersionInfo() {
        PackageManager packageManager = ctx.getPackageManager();
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(ctx.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return packInfo.versionName + "(" + packInfo.versionCode + ")";

    }

    public static String getVersionName() {
        PackageManager packageManager = ctx.getPackageManager();
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(ctx.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return packInfo.versionName;

    }

    public static void initdatas(Context context) {
        SharedPreferences settings = context.getSharedPreferences(STR_NETWORK, Context.MODE_PRIVATE);
        Editor editor = settings.edit();
        // 当前主页地址

        MAINSERVER_IP = settings.getString(ClearConstant.MAIN_SERVER_IP, "");
        if (isNormal) {
            MAIN_URI = settings.getString(ClearConstant.MAIN_SERVER, "");
            String[] server_uri_arr = MAIN_URI.split("/");
            if (server_uri_arr.length > 2) {
                MAINSERVER_IP = server_uri_arr[2];
            }
        } else {
            MAIN_URI = "http://" + MAINSERVER_IP + "/nativevod/now/main.json";
            CLOUD_UPDATE_SERVER = "http://" + MAINSERVER_IP + "/nativevod/now/update/update.xml";
            editor.putString(ClearConstant.MAIN_SERVER, MAIN_URI);
            editor.putString(ClearConstant.CLOUD_UPDATE_SERVER, CLOUD_UPDATE_SERVER);
            editor.commit();
        }
        BACKUP_URI = settings.getString(ClearConstant.BACKUP_SERVER, BACKUP_URI);
        if (MAIN_URI.equalsIgnoreCase("")) {
            // 第一次可以配置文件读取主页地址
            MAIN_URI = DEFAULT_MAIN_URI;

            initMainUri();
            editor.putString(ClearConstant.MAIN_SERVER_IP, MAINSERVER_IP);
            String[] server_uri_arrs = MAIN_URI.split("/");
            MAINSERVER_IP = server_uri_arrs[2];
            editor.putString(ClearConstant.MAIN_SERVER_IP, MAINSERVER_IP);
            Log.e("zxb", "MAINSERVER_IP:" + MAINSERVER_IP);
            editor.putString(ClearConstant.MAIN_SERVER, MAIN_URI);
            editor.putString(ClearConstant.BACKUP_SERVER, BACKUP_URI);
            editor.commit();
        }
        if (MAIN_URI.startsWith("udisk")) {
            // 当为u盘地址，修改主页前缀，但不保存
            String trueUrl = MAIN_URI;
            SERVER_URI = ClearConfig.getTFCard();
            trueUrl = ClearConfig.MAIN_URI.replace("udisk", SERVER_URI);

            SERVER_URI_PREFIX = trueUrl.substring(0, trueUrl.lastIndexOf('/'));

            // LOCAL_UPDATE_SERVER =
            // settings.getString(ClearConstant.LOCAL_UPDATE_SERVER," ");
            // if(LOCAL_UPDATE_SERVER.equalsIgnoreCase(" ")){
            LOCAL_UPDATE_SERVER = SERVER_URI_PREFIX + "/update/update.xml";
            // editor.putString(ClearConstant.LOCAL_UPDATE_SERVER,
            // LOCAL_UPDATE_SERVER);
            // editor.commit();
            // }

            CLOUD_UPDATE_SERVER = settings.getString(ClearConstant.CLOUD_UPDATE_SERVER, " ");
            if (CLOUD_UPDATE_SERVER.equalsIgnoreCase(" ")) {
                CLOUD_UPDATE_SERVER = SERVER_URI + ":8080/ClearUpgradeProxy";
                editor.putString(ClearConstant.CLOUD_UPDATE_SERVER, CLOUD_UPDATE_SERVER);
                editor.commit();
            }
        } else {
            // 网络服务器
            Log.e("zxb", "MAIN_URI:" + MAIN_URI);
            String[] server_uri_arr = MAIN_URI.split("/");
            SERVER_URI = "http://" + server_uri_arr[2];
            SERVER_URI_PREFIX = MAIN_URI.substring(0, MAIN_URI.lastIndexOf('/'));
            serverIp = server_uri_arr[2];
            LOCAL_UPDATE_SERVER = SERVER_URI_PREFIX + "/update/update.xml";
            CLOUD_UPDATE_SERVER = settings.getString(ClearConstant.CLOUD_UPDATE_SERVER, " ");
            if (CLOUD_UPDATE_SERVER.equalsIgnoreCase(" ")) {
                // CLOUD_UPDATE_SERVER = SERVER_URI + ":8080/ClearUpgradeProxy";
                CLOUD_UPDATE_SERVER = SERVER_URI + "/nativevod/now/update/update.xml";
                editor.putString(ClearConstant.CLOUD_UPDATE_SERVER, CLOUD_UPDATE_SERVER);
                editor.commit();
            }

            roomId = settings.getString(ClearConstant.ROOM_ID, roomId);

        }

        if (!MAIN_URI.startsWith("http://")) {
            isLocalSTB(context, true);
        } else {
            isLocalSTB(context, false);
        }

    }

    public static void backupInit(Context context) {
        Log.i("wzz", "backup init");
        if (!BACKUP_URI.startsWith("http://")) {
            isLocalSTB(context, true);
        } else {
            isLocalSTB(context, false);
        }

        if (BACKUP_URI.startsWith("udisk")) {
            // 当为u盘地址，修改主页前缀，但不保存
            String trueUrl = BACKUP_URI;
            SERVER_URI = ClearConfig.getTFCard();
            trueUrl = BACKUP_URI.replace("udisk", SERVER_URI);

            SERVER_URI_PREFIX = trueUrl.substring(0, trueUrl.lastIndexOf('/'));

        } else if (BACKUP_URI.startsWith("/mnt/flash")) {
            // 页面存在flash

            SERVER_URI = "/mnt/flash";
            SERVER_URI_PREFIX = BACKUP_URI.substring(0, BACKUP_URI.lastIndexOf('/'));
        } else if (BACKUP_URI.startsWith("http://")) {
            // 网络服务器
            String[] server_uri_arr = BACKUP_URI.split("/");
            SERVER_URI = "http://" + server_uri_arr[2];
            SERVER_URI_PREFIX = BACKUP_URI.substring(0, BACKUP_URI.lastIndexOf('/'));
            serverIp = server_uri_arr[2];
        } else {
            SERVER_URI = BACKUP_URI;
            SERVER_URI_PREFIX = BACKUP_URI.substring(0, BACKUP_URI.lastIndexOf('/'));
        }

        Log.i("wzz", "SERVER_URI: " + SERVER_URI + ",SERVER_URI_PREFIX:" + SERVER_URI_PREFIX);

    }

    public static int getVersionCode() {
        PackageManager packageManager = ctx.getPackageManager();
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(ctx.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return packInfo.versionCode;

    }

    public static String getMac() {
        NetworkInterface inter = null;
        String macAddr = "";
        try {
            inter = NetworkInterface.getByName("eth0");
            if (inter == null) {
                return "000000000000";
            }
            byte mac[] = inter.getHardwareAddress();
            for (int j = 0; j < mac.length; j++) {
                macAddr += String.format("%02x", mac[j]);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "000000000000";
        }
        return macAddr;
    }

    public static String getLocalIPAddres() throws SocketException {
        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
            NetworkInterface intf = en.nextElement();
            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                InetAddress inetAddress = enumIpAddr.nextElement();
                if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                    return inetAddress.getHostAddress().toString();
                }
            }
        }
        return "0.0.0.0";
    }

    public static void putString(String key, String value) {
        SharedPreferences settings = ctx.getSharedPreferences(ClearConfig.STR_NETWORK, Context.MODE_PRIVATE);
        Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getString(String key, String defaultValue) {
        SharedPreferences settings = ctx.getSharedPreferences(ClearConfig.STR_NETWORK, Context.MODE_PRIVATE);
        return settings.getString(key, defaultValue);
    }

    public static void setAutoUpdate(boolean auto) {
        autoUpdate = auto;
    }

    public static int getScreenWidth(TvManager mTvManager) {
        String videoSizeInfo = mTvManager.getVideoSize();
        String[] infos = videoSizeInfo.split("\\\n");
        if (infos.length < 4)
            return 0;
        else
            return Integer.parseInt(infos[2]);
    }

    public static int getScreenHeight(TvManager mTvManager) {
        String videoSizeInfo = mTvManager.getVideoSize();
        String[] infos = videoSizeInfo.split("\\\n");
        if (infos.length < 4)
            return 0;
        else
            return Integer.parseInt(infos[3]);
    }

    public static int getPlatformWidth(int width) {
        Log.i("getplatfrom", "width:" + (int) (width * 1000000.0 * ScreenWidth / 1280 / 1000000.0));
        return (int) (width * 1000000.0 * ScreenWidth / 1280 / 1000000.0 + 0.5f);
    }

    public static int getPlatformHeight(int height) {
        Log.i("getplatfrom", "height:" + (int) (height * 1000000.0 * ScreenHeight / 720 / 1000000.0));
        return (int) (height * 1000000.0 * ScreenHeight / 720 / 1000000.0 + 0.5f);
    }

    public static int getVideoWidth() {
        return videoWidth;
    }

    public static int getVideoHeight() {
        return videoHeight;
    }

    public static String initMainUri() {
        // String url = "/system/etc/clearconfig.json";
        Log.i(TAG, "read clearconfig.json");
        String url = getTFCard() + "/clearconfig.json";
        try {
            FileInputStream inputStream;
            inputStream = new FileInputStream(url);
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
            String uriData = new String(data, "utf-8");
            if (uriData != null) {
                JSONTokener jsonParser = new JSONTokener(uriData);
                JSONObject object;
                object = (JSONObject) jsonParser.nextValue();
                if (object.has("main_uri")) {
                    MAIN_URI = object.getString("main_uri");
                    Log.i(TAG + "get main uri from config", MAIN_URI);
                }
                if (object.has("backup_uri")) {
                    BACKUP_URI = object.getString("backup_uri");
                    Log.i(TAG + "get main uri from config", MAIN_URI);
                }

                return MAIN_URI;
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }


    public static String initMainUriFromUsb() {
        String result = readMainUriFromUSB();
        if (result == null || result.equals("")) {
            Log.e(TAG, "error usb config null!");
            return null;
        }
        JSONTokener jsonParser = new JSONTokener(result);
        JSONObject jobject;
        try {
            jobject = (JSONObject) jsonParser.nextValue();
            if (jobject.has("main_uri")) {
                String mainuri = jobject.getString("main_uri");
                Log.i(TAG, "get main uri from usb config :" + mainuri);
                return mainuri;
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e(TAG, "get main uri from usb config failure!!");
        }
        return null;
    }


    /*
     * try catch finally
     */

    public static String readMainUriFromUSB() {
        Log.i(TAG, "read ClearConfig.json");
        String filePath = getUsbDirection() + File.separator + ClearConstant.CLEAR_CONFIG_JSON_NAME;
        File file = new File(filePath);
        StringBuffer sb = new StringBuffer();
        String tmpStr = null;
        FileReader fr = null;
        BufferedReader br = null;
        if (file == null || !file.isFile())
            return null;
        try {
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            while ((tmpStr = br.readLine()) != null) {
                sb.append(tmpStr);
            }
            return sb.toString().trim();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        return sb.toString().trim();
    }


    /*
     * winter on 2017/11/24
     * to get USB direction from different devices
     * TODO,FIXME,only one USB mounted
     */
    public static String getUsbDirection() {
        String dir = "";
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            String line;
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                if (line.contains("secure")) continue;
                if (line.contains("asec")) continue;

                if (line.contains("fat")) {
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        dir = dir.concat(columns[1] + "\n");
                    }
                }
//  	            else if (line.contains("fuse")) {
//  	                String columns[] = line.split(" ");
//  	                if (columns != null && columns.length > 1) {
//  	                    dir = dir.concat(columns[1] + "\n");
//  	                }
//  	            }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.e(TAG, "return usb dir " + dir);
        return dir.trim();
    }


    public static String getTFCard() {
        // return "/mnt/mmcblk1/mmcblk1p1";
        // return "/mnt/usb/sda1";//北京张斌
        // return "/mnt/sda/sda1";//海美迪U盘

        // System.getenv(); // 返回的是一个map
        // Map<String, String> map = System.getenv();
        //
        // //遍历出来可以看到最后一项是外置SD卡路径
        //
        // Set<String> set = map.keySet();
        // Iterator<String> key = set.iterator();
        // while(key.hasNext())
        // L.i("123" + key.next());
        //
        // Collection<String> col = map.values();
        // Iterator<String> val = col.iterator();
        // while(val.hasNext())
        // L.i("456" + val.next());

        // 不同的机型获得的会有所不同,目前还没有测试，暂时注释2014/09/23

        String mount = new String();
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            String line;

            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                if (line.contains("secure"))
                    continue;
                if (line.contains("asec"))
                    continue;

                if (line.contains("fat")) {
                    Log.i(TAG, "fat:" + line);
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        mount = mount.concat("" + columns[1]);
                    }
                } /*
                 * else if (line.contains("fuse")) {
                 * Log.i(TAG,"fuse:"+line); String columns[] = line.split(
                 * " "); if (columns != null && columns.length > 1) { mount
                 * = mount.concat(columns[1]); } }
                 */
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.i(TAG, "MOUNT:" + mount);
        Log.i(TAG, "getdirectory:" + Environment.getExternalStorageDirectory());

        return mount;

    }

}
