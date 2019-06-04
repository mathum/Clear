package com.clearcrane.logic.version;


import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.util.ClearConfig;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ChannelVersion extends PrisonBaseVersion {

	
	@Override
	public void init(int remoteVersion, Context ctx,
			VersionChangeListener listener) {
		// TODO Auto-generated method stub
		super.init(remoteVersion, ctx, listener);
		versionUrl  = ClearConfig.SERVER_URI + ClearConstant.STR_BACKEND_PORT + ClearConstant.URL_CHANNEL_SUFFIX;
	}

	@Override
	protected void initSharedPreferences() {
		mSharedPreferences = mContext.getSharedPreferences(ClearConstant.STR_CHANNEL, Context.MODE_PRIVATE);
	}

	@Override
	protected void parseReturnJson(String result) {
		// TODO Auto-generated method stub
		Log.e("xb", "这是什么玩意:"+result);
		JSONTokener jsonTokener = new JSONTokener(result);
		try {
			JSONObject commandJson = (JSONObject) jsonTokener.nextValue();
			int newestversion = commandJson.getInt(ClearConstant.STR_NEWEST_VERSION);
			Editor editor = mSharedPreferences.edit();
			if (newestversion == -1){
			
			}else{
				String channelId = commandJson.getString(ClearConstant.STR_CHANNEL_ID);
				Log.e(TAG,"parseReturnJson channleId " + channelId);
				editor.putString(ClearConstant.STR_CHANNEL_ID, channelId);
			}
			editor.putInt(ClearConstant.STR_NEWEST_VERSION, newestversion);
			editor.commit();
			notifyVersionChange();
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
