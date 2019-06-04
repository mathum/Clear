package com.clearcrane.logic.state;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.logic.AccessTimeObject;
import com.clearcrane.logic.AccessTimeOrgan;
import com.clearcrane.logic.view.AccessTimeView;
import com.clearcrane.view.VoDViewManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

public class AccessTimeState extends PrisonBaseModeState {
	// private String start_time;
	// private String end_time;
	private List<AccessTimeObject> accessTimeObjectList; // 保存一星期中所有的可用时间对象
	private AccessTimeView atv;

	public AccessTimeState() {
		this.mStateCode = ClearConstant.CODE_ACCESS_TIME_STATE;
		this.accessTimeObjectList = new ArrayList<>();
	}

	@Override
	protected void updateVersionInfo() {
		// TODO Auto-generated method stub
		Log.i(TAG, "updateVersionInfo");
		reinit();
	}

	@Override
	public void init(Context context) {
		// TODO Auto-generated method stub
		this.init(context, null);
	}

	@Override
	public void init(Context context, Handler handler) {
		// TODO Auto-generated method stub
		super.init(context, handler);
		mPreference = mContext.getSharedPreferences(ClearConstant.STR_ACCESS_TIME, Context.MODE_PRIVATE);
		initStateParams();
		initPrisonOrganism();
	}

	@Override
	public void initPrisonOrganism() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clearcrane.logic.state.PrisonBaseModeState#initStateParams()
	 * modify on 2016/12/28 by ll
	 */
	@Override
	public void initStateParams() {
		// TODO Auto-generated method stub
		Log.e("xb", "accesstimestate init");
		curVersion = mPreference.getInt(ClearConstant.STR_NEWEST_VERSION, -1);
		if (curVersion == -1) {
			return;
		} else {
			accessTimeObjectList.clear();
			String result = mPreference.getString(ClearConstant.STR_ACCESSTIME_CONTENT, "");
			// 与后台约定好是7个对象，分别对应一个星期的七天。
			JSONTokener jsonTokener = new JSONTokener(result);
			JSONObject jsonObject;
			try {
				jsonObject = (JSONObject) jsonTokener.nextValue();
				// JSONObject accessTimeJsonObject =
				// (JSONObject)jsonObject.getJSONObject(ClearConstant.STR_ACCESS_TIME);
				JSONArray ja = jsonObject.getJSONArray(ClearConstant.STR_ACCESS_TIME);// 获取JSONArray  
				for (int i = 0; i < 8; i++) {
					JSONArray atObj = (JSONArray) ja.get(i);

					AccessTimeObject accessTimeObject = new AccessTimeObject(i);
					Log.e("xb", "atobj length:" + atObj.length());
					if (atObj.length() == 0) {
						accessTimeObjectList.add(accessTimeObject);
						continue;
					}
					for (int j = 0; j < atObj.length(); j++) {
						JSONObject jb = (JSONObject) atObj.opt(j);
						AccessTimeOrgan accessTimeOrgan = new AccessTimeOrgan();
						Log.e("xb", "starttime:" + jb.getString("s") + jb.getString("e"));
						accessTimeOrgan.init(jb.getString("s"), jb.getString("e"));
						accessTimeObject.mAccessTimeOrgarnList.add(accessTimeOrgan);
					}
					accessTimeObjectList.add(accessTimeObject);
				}
				Log.e("xb", "accessTimeObjectList:" + accessTimeObjectList.size());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// int count = mPreference.getInt(ClearConstant.STR_COUNT, 0);
			// ArrayList<AccessTimeOrgan> list = new ArrayList<>();
			// for (int i = 0; i < count ; i++){
			// start_time = mPreference.getString(ClearConstant.STR_START_TIME +
			// i, "00:00");
			// end_time = mPreference.getString(ClearConstant.STR_END_TIME + i,
			// "23:59");
			// AccessTimeOrgan organ = new AccessTimeOrgan();
			// organ.init(start_time,end_time);
			// list.add(organ);
			// }
			// mPrisonOrganism = new AccessTimeObject(list);

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clearcrane.logic.state.PrisonBaseModeState#isReady() TODO,FIXME
	 * access time when false show ,true not show
	 * 
	 */
	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		if (curVersion == -1) {
			return false;
		}
		for (int i = 0; i < accessTimeObjectList.size(); i++) {
			AccessTimeObject ao = accessTimeObjectList.get(i);
			Log.e("zxb", "ao.mAccessTimeOrgarnList.size():" + ao.mAccessTimeOrgarnList.size());
			if (ao.mAccessTimeOrgarnList.size() == 0) {
				Log.e("zxb", "本日没有可用时间计划");
			} else {
				Log.e("zxb", "在一次循环中会走一次");
				if (ao.isAwake()) {
					if (atv != null) {
						atv.setAccessTimeView(accessTimeObjectList);
					}
					// mPrisonOrganism = ao;
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clearcrane.logic.state.PrisonBaseModeState#startPlay() this is
	 * for Mao Rui Only this project the terminal is Philips TV
	 * 
	 */
	@Override
	public void startPlay() {
		// TODO Auto-generated method stub
		super.startPlay();
		showAccessView();
		VoDViewManager.getInstance().setActivityMode(mStateCode);
	}

	private void showAccessView() {
		atv = new AccessTimeView();
		atv.init(mContext);
		atv.setAccessTimeView(accessTimeObjectList);
		Message msg = mHandler.obtainMessage(ClearConstant.MSG_START_ACCESS_TIME);
		msg.obj = atv;
		msg.sendToTarget();
	}

	private void hideAccessView() {
		atv.destroyView();
		atv = null;
		mHandler.obtainMessage(ClearConstant.MSG_STOP_ACCESS_TIME).sendToTarget();
	}

	@Override
	public void stopPlay() {
		// TODO Auto-generated method stub
		super.stopPlay();
		hideAccessView();
		VoDViewManager.getInstance().setActivityMode(ClearConstant.CODE_VOD_STATE);
	}
}
