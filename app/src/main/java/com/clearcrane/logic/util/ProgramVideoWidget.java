package com.clearcrane.logic.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;

import com.clearcrane.view.VoDViewManager;

import java.util.ArrayList;

public class ProgramVideoWidget extends ProgramBaseWidget {


	private boolean isSingleVideo;
	private int curIndex;
	private String mCurVideoPath;
	private SharedPreferences sf;
    private long videoDuration = 0;
    private long seekMillSecond = 0;
    
    /**
     * 用于首次播放刷新应该播放的视频以及SEEK TIME.
     */
    public void  reflashVideoIndexAndTime(){
        //首先获得所有资源节目所需要的时间
        videoDuration = 0;
        for (ProgramResource res : mResourceList){
            videoDuration += res.getDuration() * 1000 + 200;
        }
        //相对时间，比如播放多次，取余后的时间
        long relativeTime =  getElapsedMillSeconds() % videoDuration;
        //如果在一次循环之内，则递增视频时间，找到应该播放的视频
        long videoTime = 0;
        for(int i = 0; i < mResourceList.size();i++){
            videoTime += mResourceList.get(i).getDuration() * 1000 + 200;
            if(relativeTime < videoTime){
                //获得应该播放的视频索引
                curIndex = i;
                int time = 0;
                for(int j = 0 ; j < i; j++){
                    time += mResourceList.get(j).getDuration() * 1000 + 200;
                }
                //获得应该播放视频的seek time；
                seekMillSecond = relativeTime - time;
                break;
            }
        }

        Log.i(TAG,"curIndex: " + curIndex + "  seekMillSecond: " + seekMillSecond + "ms");

        Log.i(TAG,"relativeTime: " + relativeTime + "ms | videoDuration: " + videoDuration + "ms");
    }
    
	@Override
	public void play() {
		if (mResourceList.size() == 0 || mResourceList == null) {
			Log.e(TAG, "error mResourceList is kong!");
			return;
		}
		if (mResourceList.size() > 1) {
			isSingleVideo = false;
		}
		
		reflashVideoIndexAndTime();
		
		refreshmCurVideoPath();

		

		VoDViewManager.getInstance().showMovieVideo();
		VoDViewManager.getInstance().setMovieVideoDisplayArea(marginLeft, marginTop, widgetWidth, widgetHeight);
		VoDViewManager.getInstance().setMovieViewCompleteListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer arg0) {
				// TODO Auto-generated method stub
				Log.e("zxb", "播放完成 重新播放："+ curIndex);
				
				arg0.stop();
				arg0.reset();
				refreshCurIndex();
				refreshmCurVideoPath();
				Log.e("zxb", "---列表视频内容："+mCurVideoPath);
				VoDViewManager.getInstance().startMovieVideo(mCurVideoPath);
				saveStatue();
			}
		});
		
		VoDViewManager.getInstance().startMovieVideo(mCurVideoPath);
		VoDViewManager.getInstance().movieSeekTo((int)seekMillSecond);
		saveStatue();
	}

	private void refreshmCurVideoPath() {
		mCurVideoPath = mResourceList.get(curIndex).getUrl();
	}

	@Override
	public void stop() { // TODO Auto-generated method stub
		// if(mVideoView != null)
		// mVideoView.stopPlayback();
		Log.e("zxb", "计划播放结束，刷新数据");
		VoDViewManager.getInstance().hideLiveVideo();
		VoDViewManager.getInstance().hideMovieVideo();
		if (mResourceList != null){
			mResourceList.clear();
			mResourceList=null;
		    curIndex = 0;
		}
	}

	/**
	 * TODO,FIXME
	 * 
	 * 
	 */
	@Override
	public void initView() {
		Log.e(TAG, "initView");
		mResourceList = new ArrayList<ProgramResource>();
		mProgramLayout.setBackgroundColor(Color.TRANSPARENT);
		curIndex = 0;
		Log.e("xb", "initView " + marginLeft + " " + marginTop + " " + widgetHeight + " " + widgetWidth);
		isSingleVideo = true;
		
		sf = mContext.getSharedPreferences("ProgramVideoFile",Context.MODE_PRIVATE);
	}

	private void refreshCurIndex() {
		curIndex += 1;
		if (curIndex >= mResourceList.size()) {
			curIndex = 0;
		}
	}

	@Override
	public void addWorkResource(ProgramResource resource) {
		// TODO Auto-generated method stub
		mResourceList.add(resource);
		Log.e("zxb", "列表大小："+mResourceList.size());
	}

	private void saveStatue() {
		Editor editor = sf.edit();
		editor.putString("video_url", mCurVideoPath);
		editor.commit();
	}

	@Override
	public int getTypeId() {
		// TODO Auto-generated method stub
		return 1;
	}
}
