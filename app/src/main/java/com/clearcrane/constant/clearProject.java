package com.clearcrane.constant;

import android.app.TvManager;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;

import com.clearcrane.view.VoDViewManager;

public class clearProject {
	public static Context ctx;
	public static String projectName;
	
	public static final String ProjectName = "project_name";
	
	public static final String Nigeria = "Nigeria";
	
	public static final String Nigeria_BLACKDIAMOND = "BlackDiamond_LagosNG";
	
	public static final String Nuogeya = "nuogeya";
	
	public static final String Yinlu = "yinlu";
	
	public static final String Zmax = "ZMAX";
	
	public static void init(Context context){
		ctx = context;
	}
	
	//*************************重庆潮曼酒店项目，断网后不能响应音量键********************
	 private static TvManager mTvmanager;
	 private static int volume;
	 private static boolean isMute = false;
	 private static String TAG  = "clearproject";
	
	public static boolean zmaxVolume(int keyCode,Context ctx){
    	if(!VoDViewManager.getInstance().project_name.contains(clearProject.Zmax)){
    		return false;
    	}
    	mTvmanager = (TvManager) ctx.getSystemService("tv");
    	switch(keyCode) {
		case KeyEvent.KEYCODE_VOLUME_DOWN:
    		Log.i(TAG,"volume down");
    		volume = mTvmanager.getVolume();
    		volume--;
    		if (volume < 0){
    			volume = 0;
    			mTvmanager.setVolume(0);
    		}else{
    			mTvmanager.setMute(false);
    			mTvmanager.setVolume(volume);
    		}
    		return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			Log.i(TAG,"volume up");
			volume = mTvmanager.getVolume();
			volume++;
    		if (volume > 100){
    			volume = 100;
    			mTvmanager.setVolume(100);
    		}else{
    			mTvmanager.setMute(false);
    			mTvmanager.setVolume(volume);
    		}
    		return true;
		case KeyEvent.KEYCODE_VOLUME_MUTE:
			Log.i(TAG,"volume mute,volume:" + volume);
			if(isMute){
				mTvmanager.setMute(false);
				mTvmanager.setVolume(volume);
				isMute = false;
			}else{
				mTvmanager.setMute(true);
				mTvmanager.setVolume(0);
				isMute = true;
			}
    		return true;
    	}
    	return false;
    }
}
