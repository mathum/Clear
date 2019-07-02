package com.clearcrane.logic;

import android.util.Log;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.logic.util.ProgramResource;
import com.clearcrane.schedule.DateUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
 * 时间片
 * 	TODO,FIXME
 * 这里isAlive的判断逻辑要自行定义
 * 不能用父类的方法来判断
 * 这个比较特殊，以天为单位，判断在一天中是否活着
 */
public class ProgramOrgan extends Organism {

    private final String TAG = "ProgramOrgan";
    private SimpleDateFormat mFormatter = new SimpleDateFormat("HH:mm:ss");
    private String dayStartTime, dayEndTime;

    public String getDayStartTime() {
        return dayStartTime;
    }

    public void setDayStartTime(String dayStartTime) {
        this.dayStartTime = dayStartTime;
    }

    public String getDayEndTime() {
        return dayEndTime;
    }

    public void setDayEndTime(String dayEndTime) {
        this.dayEndTime = dayEndTime;
    }

    //存储一个节目单中得所有时间片
    private List<ProgramResource> mResourceList;
    private int index = 0;

    public List<ProgramResource> getmResourceList() {

        return mResourceList;
    }

    public ProgramOrgan(List<ProgramResource> rlist) {
        this.mResourceList = rlist;
    }

    public ProgramOrgan(JSONObject jsonObject, int index) {
        mResourceList = new ArrayList<ProgramResource>();
        try {
            this.index = index;
            this.dayStartTime = jsonObject.getString(ClearConstant.STR_START_TIME);
            this.dayEndTime = jsonObject.getString(ClearConstant.STR_END_TIME);
            JSONArray resources = jsonObject.getJSONArray("materials");
            for (int i = 0; i < resources.length(); i++) {
                JSONObject resouceJson = (JSONObject) resources.opt(i);
                ProgramResource pr = new ProgramResource(resouceJson);
                //Log.e("11111111111111111", "url = " + pr.getUrl());
                mResourceList.add(pr);
            }
        } catch (JSONException e) {
            Log.e(TAG, "ProgramOrgan error json: " + jsonObject);
        }

    }

    private int priority;

    public ProgramOrgan(JSONObject jsonObject, int index, int priority) {
        this.priority = priority;
        mResourceList = new ArrayList<ProgramResource>();
        try {
            this.index = index;
            this.dayStartTime = jsonObject.getString(ClearConstant.STR_START_TIME);
            this.dayEndTime = jsonObject.getString(ClearConstant.STR_END_TIME);
            JSONArray resources = jsonObject.getJSONArray("materials");
            for (int i = 0; i < resources.length(); i++) {
                JSONObject resouceJson = (JSONObject) resources.opt(i);
                ProgramResource pr = new ProgramResource(resouceJson, priority);
                //Log.e("11111111111111111", "url = " + pr.getUrl());
                mResourceList.add(pr);
            }
        } catch (JSONException e) {
            Log.e(TAG, "ProgramOrgan error json: " + jsonObject);
        }

    }

    @Override
    public void init(String start_time, String end_time) {
        // TODO Auto-generated method stub
        super.init(start_time, end_time);
        this.dayStartTime = start_time;
        this.dayEndTime = end_time;
    }

    @Override
    public boolean isAlive() {
        int startSeconds;
        if (index == 1) {
            startSeconds = getTimeSecondsInOneDay(dayStartTime) + 4;
            Log.e("zxb", "startSeconds---after" + startSeconds);
        } else {
            startSeconds = getTimeSecondsInOneDay(dayStartTime);
            Log.e("zxb", "startSeconds---before" + startSeconds);
        }
        int endSeconds = getTimeSecondsInOneDay(dayEndTime);
        // 獲取當前時間戳
        Date date = new Date(DateUtil.getCurrentTimeMillSecond());
        String nowTimeStr = mFormatter.format(date);
        Log.i("zxb", "timeStr " + nowTimeStr);
        int serverSeconds = getTimeSecondsInOneDay(nowTimeStr);

        Log.e("zxb", "isAlive ser " + nowTimeStr + " start" + dayStartTime + " end" + dayEndTime);
        Log.e("zxb", "isAlive ser" + serverSeconds + " start" + startSeconds + " end" + endSeconds);
        if (serverSeconds >= startSeconds && serverSeconds <= endSeconds) {
            Log.e("zxb", "繼續播放");
            return true;
        } else {
            Log.e("zxb", "結束播妨");
            return false;
        }
    }

    /*
     * TODO,FIXME to put it in a common place param timeStr formate "HH:mm:ss"
     * returns seconds in one day
     */
    public int getTimeSecondsInOneDay(String timeStr) {
        String[] times = timeStr.split(":");
        if (times.length != 3) {
            Log.e(TAG, "getTimeSecondsInOneDay error str " + timeStr);
            return -1;
        }
        int hour = Integer.parseInt(times[0]);
        int minute = Integer.parseInt(times[1]);
        int second = Integer.parseInt(times[2]);
        return (hour * 3600 + minute * 60 + second);
    }

}
