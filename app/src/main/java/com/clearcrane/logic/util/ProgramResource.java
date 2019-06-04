package com.clearcrane.logic.util;

import android.util.Log;

import com.clearcrane.constant.ClearConstant;

import org.json.JSONException;
import org.json.JSONObject;

public class ProgramResource{
	
	private final String TAG = "ProgramResource";
	public ProgramResource(JSONObject jsonObject){
		try {
			this.name = jsonObject.getString("name");
			this.url = jsonObject.getString("source_url");
			this.layoutParamId = jsonObject.getInt(ClearConstant.STR_REGION_ID);
			this.type_id = jsonObject.getInt("type_id");
			this.duration = jsonObject.getInt("duration");
		} catch (JSONException e) {
			Log.e(TAG,"ProgramResource error json:" + jsonObject);
		}
	}
	
	private int layoutParamId; //in witch region
	private String name;
	private String url;
	private int duration;
	private int type_id;
	private int seq;
	
	
	public int getLayoutParamId() {
		return layoutParamId;
	}
	public String getName() {
		return name;
	}
	public String getUrl() {
		return url;
	}
	public int getDuration() {
		return duration;
	}
	public int getType_id() {
		return type_id;
	}
	public int getSeq() {
		return seq;
	}
	
	
}
