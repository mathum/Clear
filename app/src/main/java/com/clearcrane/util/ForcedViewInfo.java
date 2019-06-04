package com.clearcrane.util;

import android.util.Log;

import com.clearcrane.constant.ClearConstant;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;

public class ForcedViewInfo {

	public String url;
	public String type;
	
	String postUrl = ClearConfig.SERVER_URI + ClearConstant.STR_BACKEND_PORT + ClearConstant.URL_TERM_FORCED_SUFFIX;
	
	protected String mac = ClearConfig.getMac();
	
	
	public ForcedViewInfo(){
		new Thread(getRemoteVersion).start();
	}	
	
	
	
	protected String getParams(){
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("mac", mac);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonObject.toString();
	}
	protected final String TAG = this.getClass().getSimpleName();
	protected Runnable getRemoteVersion = new Runnable() {
		
		@Override
		public void run() {
			Log.e(TAG,"vurl " + postUrl);
			HttpUtils httpUtils = new HttpUtils();
			httpUtils.configCurrentHttpCacheExpiry(0);
			RequestParams requestParams = new RequestParams();
			try {
				requestParams.setBodyEntity(new StringEntity(getParams(),"utf-8"));
				httpUtils.send(HttpMethod.POST, postUrl,requestParams,requestCallBack);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
	
    RequestCallBack<String> requestCallBack = new RequestCallBack<String>() {
		
		@Override
		public void onSuccess(ResponseInfo<String> arg0) {
			Log.i(TAG,"onSuccess result:" + arg0.result);
			parseReturnJson(arg0.result);
		}
		
		@Override
		public void onFailure(HttpException arg0, String arg1) {
			Log.e(TAG,"onFailure result:" + arg1);
		}
	};
	
	private void parseReturnJson(String result){
		JSONTokener jtk = new JSONTokener(result);
		JSONObject obj;
		try{
			obj = (JSONObject) jtk.nextValue();
			url = obj.getString("url");
			type = obj.getString("type");
		}catch (JSONException e) {
			// TODO Auto-generated catch block
			url = "";
			type = "";
			e.printStackTrace();
		}
			
		
	}
}
