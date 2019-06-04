package com.clearcrane.view;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import com.clearcrane.schedule.Material;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.vod.R;

import java.util.ArrayList;

public class FullScreenChannelView extends VoDBaseView{

	private final String TAG = "FullScreenChannelView";
	private VideoView mFullVideoView;
	private ImageView mFullImageView;
	private String videoPath = "http://172.16.1.33/nativevod/resource/video_resource/afd3a538ee235b43a87a8fcdbdcf1378_145128990377.avi";
	private Handler handler;
	public ArrayList<Material> materialList;
	
	public String getVideoPath(){
		return videoPath;
	}
	
	
	public void init(Context ctx, String u) {
		super.init(ctx, u);
		Log.e(TAG,"lilei init fullchannel start init");
		this.isImsView = true;
		url = u;
//		videoPath = "http://172.16.1.33/nativevod/resource/video_resource/afd3a538ee235b43a87a8fcdbdcf1378_145128990377.avi";
		view = LayoutInflater.from(context).inflate(
				R.layout.fullscreen_channel, null);
		initLayoutInXml();
//		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
//		mr.setOnCompleteListener(mFullScreenChannelListener);
//		mr.execute(url);
		Log.e(TAG,"lilei init fullchannel startplay");
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
//		handler.post(startPlay);
//		VoDViewManager.getInstance().startLiveVideo(videoPath);
	}
	
	
	private Runnable startPlay = new Runnable() {
		public void run() {
			Log.e(TAG,"videoPath " + videoPath);
			VoDViewManager.getInstance().startLiveVideo(videoPath);
		}
	};
	
	
	
	private void initLayoutInXml(){
		VoDViewManager.getInstance().isInLiveView = true;
//		mFullVideoView = (VideoView) view.findViewById(R.id.vv_channel);
		mFullImageView = (ImageView) view.findViewById(R.id.iv_channel);
		mFullImageView.setVisibility(View.GONE);
//		mFullVideoView.setVisibility(View.GONE);
//		mFullVideoView.setVideoPath(videoPath);
		VoDViewManager.getInstance().setLiveVideoDisplayArea(0, 0, 
				ClearConfig.getScreenWidth(), ClearConfig.getScreenHeight());
//		VoDViewManager.getInstance().showLiveVideo();
//		handler = new Handler();
		
	}
	
	

}
