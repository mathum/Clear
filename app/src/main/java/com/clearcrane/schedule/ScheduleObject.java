package com.clearcrane.schedule;

public class ScheduleObject {

	public String LifeStartTime;         //总生命周期开始时刻
	public String LifeEndTime;    //总生命周期结束时刻
	
	private long trigger_Duration;
	private String start_trigger;
	
	public String getTimeSegmentTriggerTimeStr() {
		return start_trigger;
	}
	
	public void setTimeSegmentTriggerTimeStr(String trigger){
		this.start_trigger = trigger;
	}
	
	public void setTimeSegmentDuration(long duration){
		this.trigger_Duration = duration;
	}
	
	public long getTimeSegmentDuration() {
		return trigger_Duration;
	}
}
