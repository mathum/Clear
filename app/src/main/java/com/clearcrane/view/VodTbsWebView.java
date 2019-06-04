package com.clearcrane.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;

import com.clearcrane.provider.MaterialRequest;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.TipDialog;
import com.clearcrane.vod.R;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Create by Tao on 2018-05-11
 */
public class VodTbsWebView extends VoDBaseView {

    public static final String TAG = VodTbsWebView.class.getSimpleName();

    private WebView mWebView = null;
    private ProgressDialog mProgressDialog = null;
    private MaterialRequest materialRequest;
    private String content;
    private String webUrl;

    public void init(Context context, String url) {
        this.context = context;
        this.url = url;
        view = LayoutInflater.from(context).inflate(R.layout.web_view_tbs, null);
        initView();
        getData(url);
    }

    private void initView() {
        mWebView = (WebView) view.findViewById(R.id.wv_contents);

        // 启用支持javascript
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDefaultTextEncodingName("UTF -8");
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setUseWideViewPort(true);//关键点
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setDisplayZoomControls(false);
        settings.setAllowFileAccess(true); // 允许访问文件
        settings.setBuiltInZoomControls(true); // 设置显示缩放按钮
        settings.setSupportZoom(true); // 支持缩放


        // 优先使用缓存
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

    }

    private void doOperation() {
        load(webUrl);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mProgressDialog.dismiss();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

                mWebView.loadUrl("file:///android_asset/error.html");
            }
        });


    }

    public void load(String url) {

        if (mWebView != null) {
            mWebView.loadUrl(url);
            mProgressDialog = ProgressDialog.show(context, null, "页面加载中，请稍后..");
            mWebView.reload();
        }
    }

    public void getData(String url) {
        materialRequest = new MaterialRequest(context,ClearConfig.TYPE_JSON);
        materialRequest.setOnCompleteListener(RoomJsonListen);
        materialRequest.execute(url);
    }

    private MaterialRequest.OnCompleteListener RoomJsonListen = new MaterialRequest.OnCompleteListener() {

        @Override
        public void onDownloaded(Object result) {
            // TODO Auto-generated method stub
            content = (String) result;
            if (content == null) {
                TipDialog.Builder builder = new TipDialog.Builder(context);
                builder.setMessage("当前网络不可用，请检查网络");
                builder.setTitle("提示");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //设置你的操作事项
                    }
                });

                builder.create().show();
                return;
            }
            try {
                if (!TextUtils.isEmpty(content)) {
                    try {
                        Log.i(TAG, "sssssssssssssss0:"+url);
                        JSONObject object = new JSONObject(content);
                        String content = object.getString("Content");
                        JSONArray contentObject = new JSONArray(content);
                        webUrl = contentObject.getJSONObject(0).getString("HtmlPath");
                        Log.i(TAG, "sssssssssssssss1:" + webUrl);
                        doOperation();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onComplete(boolean result) {
            // TODO Auto-generated method stub
        }

    };
}
