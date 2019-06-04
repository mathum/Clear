package com.clearcrane.activity;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.clearcrane.vod.R;

/**
 * 音乐播放页面点击事件集中处理
 * @author SlientLeaves
 * 2016年8月8日  下午5:40:47
 */
public final class MusicPalyClickListener implements View.OnClickListener{

	private static final String TAG = "MusicPalyClickListener";
	
	public final void onClick(View paramView) {
		ImageButton localImageButton;
		if(paramView != null){
			localImageButton = (ImageButton) paramView;
			switch (paramView.getId()) {
			//播放模式
			case R.id.ibt_play_loop_mode:
				Log.i(TAG, "播放模式");
				
				break;
			//上一首
			case R.id.ibt_play_pre:
				Log.i(TAG, "上一首");
				
				break;
			//播放/暂停
			case R.id.ibt_play_resume:
				Log.i(TAG, "播放/暂停");
				
				break;
			//下一首
			case R.id.ibt_play_next:
				Log.i(TAG, "下一首");
				
				break;	
			//歌词
//			case R.id.ibt_play_lyric:
//				Log.i(TAG, "歌词");
//				
//				break;	
			//列表
			case R.id.ibt_play_list:
				Log.i(TAG, "列表");
				
				break;	

			default:
				break;
			}
		}
	}
	

}
