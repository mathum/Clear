package com.clearcrane.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.clearcrane.activity.ClearApplication;
import com.clearcrane.activity.VoDActivity;
import com.clearcrane.constant.ClearConstant;
import com.clearcrane.constant.clearProject;
import com.clearcrane.log.ClearLog;
import com.clearcrane.log.L;
import com.clearcrane.logic.view.AccessTimeView;
import com.clearcrane.platform.PhilipsDTVView;
import com.clearcrane.player.ClearHiPlayerView;
import com.clearcrane.player.ClearVideoView;
import com.clearcrane.provider.MaterialRequest;
import com.clearcrane.provider.MaterialRequest.OnCompleteListener;
import com.clearcrane.schedule.Channel;
import com.clearcrane.schedule.Material;
import com.clearcrane.tool.AppManager;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.PlatformSettings;
import com.clearcrane.util.PlatformSettings.Platform;
import com.clearcrane.vod.R;
import com.operationservice.MyListener;
import com.operationservice.Unit;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;

//import com.hisilicon.android.mediaplayer.HiMediaPlayer.OnSeekCompleteListener;

public class VoDViewManager {
    public Channel mChannel = null;
    private boolean mChannelFlag = false;
    private final int MSG_SHOW_IMS_VIEW = 2;
    private final int MSG_REMOVE_IMS_VIEW = 3;
    private final int MSG_IS_TIMEOUT = 0x321; //请求已经超时
    private static VoDViewManager INSTANCE = null;
    public VoDActivity context;
    private FrameLayout background_view;
    private FrameLayout foreground_view;
    /*
     * private ClearVideoView background_video; private ClearVideoView
     * movieVideo; private ClearVideoView liveVideo;
     */
    private ClearVideoView videoView;
    private ClearHiPlayerView liveView;

    // private VodDateView dateView;
    boolean isInitialized = false;

    private String backgroundVideoURL = "";
    private String welcomViewJson;
    private String welcomType;
    private String welcomViewType = "MainMenu";
    private String MainView_Type, subViewType;
    private String MainView_Json_URL;
    // LocalBackgroundVideo mLocalBackgroundVideo;
    public static final String TAG = "VoDViewManager";
    public VoDBaseView chnMainMenu = null;
    public VoDBaseView engMainMenu = null;
    public VoDBaseView chnLiveView = null;
    public VoDBaseView engLiveView = null;
    public VoDBaseView languageView = null;
    public String liveViewType = "Live", chnLiveUrl, engLiveUrl;
    public int isStarted = 0;// 0表示系统还没初始化完成，1表示加载首页中，2，播放欢迎视频，3，显示首页
    public boolean settingViewShowed = false;
    public boolean isInLiveView = false;
    public boolean quickShow = false;
    public boolean noWelcome = false;
    public boolean onlyLive = false;
    public boolean isBackupUri = false;
    public Unit mUnit;
    public MyListener myListener;

    public AppManager appManager = null;
    public static boolean isTHJstyle = false;
    public int philipsSource;
    public boolean playingWelcomVideo = false;
    public Typeface typeFace;
    public String project_name = " ";
    private SharedPreferences sharedPreferences;

    public ClearApplication mApp;

    /* add views here */
    public static VoDBaseView newViewByType(String type) {
        /* build a type classname mapping instead??? */
        Log.i(TAG, "type: " + type);
        if (type.compareToIgnoreCase("MainMenu") == 0) {
            return new VoDMainMenuView();
        } else if (type.compareToIgnoreCase("ChnEngLanguage") == 0) {
            return new VoDLanguageCHNENGView();
        } else if (type.compareToIgnoreCase("Live") == 0) {
            return new VoDPrisonLiveView();
        } else if (type.compareToIgnoreCase("Movie_TopRecommend") == 0) {
            return new VodTopRecommendView();
        } else if (type.compareToIgnoreCase("Music") == 0) {
            return new VodMusicView();
        } else if (type.compareToIgnoreCase("Category_List") == 0) {
            return new VodPictureCategoryView();
        } else if (type.compareToIgnoreCase("PicText_Category") == 0) {
            return new VodPictureListView();
        } else if (type.compareToIgnoreCase("PicText_Simple") == 0) {
            return new VodRoomsView();
        } else if (type.compareToIgnoreCase("Ordering") == 0) {
            return new VodOrderingView();
        } else if (type.compareToIgnoreCase("RoomIntroduce") == 0) {
            return new VodRoomsView();
        } else if (type.compareToIgnoreCase("Message") == 0) {
            return new VodMessageView();
        } else if (type.compareToIgnoreCase("Billing") == 0) {
            return new VodBillingInquiriesView();
        } else if (type.compareToIgnoreCase("FullImageShow") == 0) {
            return new VodFullImageShowView();
        } else if (type.compareToIgnoreCase("World_Clock_simatai") == 0) {
            return new NigeriaWorldClockView();
        } else if (type.compareToIgnoreCase("PhilipsDTV") == 0) {

            return new PhilipsDTVView();
        } else if (type.compareToIgnoreCase("ExchangeRate") == 0) {
            return new VodExchagePictureView();
        } else if (type.compareToIgnoreCase("TV_series_list") == 0) {
            return new VideoOnDemandView();
            // return new VodMovieListView();
        } else if (type.compareToIgnoreCase("TV_series_content") == 0) {
            return new VideoSetsView();
        } else if (type.compareToIgnoreCase("Picture_list") == 0) {
            Log.e(TAG, "create pcitextview type " + type);
            return new PicTextView();
        } else if (type.compareToIgnoreCase("Picture_content") == 0) {
            return new VodPDFView();
            // return new VodScalePDFView();
        } else if (type.compareToIgnoreCase("Audio") == 0) {
            // return new VodMusicView();
            return new VoDPerfectMusicView();
            // return new BannerView();
            // return new InterCutMusicView();
        } else if (type.compareToIgnoreCase("video") == 0) {
            return new VodMovieListView();
        } else if (type.compareToIgnoreCase("Html") == 0) {
            return new VodTbsWebView();
        }
        return null;
    }

    public VoDBaseView newViewByType(String type, String subtype) {
        /* build a type classname mapping instead??? */
        if (type.compareToIgnoreCase("LanguageWelcome_CHZ_ENG") == 0) {
            if (subtype.compareToIgnoreCase("native") == 0) {
                if (project_name.contains(clearProject.Nigeria)
                        || project_name
                        .contains(clearProject.Nigeria_BLACKDIAMOND)) {
                    return new NigeriaVodLanguageView();
                }
                return new VoDLanguageCHNENGView();
            } else if (subtype.compareToIgnoreCase("nigeria_welcome") == 0) {
                return new NigeriaVodLanguageView();
            } else if (subtype.compareToIgnoreCase("noWelcome") == 0) {
                noWelcome = true;
                return null;
            } else if (subtype.compareToIgnoreCase("default") == 0) {
                return new VoDLanguageCHNENGView();
            } else if (subtype.compareToIgnoreCase("jianyu") == 0) {
                return new MainPageView();
            }
        } else if (type.compareToIgnoreCase("MainMenu") == 0) {
            if (subtype.compareToIgnoreCase("native") == 0) {
                return new VoDMainMenuView();
            } else if (subtype.compareToIgnoreCase("default") == 0) {
                return new VoDMainMenuView();
            } else if (subtype.compareToIgnoreCase("prison") == 0) {
                return new MainPageView();
            }
        } else {
            return newViewByType(type);
        }
        return null;
    }

    /* a stack stores the foreground view list */
    public Stack<VoDBaseView> foregroundViewStack = null;

    /* no need to care about multi-thread, must called when activity start */
    public static VoDViewManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new VoDViewManager();
        }
        return INSTANCE;
    }

    public void startWork(Context ctx) {
        Log.e(TAG, "startWork time:" + System.currentTimeMillis());
        mApp = (ClearApplication) ctx.getApplicationContext();
        init(ctx);
        ClearConfig.init(ctx);
        clearProject.init(ctx);
        Log.e(TAG, "start work after " + System.currentTimeMillis());
        // mLocalBackgroundVideo = new LocalBackgroundVideo(ctx);
        appManager = new AppManager(ctx);
        isStarted = 1;

        Log.e(TAG, "start work after2 " + System.currentTimeMillis());

    }

    public Channel getChannel() {
        return mChannel;
    }

    public void pushForegroundView(VoDBaseView view) {
        VoDBaseView oldView = null;
        // 堆栈不为空
        if (foregroundViewStack.empty() == false) {
            oldView = foregroundViewStack.peek();
        }
        // 将新的界面压栈，同时将旧的页面隐藏
        foregroundViewStack.push(view);
        Log.i(TAG, "view:" + view);
        // foreground_view.removeView(oldView.getView());
        foreground_view.addView(view.getView());

        if (oldView != null) {
            oldView.hide();
        }
    }

    public void resetforegroundView() {
        Log.i(TAG, "begin reset welcomview");
        VoDBaseView oldView = null;
        VoDBaseView curView = null;

        if (foregroundViewStack == null) {
            return;
        }
        Log.e(TAG, "foregroundViewStack是空的吗");
        if (foregroundViewStack.empty() == false) {
            Log.e(TAG, "foregroundViewStack真的是空的");
            oldView = foregroundViewStack.pop();
            oldView.hide();
        }

        foregroundViewStack.clear();
        // ClearConfig.init(context);
        mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
        mr.setOnCompleteListener(WelcomListen);
        mr.execute(ClearConfig.MAIN_URI);
        Log.i(TAG, "exce in backroung:" + ClearConfig.MAIN_URI);
        // showBackgroundVideo();
    }

    public VoDBaseView getTopView() {

        if (foregroundViewStack.empty() == false) {
            return foregroundViewStack.peek();
        }
        return null;
    }

    public void resumeCurrentView() {
        VoDBaseView resumeView = null;

        if (foregroundViewStack.empty() == false) {
            resumeView = foregroundViewStack.peek();
            resumeView.show();
        }
    }

    public void popForegroundView() {
        Log.e(TAG, "popforegroundview!!!");
        VoDBaseView oldView = null;
        VoDBaseView curView = null;

        /* if it is empty or last, do nothing */
        if (foregroundViewStack.empty() || foregroundViewStack.size() == 1) {
            L.d("WARN, ignore to pop the current foreground view, due to empty or only one exist");
            return;
        }
        /**
         * test refresh homepage!! else if (foregroundViewStack.size() == 2) {
         *
         * resetforegroundView(); return; }
         *
         **/

        oldView = foregroundViewStack.pop();
        curView = foregroundViewStack.peek();

        curView.show();
        curView.back();
        oldView.hide();
        foreground_view.removeView(oldView.getView());

        if (mApp.SendLogMode == 1 && !mApp.isInterruptProgram) {
            mApp.SendLogMode = 0;
            // stop opreation
            String categoryPath = mApp.content + mApp.contentleft;
            String logInsert = mApp.combinatePostParasString("stop", "0", "点播",
                    mApp.viewType, mApp.resourceName, categoryPath);
            ClearLog.logInsert(logInsert);
            Log.i(TAG, "categoryPath STOP: " + logInsert);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        try {
            L.i("key: " + keyCode + ", " + event);
            VoDBaseView curView = foregroundViewStack.peek();
            if (curView != null) {
                Log.i("key", "return Pressed: " + keyCode + ", " + event + ", "
                        + curView);
                return curView.onKeyDown(keyCode, event);
            } else {
                Log.e("VManager", "curview is null !");
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private VoDViewManager() {

    }

    private void init(Context ctx) {
        context = (VoDActivity) ctx;
        typeFace = Typeface.createFromAsset(context.getAssets(),
                ClearConstant.TEXT_FONT_FOR_NIGERIA_PATH);
        background_view = (FrameLayout) context
                .findViewById(R.id.global_background_view);
        foreground_view = (FrameLayout) context
                .findViewById(R.id.global_foreground_view);
        videoView = (ClearVideoView) context
                .findViewById(R.id.global_video_view);
        liveView = (ClearHiPlayerView) context
                .findViewById(R.id.global_live_view);
        videoView.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer arg0) {
                // TODO Auto-generated method stub
                Log.e("xb", "intercut播放完成");
                videoView.seekTo(0);
                videoView.start();
            }
        });

        if (PlatformSettings.getPlatform() == Platform.philips
                || PlatformSettings.getPlatform() == Platform.tcl
                || PlatformSettings.getPlatform() == Platform.tcl_tv338
                || PlatformSettings.getPlatform() == Platform.tcl_icescreen) {
            Log.i("xb", "解决飞利浦播放器第一次播放没有声音问题");
            // 解决飞利浦播放器第一次播放没有声音问题
            startLiveVideo("android.resource://" + ctx.getPackageName() + "/"
                    + R.raw.philips);
        }

        hideBackgroundVideo();
        // liveVideo =
        // (ClearVideoView)context.findViewById(R.id.global_live_video);
        // movieVideo =
        // (ClearVideoView)context.findViewById(R.id.global_movie_video);
        // dateView = (VodDateView)context.findViewById(R.id.date);

        /* create the foreground view stack */
        foregroundViewStack = new Stack<VoDBaseView>();

        // isInitialized = true;
    }

    // }

    public int getActivityMode() {
        return context.getActivityMode();
    }

    public void setActivityMode(int mode) {
        context.setActivityMode(mode);
    }

    public void showImsView() {
        ArrayList<Material> list = mChannel.curPlayProgramSegment.materialList;
        if (list.size() == 0) {
            Log.e(TAG, "error show ims view ");
            return;
        }
        hideLiveVideo();
        FullScreenChannelView fscv = new FullScreenChannelView();
        fscv.init(context, list);
        pushForegroundView(fscv);
        showLiveVideo();
        startLiveVideo(fscv.getVideoPath());
    }

    public void playWelcomVideo(String url) {
        Log.i(TAG, "playing welcom video" + url);
        playingWelcomVideo = true;
        final String newUrl = url;
        // final String newUrl = mLocalBackgroundVideo.getUrl(url);
        Log.i(TAG, "welcom video true url:" + newUrl);
        videoView.setLooping(false);
        showBackgroundVideo();
        videoView.setVideoPath(newUrl);
        // videoView.setOnCompletionListener(new OnCompletionListener() {
        // @Override
        // public void onCompletion(MediaPlayer arg0) {
        // // TODO Auto-generated method stub
        // Log.i(TAG, "playWelcomVideo complete:" + newUrl);
        // playingWelcomVideo = false;
        // playBackgroundVideo();
        // pushForegroundView(languageView);
        // isStarted = 3;
        // context.goingShow();
        // }
        //
        // });

        videoView.start();

    }

    // public void setBackgroundVideoURL(String url) {
    // String newUrl = mLocalBackgroundVideo.getUrl(url);
    // Log.i("in http startbackgroundvideo", "url:" + newUrl);
    // backgroundVideoURL = newUrl;
    // ClearConfig.putString(ClearConstant.BACKGROUND_VIDEO_URL,
    // backgroundVideoURL);
    // }

    public void startBackgroundVideo(String url) {
        // String newUrl = mLocalBackgroundVideo.getUrl(url);
        // Log.i("in http startbackgroundvideo","url:"+newUrl);
        // backgroundVideoURL = newUrl;
        // videoView.setLooping(true);
        // videoView.setVideoPath(newUrl);
        // videoView.start();
    }

    public void setBackgroundVideoDisplayArea(int x, int y, int w, int h) {
        videoView.setDisplayArea(x, y, w, h);
    }

    public void stopBackgroundVideo() {
        videoView.stopPlayback();
        videoView.setVisibility(View.GONE);
        background_view.setVisibility(View.GONE);
    }

    public void showBackgroundVideo() {
        background_view.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.VISIBLE);
    }

    public void hideBackgroundVideo() {
        stopBackgroundVideo();
        videoView.setVisibility(View.GONE);
        background_view.setVisibility(View.GONE);
    }

    public void pauseBackgroundVideo() {
        if (videoView.canPause())
            videoView.pause();
    }

    public void playBackgroundVideo() {
        /*
         * TODO, FIXME, start from the begin anyway, should we support
         * pause/resume
         */
        if (!playingWelcomVideo)
            startBackgroundVideo(backgroundVideoURL);
    }

    public void startLiveVideo(String url) {

        videoView.setLooping(true);
        videoView.setVideoPath(url);
        videoView.start();
    }

    // add by winter
    // to start play directly
    public void startLiveViewRightNow() {
        videoView.start();
    }

    public void setLiveVideoDisplayArea(int x, int y, int w, int h) {
        videoView.setDisplayArea(x, y, w, h);
        Log.d("hemeiplayer set dis liveview", "w" + w + "h" + h);
    }

    public void stopLiveVideo() {

        videoView.stopPlayback();
    }

    public void showLiveVideo() {
        background_view.setVisibility(View.VISIBLE);

        videoView.setVisibility(View.VISIBLE);
    }

    public void hideLiveVideo() {
        stopLiveVideo();
        background_view.setVisibility(View.GONE);

        videoView.setVisibility(View.GONE);
    }

    public void startMovieVideo(String url) {
        if (context.getActivityMode() == -1) {
            videoView.setLooping(true);
        } else {
            videoView.setLooping(false);
        }
        videoView.setVideoPath(url);
        videoView.start();

        // videoView.start();

        // videoView.setOnCompletionListener(new OnCompletionListener() {
        // @Override
        // // Log.i(TAG,"playWelcomVideo complete:"+ url);
        // public void onCompletion(MediaPlayer arg0) {
        // // TODO Auto-generated method stub
        // if (context.getActivityMode() == -1) {
        //
        // } else {
        // popForegroundView();
        // }
        // }
        //
        // });
    }

    public void setMovieVideoDisplayArea(int x, int y, int w, int h) {
        videoView.setDisplayArea(x, y, w, h);
    }

    public void stopMovieVideo() {
        videoView.getCurrentPosition();
        videoView.stopPlayback();
    }

    public void showMovieVideo() {
        background_view.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.VISIBLE);

    }

    public void hideMovieVideo() {
        stopMovieVideo();
        background_view.setVisibility(View.GONE);
        videoView.setVisibility(View.GONE);
    }

    public void pauseMovieVideo() {
        if (videoView.canPause())
            videoView.pause();
    }

    public void playMovieVideo() {
        if (!videoView.isPlaying())
            videoView.start();
    }

    public void playMovieVideo2() {
        videoView.start();
    }

    public long getMovieDuration() {
        return videoView.getDuration();
    }

    public void setMoviePreparedListener(OnPreparedListener listener) {
        videoView.setOnPreparedListener(listener);
    }

    public boolean movieVideoIsPlaying() {
        return videoView.isPlaying();
    }

    public long getMovieCurrentPosition() {
        return videoView.getCurrentPosition();
    }

    public void movieSeekTo(int seekTime) {
        Log.i("in video", "seekTime:" + seekTime);
        videoView.seekTo(seekTime);
    }

    public void movieRequestFocus() {
        videoView.requestFocus();
    }

    public void startMusic(String url) {
        videoView.setLooping(true);
        videoView.setVideoPath(url);
        videoView.start();
    }

    public void stopMusic() {
        videoView.stopPlayback();
    }

    public void setVideoPreparedListener(OnPreparedListener listener) {
        videoView.setOnPreparedListener(listener);
    }

    public long getVideoCurrentPosition() {
        return videoView.getCurrentPosition();
    }

    public long getVideoDuration() {
        return videoView.getDuration();
    }

    public void videoSeekTo(int seekTime) {
        videoView.seekTo(seekTime);
    }

    private boolean isRequestTimeOut = true;  //请求是否过期
    private boolean isUpdateUI = true;  //是否更新UI
    private OnCompleteListener WelcomListen = new OnCompleteListener() {

        @Override
        public void onDownloaded(Object result) {
            // TODO Auto-generated method stub
            isRequestTimeOut = false;
            welcomViewJson = (String) result;

            if (welcomViewJson == null) {
                resetForegroundView();
                return;
            }

            //本地数据更新
            if (sharedPreferences == null) {
                sharedPreferences = context.getSharedPreferences("launcher", Context.MODE_PRIVATE);
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("result", welcomViewJson);
            editor.commit();
            Log.d(TAG, "数据更新成功;");
            if (isUpdateUI) {
                Log.d(TAG, "UI更新成功;");
                setResult(welcomViewJson);
                return;
            }
        }

        @Override
        public void onComplete(boolean result) {
            // TODO Auto-generated method stub
        }
    };

    private void setResult(String welcomViewJson) {

        if (welcomViewJson == null) {
            resetForegroundView();
            return;
        }

        try {// 网络正常开始解析json，并生成欢迎页
            VoDViewManager.getInstance().hideBackgroundVideo();
            JSONTokener jsonParser = new JSONTokener(welcomViewJson);
            Log.i(TAG,
                    "beiging lodaing welcomJson: " + jsonParser.toString());
            JSONObject object = (JSONObject) jsonParser.nextValue();

            // welcomType = object.getString("welcome_type");
            // welcomViewType = object.getString("welcome_style_type");

            // 菜单界面信息,如果不需要欢迎页，直接加载菜单页面
            MainView_Type = object.getString("MainViewType");
            subViewType = object.getString("MainView_style_type");
            languageView = newViewByType(MainView_Type, subViewType);
            MainView_Json_URL = ClearConfig.getJsonUrl(context, object.getString("MainView_Json_URL"));
            languageView.init(context, MainView_Json_URL);
            Log.i(TAG, "MainView_Type: " + MainView_Type + " subViewType： "
                    + subViewType);
            if (!subViewType.equalsIgnoreCase("onlyLive")) {
                Log.e(TAG, "in mainview");
                VoDViewManager.getInstance().showBackgroundVideo();
                // VoDViewManager.getInstance().playBackgroundVideo();
                isStarted = 2;
                if (!playingWelcomVideo) {
                    pushForegroundView(languageView);
                    isStarted = 3;
                }
            } else {// 直接进直播
                onlyLive = true;
                isStarted = 2;
                // 等待mainmenu初始化执行liveViewInit();
            }
        } catch (JSONException e) {
            Log.e(TAG, "parse welcome json error:" + e);

        }
    }

    public void projectSetting() {
        project_name = ClearConfig.getString(clearProject.ProjectName, " ");
        Log.i(TAG, "project setting:" + project_name);
        if (project_name.contains(clearProject.Nuogeya)) {
            Log.i(TAG, "PHILIPS setting");
            myListener = new MyListener() {
                @Override
                public void bindSuccess() {
                    Log.i(TAG, "bind success");
                    // TODO Auto-generated method stub
                    mHandler.obtainMessage(1).sendToTarget();
                }
            };
            mUnit = new Unit(context, myListener);
        } else if (project_name.contains(clearProject.Nigeria)
                || project_name.contains(clearProject.Nigeria_BLACKDIAMOND)) {
            Log.i(TAG, "nigeria setting");
            ClearConstant.SETTING_KEYS = "13112006";
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case 1:
                    mUnit.getInputSource();
                    Log.i(TAG, "setting volume");
                    mUnit.init();
                    mUnit.setStreamVolume(10);
                    mUnit.SetWifiTether(false);
                    mUnit.release();
                    break;
                case MSG_SHOW_IMS_VIEW:
                    // showImsView();
                    break;
                case MSG_REMOVE_IMS_VIEW:
                    // removeImsView();
                    break;
                case ClearConstant.MSG_START_ACCESS_TIME:
                    break;
                case ClearConstant.MSG_STOP_ACCESS_TIME:
                    VoDBaseView topview = VoDViewManager.getInstance().getTopView();
                    if (topview instanceof AccessTimeView) {
                        VoDViewManager.getInstance().popForegroundView();
                    }
                    break;
                case MSG_IS_TIMEOUT:
                    //请求未超时
                    if (!isRequestTimeOut) {
                        break;
                    }
                    //超时请求
                    //1.查看本地是存有启动文件
                    if (sharedPreferences == null) {
                        sharedPreferences = context.getSharedPreferences("launcher", Context.MODE_PRIVATE);
                    }
                    String result = sharedPreferences.getString("result", "");
                    //无该路径，第一次访问
                    if (TextUtils.isEmpty(result)) {
                        isUpdateUI = true;
                        break;
                    }

                    //设置请求已经超时
                    Log.e(TAG, "请求已超时!");

                    isUpdateUI = false;
                    isRequestTimeOut = true;
                    //2.直接解析
                    setResult(result);

                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    // public void startEgg(EggsFactory.Eggs egg) {
    // context.mEggsView.startEggs(egg);
    // }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            // Toast.makeText(context, intent.getAction(), 1).show();
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiInfo = manager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo activeInfo = manager.getActiveNetworkInfo();

            if (activeInfo != null && activeInfo.isAvailable()) {

            }
        } // 如果无网络连接activeInfo为null

    }

    // public void forceSetFrontView(VoDBaseView baseview){
    // Message msg = mHandler.obtainMessage(ClearConstant.MSG_START_CHANNEL);
    // msg.obj = baseview;
    // msg.sendToTarget();
    // }

    public void forceOverBack(int what) {
        mHandler.obtainMessage(what).sendToTarget();
    }

    MaterialRequest mr;

    public void resetForegroundView() {
        Log.i(TAG, "begin reset welcomview");
        if (foregroundViewStack == null) {
            return;
        }
        VoDBaseView oldView = null;
        if (foregroundViewStack.empty() == false) {
            oldView = foregroundViewStack.peek();
            oldView.hide();
        }
        // hideBackgroundVideo();
        ClearConfig.setLanguageIdByIconName("CHZ");
        foregroundViewStack.clear();
        isStarted = 1;
        isBackupUri = false;
        chnLiveView = null;
        engLiveView = null;
        languageView = null;
        chnMainMenu = null;
        engMainMenu = null;
        isUpdateUI = true;
        mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
        Log.i(TAG, "set listener");
        mr.setOnCompleteListener(WelcomListen);
        Log.i(TAG, "excc url");
        //mr.execute(ClearConfig.MAIN_URI);
        mr.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ClearConfig.MAIN_URI);
        Log.i(TAG, "exce in backroung:" + ClearConfig.MAIN_URI);
        // showBackgroundVideo();
        mHandler.sendEmptyMessageDelayed(MSG_IS_TIMEOUT, 10 * 1000);
    }

    public void showLanguageView() {
        // 只有一个view时，直接返回
        quickShow = true;
        if (foregroundViewStack.size() < 2)
            return;
        VoDBaseView oldView = null;
        oldView = foregroundViewStack.peek();
        oldView.onKeyBack();
        for (int i = foregroundViewStack.size() - 1; i > 0; i--) {
            oldView = foregroundViewStack.pop();
            foreground_view.removeView(oldView.getView());
        }
        foregroundViewStack.peek().show();
        quickShow = false;
    }

    public void showMenuView() {

        if (ClearConfig.LanguageID == 1) {
            showLanguageView();
            ClearConfig.LanguageID = 1;
            VoDViewManager.getInstance().pushForegroundView(chnMainMenu);
            chnMainMenu.show();
        } else if (ClearConfig.LanguageID == 2) {
            showLanguageView();
            ClearConfig.LanguageID = 2;
            VoDViewManager.getInstance().pushForegroundView(engMainMenu);
            engMainMenu.show();
        }

    }

    public void liveViewInit(String type, Context ctx, String u) {
        Log.e(TAG, "live view init");
        if (ClearConfig.LanguageID == 1) {
            Log.i(TAG, "LIVE VIEW INIT chn");
            liveViewType = type;
            chnLiveUrl = u;

        } else if (ClearConfig.LanguageID == 2) {
            Log.i(TAG, "LIVE VIEW INIT ENG");
            liveViewType = type;
            engLiveUrl = u;
        }

        if (onlyLive) {
            Log.e(TAG, "chnLiveUrl:" + chnLiveUrl + ";engLiveUrl:" + engLiveUrl);
            Log.e(TAG, "show live view");
            showLiveView();
            isStarted = 3;
        }
    }

    public void showLiveView() {
        if (ClearConfig.LanguageID == 1) {
            VoDViewManager.getInstance().stopBackgroundVideo();
            VoDViewManager.getInstance().hideBackgroundVideo();
            VoDBaseView newView = VoDViewManager.newViewByType(liveViewType);
            if (newView != null) {
                newView.init(context, chnLiveUrl);
                VoDViewManager.getInstance().pushForegroundView(newView);
            }
        } else {
            VoDViewManager.getInstance().stopBackgroundVideo();
            VoDViewManager.getInstance().hideBackgroundVideo();
            VoDBaseView newView = VoDViewManager.newViewByType(liveViewType);
            if (newView != null) {
                newView.init(context, chnLiveUrl);
                VoDViewManager.getInstance().pushForegroundView(newView);
            }
        }
    }

    public void setMovieViewLoop() {
        videoView.setLooping(true);
    }

    public ClearVideoView getVideoView() {
        return videoView;
    }

    public ClearHiPlayerView getLiveView() {
        return liveView;
    }

    public void setMovieViewCompleteListener(
            OnCompletionListener onCompletionListener) {
        videoView.setOnCompletionListener(onCompletionListener);
    }

    public String isVideoViewStatues() {
        if (videoView == null)
            return "kong ";
        return videoView.isInPlaybackState() + "";
    }

}
