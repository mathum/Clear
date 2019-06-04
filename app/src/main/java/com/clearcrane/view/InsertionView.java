package com.clearcrane.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.log.ClearLog;
import com.clearcrane.schedule.DateUtil;
import com.clearcrane.schedule.Material;
import com.clearcrane.vod.R;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class InsertionView extends VoDBaseView {
	private final String TAG = "InsertionView";
	private VideoView mFullVideoView;
	private ImageView mFullImageView;
	private String videoPath ;
	private boolean isInCut = false;
	private Timer mCheckTimer;
	public static SharedPreferences INTERCUTSharePre;
	//= "http://172.16.1.33/nativevod/resource/video_resource/afd3a538ee235b43a87a8fcdbdcf1378_145128990377.avi";
	private Handler handler = new Handler();
	public ArrayList<Material> materialList;
	
	public String getVideoPath(){
		return videoPath;
	}
	
	public void init(Context ctx, String u) {
		super.init(ctx, u);
		
		videoPath = u;
//		videoPath = "http://172.16.1.33/nativevod/resource/video_resource/afd3a538ee235b43a87a8fcdbdcf1378_145128990377.avi";
		view = LayoutInflater.from(context).inflate(
				R.layout.insertion_live, null);
		initLayoutInXml();
//		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
//		mr.setOnCompleteListener(mFullScreenChannelListener);
//		mr.execute(url);
		startPlay();
	}
	
	   public void init(Context ctx, String u, boolean isInCutProgram) {
	        super.init(ctx, u);	        
	        videoPath = u;
	        isInCut = isInCutProgram;
	        if(isInCut){
	            VoDViewManager.getInstance().setActivityMode(-1);
	        }
	        INTERCUTSharePre = context.getSharedPreferences(ClearConstant.INTERCUT_FILE, Context.MODE_PRIVATE);
	        if(mCheckTimer == null){
	            mCheckTimer = new Timer(true);
	            mCheckTimer.schedule(new TimerTask() {
                   
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        if(getSecDuration(INTERCUTSharePre.getString(ClearConstant.TERMINATE_TIME, "")) < 2000){
                            handler.post(new Runnable() {
                                
                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    stopInterruptProgram();
                                }
                            });
                        }
                    }
                }, 1000,1000);
	        }
	        view = LayoutInflater.from(context).inflate(
	                R.layout.insertion_live, null);
	        initLayoutInXml();
	        startPlay();
	    }
	
	
	public void init(Context ctx, ArrayList<Material>  list){
		this.materialList = list;
		if (materialList.size()>0){
			videoPath = materialList.get(0).getSrcUrl();
		}
		this.init(ctx,"");
		this.isImsView = true;
		
	}

	public void startPlay(){
		if(videoPath == null){
			Log.e(TAG,"videopath is null!");
		}
		if(mFullVideoView == null){
			Log.e(TAG,"videoView is null!");
		}
		handler.post(startPlay);
	//	VoDViewManager.getInstance().startLiveVideo(videoPath);
	}
	
	
	private Runnable startPlay = new Runnable() {
		public void run() {
			Log.e(TAG,"videoPath " + videoPath);
			
			/**
			 * 获取要求开始播放的时间，与现有时间作比较，到指定时间后放
			 * 跳到视频当前进度播（点播），直播是直接播放
			 */
			
			VoDViewManager.getInstance().showLiveVideo();
			VoDViewManager.getInstance().startLiveVideo(videoPath);
		}
	};
	
	
	
	private void initLayoutInXml(){
//		VoDViewManager.getInstance().isInLiveView = true;
//		mFullVideoView = (VideoView) view.findViewById(R.id.vv_channel);
		mFullImageView = (ImageView) view.findViewById(R.id.live_channel);
		mFullImageView.setVisibility(View.GONE);
//		mFullVideoView.setVisibility(View.GONE);
//		mFullVideoView.setVideoPath(videoPath);
//		VoDViewManager.getInstance().setLiveVideoDisplayArea(0, 0, 
//				ClearConfig.getScreenWidth(), ClearConfig.getScreenHeight());
//		VoDViewManager.getInstance().showLiveVideo();
//		handler = new Handler();
		
	}
	
    public void stopInterruptProgram() {
        VoDViewManager.getInstance().stopMovieVideo();
        try {
            if (mCheckTimer != null) {
                mCheckTimer.cancel();
                mCheckTimer.purge();
                mCheckTimer = null;
            }
            String logInsert = mApp.combinatePostParasString("stop", "0", "插播", mApp.interruptviewType,
                    mApp.interruptProgramResourceName, mApp.catePath);
            ClearLog.logInsert(logInsert);
            Log.i(TAG, "stop : " + logInsert);
        } catch (Exception e) {
            Log.i(TAG, "stop : " + e.getMessage());
            e.printStackTrace();
        }
        VoDViewManager.getInstance().popForegroundView();
        VoDViewManager.getInstance().setActivityMode(0);       
        isInCut = false;
    	VoDBaseView topView = VoDViewManager.getInstance().getTopView();   
    	if(topView instanceof VoDLiveView){
    		((VoDLiveView) topView).getHandler().sendEmptyMessage(ClearConstant.JUMP_LIVE_VIEW);
    	}else if(topView instanceof VoDMovieView){
    		((VoDMovieView)topView).getHandler().sendEmptyMessage(ClearConstant.JUMP_MOVIE_VIEW);
    	}
    }
    
    public long getSecDuration(String time){
        long curServerTime = DateUtil.getCurrentTimeMillSecond();
        long remainTime = DateUtil.getTimeMillSecondFromDateStr(time) - curServerTime;
        return remainTime;
    }
}
