package com.clearcrane.schedule;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProgramSegment extends ScheduleObject {
	
    private String TAG = "ProgramSegment";
	
	public ArrayList<Material> materialList;
	
	public ProgramSegment(){
	   	LifeEndTime = "2222-02-22 00:00:02";
	   	LifeStartTime = "2010-01-11 00:02:34";
	   	materialList = new ArrayList<Material>();
	}
	
	
	
	public void init(JSONObject jsonobj){
		Log.e(TAG,"init ps start " + jsonobj);
		try{
		    this.setTimeSegmentTriggerTimeStr(jsonobj.getString("trigger"));
			this.setTimeSegmentDuration(jsonobj.getLong("duration"));
			String start = jsonobj.getString("start");
			String end = jsonobj.getString("end");
//			this.setTimeSegmentDuration(100);
//			this.setTimeSegmentTriggerTimeStr("0 23 21 * * * *");
			JSONArray materialArray = jsonobj.getJSONArray("material_list");
			for (int i = 0; i < materialArray.length(); i++){
				JSONObject mjson = (JSONObject) materialArray.opt(i);
				Material material = new Material();
				material.init(mjson);
				materialList.add(material);
			}
		}catch(Exception e){
			Log.e(TAG,"init ps error " + jsonobj);
		}
		
	}
	
	
	
	
	
}
