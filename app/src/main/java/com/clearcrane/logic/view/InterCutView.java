package com.clearcrane.logic.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.clearcrane.view.VoDBaseView;
import com.clearcrane.view.VoDViewManager;
import com.clearcrane.vod.R;

public class InterCutView extends VoDBaseView {

	private final String TAG = "InterCutView";
	private String typeName;
	private Handler mHandler = new Handler();
	private TextView tvNote;
    private int time;
	/*
	 * TODO,FIXME!!!! when multi-videos
	 */
	public void init(Context ctx, String u, String type) {
		super.init(ctx, u);
		this.typeName = type;
		view = LayoutInflater.from(context).inflate(R.layout.insertion_live, null);
		tvNote = (TextView) view.findViewById(R.id.tv_note);
		tvNote.setText("插播中");
		tvNote.setVisibility(View.VISIBLE);
		time = 0;
		startPlay();
	}

	public void startPlay() {
		if (url == null) {
			Log.e(TAG, "url is null!");
		}
		mHandler.post(startPlay);
	}

	public void stopPlay() {
		VoDViewManager.getInstance().hideLiveVideo();
	}

	/*
	 * on 2018-01-12
	 * modify by winter
	 * to check url
	 * 3 times
	 * 
	 */
	
	
	
	private Runnable startPlay = new Runnable() {
		public void run() {
			Log.e(TAG, "startPlay InterCut " + url);
			/**
			 * 获取要求开始播放的时间，与现有时间作比较，到指定时间后放 跳到视频当前进度播（点播），直播是直接播放
			 */

			Log.e("xb", "intercut开始:" + url);
//			if (url.contains("m3u8")) {
//				VoDViewManager.getInstance().showLiveVideo();
//				VoDViewManager.getInstance().startLiveVideo(url);
//			} else {
				VoDViewManager.getInstance().showMovieVideo();
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				VoDViewManager.getInstance().startMovieVideo(url);
				VoDViewManager.getInstance().setMovieViewCompleteListener(new OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer arg0) {
						// TODO Auto-generated method stub
						//由于使用setlooping以及seekto()方法也无效，所以就重新设置播放器url实现循环。
						time++;
						arg0.stop();
						arg0.reset();
						VoDViewManager.getInstance().startMovieVideo(url);
					}
				});
			}
//		}
	};
	public String getIntercutUrl(){
		return url;
	}
	//获取当前插播内容被播放的次数
	public int getCurTime(){
	    return time;	
	}
}
