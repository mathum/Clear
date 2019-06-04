package com.clearcrane.logic.version;


import android.util.Log;

import com.clearcrane.util.ClearConfig;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

public abstract class BaseShotScreen {
	public String action;
	public String mac;
	public int resource_id;
	public String liveUrl;
	public String videoUrl;
	public String currentTime;
    
	public abstract RequestParams getPost();
    
	public void postRequest(RequestParams params) {
		HttpUtils httpUtils = new HttpUtils();
		httpUtils.configCurrentHttpCacheExpiry(0);
		Log.e("base", params.getCharset());
		httpUtils.send(HttpMethod.POST, "http://"+ClearConfig.MAINSERVER_IP+":8000/backend/SnapShotURL", params,
				new RequestCallBack<String>() {

			@Override
			public void onFailure(HttpException arg0, String arg1) {
				// TODO Auto-generated method stub
				Log.e("base", arg1);
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				// TODO Auto-generated method stub
				Log.e("base", arg0.result);
			}
		});	
	}
}
