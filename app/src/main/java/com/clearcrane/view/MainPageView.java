package com.clearcrane.view;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.log.ClearLog;
import com.clearcrane.provider.MaterialRequest;
import com.clearcrane.provider.MaterialRequest.OnCompleteListener;
import com.clearcrane.provider.MaterialRequest.OnDownLoadCompleListener;
import com.clearcrane.schedule.DateUtil;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.ImageUtil;
import com.clearcrane.util.InstallApkUtils;
import com.clearcrane.vod.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressLint("NewApi")
public class MainPageView extends VoDBaseView {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final long KEEP_ALIVE = 10L;
    private static final int APPER_UNINSTALL_PIC = 519;
    private static final int DISAPPER_UNINSTALL_PIC = 520;
    private static final int BEGIN_LOADING = 521;
    private static final int END_LOADING = 522;
    private static final int BEGIN_INSTALLED = 523;
    private static final int END_INSTALLED = 524;
    private static final int NEW_APP_LAUNCH = 525;
    private static final int NEW_APP_QUIT = 526;
    private static final int UPDATE_PROGRESSBAR = 527;
    private String MainPageJson;
    // 子菜单布局
    private GridLayout subIconLayout;
    // 主菜单布局
    private LinearLayout mainIconLayout;
    // 分页按钮布局
    private LinearLayout pageIconLayout;

    private LayoutTransition mLayoutTransition;

    private boolean Gesture = false;

    // 时间显示的textview
    private TextView timetv;

    private ImageView subprevButton;
    private ImageView mainprevButton;

    private ImageView subnextButton;
    private ImageView mainnextButton;

    private TextView maintitle;
    private ImageView mainlogo;
    private ImageView mainbackground;

    private String ViewName = "MainPage";
    public static final String TAG = "MainPageView";

    // 盒子群组ID
    private int GroupId;

    // 时间字符串信息
    private String timetext;

    // private SharedPreferences spInstall;
    public SharedPreferences moduleGroupSharePre;
    public SharedPreferences accessTimeSharePre;
    public SharedPreferences activitySharePre;

    private boolean isLoaded = false;
    private boolean isExist = false;
    private ArrayList<MainIcon> mainIconList = new ArrayList<MainIcon>();
    // 子菜单数据集合
    private ArrayList<SubIcon> subIconList;
    private ArrayList<TextView> pageIconList = new ArrayList<TextView>();

    int curFocusIndex = -1;// 顶层menu当前位置
    int curSubFocusIndex = -1;// 子menu当前位置

    private long lastTime = 0;// 按键事件控制
    private int mMaxMainPageCount;// 记录主页面最大分页数
    private int mMaxSubPageCount;// 记录子页面最大分页数
    private byte focus_control = -1;// 1焦点落在主菜单，2焦点落在子菜单 主要用来判断焦点是在主菜单还是子菜单
    private byte direction_control = -1;// 1向左切换，2向右切换
    private int mCurrentMainPageCount = -1;// 记录当前主页面的页数
    private int mCurrentSubPageCount = -1;// 记录当子主页面的页数

    private String packageName;
    private String apkLocation = Environment.getExternalStorageDirectory().toString() + "/";
    private File mTempFile;
    private RandomAccessFile mAccessFile;
    private Map<String, Boolean> map = new HashMap<String, Boolean>();
    private Map<String, ApkInfo> apkInfoMap = new HashMap<String, ApkInfo>();// 保存从后台下载过来的所有应用数据
    private boolean isLoading;
    private boolean isInstalling;
    private boolean isLaunchNewApp;
    // private DBHelper db = null;
    private RoundProgressBar progressBar = null;
    private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
    private ArrayList<String> appList = null;// 保存当前盒子中所有的app包名信息
    private final int MSG_IS_TIMEOUT = 0x321; //请求已经超时

    private boolean isRequestTimeOut = true;  //请求是否过期
    private boolean isUpdateUI = true;  //是否更新UI
    private SharedPreferences sharedPreferences;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {

                case APPER_UNINSTALL_PIC:
                    // refrshCurrentSubIcon(true);
                    break;

                case DISAPPER_UNINSTALL_PIC:
                    // refrshCurrentSubIcon(false);
                    break;

                case BEGIN_LOADING:
                    isLoading = true;
                    break;

                case END_LOADING:
                    isLoading = false;
                    break;
                case BEGIN_INSTALLED:
                    isInstalling = true;
                    break;

                case END_INSTALLED:
                    isInstalling = false;
                    break;
                case NEW_APP_LAUNCH:
                    isLaunchNewApp = true;
                    break;

                case NEW_APP_QUIT:
                    isLaunchNewApp = false;
                    break;

                case UPDATE_PROGRESSBAR:
                    if (progressBar != null) {
                        int temp = msg.getData().getInt("progress");
                        progressBar.setProgress(temp);
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
                    String result = sharedPreferences.getString("mainMenu", "");
                    //无该路径，第一次访问
                    if (TextUtils.isEmpty(result)) {
                        isUpdateUI = true;
                        break;
                    }
                    if (mr != null && !mr.isCancelled() && mr.getStatus() == AsyncTask.Status.RUNNING) {
                        mr.cancel(true);
                    }
                    //设置请求已经超时
                    Log.e(TAG, "请求已超时!");

                    isUpdateUI = false;
                    isRequestTimeOut = true;
                    //2.直接解析
                    MainPageJson = result;
                    setResult();

                    break;
                default:

                    timetv.setTextSize(TypedValue.COMPLEX_UNIT_PX, ClearConfig.getScreenHeight() / 20);
                    timetv.setTextColor(Color.rgb(255, 255, 255));
                    timetv.setText(timetext);
                    super.handleMessage(msg);


                    break;
            }

        }
    };

    private Timer timer = new Timer();

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "MainPageVivew#" + mCount.getAndIncrement());
        }
    };

    public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
            KEEP_ALIVE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), sThreadFactory);

    class SubIcon {
        String nextViewID = null;
        String name = null;
        String iconURL = null;
        String iconBgURL = null;
        String type = null;
        String jsonUrl = null;
        int[] groups = new int[100];
        boolean iconVisibilty = false;
        FrameLayout subiconview;
    }

    class ApkInfo {
        public String apkUrl = null;
        public String packageName = null;
    }

    class MainIcon {
        String name = null;
        int subCount = 0;
        Button mainicon_text;
        ArrayList<SubIcon> subIconList = new ArrayList<SubIcon>();
    }

    ;

    MaterialRequest mr;

    public void init(Context ctx, String u) {
        super.init(ctx, u);
        Log.e("xb", "u" + u);
        view = LayoutInflater.from(context).inflate(R.layout.main_page, null);
        initLayoutInXml();

        /**
         * getWritableDatabase()和getReadableDatabase()
         * 方法都可以获取一个用于操作数据库的SQLiteDatabase实例。
         * 但getWritableDatabase()方法以读写方式打开数据库，一旦数据库的磁盘空间满了，数据库就只能读而不能写，
         * getWritableDatabase()打开数据库就会出错。getReadableDatabase()方法先以读写方式打开数据库，
         * 倘若使用如果数据库的磁盘空间满了，就会打开失败，当打开失败后会继续尝试以只读方式打开数据库.
         */

        // db = DBHelper.getInstance(ctx);
        moduleGroupSharePre = context.getSharedPreferences(ClearConstant.STR_MODULE_GROUP, Context.MODE_PRIVATE);
        accessTimeSharePre = context.getSharedPreferences(ClearConstant.ACCESSTIME_FILE, Context.MODE_PRIVATE);
        activitySharePre = context.getSharedPreferences(ClearConstant.Activity_FILE, Context.MODE_PRIVATE);

        saveStatue();

        isUpdateUI = true;
        mr = new MaterialRequest(ctx, ClearConfig.TYPE_JSON);
        mr.setOnCompleteListener(MainPageJsonListen);
        mr.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this.url);
        Log.i(TAG, "MainPageView init ok!");

        mHandler.sendEmptyMessageDelayed(MSG_IS_TIMEOUT, 4 * 1000);

        appList = getAppList();
    }

    private void saveStatue() {
        Editor editor = activitySharePre.edit();
        editor.putInt(ClearConstant.Play_Statue, 3);
        editor.putString(ClearConstant.VIEW_NAME, ViewName);
        editor.commit();
    }

    private void updateTimeZone() {
        /**
         * long currentTime =DateUtil.getInstance().getDate(); Date date = new
         * Date(currentTime); timetext = formatter.format(date);
         *
         **/


        long currentTime = DateUtil.getCurrentTimeMillSecond();
        Date date = new Date(currentTime);
        timetext = formatter.format(date);


        //timetext = accessTimeSharePre.getString("NativeTime", null);
        Log.i("2016", ":" + timetext);
    }

    private void initLayoutInXml() {
        subIconLayout = (GridLayout) view.findViewById(R.id.subView);
        mainIconLayout = (LinearLayout) view.findViewById(R.id.subMenu);
        pageIconLayout = (LinearLayout) view.findViewById(R.id.pageIcon);

        timetv = (TextView) view.findViewById(R.id.time_clock);

        subprevButton = (ImageView) view.findViewById(R.id.prev_Button);
        subnextButton = (ImageView) view.findViewById(R.id.next_Button);
        mainprevButton = (ImageView) view.findViewById(R.id.prev_Button2);
        mainnextButton = (ImageView) view.findViewById(R.id.next_Button2);

        maintitle = (TextView) view.findViewById(R.id.main_title);
        mainlogo = (ImageView) view.findViewById(R.id.logo);
        mainbackground = (ImageView) view.findViewById(R.id.main_bg_pic);

        mLayoutTransition = new LayoutTransition();
        // 为GridLayout设置mLayoutTransition对象
        subIconLayout.setLayoutTransition(mLayoutTransition);
        // 设置每个动画持续的时间
        mLayoutTransition.setDuration(300);
    }

    private void customLayoutTransition(boolean Gesture) {

        if (Gesture == true) {
            /**
             * 向右翻页 view出现时 view自身的动画效果，右移
             */
            ObjectAnimator animator1 = ObjectAnimator.ofFloat(null, "translationX", ClearConfig.getScreenWidth(), 0F)
                    .setDuration(mLayoutTransition.getDuration(LayoutTransition.APPEARING));
            mLayoutTransition.setAnimator(LayoutTransition.APPEARING, animator1);

            /**
             * view消失时 view自身的动画效果，左移
             */

            ObjectAnimator animator2 = ObjectAnimator.ofFloat(null, "translationX", 0F, -ClearConfig.getScreenWidth())
                    .setDuration(mLayoutTransition.getDuration(LayoutTransition.DISAPPEARING));
            mLayoutTransition.setAnimator(LayoutTransition.DISAPPEARING, animator2);

        } else {
            ObjectAnimator animator1 = ObjectAnimator.ofFloat(null, "translationX", -ClearConfig.getScreenWidth(), 0F)
                    .setDuration(mLayoutTransition.getDuration(LayoutTransition.APPEARING));
            mLayoutTransition.setAnimator(LayoutTransition.APPEARING, animator1);

            ObjectAnimator animator2 = ObjectAnimator.ofFloat(null, "translationX", 0F, ClearConfig.getScreenWidth())
                    .setDuration(mLayoutTransition.getDuration(LayoutTransition.DISAPPEARING));
            mLayoutTransition.setAnimator(LayoutTransition.DISAPPEARING, animator2);

        }

    }

    private void setTitleFocused() {
        if (maintitle != null) {
            maintitle.requestFocus();
        }
    }

    @Override
    public boolean onKeyDpadUp() {
        setTitleFocused();
        long last = System.currentTimeMillis();
        if (last - lastTime < 300) {
            return true;
        }
        lastTime = last;

        if (isLaunchNewApp) {
            return true;
        }

        switch (focus_control) {
            // 当焦点在子菜单 准备向主菜单切换时
            case 2:

                if (curSubFocusIndex < 0) {
                    curSubFocusIndex = 0;
                }

                if (subIconList.size() > 0) {
                    final int M = (mCurrentSubPageCount - 1) * 10;
                    final int P = Math.min(M + 10, subIconList.size());
                    if (curSubFocusIndex >= M && curSubFocusIndex < P) {
                        int tempSubFocusIndex = curSubFocusIndex;
                        int nextSubFocusIndex = curSubFocusIndex - 5;
                        // 判断是在第二行还是第一行 如果是第二行那么就跳转到第一行 如果是第一行那么直接跳转至主菜单
                        if (nextSubFocusIndex >= M && nextSubFocusIndex < P) {
                            changeSubItemFoucusStatus(tempSubFocusIndex, false);
                            curSubFocusIndex = nextSubFocusIndex;
                            // 设置子菜单选项为选中状态
                            changeSubItemFoucusStatus(curSubFocusIndex, true);

                        } else {
                            focus_control = 1;
                            onChageMainItemFoucusStatus(curFocusIndex);
                            showSubItemArrow(mCurrentSubPageCount);
                            changeSubItemFoucusStatus(tempSubFocusIndex, false);
                        }
                    }
                } else {
                    focus_control = 1;
                    onChageMainItemFoucusStatus(curFocusIndex);
                    showSubItemArrow(mCurrentSubPageCount);
                }

                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public boolean onKeyDpadDown() {

        if (isLaunchNewApp) {
            return true;
        }

        setTitleFocused();
        long last = System.currentTimeMillis();
        if (last - lastTime < 300) {
            return true;
        }
        lastTime = last;

        switch (focus_control) {
            case 1:

                focus_control = 2;
                if (curSubFocusIndex <= -1) {
                    curSubFocusIndex = 0;
                }
                onChageMainItemFoucusStatus(curFocusIndex);
                showSubItemArrow(mCurrentSubPageCount);
                changeSubItemFoucusStatus(curSubFocusIndex, true);

                break;

            case 2:

                if (curSubFocusIndex < 0) {
                    curSubFocusIndex = 0;
                }
                // 当主菜单只有一个，子菜单也只有一个的时候 不作任何操作
                if (curFocusIndex == mainIconList.size() - 1 && subIconList.size() - 1 == curSubFocusIndex) {
                    return false;
                }

                final int M = (mCurrentSubPageCount - 1) * 10;
                final int P = Math.min(M + 10, subIconList.size());
                if (curSubFocusIndex >= M && curSubFocusIndex < P) {
                    int tempSubFocusIndex = curSubFocusIndex;

                    int nextSubFocusIndex = curSubFocusIndex + 5;
                    // 判断焦点在子菜单的第一行还是第二行
                    if (nextSubFocusIndex >= M && nextSubFocusIndex < P) {

                        changeSubItemFoucusStatus(tempSubFocusIndex, false);
                        curSubFocusIndex = nextSubFocusIndex;
                        changeSubItemFoucusStatus(curSubFocusIndex, true);

                    } else {

                        if (curSubFocusIndex < P - 1 && curSubFocusIndex % 10 < 5) {
                            changeSubItemFoucusStatus(tempSubFocusIndex, false);
                            changeSubItemFoucusStatus(++curSubFocusIndex, true);
                        } else {
                            changeSubItemFoucusStatus(curSubFocusIndex, true);
                        }
                    }
                }

                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 改变主菜单条目状态颜色
     *
     * @param curFocusIndex
     */
    private void onChageMainItemFoucusStatus(int curFocusIndex) {

        final int M = (mCurrentMainPageCount - 1) * 7;
        final int N = Math.min(M + 7, mainIconList.size());

        for (int i = M; i < N; i++) {
            MainIcon mainIcon = mainIconList.get(i);

            // 当焦点落在主菜单时
            if (focus_control == 1) {
                // 所有字体设置为白色
                mainIcon.mainicon_text.setTextColor(Color.argb(0xff, 0xff, 0xff, 0xff));
            }
            // 当焦点落在子菜单时
            if (focus_control == 2) {
                // 所有字体设置为灰暗，除被选中的主菜单选项
                if (i == curFocusIndex) {
                    mainIcon.mainicon_text.setTextColor(Color.argb(0xff, 0xff, 0xff, 0xff));
                } else {
                    mainIcon.mainicon_text.setTextColor(Color.argb(0x33, 0xff, 0xff, 0xff));
                }
            }

        }
        // 当焦点在主菜单时 改变选中项背景出现
        if (focus_control == 1) {
            changeMainItemFoucusBg(curFocusIndex, true);
        }
        // 当焦点在子菜单时 改变选中项背景消失
        if (focus_control == 2) {
            changeMainItemFoucusBg(curFocusIndex, false);
        }
    }

    @Override
    public boolean onKeyDpadLeft() {

        if (isLaunchNewApp) {
            return true;
        }

        setTitleFocused();
        Gesture = false;
        customLayoutTransition(Gesture);
        long last = System.currentTimeMillis();
        if (last - lastTime < 300) {
            return true;
        }
        lastTime = last;

        if (focus_control == 1) {
            if (curFocusIndex <= 0) {
                return false;
            } else {// 主菜单翻页
                // if (mainIconList.get(curFocusIndex -
                // 1).mainicon_text.getVisibility() == View.VISIBLE) {
                int tempFocusIndex = curFocusIndex;
                curFocusIndex--;
                Log.i(TAG, "left: " + " tempFocusIndex:" + tempFocusIndex + " curFocusIndex:" + curFocusIndex);
                // 向左翻页
                curSubFocusIndex = 0;
                final int tempCurrentMainPageCount = mCurrentMainPageCount;
                int thresholdRightIndex = (mCurrentMainPageCount - 1) * 7;// 需要切换的临界值
                if (tempFocusIndex == thresholdRightIndex && mCurrentMainPageCount >= 1) {
                    mCurrentMainPageCount--;
                    addMainLayoutView(mCurrentMainPageCount, curFocusIndex);
                }
                changeMainItemFoucusBg(tempFocusIndex, false);
                changeMainItemFoucusBg(curFocusIndex, true);
                addSubLayoutView(curFocusIndex);
            }
            // }
        }

        if (focus_control == 2) {
            Log.i(TAG, "FOCUS_CONTROL:" + focus_control);

            if (curFocusIndex < 0) {
                curFocusIndex = 0;
                return false;
            }

            if (curSubFocusIndex < 0) {
                return false;
            } else {
                if (0 == curSubFocusIndex || 5 == curSubFocusIndex) {
                    if (0 == curFocusIndex) {
                        return false;
                    }
                }
                if (mCurrentSubPageCount < 1) {
                    mCurrentSubPageCount = 1;
                }

                // 每一页的最大模块个数
                final int M = (mCurrentSubPageCount - 1) * 10;
                final int P = Math.min(M + 10, subIconList.size());

                if (M == P && P == 0) {
                    // 主菜单切换上一页,向左翻页
                    int tempFocusIndex = curFocusIndex;
                    curFocusIndex--;

                    if (curFocusIndex < 0) {
                        curFocusIndex = 0;
                        return false;
                    }
                    mCurrentSubPageCount = 1;
                    final int tempCurrentMainPageCount = mCurrentMainPageCount;
                    int thresholdLeftIndex = (mCurrentMainPageCount - 1) * 7;
                    if (tempFocusIndex == thresholdLeftIndex && mMaxMainPageCount >= 2) {
                        mCurrentMainPageCount--;
                        addMainLayoutView(mCurrentMainPageCount, curFocusIndex);
                    }

                    if (tempFocusIndex != -1) {
                        changeSubItemFoucusStatus(curSubFocusIndex, false);
                        changeMainItemFoucusStatus(tempFocusIndex, false);
                        changeMainItemFoucusStatus(curFocusIndex, true);
                        addSubLayoutView(curFocusIndex);
                        curSubFocusIndex = 0;
                        changeSubItemFoucusStatus(curSubFocusIndex, true);
                    }
                }

                if (curSubFocusIndex >= M && curSubFocusIndex < P) {
                    int tempSubFocusIndex = curSubFocusIndex;
                    int nextSubFocusIndex = curSubFocusIndex - 1;

                    if (nextSubFocusIndex >= M && nextSubFocusIndex < P) {

                        if (0 == tempSubFocusIndex % 10 || 5 == tempSubFocusIndex % 10) {
                            if (mMaxSubPageCount >= 2 && mCurrentSubPageCount >= 2) {
                                // 子菜单切换到上一页
                                changeSubItemFoucusStatus(tempSubFocusIndex, false);
                                mCurrentSubPageCount--;
                                nextSubPageView(mCurrentSubPageCount);

                            } else if (curSubFocusIndex >= 0 && curSubFocusIndex < subIconList.size()) {
                                /// 主菜单切换上一个,向左翻页
                                int tempFocusIndex = curFocusIndex;
                                curFocusIndex--;
                                mCurrentSubPageCount = 1;

                                final int tempCurrentMainPageCount = mCurrentMainPageCount;
                                int thresholdLeftIndex = (mCurrentMainPageCount - 1) * 7;
                                if (tempFocusIndex == thresholdLeftIndex && mMaxMainPageCount >= 2) {
                                    mCurrentMainPageCount--;
                                    addMainLayoutView(mCurrentMainPageCount, curFocusIndex);
                                }
                                changeSubItemFoucusStatus(curSubFocusIndex, false);
                                changeMainItemFoucusStatus(tempFocusIndex, false);
                                changeMainItemFoucusStatus(curFocusIndex, true);
                                addSubLayoutView(curFocusIndex);
                                curSubFocusIndex = 0;
                                changeSubItemFoucusStatus(curSubFocusIndex, true);
                            }
                        } else {
                            changeSubItemFoucusStatus(tempSubFocusIndex, false);
                            changeSubItemFoucusStatus(--curSubFocusIndex, true);

                        }

                    } else if (nextSubFocusIndex == -1) {
                        // 主菜单切换上一页,向左翻页
                        int tempFocusIndex = curFocusIndex;
                        curFocusIndex--;

                        if (curFocusIndex < 0) {
                            curFocusIndex = 0;
                            return false;
                        }
                        mCurrentSubPageCount = 1;
                        final int tempCurrentMainPageCount = mCurrentMainPageCount;
                        int thresholdLeftIndex = (mCurrentMainPageCount - 1) * 7;
                        if (tempFocusIndex == thresholdLeftIndex && mMaxMainPageCount >= 2) {
                            mCurrentMainPageCount--;
                            addMainLayoutView(mCurrentMainPageCount, curFocusIndex);
                        }

                        if (tempFocusIndex != -1) {
                            changeSubItemFoucusStatus(curSubFocusIndex, false);
                            changeMainItemFoucusStatus(tempFocusIndex, false);
                            changeMainItemFoucusStatus(curFocusIndex, true);
                            addSubLayoutView(curFocusIndex);
                            curSubFocusIndex = 0;
                            changeSubItemFoucusStatus(curSubFocusIndex, true);
                        }
                    } else if (4 == nextSubFocusIndex % 10 || 9 == nextSubFocusIndex % 10) {
                        // 子菜单切换到上一页
                        changeSubItemFoucusStatus(tempSubFocusIndex, false);
                        mCurrentSubPageCount--;
                        nextSubPageView(mCurrentSubPageCount);
                    }
                    Log.i(TAG, "FOCUS_CONTROL:" + focus_control + " tempSubFocusIndex:" + tempSubFocusIndex
                            + " curSubFocusIndex:" + curSubFocusIndex);
                } else {

                }
            }
        }

        return true;
    }

    /**
     * 根据当前mCurrentMainPageCount，curFocusIndex，动态添加主菜单view
     *
     * @param mCurrentMainPageCount
     * @param curFocusIndex
     */
    private void addMainLayoutView(int mCurrentMainPageCount, int curFocusIndex) {
        if (mCurrentMainPageCount == -1 || curFocusIndex == -1 || mainIconList.size() == 0) {
            return;
        }
        mainIconLayout.removeAllViews();
        final int N = (mCurrentMainPageCount - 1) * 7;
        final int P = Math.min(N + 7, mainIconList.size());// 寻找最小值或最大值
        for (int i = N; i < P; i++) {
            MainIcon mainIcon = mainIconList.get(i);
            mainIcon.mainicon_text = new Button(context);
            mainIcon.mainicon_text.getBackground().setAlpha(0);
            mainIcon.mainicon_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, ClearConfig.getScreenHeight() / 30);// 30
            mainIcon.mainicon_text.setTextColor(Color.argb(0xff, 0xff, 0xff, 0xff));
            mainIcon.mainicon_text.setGravity(Gravity.CENTER);
            mainIcon.mainicon_text.setSingleLine(true);
            mainIcon.mainicon_text.setText(mainIcon.name);
            mainIcon.mainicon_text.setPadding(0, 0, 0, 20);
            LayoutParams lp = new LayoutParams(ClearConfig.getScreenWidth() / 8,
                    ClearConfig.getScreenHeight() / 12);
//			LinearLayout.LayoutParams lp = new LayoutParams(1920 / 8,
//					1080 / 12);
            mainIcon.mainicon_text.setLayoutParams(lp);
            mainIconLayout.addView(mainIcon.mainicon_text);
        }

        // 当焦点在主菜单时
        if (focus_control == 1) {
            mainIconList.get(curFocusIndex).mainicon_text
                    .setBackground(context.getResources().getDrawable(R.drawable.main_page_top_box));
        }
        // 当焦点在子菜单时
        if (focus_control == 2) {
            onChageMainItemFoucusStatus(curFocusIndex);
        }

        showMainItemArrow(mCurrentMainPageCount);

    }

    /**
     * 根据当前curFocusIndex，动态添加子菜单view
     *
     * @param curFocusIndex
     */
    private void addSubLayoutView(int curFocusIndex) {
        if (mainIconList.size() == 0) {
            return;
        }
        clearSubLayoutView();
        subIconList = mainIconList.get(curFocusIndex).subIconList;
        mMaxSubPageCount = subIconList.size() % 10 == 0 ? subIconList.size() / 10 : subIconList.size() / 10 + 1;
        mCurrentSubPageCount = 1;

        final int N = Math.min(10, subIconList.size());
        for (int i = 0; i < N; i++) {
            SubIcon subIcon = subIconList.get(i);
            changeSubItemBg(subIcon);
            ImageView ivSubIcon = (ImageView) subIcon.subiconview.findViewById(R.id.sub_pic);
            //new MaterialRequest(context, ivSubIcon, ClearConfig.TYPE_IMAGE_SUB_ICON).execute(subIcon.iconBgURL);
            ImageLoader.getInstance().displayImage(subIcon.iconBgURL, ivSubIcon);

            TextView subIconText = (TextView) subIcon.subiconview.findViewById(R.id.sub_name);
            subIconText.setTextSize(TypedValue.COMPLEX_UNIT_SP, ClearConfig.getScreenWidth() / 90);
//			subIconText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 1920 / 70);
            subIconText.setTextColor(Color.rgb(255, 255, 255));
            subIconText.setText(subIcon.name);
            Log.e("zxb", "subiconName" + subIcon.name);
            subIcon.subiconview.setPadding(1, 1, 1, 1);
            // LinearLayout.LayoutParams lp = new
            // LayoutParams(LayoutParams.WRAP_CONTENT,
            // LayoutParams.WRAP_CONTENT);
            // lp.setMargins(50, 50, 50, 50);
            // subIcon.subiconview.setLayoutParams(lp);
            subIconLayout.addView(subIcon.subiconview);

        }
        showSubItemArrow(mCurrentSubPageCount);
        addPageIndicator();

        // ((VoDActivity)context).mMyProgressBarView.setVisibility(view.GONE);
    }

    /**
     * 添加页面页数指示值
     */
    private void addPageIndicator() {

        pageIconLayout.removeAllViews();
        pageIconList.clear();

        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        lp.setMargins(10, 0, 0, 0);
        lp.gravity = Gravity.CENTER;
        for (int i = 1; i <= mMaxSubPageCount; i++) {
            TextView btnPageIndicator = new TextView(context);
            btnPageIndicator.setWidth(ClearConfig.getScreenWidth() / 40);
            btnPageIndicator.setHeight(ClearConfig.getScreenHeight() / 20);
//			btnPageIndicator.setWidth(1920 / 40);
//			btnPageIndicator.setHeight(1080 / 20);
            if (i == mCurrentSubPageCount) {
                btnPageIndicator.setBackgroundResource(R.drawable.main_page_bottom_box);
                btnPageIndicator.setTextColor(Color.argb(255, 255, 255, 255));
            } else {
                btnPageIndicator.setBackgroundResource(Color.TRANSPARENT);
                btnPageIndicator.setTextColor(Color.argb(255, 255, 255, 255));
            }
            btnPageIndicator.setTextSize(TypedValue.COMPLEX_UNIT_SP, ClearConfig.getScreenWidth() / 70);
            btnPageIndicator.setText("" + i);
            btnPageIndicator.setGravity(Gravity.CENTER);
            pageIconLayout.addView(btnPageIndicator, lp);
            pageIconList.add(btnPageIndicator);
        }
    }

    /**
     * 控制子菜单条目两边的方向键的显示
     *
     * @param currentSubPageCount
     */
    private void showSubItemArrow(int currentSubPageCount) {
        AlphaAnimation appearAnim = new AlphaAnimation(0, 1);
        appearAnim.setDuration(1000);
        appearAnim.setFillAfter(true);

        AlphaAnimation disappearAnim = new AlphaAnimation(1, 0);
        disappearAnim.setDuration(1000);
        disappearAnim.setFillAfter(true);

        if (currentSubPageCount <= 0) {
            return;
        }

        if (1 == currentSubPageCount) {
            subprevButton.clearAnimation();
            subprevButton.setVisibility(4);
            if (mMaxSubPageCount > 1) {
                subnextButton.startAnimation(appearAnim);
                subnextButton.setVisibility(0);
            } else {
                subnextButton.clearAnimation();
                subnextButton.setVisibility(4);
            }
        } else if (currentSubPageCount == mMaxSubPageCount) {
            subnextButton.clearAnimation();
            subnextButton.setVisibility(4);
            subprevButton.startAnimation(appearAnim);
            subprevButton.setVisibility(0);
        } else {
            subprevButton.startAnimation(appearAnim);
            subprevButton.setVisibility(0);
            subnextButton.startAnimation(appearAnim);
            subnextButton.setVisibility(0);
        }

    }

    /**
     * 改变主菜单条目的获取焦点时的背景显示
     *
     * @param curFocusIndex
     * @param isShow
     */
    private void changeMainItemFoucusBg(int curFocusIndex, boolean isShow) {

        if (curFocusIndex <= -1 || curFocusIndex >= mainIconList.size()) {
            curFocusIndex = 0;
            return;
        }

        if (curFocusIndex >= mainIconList.size()) {
            curFocusIndex = mainIconList.size() - 1;
            return;
        }

        if (isShow) {

            mainIconList.get(curFocusIndex).mainicon_text
                    .setBackground(context.getResources().getDrawable(R.drawable.main_page_top_box));
        } else {
            if (direction_control == 0 && curFocusIndex == 1) {
                return;
            }
            if (direction_control == 2 && curFocusIndex == (mainIconList.size() - 1)) {
                return;
            }
            mainIconList.get(curFocusIndex).mainicon_text
                    .setBackground(context.getResources().getDrawable(R.drawable.remove_focus_border));
        }
    }

    /*
     * 改子主菜单条目的获取焦点时的背景显示
     *
     * @param curSubFocusIndex
     *
     * @param isShow
     */
    private void changeSubItemFoucusStatus(int curSubFocusIndex, boolean isShow) {

        if (curSubFocusIndex <= -1 || curSubFocusIndex >= subIconList.size()) {
            curSubFocusIndex = 0;
            return;
        }

        if (curSubFocusIndex >= subIconList.size()) {
            curSubFocusIndex = subIconList.size() - 1;
            return;
        }

        SubIcon subIcon = subIconList.get(curSubFocusIndex);
        if (isShow) {
            ImageView focus_iv_border = (ImageView) subIcon.subiconview.findViewById(R.id.sub_pic_border);
            focus_iv_border.setVisibility(View.VISIBLE);
        } else {
            ImageView focus_iv_border = (ImageView) subIcon.subiconview.findViewById(R.id.sub_pic_border);
            focus_iv_border.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onKeyDpadRight() {

        if (isLaunchNewApp) {
            return true;
        }

        setTitleFocused();
        Gesture = true;
        customLayoutTransition(Gesture);
        long last = System.currentTimeMillis();
        if (last - lastTime <= 300) {
            return true;
        }
        lastTime = last;

        if (focus_control == 1) {
            // 右边界 用于判断超过主菜单个数后返回到第一个。
            if (curFocusIndex >= (mainIconList.size() - 1)) {
                curFocusIndex = mainIconList.size() - 1;
                return false;
            } else {
                // if (mainIconList.get(curFocusIndex +
                // 1).mainicon_text.getVisibility() == View.VISIBLE) {
                int tempFocusIndex = curFocusIndex;
                curFocusIndex++;
                // 判断下一个菜单下是否有模块，如果没有则不需要切换
                Log.i(TAG, "right: " + " tempFocusIndex:" + tempFocusIndex + " curFocusIndex:" + curFocusIndex);
                // 向右翻页
                curSubFocusIndex = 0;
                final int tempCurrentMainPageCount = mCurrentMainPageCount;
                int thresholdRightIndex = Math.min(((mCurrentMainPageCount - 1) * 7 + 6), (mainIconList.size() - 1));
                if (tempFocusIndex == thresholdRightIndex && mCurrentMainPageCount <= (mMaxMainPageCount - 1)) {
                    mCurrentMainPageCount++;
                    addMainLayoutView(mCurrentMainPageCount, curFocusIndex);
                }
                changeMainItemFoucusBg(tempFocusIndex, false);
                changeMainItemFoucusBg(curFocusIndex, true);
                addSubLayoutView(curFocusIndex);
            }
            // }
        }

        if (focus_control == 2) {

            Log.i(TAG, "FOCUS_CONTROL:" + focus_control);

            if (curFocusIndex > (mainIconList.size() - 1)) {
                curFocusIndex = mainIconList.size() - 1;
                return false;
            }

            if (curSubFocusIndex >= subIconList.size()) {

                if (subIconList.size() != 0) {
                    return false;
                }
            }

            if (curFocusIndex == mainIconList.size() - 1 && subIconList.size() - 1 == curSubFocusIndex) {
                return false;
            }

            final int M = (mCurrentSubPageCount - 1) * 10;
            final int P = Math.min(M + 10, subIconList.size());

            if (M == P && P == 0) {
                /// 主菜单切换下一个,向右翻页
                int tempFocusIndex = curFocusIndex;
                curFocusIndex++;
                mCurrentSubPageCount = 1;

                int thresholdRightIndex = Math.min(((mCurrentMainPageCount - 1) * 7 + 6), (mainIconList.size() - 1));
                if (tempFocusIndex == thresholdRightIndex && mCurrentMainPageCount <= (mMaxMainPageCount - 1)) {
                    mCurrentMainPageCount++;
                    addMainLayoutView(mCurrentMainPageCount, curFocusIndex);
                }

                changeMainItemFoucusStatus(tempFocusIndex, false);
                changeMainItemFoucusStatus(curFocusIndex, true);
                addSubLayoutView(curFocusIndex);
                curSubFocusIndex = 0;
                changeSubItemFoucusStatus(curSubFocusIndex, true);

            }

            if (curSubFocusIndex >= M && curSubFocusIndex < P) {
                int tempSubFocusIndex = curSubFocusIndex;
                int nextSubFocusIndex = curSubFocusIndex + 1;
                if (nextSubFocusIndex >= M && nextSubFocusIndex < P) {

                    if (4 == tempSubFocusIndex % 10 || 9 == tempSubFocusIndex % 10) {
                        if (mCurrentSubPageCount <= mMaxSubPageCount - 1) {
                            // 子菜单切换到下一页
                            changeSubItemFoucusStatus(tempSubFocusIndex, false);
                            mCurrentSubPageCount++;
                            nextSubPageView(mCurrentSubPageCount);

                        } else if (curSubFocusIndex >= 0 && curSubFocusIndex < subIconList.size()) {
                            if (mainIconList.get(curFocusIndex + 1).mainicon_text.getVisibility() == View.VISIBLE) {
                                /// 主菜单切换下一个,向右翻页
                                int tempFocusIndex = curFocusIndex;
                                curFocusIndex++;
                                mCurrentSubPageCount = 1;

                                int thresholdRightIndex = Math.min(((mCurrentMainPageCount - 1) * 7 + 6),
                                        (mainIconList.size() - 1));
                                if (tempFocusIndex == thresholdRightIndex
                                        && mCurrentMainPageCount <= (mMaxMainPageCount - 1)) {
                                    mCurrentMainPageCount++;
                                    addMainLayoutView(mCurrentMainPageCount, curFocusIndex);
                                }
                                changeSubItemFoucusStatus(curSubFocusIndex, false);
                                changeMainItemFoucusStatus(tempFocusIndex, false);
                                changeMainItemFoucusStatus(curFocusIndex, true);
                                addSubLayoutView(curFocusIndex);
                                curSubFocusIndex = 0;
                                changeSubItemFoucusStatus(curSubFocusIndex, true);
                            }
                        }
                    } else {
                        changeSubItemFoucusStatus(tempSubFocusIndex, false);
                        changeSubItemFoucusStatus(++curSubFocusIndex, true);

                    }

                } else if (nextSubFocusIndex == P && mCurrentSubPageCount <= mMaxSubPageCount - 1) {
                    // 子菜单切换到下一页
                    changeSubItemFoucusStatus(tempSubFocusIndex, false);
                    mCurrentSubPageCount++;
                    nextSubPageView(mCurrentSubPageCount);

                } else if (nextSubFocusIndex >= M && nextSubFocusIndex == P) {
                    /// 主菜单切换下一个,向右翻页
                    if (mainIconList.get(curFocusIndex + 1).mainicon_text.getVisibility() == View.VISIBLE) {
                        int tempFocusIndex = curFocusIndex;
                        curFocusIndex++;
                        mCurrentSubPageCount = 1;
                        int thresholdRightIndex = Math.min(((mCurrentMainPageCount - 1) * 7 + 6),
                                (mainIconList.size() - 1));
                        if (tempFocusIndex == thresholdRightIndex && mCurrentMainPageCount <= (mMaxMainPageCount - 1)) {
                            mCurrentMainPageCount++;
                            addMainLayoutView(mCurrentMainPageCount, curFocusIndex);
                        }

                        if (tempFocusIndex != mainIconList.size() - 1) {
                            if (tempFocusIndex < mainIconList.size() - 1) {
                                changeSubItemFoucusStatus(curSubFocusIndex, false);
                            }
                            changeMainItemFoucusStatus(tempFocusIndex, false);
                            changeMainItemFoucusStatus(curFocusIndex, true);
                            addSubLayoutView(curFocusIndex);
                            curSubFocusIndex = 0;
                            changeSubItemFoucusStatus(curSubFocusIndex, true);
                        }
                    }
                }
                Log.i(TAG, "FOCUS_CONTROL:" + focus_control + " tempSubFocusIndex:" + tempSubFocusIndex
                        + " curSubFocusIndex:" + curSubFocusIndex);
            } else {

            }
        }

        return true;
    }

    /**
     * 改变主菜单颜色状态
     *
     * @param currentFocusIndex
     * @param isShow
     */
    private void changeMainItemFoucusStatus(int currentFocusIndex, boolean isShow) {

        if (currentFocusIndex <= -1 || currentFocusIndex >= mainIconList.size()) {
            return;
        }

        MainIcon mainIcon = mainIconList.get(currentFocusIndex);

        if (isShow) {
            mainIcon.mainicon_text.setTextColor(Color.argb(0xff, 0xff, 0xff, 0xff));
        } else {
            mainIcon.mainicon_text.setTextColor(Color.argb(0x33, 0xff, 0xff, 0xff));
        }
    }

    /**
     * 切换子菜单内容页
     *
     * @param currentSubPageCount
     */
    private void nextSubPageView(int currentSubPageCount) {
        clearSubLayoutView();
        if (subIconList.size() > 0) {
            final int M = (currentSubPageCount - 1) * 10;
            final int N = Math.min(M + 10, subIconList.size());

            for (int i = M; i < N; i++) {
                SubIcon subIcon = subIconList.get(i);
                changeSubItemBg(subIcon);
                ImageView ivSubIcon = (ImageView) subIcon.subiconview.findViewById(R.id.sub_pic);
                ImageLoader.getInstance().displayImage(subIcon.iconBgURL, ivSubIcon);
                //new MaterialRequest(context, ivSubIcon, ClearConfig.TYPE_IMAGE_SUB_ICON).execute(subIcon.iconBgURL);

                TextView subIconText = (TextView) subIcon.subiconview.findViewById(R.id.sub_name);
                subIconText.setTextSize(TypedValue.COMPLEX_UNIT_SP, ClearConfig.getScreenWidth() / 58);
                subIconText.setTextColor(Color.rgb(255, 255, 255));
                subIconText.setText(subIcon.name);
                subIcon.subiconview.setPadding(1, 1, 1, 1);
                subIconLayout.addView(subIcon.subiconview);
            }
            curSubFocusIndex = M;
            changeSubItemFoucusStatus(curSubFocusIndex, true);
            showSubItemArrow(mCurrentSubPageCount);
            showSubPageIndicator(mCurrentSubPageCount);
        }

    }

    /**
     * 清除子菜单的view和方向按键的动画
     */
    private void clearSubLayoutView() {

        subIconLayout.removeAllViews();
    }

    /**
     * 显示子页面指示值
     *
     * @param currentSubPageCount
     */
    private void showSubPageIndicator(int currentSubPageCount) {
        if (pageIconList != null && pageIconList.size() > 0) {
            for (int i = 0; i < pageIconList.size(); i++) {

                TextView btnPageIndicator = pageIconList.get(i);
                if (i + 1 == currentSubPageCount) {
                    btnPageIndicator.setBackgroundResource(R.drawable.main_page_bottom_box);
                    btnPageIndicator.setTextColor(Color.argb(255, 255, 255, 255));
                } else {
                    btnPageIndicator.setBackgroundResource(Color.TRANSPARENT);
                    btnPageIndicator.setTextColor(Color.argb(255, 255, 255, 255));
                }
            }
        }
    }

    @Override
    public boolean onKeyEnter() {
        Log.i("Enter", "curSubFocusIndex：" + curSubFocusIndex);
        long last = System.currentTimeMillis();
        if (last - lastTime < 300) {
            return true;
        }
        lastTime = last;

        if (isLaunchNewApp) {
            return true;
        }

        if (curSubFocusIndex != -1 && mainIconList.get(curFocusIndex).subIconList.size() != 0) {
            Log.e(TAG, "onKeyEnter curFocusIndex " + curFocusIndex + " curSubFocusIndex " + curSubFocusIndex);
            final SubIcon focusIcon = mainIconList.get(curFocusIndex).subIconList.get(curSubFocusIndex);
            final ApkInfo apkInfo = apkInfoMap.get(focusIcon.nextViewID);

            VoDBaseView newView = null;

            if (!"App".equals(focusIcon.type)) {
                newView = VoDViewManager.newViewByType(focusIcon.type);
            }

            Log.i(TAG, "focusIcon.type: " + focusIcon.type);
            String type = "";
            switch (focusIcon.type) {
                case "Picture_list":
                    type = "图文";
                    break;
                case "TV_series_list":
                    type = "视频";
                    break;
                case "Live":
                    type = "直播";
                    break;
                case "App":
                    type = "应用";
                    break;
                case "Audio":
                    type = "音乐";
                    // type = null;
                    break;
                case "Video":
                    type = "电影";
                    break;
                default:
                    type = "其他";
                    break;
            }
            isLoaded = true;
            Log.i("newview", "" + focusIcon.jsonUrl);
            // 写死focusicon的jsonurl，调试页面跳转

            // if (newView instanceof PicTextView) {
            // newView = null;
            // }

            if (newView != null) {
                newView.init(context, focusIcon.jsonUrl);
                newView.setName(focusIcon.name);
                VoDViewManager.getInstance().pushForegroundView(newView);

                mApp.content = mainIconList.get(curFocusIndex).name + "-" + focusIcon.name;
                mApp.viewType = type;
                Log.i(TAG, "content: " + mApp.content);
                if (type.equalsIgnoreCase("直播")) {
                    mApp.resourceName = focusIcon.name;
                    // mApp.timeInS =
                    // DateUtil.getTimeMillSecondFromDateStr(mApp.curServTime) /
                    // 1000;
                    mApp.timeInS = com.clearcrane.schedule.DateUtil.getCurrentTimeSecond();
                    mApp.contentleft = "";
                    String logInsert = mApp.combinatePostParasString("start", "0", "点播", mApp.viewType, focusIcon.name,
                            mApp.content);
                    Log.i(TAG, " live START: " + logInsert);
                    ClearLog.logInsert(logInsert);
                    mApp.SendLogMode = 1;
                }
            } else {

                if ("应用".equals(type)) {

                    // String pkgName =
                    // db.queryByNextViewId(focusIcon.nextViewID);
//					boolean temp = false;

                    Log.i(TAG, "code row: 1162 apkInfo.packageName:" + apkInfo.packageName);

                    // 判断在系统中已经安装了该apk
                    if (appList != null && appList.contains(apkInfo.packageName)) {
                        // spInstall.edit().putInt("status", 0).commit();
                        isExist = true;
                    }


                    if (isExist) {// 在应用存在的情况下

                        packageName = apkInfo.packageName;
                        // boolean hasExist =
                        // db.queryByParams(apkInfo.packageName, curFocusIndex,
                        // curSubFocusIndex,
                        // focusIcon.nextViewID);
                        // if (!hasExist) {
                        // db.insert(apkInfo.packageName, curFocusIndex,
                        // curSubFocusIndex, focusIcon.nextViewID, 1);
                        // }

                        // mHandler.obtainMessage(DISAPPER_UNINSTALL_PIC).sendToTarget();

                        if (packageName != null) {
                            if (!isInstalling || !isLoading) {
                                mHandler.obtainMessage(NEW_APP_LAUNCH).sendToTarget();
                                InstallApkUtils.doStartApplicationWithPackageName(packageName, context);
                                mHandler.obtainMessage(NEW_APP_QUIT).sendToTarget();
                            }
                        } else {
                            packageName = apkInfo.packageName;
                            // mHandler.obtainMessage(DISAPPER_UNINSTALL_PIC).sendToTarget();
                            if (!isInstalling || !isLoading) {
                                mHandler.obtainMessage(NEW_APP_LAUNCH).sendToTarget();
                                InstallApkUtils.doStartApplicationWithPackageName(apkInfo.packageName, context);
                                mHandler.obtainMessage(NEW_APP_QUIT).sendToTarget();
                            }
                        }

                    } else {// 如果应用不存在

                        Log.i("isInstalled:", "false");
                        if (isInstalling) {
                            return true;
                        }

                        // boolean flag = db.insert(apkInfo.packageName,
                        // curFocusIndex, curSubFocusIndex,
                        // focusIcon.nextViewID, 0);
                        // Log.i(TAG,
                        // "flag:" + flag + " packageName:" + packageName + "
                        // curFocusIndex:" + curFocusIndex
                        // + " curSubFocusIndex:" + curSubFocusIndex + "
                        // focusIcon.nextViewID:"
                        // + focusIcon.nextViewID);

                        Runnable downloadApkTask = new Runnable() {

                            @Override
                            public void run() {
                                // 1.下载apk到sdcard
                                mHandler.obtainMessage(BEGIN_LOADING).sendToTarget();
                                if (apkInfo.apkUrl != null) {
                                    Log.e("zxb", "apkUrl:" + apkInfo.apkUrl);
                                    downloadApkFile(apkInfo.apkUrl);
                                } else {
                                    Toast.makeText(context, "apkUrl为空！", Toast.LENGTH_SHORT).show();
                                    mHandler.obtainMessage(END_LOADING).sendToTarget();
                                    return;
                                }
                                // mHandler.obtainMessage(END_LOADING).sendToTarget();
                                // 2.apk文件获取包名
                                String filePath = apkLocation + apkInfo.packageName + ".apk";
                                apkInfo(filePath, context);
                                Log.i(TAG, "filePath:" + filePath.toString());

                                if (isExist && !isLoading) {

                                    mHandler.obtainMessage(BEGIN_INSTALLED).sendToTarget();
                                    int result = InstallApkUtils.installApk(filePath);

                                    // installFromClearInstall("com.clearcrane.clearapp",
                                    // filePath,
                                    // "package:" + apkInfo.packageName);
                                    Log.i(TAG, "result:" + result);
                                    if (9 == result || -1 == result) {

                                        Log.e(TAG, "root权限获取失败，将进行普通安装");
                                        Intent intent = new Intent();
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.setAction(Intent.ACTION_VIEW);
                                        intent.setDataAndType(Uri.fromFile(mTempFile),
                                                "application/vnd.android.package-archive");
                                        context.startActivity(intent);
                                    }
                                    mHandler.obtainMessage(END_INSTALLED).sendToTarget();
                                } else {

                                }
                            }

                        };
                        // THREAD_POOL_EXECUTOR.execute(downloadApkTask);
                        new Thread(downloadApkTask).start();

                    }
                }
            }
        }

        return true;
    }

    private OnCompleteListener MainPageJsonListen = new OnCompleteListener() {

        @Override
        public void onComplete(boolean result) {

        }

        @Override
        public void onDownloaded(Object result) {
            isRequestTimeOut = false;
            MainPageJson = (String) result;
            //本地数据更新
            if (sharedPreferences == null) {
                sharedPreferences = context.getSharedPreferences("launcher", Context.MODE_PRIVATE);
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("mainMenu", MainPageJson);
            editor.commit();
            Log.d(TAG, "数据更新成功;");
            if (isUpdateUI) {
                Log.d(TAG, "UI更新成功;");
                setResult();
                return;
            }
        }

    };


    private void setResult() {
        Log.e("Json", MainPageJson);
        if (MainPageJson == null) {
//				TipDialog.Builder builder = new TipDialog.Builder(context);
//				builder.setMessage("当前网络不可用，请检查网络");
//				builder.setTitle("提示");
//				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//						// 设置你的操作事项
//					}
//				});
//
//				builder.create().show();
            Log.e("Json", "mainpagejson is null check internet ");
            return;
        }
        try {

            JSONTokener jsonParser = new JSONTokener(MainPageJson);
            JSONObject objectjson = (JSONObject) jsonParser.nextValue();

            /** 设置mainlogo和background **/
            String bg_url = ClearConfig.getJsonUrl(context, objectjson.getString("background_pic_url"));
            String prison_title = objectjson.getString("prison_name");
            String logo_url = ClearConfig.getJsonUrl(context, objectjson.getString("logo"));
            Log.i("JSON", "BG_URL:" + bg_url + " LOGO_URL:" + logo_url);
            // 设置系统名称
//				SpannableStringBuilder spsb = new SpannableStringBuilder(prison_title);
//				spsb.setSpan(new ForegroundColorSpan(Color.parseColor("#f0f031")), 0, spsb.length() - 6,
//						Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
//				// spsb.append("电视教育系统");//追加后面文案
//				spsb.setSpan(new ForegroundColorSpan(Color.parseColor("#ffffff")), spsb.length() - 6, spsb.length(),
//						Spannable.SPAN_EXCLUSIVE_INCLUSIVE);// 设置后面的字体颜色
//				maintitle.setTextColor(Color.parseColor("#f0f031"));
            maintitle.setTextColor(Color.parseColor("#ffffff"));
            maintitle.setText(prison_title);
            maintitle.requestFocus();
            ImageLoader.getInstance().displayImage(bg_url, mainbackground);
            // 获取背景图片
            //new MaterialRequest(context, mainbackground, ClearConfig.TYPE_IMAGE_BG).execute(bg_url);
            // 获取logo
            ImageLoader.getInstance().displayImage(logo_url, mainlogo);
            //new MaterialRequest(context, mainlogo, ClearConfig.TYPE_IMAGE_BG).execute(logo_url);

            // 获取到的菜单内容
            JSONArray contentArray = objectjson.getJSONArray("Content");
            JSONObject joApp = (JSONObject) contentArray.opt(contentArray.length() - 1);

            for (int i = 0; i < contentArray.length(); i++) {

                JSONObject objecttmp = (JSONObject) contentArray.opt(i);
                MainIcon mainicon = new MainIcon();
                mainicon.name = objecttmp.getString("Name");
                mainicon.subCount = objecttmp.getInt("Count");
                JSONArray secondArray = objecttmp.getJSONArray("Content");
                for (int j = 0; j < secondArray.length(); j++) {
                    JSONObject secondTmp = (JSONObject) secondArray.opt(j);
                    SubIcon subicon = new SubIcon();
                    subicon.type = secondTmp.getString("Type");
                    subicon.jsonUrl = ClearConfig.getJsonUrl(context, secondTmp.getString("Json_URL"));
                    subicon.name = secondTmp.getString("Name");
                    subicon.iconURL = ClearConfig.getJsonUrl(context, secondTmp.getString("Icon_URL"));
                    subicon.iconBgURL = ClearConfig.getJsonUrl(context, secondTmp.getString("Icon_background_URL"));
                    subicon.subiconview = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.sub_page,
                            null);
                    // 对需要的app进行特殊处理
                    if ("App".equals(subicon.type)) {
                        //
                        subicon.nextViewID = secondTmp.getString("NextViewID");
                        MaterialRequest materialRequest = new MaterialRequest(context, ClearConfig.TYPE_JSON);

                        materialRequest.setOnDownloadCompleteListener(new OnDownLoadCompleListener() {

                            @Override
                            public void onDownloadedComplete(Object result, String nextViewID) {

                                Log.i("json", nextViewID + "");
                                String apkInfos = (String) result;
                                JSONTokener jtApkInfo = new JSONTokener(apkInfos);
                                try {
                                    JSONObject joApkInfo = (JSONObject) jtApkInfo.nextValue();
                                    JSONArray jaContent = joApkInfo.getJSONArray("Content");
                                    if (jaContent.length() > 0) {
                                        ApkInfo apkInfo = new ApkInfo();
                                        JSONObject joContent = (JSONObject) jaContent.get(0);
                                        apkInfo.packageName = joContent.getString("AppPkg");
                                        apkInfo.apkUrl = joContent.getString("AppPath");
                                        apkInfoMap.put(nextViewID, apkInfo);
                                        Log.i("json", "packageName:" + apkInfo.packageName);
                                        Log.i("json", "apkUrl:" + apkInfo.apkUrl);
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                        materialRequest.execute(subicon.jsonUrl, subicon.nextViewID);
                        //
                    }

                    // 暂时在初始化时写死盒子从后台获得的GroupId

                    GroupId = moduleGroupSharePre.getInt(ClearConstant.STR_MODULE_GROUP_ID, -1);

                    Log.e("xb", "GroupId:" + GroupId);

                    String start = accessTimeSharePre.getString(ClearConstant.END_TIME, "23:59");

                    Log.i("MainPage", "GroupId:" + GroupId + " Start:" + start);

                    // group数组
                    if (secondTmp.has("Group")) {
                        JSONArray thirdArray = secondTmp.getJSONArray("Group");
                        // 在某些特殊情况下会出现group数组为空的情况，这种情况默认为显示所有板块
                        // group数组中有群组,没有数组默认加载所有板块
                        if (GroupId != -1) {
                            for (int k = 0; k < thirdArray.length(); k++) {
                                subicon.groups[k] = thirdArray.getInt(k);
                                // 根据盒子制定的group分组，选择加载显示包含该group的子模块,加入要显示的subiconlist
                                if (subicon.groups[k] == GroupId) {
                                    Log.e("xb", "group匹配 添加子菜单项");
                                    mainicon.subIconList.add(subicon);
                                    break;
                                }
                            }
                        } else {
                            mainicon.subIconList.add(subicon);
                        }
                    }
                }
                // 如果子菜单为空就不显示该主菜单
                if (mainicon.subIconList.size() > 0) {
                    mainIconList.add(mainicon);
                }
                // 为了防止首页无内容导致无法进入崩溃的问题，需要进行判断，如果都不存在就默认显示最后一个
                // if (mainIconList.size() <= 0) {
                // Log.e(TAG, "分组出现错误，只显示最后一个");
                // mainIconList.add(mainicon);
                // }
            }

            int mainIconListSize = mainIconList.size();
            if (mainIconListSize > 0) {
                mMaxMainPageCount = mainIconListSize % 7 == 0 ? mainIconListSize / 7 : mainIconListSize / 7 + 1;
            }

            // 默认当前焦点为第一个主菜单
            if (curFocusIndex < 0) {
                // first focus
                mCurrentMainPageCount = 1; // 菜单第一页
                curFocusIndex = 0; // 主菜单当前页的指针
                focus_control = 1;
                addMainLayoutView(mCurrentMainPageCount, curFocusIndex);
                addSubLayoutView(0);
            } else {
                Log.i("FirstFocus", "curFocusIndex:" + curFocusIndex);
            }

            // 每隔1000ms更新一次时间
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateTimeZone();
                    Log.i("xyltime", "" + timetext);
                    Message msg = mHandler.obtainMessage();
                    msg.sendToTarget();
                }
            }, 0, ClearConfig.UPDATE_TIME);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 根据newMpage，控制主菜单条目两边的方向键
    private void showMainItemArrow(int newMpage) {
        AlphaAnimation animBegin = new AlphaAnimation(0, 1);
        animBegin.setDuration(500);
        animBegin.setFillAfter(true);

        AlphaAnimation animEnd = new AlphaAnimation(1, 0);
        animEnd.setDuration(500);
        animEnd.setFillAfter(true);

        if (newMpage == 1) {
            mainprevButton.setAnimation(animEnd);
            mainprevButton.setVisibility(4);
            if (mMaxMainPageCount > 1) {
                mainnextButton.setAnimation(animBegin);
                mainnextButton.setVisibility(0);
            } else {
                mainnextButton.setAnimation(animEnd);
                mainnextButton.setVisibility(4);
            }
        } else if (newMpage == mMaxMainPageCount) {
            mainnextButton.setAnimation(animEnd);
            mainnextButton.setVisibility(4);
            if (mMaxMainPageCount > 2) {
                mainprevButton.setAnimation(animBegin);
                mainprevButton.setVisibility(0);
            } else {
                mainprevButton.setAnimation(animEnd);
                mainprevButton.setVisibility(4);
            }
        } else if (newMpage < 1) {
            mainprevButton.setAnimation(animEnd);
            mainprevButton.setVisibility(4);
            mainnextButton.setAnimation(animEnd);
            mainnextButton.setVisibility(4);
        } else {
            mainprevButton.setAnimation(animBegin);
            mainprevButton.setVisibility(0);
            mainnextButton.setAnimation(animBegin);
            mainnextButton.setVisibility(0);
        }
    }

    @Override
    public void back() {

        if (maintitle != null) {
            maintitle.post(new Runnable() {
                @Override
                public void run() {
                    maintitle.requestFocus();
                    Log.i("back1", "focus" + maintitle.requestFocus());
                }
            });
        }

    }

    // 下载apk
    protected File downloadApkFile(String httpurl) {

        SubIcon subIcon = subIconList.get(curSubFocusIndex);
        ApkInfo apkInfo = apkInfoMap.get(subIcon.nextViewID);

        progressBar = (RoundProgressBar) subIcon.subiconview.findViewById(R.id.roundProgressBar);
        progressBar.setMax(100);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            InputStream is = null;

            String sdCardRootDir = Environment.getExternalStorageDirectory().getPath();
            mTempFile = new File(sdCardRootDir, apkInfo.packageName + ".apk");
//			Log.e(TAG, apkInfo.packageName + "_" + "application.apk");
            try {
                if (!mTempFile.exists()) {
                    mTempFile.createNewFile();
                }
                Log.e(TAG, "mTempFile:" + mTempFile.getAbsolutePath());
                URL url = new URL(httpurl);
                HttpURLConnection openConnection = (HttpURLConnection) url.openConnection();
                int fileLength = openConnection.getContentLength();
                is = openConnection.getInputStream();
                is = new BufferedInputStream(openConnection.getInputStream());
                FileOutputStream fos = new FileOutputStream(mTempFile);

                byte[] buffer = new byte[1024];
                openConnection.connect();
                int total = 0;

                if (openConnection.getResponseCode() >= 400) {
                    Toast.makeText(context, "连接超时！", Toast.LENGTH_SHORT).show();
                } else {

                    int numRead = -1;
                    map.put(subIcon.nextViewID, true);

                    if (is != null) {
                        while ((numRead = is.read(buffer)) != -1) {

                            total += numRead;
                            int temp = (int) (((float) total / (float) fileLength) * 100);
                            // Log.e("eee",
                            // "total:"+total+"---"+"fileLength:"+fileLength+"---"+"temp:"+temp);

                            Message msg = new Message();
                            msg.what = UPDATE_PROGRESSBAR;
                            Bundle bundle = new Bundle();
                            bundle.putInt("progress", temp);
                            msg.setData(bundle);
                            mHandler.sendMessage(msg);
                            fos.write(buffer, 0, numRead);
                            // Log.e("Slient", "fileLength:" + fileLength +
                            // "total:" +
                            // total+"time:"+System.currentTimeMillis());
                        }
                    }

                }
                Log.e("Slient", "连接断开");
                openConnection.disconnect();
                fos.close();
                is.close();

                progressBar.setVisibility(View.INVISIBLE);
                mHandler.obtainMessage(END_LOADING).sendToTarget();
            } catch (Exception e) {
                e.toString();
            }
        }

        return null;
    }

    /**
     * 获取apk包的信息：版本号，名称，图标等
     *
     * @param absPath apk包的绝对路径
     * @param context
     */
    public void apkInfo(String absPath, Context context) {

        PackageManager pm = context.getPackageManager();
        PackageInfo pkgInfo = pm.getPackageArchiveInfo(absPath, PackageManager.GET_ACTIVITIES);
        if (pkgInfo != null) {
            ApplicationInfo appInfo = pkgInfo.applicationInfo;
            /* 必须加这两句，不然下面icon获取是default icon而不是应用包的icon */
            appInfo.sourceDir = absPath;
            appInfo.publicSourceDir = absPath;
            String appName = pm.getApplicationLabel(appInfo).toString();// 得到应用名
            packageName = pkgInfo.packageName;
            String version = pkgInfo.versionName; // 得到版本信息
            /* icon1和icon2其实是一样的 */
            Drawable icon1 = pm.getApplicationIcon(appInfo);// 得到图标信息
            Drawable icon2 = appInfo.loadIcon(pm);
            String pkgInfoStr = String.format("PackageName:%s, Vesion: %s, AppName: %s", packageName, version, appName);
            Log.i(TAG, String.format("PkgInfo: %s", pkgInfoStr));
        }
    }

    /**
     * 改变子菜单是否安装背景图
     *
     * @param subIcon
     */
    private void changeSubItemBg(SubIcon subIcon) {

        RoundProgressBar roundProgressBar = (RoundProgressBar) subIcon.subiconview.findViewById(R.id.roundProgressBar);

        if ("App".equals(subIcon.type)) {

            // String result = db.queryByNextViewId(subIcon.nextViewID);
            ImageView imageView = (ImageView) subIcon.subiconview.findViewById(R.id.sub_install_status_pic);
            ImageUtil.displayImage("assets://ic_uninstall.image", imageView);

            boolean isExist = false;
            ApkInfo apkInfo = null;

            if (apkInfoMap.containsKey(subIcon.nextViewID)) {
                apkInfo = apkInfoMap.get(subIcon.nextViewID);
                isExist = checkApkExist(context, apkInfo.packageName);
            } else {
                imageView.setVisibility(View.VISIBLE);
                roundProgressBar.setVisibility(View.VISIBLE);
                return;
            }

            Log.i("jason", "isExist:" + isExist);

            if (isExist) {
                imageView.setVisibility(View.INVISIBLE);
                roundProgressBar.setVisibility(View.INVISIBLE);
            } else {
                if (apkInfoMap.isEmpty()) {
                    imageView.setVisibility(View.VISIBLE);
                    roundProgressBar.setVisibility(View.VISIBLE);
                    return;
                }
                String filePath = apkLocation + apkInfoMap.get(subIcon.nextViewID).packageName + ".apk";
                Log.e("jason", "filePath:" + filePath);
                File file = new File(filePath);
                Log.e("jason", "isfile:" + file.exists() + subIcon.name);
                if (file.exists()) {//文件已下载完成 但是未安装
                    imageView.setVisibility(View.VISIBLE);
                    roundProgressBar.setVisibility(View.INVISIBLE);
                } else {//既没下载也没安装
                    imageView.setVisibility(View.VISIBLE);
                    roundProgressBar.setVisibility(View.VISIBLE);
                    roundProgressBar.setProgress(0);
                }
            }

        } else {
            roundProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 刷新子菜单的图标
     */
    // private void refrshCurrentSubIcon(boolean isShow) {
    //
    // List<ApkInstallInfo> apkInstallInfoList =
    // db.queryByPackageName(packageName);
    //
    // if (apkInstallInfoList.size() > 0) {
    // for (ApkInstallInfo apkInstallInfo : apkInstallInfoList) {
    // MainIcon mainIcon = mainIconList.get(apkInstallInfo.mainIndex);
    // ArrayList<SubIcon> subIconList = mainIcon.subIconList;
    // if (subIconList != null && subIconList.size() > 0) {
    // SubIcon subIcon = subIconList.get(apkInstallInfo.subIndex);
    // ImageView imageView = (ImageView)
    // subIcon.subiconview.findViewById(R.id.sub_install_status_pic);
    // ImageUtil.displayImage("assets://ic_uninstall.image", imageView);
    // RoundProgressBar roundProgressBar = (RoundProgressBar)
    // subIcon.subiconview
    // .findViewById(R.id.roundProgressBar);
    //
    // if (isShow) {
    // // 显示未下载的提示图片和进度条
    // if (imageView.getVisibility() != View.VISIBLE) {
    // imageView.setVisibility(View.VISIBLE);
    // }
    // if (roundProgressBar.getVisibility() != View.VISIBLE) {
    // roundProgressBar.setVisibility(View.VISIBLE);
    // roundProgressBar.setProgress(0);
    // }
    //
    // } else {
    // // 不显示未下载的提示图片和进度条
    // if (imageView.getVisibility() != View.INVISIBLE) {
    // imageView.setVisibility(View.INVISIBLE);
    // }
    // if (roundProgressBar.getVisibility() != View.INVISIBLE) {
    //
    // roundProgressBar.setVisibility(View.INVISIBLE);
    // }
    // }
    // }
    //
    // }
    //
    // if (isShow) {
    //// db.deleteByPakageName(packageName);
    // }
    // }
    //
    // }

    /**
     * 卸载app后更新桌面图片状态
     */
    public void afterUninstll2Reresh(String pkgName) {

        packageName = pkgName;
        mHandler.sendEmptyMessage(APPER_UNINSTALL_PIC);
    }

    /**
     * 安装app后更新桌面图片状态
     */
    public void afterInstll2Reresh(String pkgName) {

        packageName = pkgName;
        // db.updateByPackageName(pkgName);
        mHandler.sendEmptyMessage(DISAPPER_UNINSTALL_PIC);
    }

    /**
     * 获取非系统应用信息列表
     */
    private ArrayList<String> getAppList() {
        ArrayList<String> appList = null;
        PackageManager pm = context.getPackageManager();
        // Return a List of all packages that are installed on the device.
        List<PackageInfo> packages = pm.getInstalledPackages(0);

        if (packages.size() > 0) {
            appList = new ArrayList<String>();
        }

        for (PackageInfo packageInfo : packages) {
            // 判断系统/非系统应用
            // if ((packageInfo.applicationInfo.flags &
            // ApplicationInfo.FLAG_SYSTEM) == 0) // 非系统应用
            // {
            appList.add(packageInfo.packageName);
            Log.e(TAG, "本机中已安装的包:" + packageInfo.packageName);

            // } else {
            // // 系统应用
            // }
        }

        return appList;

    }

    // 根据包名判断
    public boolean checkApkExist(Context context, String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        ApplicationInfo info = null;
        try {
            info = context.getPackageManager().getApplicationInfo(packageName, 0);
            return info != null;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

}
