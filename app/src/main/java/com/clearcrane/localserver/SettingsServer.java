package com.clearcrane.localserver;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.Log;

import com.clearcrane.activity.ClearApplication;
import com.clearcrane.constant.ClearConstant;
import com.clearcrane.localserver.NanoHTTPD.Response.Status;
import com.clearcrane.util.ClearConfig;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//import com.clearcrane.ims.core.ChannelSync;
//import com.clearcrane.ims.platform.MsgKeys;
//import com.clearcrane.ims.platform.PreferenceKeys;
//import com.clearcrane.ims.platform.Register;
//import com.clearcrane.ims.settings.SysConfig;
//import com.clearcrane.ims.settings.SysConfig.Project;
//import com.clearcrane.ims.tools.Common;
//import com.clearcrane.ims.tools.DateUtil;
//import com.clearcrane.ims.tools.L;
//import com.clearcrane.ims.tools.XMLTool;

public class SettingsServer extends NanoHTTPD {

    public final static String TAG = "SettingsServer";
    public static int port = 7777;
    public final static String WEBROOT = "html";
    public static String osVersionStr = null;

    private String netMode = null;
    private String netIP = null;
    private String netNetmask = null;
    private String netGateway = null;
    private String netDNS1 = null;
    private String netDNS2 = null;

    private String lastSyncCommand = null;
    private int lastSyncCommandId = -1;
    // public boolean isReadyToPlay = false;

    private Handler mHandler = new Handler();
    private Runnable startRunnable = new Runnable() {

        @Override
        public void run() {
            mSettingsServer = new SettingsServer(++port);
        }
    };

    /**
     * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
     */
    private static final Map<String, String> MIME_TYPES = new HashMap<String, String>() {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        {
            put("css", "text/css");
            put("htm", "text/html");
            put("html", "text/html");
            put("xml", "text/xml");
            put("java", "text/x-java-source, text/java");
            put("md", "text/plain");
            put("txt", "text/plain");
            put("asc", "text/plain");
            put("gif", "image/gif");
            put("jpg", "image/jpeg");
            put("jpeg", "image/jpeg");
            put("png", "image/png");
            put("mp3", "audio/mpeg");
            put("m3u", "audio/mpeg-url");
            put("mp4", "video/mp4");
            put("ogv", "video/ogg");
            put("flv", "video/x-flv");
            put("mov", "video/quicktime");
            put("swf", "application/x-shockwave-flash");
            put("js", "application/javascript");
            put("pdf", "application/pdf");
            put("doc", "application/msword");
            put("ogg", "application/x-ogg");
            put("zip", "application/octet-stream");
            put("exe", "application/octet-stream");
            put("class", "application/octet-stream");
            put("ts", "text/plain");
            put("m3u8", "application/vnd.apple.mpegURL");
        }
    };

    private static SettingsServer mSettingsServer = null;

    public static SettingsServer instance() {
        if (mSettingsServer == null) {
            mSettingsServer = new SettingsServer(port);
        }
        return mSettingsServer;
    }

    private SettingsServer(int port) {
        super(port);

        try {
        	Log.e("zxb", "settiingsServer启动");
            start();
        } catch (Exception e) {
            Log.e(TAG, "[" + e.getMessage() + "]");
            mHandler.postDelayed(startRunnable, 2000);
        }

        osVersionStr = GetBuildProproperties("ro.product.cleartv.version");
        if (osVersionStr == null) {
            osVersionStr = "0";
        }
    }

    public void destroy() {
        mSettingsServer.stop();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public Response serve(String uri, Method method,
                          Map<String, String> header, Map<String, String> parameters,
                          Map<String, String> files) {
         Log.e("zxb", method + " '" + uri + "' ");
        //
        // Iterator<String> iter2 = header.keySet().iterator();
        // while (iter2.hasNext()) {
        // String value = iter2.next();
        // L.w(TAG, "  HDR: '" + value + "' = '" + header.get(value) + "'");
        // L.w("zlei", "  HDR: '" + value + "' = '" + header.get(value)
        // + "'");
        // }

        Iterator<String> iter = parameters.keySet().iterator();
        while (iter.hasNext()) {
            String value = iter.next();
             Log.e("zxb", "  PRM: '" + value + "' = '" + parameters.get(value)
             + "'");

            if (value.equalsIgnoreCase("getsynctime")) {
                String json = "false";
                return new NanoHTTPD.Response(Status.OK, "text/json", json);
            }

            if (value.equalsIgnoreCase("getinfo")) {
            	Log.e("zxb", "getinfo");
                String json = getSettingsInfo();
                return new NanoHTTPD.Response(Status.OK, "text/json", json);
            }

            if (value.equalsIgnoreCase("getsimpleinfo")) {
            	Log.e("zxb", "getsimpleinfo");
                String json = getSimSettingsInfo();
                return new NanoHTTPD.Response(Status.OK, "text/json", json);
            }

            if (value.equalsIgnoreCase("getnetinfo")) {
                String json = getNetInfo();
                return new NanoHTTPD.Response(Status.OK, "text/json", json);
            }

            if (value.equalsIgnoreCase("getdefaultinfo")) {
                String json = getDefaultInfo();
                return new NanoHTTPD.Response(Status.OK, "text/json", json);
            }

//            if (value.equalsIgnoreCase("setinfo")) {
//                String json = parameters.get(value);
//                String ret = setSettingsInfo(json);
//                return new NanoHTTPD.Response(Status.OK, "text/html", ret);
//            }

            if (value.equalsIgnoreCase("setsimpleinfo")) {
                String json = parameters.get(value);
                String ret = setSimSettingsInfo(json);
                return new NanoHTTPD.Response(Status.OK, "text/html", ret);
            }

            if (value.equalsIgnoreCase("setnetinfo")) {
                String json = parameters.get(value);
                String ret = setNetInfo(json);
                return new NanoHTTPD.Response(Status.OK, "text/html", ret);
            }

            if (value.equalsIgnoreCase("setdefaultinfo")) {
                String json = parameters.get(value);
                String ret = setDefaultInfo(json);
                return new NanoHTTPD.Response(Status.OK, "text/html", ret);
            }

            if (value.equalsIgnoreCase("op")) {
                String op = parameters.get(value);
                String ret = doOperate(op);
                return new NanoHTTPD.Response(Status.OK, "text/html", ret);
            }
        }

        String bytesStr = header.get("range");
        int bytes = 0;
        if (bytesStr != null && bytesStr.trim().length() > 0) {
            bytesStr = bytesStr.substring("bytes=".length(),
                    bytesStr.length() - 1);
            Log.w(TAG, "bytes=" + bytesStr);
            bytes = Integer.parseInt(bytesStr);
        }

        if (uri.equals("/")) {
            uri += "index.html";
        }
        InputStream fis = null;
        try {
            fis = ClearApplication.instance().getAssets().open(WEBROOT + uri);
        } catch (Exception e) {
            Log.w(TAG, "[" + Log.getStackTraceString(e) + "]");
            fis = null;
            return new NanoHTTPD.Response(Status.NOT_FOUND, "text/html", "404");
        }
        Response response = null;
        try {
            Log.w(TAG, "file size=" + fis.available());
            String extention = "";
            int i = uri.lastIndexOf(".");
            if (i > -1 && i < uri.length()) {
                extention = uri.substring(i + 1).toLowerCase();
            }
            String mimeType = MIME_TYPES.get(extention);
            if (mimeType == null || mimeType.trim().length() <= 0) {
                mimeType = "text/plain";
            }

            response = new NanoHTTPD.Response(Status.PARTIAL_CONTENT, mimeType,
                    fis);
            response.addHeader("accept-ranges", "bytes");
            response.addHeader(
                    "content-range",
                    "bytes " + bytes + "-" + (fis.available() - 1) + "/"
                            + fis.available());
            response.addHeader("content-length", "" + (fis.available() - bytes));
            response.addHeader("connection", "close");
        } catch (Exception e) {
            Log.w(TAG, "[" + Log.getStackTraceString(e) + "]");
            response = new NanoHTTPD.Response(Status.NOT_FOUND, "text/html",
                    "404");
        }

        return response;
    }

    private String getSettingsInfo() {
//        SharedPreferences prefs = ClearApplication.instance()
//                .getSharedPreferences(Register.TAG, Context.MODE_PRIVATE);
//        String licence = prefs.getString(PreferenceKeys.PREF_LICENCE, "");
//        String uuid = prefs.getString(PreferenceKeys.PREF_UUID, "");

        String connectStatus;
        if (ClearApplication.instance().isSocketBroken()) {
            connectStatus = "false";
        } else {
            connectStatus = "true";
        }

        String connectServers = "";
        if (ClearConfig.connectedServerMap != null) {
            Iterator<String> iter = ClearConfig.connectedServerMap.keySet()
                    .iterator();
            while (iter.hasNext()) {
                String serverIP = iter.next();
                boolean flag = ClearConfig.connectedServerMap.get(serverIP);
                if (!flag) {
                    continue;
                }
                if (connectServers.equals("")) {
                    connectServers += serverIP;
                } else {
                    connectServers += "," + serverIP;
                }
            }
        }

        JSONObject settingsObj = new JSONObject();
        try {
            settingsObj.put("mainserverip", ClearConfig.MAINSERVER_IP);
            settingsObj.put("mainserverport", ClearConfig.mainServerPort);
//            settingsObj.put("mainserverurl", SysConfig.mainServerUrl);
//            settingsObj.put("logserver", SysConfig.logServer);
//            settingsObj.put("updateserver", SysConfig.updateServer);
//            settingsObj.put("worksection", SysConfig.getWorkSectionStr());
//            settingsObj.put("downloadsection",
//                    SysConfig.getDownloadSectionStr());
//              settingsObj.put("version", ClearConfig.appVersion);
//            settingsObj.put("osversion", osVersionStr);
//            settingsObj.put("deviceid", SysConfig.stbMac);
//            settingsObj.put("ip", SysConfig.stbIP);
//            settingsObj.put("connecttype", SysConfig.connectType);
//            settingsObj.put("uuid", uuid);
//            settingsObj.put("licence", licence);
//              settingsObj.put("connectstatus", connectStatus);
//            settingsObj.put("offsetleft", SysConfig.offsetLeft);
//            settingsObj.put("offsettop", SysConfig.offsetTop);
//            settingsObj.put("offsetwidth", SysConfig.offsetWidth);
//            settingsObj.put("offsetheight", SysConfig.offsetHeight);
//            settingsObj.put("connectservers", connectServers);
//            settingsObj
//                    .put("debugflag", SysConfig.debugFlag ? "true" : "false");
//
//            settingsObj.put("programsync", SysConfig.programSync ? "true"
//                    : "false");
//            settingsObj.put("syncsetid", SysConfig.syncSetId);
//            settingsObj.put("syncmulticastip", SysConfig.syncMulticastIP);
//            settingsObj.put("syncmulticastport", SysConfig.syncMulticastPort);
//            settingsObj.put("syncswitchtimeout", SysConfig.syncSwitchTimeout);
//            String isMaster = "false";
//            if (SysConfig.programSync) {
//                if (ChannelSync.instance().isSyncMaster()) {
//                    isMaster = "true";
//                }
//            }
//            settingsObj.put("syncismaster", isMaster);
//
//            String forceUsb = "true";
//            if (!SysConfig.forceUSB || SysConfig.project == Project.zjcmcc
//                    || SysConfig.project == Project.lygglhyzx
//                    || PlatformSettings.platform == Platform.skyworth_3RT84) {
//                forceUsb = "false";
//            }
//            settingsObj.put("forceusb", forceUsb);
//
//            settingsObj.put("showdownloadinfo",
//                    SysConfig.showDownloadInfo ? "true" : "false");
            // settingsObj.put("qrflag", qrflag);
        } catch (Exception e) {
            Log.w(TAG, "[" + Log.getStackTraceString(e) + "]");
        }

        return settingsObj.toString();
    }

//    private String setSettingsInfo(String jsonStr) {
//        JSONObject obj = null;
//        try {
//            obj = new JSONObject(jsonStr);
//
//            try {
//                ClearConfig.MAINSERVER_IP = obj.getString("mainserverip");
//            } catch (Exception e) {
//            }
//
//            try {
//                ClearConfig.mainServerPort = obj.getInt("mainserverport");
//            } catch (Exception e) {
//            }
            
//            try {
//                SysConfig.logServer = obj.getString("logserver");
//            } catch (Exception e) {
//            }
//
//            try {
//                SysConfig.connectType = obj.getInt("connecttype");
//            } catch (Exception e) {
//            }
            
//            if (SysConfig.connectType == SysConfig.CONNECTTYPE_HTTP) {
//                try {
//                    SysConfig.mainServerUrl = obj.getString("mainserverurl");
//                } catch (Exception e) {
//                }
//            }

//            try {
//                SysConfig.updateServer = obj.getString("updateserver");
//            } catch (Exception e) {
//            }

//            try {
//                SysConfig.offsetLeft = obj.getInt("offsetleft");
//            } catch (Exception e) {
//            }

//            try {
//                SysConfig.offsetTop = obj.getInt("offsettop");
//            } catch (Exception e) {
//            }

//            try {
//                SysConfig.offsetWidth = obj.getInt("offsetwidth");
//            } catch (Exception e) {
//            }
//
//            try {
//                SysConfig.offsetHeight = obj.getInt("offsetheight");
//            } catch (Exception e) {
//            }
//
//            String licence = "";
//            try {
//                licence = obj.getString("licence");
//            } catch (Exception e) {
//            }
//
//            try {
//                SysConfig.debugFlag = obj.getBoolean("debugflag");
//            } catch (Exception e) {
//            }
//
//            try {
//                SysConfig.programSync = obj.getBoolean("programsync");
//            } catch (Exception e) {
//            }
//
//            try {
//                SysConfig.syncSetId = obj.getString("syncsetid");
//            } catch (Exception e) {
//            }
//
//            try {
//                SysConfig.syncMulticastIP = obj.getString("syncmulticastip");
//            } catch (Exception e) {
//            }
//
//            try {
//                SysConfig.syncMulticastPort = obj.getInt("syncmulticastport");
//            } catch (Exception e) {
//            }
//
//            try {
//                SysConfig.syncSwitchTimeout = obj.getInt("syncswitchtimeout");
//            } catch (Exception e) {
//            }
//
//            try {
//                SysConfig.forceUSB = obj.getBoolean("forceusb");
//            } catch (Exception e) {
//            }
//            
//            try {
//                SysConfig.showDownloadInfo = obj.getBoolean("showdownloadinfo");
//            } catch (Exception e) {
//            }

            //将服务器获取到的内容存入到本地
//            SysConfig.dumpValue();

//            SharedPreferences prefs = IMSApplication.instance()
//                    .getSharedPreferences(Register.TAG, Context.MODE_PRIVATE);
//            String licenceOrg = prefs
//                    .getString(PreferenceKeys.PREF_LICENCE, "");
//            if (!licenceOrg.equals(licence)) {
//                Editor edit = prefs.edit();
//                edit.putString(PreferenceKeys.PREF_LICENCE, licence);
//                edit.commit();
//                Register.actionRestart(IMSApplication.instance());
//            }
//
//            return "true";
//        } catch (Exception e) {
//            Log.w(TAG, "[" + Log.getStackTraceString(e) + "]");
//            return "" + e.getMessage();
//        }
//    }

    private String getSimSettingsInfo() {
        JSONObject settingsObj = new JSONObject();
        try {
            settingsObj.put("simmainserverip", ClearConfig.MAINSERVER_IP);
            String tmpHost;
            tmpHost = getHost(ClearConfig.LOCAL_UPDATE_SERVER);
            if (tmpHost == null) {
                tmpHost = ClearConfig.MAIN_URI;
            }
            settingsObj.put("simupdateserver", tmpHost);
            settingsObj.put("simversion", ClearConfig.getVersionCode());
            settingsObj.put("simosversion", osVersionStr);
            settingsObj.put("simdeviceid", ClearConfig.getMac());
            settingsObj.put("simip", ClearConfig.getLocalIPAddres());
            settingsObj.put("simconnectstatus", "true");
        } catch (Exception e) {
            Log.w(TAG, "[" + Log.getStackTraceString(e) + "]");
        }

        return settingsObj.toString();
    }
    //处理远程设置的代码块
    private String setSimSettingsInfo(String jsonStr) {
        JSONObject obj = null;
        try {
            obj = new JSONObject(jsonStr);
            
//            String imsHost = obj.getString("simmainserverip");
            ClearConfig.MAINSERVER_IP = obj.getString("simmainserverip");
            ClearConfig.putString(ClearConstant.MAIN_SERVER_IP, obj.getString("simmainserverip"));
//            ClearConfig.saveByHost(imsHost, imsHost, imsHost);
            ClearConfig.isNormal = false;
            ClearConfig.initdatas(ClearApplication.instance().getBaseContext());
            return "true";
        } catch (Exception e) {
            Log.w(TAG, "[" + Log.getStackTraceString(e) + "]");
            return "" + e.getMessage();
        }
    }

    private String setDefaultInfo(String jsonStr) {
        JSONObject obj = null;
        try {
            obj = new JSONObject(jsonStr);
            String password = obj.getString("defpassword");

            if (password == null || !password.equals("clear!@#ims")) {
                return "password error!!";
            }

//            SysConfig.logServerDef = obj.getString("deflogserver");
//            SysConfig.updateServerDef = obj.getString("defupdateserver");
//            SysConfig.dumpDefValue();

            return "true";
        } catch (Exception e) {
            Log.w(TAG, "[" + Log.getStackTraceString(e) + "]");
            return "" + e.getMessage();
        }
    }

    private String getDefaultInfo() {

        JSONObject settingsObj = new JSONObject();
        try {
//            settingsObj.put("deflogserver", SysConfig.logServerDef);
//            settingsObj.put("defupdateserver", SysConfig.updateServerDef);
        } catch (Exception e) {
            Log.w(TAG, "[" + e.getMessage() + "]");
        }

        return settingsObj.toString();
    }

    private String getNetInfo() {
        String netInfo = null;
//        		Common
//                .readContentFromGet("http://127.0.0.1:19003/index.html?net=get");
        Log.d(TAG, "net info=" + netInfo);
        if (netInfo == null) {
            return "null";
        }
        JSONObject obj = null;
        try {
            obj = new JSONObject(netInfo);

            netMode = obj.getString("mode");
            netIP = obj.getString("ip");
            netNetmask = obj.getString("netmask");
            netGateway = obj.getString("gateway");
            netDNS1 = obj.getString("dns1");
            netDNS2 = obj.getString("dns2");

        } catch (Exception e) {
            Log.w(TAG, "[" + Log.getStackTraceString(e) + "]");
            return "" + e.getMessage();
        }
        return netInfo;
    }

    private String setNetInfo(String jsonStr) {
        JSONObject obj = null;
        try {
            obj = new JSONObject(jsonStr);

            String mode = obj.getString("mode");
            String ip = obj.getString("ip");
            String netmask = obj.getString("netmask");
            String gateway = obj.getString("gateway");
            String dns1 = obj.getString("dns1");
            String dns2 = obj.getString("dns2");

            // L.i(TAG, "mode=" + mode + " ip=" + ip + " netmask=" + netmask +
            // " gateway=" + gateway + " dns1=" + dns1 + " dns2=" + dns2);
            // L.i(TAG, "mode=" + netMode + " ip=" + netIP + " netmask=" +
            // netNetmask + " gateway=" + netGateway + " dns1=" + netDNS1 +
            // " dns2=" + netDNS2);

            if (netMode == null || !netMode.equalsIgnoreCase(mode)
                    || netIP == null || !netIP.equalsIgnoreCase(ip)
                    || netNetmask == null
                    || !netNetmask.equalsIgnoreCase(netmask)
                    || netGateway == null
                    || !netGateway.equalsIgnoreCase(gateway) || netDNS1 == null
                    || !netDNS1.equalsIgnoreCase(dns1) || netDNS2 == null
                    || !netDNS2.equalsIgnoreCase(dns2)) {
                doGet("http://127.0.0.1:19003/index.html?net="
                        + URLEncoder.encode(jsonStr, "UTF-8"));
                netMode = mode;
                netIP = ip;
                netNetmask = netmask;
                netGateway = gateway;
                netDNS1 = dns1;
                netDNS2 = dns2;
            } else {
                Log.i(TAG, "same info");
            }

            return "true";
        } catch (Exception e) {
            Log.w(TAG, "[" + Log.getStackTraceString(e) + "]");
            return "" + e.getMessage();
        }
    }

    private String doOperate(String op) {
        if (op == null || op.trim().length() <= 0) {
        	Log.w(TAG, "op is null");
            return "operate is null";
        }

        
        if (op.equalsIgnoreCase("restart")) {
        	Log.e(TAG,"winter restart!");
            return doRestart() ? "true" : "restart app fail";
        }

        return "operate not support";
    }

//    private boolean doReboot() {
//        PlatformSettings.reboot();
//        return true;
//    }

//    private boolean doPowerOff() {
//        PlatformSettings.sendKey(26);
//        return true;
//    }

    private boolean doRestart() {

        // IMSApplication.instance().getHandler()
        // .sendEmptyMessage(MsgKeys.MSG_APP_RESTART);
        //
        // Register.actionRestart(IMSApplication.instance());
        // UpdateService.actionRestart(IMSApplication.instance());

        // PlatformSettings.reboot();


        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                ClearApplication.instance().getHandler()
                        .sendEmptyMessage(ClearConstant.MSG_APP_RESTART);
            }

        }, 500);

        return true;
    }

    private boolean doQRHide() {
        return doGet("http://127.0.0.1:19003/index.html?qr=hide");
    }

    private boolean doQRShow() {
        return doGet("http://127.0.0.1:19003/index.html?qr=show");
    }

    private boolean doGet(String uri) {
        try {
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "text/xml");
            conn.setRequestProperty("charset", "utf-8");
            conn.setConnectTimeout(10000);
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return true;
            } else {
                Log.w(TAG, "response code=" + conn.getResponseCode());
            }
        } catch (Exception e) {
            Log.w(TAG, "[" + Log.getStackTraceString(e) + "]");
        }
        return false;
    }

    // 获取build.prop中的指定属性
    public String GetBuildProproperties(String PropertiesName) {
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(
                    new File("/system/build.prop")));
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String strTemp = "";
            while ((strTemp = br.readLine()) != null) {// 如果文件没有读完则继续
                if (strTemp.indexOf(PropertiesName) != -1) {
                    br.close();
                    is.close();
                    return strTemp.substring(strTemp.indexOf("=") + 1);
                }
            }
            br.close();
            is.close();
            return null;
        } catch (Exception e) {
            Log.w(TAG, "[" + Log.getStackTraceString(e) + "]");
            return null;
        }
    }

//    private String getSyncTime() {
//        if (!SysConfig.programSync) {
//            return "false";
//        }
//
//        int id = IMSApplication.instance().getSyncCommandID();
//
//        if (lastSyncCommandId != id || lastSyncCommand == null) {
//            lastSyncCommandId = id;
//            Element ele = IMSApplication.instance().getSyncRootElement();
//            if (ele != null) {
//                lastSyncCommand = XMLTool.toString(ele);
//            } else {
//                return "false";
//            }
//        }
//
//        JSONObject jsonObj = new JSONObject();
//        try {
//            jsonObj.put("id", lastSyncCommandId);
//            jsonObj.put("command", lastSyncCommand);
//            jsonObj.put("time", DateUtil.getDate().getTime());
//            jsonObj.put("starttime", ChannelSync.instance().getMasterStartTime());
//        } catch (Exception e) {
//            L.d(TAG, "[" + e.getMessage() + "]");
//            return "false";
//        }
//
//        return jsonObj.toString();
//    }
    
    public String getHost(String str) {
        String tmpHost = null;
        try {
            URL url = new URL(str);
            tmpHost = url.getHost();
        } catch (Exception e) {
            Log.d(TAG, "[" + Log.getStackTraceString(e) + "]");
        }
        return tmpHost;
    }
}
