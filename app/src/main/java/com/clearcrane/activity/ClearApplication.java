package com.clearcrane.activity;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.schedule.DateUtil;
import com.clearcrane.tool.RebootTool;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.CrashHandler;
import com.clearcrane.util.ImageUtil;
import com.clearcrane.util.PlatformSettings;
import com.hisense.hotel.HotelSystemManager;
import com.hisense.hotel.IServicesReadyListener;
import com.tcl.customerapi.ICustomerApi;
import com.tencent.smtt.sdk.QbSdk;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClearApplication extends Application {

    public static final String TAG = "ClearApplication";


    private long uptimecount;
    public String content = "";
    public String contentleft = "";
    public String resourceName = "";
    public String viewType = "";
    public long timeInS = 0;
    public boolean isUpdateApp = false;
    public String curServTime;
    private HotelSystemManager hotelSystemManager;
    /**
     * 0 为不发送， 1 发送
     */
    public int SendLogMode = 0;

    private int dogHungryTime = 0;

    // 插播
    public boolean isInterruptProgram = false;
    // 内容
    public String interruptProgramContent = "";
    public String interruptProgramResourceName = "";
    public long interruptProgramTimeInS = 0; // start time
    public String interruptviewType;
    public String catePath;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ClearConstant.MSG_APP_RESTART:
                    mHandler.removeMessages(ClearConstant.MSG_WATCH_DOG);
                    RebootTool.rebootApp();
                case ClearConstant.MSG_WATCH_DOG:
                    watchDog();
                    mHandler.sendEmptyMessageDelayed(ClearConstant.MSG_WATCH_DOG, 20000);
                    break;
                case 10086:
                    RebootTool.doReboot();
                    break;
            }
        }
    };
    public final static String DATE_ZONE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /**
     * 0 为正常模式, 1为非可用时间黑屏状态, 2为插播状态, 3为计划播状态。
     */
    public int appPageState = 0;

    private static ClearApplication mClearApp = null;
    private boolean socketBroken = true;

    public static ClearApplication instance() {
        return mClearApp;
    }

    public Handler getHandler() {
        return mHandler;
    }

    public ICustomerApi myCustomerApi = null;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myCustomerApi = ICustomerApi.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myCustomerApi = null;
        }
    };

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        CrashHandler.getInstance().init(this);
        mClearApp = this;
        uptimecount = 0;
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_ZONE_FORMAT);
        curServTime = formatter.format(new Date(DateUtil.getCurrentTimeMillSecond()));
        ImageUtil.initImageLoader(getApplicationContext());
        //Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
        dogHungryTime = 0;
        mHandler.sendEmptyMessageDelayed(ClearConstant.MSG_WATCH_DOG, 100);
//        initTbs();

        //更换开机logo开机视频，打开adb
        hotelSystemManager = new HotelSystemManager(this);
        hotelSystemManager.addServiceReadyListener(new IServicesReadyListener() {
            @Override
            public void allServicesReady() {

                Log.d(TAG, "直播键 = " + hotelSystemManager.setKeyLock(170, true));
                Log.d(TAG, "影视键 = " + hotelSystemManager.setKeyLock(131, true));
                Log.d(TAG, "游戏键 = " + hotelSystemManager.setKeyLock(209, true));
                Log.d(TAG, "指南键 = " + hotelSystemManager.setKeyLock(132, true));
                Log.d(TAG, "主页键 = " + hotelSystemManager.setKeyLock(142, true));
                Log.d(TAG, "设置键 = " + hotelSystemManager.setKeyLock(82, true));
                Log.d(TAG, "设置键 = " + hotelSystemManager.setKeyLock(176, true));
                Log.d(TAG, "信号源键 = " + hotelSystemManager.setKeyLock(140, true));
                Log.d(TAG, "信号源键 = " + hotelSystemManager.setKeyLock(178, true));
                Log.d(TAG, "信号源左键 = " + hotelSystemManager.setKeyLock(4307, true));
                Log.d(TAG, "信号源右键 = " + hotelSystemManager.setKeyLock(4308, true));
                Log.d(TAG, "智能截屏键 = " + hotelSystemManager.setKeyLock(2021, true));
                Log.d(TAG, "搜索键 = " + hotelSystemManager.setKeyLock(2022, true));
                if (!hotelSystemManager.isAdbEnabled()) {
                    hotelSystemManager.enableAdb(true);
                }
                String root = readUsbDevice();
                Log.d(TAG, root);
                if (!TextUtils.isEmpty(root)) {
                    Log.d(TAG, "bootLogo : " + hotelSystemManager.setBootLogo("/mnt/usb/sda4/startup_logo/"));
                    Log.d(TAG, "bootAnimation : " + hotelSystemManager.setBootAnimation("/mnt/usb/sda4/third_party_bootanimation/"));
                }

            }
        });
    }

    public HotelSystemManager getHotelSystemManager() {
        return hotelSystemManager;
    }

    public static String readUsbDevice() {
        try {
            StorageManager sm = (StorageManager) mClearApp.getSystemService(STORAGE_SERVICE);
            Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths", null);
            String[] paths = (String[]) getVolumePathsMethod.invoke(sm, null);
            // second element in paths[] is secondary storage path
            return paths.length <= 1 ? null : paths[1];

        } catch (Exception e) {
            Log.e(TAG, "------getSecondaryStoragePath() failed", e);
        }
        return null;
    }


    public void initOtherLib() {
        if (PlatformSettings.platformStr.equals(PlatformSettings.TCL_49)) {
            if (myCustomerApi == null) {
                Intent service = new Intent("action.tvcustomer.api");
                bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
            }
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public void feedWattchDog() {
        dogHungryTime = 0;
    }

    private void watchDog() {
        Log.e(TAG, "watchDog time " + dogHungryTime + " uptime " + uptimecount++);
        if (dogHungryTime > ClearConstant.WATCH_DOG_WAIT_TIMES) {
            RebootTool.doReboot();
            mHandler.sendEmptyMessage(ClearConstant.MSG_APP_RESTART);
        } else {
            dogHungryTime++;
        }
    }


    private UncaughtExceptionHandler uncaughtExceptionHandler = new UncaughtExceptionHandler() {

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            String info = null;
            ByteArrayOutputStream baos = null;
            PrintStream printStream = null;
            try {
                baos = new ByteArrayOutputStream();
                printStream = new PrintStream(baos);
                ex.printStackTrace(printStream);
                byte[] data = baos.toByteArray();
                info = new String(data);
                data = null;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (printStream != null) {
                        printStream.close();
                    }
                    if (baos != null) {
                        baos.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Log.i("Clear", "info:" + info);

            //Intent intent = new Intent(getApplicationContext(),
            //        VoDActivity.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
            //       | Intent.FLAG_ACTIVITY_NEW_TASK);
            //startActivity(intent);
            RebootTool.doReboot();
            //System.exit(0);
        }
    };

    /**
     * @param event         "event":"start","end","skip"
     * @param duration      duration":90(单位s)
     * @param play_type     "点播"/"插播"
     * @param resource_type "视频"/"图文"/"直播"
     * @param resource_name "<<法制讲座>>"
     * @param module        "公告-直播-xx"
     * @return json string
     */
    public String combinatePostParasString(String event, String duration, String play_type, String resource_type, String resource_name, String module) {

        try {
            String mDuration = duration;
            SimpleDateFormat mFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String time;
            time = mFormat.format(new Date(DateUtil.getCurrentTimeMillSecond()));

            if (event.equals("stop")) {
                Log.e("aaaa", "点播duration：" + mDuration);
                if (play_type.equals("点播")) {
                    mDuration = "" + (DateUtil.getCurrentTimeSecond() - timeInS);
                    Log.e("aaaa", "点播duration：" + mDuration);
                } else if (play_type.equals("插播")) {
                    Log.e("aaa", "play_type:" + DateUtil.getCurrentTimeSecond());
                    mDuration = String.valueOf((DateUtil.getCurrentTimeSecond() - interruptProgramTimeInS));
                    Log.e("aaa", "duration:" + mDuration);
                }
            }

            JSONObject mJsonObject = new JSONObject();
            mJsonObject.put("event", event);
            mJsonObject.put("term_id", ClearConfig.getMac());
            //mJsonObject.put("termIP", ClearConfig.getLocalIPAddres());	
            mJsonObject.put("start_time", time);
            mJsonObject.put("duration", mDuration);
            mJsonObject.put("play_type", play_type);
            mJsonObject.put("resource_type", resource_type);
            mJsonObject.put("resource_name", resource_name);
            mJsonObject.put("module", module);
            Log.i("eee", "mJsonObject:" + mJsonObject.toString());
            return mJsonObject.toString();
        } catch (Exception e) {
            Log.i(TAG, "error!!!!");
            e.printStackTrace();
            return null;
        }
    }

    public synchronized boolean isSocketBroken() {
        return socketBroken;
    }


    //利用adbshell实现系统重启
    private void execCommand(String command) throws IOException {

        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec(command);
        try {
            if (proc.waitFor() != 0) {
                System.err.println("exit value = " + proc.exitValue());
            }
        } catch (InterruptedException e) {
            System.err.println(e);
        }
        try {
            execCommand(command);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void sendRebootMsg() {
        mHandler.sendEmptyMessage(10086);
    }


    public void initTbs() {
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                // TODO Auto-generated method stub
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                Log.d("app", " onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
                // TODO Auto-generated method stub
            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(), cb);
    }

}
