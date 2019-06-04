/**
 * Created on 2015-11-29
 *
 * @author: wgq
 */
package com.clearcrane.schedule;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * 
 * 非常重要！！！
 * 
 * 维持系统时间，所有想获取当前系统时间，都必须用这个类提供的接口
 * 
 * 因为安卓不提供应用层来设置系统时间，所以我们只好自己在应用层与服务器同步时间，
 * 并在应用层内维持一个“系统时间”
 * 
 * 
 * TODO, FIXME 
 * 目前不考虑时区，服务器与客户端传递的时间都是字符串形式的时间戳，如：
 * 2015-11-29 16:00:00
 * 默认盒子与服务器都在同一时区
 * 即使不在同一时区，也不做转换
 * 默认大家理解的16:00:00就都是下午4点，而不管时区在哪，都是本地时区的下午4点
 * 
 * @author wgq
 *
 */
public class DateUtil {
	private static final String TAG = "DateUtil";
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"; 
	
	public static long systemMillsTimeDelta = 0;
	
	/* 获取当前时间戳, 绝对秒数 */
	public static long getCurrentTimeSecond() {
		return getCurrentTimeMillSecond() / 1000 ; 
	}
	
	/* 获取当前时间戳, 微秒 */
	public static long getCurrentTimeMillSecond() {
		return Calendar.getInstance().getTimeInMillis() + systemMillsTimeDelta; 
	}
	
	/* 获取当前时间戳, Calendar 对象 */
	public static Calendar getCurrentTimeCalendar() {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(getCurrentTimeMillSecond());
		return c ; 
	}
	
	
	/* 解析时间字符串，返回绝对秒数 */
	public static long getTimeSecondFromDateStr(String dateStr) {
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT); 
		try {
			Date parseDate = formatter.parse(dateStr);
			return (parseDate.getTime() / 1000);
		} catch (ParseException e) {
			Log.i(TAG, "parse date string error: " + dateStr);
			e.printStackTrace();
			return Long.MAX_VALUE;
		}
	}
	
	/* 解析时间字符串，返回绝对微秒 */
	public static long getTimeMillSecondFromDateStr(String dateStr) {
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT); 
		try {
			Date parseDate = formatter.parse(dateStr);
			return (parseDate.getTime());
		} catch (ParseException e) {
			Log.i(TAG, "parse date string error: " + dateStr);
			e.printStackTrace();
			return Long.MAX_VALUE;
		}
	}
	
	public static void setSystemTime(final Context ctx, final String str) {
		long curSystemMillTime = getCurrentTimeMillSecond();
		long newSystemMillTime = getTimeMillSecondFromDateStr(str);
		
		/* 如果差距超过5秒，我们就重设 “系统时间 ” */
		long curDelta = Math.abs(newSystemMillTime - curSystemMillTime);
		if(curDelta > 2 * 1000) {
			Log.i(TAG, "set system time " + str);
			long realSystemTime = Calendar.getInstance().getTimeInMillis();
			systemMillsTimeDelta = newSystemMillTime - realSystemTime;
		}
	}
	
	/**
	 * 获得两个时间的秒数，时分秒，不含年月日版本。
	 * @param start  00:00:00
	 * @param current 13:00:00
	 * @return
	 */
	public static long getElapsedSeconds(String start, String current){
		try{
			if(TextUtils.isEmpty(start) || TextUtils.isEmpty(current)){
				return 0;
			}
			String[] starts = start.split(":");
			String[] currents = current.split(":");

			long seconds = 3600 * (Integer.parseInt(currents[0]) - Integer.parseInt(starts[0])) + 60 * (Integer.parseInt(currents[1])-Integer.parseInt(starts[1])) +  Integer.parseInt(currents[2]) - Integer.parseInt(starts[2]);

			Log.i(TAG,"Get Elapsed Seconds: " + seconds);

			if(seconds < 0)
				return 0;
			else
				return seconds;
		}catch (Exception ex){
			ex.printStackTrace();
			return 0;
		}
	}
}
