package com.clearcrane.logic;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.logic.state.AccessTimeState;
import com.clearcrane.logic.state.ChannelState;
import com.clearcrane.logic.state.InterCutState;
import com.clearcrane.logic.state.PrisonBaseModeState;
import com.clearcrane.logic.state.ScrollTextState;
import com.clearcrane.logic.state.StateInterCutState;
import com.clearcrane.logic.state.VodState;
import com.clearcrane.logic.version.VersionChangeListener;
import com.clearcrane.view.VoDViewManager;


public class PrisonLogicManager {

    private final int CHECK_STATE_TIME = 2000;
    private final String TAG = "PrisonLogicManager";
    private Context mContext;
    private PrisonBaseModeState curModeState;
    private Handler mHandler;
    private VodState mVodState;
    private ScrollTextState mScrollTextState;
    private InterCutState mInterCutState;
    private StateInterCutState mStateInterCutState;
    private ChannelState mChannelState;
    private AccessTimeState mAccessTimeState;
    private boolean isRunning = false;

    public PrisonLogicManager(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        initStates();
    }

    /*
     * TODO,FIXME
     * 1.nullpoint check
     * 2.other kinds of state
     *
     */
    public VersionChangeListener getVersionChangeListenerByTypeName(String typeName) {
        if (typeName.equals(ClearConstant.STR_STATE_INTER_CUT)) {
            return mStateInterCutState.getVersionChangeListener();
        } else if (typeName.equals(ClearConstant.STR_SCROLL_TEXT)) {
            return mScrollTextState.getVersionChangeListener();
        } else if (typeName.equals(ClearConstant.STR_INTER_CUT)) {
            return mInterCutState.getVersionChangeListener();
        } else if (typeName.equals(ClearConstant.STR_CHANNEL)) {
            return mChannelState.getVersionChangeListener();
        } else if (typeName.equals(ClearConstant.STR_ACCESS_TIME)) {
            return mAccessTimeState.getVersionChangeListener();
        } else
            return null;
    }

    /*
     * init all kinds of states;
     *
     */
    private void initStates() {
        mScrollTextState = new ScrollTextState();
        mScrollTextState.init(mContext);

        mInterCutState = new InterCutState();
        mInterCutState.init(mContext, mHandler);

        mVodState = new VodState();
        mVodState.init(mContext, null);

        mChannelState = new ChannelState();
        mChannelState.init(mContext, mHandler);

        mAccessTimeState = new AccessTimeState();
        mAccessTimeState.init(mContext, mHandler);

        mStateInterCutState = new StateInterCutState();
        mStateInterCutState.init(mContext, mHandler);

        curModeState = mVodState;
    }


    public void startCheckThread() {
        if (isRunning) {
            return;
        }
        isRunning = true;
        new Thread() {
            public void run() {
                while (isRunning) {
                    try {
                        Thread.sleep(CHECK_STATE_TIME);
                        checkReadyState();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }.start();

    }

    public void stopCheckThread() {
        isRunning = false;
    }

    private void checkReadyState() {
        if (VoDViewManager.getInstance().getActivityMode() == ClearConstant.CODE_TERM_FORCED_STATE) {
//			curModeState = mVodState;
            return;
        }
        PrisonBaseModeState tempState = mScrollTextState;
        if (mAccessTimeState.isReady()) {
            tempState = mAccessTimeState;
        } else if (mStateInterCutState.isReady()) {
            Log.e(TAG, "checkReadyState mStateInterCutState : " + true);
            tempState = mStateInterCutState;
        } else if (mInterCutState.isReady()) {
            Log.e("xb", "mintercutstate");
            tempState = mInterCutState;
        } else if (mChannelState.isReady()) {
            Log.e(TAG, "checkReadyState channelState : " + true);
            tempState = mChannelState;
        } else {
            tempState = mVodState;
        }

        if (curModeState != tempState) {
            changeModeState(tempState);
        } else if (!curModeState.isPlaying()) {
            Message msg = mHandler.obtainMessage(ClearConstant.MSG_START_CHANNEL);
            msg.obj = curModeState;
            msg.sendToTarget();
        }

        if (mScrollTextState.isReady()) {
            mScrollTextState.startPlay();
            Log.i("l", "mScrollTextState");
        } else {
            mScrollTextState.stopPlay();
        }

        Log.i(TAG, "curModeState name is " + curModeState.getClass().getName());
        Log.i(TAG, "curModeState version is " + curModeState.getCurVersion());
    }

    private void changeModeState(PrisonBaseModeState state) {
        Log.e("zxb", "改变模式了已经");
        if (curModeState.isPlaying()) {
            Message msg = mHandler.obtainMessage(ClearConstant.MSG_STOP_CHANNEL);
            msg.obj = curModeState;
            msg.sendToTarget();
        }
        if (state instanceof StateInterCutState) {
            if (curModeState instanceof ChannelState && curModeState.isPlaying()) {
                Message msg = mHandler.obtainMessage(ClearConstant.MSG_STOP_CHANNEL);
                msg.obj = curModeState;
                msg.sendToTarget();
            }
            if (curModeState instanceof InterCutState && curModeState.isPlaying()) {
                Message msg = mHandler.obtainMessage(ClearConstant.MSG_STOP_INTER_CUT);
                msg.obj = curModeState;
                msg.sendToTarget();
            }
        }
        curModeState = state;
        Message msg = mHandler.obtainMessage(ClearConstant.MSG_START_CHANNEL);
        msg.obj = curModeState;
        /*
         * TODO,FIXME
         * just 1 second delay to prevent something!!
         */
        mHandler.sendMessageDelayed(msg, 1000);
//		curModeState.startPlay();
    }


}
