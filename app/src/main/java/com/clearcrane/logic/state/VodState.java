package com.clearcrane.logic.state;

import android.content.Context;
import android.os.Handler;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.view.VoDViewManager;

public class VodState extends PrisonBaseModeState {

	
	public VodState(){
		this.mStateCode = ClearConstant.CODE_VOD_STATE;
	}
	
	@Override
	public void init(Context context) {
		// TODO Auto-generated method stub
		this.init(context,null);
	}

	@Override
	public void init(Context context, Handler handler) {
		// TODO Auto-generated method stub
		super.init(context, handler);
	}

	@Override
	protected void updateVersionInfo() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initPrisonOrganism() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initStateParams() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startPlay() {
		// TODO Auto-generated method stub
		super.startPlay();
		VoDViewManager.getInstance().setActivityMode(mStateCode);
	}

	@Override
	public void stopPlay() {
		// TODO Auto-generated method stub
		super.stopPlay();
		VoDViewManager.getInstance().setActivityMode(mStateCode);
	}

	
}
