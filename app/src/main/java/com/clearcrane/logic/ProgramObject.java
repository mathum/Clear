package com.clearcrane.logic;

import android.util.Log;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.logic.util.ProgramLayoutParam;
import com.clearcrane.schedule.DateUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ProgramObject extends PrisonOrganism {

    private final String TAG = "ProgramObject";
    private List<ProgramLayoutParam> mRegionList;
    private List<ProgramOrgan> mSegmentList;
    private boolean isValid = false; //check the json ,if valid json ,set to true;
    private int priority;
    private int repeatation;
    private String background;

    private ProgramOrgan organ;
    public int index = 0;


    public List<ProgramLayoutParam> getmRegionList() {
        return mRegionList;
    }

    public int getPriority() {
        return priority;
    }

    public ProgramObject(List<ProgramLayoutParam> layoutList, List<ProgramOrgan> orgamList) {
        this.mRegionList = layoutList;
        this.mSegmentList = orgamList;
    }

    public ProgramObject(JSONObject programJson, int index) {
        mRegionList = new ArrayList<ProgramLayoutParam>();
        mSegmentList = new ArrayList<ProgramOrgan>();
        this.repeatation = -1;
        try {
            this.index = index;
            this.name = programJson.getString("name");
            this.priority = programJson.getInt("priority");
            this.repeatation = programJson.getInt("repeat");
            this.background = programJson.getString("background");
            String startTime = programJson.getString(ClearConstant.STR_START_TIME);
            String endTime = programJson.getString(ClearConstant.STR_END_TIME);
            this.init(startTime, endTime);
            JSONArray regions = programJson.getJSONArray("regions");
            Log.e(TAG, regions.toString());
            for (int i = 0; i < regions.length(); i++) {
                JSONObject regionJson = (JSONObject) regions.opt(i);
                ProgramLayoutParam plp = new ProgramLayoutParam(regionJson);
                mRegionList.add(plp);
            }
            JSONArray segments = programJson.getJSONArray("segments");
            for (int i = 0; i < segments.length(); i++) {
                JSONObject segmentJson = (JSONObject) segments.opt(i);
                if (index > 0) {
                    this.index = 1;
                } else {
                    this.index = 0;
                }
                ProgramOrgan organ = new ProgramOrgan(segmentJson, this.index, priority);
                mSegmentList.add(organ);
            }
            this.isValid = true;
        } catch (JSONException e) {
            Log.e(TAG, "programObject error json: " + programJson);
            this.isValid = false;
        }
    }


    @Override
    public boolean isAwake() {
        // TODO Auto-generated method stub
        Log.e(TAG, "isValid " + isValid);
//
        return isValid && isInWeekDay() && isInAwakeSegment() && isAlive();
    }

    /*
     * TODO,FIXME
     * to put this to a common place for it's a tool
     */
    private boolean isInWeekDay() {
        if (repeatation <= 0)
            return false;
        Calendar calendar = DateUtil.getCurrentTimeCalendar();
        int dow = getNumByDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK));
        Log.e(TAG, "dow " + dow + "re " + repeatation + "isture" + ((dow & repeatation) > 0));
        return (dow & repeatation) > 0;
    }

    private int getDayOfWeekByNum(int i) {
        switch (i) {
            case 1:
                return Calendar.MONDAY;
            case 2:
                return Calendar.TUESDAY;
            case 3:
                return Calendar.WEDNESDAY;
            case 4:
                return Calendar.THURSDAY;
            case 5:
                return Calendar.FRIDAY;
            case 6:
                return Calendar.SATURDAY;
            case 7:
                return Calendar.SUNDAY;
            default:
                return -1;
        }
    }

    /*
     * not 1234567
     * 1 2 4 8 16 32 64
     * for &
     */
    private int getNumByDayOfWeek(int dayofweek) {
        switch (dayofweek) {
            case Calendar.MONDAY:
                return 1;
            case Calendar.TUESDAY:
                return 2;
            case Calendar.WEDNESDAY:
                return 4;
            case Calendar.THURSDAY:
                return 8;
            case Calendar.FRIDAY:
                return 16;
            case Calendar.SATURDAY:
                return 32;
            case Calendar.SUNDAY:
                return 64;
            default:
                return 0;
        }
    }


    private boolean isInAwakeSegment() {
        int len = mSegmentList.size();
        Log.e("zxb", "isInAwakeSegment " + len);
        for (int i = 0; i < len; i++) {
            organ = mSegmentList.get(i);
            if (organ.isAlive()) {
                //设置当前正在播放的片段
                workOrgan = organ;
                return true;
            }
        }
        return false;
    }

}
