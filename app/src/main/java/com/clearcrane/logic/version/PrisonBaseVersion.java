package com.clearcrane.logic.version;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.clearcrane.util.ClearConfig;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public abstract class PrisonBaseVersion {

    protected String mac = ClearConfig.getMac();

    protected final String TAG = this.getClass().getSimpleName();
    private int curVersion;
    protected String versionUrl;
    protected Context mContext;
    protected VersionChangeListener versionChangeListener;
    protected Runnable getRemoteVersion = new Runnable() {

        @Override
        public void run() {
            HttpUtils httpUtils = new HttpUtils();
            httpUtils.configCurrentHttpCacheExpiry(0);
            RequestParams requestParams = new RequestParams();
            try {
                Log.e("xb", "prisonBaseVersion:" + versionUrl);
                requestParams.setBodyEntity(new StringEntity(getParams(), "utf-8"));
                httpUtils.send(HttpMethod.POST, versionUrl, requestParams, requestCallBack);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };
    protected SharedPreferences mSharedPreferences;

    public PrisonBaseVersion() {

    }

    public void init(int remoteVersion, Context ctx, VersionChangeListener listener) {
        this.curVersion = remoteVersion;
        this.mContext = ctx;
        this.versionChangeListener = listener;
        initSharedPreferences();
    }

    protected abstract void initSharedPreferences();

    /*
     * parse return version json
     */
    protected abstract void parseReturnJson(String result);

    //得到需要插播的内容
    public void updateVersion() {
        new Thread(getRemoteVersion).start();
    }


    protected String getParams() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("mac", mac);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jsonObject.toString();
    }


    protected void notifyVersionChange() {
        versionChangeListener.versionChanges();
    }

    RequestCallBack<String> requestCallBack = new RequestCallBack<String>() {

        @Override
        public void onSuccess(ResponseInfo<String> arg0) {
            Log.i(TAG, "onSuccess result:" + arg0.result);
            parseReturnJson(arg0.result);
        }

        @Override
        public void onFailure(HttpException arg0, String arg1) {
            Log.e(TAG, "onFailure resulte:" + arg1);
        }
    };

}
