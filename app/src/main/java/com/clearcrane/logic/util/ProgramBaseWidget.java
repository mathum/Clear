package com.clearcrane.logic.util;

import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;

import com.clearcrane.schedule.DateUtil;
import com.clearcrane.util.ClearConfig;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * TODO,FIXME
 * how to match the regionid
 */

public abstract class ProgramBaseWidget {
	
	protected String TAG = getClass().getSimpleName().toString();
	protected ProgramLayoutParam mParam;
	protected Context mContext;
	protected ArrayList<ProgramResource> mResourceList;
	protected int mRegionId;
	protected int marginLeft;
	protected int widgetWidth;
	protected int widgetHeight;
	protected int marginTop;
	protected int curPlayIndex = 0;
	protected int duration; //auto next time
	
	protected FrameLayout mProgramLayout;
	public String startSecond;
	public String endSecond;
	private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
	
	/**
	 * 计算已经过去的时间（秒）
	 * @return 秒
	 */
	public long getElapsedMillSeconds(){
		Date curDate = new Date(DateUtil.getCurrentTimeMillSecond());
		String currentTime = formatter.format(curDate);
		return  DateUtil.getElapsedSeconds(startSecond,currentTime) * 1000;
	}
	
	public int getmRegionId() {
		return mRegionId;
	}


	public ProgramBaseWidget(){
		this.mRegionId = -1;
	}
	

	public void init(Context ctx, ProgramLayoutParam param, FrameLayout layout, String start, String end){
		this.mContext = ctx;
		this.mParam = param;
		this.mProgramLayout = layout;
		startSecond = start;
		endSecond = end;
//		mResourceList = new ArrayList<>();
		initTopLeftWidthHeight();
		initView();
	}
	
	/**
	 * TODO,FIXME
	 * to init the true Top Left Width Height
	 */
	private void initTopLeftWidthHeight(){
		if(mParam == null){
			marginLeft = 0;
			marginTop = 0;
			widgetHeight = 0;
			widgetWidth = 0;
			duration = 10;
			return;
		}
		float screenHeightScale = ClearConfig.getScreenHeight()/1080.0f;
		float screenWidthScale = ClearConfig.getScreenWidth()/1920.0f;
//		float screenHeightScale = 0.5f;
//		float screenWidthScale = 0.5f;
		marginLeft = (int) (mParam.left * screenWidthScale);
		widgetWidth = (int) (mParam.width * screenWidthScale);
		marginTop = (int) (mParam.top * screenHeightScale);
		widgetHeight = (int) (mParam.height * screenHeightScale);
		Log.e("xb","initTopLeftWidthHeight " + marginLeft + " " + widgetWidth);
		mRegionId = mParam.layoutParamId;
		duration = 10;
		
	}
	
	
	public void reinit(){
		mResourceList.clear();
	}
	
	public void addResource(ProgramResource resource){
		if(mResourceList == null){
			mResourceList = new ArrayList<>();
		}
		mResourceList.add(resource);
	}
	
	abstract public void addWorkResource(ProgramResource resource);
	
	abstract public void initView();
	
	abstract public void play();
	
	abstract public void stop();
	
	abstract public int getTypeId();
	
}
