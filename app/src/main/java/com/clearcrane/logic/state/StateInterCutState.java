package com.clearcrane.logic.state;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.logic.InterCutObject;
import com.clearcrane.logic.InterCutOrgan;
import com.clearcrane.logic.StateInterCutObject;
import com.clearcrane.logic.StateInterCutOrgan;
import com.clearcrane.view.VoDViewManager;

public class StateInterCutState extends PrisonBaseModeState {

    String sourcrUrl = "";
    String startTime = "";
    String endTime = "";
    String interCutTitle = "";
    String interCutType = "";
    String songName = "";
    String duration = "";
    SharedPreferences forced_sharedPreferences;

    public StateInterCutState() {
        this.mStateCode = ClearConstant.CODE_INTER_CUT_STATE;
    }

    @Override
    protected void updateVersionInfo() {
        // TODO Auto-generated method stub
        Log.i(TAG, "updateVersionInfo!");
        reinit();
    }

    @Override
    public void startPlay() {
        super.startPlay();
        Log.e("xb2", interCutType);
        if (interCutType.equals("audio")) {
            Message msg = mHandler.obtainMessage(ClearConstant.MSG_INTERRUPT_MUSIC_START);
            msg.obj = mPrisonOrganism.getWorkOrgan();
            msg.sendToTarget();
        } else if (interCutType.equals("picture")) {
            Log.e("xb", "picture：startplay");
            Message msg = mHandler.obtainMessage(ClearConstant.MSG_INTERRUPT_PICTURE_START);
            msg.obj = mPrisonOrganism.getWorkOrgan();
            msg.sendToTarget();
        } else {
            Log.e("xb", "intercut startplay");
            Message msg = mHandler.obtainMessage(ClearConstant.MSG_START_STATE_INTER_CUT);
            msg.obj = mPrisonOrganism.getWorkOrgan();
            msg.sendToTarget();
        }
        mPrisonOrganism.setWorking(true);
        /*
         * to set activiy mode
         */
        VoDViewManager.getInstance().setActivityMode(mStateCode);
    }

    @Override
    public void stopPlay() {
        // TODO Auto-generated method stub
        super.stopPlay();
        if (interCutType.equals("audio")) {
            Message msg = mHandler.obtainMessage(ClearConstant.MSG_INTERRUPT_MUSIC_STOP);
            msg.sendToTarget();
        } else if (interCutType.equals("picture")) {
            Message msg = mHandler.obtainMessage(ClearConstant.MSG_INTERRUPT_PICTURE_STOP);
            msg.sendToTarget();
        } else {
            mHandler.obtainMessage(ClearConstant.MSG_STOP_STATE_INTER_CUT).sendToTarget();
            mPrisonOrganism.setWorking(false);
        }
        VoDViewManager.getInstance().setActivityMode(ClearConstant.CODE_VOD_STATE);
    }

    /*
     * TODO,FIXME
     */
    @Override
    public void init(Context context) {
        init(context, null);
    }

    /*
     * TODO,FIXME
     */
    @Override
    public void init(Context context, Handler handler) {
        // TODO Auto-generated method stub
        super.init(context, handler);
        if (forced_sharedPreferences == null) {
            forced_sharedPreferences = mContext.getSharedPreferences("is_forced", Context.MODE_PRIVATE);
        }
        if (forced_sharedPreferences.getInt("is_forced", 0) == 2) {
            return;
        }
        mPreference = mContext.getSharedPreferences(ClearConstant.STR_STATE_INTER_CUT, Context.MODE_PRIVATE);
        initStateParams();
        initPrisonOrganism();
    }

    @Override
    public void initStateParams() {
        int version = mPreference.getInt(ClearConstant.STR_NEWEST_VERSION, -1);
        if (version == -1) {
            Log.i(TAG, "version is -1!!");
            startTime = "2016-5-12 13:00:00";
            endTime = "2016-5-12 16:00:00";
        } else {
            sourcrUrl = mPreference.getString(ClearConstant.STR_SOURCE_URL, "");
            startTime = mPreference.getString(ClearConstant.STR_START_TIME, "");
            endTime = mPreference.getString(ClearConstant.STR_END_TIME, "");
            //interCutTitle = mPreference.getString(ClearConstant.STR_TITLE, "");
            //interCutType = mPreference.getString(ClearConstant.STR_TYPE, "");
//			if(interCutType.equals("audio")){
//				songName = mPreference.getString(ClearConstant.STR_MATERIAL_NAME, "未知");
//				duration = mPreference.getString(ClearConstant.STR_DURATION, "未知");
//			}
        }
        setCurVersion(version);
        return;
    }

    @Override
    public void initPrisonOrganism() {
        StateInterCutOrgan organ = new StateInterCutOrgan(sourcrUrl, interCutType, interCutTitle);
        if (interCutType.equals("audio")) {
            organ = new StateInterCutOrgan(sourcrUrl, songName, duration);
        }
        organ.init(startTime, endTime);
        mPrisonOrganism = new StateInterCutObject(organ);
        mPrisonOrganism.init(startTime, endTime);
    }

    @Override
    public boolean isReady() {
        Log.e("111111111111111", "isAwake = " + mPrisonOrganism.isAwake());
        return mPrisonOrganism.isAwake();
    }

}
