package com.clearcrane.schedule;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Region {
	
	private final String TAG = "Region";
	
	private JSONObject mRegionJson;
	public ArrayList<ProgramSegment> programSegmentList;
	
	public Region(JSONObject jobj){
		this.mRegionJson = jobj;
		this.programSegmentList = new ArrayList<ProgramSegment>();
	}

	public void init(){
		Log.e(TAG,"region init start jobj :" + mRegionJson);
		try{
		    JSONArray triggers = mRegionJson.getJSONArray("triggers");
		    for (int i = 0; i < triggers.length(); i++){
		    	JSONObject programSegmentJson = (JSONObject) triggers.opt(i);
		    	ProgramSegment ps = new ProgramSegment();
		    	ps.init(programSegmentJson);
		    	programSegmentList.add(ps);
		    }
		    Log.e(TAG,"init region OK!");
		}catch(Exception e){
			Log.e(TAG,"error init region " + mRegionJson);
		}
	}
}
