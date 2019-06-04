package com.clearcrane.platform;


import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.TvManager;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.clearcrane.util.ClearConfig;
import com.clearcrane.view.VoDViewManager;
import com.clearcrane.vod.R;

public class SkyworthHDMIActivity extends Activity {
	private TvManager mTvManager = null;
	private SurfaceView sf = null;
	private TextView text = null;
	private TextView hdmi_text_change = null;
	private boolean hdmiSourceChanged = false;
	private boolean hdmiReadyCheck = false;
	private boolean pressExit= false;
	private Builder RateBuilder;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hdmi);
		
		//strict mode
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
	    .penaltyLog()
	    .build());
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		.penaltyLog()
	    .build());
		
		sf = (SurfaceView)findViewById(R.id.hdmisf);
		sf.getHolder().setFormat(PixelFormat.TRANSPARENT);
		sf.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		text = (TextView)findViewById(R.id.hdmi_text);
		hdmi_text_change = (TextView)findViewById(R.id.hdmi_text_change);
		
		mTvManager = (TvManager)getSystemService("tv");
		mTvManager.setVideoSize(0,0,
				ClearConfig.getScreenWidth(mTvManager),
				ClearConfig.getScreenHeight(mTvManager));
		
		new SourceRunnable(TvManager.SOURCE_HDMI1).start();
		
		// for 3d setting
		DialogInterface.OnClickListener SingleChoiceListener = new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int mode_3d = TvManager.SLR_3DMODE_2D;
				switch(which) {
				case 0:
					mode_3d = TvManager.SLR_3DMODE_2D;
					break;
				case 1:
					mode_3d = TvManager.SLR_3DMODE_3D_SBS;
					break;
				case 2:
					mode_3d = TvManager.SLR_3DMODE_3D_TB;
					break;
				default:
					break;
				}
				mTvManager.set3dMode(mode_3d);
				
				//new AlertDialog.Builder(HDMIActivity.this)
				//.setTitle("complete")
				//.setMessage("" + which)
				//.setPositiveButton("OK", null).show();
				// TODO Auto-generated method stub
			}
		};

		DialogInterface.OnClickListener BuilderListener = new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		};
		
		RateBuilder = new Builder(SkyworthHDMIActivity.this);
		RateBuilder.setTitle("3D设置");
		//RateBuilder.setIcon(R.drawable.ic_launcher);
		RateBuilder.setSingleChoiceItems(new String[] {"3D关", "3D左右", "3D上下"}, 0, SingleChoiceListener);
		RateBuilder.setPositiveButton("确定", BuilderListener);
		//RateBuilder.setNegativeButton("取消", null);
		RateBuilder.create();
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
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
			
			if(type == TvManager.SOURCE_HDMI1) {
				hdmiSourceChanged = true;
				hdmiReadyCheck = true;
				
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				hdmi_text_change.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						hdmi_text_change.setVisibility(android.view.View.GONE);
					}
				});
				
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				while(hdmiReadyCheck) {
					
					Log.i("Leo test", "is no signal? " + mTvManager.getIsNoSignal());
					//Log.i("Leo test", "getNoSignalDisplayReady? " + mTvManager.getNoSignalDisplayReady());
					
					if(mTvManager.getIsNoSignal()) {
						text.post(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								Log.i("Leo test", "set visible");
								text.setVisibility(android.view.View.VISIBLE);
							}
						});
					}
					else {
						text.post(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								text.setVisibility(android.view.View.GONE);
							}
						});
					}
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode ==KeyEvent.KEYCODE_VOLUME_DOWN){
			return super.onKeyDown(keyCode, event);
		}
		else if(keyCode == KeyEvent.KEYCODE_BACK)
		{	
			if(hdmiSourceChanged) {
				if(!pressExit)
				{
					pressExit = true;
					hdmiReadyCheck = false;
					new SourceRunnable(TvManager.SOURCE_PLAYBACK).start();
					finish();
				}
			}
			VoDViewManager.getInstance().showBackgroundVideo();
			VoDViewManager.getInstance().playBackgroundVideo();
		}
		else if(keyCode == KeyEvent.KEYCODE_MENU) {
			RateBuilder.show();
			return true;
		}
		/*
		else if(keyCode == KeyEvent.KEYCODE_SETTING) {
			return false;
		}
		*/
		return true;
	}
	@Override
	protected void onDestroy(){
		super.onDestroy();
	}
}
