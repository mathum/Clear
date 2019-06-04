package com.clearcrane.logic.version;

import com.clearcrane.util.ClearConfig;
import com.lidroid.xutils.http.RequestParams;

public class LivePageShot extends BaseShotScreen {
    public LivePageShot(String action,String mac,String url) {
		// TODO Auto-generated constructor stub
    	this.liveUrl = url;
    	this.action = action;
    	this.mac = mac;
	}
	@Override
	public RequestParams getPost() {
		// TODO Auto-generated method stub
		RequestParams params = new RequestParams();
		params.addBodyParameter("mac", ClearConfig.getMac());
		params.addBodyParameter("action", action);
		params.addBodyParameter("live_path", liveUrl);
		return params;
	}

}
