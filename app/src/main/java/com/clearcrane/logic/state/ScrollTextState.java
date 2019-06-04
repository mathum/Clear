package com.clearcrane.logic.state;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.logic.ScrollTextObject;
import com.clearcrane.logic.ScrollTextOrgan;
import com.clearcrane.logic.view.ScrollTextView;

public class ScrollTextState extends PrisonBaseModeState{
	
	private String commandContent = "";
	private String startTime;
	private String endTime;
	private String commandTitle = "";
	private String color="";
	private String interval=""; 
	private String location =""; 
	private String font_family = "";
	private String direction = "";
	
	public ScrollTextState(){
		mode =  5;
	}

	//启动滚动字幕
	@Override
	public void startPlay() {
		// TODO Auto-generated method stub
		super.startPlay();
		Intent intent = new Intent();
		if (mContext == null)
			return;
		intent.setClass(mContext, ScrollTextView.class);
		intent.setAction("start");
		mContext.startService(intent);
		mPrisonOrganism.setWorking(true);
	}
    //停止滚动字幕
	@Override
	public void stopPlay() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(mContext,ScrollTextView.class);
		intent.setAction("stop");
		mContext.stopService(intent);
		mPrisonOrganism.setWorking(false);
		super.stopPlay();
	}

	@Override
	protected void updateVersionInfo() {
		// TODO Auto-generated method stub
		Log.i(TAG,"updateVersionInfo!");
		reinit();
//		startPlay();
	}



	@Override
	public boolean isReady() {
		return mPrisonOrganism.isAwake();
	}

	@Override
	public void init(Context context) {
		// TODO Auto-generated method stub
		Log.i(TAG,"init");
		super.init(context);
		this.mPreference = (SharedPreferences) mContext.getSharedPreferences(ClearConstant.STR_SCROLL_TEXT, Context.MODE_PRIVATE);
		initStateParams();
		initPrisonOrganism();
	}

	//设置滚动字幕的相关参数
	@Override
	public void initStateParams() {
		int version = mPreference.getInt(ClearConstant.STR_NEWEST_VERSION,-1);
		if (version == -1){
			Log.i(TAG,"version is -1!!");
			startTime = "2016-5-12 13:00:00";
			endTime = "2016-5-12 16:00:00";
		}else{
			commandContent = mPreference.getString(ClearConstant.STR_CONTENT,"");
			Log.i(TAG,"init state params! content :" + commandContent);
			startTime = mPreference.getString(ClearConstant.STR_START_TIME, "");
			endTime = mPreference.getString(ClearConstant.STR_END_TIME,"");
			commandTitle = mPreference.getString(ClearConstant.STR_TITLE,"");
			color = mPreference.getString(ClearConstant.STR_COLOR, "");
			direction = mPreference.getString(ClearConstant.STR_TYPE_DIRECTION ,"");
			location = mPreference.getString(ClearConstant.STR_LOCATION, "");
			interval = mPreference.getString(ClearConstant.STR_INTERVAL, "");
			font_family = mPreference.getString(ClearConstant.STR_FONT_FAMILY, "");
			
		}
		setCurVersion(version);
		return;
	}
    //初始化生命周期
	@Override
	public void initPrisonOrganism() {
		ScrollTextOrgan organ = new ScrollTextOrgan(commandContent, commandTitle);
		organ.init(startTime,endTime);
		mPrisonOrganism = new ScrollTextObject(organ);
		mPrisonOrganism.init(startTime,endTime);
	}
}
