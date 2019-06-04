package com.clearcrane.tool;

import android.R.color;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.clearcrane.tool.VerticalViewPager.OnPageChangeListener;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.PlatformSettings;
import com.clearcrane.vod.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AppManager implements OnItemClickListener, OnPageChangeListener {
    private static final String TAG = "AppManage";
    private static final String ICON = "icon";
    private static final String APPNAME = "appname";
    private static final String PACKAGENAME = "packagename";
    private static final String VERSIONCODE = "versioncode";
    private static final String VERSIONNAME = "versionname";
    private static final int MSG_SHOWAPPSDIALOG = 1;
    private static final int MSG_WEBGOBACK = 2;
    private static final int APPSNUM = 10;

    private Context mCtx = null;
    private WebView mWebView = null;
    private List<HashMap<String, Object>> mAppList = null;
    private List<String> mAppFilter = null;
    private PackageManager mPackageManager = null;
    private boolean mIsWeb = false;
    private boolean mIsFilter = false;

    private View mAppsView;
    private VerticalViewPager mViewPager;
    private AppsPagerAdapter mAppsPagerAdapter;
    private AlertDialog mAlertDialog;
    private List<View> mViewList;
    private TextView mPageText;
    private int with = 0;
    private int height = 0;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case MSG_WEBGOBACK:
                //mWebView.goBack();
                break;
            case MSG_SHOWAPPSDIALOG: {
                if (mAlertDialog == null) {

                    mAlertDialog = new AlertDialog.Builder(mCtx).create();
                    mAlertDialog.setOnCancelListener(new OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface arg0) {
                            mAppList.clear();
                            mViewList.clear();
                            if (mIsWeb) {
                                Log.d(TAG, "web called");
                                mHandler.sendEmptyMessage(MSG_WEBGOBACK);
                            }
                            // System.gc();
                        }

                    });

                    mAlertDialog.show();
                    Window w = mAlertDialog.getWindow();
                    w.setContentView(mAppsView);
                    WindowManager.LayoutParams params = w.getAttributes();
                    // params.width = PlatformSettings.DisplayWidth * 3 / 4;
                    // params.height = PlatformSettings.DisplayHeight * 5 / 6;
                    params.width = with;
                    params.height = height;
                    Log.i(TAG,"with:"+with+" height:"+height);
                    params.dimAmount = 0f;
                    w.setAttributes(params);
                } else {
                    mAlertDialog.show();
                }
            }
                break;
            default:
                break;
            }
        }
    };

    public AppManager(Context context) {
        this.mCtx = context;
        //mWebView = webview;
        mPackageManager = context.getPackageManager();
        mAppList = new ArrayList<HashMap<String, Object>>();
        mViewList = new ArrayList<View>();
        mAppFilter = new ArrayList<String>();

        // initAppsList();

        LayoutInflater layoutInflater = LayoutInflater.from(mCtx);
        mAppsView = layoutInflater.inflate(R.layout.dialog_apps, null);
        mViewPager = (VerticalViewPager) mAppsView
                .findViewById(R.id.appsViewPager);
        mAppsPagerAdapter = new AppsPagerAdapter();
        mViewPager.setAdapter(mAppsPagerAdapter);
        mAppsPagerAdapter.notifyDataSetChanged();
        mViewPager.setOnPageChangeListener(this);

        mPageText = (TextView) mAppsView.findViewById(R.id.apps_page_text);
        mPageText.setText("-1/" + mViewList.size() + "-");
        with = ClearConfig.getPlatformWidth(945);
        height = ClearConfig.getPlatformHeight(620);
        Log.i(TAG+"init","with:"+with+" height:"+height);
    }

    // TODO poor efficient
    private void initAppsList() {
        mAppList.clear();
        mViewList.clear();

        List<PackageInfo> appsList = mPackageManager.getInstalledPackages(0);

        int pos = 0;
        for (int i = 0; i < appsList.size(); i++) {
            PackageInfo info = appsList.get(i);
            Intent intent = mPackageManager
                    .getLaunchIntentForPackage(info.applicationInfo.packageName);
            if (intent == null) {
                continue;
            }

            // do not display our app here
            if (info.applicationInfo.packageName.equals(mCtx.getPackageName())) {
                continue;
            }

            if (!packageFilter(info.applicationInfo.packageName)) {
                continue;
            }

            // String iconPath =
            // saveBitmap(info.applicationInfo.loadIcon(mPackageManager),
            // info.applicationInfo.packageName);
            HashMap<String, Object> app = new HashMap<String, Object>();
            app.put(ICON, info.applicationInfo.loadIcon(mPackageManager));
            app.put(APPNAME, info.applicationInfo.loadLabel(mPackageManager));
            app.put(PACKAGENAME, info.applicationInfo.packageName);
            app.put(VERSIONCODE, info.versionCode);
            app.put(VERSIONNAME, info.versionName);
            mAppList.add(app);

            if (mAppList.size() % APPSNUM == 0) {
                List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>(
                        mAppList.subList(pos, pos + APPSNUM));
                mViewList.add(getGridView(list));
                pos += APPSNUM;
            }
        }

        if (pos < mAppList.size()) {
            List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>(
                    mAppList.subList(pos, mAppList.size()));
            mViewList.add(getGridView(list));
        }

        if (mViewPager != null) {
            // mViewPager.removeAllViews();
            mViewPager.setAdapter(mAppsPagerAdapter);
            mAppsPagerAdapter.notifyDataSetChanged();
            mViewPager.setCurrentItem(0);
            mPageText.setText("-1/" + mViewList.size() + "-");
        }
    }

    @SuppressLint("NewApi")
    @JavascriptInterface
    public String getAppInfo(int index) {
        if (index < 0 || index >= mAppList.size()) {
            Log.w(TAG, "index " + index + " out of list");
            return null;
        }

        String retStr = null;
        JSONObject object = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        try {
            object.put("size", mAppList.size());

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("index", index);
            HashMap<String, Object> app = mAppList.get(index);
            jsonObj.put("appname", app.get(APPNAME));
            jsonObj.put("packagename", app.get(PACKAGENAME));
            jsonObj.put("icon", app.get(ICON));
            jsonObj.put("versionname", app.get(VERSIONNAME));
            jsonObj.put("versioncode", app.get(VERSIONCODE));
            jsonArray.put(jsonObj);

            object.put("application", jsonArray);
            retStr = object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return retStr;
    }

    @JavascriptInterface
    public int getAppsCount() {
        return mAppList.size();
    }

    @JavascriptInterface
    public boolean startAppByIndex(int index) {
        if (index < 0 || index >= mAppList.size()) {
            Log.w(TAG, "index " + index + " out of list");
            return false;
        }

        String packageName = getPackageName(index);
        if (packageName == null) {
            Log.w(TAG, "get package name fail");
            return false;
        }
        return PlatformSettings.launchApp(mCtx, packageName);
    }

    @SuppressLint("NewApi")
    @JavascriptInterface
    public boolean startAppByPackage(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            Log.w(TAG, "package name is null");
            return false;
        }

        return PlatformSettings.launchApp(mCtx, packageName);
    }

    /*@JavascriptInterface
    public boolean startOffice(String officePath) {
        if (officePath == null || officePath.length() <= 0) {
            Log.w(OfficeUtil.TAG, "office path is null");
            return false;
        }

        int isOffice = OfficeUtil.isOffice(officePath);
        if (isOffice != OfficeUtil.NOTOFFICE) {
            Intent intent = OfficeUtil.getOfficeFileIntent(officePath);
            try {
                mCtx.startActivity(intent);
                return true;
            } catch (Exception e) {
                Log.w(OfficeUtil.TAG, "[" + e.getMessage() + "]");
            }
        }
        return false;
    }*/

    @JavascriptInterface
    public boolean startAppTest(String packageName) {
        if (packageName == null || packageName.length() <= 0) {
            Log.w(TAG, "package name is null");
            return false;
        }

        return PlatformSettings.launchApp(mCtx, packageName);
    }

    @JavascriptInterface
    public void startBrowser() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse("http://www.baidu.com");
        intent.setData(content_url);
        mCtx.startActivity(intent);
    }

    @JavascriptInterface
    public void startBrowser(String url) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        mCtx.startActivity(intent);
    }

    @JavascriptInterface
    public boolean uninstallAppByIndex(int index) {
        if (index < 0 || index >= mAppList.size()) {
            Log.w(TAG, "index " + index + " out of list");
            return false;
        }
        String packageName = getPackageName(index);
        if (packageName == null) {
            Log.w(TAG, "get package name fail");
            return false;
        }
        Uri uri = Uri.parse("package:" + packageName);// 格式package:+包名
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        mCtx.startActivity(intent);
        return true;
    }

    @SuppressLint("NewApi")
    @JavascriptInterface
    public boolean uninstallAppByPackage(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            Log.w(TAG, "package name is null");
            return false;
        }
        Uri uri = Uri.parse("package:" + packageName);// 格式package:+包名
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        mCtx.startActivity(intent);
        return true;
    }

    @JavascriptInterface
    public boolean installApp(String fileName) {
        String extention = "";
        int i = fileName.lastIndexOf(".");
        if (i > -1 && i < fileName.length()) {
            extention = fileName.substring(i + 1);
        }
        Log.d(TAG, "extention: " + extention);

        if (extention.equalsIgnoreCase("apk")) {
            Log.w(TAG, "not apk file");
            return false;
        }

        File file = new File(fileName);
        if (!file.exists()) {
            Log.w(TAG, "file not exists");
            return false;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        mCtx.startActivity(intent);
        return true;
    }

    // use for web
    @JavascriptInterface
    public void showApps() {
        Log.d(TAG, "show all apps by web");
        mAppFilter.clear();
        mIsFilter = false;
        showAppsByNative(true);
    }

    /**
     * json格式： {"size":2, "apps":[ {"packagename":"com.kb.Carrom3DFull"},
     * {"packagename":"com.android.browser"} ] }
     **/
    @JavascriptInterface
    public void showApps(String jsonApps) {
        Log.d(TAG, "show apps by web: " + jsonApps);
        mAppFilter.clear();
        mIsFilter = true;
        try {
            JSONObject jsonObject = new JSONObject(jsonApps);
            int size = jsonObject.getInt("size");

            if (size > 0) {
                JSONArray apps = jsonObject.getJSONArray("apps");
                for (int i = 0; i < apps.length(); i++) {
                    JSONObject obj = (JSONObject) apps.get(i);
                    String packagename = obj.getString(PACKAGENAME);
                    mAppFilter.add(packagename);
                }
            }
        } catch (JSONException e) {
            mAppFilter.clear();
        }
        Log.e("zlei", "mIsFilter=" + mIsFilter + " mAppFilter size="
                + mAppFilter.size());
        showAppsByNative(true);
    }

    public void showAppsByNative(boolean isWeb) {
        Log.d(TAG, "showAppsByNative " + isWeb);

        mIsWeb = isWeb;
        if (!isWeb) {
            mAppFilter.clear();
            mIsFilter = false;
        }

        initAppsList();
        mHandler.sendEmptyMessage(MSG_SHOWAPPSDIALOG);
    }

    private String getPackageName(int index) {
        if (index < 0 || index >= mAppList.size()) {
            Log.w(TAG, "index " + index + " out of list");
            return null;
        }
        HashMap<String, Object> app = mAppList.get(index);
        return (String) app.get(PACKAGENAME);
    }

    public String saveBitmap(Drawable icon, String bmpName) {
        String filePath = "/data/data/com.clearcrane.cleartv/icons/";
        // if (PlatformSettings.getPlatform() == Platform.letv) {
        // filePath += "/sdcard/cleartv/";
        // } else if (PlatformSettings.getPlatform() == Platform.coship) {
        // filePath += "/mnt/sdcard/cleartv/";
        // } else {
        // filePath += "/cache/cleartv/";
        // }
        File dir = new File(filePath);
        dir.mkdirs();
        filePath += bmpName + ".png";
        File file = new File(filePath);
        if (file.exists()) {
            Log.d(TAG, filePath + " is exists");
            return filePath;
        }
        try {
            file.createNewFile();
            FileOutputStream fOut = null;
            fOut = new FileOutputStream(file);
            BitmapDrawable iconBmp = (BitmapDrawable) icon;
            Bitmap bmp = iconBmp.getBitmap();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            // e.printStackTrace();
            Log.d(TAG, "IOException");
            return "";
        }
        return filePath;
    }

    public class AppsAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<HashMap<String, Object>> appList = null;

        public AppsAdapter(Context context, List<HashMap<String, Object>> list) {
            this.mInflater = LayoutInflater.from(context);
            appList = list;
        }

        @Override
        public int getCount() {
            return appList.size();
        }

        @Override
        public Object getItem(int position) {
            return appList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.app_item, null);
                holder = new ViewHolder();
                holder.img = (ImageView) convertView
                        .findViewById(R.id.AppItemImage);
                holder.text = (TextView) convertView
                        .findViewById(R.id.AppItemText);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            HashMap<String, Object> app = appList.get(position);

            holder.img.setImageDrawable((Drawable) app.get(ICON));
            holder.text.setText((String) app.get(APPNAME));
            holder.packageName = (String) app.get(PACKAGENAME);

            return convertView;
        }

    }

    static class ViewHolder {
        public ImageView img;
        public TextView text;
        public String packageName;
    }

    class AppsPagerAdapter extends VerticalPagerAdapter {
        @Override
        public int getCount() {
            return mViewList.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (position < mViewList.size()) {
                container.removeView(mViewList.get(position));
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mViewList.get(position), 0);
            return mViewList.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            // Object tag = ((View)object).getTag();
            // for (int i = 0; i < mViewList.size(); i++) {
            // if (object.equals(mViewList.get(i))) {
            // return i;
            // }
            // }
            return POSITION_NONE;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            super.registerDataSetObserver(observer);
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            super.unregisterDataSetObserver(observer);
        }
    }

    private GridView getGridView(List<HashMap<String, Object>> list) {
        GridView grid = new GridView(mCtx);
        grid.setNumColumns(5);
        grid.setHorizontalSpacing(50);
        grid.setVerticalSpacing(30);
        grid.setColumnWidth(121);
        grid.setStretchMode(2);
        grid.setSelector(color.transparent);
        AppsAdapter adapter = new AppsAdapter(mCtx, list);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(this);

        return grid;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        Log.d(TAG, "onItemClick");
        ViewHolder holder = (ViewHolder) view.getTag();
        String pkg = holder.packageName;
        PlatformSettings.launchApp(mCtx, pkg);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // Log.d("zlei", "onPageScrollStateChanged: " + state);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset,
            int positionOffsetPixels) {
        // Log.d("zlei", "onPageScrolled: " + position + ", " + positionOffset +
        // ", " + positionOffsetPixels);
    }

    @Override
    public void onPageSelected(int position) {
        // Log.d("zlei", "onPageSelected: " + position);
        mPageText.setText("-" + (position + 1) + "/" + mViewList.size() + "-");
    }

    private boolean packageFilter(String packageName) {
        if (!mIsFilter && mAppFilter.size() <= 0) {
            return true;
        }

        for (int i = 0; i < mAppFilter.size(); i++) {
            if (packageName.equals(mAppFilter.get(i))) {
                return true;
            }
        }

        return false;
    }
}
