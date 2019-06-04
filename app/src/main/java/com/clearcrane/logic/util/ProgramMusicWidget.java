package com.clearcrane.logic.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.clearcrane.adapter.PerfectMusicAdapter;
import com.clearcrane.constant.ClearConstant;
import com.clearcrane.databean.Mp3Info;
import com.clearcrane.service.PerfectPlayerService;
import com.clearcrane.vod.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProgramMusicWidget extends ProgramBaseWidget {
	// 一系列动作
	public static final String UPDATE_ACTION = "com.zxb.action.UPDATE_ACTION"; // 更新动作
	public static final String CTL_ACTION = "com.zxb.action.CTL_ACTION"; // 控制动作
	public static final String MUSIC_CURRENT = "com.zxb.action.MUSIC_CURRENT"; // 当前音乐改变动作
	public static final String MUSIC_DURATION = "com.zxb.action.MUSIC_DURATION"; // 音乐时长改变动作
	public static final String REPEAT_ACTION = "com.zxb.action.REPEAT_ACTION"; // 音乐重复改变动作
	public static final String SHUFFLE_ACTION = "com.zxb.action.SHUFFLE_ACTION"; // 音乐随机播放动作
	public static final String MUSIC_PLAY = "com.zxb.action.MUSIC_PLAY";// 播放音乐
	
	private ListView musicListView;
	private PerfectMusicAdapter musicAdapter;
	private int listPosition = 0;
	private int playMode = 1;
	private boolean isRefresh = false;
	private MusicViewBroadcastReceiver musicViewBroadcastReceiver;
	private List<Mp3Info> musicList;
	@Override
	public void addWorkResource(ProgramResource resource) {
		// TODO Auto-generated method stub
		mResourceList.add(resource);
		Log.e("ProgramMusicWidget", "音乐计划播开始中"+ resource.getUrl());
	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub
		mResourceList = new ArrayList<>();
		View v = LayoutInflater.from(mContext).inflate(R.layout.music_view, null);
		mProgramLayout.addView(v);
		
		musicListView = (ListView) v.findViewById(R.id.musicList);
		
		//注册音乐播放器广播
		musicViewBroadcastReceiver = new MusicViewBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(MUSIC_CURRENT);
		filter.addAction(MUSIC_DURATION);
		filter.addAction(UPDATE_ACTION);
		mContext.registerReceiver(musicViewBroadcastReceiver, filter);
	}

	@Override
	public void play() {
		// TODO Auto-generated method stub
		musicList = new ArrayList<>();
		for(ProgramResource pr:mResourceList){
			Mp3Info mp3Info = new Mp3Info();
			StringBuilder str = new StringBuilder(pr.getName());

			int indexEnd = str.indexOf(".");
			if (indexEnd != -1) {
				mp3Info.name = str.substring(0, indexEnd);
			}
			mp3Info.playURL = pr.getUrl();

			mp3Info.nameEng = "weizhi";
			// m.picURL = "";
			mp3Info.singer = "singer";
			mp3Info.singerEng = "singerEng";
			mp3Info.album = "album";
			mp3Info.albumEng = "albumEng";
			mp3Info.summary = "summary";
			mp3Info.summaryEng = "summaryEng";
			if (pr.getDuration() != 0) {
				mp3Info.duration = pr.getDuration();
			} else {
				mp3Info.duration = 309;
			}
			musicList.add(mp3Info);
		}
		
		musicAdapter = new PerfectMusicAdapter(mContext, musicList);
		musicListView.setAdapter(musicAdapter);
		playMusic(0, ClearConstant.PLAY_MSG);
		
		setPlayState(0);
		// 设置播放模式
		Intent intent = new Intent(CTL_ACTION);
		intent.putExtra("control", 2);
		mContext.sendBroadcast(intent);
		
		// 设置播放内容
		Intent broadIntent = new Intent(CTL_ACTION);
		broadIntent.putExtra("musicList", (Serializable) musicList);
		broadIntent.putExtra("control", 5);
	    mContext.sendBroadcast(broadIntent);
	    
	}
	public void playMusic(int musicPosition, int message) {
		Intent intent = new Intent(mContext, PerfectPlayerService.class);
		Log.e("xb", musicList.get(musicPosition).getPlayURL());
		intent.putExtra("url", musicList.get(musicPosition).getPlayURL());
		intent.putExtra("listPosition", musicPosition);
		intent.putExtra("MSG", message);
		mContext.startService(intent);
	}
	private void setPlayState(int position) {
		// 如果是顺序播放并且回到了第一首歌曲，那么设置所有的歌曲状态为未播放
		if (isRefresh && playMode == 3) {
			for (Mp3Info music : musicList) {
				music.isPlay = false;
			}
		} else {
			for (Mp3Info music : musicList) {
				music.isPlay = false;
			}
			musicList.get(position).isPlay = true;
		}
		musicAdapter.notifyDataSetChanged();
		musicListView.setSelection(position);
	}
	@Override
	public void stop() {
		// TODO Auto-generated method stub
		Intent broadIntent = new Intent(CTL_ACTION);
		broadIntent.putExtra("control", 0);
		mContext.sendBroadcast(broadIntent);
	}

	@Override
	public int getTypeId() {
		// TODO Auto-generated method stub
		return 8;
	}
	class MusicViewBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			switch (arg1.getAction()) {
			case MUSIC_CURRENT:
				// StringBuilder msb = new
				// StringBuilder(formatterTime(arg1.getIntExtra("currentTime",
				// 0)));
				// takeTimeTextView.setText(msb);
				// musicProgress.setProgress(
				// (int) (((double) arg1.getIntExtra("currentTime", 0) /
				// (double) arg1.getIntExtra("duration", 0))
				// * musicProgress.getMax()));
				break;
			case MUSIC_DURATION:
				// String ms = formatterTime(arg1.getIntExtra("duration", 0));
				// durationTextView.setText(ms);
				break;
			case UPDATE_ACTION:
				listPosition = arg1.getIntExtra("current", 0);
				Log.e("xb", "listposition:" + arg1.getIntExtra("current", 0));
				isRefresh = arg1.getBooleanExtra("refresh", false);
				// changeInfo();
//				saveMusicPosition();
				setPlayState(listPosition);
//				saveStatue(1, musicList.get(listPosition).name);
				break;
			default:
				break;
			}
		}
	}
}
