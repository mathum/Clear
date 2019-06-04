package com.clearcrane.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.databean.Mp3Info;
import com.clearcrane.service.PerfectPlayerService;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.MusicListAdapter;
import com.clearcrane.vod.R;

import java.io.Serializable;
import java.util.ArrayList;

public class InterCutMusicView extends VoDBaseView {
	// 一系列动作
	public static final String UPDATE_ACTION = "com.zxb.action.UPDATE_ACTION"; // 更新动作
	public static final String CTL_ACTION = "com.zxb.action.CTL_ACTION"; // 控制动作
	public static final String MUSIC_CURRENT = "com.zxb.action.MUSIC_CURRENT"; // 当前音乐改变动作
	public static final String MUSIC_DURATION = "com.zxb.action.MUSIC_DURATION"; // 音乐时长改变动作
	public static final String REPEAT_ACTION = "com.zxb.action.REPEAT_ACTION"; // 音乐重复改变动作
	public static final String SHUFFLE_ACTION = "com.zxb.action.SHUFFLE_ACTION"; // 音乐随机播放动作

	private ListView musicListView;
	private int curFocusMusicIdx = -1;// 当前在播放的音乐
	private ArrayList<Mp3Info> musicList = new ArrayList<Mp3Info>();
	private MusicListAdapter musicAdapter;
	private ImageView musicOkBurron;
	private TextView musicNameInTitle;
	private TextView musicSingerInTitle;
	private TextView musicAlbumInTitle;
	private TextView songListName;
	private TextView singerListName;
	private ImageView musicAlbumImage;

	private int listPosition = 0;

	private MusicViewBroadcastReceiver musicViewBroadcastReceiver;
 
	private String songName;
	private String duration;
	@Override
	public void init(Context ctx, String u,String songName,String duration) {
		// TODO Auto-generated method stub
		super.init(ctx, u);
		this.songName = songName;
		this.duration = duration;
		view = LayoutInflater.from(ctx).inflate(R.layout.music_view, null);
		initLayoutInXml();

		// 启动音乐服务
		// 音乐服务在此开启会出现不能马上接受广播的问题，故此我将音乐服务开启放置到程序进入时
//		Intent intent = new Intent(context, PerfectPlayerService.class);
//		context.startService(intent);

		initData(u);

		// onKeyEnter();

		// 注册音乐播放状态监听
		musicViewBroadcastReceiver = new MusicViewBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(MUSIC_CURRENT);
		filter.addAction(MUSIC_DURATION);
		filter.addAction(UPDATE_ACTION);
		ctx.registerReceiver(musicViewBroadcastReceiver, filter);

		// 播放音乐
		playMusic(listPosition, ClearConstant.PLAY_MSG);

		// TODO Auto-generated method stub
		// 设置播放模式
		Intent intent2 = new Intent(CTL_ACTION);
		intent2.putExtra("control", 2);
		context.sendBroadcast(intent2);

		// 设置播放内容
		Intent broadIntent = new Intent(CTL_ACTION);
		broadIntent.putExtra("musicList", (Serializable) musicList);
		broadIntent.putExtra("control", 5);
		context.sendBroadcast(broadIntent);
	}

	private void initLayoutInXml() {
		musicListView = (ListView) view.findViewById(R.id.musicList);
		musicListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				curFocusMusicIdx = arg2;
				for (Mp3Info music : musicList) {
					music.isPlay = false;
				}
				musicList.get(curFocusMusicIdx).isPlay = true;
				onKeyEnter();
				musicAdapter.notifyDataSetChanged();
				musicListView.setSelection(curFocusMusicIdx);
			}
		});
		songListName = (TextView) view.findViewById(R.id.music_name_and_count);
		singerListName = (TextView) view.findViewById(R.id.music_singer_list);
		musicOkBurron = (ImageView) view.findViewById(R.id.music_ok_button);

		musicOkBurron.setVisibility(View.INVISIBLE);
		if (ClearConfig.LanguageID == 1) {
			Log.i("music", "ok button chinese");
			musicOkBurron.setImageResource(R.drawable.ok_background);
			songListName.setText("音频列表");
			singerListName.setText(R.string.music_singer);
		} else {
			Log.i("music", "ok button english");
			musicOkBurron.setImageResource(R.drawable.ok_background);
			songListName.setText("音频列表");
			singerListName.setText(R.string.music_singer_eng);
		}
		musicAlbumImage = (ImageView) view.findViewById(R.id.music_album_image);
		musicNameInTitle = (TextView) view.findViewById(R.id.music_name_in_title);
		musicSingerInTitle = (TextView) view.findViewById(R.id.music_singer_in_title);
		musicAlbumInTitle = (TextView) view.findViewById(R.id.music_album_in_title);

		/* view initialize more */
		musicAdapter = new MusicListAdapter(context, musicList);
		musicListView.setAdapter(musicAdapter);
		musicListView.requestFocus();
	}

	private void initData(String u) {
		Mp3Info m = new Mp3Info();
		// Music m = new Music();
//		m.name = str.substring(0, indexEnd);
		m.name = songName;
		m.playURL = u;
		// Log.i("music", "playurl:" + m.playURL);
		m.nameEng = duration;
		m.singer = "singer";
		m.singerEng = "singerEng";
		m.album = "album";
		m.albumEng = "albumEng";
		m.summary = "summary";
		m.summaryEng = "summaryEng";

		musicList.add(m);
		musicAdapter.notifyDataSetChanged();

		// curFocusMusicIdx = 0;
		// musicListView.setSelection(curFocusMusicIdx);

	}

	private void playMusic(int musicPosition, int message) {
		Intent intent = new Intent(context, PerfectPlayerService.class);
		intent.putExtra("url", musicList.get(musicPosition).playURL);
		intent.putExtra("listPosition", musicPosition);
		intent.putExtra("MSG", message);
		context.startService(intent);
	}

	public void exit() {
		Intent broadIntent = new Intent(CTL_ACTION);
		broadIntent.putExtra("control", 0);
		context.sendBroadcast(broadIntent);
	}
	// @Override
	// public boolean onKeyEnter() {
	// // TODO Auto-generated method stub
	// // 设置播放模式
	// Intent intent2 = new Intent(CTL_ACTION);
	// intent2.setAction(CTL_ACTION);
	// intent2.putExtra("control", 2);
	// context.sendBroadcast(intent2);
	//
	// Intent broadIntent = new Intent(CTL_ACTION);
	// broadIntent.putExtra("musicList", (Serializable) musicList);
	// broadIntent.putExtra("control", 5);
	// context.sendBroadcast(broadIntent);
	// return true;
	// }

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
				Log.e("xb", arg1.getIntExtra("current", 0) + "");
				// changeInfo();
				// musicListView.setSelection(listPosition);
				break;
			default:
				break;
			}
		}
	}
}
