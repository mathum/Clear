package com.clearcrane.logic.version;

import com.clearcrane.util.ClearConfig;
import com.lidroid.xutils.http.RequestParams;

public class VideoPageShot extends BaseShotScreen {
    public VideoPageShot(String action,String mac,String videoUrl,String currentTime) {
		// TODO Auto-generated constructor stub
    	this.action = action;
    	this.mac = mac;
    	this.videoUrl = videoUrl;
    	this.currentTime = currentTime;
	}
	@Override
	public RequestParams getPost() {
		// TODO Auto-generated method stub
		RequestParams params = new RequestParams();
		params.addBodyParameter("mac", ClearConfig.getMac());
		params.addBodyParameter("action", action);
		params.addBodyParameter("video_http_path", videoUrl);
		params.addBodyParameter("nowTime", currentTime);		
		return params;
	}

}
