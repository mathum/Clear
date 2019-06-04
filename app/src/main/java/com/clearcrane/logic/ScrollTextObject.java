package com.clearcrane.logic;


public class ScrollTextObject extends PrisonOrganism {
	
	private ScrollTextOrgan mScrollTextOrgan;
	

	public ScrollTextObject(ScrollTextOrgan organ){
		this.mScrollTextOrgan = organ;
		this.workOrgan = organ;
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
		return isAlive() && mScrollTextOrgan.isAlive();
	}
	
	
}
