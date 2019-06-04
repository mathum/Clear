package com.clearcrane.logic.version;

import com.clearcrane.util.ClearConfig;
import com.lidroid.xutils.http.RequestParams;

public class StaticPageShot extends BaseShotScreen {
	public StaticPageShot(String action,String mac,int resource_id) {
		// TODO Auto-generated constructor stub
		this.action = action;
		this.mac = mac;
		this.resource_id = resource_id;
	}
	
	@Override
	public RequestParams getPost() {
		// TODO Auto-generated method stub
		RequestParams params = new RequestParams();
		params.addBodyParameter("mac", ClearConfig.getMac());
		params.addBodyParameter("action", "snapshot_url");
		params.addBodyParameter("resource_id", String.valueOf(resource_id));
		return params;
	}
}
