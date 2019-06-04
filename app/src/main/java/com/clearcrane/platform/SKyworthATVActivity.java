package com.clearcrane.platform;

import android.app.Activity;
import android.app.TvManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.clearcrane.constant.clearProject;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.view.VoDViewManager;
import com.clearcrane.vod.R;

import java.util.Date;

public class SKyworthATVActivity extends Activity {
	private static final String TV_SERVICE = "tv";
	private SurfaceView sf = null;
	private TextView tv = null;
	private TvManager mTvManager = null;
	private int index;
	private Handler mHandler = null;//数字键按下等待两秒，异步处理
	private Counter counter = null;//超时执行操作
	private boolean next = false;
	private TitleRunnable mTitleRunnable = null;
	private int lastKeyCode = -1;
	private long lastKeyeventTime = 0; //millisecond 10-6
	private KeyIgnoreWaiter ignoreKey = null;
	private int maxIndex = 0;
	private boolean pressExit= false;
	private String TAG = "skyworthatv";
	
	private int source_type = TvManager.SOURCE_ATV1;
	
	private SharedPreferences mPrefs = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_autotv);
		
		source_type = TvManager.SOURCE_ATV1;
		Intent intent = getIntent();
		String intent_source_type = intent.getStringExtra("source");  
		if(intent_source_type != null && intent_source_type.compareTo("DTV") == 0) {
			source_type = TvManager.SOURCE_DTV1;
		}
		
		Log.d("clear", "source: " + source_type);
		
		//strict mode
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
	    .penaltyLog()
	    .build());
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		.penaltyLog()
	    .build());
		
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		mTvManager = (TvManager)this.getSystemService(TV_SERVICE);
		tv = (TextView)findViewById(R.id.autotv);
		tv.setTextSize(150);
		tv.setTextColor(Color.GREEN);
		
		sf = (SurfaceView)findViewById(R.id.autosf);
		
		sf.getHolder().setFormat(PixelFormat.TRANSPARENT);
		sf.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		mHandler = new Handler();
		counter = new Counter();
		mTitleRunnable = new TitleRunnable();
		
		mTvManager.setVideoSize(0,0,
				ClearConfig.getScreenWidth(),
				ClearConfig.getScreenHeight());
		new SourceRunnable(source_type).start();
		
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event){
		Log.d("autoatv","key up"+keyCode);
		int tmp_lastKeyCode = lastKeyCode;
		lastKeyCode = keyCode;
		
		long tmp_lastKeyeventTime = lastKeyeventTime;
		lastKeyeventTime = new Date().getTime();
		long curTime = lastKeyeventTime;
		
		mHandler.removeCallbacks(ignoreKey);
		
		if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == 166) {
//			removeCounter();
//			startNewTitleRunnable();
			new MoveCounter().start();
			new StartNewTitle().start();
			
			if(keyCode == tmp_lastKeyCode && curTime - tmp_lastKeyeventTime < 1000000)
			{
				ignoreKey = new KeyIgnoreWaiter();
				mHandler.postDelayed(ignoreKey, 2000);
			}
			else
			{
				new PlayChannelRunnable().start();
			}
			return true;
		}
		else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == 167) {
//			removeCounter();
//			startNewTitleRunnable();

			new MoveCounter().start();
			new StartNewTitle().start();
			
			if(keyCode == tmp_lastKeyCode && curTime - tmp_lastKeyeventTime < 1000000)
			{
				ignoreKey = new KeyIgnoreWaiter();
				mHandler.postDelayed(ignoreKey, 2000);
			}
			else
			{
				new PlayChannelRunnable().start();
			}
			return true;
		}
		else if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
			mHandler.removeCallbacks(mTitleRunnable);
			//连续按下数字键，认为是组合数字
			//释放掉前面一个 计时器对象，启动一个新的额计时器，等待2s
			if(next)
			{
				mHandler.removeCallbacks(counter);
				counter = new Counter();
				mHandler.postDelayed(counter, 2000);
				index = index*10 + keyCode - 7;
			}
			//不是连续按键
			else
			{
				mHandler.postDelayed(counter, 2000);
				index = keyCode - 7;
			}
			next  = true;
			if(index < 0 )
				index = 0;
			if(index > maxIndex - 1)
				index = maxIndex - 1;
			if(maxIndex <= 0)
				index = 0;
			setChannel();
			return true;
		}
		else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
		{
			next = false;
			removeCounter();
			return super.onKeyUp(keyCode, event);
		}
		else if (keyCode == 164) {
			next = false;
			removeCounter();
			return super.onKeyUp(keyCode, event);
		}
		/*else if(keyCode == KeyEvent.KEYCODE_ALTERNATE){
			next = false;
			removeCounter();
			mTvManager.playHistoryChannel();
			index = mTvManager.getCurChannelIndex();
			//台标开始计时
			setChannel();
			startNewTitleRunnable();
		}*/
		//else if(keyCode == KeyEvent.KEYCODE_BACK||keyCode == KeyEvent.KEYCODE_SHARE)
		else if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			if(!pressExit)
			{
				pressExit = true;
				new SourceRunnable(TvManager.SOURCE_PLAYBACK).start();
				removeCounter();
				finish();
			}
		}
		return true;
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		Log.d("autoatv","key down"+keyCode);
		//mHandler.removeCallbacks(ignoreKey);
		
		if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == 166) {
			//removeCounter();

			new MoveCounter().start();
			
			index++;
			if(index > maxIndex - 1){
				index = 0;
			}
			setChannel();
			mHandler.removeCallbacks(mTitleRunnable);
			return true;
		}
		else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == 167) {
			//removeCounter();
			new MoveCounter().start();
			
			index--;
			if(index < 0){
				index = maxIndex - 1;
			}
			if(index < 0)
				index = 0;
			setChannel();
			mHandler.removeCallbacks(mTitleRunnable);
			return true;
		}
		//*************************重庆潮曼酒店项目，断网后不能响应音量键********************
		else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE){
			if(VoDViewManager.getInstance().project_name.contains(clearProject.Zmax)){
				return clearProject.zmaxVolume(keyCode,this);
			}else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
			{
				next = false;
				removeCounter();
				return super.onKeyDown(keyCode, event);
			}
		}
		else if(keyCode == KeyEvent.KEYCODE_BACK)
		{
//			new SourceRunnable(TvManager.SOURCE_PLAYBACK).start();
//			removeCounter();
//			finish();
		}
		else if (keyCode == 164) {
			next = false;
			removeCounter();
			return super.onKeyDown(keyCode, event);
		}
		return true;
	}
	/**
	 * 计时结束或者在2s时间内按下了其他的按键，去掉任务
	 */
	/**
	 * 启动一个新的台标计时器
	 */
	private void startNewTitleRunnable(){
		mHandler.removeCallbacks(mTitleRunnable);
		mTitleRunnable = new TitleRunnable();
		mHandler.postDelayed(mTitleRunnable, 2000);
	}
	private void removeCounter(){
		next = false;
		mHandler.removeCallbacks(counter);
	}

	private void setChannel(){
		//final String str = "                                            "+index;//6
		final String str = "" + index;
		tv.setText(str);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	/**
	 * 上下键连续按键等待超时，时间到后，跳到当前按下的频道播放
	 * @author caowei
	 *
	 */
	class KeyIgnoreWaiter implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			mTvManager.playChannelByNum(index);
			lastKeyCode = -1;
		}
		
	}
	
	/**
	 * 按键等待超时，时间到后，跳到当前按下的频道播放
	 * @author caowei
	 *
	 */
	class Counter implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			setChannel();
			mTvManager.playChannelByNum(index);
			removeCounter();
			//台标开始计时
			startNewTitleRunnable();
		}
		
	}
	/**
	 * 台标显示2.5s，然后消失
	 * @author caowei
	 *
	 */
	class TitleRunnable implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			tv.setText("");
			mHandler.removeCallbacks(mTitleRunnable);
		}
	}
	/**
	 * 播放频道，子线程处理，为了不阻塞主线程
	 * @author caowei
	 *
	 */
	class PlayChannelRunnable extends Thread{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			mTvManager.playChannelByNum(index);
			lastKeyCode = -1;
		}
	}
	/**
	 * 子线程，切换通道
	 * @author caowei
	 *s
	 */
	class SourceRunnable extends Thread{
		public int type;
		
		public SourceRunnable(int t)
		{
			type = t;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			mTvManager.setSource(type);
			index = mTvManager.getCurChannelIndex();
			tv.post(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					setChannel();
				}
				
			});
			startNewTitleRunnable();
			//maxIndex = mTvManager.tvScanInfo(TvManager.SCAN_INFO_CHANNEL_COUNT);
			maxIndex = mPrefs.getInt("Channels", 0);
		}
	}
	/**
	 * 子线程，删除counter计时器
	 * @author caowei
	 *
	 */
	class MoveCounter extends Thread{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			removeCounter();
		}
	}
	/**
	 * 子线程，删除counter计时器
	 * @author caowei
	 *
	 */
	class StartNewTitle extends Thread{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			startNewTitleRunnable();
		}
	}
}

