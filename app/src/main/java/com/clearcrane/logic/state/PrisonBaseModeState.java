package com.clearcrane.logic.state;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import com.clearcrane.logic.PrisonOrganism;
import com.clearcrane.logic.version.VersionChangeListener;

public abstract class PrisonBaseModeState {

	protected int mStateCode;
	protected String TAG = getClass().getSimpleName();
	protected boolean isPlaying = false;
	protected SharedPreferences mPreference;
	protected Context mContext;
	protected int curVersion;
	protected PrisonOrganism mPrisonOrganism;
	protected Handler mHandler;
	
	public boolean isPlaying(){
		return isPlaying;
	}
	

	public int getCurVersion() {
		return curVersion;
	}

	public void setCurVersion(int curVersion) {
		this.curVersion = curVersion;
	}

	protected VersionChangeListener mListener = new VersionChangeListener() {
		
		@Override
		public void versionChanges() {
			// TODO Auto-generated method stub
			updateVersionInfo();
		}
		
		@Override
		public int getCurrentVersion() {
			// TODO Auto-generated method stub
			return curVersion;
		}
	};
	
	/*
	 *  0 for vod default
	 *  1 for accesstime
	 *  2 for intercut
	 *  3 for channel
	 */
	protected int mode = 0;
	
	/* 
	 * callback,to change VersionInfo in PrisonBaseVersion
	 * by VersionChangeListener
	 * just reinit()
	 */
	protected abstract void updateVersionInfo();
	
	protected void reinit(){
		Log.i(TAG,"reinit");
		if(isPlaying){
			stopPlay();
		}
		initStateParams();
		initPrisonOrganism();
	}
	
	public VersionChangeListener getVersionChangeListener(){
		return this.mListener;
	}
	
	public  void init(Context context){
		this.init(context,null);
	};
	
	public void init(Context context,Handler handler){
		this.mContext = context;
		this.mHandler = handler;
	}
	
	
	public abstract void initPrisonOrganism();
	public abstract void initStateParams();
	
	/*
	 * must be called in Main Thread!@!@#@#@#$	
	 */
	public void startPlay(){
		if(isPlaying){
			Log.i(TAG,"is playing do nothing!");
		}else{
			isPlaying = true;
		}
	}
	
	public void stopPlay(){
		isPlaying = false;
	}
	
	public boolean isReady(){
		return false;
	}
	
}
