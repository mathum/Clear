package com.clearcrane.schedule;

import java.util.Calendar;


public class Scheduler {

	
	public static class ScheduleResult {
		public ScheduleObject playObj = null; 
		public long finishTime; //绝对时间戳, 单位秒
		
		
		/* -1 : 立刻播放；others, 延迟播放  */
		/**
		 * TODO, FIXME，如果有一个好的算法，能方便计算支持复选功能的定时时刻
		 * 再用这种方法了，目前是每秒检查一下有无定时发生
		 */
		//public long nextPlayDelay;            //seconds 单位秒
		
		/* 返回的节目理论上已经播放了多长时间，对于定时节目而言 */
		//public long elapseTime;              //seconds 单位秒
	}
	
	//check day of week ---dow
	public static boolean isInWeekday(String segment){
		Calendar curTime = DateUtil.getCurrentTimeCalendar();
		if (segment.equals("*") == true)
			return true;
		String[] dows = segment.split(",");
		for(String dow:dows){
			if(curTime.get(Calendar.DAY_OF_WEEK) == getCalendarDOWByStr(dow))
				return true;
		}
		return false;
	}
	
	public static boolean isInWeekday(Program prog){
		String segment  = prog.getRepeatDow();
		return isInWeekday(segment);
	}
	
	public static boolean isInLifeCycle(ScheduleObject obj) {
		long lifeStart = DateUtil.getTimeSecondFromDateStr(obj.LifeStartTime);
		long lifeEnd = DateUtil.getTimeSecondFromDateStr(obj.LifeEndTime);
		long cur = DateUtil.getCurrentTimeSecond();
		
		//Log.d(TAG, "isInLifeCycle start: " + lifeStart + " end: " + lifeEnd + " cur: " + cur);
		
		if(lifeStart > cur || lifeEnd <= cur) {
			return false;
		}
		
		return true;
	}
	
	
	/**
	 *  是否在时间段限制里
	 */
	public static boolean isInTimeSegment(ScheduleObject obj) {
		
		ScheduleResult result = new ScheduleResult();
		boolean isIn = isInTimeSegmentDuration(
				obj.getTimeSegmentTriggerTimeStr(),
				obj.getTimeSegmentDuration(), 
				result);
		if(isIn == true) {
			result.playObj = obj;
		}
		return isIn;
	}
	
	
	private static boolean isInTimeSegmentDuration(String triggerStr, long duration, ScheduleResult result) {
		Calendar curTime = DateUtil.getCurrentTimeCalendar();
		Calendar triggerTime = DateUtil.getCurrentTimeCalendar();
		
		if(triggerStr == null || result == null)
			return false;
		
		String[] segments = triggerStr.split(" ");
		if(segments == null || segments.length != 7) {
			return false;
		}
		
		/* 周，年月日，直接判断是否相等，由此可见，计算粒度是在天 */
		//compare day of week
		if(segments[6].equals("*") == false) {
			String[] dows = segments[6].split(",");
			boolean dowBingo = false;
			for(String dow:dows) {
				if(curTime.get(Calendar.DAY_OF_WEEK) == getCalendarDOWByStr(dow))
					dowBingo = true;
			}
			if(dowBingo == false)
				return false;
		}
		
		//compare year
		if(segments[5].equals("*") == false) {
			String[] years = segments[5].split(",");
			boolean yearBingo = false;
			for(String year:years) {
				if(curTime.get(Calendar.YEAR) == getCalendarYearByStr(year))
					yearBingo = true;
			}
			if(yearBingo == false)
				return false;
		}
		
		//compare month
		if(segments[4].equals("*") == false) {
			String[] months = segments[4].split(",");
			boolean monthBingo = false;
			for(String month:months) {
				if(curTime.get(Calendar.MONTH) == getCalendarMonthByStr(month))
					monthBingo = true;
			}
			if(monthBingo == false)
				return false;
		}

		//compare day
		if(segments[3].equals("*") == false) {
			String[] days = segments[3].split(",");
			boolean dayBingo = false;
			for(String day:days) {
				if(curTime.get(Calendar.DAY_OF_MONTH) == getCalendarDayByStr(day))
					dayBingo = true;
			}
			if(dayBingo == false)
				return false;
		}
		
		/* 时分 计算离现在最近的过去某个触发时刻点 */
		
		/* 计算 “时” */
		if(segments[2].equals("*") == false) {
			String[] hours = segments[2].split(",");
			
			/* 如果当前 hour 比预定最小的还小，则未到 */
			if(hours.length <= 0 || 
				curTime.get(Calendar.HOUR_OF_DAY) < getCalendarHourByStr(hours[0])) {
				return false;
			}
			
			/* 找到 hours 里面离现在最近的（小于）, 要求给予的复选字段是排序的，从小到大 */
			boolean hourBingo = false;
			int i = 0; 
			for(; i < hours.length; i++) {
				if(curTime.get(Calendar.HOUR_OF_DAY) == getCalendarHourByStr(hours[i])) {
					hourBingo = true;
					break;
				}
				else if(curTime.get(Calendar.HOUR_OF_DAY) < getCalendarHourByStr(hours[i])){
					break;
				}
			}
			if(hourBingo == false) {
				/* 设时为最近的过去某时刻 */
				triggerTime.set(Calendar.HOUR_OF_DAY, getCalendarHourByStr(hours[i - 1]));
				triggerTime.set(Calendar.MINUTE, getCalendarMinuteByStr(
						getBiggestValueFromTriggerSegment(segments[1])));
				triggerTime.set(Calendar.SECOND, getCalendarSecondByStr(
						getBiggestValueFromTriggerSegment(segments[0])));
				if((triggerTime.getTimeInMillis() / 1000 + duration)
						<= (curTime.getTimeInMillis() / 1000)) {
					/* 已经超过播放时长了 */
					return false;
				}
				
				result.finishTime = (triggerTime.getTimeInMillis() / 1000) +  duration;
				return true;
			}
			
			/* 时相等，往下继续比对分 */		
		}
         
		/* 比对分 */	
		if(segments[1].equals("*") == false) {
			String[] minutes = segments[1].split(",");
			
			/* 如果当前 分 比预定最小的还小，则未到 */
			if(minutes.length <= 0 || 
				curTime.get(Calendar.MINUTE) < getCalendarHourByStr(minutes[0])) {
				return false;
			}
			
			/* 找到 分 里面离现在最近的（小于）, 要求给予的复选字段是排序的，从小到大 */
			boolean minuteBingo = false;
			int i = 0; 
			for(; i < minutes.length; i++) {
				if(curTime.get(Calendar.MINUTE) == getCalendarHourByStr(minutes[i])) {
					minuteBingo = true;
					break;
				}
				else if(curTime.get(Calendar.MINUTE) < getCalendarHourByStr(minutes[i])){
					break;
				}
			}
			
			if(minuteBingo == false) {
				/* 设时为最近的过去某时刻 */
				triggerTime.set(Calendar.MINUTE, getCalendarHourByStr(minutes[i - 1]));
				triggerTime.set(Calendar.SECOND, getCalendarSecondByStr(
						getBiggestValueFromTriggerSegment(segments[0])));
				if((triggerTime.getTimeInMillis() / 1000 + duration)
						<= (curTime.getTimeInMillis() / 1000)) {
					/* 已经超过播放时长了 */
					return false;
				}
				
				result.finishTime = (triggerTime.getTimeInMillis() / 1000) +  duration;
				return true;
			}
			
			/* 分相等，往下继续比对秒 */	
		}
		
		/* 比对秒 */	
		if(segments[0].equals("*") == false) {
			String[] seconds = segments[0].split(",");
			
			/* 如果当前 分 比预定最小的还小，则未到 */
			if(seconds.length <= 0 || 
				curTime.get(Calendar.SECOND) < getCalendarHourByStr(seconds[0])) {
				return false;
			}
			
			/* 找到 分 里面离现在最近的（小于）, 要求给予的复选字段是排序的，从小到大 */
			boolean secondBingo = false;
			int i = 0; 
			for(; i < seconds.length; i++) {
				if(curTime.get(Calendar.SECOND) == getCalendarHourByStr(seconds[i])) {
					secondBingo = true;
					break;
				}
				else if(curTime.get(Calendar.SECOND) < getCalendarHourByStr(seconds[i])){
					break;
				}
			}
			
			if(secondBingo == false) {
				/* 设时为最近的过去某时刻 */
				triggerTime.set(Calendar.SECOND, getCalendarHourByStr(seconds[i - 1]));
				if((triggerTime.getTimeInMillis() / 1000 + duration)
						<= (curTime.getTimeInMillis() / 1000)) {
					/* 已经超过播放时长了 */
					return false;
				}
				
				result.finishTime = (triggerTime.getTimeInMillis() / 1000) +  duration;
				return true;
			}
		}
		
		/* 都对上了，正好一个定时节目开始 */
		result.finishTime = (curTime.getTimeInMillis() / 1000) +  duration;
		return true;
	}
		
		private static String getBiggestValueFromTriggerSegment(String segment) {
			if(segment.equals("*")) {
				/* May be we should return 60, 24... */
				return "0";  
			}
			
			String[] subValues = segment.split(",");
			if(subValues != null && subValues.length > 0) {
				return subValues[subValues.length - 1];
			}
			
			return "0";
		}
		
		
		
		private static int getCalendarDOWByStr(String str) {
			if(str.equals("1"))
				return Calendar.MONDAY;
			if(str.equals("2"))
				return Calendar.TUESDAY;
			if(str.equals("3"))
				return Calendar.WEDNESDAY;
			if(str.equals("4"))
				return Calendar.THURSDAY;
			if(str.equals("5"))
				return Calendar.FRIDAY;
			if(str.equals("6"))
				return Calendar.SATURDAY;
			if(str.equals("7"))
				return Calendar.SUNDAY;
			return -1;
		}
		
		private static int getCalendarYearByStr(String str) {
			return Integer.parseInt(str);
		}
		
		private static int getCalendarMonthByStr(String str) {
			/*
			if(str.equals("1"))
				return Calendar.JANUARY;
			....
			*/
			int m = Integer.parseInt(str);
			return m - 1;
		}
		
		private static int getCalendarDayByStr(String str) {
			return Integer.parseInt(str);
		}
		
		private static int getCalendarHourByStr(String str) {
			return Integer.parseInt(str);
		}
		
		private static int getCalendarMinuteByStr(String str) {
			return Integer.parseInt(str);
		}
		
		private static int getCalendarSecondByStr(String str) {
			return Integer.parseInt(str);
		}
		
}
