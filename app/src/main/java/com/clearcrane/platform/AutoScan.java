package com.clearcrane.platform;
import android.app.TvManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

public class AutoScan{
	private static final float TV_FREQUENCY_MINIMUM = (float) 44.25;
	private static final float TV_FREQUENCY_MAXIMUM = (float) 865.25;	

	private TvManager mTvManager = null;
	private volatile boolean stop = false;
	private volatile boolean finish = false;
	private TextView mTextView;
	private int tv_type = TvManager.TV_TYPE_ATV;
	private int color_mode = TvManager.RT_COLOR_STD_PAL_60;
	private int sound_mode = TvManager.RT_ATV_SOUND_SYSTEM_DK;
	public String TV_SERVICE = "tv";
	
	private SharedPreferences mPrefs = null;
	private int totalChannel = 0;
	public AutoScan(skyworthSetPreference main,TextView mTextView){
		Log.i("AutoScan","constructor2");
		
		this.mTextView = mTextView;

		mPrefs = PreferenceManager.getDefaultSharedPreferences(main);
		mTvManager = (TvManager)main.getSystemService(TV_SERVICE);
	}
	public AutoScan(Context context,int tv_type){
		Log.i("AutoScan","constructor2");
		
		mTvManager = (TvManager)context.getSystemService(TV_SERVICE);
		
		this.tv_type = tv_type;
	}
	public AutoScan(Context context,int tv_type,int color_mode,int sound_mode){
		Log.i("AutoScan","constructor3");
		
		mTvManager = (TvManager)context.getSystemService(TV_SERVICE);
		
		this.tv_type = tv_type;
		this.color_mode = color_mode;
		this.sound_mode = sound_mode;
	}
	public void start(){
		Log.i("AutoScan","***************************");
		mTextView.post(new Runnable(){
			@Override
			public void run(){
				mTextView.setVisibility(0);
			    mTextView.setGravity(Gravity.CENTER);
			    mTextView.setBackgroundColor(Color.BLACK);
			    mTextView.setTextColor(Color.WHITE);
			    mTextView.setTextSize(35);
			    mTextView.setBackgroundColor(Color.argb(0xaf, 0, 0, 0));
			}
		});
	    
		AutoScanThread ast  = new AutoScanThread();
		ast.start();
	}
	public boolean isFinished(){
		return finish;
	}
	public int getChanenlInfo(){
		return mTvManager.getChannelCount();
	}
	class AutoScanThread extends Thread{
		private void getScanInfo(boolean autoScan)
		{
			int freq = 0, band = 0, channel = 0;		
			try{
				if(autoScan)
				{
					channel = mTvManager.tvScanInfo(TvManager.SCAN_INFO_CHANNEL_COUNT);
					freq = mTvManager.tvScanInfo(TvManager.SCAN_INFO_CHANNEL_FREQUENCY);
				}
				else
				{
					channel = mTvManager.tvScanInfo(TvManager.SCAN_INFO_CURRENT_CHANNEL_NUMBER);
					freq = mTvManager.tvScanInfo(TvManager.SCAN_INFO_CHANNEL_FREQUENCY);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			Log.d("yaofangauto","channel:" + channel + ", freq:" + freq);
			
			if(freq < (int)TV_FREQUENCY_MINIMUM*1000000)
				freq = (int)TV_FREQUENCY_MINIMUM*1000000;
			else if(freq > (int)TV_FREQUENCY_MAXIMUM*1000000)
				freq = (int)TV_FREQUENCY_MAXIMUM*1000000;
			
			if(freq < 137000000)
				band = 0; //"VHF-L"
			else if(freq < 425000000)
				band = 1; //"VHF-H"
			else
				band = 2; //"UHF"

			Log.i("------------------","freq="+freq+";band="+band+";channel="+channel);
			totalChannel = channel;
			//Toast.makeText(ct,"freq="+freq+";\n\rband="+band+";\n\rchannel="+channel+";\n\rMAC"+mTvManager.getMacAddress()+";\n\rSerial Code:"+mTvManager.getSerialCode(),Toast.LENGTH_LONG).show();
			MRunnable mr = new MRunnable();
			mr.t = "FM = "+freq/1000000+"MHz\n\r\n\rNo. = "+channel+"\r\n";
			mTextView.post(mr);
		}
		
		@Override
		public void run(){
			int prevProgress = 0, progress = 0;
			
			mTvManager.setFacAutoScanGuide(false);
			mTvManager.setSource(tv_type);
			mTvManager.setCurAtvColorStd(color_mode);
			mTvManager.setCurAtvSoundStd(sound_mode);
			mTvManager.setMute(true);
			
			try
			{
				int tv_type = mTvManager.getCurLiveSource();
				
				if(tv_type == TvManager.SOURCE_ATV1 || tv_type == TvManager.SOURCE_ATV2)
					tv_type = TvManager.TV_TYPE_ATV;
				else
					tv_type = TvManager.TV_TYPE_DTV;
				
				int converted3D = mTvManager.get3dMode();
				
				if(TvManager.SLR_3DMODE_2D_CVT_3D == converted3D) 
				{
					mTvManager.set3dMode(TvManager.SLR_3DMODE_2D);
				}
				
				if(mTvManager.tvAutoScanStart( false))
				{
					while (!stop && prevProgress < 100) 
					{
						progress = mTvManager.tvScanInfo(TvManager.SCAN_INFO_PROGRESS);
						
						Log.i("-----------------","state="+mTvManager.tvScanInfo(TvManager.SCAN_INFO_STATE));
						Log.i("-----------------","count="+mTvManager.tvScanInfo(TvManager.SCAN_INFO_CHANNEL_COUNT));
						Log.i("-----------------","progress="+progress);
						
						if(prevProgress > 0 && progress == 0)
						{
							stop = true;
							break;			
						}
						
						if(progress == 0) 
						{
							int startFreq = mTvManager.getAtvSeqScanStartFreq();
							Log.i("----------------------","startFreq = " + startFreq);
						}
						else if(progress > 0)
						{
							prevProgress = progress;
							Log.i("-----------------------","curProgress = " + prevProgress);
							getScanInfo(true);
							if(100 == progress)
							{
								break;
							}
						}
						sleep(500);
					}
					mTextView.post(new Runnable(){
						@Override
						public void run(){
							mTextView.setText("搜索完毕，请安确定键退出");
						}
					});
					if (stop == true)
					{
						Log.i("------------------","Autoscan aborted!!");
						mTvManager.tvAutoScanStop();
						Log.i("--------------------","Autoscan completeded, first channel will be played!!");
						finish = true;
						mTvManager.tvAutoScanComplete();
						mTvManager.setSource(TvManager.SOURCE_PLAYBACK);
					}
					else
					{
						Log.i("--------------------------","Autoscan completeded, first channel will be played!!");
						finish = true;
						mTvManager.tvAutoScanComplete();
						mTvManager.setSource(TvManager.SOURCE_PLAYBACK);
					}
				}
				else
				{
					Log.e("--------------------------","Failed to start auto scan!!");
				}
				Log.d("-------------","total channel:"+totalChannel);
				mPrefs.edit().putInt("Channels", totalChannel).commit();
				mTvManager.setMute(false);
			}
			catch (Exception e) {
				Log.e("----------------------","AutoScanThread throws exception!!");
				e.printStackTrace();
			}
		}
	}	
	public void requestStop() 
	{
		Log.i("---------------------","Auto scan is requested to stop...");
	    stop = true;
	}
	
	void requestStopNoChannelSwitch()
	{
		Log.i("----------------------","Auto scan is requested to stop and don't want channel to be switched...");
		stop = true;
	}
	class MRunnable implements Runnable{
		public String t;
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			mTextView.setText(t);
		}
		
	}
}
