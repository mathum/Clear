package com.clearcrane.logic;

import android.util.Log;

import com.clearcrane.schedule.DateUtil;

import java.util.ArrayList;
import java.util.Calendar;

public class AccessTimeObject extends PrisonOrganism {

	private AccessTimeOrgan mAccessTimeOrgan;
	
	//for organ array
	public ArrayList<AccessTimeOrgan> mAccessTimeOrgarnList;//保存一天当中可以使用的所有时间段
	private int repeatation;//代表在星期几进行重复播放
	public AccessTimeObject(AccessTimeOrgan organ) {
		this.mAccessTimeOrgan = organ;
		this.workOrgan = mAccessTimeOrgan;
	}
	
	public AccessTimeObject(ArrayList<AccessTimeOrgan> list){
		mAccessTimeOrgarnList = list;
	}
    public AccessTimeObject(int repeatation){
    	this.repeatation = repeatation;
    	this.mAccessTimeOrgarnList = new ArrayList<>();
    }
	/*
	 * (non-Javadoc)
	 * @see com.clearcrane.logic.PrisonOrganism#isAwake()
	 * modify on 2016/12/28
	 * 
	 */
	@Override
	public boolean isAwake() {
		// TODO Auto-generated method stub
		if(mAccessTimeOrgarnList.size() == 0){
			return false;
		}
		Log.e("AccessTimeObject", "isInWeekDay:"+isInWeekDay()+"isInAwakeSegment:"+isInAwakeSegment());
		return isInWeekDay() && isInAwakeSegment();
	}
	private boolean isInWeekDay(){
		Calendar calendar = DateUtil.getCurrentTimeCalendar();
		int dow = getNumByDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK));
		//本日的星期和object相同返回true
		Log.e("123321", "dow:"+dow);
		return dow==repeatation;
	}
	//循环一天中所有的时间体，检查是否有需要显示的时间体。
	private boolean isInAwakeSegment(){
		int len = mAccessTimeOrgarnList.size();
		Log.e("123321","isInAwakeSegment " + len);
		int count = 0;
		for(int i = 0; i < len ; i++){
			mAccessTimeOrgan = mAccessTimeOrgarnList.get(i);
			Log.e("123321","starttime: " + mAccessTimeOrgan.dayTimeStart +"endtime:"+mAccessTimeOrgan.dayTimeEnd);
			//在非可用时间显示黑屏状态 
			if (mAccessTimeOrgan.isAlive()){
				workOrgan = mAccessTimeOrgan;
				count++;
			}
		}
		//当在遍历一天所有的可用时间发现都不在可用的区间中，那么就返回true，返回黑屏状态。
		if(count == len){
			return true;
		}else{
			return false;
		}
	}
	/*
	 * not 1234567
	 * 1 2 4 8 16 32 64
	 * for & 
	 */
	private int getNumByDayOfWeek(int dayofweek){
		switch(dayofweek){
		case Calendar.MONDAY:
			return 1;
		case Calendar.TUESDAY:
			return 2;
		case Calendar.WEDNESDAY:
			return 3;
		case Calendar.THURSDAY:
			return 4;
		case Calendar.FRIDAY:
			return 5;
		case Calendar.SATURDAY:
			return 6;
		case Calendar.SUNDAY:
			return 7;
		default:return 0;
		}
	}
	
}
