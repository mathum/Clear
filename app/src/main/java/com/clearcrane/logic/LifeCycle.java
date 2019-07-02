package com.clearcrane.logic;


import android.util.Log;

import com.clearcrane.schedule.DateUtil;


public class LifeCycle {


    /*
     * all time is seconds since Jan 1,1970
     * if the time_str formats error
     * time_seconds =  Long.MAX TODO,FIXME
     *
     */

    private long birth_time_seconds;
    private long dead_time_seconds;


    public LifeCycle(String start_time, String end_time) {
        birth_time_seconds = DateUtil.getTimeSecondFromDateStr(start_time);
        dead_time_seconds = DateUtil.getTimeSecondFromDateStr(end_time);
    }

    public LifeCycle(String start_time, long duration) {
        birth_time_seconds = DateUtil.getTimeSecondFromDateStr(start_time);
        dead_time_seconds = birth_time_seconds + duration;
    }

    /*
     * TODO,FIXME
     * the time trigger like * * * * * * * 3000
     *
     */
    public LifeCycle(String time_triggle) {
        birth_time_seconds = 0;
        dead_time_seconds = -1;
    }

    /*
     * check is inLifeCycle
     * when format time_str error
     * the seconds is Long.MAX_VALUE
     * when long.max plus a num > 0
     * get a num < 0
     *
     */
    public boolean isInLifeCycle() {
        long now_seconds = DateUtil.getCurrentTimeSecond();
		Log.e("organrism","isAlive start " + birth_time_seconds);
		Log.e("organrism","isAlive end " + dead_time_seconds);
		Log.e("organrism","isAlive now " + now_seconds);
        if (birth_time_seconds < 0 || birth_time_seconds == Long.MAX_VALUE)
            return false;
        if (dead_time_seconds < 0 || dead_time_seconds == Long.MAX_VALUE)
            return false;

        return (now_seconds >= birth_time_seconds) && (now_seconds <= dead_time_seconds);
    }


}
