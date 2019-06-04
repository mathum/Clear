package com.clearcrane.logic;

import android.util.Log;

public class InterCutObject extends PrisonOrganism {
	
	private final String TAG = "InterCutObject";
	private InterCutOrgan mInterCutOrgam;
	
	public InterCutObject(InterCutOrgan organ){
		this.mInterCutOrgam = organ;
		this.workOrgan = mInterCutOrgam;
	}
	
	@Override
	public void setWorking(boolean isWorking) {
		// TODO,FIXME
		super.setWorking(isWorking);
		if (workOrgan != null){
			workOrgan.setWorking(isWorking);
		}
		
	}
	
	@Override
	public boolean isAwake() {
		Log.e(TAG,"isAwake : isAlive " + isAlive());
		return isAlive() && mInterCutOrgam.isAlive();
	}
	
}
