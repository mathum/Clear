package com.clearcrane.logic.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.logic.AccessTimeObject;
import com.clearcrane.logic.AccessTimeOrgan;
import com.clearcrane.schedule.DateUtil;
import com.clearcrane.view.VoDBaseView;
import com.clearcrane.vod.R;

import java.util.Calendar;
import java.util.List;

public class AccessTimeView extends VoDBaseView {

	private final String TAG = "AccessTimeView";
	private TextView tvAccessTime;
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(msg.what == 0){
			tvAccessTime.setText(msg.getData().getString("note"));
			}
		}
	};
	@Override
	public void init(Context ctx, String u) {
		// TODO Auto-generated method stub
		super.init(ctx, u);
	}

	@Override
	public void init(Context ctx, String u, String name) {
		// TODO Auto-generated method stub
		super.init(ctx, u, name);
	}

	@Override
	public void init(Context ctx) {
		// TODO Auto-generated method stub
		super.init(ctx);
		view = LayoutInflater.from(ctx).inflate(R.layout.access_time_view, null);
		initLayoutInXml();
	}
	
	private void initLayoutInXml(){
		tvAccessTime = (TextView) view.findViewById(R.id.tv_access_time);
//		setAccessTimeView();
	}
	public void destroyView(){
		handler.removeCallbacksAndMessages(null);
	}
	
	/*
	 * to set the notes
	 */
	public void setAccessTimeView(List<AccessTimeObject> accessTimeObjectList){
//		SharedPreferences sp = context.getSharedPreferences(ClearConstant.STR_ACCESS_TIME, Context.MODE_PRIVATE);
//		int count = sp.getInt(ClearConstant.STR_COUNT, 0);
		Calendar calendar = DateUtil.getCurrentTimeCalendar();
		int dow = getNumByDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK));
		List<AccessTimeOrgan> list = accessTimeObjectList.get(dow).mAccessTimeOrgarnList;
		StringBuffer sp2 = new StringBuffer();
		sp2.append(ClearConstant.STR_ACCESS_TIME_CN);
		String start = "";
		String end = "";
		for(int i = 0; i < list.size() ; i++){
			start = list.get(i).dayTimeStart;
			end = list.get(i).dayTimeEnd;
			sp2.append("\r\n");
			sp2.append(start);
			sp2.append("~~");
			sp2.append(end);
		}
		String note = sp2.toString();
		Message msg = new Message();
		
		msg.what = 0;
		Bundle bundle = new Bundle();
		bundle.putString("note", note);
		msg.setData(bundle);
		handler.sendMessage(msg);
	}
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
