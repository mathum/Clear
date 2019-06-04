package com.clearcrane.view;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.clearcrane.activity.ClearApplication;
import com.clearcrane.constant.clearKey;
import com.clearcrane.util.PlatformSettings;

/* used for foreground view */
public abstract class VoDBaseView {
	protected View view = null;
	protected Context context;
	protected String url; //json address or movie address
	protected boolean isInserted = false ;
	protected String type = null;
	protected LinearLayout menuLinearLayout=null;
	protected FrameLayout pageLayout=null; //页面布局
	protected String nameInIcon = "";
	private String TAG = "vodbaseview";
	protected boolean isImsView = false;
	protected ClearApplication mApp;
	 
	public boolean getIsImsView(){
		return this.isImsView;
	}
	/* data provider */
	/* build the view layout/element */
	/* start animation */
	
	public void init(Context ctx) {
	    mApp = (ClearApplication) ctx.getApplicationContext();
		context = ctx;
	}
	
	public void init(Context ctx, String u) {
	    mApp = (ClearApplication) ctx.getApplicationContext();
		context = ctx;
		url = u;
	}
	
	public void init(Context ctx, String u, LinearLayout layout) {
		context = ctx;
		url = u;
		menuLinearLayout = layout;
	}
	
	public void init(Context ctx, String u, FrameLayout layout) {
		context = ctx;
		url = u;
		pageLayout = layout;
	}
	
	public void init(Context ctx, String u, String name) {
		context = ctx;
		url = u;
	}
	
	public void init(Context ctx, String u, String name, Boolean inserted) {
		context = ctx;
		url = u;
	}
	
	public void init(Context ctx, String u, String name, Boolean inserted,long time) {
		context = ctx;
		url = u;
	}
	public void init(Context ctx, String u, String b, String c) {
		context = ctx;
		url = u;
	}
	
	public void setName(String n) {
		nameInIcon = n;
	}
	
	/* key event control */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.i("base view keycode:"," "+keyCode);
		
		switch(keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			return onKeyDpadLeft();
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			Log.i("in video key","baseview sonkeydownright");
			return onKeyDpadRight();
		case KeyEvent.KEYCODE_DPAD_UP:
			return onKeyDpadUp();
		case KeyEvent.KEYCODE_DPAD_DOWN:
			return onKeyDpadDown();
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
			return onKeyEnter();
		case KeyEvent.KEYCODE_BACK:
			return onKeyBack();
		case KeyEvent.KEYCODE_FUNCTION://key home
			return onKeyBack();
		case KeyEvent.KEYCODE_MENU:
			if(PlatformSettings.getPlatform().equals(PlatformSettings.Platform.philips)){
				//philips retunr home
				return onKeyHome();
			}
			return onKeyMenu();
		case clearKey.PHILIPS_OPTINOS:
			return onKeyMenu();
		case KeyEvent.KEYCODE_SETTINGS:
			return onKeySettings();
		case KeyEvent.KEYCODE_F2://key 鼠标
		case KeyEvent.KEYCODE_F1://key 输出
			return true;
		case KeyEvent.KEYCODE_HOME:
		case KeyEvent.KEYCODE_F5:
		case clearKey.PHILIPS_RED_KEY:
			return onKeyHome();
		//屏蔽创维键值
		case clearKey.SKYWORTH_SETTING:
		case clearKey.SKYWORTH_SEARCH:
		case clearKey.SKYWORTH_LOCAL_MEDIA:
		case clearKey.SKYWORTH_TIME_SPOT:
	    	return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_VOLUME_MUTE:
			return false;
		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
			return onKeyDpadMediaPlayPause();
		case KeyEvent.KEYCODE_PAGE_UP:
			return onKeyDpadPageUp();
		case KeyEvent.KEYCODE_PAGE_DOWN:
			return onKeyDpadPageDown();
		//这两个键值是为了适配tcl电视   tcl遥控器没有翻页键  只有用音乐的上一曲和下一曲按键替代
		case KeyEvent.KEYCODE_MEDIA_NEXT:
			return onKeyDpadPageDown();
		case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
			return onKeyDpadPageUp();
		case KeyEvent.KEYCODE_TV:
			return onKeyTV();
		default:
			break;
		}
		return false;
	}
	
	public  boolean onKeyDpadMediaPlayPause() {
		return false;
	}

	public boolean onKeyDpadPageUp() {
		return false;
	}
	
	public boolean onKeyDpadPageDown() {
		return false;
	}
	
	public boolean onKeyTV(){
		if(!VoDViewManager.getInstance().isInLiveView && VoDViewManager.getInstance().isTHJstyle)
			VoDViewManager.getInstance().showLiveView();
		return true;
	}
	public boolean onKeyDpadLeft() {
		return false;
	}
	
	public boolean onKeyDpadRight() {
		Log.i("in video key","baseview right");
		return false;
	}
	
	public boolean onKeyDpadUp() {
		return false;
	}
	
	public boolean onKeyDpadDown() {
		return false;
	}
	
	public boolean onKeyEnter() {
		return false;
	}
	
	public boolean onKeyBack() {
		VoDViewManager.getInstance().popForegroundView();
		return true;

	}
	
	public boolean onKeyHome() {
		Log.i("key","home key");
		if(VoDViewManager.getInstance().isTHJstyle)
			VoDViewManager.getInstance().showLanguageView();
		return true;
	}
	
	public boolean onKeyMenu() {
		if(VoDViewManager.getInstance().isTHJstyle)
			VoDViewManager.getInstance().showMenuView();
		return true;
	}
	
	public boolean onKeySettings() {
		return false;
	}
	
	/* touch event control */
	
	
	/* show control */
	public View getView() {
		return view;
	}
	
	public void hide() {
		if(view != null) {
			view.setVisibility(View.GONE);
		}
	}

	public void show() {
		if(view != null) {
			view.setVisibility(View.VISIBLE);
		}
	}
	
	public void back(){
		
	}

	public void turnOverTv(String Keys){
		
	}
}
