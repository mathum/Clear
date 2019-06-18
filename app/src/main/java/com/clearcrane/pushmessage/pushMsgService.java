package com.clearcrane.pushmessage;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.Service;
import android.app.TvManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import com.clearcrane.activity.ClearApplication;
import com.clearcrane.activity.VoDActivity;
import com.clearcrane.constant.ClearConstant;
import com.clearcrane.log.ClearLog;
import com.clearcrane.logic.InterCutOrgan;
import com.clearcrane.logic.PrisonLogicManager;
import com.clearcrane.logic.state.PrisonBaseModeState;
import com.clearcrane.logic.version.AccessTimeVersion;
import com.clearcrane.logic.version.BaseShotScreen;
import com.clearcrane.logic.version.ChannelVersion;
import com.clearcrane.logic.version.InterCutVersion;
import com.clearcrane.logic.version.LivePageShot;
import com.clearcrane.logic.version.ModuleGroupVersion;
import com.clearcrane.logic.version.PrisonBaseVersion;
import com.clearcrane.logic.version.ScrollTextVersion;
import com.clearcrane.logic.version.StaticPageShot;
import com.clearcrane.logic.version.VersionFatory;
import com.clearcrane.logic.version.VideoPageShot;
import com.clearcrane.logic.view.AccessTimeView;
import com.clearcrane.logic.view.BannerView;
import com.clearcrane.logic.view.InterCutView;
import com.clearcrane.logic.view.ProgramView;
import com.clearcrane.logic.view.TermForcedView;
import com.clearcrane.player.ClearVideoView;
import com.clearcrane.schedule.DateUtil;
import com.clearcrane.service.PerfectPlayerService;
import com.clearcrane.tool.RebootTool;
import com.clearcrane.tool.ShotScreen;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.ForcedViewInfo;
import com.clearcrane.util.PlatformSettings;
import com.clearcrane.util.PlatformSettings.Platform;
import com.clearcrane.util.StringUtils;
import com.clearcrane.view.InterCutMusicView;
import com.clearcrane.view.MainPageView;
import com.clearcrane.view.VoDBaseView;
import com.clearcrane.view.VoDMovieView;
import com.clearcrane.view.VoDPerfectMusicView;
import com.clearcrane.view.VoDPrisonLiveView;
import com.clearcrane.view.VoDViewManager;
import com.clearcrane.view.VodPDFView;
import com.clearcrane.vod.R;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class pushMsgService extends Service {

    private static String TAG = "pushMsgService";
    private final int period = 4 * 1000;// 轮询周期
    private static final String ACTION_START = "action_start";
    private static final String ACTION_STOP = "action_stop";
    private static final String ACTION_RESTART = "action_restart";
    private boolean isStarted = false;
    private String heartbeatUrl, getMessageUrl, getModuleGroupUrl, getConfigUrl;
    private String mac, ip, termVersion, roomid;
    public int curScorollVersion = -1;
    public int curMessageVersion = -1;
    public int curAccessTimeVersion = -1;
    public int curModuleGroupVersion = -1;
    public int curChannelVersion = -1;
    public int curInterCutVersion = -1;
    //心跳间隔的倍数
    public int interval = 7;
    //当前的倍数
    public int currentInterval = 0;
    public static Context context;

    //终端是否被限制了
    private int is_forced = 0;

    public SharedPreferences messageSharePre;
    public SharedPreferences accessTimeSharePre;
    public SharedPreferences moduleGroupSharePre;
    public SharedPreferences activitySharePre;
    public boolean curAccessible = true;
    private String curServTime = null;
    private DecimalFormat df = new DecimalFormat("00");// 格式化小数  
    private static ClearApplication mApp;

    private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
    Timer timer = null;

    private Map<String, String> typeClassMap;
    private PrisonLogicManager logicManager;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            PrisonBaseModeState state;
            switch (msg.what) {
                case ClearConstant.MSG_SET_TERM_FORCED:
                    ForcedViewInfo fvi = (ForcedViewInfo) msg.obj;
                    showTermForcedView(fvi.url, fvi.type);
                    saveStatue(6, "限制使用");
                    break;
                case ClearConstant.MSG_SET_TERM_FREE:
                    hideTermForcedView();
                    break;
                case ClearConstant.MSG_START_INTER_CUT:
                    Log.e("xb", "showInterCutView");
                    InterCutOrgan organ = (InterCutOrgan) msg.obj;
                    showInterCutView(organ);
                    saveStatue(4, organ.getInterCutTitle());
                    break;
                case ClearConstant.MSG_STOP_INTER_CUT:
                    Log.e("xb", "hideInterCutView");
                    hideInterCutView();
                    break;
                case ClearConstant.MSG_START_ACCESS_TIME:
                    AccessTimeView atv = (AccessTimeView) msg.obj;
                    showAccessTimeView(atv);
                    break;
                case ClearConstant.MSG_STOP_ACCESS_TIME:
                    hideAccessTimeView();
                    break;
                case ClearConstant.MSG_STOP_CHANNEL:
                    state = (PrisonBaseModeState) msg.obj;
                    state.stopPlay();
                    break;
                case ClearConstant.MSG_START_CHANNEL:
                    state = (PrisonBaseModeState) msg.obj;
                    state.startPlay();
                    break;
                case ClearConstant.MSG_START_CHANNEL_LIST:
                    ProgramView pv = (ProgramView) msg.obj;
                    showChannelView(pv);
                    saveStatue(5, "正在计划播");
                    break;
                case ClearConstant.MSG_STOP_CHANNEL_LIST:
                    ProgramView prv = (ProgramView) msg.obj;
                    hideChannelView(prv);
                    break;
                case ClearConstant.MSG_INTERRUPT_MUSIC_START:
                    InterCutOrgan musicOrgan = (InterCutOrgan) msg.obj;
                    showMusicView(musicOrgan);
                    saveStatue(4, musicOrgan.getInterCutType());
                    break;
                case ClearConstant.MSG_INTERRUPT_MUSIC_STOP:
                    Log.e("xb", "音樂插播退出啦");
                    hideMusicView();
                    break;
                case ClearConstant.MSG_INTERRUPT_PICTURE_START:
                    Log.e("xb", "图片插播开始啦");
                    InterCutOrgan pictureOrgan = (InterCutOrgan) msg.obj;
                    showBannerView(pictureOrgan);
                    saveStatue(4, pictureOrgan.getInterCutTitle());
                    break;
                case ClearConstant.MSG_INTERRUPT_PICTURE_STOP:
                    Log.e("xb", "图片插播退出啦");
                    hideBannerView();
                    break;
                default:
                    break;
            }
        }
    };
    ;
    private int moviePosition;

    /*
     * TODO,FIXME to move these to a common place
     *
     */
    private void showAccessTimeView(AccessTimeView atv) {
        VoDViewManager voDViewManager = VoDViewManager.getInstance();
        VoDBaseView topView = voDViewManager.getTopView();
        specialShowHandle(topView);
        voDViewManager.pushForegroundView(atv);
    }

    private void hideAccessTimeView() {
        VoDViewManager voDViewManager = VoDViewManager.getInstance();
        VoDBaseView topView = voDViewManager.getTopView();
        if (topView instanceof AccessTimeView) {
            VoDViewManager.getInstance().popForegroundView();
        }
        topView = voDViewManager.getTopView();
        specialHideHandle(topView);
    }

    public void showTermForcedView(String url, String type) {
        VoDViewManager voDViewManager = VoDViewManager.getInstance();
        VoDBaseView topView = voDViewManager.getTopView();
        specialShowHandle(topView);
        TermForcedView tfv = new TermForcedView();
        tfv.init(this, url, type);
        if (tfv != null) {
            voDViewManager.pushForegroundView(tfv);
            voDViewManager.setActivityMode(ClearConstant.CODE_TERM_FORCED_STATE);
            sendLogStart("限制使用", "限制使用", "限制使用");
        }
    }

    public void hideTermForcedView() {
//    	VoDViewManager voDViewManager = VoDViewManager.getInstance();
//		VoDBaseView topView = voDViewManager.getTopView();
//		if(topView instanceof TermForcedView){
//			VoDViewManager.getInstance().popForegroundView();
//		}
//		topView = voDViewManager.getTopView();
//		voDViewManager.setActivityMode(ClearConstant.CODE_VOD_STATE);
//		specialHideHandle(topView);
        sendLogEnd("限制使用");
        //restartapp
        RebootTool.rebootApp();
    }


    private void showInterCutView(InterCutOrgan organ) {
        VoDViewManager voDViewManager = VoDViewManager.getInstance();
        VoDBaseView voDBaseView = voDViewManager.getTopView();
        specialShowHandle(voDBaseView);
        InterCutView interCutView = new InterCutView();
        interCutView.init(this, organ.getSource_url(), organ.getInterCutType());
        if (interCutView != null) {
            VoDViewManager.getInstance().pushForegroundView(interCutView);
            if (organ.getSource_url().contains("m3u8"))
                mApp.interruptProgramContent = "直播";
            else
                mApp.interruptProgramContent = "视频";
            sendLogStart(organ.getInterCutTitle(), "插播", mApp.interruptProgramContent);
        }
    }

    private void hideInterCutView() {
        Log.e(TAG, "hideInterCutView");
        VoDViewManager voDViewManager = VoDViewManager.getInstance();
        VoDBaseView topView = voDViewManager.getTopView();
        if (topView instanceof InterCutView) {
            Log.e(TAG, "hideInterCutView stop !");
            ((InterCutView) topView).stopPlay();
            VoDViewManager.getInstance().popForegroundView();
            VoDViewManager.getInstance().setActivityMode(0);
        }
        topView = voDViewManager.getTopView();
        specialHideHandle(topView);
        sendLogEnd(mApp.interruptProgramContent);
    }

    private void showMusicView(InterCutOrgan organ) {
        VoDViewManager voDViewManager = VoDViewManager.getInstance();
        VoDBaseView topView = voDViewManager.getTopView();
        specialShowHandle(topView);
        InterCutMusicView view = new InterCutMusicView();
        view.init(context, organ.getSource_url(), organ.getInterCutType(), organ.getInterCutTitle());
        if (view != null) {
            VoDViewManager.getInstance().pushForegroundView(view);
            sendLogStart(organ.getInterCutType(), "插播", "音频");
        }
    }

    private void hideMusicView() {
        VoDViewManager voDViewManager = VoDViewManager.getInstance();
        VoDBaseView topView = voDViewManager.getTopView();
        if (topView instanceof InterCutMusicView) {
            Log.e(TAG, "hideInterCutView stop !");
            ((InterCutMusicView) topView).exit();
            VoDViewManager.getInstance().popForegroundView();
            VoDViewManager.getInstance().setActivityMode(0);
        }
        topView = voDViewManager.getTopView();
        specialHideHandle(topView);
        sendLogEnd("音频");
    }

    private void showBannerView(InterCutOrgan organ) {
        VoDViewManager voDViewManager = VoDViewManager.getInstance();
        VoDBaseView topView = voDViewManager.getTopView();
        specialShowHandle(topView);
        BannerView view = new BannerView();
        view.init(context, organ.getSource_url());
        if (view != null) {
            VoDViewManager.getInstance().pushForegroundView(view);
            sendLogStart(organ.getInterCutTitle(), "插播", "图文");
        }
    }

    private void hideBannerView() {
        // ????why?
        // VoDViewManager.getInstance().popForegroundView();

        VoDViewManager voDViewManager = VoDViewManager.getInstance();
        VoDBaseView topView = voDViewManager.getTopView();
        if (topView instanceof BannerView) {
            VoDViewManager.getInstance().popForegroundView();
        }
        topView = voDViewManager.getTopView();
        specialHideHandle(topView);
        sendLogEnd("图文");
    }

    private void showChannelView(ProgramView pv) {
        VoDViewManager voDViewManager = VoDViewManager.getInstance();
        VoDBaseView topView = voDViewManager.getTopView();
        specialShowHandle(topView);
        VoDViewManager.getInstance().pushForegroundView(pv);
        pv.play();
    }

    private void hideChannelView(ProgramView pv) {
        Log.e("zxb", "hideChannelView");
        VoDViewManager voDViewManager = VoDViewManager.getInstance();
        if (voDViewManager.getTopView() instanceof ProgramView) {
            pv.stop();
            VoDViewManager.getInstance().popForegroundView();
        }
        VoDBaseView topView = voDViewManager.getTopView();
        specialHideHandle(topView);
    }

    // 针对截取当前屏幕做特殊处理
    private void specialShotScreenHandle() {
        Log.e(TAG, "shotscreen");
        VoDViewManager voDViewManager = VoDViewManager.getInstance();
        VoDBaseView topView = voDViewManager.getTopView();
        BaseShotScreen baseShotScreen = null;
        if (topView instanceof VoDPrisonLiveView) {
            SharedPreferences sp = context.getSharedPreferences("liveUrl", MODE_PRIVATE);
            String url = sp.getString("url", "http://192.168.18.235/live/BJTVHD/BJTVHD.m3u8");
            baseShotScreen = new LivePageShot("live_url", ClearConfig.getMac(), url);
            baseShotScreen.postRequest(baseShotScreen.getPost());
        } else if (topView instanceof VoDMovieView) {
            SharedPreferences activitySharePre = context.getSharedPreferences(ClearConstant.Activity_FILE,
                    Context.MODE_PRIVATE);
            String url = activitySharePre.getString("video_url", "");
            long time = VoDViewManager.getInstance().getMovieCurrentPosition();
            String currentTime = StringUtils.transferLongToDate(time);
            baseShotScreen = new VideoPageShot("video_url", ClearConfig.getMac(), url, currentTime);
            baseShotScreen.postRequest(baseShotScreen.getPost());
        } else if (topView instanceof InterCutView) {
            switch (mApp.interruptProgramContent) {
                case "直播":
                    baseShotScreen = new LivePageShot("live_url", ClearConfig.getMac(),
                            ((InterCutView) topView).getIntercutUrl());
                    baseShotScreen.postRequest(baseShotScreen.getPost());
                    break;
                case "视频":
                    long time = VoDViewManager.getInstance().getMovieCurrentPosition();
                    String currentTime = StringUtils.transferLongToDate(time);
                    baseShotScreen = new VideoPageShot("video_url", ClearConfig.getMac(),
                            ((InterCutView) topView).getIntercutUrl(), currentTime);
                    baseShotScreen.postRequest(baseShotScreen.getPost());
                    break;
            }
            ;
        } else if (topView instanceof ProgramView) {
            Bitmap bitmap = null;
            int resouce_id;
            switch (((ProgramView) topView).getProgramViewId()) {
                // 视频计划播
                case 1:
                    SharedPreferences sp = context.getSharedPreferences("ProgramVideoFile", Context.MODE_PRIVATE);
                    String url = sp.getString("video_url", "");
                    // 如果没有读取到视频地址，那么视为计划播的为完全的图文页面，采用静态截屏
                    if (url.equals("")) {
                        bitmap = ShotScreen.shot((VoDActivity) context);
                        resouce_id = ShotScreen.saveBitmap(bitmap);
                        if (resouce_id != -1) {
                            baseShotScreen = new StaticPageShot("snapshot_url", ClearConfig.getMac(), resouce_id);
                            baseShotScreen.postRequest(baseShotScreen.getPost());
                        }
                    } else {
                        // 包含m3u8字段则视为直播地址
                        if (url.contains("m3u8")) {
                            baseShotScreen = new LivePageShot("live_url", ClearConfig.getMac(), url);
                            baseShotScreen.postRequest(baseShotScreen.getPost());
                        } else {
                            long time = VoDViewManager.getInstance().getMovieCurrentPosition();
                            String currentTime = StringUtils.transferLongToDate(time);
                            baseShotScreen = new VideoPageShot("video_url", ClearConfig.getMac(), url, currentTime);
                            baseShotScreen.postRequest(baseShotScreen.getPost());
                        }
                    }
                    break;
                // 图片计划播
                case 2:
                    bitmap = ShotScreen.shot((VoDActivity) context);
                    resouce_id = ShotScreen.saveBitmap(bitmap);
                    Log.e(TAG, "shotscreen" + resouce_id);
                    if (resouce_id != -1) {
                        baseShotScreen = new StaticPageShot("snapshot_url", ClearConfig.getMac(), resouce_id);
                        baseShotScreen.postRequest(baseShotScreen.getPost());
                    }
                    break;
                // 音乐计划播
                case 8:
                    bitmap = ShotScreen.shot((VoDActivity) context);
                    resouce_id = ShotScreen.saveBitmap(bitmap);
                    Log.e(TAG, "shotscreen" + resouce_id);
                    if (resouce_id != -1) {
                        baseShotScreen = new StaticPageShot("snapshot_url", ClearConfig.getMac(), resouce_id);
                        baseShotScreen.postRequest(baseShotScreen.getPost());
                    }
                    break;
            }
        } else if (topView instanceof TermForcedView) {
            ;
            switch (((TermForcedView) topView).getTermForcedType()) {
                //图片
                case 0:
                    Bitmap bitmap = ShotScreen.shot((VoDActivity) context);
                    int resouce_id = ShotScreen.saveBitmap(bitmap);
                    Log.e(TAG, "shotscreen" + resouce_id);
                    if (resouce_id != -1) {
                        baseShotScreen = new StaticPageShot("snapshot_url", ClearConfig.getMac(), resouce_id);
                        baseShotScreen.postRequest(baseShotScreen.getPost());
                    }
                    break;
                //视频
                case 1:
                    long time = VoDViewManager.getInstance().getMovieCurrentPosition();
                    String currentTime = StringUtils.transferLongToDate(time);
                    baseShotScreen = new VideoPageShot("video_url", ClearConfig.getMac(),
                            ((TermForcedView) topView).getUrl(), currentTime);
                    baseShotScreen.postRequest(baseShotScreen.getPost());
                    break;
            }
        } else {
            Bitmap bitmap = ShotScreen.shot((VoDActivity) context);
            int resouce_id = ShotScreen.saveBitmap(bitmap);
            Log.e(TAG, "shotscreen" + resouce_id);
            if (resouce_id != -1) {
                baseShotScreen = new StaticPageShot("snapshot_url", ClearConfig.getMac(), resouce_id);
                baseShotScreen.postRequest(baseShotScreen.getPost());
            }
        }
    }

    private void specialShowHandle(VoDBaseView topView) {
        if (topView instanceof VoDPrisonLiveView) {
            Log.e("zxb", "specialShowHandle:直播页面被关闭了");
            VoDViewManager.getInstance().hideLiveVideo();
        }
        if (topView instanceof VoDMovieView) {
            moviePosition = (int) VoDViewManager.getInstance().getVideoView().getCurrentPosition();
            Log.e("zxb", "moviePosition:" + moviePosition);
            ((VoDMovieView) topView).setPosition(moviePosition);
            VoDViewManager.getInstance().hideMovieVideo();
        }
        if (topView instanceof VoDPerfectMusicView) {
            SharedPreferences sp = context.getSharedPreferences("musicUrl", MODE_PRIVATE);
            Log.e(TAG, "winter special show music stop");
            ((VoDPerfectMusicView) topView).playMusic(sp.getInt("musicPos", 0), ClearConstant.PAUSE_MSG);
        }
    }

    private void specialHideHandle(VoDBaseView topView) {
        VoDViewManager.getInstance().hideLiveVideo();
        Log.e(TAG, "lilei specail hide handle");
        if (topView instanceof VoDPrisonLiveView) {
            int width = ((VoDActivity) context).getWindowManager().getDefaultDisplay().getWidth();
            int height = ((VoDActivity) context).getWindowManager().getDefaultDisplay().getHeight();
            VoDViewManager.getInstance().setLiveVideoDisplayArea(0, 0, width, height);
            VoDViewManager.getInstance().showLiveVideo();
            SharedPreferences sp = context.getSharedPreferences("liveUrl", MODE_PRIVATE);
            VoDViewManager.getInstance()
                    .startLiveVideo(sp.getString("url", "http://192.168.18.235/live/BJTVHD/BJTVHD.m3u8"));
            saveStatue(1, sp.getString("live_name", "cctv1"));
        }
        if (topView instanceof VoDMovieView) {
            SharedPreferences sp = context.getSharedPreferences(ClearConstant.Activity_FILE, Context.MODE_PRIVATE);
            saveStatue(1, sp.getString(ClearConstant.Movie_NAME, "电影"));
        }
        if (topView instanceof VoDPerfectMusicView) {
            SharedPreferences sp = context.getSharedPreferences("musicUrl", MODE_PRIVATE);
            ((VoDPerfectMusicView) topView).playMusic(sp.getInt("musicPos", 0), ClearConstant.PLAY_MSG);
            saveStatue(1, sp.getString("music_name", "音乐"));
        }
        if (topView instanceof MainPageView) {
            saveStatue(3, "");
        }
        if (topView instanceof VodPDFView) {
            saveStatue(1, "图文");
        }
    }

    private void specialUpdateMainMenu(int updateMainMenuVersion) {
        SharedPreferences sharePre = getSharedPreferences(ClearConstant.STR_UPDATE_MAINMENU, Context.MODE_PRIVATE);
        Editor editor = sharePre.edit();
        editor.putInt(ClearConstant.STR_NEWEST_VERSION, updateMainMenuVersion);
        editor.commit();
        RebootTool.rebootApp();
    }


    private void specialVolumeHandle(int volumeNumber) {
        // TODO Auto-generated method stub
        // 保存版本号，保证每次音量的设置只执行一次
        SharedPreferences sharePre = getSharedPreferences(ClearConstant.STR_VOLUME, Context.MODE_PRIVATE);
        Editor editor = sharePre.edit();
        editor.putInt(ClearConstant.STR_NEWEST_VERSION, volumeNumber);
        editor.commit();
        if (volumeNumber != -1) {
            AudioManager audiomanage = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//			float max = audiomanage.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
            float max = audiomanage.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int volume = (int) (max * ((float) volumeNumber / 100));
            Log.e("111111111111111", "volume = " + volume);
            if (PlatformSettings.getPlatform() == Platform.skyworth_368W) {
                TvManager mTvmanager = (TvManager) context.getSystemService("tv_manager");
                mTvmanager.setVolume(volume);
                Log.e("zxb", "设置音量启动123" + volume);
            } else {
//				audiomanage.setStreamVolume(AudioManager.STREAM_SYSTEM, volume, AudioManager.FLAG_SHOW_UI);
                audiomanage.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI);
            }
        }
    }

    public static void actionStart(Context ctx) {
        Log.i(TAG, "actionstart");
        context = ctx;
        mApp = (ClearApplication) ctx.getApplicationContext();
        Intent i = new Intent(ctx, pushMsgService.class);
        i.setAction(ACTION_START);
        ctx.startService(i);
    }

    public static void actionRestart(Context ctx) {
        Log.i(TAG, "actionRestart");
        context = ctx;
        mApp = (ClearApplication) ctx.getApplicationContext();
        Intent i = new Intent(ctx, pushMsgService.class);
        i.setAction(ACTION_RESTART);
        ctx.startService(i);
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        Log.d(TAG, "onCreate");
        messageSharePre = getSharedPreferences(ClearConstant.MESSAGE_FILE, Context.MODE_PRIVATE);
        accessTimeSharePre = getSharedPreferences(ClearConstant.ACCESSTIME_FILE, Context.MODE_PRIVATE);
        moduleGroupSharePre = getSharedPreferences(ClearConstant.MODULEGROUP_FILE, Context.MODE_PRIVATE);
        activitySharePre = getSharedPreferences(ClearConstant.Activity_FILE, Context.MODE_PRIVATE);

        curMessageVersion = messageSharePre.getInt(ClearConstant.MESSAGE_NEWSETVERSION, 0);

        curAccessTimeVersion = accessTimeSharePre.getInt(ClearConstant.ACCESSTIME_VERSION, -1);

        curModuleGroupVersion = moduleGroupSharePre.getInt(ClearConstant.MODULEGROUP_VERSION, -1);

        initVersionClassMap();

        logicManager = new PrisonLogicManager(getApplicationContext(), mHandler);
        logicManager.startCheckThread();

    }

    /*
     * for abstract factory
     */

    private void initVersionClassMap() {
        typeClassMap = new HashMap<>();
        typeClassMap.put(ClearConstant.STR_SCROLL_TEXT, ScrollTextVersion.class.getName().toString());
        typeClassMap.put(ClearConstant.STR_INTER_CUT, InterCutVersion.class.getName().toString());
        typeClassMap.put(ClearConstant.STR_CHANNEL, ChannelVersion.class.getName().toString());
        typeClassMap.put(ClearConstant.STR_ACCESS_TIME, AccessTimeVersion.class.getName().toString());
        typeClassMap.put(ClearConstant.STR_MODULE_GROUP, ModuleGroupVersion.class.getName().toString());
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        stop();
    }

    @Override
    @Deprecated
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
        Log.d(TAG, "onstart");

        if (intent == null) {
            return;
        }

        if (intent.getAction().equals(ACTION_STOP)) {
            stop();
            stopSelf();
        } else if (intent.getAction().equals(ACTION_START)) {
            start();
        } else if (intent.getAction().equals(ACTION_RESTART)) {
            stop();
            start();
        }
    }

    public void stop() {
        isStarted = false;
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    private void start() {
        Log.d(TAG, "isStarted = " + isStarted);
//		if(!ClearConfig.isEthernetModeDhcp()){
//			ClearConfig.changeToDhcp();
//		}		
        if (isStarted) {
            Log.d(TAG, "servie already started");
            return;
        }
        try {
            heartbeatUrl = ClearConfig.SERVER_URI + ":8000/backend/HeartBeat";
            getMessageUrl = ClearConfig.SERVER_URI + ":8000/backend/GetMessage";
            getModuleGroupUrl = ClearConfig.SERVER_URI + ":8000/backend/GetModuleGroup";

            mac = ClearConfig.getMac();
            ip = ClearConfig.getLocalIPAddres();
            termVersion = ClearConfig.getVersionInfo();
            roomid = ClearConfig.roomId;
            getConfigUrl = ClearConfig.SERVER_URI + ":8000/backend/GetConfiguration?termID=" + mac;
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (timer != null) {
            stop();
        }

        timer = new Timer(true);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                // doHeartBeat();
                try {
                    doRebootSchdule();
                    doClearHeartBeat();
                    currentInterval++;
                    if (currentInterval % 7 == 0) {
                        getClearConfig();
                        currentInterval = 0;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }, 0, period);

        isStarted = true;
    }

    private void getClearConfig() {
        getConfigUrl = ClearConfig.SERVER_URI + ":8000/backend/GetConfiguration?termID=" + mac;
        HttpUtils httpUtils = new HttpUtils();
        httpUtils.configCurrentHttpCacheExpiry(0);
        httpUtils.send(HttpMethod.GET, getConfigUrl, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> arg0) {
                // TODO Auto-generated method stub
                Log.i(TAG, "onSuccess " + arg0.result);
                parseReturnJson(arg0.result);
            }

            @Override
            public void onFailure(HttpException arg0, String arg1) {
                // TODO Auto-generated method stub
                Log.e(TAG, "onFailure " + arg1);
            }
        });
    }

    private void parseReturnJson(String result) {
        try {
            JSONObject object = new JSONObject(result);
            JSONObject contentObject = new JSONObject(object.getString("reboot_time"));
            if (contentObject.getInt("on") == 1) {
                if (!TextUtils.isEmpty(contentObject.getString("trigger"))) {
                    Editor editor = getSharedPreferences(ClearConfig.STR_NETWORK, Context.MODE_PRIVATE).edit();
                    editor.putBoolean(ClearConstant.REBOOT_ENABLE, true);
                    String[] rebootTime = contentObject.getString("trigger").split(":");
                    Log.i(TAG, "-------------------------------------->" + rebootTime[0]);
                    Log.i(TAG, "-------------------------------------->" + rebootTime[1]);
                    editor.putInt(ClearConstant.REBOOT_HOUR, Integer.parseInt(rebootTime[0]));
                    editor.putInt(ClearConstant.REBOOT_MINUTE, Integer.parseInt(rebootTime[1]));
                    editor.commit();
                }
            } else {
                Editor editor = getSharedPreferences(ClearConfig.STR_NETWORK, Context.MODE_PRIVATE).edit();
                editor.putBoolean(ClearConstant.REBOOT_ENABLE, false);
                editor.commit();
            }

        } catch (Exception ex) {
            Log.i(TAG, "-------------------------------------->");
            ex.printStackTrace();
            Log.i(TAG, "-------------------------------------->");
        }
    }

    private void doClearHeartBeat() {
        Log.i(TAG, "send heartbeat");
        heartbeatUrl = ClearConfig.SERVER_URI + ":8000/backend/HeartBeat";
        Log.e("zxb", heartbeatUrl);
        HttpUtils httpUtils = new HttpUtils();
        httpUtils.configCurrentHttpCacheExpiry(0);
        httpUtils.configTimeout(1800);
        httpUtils.configSoTimeout(1800);
        RequestParams requestParams = new RequestParams();
        try {
            requestParams.setBodyEntity(new StringEntity(getPostParas(), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        httpUtils.send(HttpRequest.HttpMethod.POST, heartbeatUrl, requestParams, new RequestCallBack<String>() {

            @Override
            public void onFailure(HttpException arg0, String arg1) {
                // TODO Auto-generated method stub
                Log.i(TAG, "network is error!");
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {

            }

            @Override
            public void onStart() {
                Log.i(TAG, "onStart");
            }

            @Override
            public void onSuccess(ResponseInfo<String> arg0) {
                handleHeartBeatResponse(arg0.result);
                feedWatchDog();
            }

        });
    }

    private void doRebootSchdule() {
        SharedPreferences mPrefs = getSharedPreferences(ClearConfig.STR_NETWORK, Context.MODE_PRIVATE);
        boolean rebootEnable = mPrefs.getBoolean(ClearConstant.REBOOT_ENABLE, true);
        Log.e(TAG, "rebootEnable:" + rebootEnable);
        if (rebootEnable) {
            String[] hours = getResources().getStringArray(R.array.hour);
            String[] minutes = getResources().getStringArray(R.array.minute);
            int rebootHour = mPrefs.getInt(ClearConstant.REBOOT_HOUR, 6);
            int rebootMinute = mPrefs.getInt(ClearConstant.REBOOT_MINUTE, 0);
            String rh = hours[rebootHour];
            Log.e("winter", "rm: " + rebootMinute);
            String rm = minutes[rebootMinute];
            rh = rh.substring(0, rh.length() - 1);
            rm = rm.substring(0, rm.length() - 1);
            String rhrm = DateUtil.getCurrentTimeCalendar().get(GregorianCalendar.YEAR) + "-"
                    + (DateUtil.getCurrentTimeCalendar().get(GregorianCalendar.MONTH) + 1) + "-"
                    + DateUtil.getCurrentTimeCalendar().get(GregorianCalendar.DAY_OF_MONTH) + " " + rh + ":" + rm
                    + ":00";
            Log.e(TAG, "winter rhrm:" + rhrm);
            Log.e(TAG, "winter dateutil: " + DateUtil.getCurrentTimeSecond() + " DateStr: "
                    + DateUtil.getTimeSecondFromDateStr(rhrm));
            // 当当前的系统时间与预定的重启时间间隔不超过5秒时启动重启功能
            if (Math.abs(DateUtil.getCurrentTimeSecond() - DateUtil.getTimeSecondFromDateStr(rhrm)) < 5) {
                Log.e(TAG, "rhrm:" + "万事俱备，准备重启");
                RebootTool.doReboot();
            }
        }
    }

    // 处理每三秒从后台获取的数据
    private boolean isFirstAlermCome = true;

    private void handleHeartBeatResponse(String result) {
        Log.e(TAG, "heartbeat return " + result);
        JSONTokener jsonTokener = new JSONTokener(result);
        try {
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            String serverTime = jsonObject.getString(ClearConstant.STR_SERVER_TIME);
            DateUtil.setSystemTime(context, serverTime);
            if (jsonObject.has("is_forced")) {
                is_forced = jsonObject.getInt("is_forced");

            }

            if (is_forced == 1 && isFirstAlermCome) {
                isFirstAlermCome = false;
                VoDViewManager.getInstance().stopBackgroundVideo();
                VoDViewManager.getInstance().stopMusic();
                Intent intent = new Intent(context, PerfectPlayerService.class);
                intent.putExtra("url", "");
                intent.putExtra("listPosition", 0);
                intent.putExtra("MSG", ClearConstant.PAUSE_MSG);
                context.startService(intent);
                VoDViewManager.getInstance().popForegroundView();
            }
            checkIsInForcedState();

            JSONArray jsonArray = jsonObject.getJSONArray("ControlCommandsRes");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject tempjson = (JSONObject) jsonArray.opt(i);
                String typeName = tempjson.getString("commandtype");

                // 获取后台推送的最新消息标志
                int remoteVersion = tempjson.getInt(ClearConstant.STR_NEWEST_VERSION);
                Log.e(TAG, "remoteVersion:" + remoteVersion);
                /*
                 * TODO,FIXME every time new and read ? 存储获取的数据
                 */
                SharedPreferences sharePre = getSharedPreferences(typeName, Context.MODE_PRIVATE);

                if (typeName.equals(ClearConstant.STR_SNAP_SHOT) &&
                        remoteVersion != sharePre.getInt(ClearConstant.STR_NEWEST_VERSION, -1)) {
                    specialShotScreenHandle();
                }

                if (is_forced == 1) {
                    continue;
                }

                if (typeName.equals(ClearConstant.STR_INTER_CUT) || typeName.equals(ClearConstant.STR_SCROLL_TEXT)
                        || typeName.equals(ClearConstant.STR_CHANNEL) || typeName.equals(ClearConstant.STR_ACCESS_TIME)
                        || typeName.equals(ClearConstant.STR_MODULE_GROUP)
                        //|| typeName.equals(ClearConstant.STR_SNAP_SHOT)
                        || typeName.equals(ClearConstant.STR_VOLUME)
                        || typeName.equals(ClearConstant.STR_UPDATE_MAINMENU)) {

                    // 如果获取的消息标志与上次一样那么就不做改变，直接跳到下一个type
                    if (remoteVersion == sharePre.getInt(ClearConstant.STR_NEWEST_VERSION, -1)) {
                        continue;
                    }
                    //重启apk首页单独处理
                    if (typeName.equals(ClearConstant.STR_UPDATE_MAINMENU)) {
                        specialUpdateMainMenu(remoteVersion);
                        return;
                    }
                    // 截屏单独处理
//                    else if (typeName.equals(ClearConstant.STR_SNAP_SHOT)) {
//                        specialShotScreenHandle();
//                    }
                    else if (typeName.equals(ClearConstant.STR_VOLUME)) {// 设置终端音量处理
                        specialVolumeHandle(remoteVersion);
                    } else {
                        PrisonBaseVersion prisonBaseVersion = VersionFatory.createVersion(typeClassMap.get(typeName));
                        if (prisonBaseVersion == null) {
                            Log.e("xb", "prisonBaseVersion is null!!!!" + typeName);
                            continue;
                        }
                        // 如果有某个type的version改变了的情况则视为有东西需要推送。
                        prisonBaseVersion.init(remoteVersion, this,
                                logicManager.getVersionChangeListenerByTypeName(typeName));
                        // 启动获取得到需要插播的内容的线程
                        prisonBaseVersion.updateVersion();
                    }
                }
            }
//			SharedPreferences networkSp = getSharedPreferences(ClearConfig.STR_NETWORK, Context.MODE_PRIVATE);
//			if(networkSp.getBoolean(ClearConstant.STR_FIRST_CONNECT_SERVER, true)){
//							if(!ClearConfig.isEthernetModeDhcp()){
//								Editor ed = networkSp.edit();
//								ed.putBoolean(ClearConfig.STR_NETWORK, ClearConfig.changeToDhcp());
//								ed.commit();
            //				}
//						}
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private void checkIsInForcedState() {
        VoDViewManager vvm = VoDViewManager.getInstance();
        if ((is_forced == 0) && (vvm.getActivityMode() == ClearConstant.CODE_TERM_FORCED_STATE)) {
            mHandler.sendEmptyMessage(ClearConstant.MSG_SET_TERM_FREE);
        } else if ((is_forced == 1) && (vvm.getActivityMode() != ClearConstant.CODE_TERM_FORCED_STATE)) {
            VoDViewManager.getInstance().setActivityMode(ClearConstant.CODE_TERM_FORCED_STATE);
            ForcedViewInfo fvi = new ForcedViewInfo();
            Message msg = mHandler.obtainMessage();
            msg.obj = fvi;
            msg.what = ClearConstant.MSG_SET_TERM_FORCED;
            mHandler.sendMessageDelayed(msg, period / 4);
        }
    }


    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public void getMessage() {
        // Log.i(TAG, "get message");
        HttpClient mHttpClient = new DefaultHttpClient();
        HttpResponse mHttpRes = null;
        HttpPost mHttpPost = new HttpPost(getMessageUrl);

        String paras = "{\"roomid\":\"" + roomid + "\"}";
        // Log.i(TAG, "get message pars:" + paras);

        try {
            StringEntity entity = new StringEntity(paras);
            mHttpPost.setEntity(entity);
            mHttpRes = mHttpClient.execute(mHttpPost);
            if (mHttpRes.getStatusLine().getStatusCode() == 200) {

                JSONObject jObject = new JSONObject(convertStreamToString(mHttpRes.getEntity().getContent()));
                // Log.i(TAG, "http get message success: " + jObject);
                if (jObject.getString("rescode").equals("200")) {
                    JSONArray array = jObject.getJSONArray("messages");
                    Editor editor = messageSharePre.edit();
                    // Log.i(TAG, "write message:"+jObject.toString());
                    editor.putString(ClearConstant.MESSAGE_JSON, jObject.toString());
                    editor.commit();

                    if (array.length() > 0) {
                        curMessageVersion = array.getJSONObject(0).getInt("versionid");
                        for (int i = 0; i < array.length(); i++) {
                            int version = array.getJSONObject(i).getInt("versionid");
                            if (version > curMessageVersion) {
                                curMessageVersion = version;
                                Editor editor1 = messageSharePre.edit();
                                editor1.putInt(ClearConstant.MESSAGE_NEWSETVERSION, curMessageVersion);
                                editor1.commit();
                            }
                        }
                        // Log.i(TAG, "not null message ,show message image
                        // view");
                        FloatViewService.actionShowMessage(this);
                    } else {
                        // Log.i(TAG, "null message ,hide message image view");
                        FloatViewService.actionHideMessage(this);
                    }
                } else {
                    // Log.e(TAG, " get invalied message " );
                }

            } else {
                // Log.e(TAG, "get message response error" + paras);
                return;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e(TAG, "post scrollText request error" + paras);
            return;
        }
    }

    @SuppressLint("NewApi")
    public String getPostParas() {
        /**********************************************************
         * http://172.16.1.39:8000/backend/HeartBeat{ "roomid":"205",
         * "termType":"himediastb", "termVersion":"2.0.1", "mac":"aabbccddeeff",
         * "ip":"192.168.17.250", "state":"reserved", "cpu":20,
         * "mem":"100MB/512MB", "storage":"200MB/8000MB",
         * "curPlayVideo":"xxxxx.mp4",
         *
         * "ControlCommands":[ { "commandtype":"scrolltext", "curversion":1 }, {
         * "commandtype":"message", "curversion":1 } ] }
         **********************************************************/
        String cpuUsage = ((int) (ClearLog.readCPUUsage() * 100)) + "%";

        ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo mi = new MemoryInfo();
        activityManager.getMemoryInfo(mi);
        String memInfo = mi.availMem / 1024 / 1024 + "MB/" + mi.totalMem / 1024 / 1024 + "MB";

        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        float max;
        float current;
//		current = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
//		max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float soundsPercent = current / max;
        int volPerscent = (int) (soundsPercent * 100);
        // String soundsPercentString = df.format(volPerscent);// 返回的是String类型 

        Log.d("SYSTEM", "max : " + max + " current : " + current + "volPerscent" + volPerscent);

        File root = Environment.getRootDirectory();
        StatFs sf = new StatFs(root.getPath());
        long blockSize = sf.getBlockSize();
        long availCount = sf.getAvailableBlocks();
        long totalCount = sf.getBlockCount();
        String storageInfo = (availCount * blockSize / 1024 / 1024) + "MB/" + (totalCount * blockSize / 1024 / 1024)
                + "MB";

        String movieName = activitySharePre.getString(ClearConstant.Movie_NAME, null);

        int playStatue = activitySharePre.getInt(ClearConstant.Play_Statue, -1);

        String viewName = activitySharePre.getString(ClearConstant.VIEW_NAME, null);

        String statueText = "";

        if (curAccessible == false) {
            statueText = "当前系统处于禁用时间";
        } else {
            if (playStatue == 1) {
                // 播放
                statueText = "正在播放-" + movieName;
            } else if (playStatue == 2) {
                // 暂停
                statueText = "暂停播放" + movieName;
            } else if (playStatue == 3) {
                // 其他页面
                statueText = "菜单界面";
            } else if (playStatue == 4) {
                statueText = "正在插播-" + movieName;
            } else if (playStatue == 5) {
                statueText = movieName;
            }
        }

        Log.i(TAG, "getPlayStatue:" + playStatue + "Access:" + curAccessible + "statueText:" + statueText);

        String paras = "{\"roomid\":\"" + roomid + "\",";
        paras += "\"termType\":\"himediastb\",";
        paras += "\"termVersion\":\"" + termVersion + "\",";
        paras += "\"mac\":\"" + mac + "\",";
        paras += "\"ip\":\"" + ip + "\",";
        paras += "\"state\":\"" + "reserved" + "\",";
        paras += "\"cpu\":\"" + cpuUsage + "\",";
        paras += "\"mem\":\"" + memInfo + "\",";
        paras += "\"storage\":\"" + storageInfo + "\",";
        paras += "\"volume\":\"" + volPerscent + "\",";
        paras += "\"curPlayVideo\":\"" + statueText + "\",";
        paras += "\"ControlCommands\":[{\"";
        paras += "commandtype\":\"" + ClearConstant.STR_SCROLL_TEXT + "\",";
        paras += "\"curScorollVersion\":" + curScorollVersion + "},{";
        paras += "\"commandtype\":\"" + ClearConstant.STR_ACCESS_TIME + "\",";
        paras += "\"curAccessTimeVersion\":" + curAccessTimeVersion + "},{";
        paras += "\"commandtype\":\"" + ClearConstant.STR_INTER_CUT + "\",";
        paras += "\"curInterCutVersion\":" + curInterCutVersion + "},{";
        paras += "\"commandtype\":\"" + ClearConstant.STR_CHANNEL + "\",";
        paras += "\"curChannelVersion\":" + curChannelVersion + "},{";
        paras += "\"commandtype\":\"" + ClearConstant.STR_MODULE_GROUP + "\",";
        paras += "\"curModuleGroupVersion\":" + curModuleGroupVersion + "},{";
        paras += "\"commandtype\":\"" + "message" + "\",";
        paras += "\"curScorollVersion\":" + curMessageVersion + "},{";
        paras += "\"commandtype\":\"" + ClearConstant.STR_VOLUME + "\",";
        paras += "\"curVolumeVersion\":" + -1 + "},{";
        paras += "\"commandtype\":\"" + ClearConstant.STR_UPDATE_MAINMENU + "\",";
        paras += "\"curVolumeVersion\":" + -1 + "},{";
        paras += "\"commandtype\":\"" + ClearConstant.STR_SNAP_SHOT + "\",";
        paras += "\"curSnapShotVersion\":" + -1 + "}]}";
        Log.i(TAG, "post paras :" + paras);

        return paras;
    }

    public String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;

        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private void sendLogStart(String resourceName, String type, String resourceType) {
        mApp.interruptProgramResourceName = resourceName;
        mApp.interruptProgramTimeInS = DateUtil.getCurrentTimeSecond();
        String logInsert = mApp.combinatePostParasString("start", "0", type, resourceType, resourceName, "");
        // ClearLog.logInsert(logInsert);
        mApp.SendLogMode = 1;
        mApp.isInterruptProgram = true;

    }

    private void sendLogEnd(String resource_type) {
        mApp.isInterruptProgram = true;
        // String categoryPath = mApp.interruptProgramContent;
        String logInsert = mApp.combinatePostParasString("stop", "0", "插播", resource_type,
                mApp.interruptProgramResourceName, "");
        ClearLog.logInsert(logInsert);
    }

    // 保存当前播放状态 用于给后台展示终端状态
    private void saveStatue(int state, String movieName) {
        Editor editor = activitySharePre.edit();
        editor.putInt(ClearConstant.Play_Statue, state);
        editor.putString(ClearConstant.Movie_NAME, movieName);
        editor.commit();
    }

    //利用adbshell实现系统重启
    public void execCommand(String command) throws IOException {

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


    private void feedWatchDog() {
        mApp.feedWattchDog();
    }

}
