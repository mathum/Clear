package com.clearcrane.schedule;

import android.util.Log;

import org.json.JSONObject;

public class Material extends ScheduleObject{

	private final String TAG = "Material";
	private final String PREFIX_VIDEO_PATH = "/Main/resource/";
	private String materialName;
	private int materialType;
	private int duration;
	private String srcUrl;
	
	public String getSrcUrl(){
//		if (materialType == 2){
			return this.srcUrl;
//		}else{
//			/*TODO,FIXME quite low,here*/
//			String relate_path = "";
//			if (srcUrl != null){
//				String[] temp = srcUrl.split("/"); 
//				relate_path = PREFIX_VIDEO_PATH + temp[temp.length -1 ];
//				Log.e(TAG,"relate_path " + relate_path);
//			}
//			if (relate_path.equals("")){
//				return this.srcUrl;
//			}
//			String resultUrl = ClearConfig.getJsonUrl(null, relate_path);
//			Log.e(TAG,"resultUrl " + resultUrl);
//			return resultUrl;
//		}
	}
	
	public void init(JSONObject jsonobj){
		Log.e(TAG,"init material start " + jsonobj);
		try{
			materialName = jsonobj.getString("material_name");
			materialType = jsonobj.getInt("material_type");
			srcUrl = jsonobj.getString("material_path");
			duration = jsonobj.getInt("duration");
			Log.e(TAG,"GETSRCURL" + getSrcUrl());
		}catch(Exception e){
			Log.e(TAG,"init material error " + jsonobj);
		}
	}
	
}
