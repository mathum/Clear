package com.clearcrane.logic;

import android.util.Log;

import com.clearcrane.schedule.DateUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AccessTimeOrgan extends Organism {

	
	/*
	 * 时间片
	 * 	TODO,FIXME
	 * 这里isAlive的判断逻辑要自行定义
	 * 不能用父类的方法来判断
	 * 这个比较特殊，以天为单位，判断在一天中是否活着
	 * 时间格式 "23:22"
	 */
	private final String TAG = "AccessTimeOrgan";	
	public String dayTimeStart,dayTimeEnd;
	private SimpleDateFormat mFormatter = new SimpleDateFormat("HH:mm");
	
	
	
	
	public AccessTimeOrgan() {
	}

 
	public void init(String start_time, String end_time) {
		// TODO Auto-generated method stub
		this.dayTimeEnd = end_time;
		this.dayTimeStart = start_time;
	}

	/*
	 * TODO,FIXME to put it in a common place
	 * param timeStr formate "HH:mm"
	 * returns minutes in one day
	 */
	public int getTimeMinutesInOneDay(String timeStr){
		String[] times = timeStr.split(":");
		if (times.length != 2){
			Log.e(TAG,"getTimeMunutesInOneDay error str " + timeStr);
			return -1;
		}
		int hour = Integer.parseInt(times[0]);
		int minute = Integer.parseInt(times[1]);
		return (hour * 60 + minute);
	}


    @Override
    public boolean isAlive() {
	// TODO Auto-generated method stub
    	int startSeconds= getTimeSecondsInOneDay(dayTimeStart);
		int endSeconds = getTimeSecondsInOneDay(dayTimeEnd);
		// 獲取當前時間戳
		Date date = new Date(DateUtil.getCurrentTimeMillSecond());
		String nowTimeStr = mFormatter.format(date);
		Log.i("zxb", "timeStr " + nowTimeStr);
		int serverSeconds = getTimeSecondsInOneDay(nowTimeStr);

		Log.e("zxb", "isAlive ser " + nowTimeStr + " start" + dayTimeStart + " end" + dayTimeEnd);
		Log.e("zxb", "isAlive ser" + serverSeconds + " start" + startSeconds + " end" + endSeconds);
		if (serverSeconds <= startSeconds || serverSeconds >= endSeconds) {
			Log.e("zxb", "不在可用时间中");
			return true;
		} else {
			Log.e("zxb", "在可用时间中");
			return false;
		}
}
//	@Override
//	public boolean isAwake() {
//		int startMinutes = getTimeMinutesInOneDay(dayTimeStart);
//		int endMinutes = getTimeMinutesInOneDay(dayTimeEnd);
//		Date date = new Date(DateUtil.getCurrentTimeMillSecond());
//		String nowTimeStr = mFormatter.format(date);
//		int nowTimeMinutes = getTimeMinutesInOneDay(nowTimeStr);
//		return (startMinutes > nowTimeMinutes)|| (nowTimeMinutes > endMinutes);
//	}
	/*
	 * TODO,FIXME to put it in a common place param timeStr formate "HH:mm:ss"
	 * returns seconds in one day
	 */
	public int getTimeSecondsInOneDay(String timeStr) {
		String[] times = timeStr.split(":");
		if (times.length != 2) {
			Log.e(TAG, "getTimeSecondsInOneDay error str " + timeStr);
			return -1;
		}
		int hour = Integer.parseInt(times[0]);
		int minute = Integer.parseInt(times[1]);
//		int second = Integer.parseInt(times[2]);
		return (hour * 3600 + minute * 60 + 0);
	}
}
