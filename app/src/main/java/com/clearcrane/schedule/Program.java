package com.clearcrane.schedule;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Program extends ScheduleObject{

	private final String TAG = "Program";
	private int priority;
    private JSONObject mProgramJson;
    public String mProgramName;
    private String repeatdow;
    
    public String getRepeatDow(){
    	return this.repeatdow;
    }
	
    public Map<Integer,Region> mRegionMap; 
    
    
	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public Program(JSONObject jsonobj){
		this.mProgramJson = jsonobj;
		this.mRegionMap = new HashMap<Integer,Region>();
	}
	
	public void init(){
		Log.e(TAG,"init start program " + mProgramJson);
		try{
			LifeStartTime = mProgramJson.getString("valid_time_start");
			LifeEndTime = mProgramJson.getString("valid_time_end");
//			LifeEndTime="2133-12-12 00:01:02";
//			LifeStartTime = "2011-12-12 00:00:12";
			priority = mProgramJson.getInt("priority");
			mProgramName = mProgramJson.getString("name");
			repeatdow = mProgramJson.getString("repeat");
			Log.e(TAG,"init program repeat ok!");
			JSONArray regionArray = mProgramJson.getJSONArray("regions");
			int regionLength = regionArray.length();
 
			for (int i = 0; i < regionLength; i ++){
				JSONObject jobj = (JSONObject) regionArray.opt(i);
				int seq = jobj.getInt("seq");
				Log.e(TAG,"init program seq ok!");
				Region region = new Region(jobj);
				Log.e(TAG,"init program jobj : "+ jobj);
				region.init();
				Log.e(TAG,"to add one region");
				mRegionMap.put(seq, region);
			}
			
			
			
			
		}catch(Exception e)
		{
			Log.e(TAG,"init program error! "+mProgramJson);
		}
	}
}
