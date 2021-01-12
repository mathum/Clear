package com.clearcrane.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.clearcrane.apkupdate.UpdateService;
import com.clearcrane.constant.ClearConstant;
import com.clearcrane.constant.clearProject;
import com.clearcrane.localserver.SettingsServer;
import com.clearcrane.log.ClearLog;
import com.clearcrane.pushmessage.FloatViewService;
import com.clearcrane.pushmessage.pushMsgService;
import com.clearcrane.receiver.InStalledReceiver;
import com.clearcrane.service.PerfectPlayerService;
import com.clearcrane.tool.RebootTool;
import com.clearcrane.tool.SettingsTool;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.view.MainPageView;
import com.clearcrane.view.MyProgressBarView;
import com.clearcrane.view.VoDViewManager;
import com.clearcrane.vod.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class VoDActivity extends Activity implements OnClickListener {

    private View mSettingsView;
    private EditText mAuthServerText, mUpdateServerText, mRoomIdText;
    private TextView mVersionText, mIpText;
    private Button mTextSaveButton, mSystemSettingButton;
    private Spinner rebootEnableSpinner, rebootHourSpinner, rebootMinuteSpinner;
    private Dialog mAlertDialog = null;
    private SharedPreferences mPrefs;
    private ImageView ivLoading = null;
    private boolean loadingAnimRun = false;
    private static int waitPeriod = 3000;
    private Context ctx;
    private final static String SETTINGSKEYS = "0731";
    private final static String SETTINGSKEYS_BAOFENG = "1919202021222122";
    private final static String APPKEYS = "6321";
    // 测试用组合按键
    private final static String SHOTSCREEN = "122";// 截图快捷键
    private String mSettingsKeys = "";
    private String mAppKeys = "";
    // public EggsSurfaceView mEggsView = null;
    // private ProgressDialog mProgressDialog;
    public boolean rebootEnable = false;
    public int rebootHour, rebootMinute;
    private BroadcastReceiver externalStorageReceiver = null;
    private BroadcastReceiver connectionReceiver = null;

    public static final String TAG = "VoDActivity";
    private int indexOfBitmapDrawables = 0;
    private final int WAIT_INIT_MSG = 0;
    private final int CHECK_NET_MSG = 1;
    private final int NET_READY_MSG = 2;
    private final int MAINPAGE_LOAD_FINISH_MSG = 3;
    private final int MAINPAGE_LOAD_ANIM_MSG = 4;
    private final int PHILIPS_SERVICES = 4;
    private ArrayList<BitmapDrawable> bitmapDrawables = new ArrayList<BitmapDrawable>();
    public ClearApplication mApp;
    private InStalledReceiver mInStalledReceiver;
    public SharedPreferences moduleGroupSharePre;
    // private UpdateManagerService mUpdateManagerService;
    // private IMyBinder mUpdateServiceBinder;
    private int re = -1;
    // 0 for vod 1 for ims
    public int activityMode = 0;
    public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000; //需要自己定义标志

    public void setActivityMode(int mode) {
        this.activityMode = mode;
    }

    private long times = 0;
    private long lasttimes = 0;
    //public static boolean forbidKeycode = false;

    private long getOffsetTimes() {
        return System.currentTimeMillis() / 1000 - times;
    }

    public int getActivityMode() {
        return this.activityMode;
    }

    Runnable run = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                Runtime.getRuntime().exec("logcat -f /data/winter/ll.log");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        onRestoreInstanceState(savedInstanceState);

        times = System.currentTimeMillis() / 1000;
        // mHandler.post(run);
        Log.e("lilei", "onCreate time:" + times);
        mApp = (ClearApplication) getApplication();

        this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED, FLAG_HOMEKEY_DISPATCHED);//关键代码
        setContentView(R.layout.activity_vod);

        // universal image loader initting
        ImageLoaderConfiguration configuration = ImageLoaderConfiguration.createDefault(this);
        ImageLoader.getInstance().init(configuration);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites()
                .detectNetwork().penaltyLog().build());

        Log.e("xbVod", "height:" + this.getWindowManager().getDefaultDisplay().getHeight() + "width:"
                + this.getWindowManager().getDefaultDisplay().getWidth());
        // 远程配置ip地址的服务器启动
        SettingsServer.instance();
        //设置自动关机关闭
//		try {
//			com.mstar.android.tvapi.common.TvManager.getInstance().setTvosCommonCommand("SetAutoSleepOffStatus");
//		} catch (TvCommonException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
        initView();
        ctx = this;
        // bindUpdateManagerService();
        mainEntry();

//        String str = null;
//        str.trim();
    }

    SharedPreferences rebootSp;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState");
        if (rebootSp == null) {
            rebootSp = getSharedPreferences("reboot", Context.MODE_PRIVATE);
        }
        Editor editor = rebootSp.edit();
        editor.putBoolean("isNeedReboot", true);
        editor.commit();
    }


    @Override
    protected void onRestoreInstanceState(Bundle arg0) {
        if (arg0 == null) {
            return;
        }
        Log.i(TAG, "onRestoreInstanceState");
        // super.onRestoreInstanceState(arg0);
    }

    private void initView() {
        mMyProgressBarView = (MyProgressBarView) findViewById(R.id.pb_page_loading);
    }

    public void mainEntry() {
        Log.d(TAG, "------main entry --------- " + getOffsetTimes());
        mPrefs = getSharedPreferences(ClearConfig.STR_NETWORK, Context.MODE_PRIVATE);
        // 默认设置自动重启为早上6点

        Editor editor = mPrefs.edit();
//		editor.putBoolean(ClearConstant.REBOOT_ENABLE, false);
//		editor.commit();
        //2018-5-16 winter marks, why init reboot config here!!!
        Log.e("xb", "REBOOT_HOUR" + mPrefs.getInt(ClearConstant.REBOOT_HOUR, -1) + "REBOOT_MINUTE:"
                + mPrefs.getInt(ClearConstant.REBOOT_MINUTE, -1));
        if (mPrefs.getInt(ClearConstant.REBOOT_HOUR, -1) == -1
                || mPrefs.getInt(ClearConstant.REBOOT_MINUTE, -1) == -1) {
            editor.putBoolean(ClearConstant.REBOOT_ENABLE, true);
            editor.putInt(ClearConstant.REBOOT_HOUR, 6);
            editor.putInt(ClearConstant.REBOOT_MINUTE, 0);
            editor.commit();
        }
        moduleGroupSharePre = getSharedPreferences(ClearConstant.STR_MODULE_GROUP, Context.MODE_PRIVATE);
        initSetting();
        // mHandler.sendEmptyMessage(WAIT_INIT_MSG);
        VoDViewManager.getInstance().startWork(ctx);
        mHandler.sendEmptyMessageDelayed(WAIT_INIT_MSG, 2000);
    }

    public void goingShow() {
        Log.i(TAG, "----------going show language view-------- ");
        // 滚动字幕、消息查询、升级服务、运维日志
        Intent intent = new Intent(ctx, FloatViewService.class);
        ctx.startService(intent);

        intent = new Intent(ctx, PerfectPlayerService.class);
        ctx.startService(intent);

        // mUpdateServiceBinder.invokeMethodInMyService();
        UpdateService.actionStart(ctx);
        pushMsgService.actionStart(ctx);

        // mEggsView = new EggsSurfaceView(this);

        // DisplayMetrics metrics = new DisplayMetrics();
        // getWindowManager().getDefaultDisplay().getMetrics(metrics);
        // addContentView(mEggsView, new LayoutParams(metrics.widthPixels,
        // metrics.heightPixels));
        startLogUploadTask(ClearConfig.MAIN_URI);
        // ClearLog.LogInfo("APP\tStart");
        // DateUtil.getInstance().setRebootSchdule(rebootEnable, rebootHour,
        // rebootMinute);
        // if(ivLoading != null){
        // ivLoading.setVisibility(View.GONE);
        // bitmapDrawables.clear();
        // }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.e(TAG, "get message " + msg.what + " time " + getOffsetTimes());
            switch (msg.what) {
                case WAIT_INIT_MSG:
                    // 判断网络或者u盘是可用的，如果没有连接，等待网络连接
                    if (isNetworkConnected() || isSDMounted()) {
                        VoDViewManager.getInstance().resetForegroundView();
                        mHandler.sendEmptyMessageDelayed(MAINPAGE_LOAD_FINISH_MSG, waitPeriod);
                    } else {
                        Log.i(TAG, "network is not ready");
                        registerNetworkReceivers();
                        mHandler.sendEmptyMessageDelayed(CHECK_NET_MSG, 30 * 1000);
                    }
                    break;
                case NET_READY_MSG:
                    Log.i(TAG, "network is ready ");
                    mHandler.removeMessages(CHECK_NET_MSG);
                    VoDViewManager.getInstance().resetforegroundView();
                    mHandler.sendEmptyMessageDelayed(MAINPAGE_LOAD_FINISH_MSG, waitPeriod);
                    break;
                case CHECK_NET_MSG:
                    // 不管网络是否连接，开始工作
                    Log.i(TAG, "check network timeout ");
                    VoDViewManager.getInstance().resetForegroundView();
                    mHandler.sendEmptyMessageDelayed(MAINPAGE_LOAD_FINISH_MSG, waitPeriod);
                    break;
                case MAINPAGE_LOAD_FINISH_MSG:
                    // 判断主页加载完成，
                    if (VoDViewManager.getInstance().isStarted < 2) {
                        Log.e(TAG, "mainpage load finish msg ,will send finish 3 seconds later " + getOffsetTimes());
                        mHandler.sendEmptyMessageDelayed(MAINPAGE_LOAD_FINISH_MSG, waitPeriod);
                        break;
                    }
                    // mProgressDialog.dismiss();
                    if (mMyProgressBarView != null)
                        mMyProgressBarView.setVisibility(View.GONE);
                    Log.i(TAG, "main page load finish ");
                    if (VoDViewManager.getInstance().isStarted == 2) {
                        // ivLoading.setImageResource(drawable.wlcomvideobk);
                    } else {
                        goingShow();
                    }
                    break;
                case MAINPAGE_LOAD_ANIM_MSG:
                    // ivLoading.setImageDrawable(bitmapDrawables.get(indexOfBitmapDrawables));
                    // if(++indexOfBitmapDrawables >= bitmapDrawables.size()){
                    // indexOfBitmapDrawables = 0;
                    // }
                    // mHandler.sendEmptyMessageDelayed(MAINPAGE_LOAD_ANIM_MSG,
                    // 500);
                    break;
                default:
                    break;

            }
        }
    };


    private boolean isClickToFast() {
        long last = System.currentTimeMillis();
        if (last - lasttimes < 100) {
//			lastTime = last;
            return true;
        }
        lasttimes = last;
        return false;
    }

    /* TODO, FIXME */
    /* 1. where should we take the key event, here or let view's requestFocus */
    /* 2. support touch event */
    boolean isNeedReboot = false;

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG + "key", "key down:" + keyCode);

        if (isClickToFast()) {
            return true;
        }
        if (VoDViewManager.getInstance().isStarted == 0)
            return true;
        /*
         * only in vodstate
         */
        if (this.activityMode != ClearConstant.CODE_VOD_STATE) {
            return true;
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_SETTINGS:
                Log.e(TAG + "key", "shezhi shezhi jian!");
                //for 362
            case 3:
            case 769:
            case 228:
                //for jiulian
                return true;
            case KeyEvent.KEYCODE_0:
                addKeys("0");
                break;
            case KeyEvent.KEYCODE_1:
                addKeys("1");
                break;
            case KeyEvent.KEYCODE_2:
                addKeys("2");
                break;
            case KeyEvent.KEYCODE_3:
                addKeys("3");
                break;
            case KeyEvent.KEYCODE_4:
                addKeys("4");
                break;
            case KeyEvent.KEYCODE_5:
                addKeys("5");
                break;
            case KeyEvent.KEYCODE_6:
                addKeys("6");
                break;
            case KeyEvent.KEYCODE_7:
                addKeys("7");
                break;
            case KeyEvent.KEYCODE_8:
                addKeys("8");
                break;
            case KeyEvent.KEYCODE_9:
                addKeys("9");
                break;
            //对暴风电视没有按键采用特殊处理
            case 19:
                addKeys("19");
                break;
            case 20:
                addKeys("20");
                break;
            case 21:
                addKeys("21");
                break;
            case 22:
                addKeys("22");
                break;
            // *************************重庆潮曼酒店项目，断网后不能响应音量键********************
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                return clearProject.zmaxVolume(keyCode, ctx);
            default:
                mSettingsKeys = "";
                mAppKeys = "";
                break;
        }
        checkKeys();
        // 没加载好首页前，不响应按键
        // if (VoDViewManager.getInstance().isStarted < 3) {
        // if (VoDViewManager.getInstance().getInstance().playingWelcomVideo) {
        // VoDViewManager.getInstance().playingWelcomVideo = false;
        // VoDViewManager.getInstance().playBackgroundVideo();
        // VoDViewManager.getInstance().pushForegroundView(VoDViewManager.getInstance().languageView);
        // VoDViewManager.getInstance().isStarted = 3;
        // goingShow();
        // }
        // return true;
        // }
        if (VoDViewManager.getInstance().onKeyDown(keyCode, event) == false) {
            Log.d("key activity", "key return false,system handle");
            return super.onKeyDown(keyCode, event);
        }
        Log.d("key activity", "key return true");
        return true;
    }

    public void addKeys(String key) {
        mSettingsKeys += key;
        mAppKeys += key;
    }


    private boolean checkKeys() {
        Log.i(TAG, "addkeys: " + mSettingsKeys);
        if (!ClearConstant.SETTING_KEYS.startsWith(mSettingsKeys) && !SETTINGSKEYS_BAOFENG.startsWith(mSettingsKeys)) {
            mSettingsKeys = "";
        } else if (ClearConstant.SETTING_KEYS.equals(mSettingsKeys) || SETTINGSKEYS_BAOFENG.equals(mSettingsKeys)) {
            Log.i(TAG, "show setting view");
            mSettingsKeys = "";
            if (!VoDViewManager.getInstance().isInLiveView || VoDViewManager.getInstance().onlyLive) {
                this.showSettingsDialog();
            }

        } else if (SHOTSCREEN.equals(mSettingsKeys)) {
            // Log.e(TAG, "shotscreen");
            // Bitmap bitmap = ShotScreen.shot(VoDActivity.this);
            // int resouce_id = ShotScreen.saveBitmap(bitmap);
            // Log.e(TAG, "shotscreen"+resouce_id);
            // if(resouce_id != -1){
            // re = resouce_id;
            // new Thread(getRemoteVersion).start();
            // }
        }

        if (!ClearConstant.APPKEYS.startsWith(mAppKeys)) {
            mAppKeys = "";
        } else if (ClearConstant.APPKEYS.equals(mAppKeys)) {
            Log.i(TAG, "show app view");
            mAppKeys = "";

            VoDViewManager.getInstance().appManager.showAppsByNative(false);
        }
        return false;
    }

    // @Override
    // protected void onResume() {
    // Log.d(TAG, "onresume");
    // super.onResume();
    // if (VoDViewManager.getInstance().isStarted == 3) {
    // if (!VoDViewManager.getInstance().isInLiveView) {
    // Log.d(TAG, "show language view");
    // VoDViewManager.getInstance().showBackgroundVideo();
    // VoDViewManager.getInstance().playBackgroundVideo();
    // } else {
    // VoDViewManager.getInstance().showLiveView();
    // }
    // }
    // }

    @Override
    protected void onStart() {
        try {
            Log.d(TAG, "onstart");
            super.onStart();

            mInStalledReceiver = new InStalledReceiver(this);
            IntentFilter intentFilter = new IntentFilter();

            intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
            intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            intentFilter.addDataScheme("package");

            // 注册广播
            this.registerReceiver(mInStalledReceiver, intentFilter);
        } catch (Exception e) {

        }


        if (rebootSp == null) {
            rebootSp = getSharedPreferences("reboot", Context.MODE_PRIVATE);
        }
        isNeedReboot = rebootSp.getBoolean("isNeedReboot", false);
        //重启之前需要赋初值
        Editor editor = rebootSp.edit();
        editor.putBoolean("isNeedReboot", false);
        editor.commit();

        if (isNeedReboot) {
            //RebootTool.doReboot();
        }
    }


    @Override
    protected void onStop() {
        Log.d(TAG, "stop");
        super.onStop();
        // this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "on activety result:" + requestCode);
        switch (requestCode) {
            case 1:
                // 如果没有正常
                Log.i(TAG, "on activety result is 1，reset forcegroundView");
                if (VoDViewManager.getInstance().isStarted < 3)
                    VoDViewManager.getInstance().resetForegroundView();
                break;
        }

    }

    public void stopLogUploadTask() {
        ClearLog.finit();
    }

    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        ClearLog.LogInfo("APP\tExit");
        super.onDestroy();
        stopLogUploadTask();
        unregisterReceivers();
    }

    @SuppressLint("InflateParams")
    public void initSetting() {

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        mSettingsView = layoutInflater.inflate(R.layout.setting_view, null);
        mAuthServerText = (EditText) mSettingsView.findViewById(R.id.settings_auth_server_edittext);

        mUpdateServerText = (EditText) mSettingsView.findViewById(R.id.settings_update_server_edittext);

        mRoomIdText = (EditText) mSettingsView.findViewById(R.id.room_id_text);

        mVersionText = (TextView) mSettingsView.findViewById(R.id.version_info_text);

        mIpText = (TextView) mSettingsView.findViewById(R.id.ip_info_text);

        mTextSaveButton = (Button) mSettingsView.findViewById(R.id.settings_save_button);
        mTextSaveButton.setOnClickListener(this);

        mSystemSettingButton = (Button) mSettingsView.findViewById(R.id.system_settings_button);
        mSystemSettingButton.setOnClickListener(this);

        initRebootSchdule();

    }

    public void initRebootSchdule() {

        rebootEnable = mPrefs.getBoolean(ClearConstant.REBOOT_ENABLE, true);
        rebootHour = mPrefs.getInt(ClearConstant.REBOOT_HOUR, 6);
        rebootMinute = mPrefs.getInt(ClearConstant.REBOOT_MINUTE, 0);

        rebootEnableSpinner = (Spinner) mSettingsView.findViewById(R.id.reboot_enable_spinner);
        List<String> enable_list = new ArrayList<String>();
        enable_list.add("Disable");
        enable_list.add("Enable");
        ArrayAdapter<String> arrapapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                enable_list);
        arrapapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rebootEnableSpinner.setAdapter(arrapapter);
        if (rebootEnable) {
            rebootEnableSpinner.setSelection(1);
        } else {
            rebootEnableSpinner.setSelection(0);
        }
        rebootEnableSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                if (arg2 == 1) {
                    Log.i(TAG, "enable reboot schdule");
                    rebootEnable = true;
                    Editor editor = mPrefs.edit();
                    editor.putBoolean(ClearConstant.REBOOT_ENABLE, rebootEnable);
                    editor.commit();

                } else {
                    Log.i(TAG, "disable reboot schdule");
                    rebootEnable = false;
                    Editor editor = mPrefs.edit();
                    editor.putBoolean(ClearConstant.REBOOT_ENABLE, rebootEnable);
                    editor.commit();
                }
                // DateUtil.getInstance().setRebootSchdule(rebootEnable,
                // rebootHour, rebootMinute);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });

        rebootHourSpinner = (Spinner) mSettingsView.findViewById(R.id.reboot_hour_spinner);
        String[] hours = getResources().getStringArray(R.array.hour);
        ArrayAdapter<String> hourApter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, hours);
        arrapapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rebootHourSpinner.setAdapter(hourApter);
        rebootHourSpinner.setSelection(rebootHour);
        rebootHourSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                rebootHour = arg2;
                Editor editor = mPrefs.edit();
                editor.putInt(ClearConstant.REBOOT_HOUR, rebootHour);
                editor.commit();
                // DateUtil.getInstance().setRebootSchdule(rebootEnable,
                // rebootHour, rebootMinute);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });

        rebootMinuteSpinner = (Spinner) mSettingsView.findViewById(R.id.reboot_minute_spinner);
        String[] minutes = getResources().getStringArray(R.array.minute);
        ArrayAdapter<String> minuteApter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                minutes);
        arrapapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rebootMinuteSpinner.setAdapter(minuteApter);
        rebootMinuteSpinner.setSelection(rebootMinute);
        rebootMinuteSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                rebootMinute = arg2;
                Editor editor = mPrefs.edit();
                editor.putInt(ClearConstant.REBOOT_MINUTE, rebootMinute);
                editor.commit();
                // DateUtil.getInstance().setRebootSchdule(rebootEnable,
                // rebootHour, rebootMinute);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

    }

    public void showSettingsDialog() {
        Log.i(TAG, "show setting dialog");
        if (mAlertDialog == null) {
            mAlertDialog = new AlertDialog.Builder(this).
                    // setTitle(R.string.settings).
                            setView(mSettingsView).create();
        }
        initSettingsData(this);
        mAlertDialog.show();
        WindowManager.LayoutParams params = mAlertDialog.getWindow().getAttributes();
        mAlertDialog.getWindow().setAttributes(params);

        mAlertDialog.setOnCancelListener(listener);

    }

    private void reloadData() {
        rebootEnable = mPrefs.getBoolean(ClearConstant.REBOOT_ENABLE, true);
        rebootHour = mPrefs.getInt(ClearConstant.REBOOT_HOUR, 6);
        rebootMinute = mPrefs.getInt(ClearConstant.REBOOT_MINUTE, 0);

        if (rebootEnableSpinner != null) {
            if (rebootEnable) {
                rebootEnableSpinner.setSelection(1);
            } else {
                rebootEnableSpinner.setSelection(0);
            }
        }
        if (rebootHourSpinner != null) {
            rebootHourSpinner.setSelection(rebootHour);
        }
        if (rebootMinuteSpinner != null) {
            rebootMinuteSpinner.setSelection(rebootMinute);
        }
    }

    public void initSettingsData(Context ctx) {
        mAuthServerText.setText(mPrefs.getString(ClearConstant.MAIN_SERVER, ClearConfig.DEFAULT_MAIN_URI));
        mUpdateServerText.setText(mPrefs.getString(ClearConstant.CLOUD_UPDATE_SERVER, ClearConfig.CLOUD_UPDATE_SERVER));
        mVersionText.setText(ClearConfig.getVersionInfo());
        mRoomIdText.setText(mPrefs.getString(ClearConstant.ROOM_ID, ClearConfig.roomId));
        try {
            mIpText.setText(ClearConfig.getLocalIPAddres());
            reloadData();
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (this.mSystemSettingButton == v) {

            SettingsTool.startSettingsApp(this);
            return;
        } else if (this.mTextSaveButton == v) {
            String authText = null;
            String updateText = null;
            authText = mAuthServerText.getText().toString();
            updateText = mUpdateServerText.getText().toString();
            String roomIdText = mRoomIdText.getText().toString();
            Editor editor = mPrefs.edit();
            ClearConfig.isNormal = true;
            if (!ClearConfig.CLOUD_UPDATE_SERVER.equals(updateText)) {
                ClearConfig.CLOUD_UPDATE_SERVER = updateText;
                editor.putString(ClearConstant.CLOUD_UPDATE_SERVER, updateText);
                editor.commit();
            }

            if (!ClearConfig.roomId.equals(roomIdText)) {
                editor.putString(ClearConstant.ROOM_ID, roomIdText);
                ClearConfig.roomId = roomIdText;
                editor.commit();
                // 重启滚动字幕服务
                FloatViewService.actionRestart(ctx);
                pushMsgService.actionRestart(ctx);
            }

            if (!ClearConfig.MAIN_URI.equals(authText)) {
                Editor me = moduleGroupSharePre.edit();
                // 为防止切换主页后，分组数据不正常导致主页无限崩溃
                me.putInt(ClearConstant.STR_MODULE_GROUP_ID, -1);
                me.commit();
                editor.putString(ClearConstant.MAIN_SERVER, authText);
                editor.commit();
                ClearConfig.initdatas(this);
                startLogUploadTask(ClearConfig.MAIN_URI);
                // 重启滚动字幕服务
                FloatViewService.actionRestart(ctx);
                pushMsgService.actionRestart(ctx);
                // 重新生成
                VoDViewManager.getInstance().resetforegroundView();
            }
        }
    }

    public void startLogUploadTask(String epgURL) {
        Log.i("ClearLog", "start task ");
        String defaultRemoteURL = "";
        if (epgURL != null) {
            String[] segs = epgURL.split("/");
            if (segs != null && segs.length > 3) {
                defaultRemoteURL = "http://" + segs[2] + ":8000/ClearLogProxy";
            }
        }

        String defaultPlayURL = "";
        if (epgURL != null) {
            String[] segs = epgURL.split("/");
            if (segs != null && segs.length > 3) {
                defaultPlayURL = "http://" + segs[2] + ":8000/backend/pblog/";
            }
        }

        Log.i("ClearLog", "start task1 ");
        String remoteURL = mPrefs.getString(ClearConstant.PREF_LOG_SERVER, defaultRemoteURL);
        if (remoteURL == null || remoteURL.equals("")) {
            remoteURL = defaultRemoteURL;
        }

        String playLogURL = mPrefs.getString(ClearConstant.PREF_PLAY_LOG_SERVER, defaultPlayURL);
        if (playLogURL == null || remoteURL.equals("")) {
            playLogURL = defaultPlayURL;
        }

        Log.i("ClearLog", "log url: " + remoteURL + " / " + playLogURL);
        ClearLog.init(this, "General", remoteURL, playLogURL);
        Log.i("ClearLog", "after init  ");
    }

    OnCancelListener listener = new OnCancelListener() {
        @Override
        public void onCancel(DialogInterface arg0) {
            // TODO Auto-generated method stub
            Log.i(TAG, "cancel listener");
            if (VoDViewManager.getInstance().isStarted < 3) {
                VoDViewManager.getInstance().resetForegroundView();
            }
        }

        ;

    };
    public MyProgressBarView mMyProgressBarView;

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isAvailable();
    }

    public boolean isSDMounted() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    /**
     * 注册广播
     */
    private void registerNetworkReceivers() {
        if (connectionReceiver == null) {
            connectionReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "network connected");
                    mHandler.sendEmptyMessageDelayed(NET_READY_MSG, 1000);
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(connectionReceiver, intentFilter);
        }
    }

    /**
     * 取消注册
     */
    private void unregisterReceivers() {
        if (connectionReceiver != null) {
            unregisterReceiver(connectionReceiver);
            connectionReceiver = null;
        }

        if (mInStalledReceiver != null) {
            this.unregisterReceiver(mInStalledReceiver);
            mInStalledReceiver = null;
        }
    }
    // /**
    // * 绑定并调用更新apk服务
    // */
    // private void bindUpdateManagerService() {
    //
    // Intent intent = new Intent(this, UpdateManagerService.class);
    // UpdateApkConnection updateServiceConn = new UpdateApkConnection();
    //
    // // 绑定服务
    // // 第一个参数是intent对象，表面开启的服务。
    // // 第二个参数是绑定服务的监听器
    // // 第三个参数一般为BIND_AUTO_CREATE常量，表示自动创建bind
    // bindService(intent, updateServiceConn, BIND_AUTO_CREATE);
    // }

    // private class UpdateApkConnection implements ServiceConnection {
    //
    //
    // @Override
    // public void onServiceConnected(ComponentName name, IBinder iBinder) {
    //
    // mUpdateServiceBinder = (IMyBinder) iBinder;
    // mUpdateManagerService=mUpdateServiceBinder.getService();
    // mUpdateManagerService.setContext(VoDActivity.this);
    // }
    //
    // @Override
    // public void onServiceDisconnected(ComponentName name) {
    //
    // }
    // }
    // protected Runnable getRemoteVersion = new Runnable() {
    //
    // @Override
    // public void run() {
    // HttpUtils httpUtils = new HttpUtils();
    // httpUtils.configCurrentHttpCacheExpiry(0);
    // RequestParams params = new RequestParams();
    // params.addBodyParameter("mac", ClearConfig.getMac());
    // params.addBodyParameter("action", "snapshot_url");
    // params.addBodyParameter("resource_id", String.valueOf(re));
    // try {
    // httpUtils.send(HttpMethod.POST,
    // "http://192.168.0.62:8000/backend/SnapShotURL",params,new
    // RequestCallBack<String>() {
    //
    // @Override
    // public void onFailure(HttpException arg0, String arg1) {
    // // TODO Auto-generated method stub
    // Log.e(TAG, arg1);
    // }
    // @Override
    // public void onSuccess(ResponseInfo<String> arg0) {
    // // TODO Auto-generated method stub
    // Log.e(TAG, arg0.result);
    // }
    // });
    // } catch (Exception e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }
    // };
}
