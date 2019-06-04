package com.clearcrane.logic.util;

import com.clearcrane.constant.ClearConstant;

import org.json.JSONException;
import org.json.JSONObject;

public class ProgramLayoutParam {
	
	public ProgramLayoutParam(JSONObject jsonObject){
		if(jsonObject == null)
			return;
		try {
			this.layoutParamId = jsonObject.getInt(ClearConstant.STR_REGION_ID);
			this.top = jsonObject.getInt("top");
			this.left = jsonObject.getInt("left");
			this.width = jsonObject.getInt("width");
			this.height = jsonObject.getInt("height");
			this.typeId = jsonObject.getInt("type_id");
			this.duration = 10;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int layoutParamId;  //for resource matrix
	public int typeId;  //1 for video 2 for pic
	public int top;   //marggin top
	public int left; //marggin left
	public int width; 
	public int height;
	public int duration;  //pic's auto change seconds

}
