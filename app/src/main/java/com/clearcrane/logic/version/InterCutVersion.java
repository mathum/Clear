package com.clearcrane.logic.version;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.util.ClearConfig;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class InterCutVersion extends PrisonBaseVersion {
	
	@Override
	public void init(int remoteVersion, Context ctx,
			VersionChangeListener listener) {
		// TODO Auto-generated method stub
		super.init(remoteVersion, ctx, listener);
		versionUrl  = ClearConfig.SERVER_URI + ClearConstant.STR_BACKEND_PORT + ClearConstant.URL_INTER_CUT_SUFFIX;
	}

	@Override
	protected void initSharedPreferences() {
		// TODO Auto-generated method stub
		mSharedPreferences = mContext.getSharedPreferences(ClearConstant.STR_INTER_CUT, Context.MODE_PRIVATE);
	}

	@Override
	protected void parseReturnJson(String result) {
		// TODO Auto-generated method stub
		Log.e("xb", "result"+result);
		JSONTokener jsonTokener = new JSONTokener(result);
		try {
			JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
			int newestversion = jsonObject.getInt(ClearConstant.STR_NEWEST_VERSION);
			Editor editor = mSharedPreferences.edit();
			if (newestversion == -1){
				
			}else{
				JSONObject commandJson = jsonObject.getJSONObject(ClearConstant.STR_CONTENT);
				String startTime = commandJson.getString(ClearConstant.STR_START_TIME);
				String endTime = commandJson.getString(ClearConstant.STR_END_TIME);
				String title = commandJson.getString(ClearConstant.STR_TITLE);
				String sourceUrl = commandJson.getString(ClearConstant.STR_SOURCE_URL);
				Log.e("xb1","sourceUrl:"+sourceUrl);
				String interCutType = commandJson.getString(ClearConstant.STR_TYPE);
			    if (interCutType.equals("picture")) {
					String timeInterval = commandJson.getString(ClearConstant.STR_TIME_INTERVAL);
					editor.putString(ClearConstant.STR_TIME_INTERVAL, timeInterval);
				}
			    if(interCutType.equals("audio")){
			    	String songName = commandJson.getString(ClearConstant.STR_MATERIAL_NAME);
			    	String duration = commandJson.getString(ClearConstant.STR_DURATION);
			    	editor.putString(ClearConstant.STR_MATERIAL_NAME, songName);
			    	editor.putString(ClearConstant.STR_DURATION, duration);
			    }
				editor.putString(ClearConstant.STR_SOURCE_URL, sourceUrl);
				editor.putString(ClearConstant.STR_START_TIME, startTime);
				editor.putString(ClearConstant.STR_END_TIME, endTime);
				editor.putString(ClearConstant.STR_TITLE, title);
				editor.putString(ClearConstant.STR_TYPE, interCutType);
			}
			editor.putInt(ClearConstant.STR_NEWEST_VERSION, newestversion);
			editor.commit();
			//更新
			notifyVersionChange();
			
		} catch (JSONException e) {
			Log.e(TAG,"parseReturnJson error!");
			e.printStackTrace();
		}
	}

}
