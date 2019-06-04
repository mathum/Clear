package com.clearcrane.logic.version;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Log;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.util.ClearConfig;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ScrollTextVersion extends PrisonBaseVersion {
	
	@Override
	public void init(int remoteVersion, Context ctx, VersionChangeListener listener) {
		super.init(remoteVersion, ctx,listener);
		this.versionUrl = ClearConfig.SERVER_URI + ClearConstant.STR_BACKEND_PORT + ClearConstant.URL_SCROLL_TEXT_SUFFIX;
	}

	@Override
	protected void initSharedPreferences() {
		mSharedPreferences = mContext.getSharedPreferences(ClearConstant.STR_SCROLL_TEXT, Context.MODE_PRIVATE);
	}
	
	@Override
	protected void parseReturnJson(String result) {
		Log.e("scrolltext", result);
		JSONTokener jsonTokener = new JSONTokener(result);
		try {
			JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
			int newestversion = jsonObject.getInt(ClearConstant.STR_NEWEST_VERSION);
			Editor editor = mSharedPreferences.edit();
			if (newestversion == -1){
			
			}else{
				JSONObject commandJson = jsonObject.getJSONObject(ClearConstant.STR_COMMAND_CONTENT);
				String commandContent = commandJson.getString(ClearConstant.STR_CONTENT);
				String starttime = commandJson.getString(ClearConstant.STR_START_TIME);
				String endtime = commandJson.getString(ClearConstant.STR_END_TIME);
				String title = commandJson.getString(ClearConstant.STR_TITLE);
			    JSONObject scrollStyle = commandJson.getJSONObject(ClearConstant.STR_SCROLL_STYLE);
			    
			    String color = scrollStyle.getString(ClearConstant.STR_COLOR);
			    String interval = scrollStyle.getString(ClearConstant.STR_INTERVAL);
			    String location = scrollStyle.getString(ClearConstant.STR_LOCATION);
			    String font_family = scrollStyle.getString(ClearConstant.STR_FONT_FAMILY);
				String direction = null;
			    try{
					direction = scrollStyle.getString(ClearConstant.STR_TYPE_DIRECTION);
				}catch (Exception ex){
			    	ex.printStackTrace();
					direction = "r2l";
				}
			    if(TextUtils.isEmpty(direction)) {
					direction = "r2l";
				}
			    if(scrollStyle.has(ClearConstant.STR_FONT_SIZE)){
			    	String font_size = scrollStyle.getString(ClearConstant.STR_FONT_SIZE);
			    	editor.putString(ClearConstant.STR_FONT_SIZE, font_size);
			    }
				editor.putString(ClearConstant.STR_CONTENT, commandContent);
				editor.putString(ClearConstant.STR_START_TIME, starttime);
				editor.putString(ClearConstant.STR_END_TIME, endtime);
				editor.putString(ClearConstant.STR_TITLE, title);
				editor.putString(ClearConstant.STR_COLOR, color);
				editor.putString(ClearConstant.STR_INTERVAL, interval);
				editor.putString(ClearConstant.STR_LOCATION, location);
				editor.putString(ClearConstant.STR_FONT_FAMILY, font_family);
				editor.putString(ClearConstant.STR_TYPE_DIRECTION, direction);
			}
			editor.putInt(ClearConstant.STR_NEWEST_VERSION, newestversion);
			editor.commit();
			notifyVersionChange();
			
		} catch (JSONException e) {
			Log.e(TAG,"parseReturnJson error!");
			e.printStackTrace();
		}
		
	}
	
	
	
	
}
