package com.clearcrane.logic.view;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.IBinder;
import android.text.method.SingleLineTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;

import com.clearcrane.constant.ClearConstant;

public class ScrollTextView extends Service{
	
	private final String TAG  = "ScrollTextView";
	
	
	MarqueeTextView mTextView;
	private int mFontSize = 28;
	private int mFontColor = 0xFFF0F0F0;
	private int mBgColor = Color.BLACK;
	private int mBgAlpha = 120;
	
	private WindowManager windowManager;
	private String mTextContent = "";
	private String color="";
	private String interval=""; 
	private String location =""; 
	private String font_family = "";
	private String direction = "";

	public void setTextContent(String textContent) {
		this.mTextContent = textContent;
	}


	private int curVersion = -1;
	
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		readTextContent();
		createTextView();
		
	}
	
	
	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		if (intent == null || intent.getAction() == null) {
			Log.d(TAG, "no action");
			return;
		}
		
		if (intent.getAction().equals("start")){
			startScroll();
		}else{
			stopScroll();
			stopSelf();
		}
	}
	
	
	
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		stopScroll();
		if( windowManager != null){
			windowManager.removeView(mTextView);
			mTextView = null;
			windowManager = null;
		}
	}

	private void startScroll(){
		Log.i(TAG,"scrolltext version is :" + curVersion);
		if(curVersion == -1){
			stopSelf();
		}else{
			mTextView.setText(mTextContent);
			mTextView.init(windowManager,color,interval,direction);
			mTextView.startScroll();
		}
	}
	
	private void stopScroll(){
		if(mTextView != null)
		mTextView.stopScroll();
	}

	private void readTextContent(){
		SharedPreferences mPreferences = getSharedPreferences(ClearConstant.STR_SCROLL_TEXT, Context.MODE_PRIVATE);
		mTextContent = mPreferences.getString(ClearConstant.STR_CONTENT, "welcome!!");
		color = mPreferences.getString(ClearConstant.STR_COLOR, "");
		direction = mPreferences.getString(ClearConstant.STR_TYPE_DIRECTION, "");
		location = mPreferences.getString(ClearConstant.STR_LOCATION, "");
		interval = mPreferences.getString(ClearConstant.STR_INTERVAL, "");
		font_family = mPreferences.getString(ClearConstant.STR_FONT_FAMILY, "28");
		mFontSize = Integer.parseInt(mPreferences.getString(ClearConstant.STR_FONT_SIZE,"28"));
		curVersion = mPreferences.getInt(ClearConstant.STR_NEWEST_VERSION, -1);
	}
	
	
	@SuppressWarnings("deprecation")
	private void createTextView(){
		mTextView = new MarqueeTextView(this);
		
		mTextView.setTextSize(mFontSize);
//		mTextView.setTextColor(Color.parseColor(color.toString()));
		mTextView.setBackgroundColor(mBgColor);
		mTextView.setTransformationMethod(SingleLineTransformationMethod.getInstance());
		Typeface font = null;
		if(font_family.equals("sans")){
		font = Typeface.create(Typeface.SANS_SERIF,Typeface.BOLD);
		}else if(font_family.equals("serif")){
		font = Typeface.create(Typeface.SERIF,Typeface.BOLD);
		}else{
		font = 	Typeface.create(Typeface.MONOSPACE,Typeface.BOLD);
		}
		mTextView.setTypeface(font);
		mTextView.getBackground().setAlpha(mBgAlpha);
//		mTextView.setSingleLine();
		mTextView.setGravity(Gravity.CENTER);
		mTextView.setPadding(0, mFontSize/3, 0, 0);
		
		DisplayMetrics metrics = new DisplayMetrics();
		windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(metrics);
		
		WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
						| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
				PixelFormat.TRANSLUCENT);
		
		if (location.equals("up")) {			
			params.gravity = Gravity.TOP;
		}else{
			params.gravity = Gravity.BOTTOM;
		}
		//左右两侧由于对英文数字支持不友好，暂时不支持
//		params.gravity = Gravity.LEFT;
	 //winter on 2017/12/06	
     // 如果忽略gravity属性，那么它表示窗口的绝对Y位置。
	 // 当设置了 Gravity.TOP 或 Gravity.BOTTOM 之后，y值就表示到特定边的距离。
		params.y = 10;
		// set screen size
		params.width = windowManager.getDefaultDisplay().getWidth();
		/*
		 * TODO,FIXME,just for singleline
		 * if we set singleline 
		 * the background will disappear
		 * 手动控制marqueeTextView高度为单行。
		 */
		params.height = windowManager.getDefaultDisplay().getHeight()/(504/mFontSize);
		
//		params.width = windowManager.getDefaultDisplay().getWidth()/25;
//		params.height = windowManager.getDefaultDisplay().getHeight();
		
		params.setTitle("TopTextView");
//		mTextView.setLayoutParams(params);
		windowManager.addView(mTextView, params);
	}
	
	

}
