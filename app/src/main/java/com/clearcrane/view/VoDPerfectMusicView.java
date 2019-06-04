package com.clearcrane.view;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.clearcrane.adapter.PerfectMusicAdapter;
import com.clearcrane.constant.ClearConstant;
import com.clearcrane.databean.Mp3Info;
import com.clearcrane.log.ClearLog;
import com.clearcrane.provider.MaterialRequest;
import com.clearcrane.provider.MaterialRequest.OnCompleteListener;
import com.clearcrane.service.PerfectPlayerService;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.LogUtils;
import com.clearcrane.util.TipDialog;
import com.clearcrane.vod.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VoDPerfectMusicView extends VoDBaseView implements OnClickListener {
	// 一系列动作
	public static final String UPDATE_ACTION = "com.zxb.action.UPDATE_ACTION"; // 更新动作
	public static final String CTL_ACTION = "com.zxb.action.CTL_ACTION"; // 控制动作
	public static final String MUSIC_CURRENT = "com.zxb.action.MUSIC_CURRENT"; // 当前音乐改变动作
	public static final String MUSIC_DURATION = "com.zxb.action.MUSIC_DURATION"; // 音乐时长改变动作
	public static final String REPEAT_ACTION = "com.zxb.action.REPEAT_ACTION"; // 音乐重复改变动作
	public static final String SHUFFLE_ACTION = "com.zxb.action.SHUFFLE_ACTION"; // 音乐随机播放动作
	public static final String MUSIC_PLAY = "com.zxb.action.MUSIC_PLAY";// 播放音乐
	private Button singleButton;
	private Button listPlayButton;
	private Button repeatButton;
//	private Button playButton;
	private Button shuffleButton;
//	private Button nextButton;
//	private List<Button> controlButtons;
//	private SeekBar musicProgress;
//	private TextView takeTimeTextView;
//	private TextView durationTextView;
	private ListView musicListView;
	private List<Mp3Info> musicList = new ArrayList<>();
	private PerfectMusicAdapter musicAdapter;
	// private LinearLayout controlLinearLayout;
//	private LinearLayout musicInfoLayout;
	private RoundImageView musicRoundImageView;
	// private TextView currentPlay;
	// private TextView currentSinger;
	// private TextView currentMode;
	// private int controlPosition = 0;
	private int listPosition = 0;
	private int playMode = 1;
	private boolean isRefresh = false;
	// private boolean isPlaying = false;
	// private boolean isFirstPlay = true;
	// private boolean isSingleLoop = true;// 默认全部循环，单曲循环为用户的下次点击做准备
	// private String playMode = "全部循环";
	private MusicViewBroadcastReceiver musicViewBroadcastReceiver;
	// private Handler handler = new Handler() {
	// public void handleMessage(android.os.Message msg) {
	//
	// };
	// };
	// private HideInfoRunnable hideInfoRunnable = new HideInfoRunnable();
	private SharedPreferences sp;
	public SharedPreferences activitySharePre;

	@Override
	public void init(Context ctx) {
		// TODO Auto-generated method stub
		super.init(ctx);
	}

	@Override
	public void init(Context ctx, String u) {
		// TODO Auto-generated method stub
		super.init(ctx, u);
		view = LayoutInflater.from(ctx).inflate(R.layout.perfect_music_view, null);
		initView();

		sp = context.getSharedPreferences("musicUrl", Context.MODE_PRIVATE);
		activitySharePre = context.getSharedPreferences(ClearConstant.Activity_FILE, Context.MODE_PRIVATE);
		// Intent intent = new Intent(context, PerfectPlayerService.class);
		// context.startService(intent);

		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(DataJsonListen);
		mr.execute(u);

		musicViewBroadcastReceiver = new MusicViewBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(MUSIC_CURRENT);
		filter.addAction(MUSIC_DURATION);
		filter.addAction(UPDATE_ACTION);
		context.registerReceiver(musicViewBroadcastReceiver, filter);
	}

	public void initView() {
		// controlButtons = new ArrayList<>();
		singleButton = (Button) view.findViewById(R.id.single_repeat);
		singleButton.setTextColor(Color.YELLOW);
		// previousButton = (Button) view.findViewById(R.id.previous_music);
		// controlButtons.add(previousButton);
		listPlayButton = (Button) view.findViewById(R.id.list_play_music);

		repeatButton = (Button) view.findViewById(R.id.list_repeat_music);
		// controlButtons.add(repeatButton);
		// playButton = (Button) view.findViewById(R.id.play_music);
		// controlButtons.add(playButton);
		shuffleButton = (Button) view.findViewById(R.id.shuffle_music);
		// controlButtons.add(shuffleButton);
		// nextButton = (Button) view.findViewById(R.id.next_music);
		// controlButtons.add(nextButton);

		// musicProgress = (SeekBar) view.findViewById(R.id.music_progress);
		musicListView = (ListView) view.findViewById(R.id.music_list);
		// controlLinearLayout = (LinearLayout)
		// view.findViewById(R.id.control_linearLayout);
		// musicInfoLayout = (LinearLayout)
		// view.findViewById(R.id.music_infomation_show_layout);
		// takeTimeTextView = (TextView)
		// view.findViewById(R.id.music_take_time);
		// durationTextView = (TextView)
		// view.findViewById(R.id.music_left_time);
		// currentPlay = (TextView) view.findViewById(R.id.music_current_play);
		// currentSinger = (TextView)
		// view.findViewById(R.id.music_current_singer);
		// currentMode = (TextView) view.findViewById(R.id.music_current_mode);

		// musicInfoLayout.setPivotX(musicInfoLayout.getWidth()/2);
		// musicInfoLayout.setPivotY(musicInfoLayout.getHeight());
		musicRoundImageView = (RoundImageView) view.findViewById(R.id.music_round_image_view);
		// musicListView.clearFocus();
		musicListView.requestFocus();

		musicAdapter = new PerfectMusicAdapter(context, musicList);
		musicListView.setAdapter(musicAdapter);

		setViewOnclickListener();
	}

	private void setViewOnclickListener() {
		// TODO Auto-generated method stub
		// previousButton.setOnClickListener(this);
		listPlayButton.setOnClickListener(this);
		singleButton.setOnClickListener(this);
		repeatButton.setOnClickListener(this);
		// playButton.setOnClickListener(this);
		shuffleButton.setOnClickListener(this);
		// nextButton.setOnClickListener(this);

		musicListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				// 发生切换，上传结束日志
				if (listPosition != arg2) {
					LogUtils.sendLogEnd(mApp, "点播", "音频", musicList.get(listPosition).name);
				}
				listPosition = arg2;
				isRefresh = false;
				setPlayState(arg2);
				LogUtils.sendLogStart(mApp, "点播", "音频", musicList.get(listPosition).name);
				playMusic(arg2, ClearConstant.PLAY_MSG);
				saveMusicPosition();
				// showOrHideMusicInfo();
			}
		});
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
			int dur = musicList.get(position).getDuration();
			int degree = dur / 10 * 360;
			rotateMusicImage(dur * 1000, degree);
		}
		musicAdapter.notifyDataSetChanged();
		musicListView.setSelection(position);
	}

	@Override
	public boolean onKeyBack() {
		// TODO Auto-generated method stub
		Intent broadIntent = new Intent(CTL_ACTION);
		broadIntent.putExtra("control", 0);
		context.sendBroadcast(broadIntent);
		saveStatue(3, "");

		// mApp.isInterruptProgram = false;
		//// String categoryPath = mApp.interruptProgramContent;
		// String logInsert = mApp.combinatePostParasString("stop", "0", "点播",
		// "音频", mApp.interruptProgramResourceName,
		// "");
		// ClearLog.logInsert(logInsert);
		if (musicList.size() > 0) {
			LogUtils.sendLogEnd(mApp, "点播", "音频", musicList.get(listPosition).name);
		}
		return super.onKeyBack();
	}

	private OnCompleteListener DataJsonListen = new OnCompleteListener() {

		@Override
		public void onDownloaded(Object result) {
			// TODO Auto-generated method stub
			String dataJson = (String) result;
			if (dataJson == null) {
				TipDialog.Builder builder = new TipDialog.Builder(context);
				builder.setMessage("当前网络不可用，请检查网络");
				builder.setTitle("提示");
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// 设置你的操作事项
					}
				});

				builder.create().show();
				return;
			}
			try {
				JSONTokener jsonParser = new JSONTokener(dataJson);
				JSONObject mainViewObj = (JSONObject) jsonParser.nextValue();

				JSONArray contentArray = (JSONArray) mainViewObj.getJSONArray("Content");
				Log.e("xb", "length:" + contentArray.length());
				for (int i = 0; i < contentArray.length(); i++) {
					JSONObject objecttmp = (JSONObject) contentArray.opt(i);

					Mp3Info m = new Mp3Info();
					StringBuilder str = new StringBuilder(objecttmp.getString("AudioName"));

					int indexEnd = str.indexOf(".");
					if (indexEnd != -1) {
						m.name = str.substring(0, indexEnd);
					}
					m.playURL = objecttmp.getString("AudioPath");
					// Log.i("music", "playurl:" + m.playURL);

					m.nameEng = "weizhi";
					// m.picURL = "";
					m.singer = "singer";
					m.singerEng = "singerEng";
					m.album = "album";
					m.albumEng = "albumEng";
					m.summary = "summary";
					m.summaryEng = "summaryEng";
					if (objecttmp.getString("duration") != null) {
						m.duration = Integer.parseInt(objecttmp.getString("duration"));
					} else {
						m.duration = 309;
					}
					musicList.add(m);

					// musicListView.setFocusable(true);
					// musicListView.requestFocus();
				}
				musicAdapter.notifyDataSetChanged();
			} catch (JSONException e) {
				ClearLog.LogError("BROSWER\tLoad\tFAIL\t0ms\t" + url);
				e.printStackTrace();
			}
			Log.e("xb", "初始化musicList:" + musicList.size());
			Intent broadIntent = new Intent(CTL_ACTION);
			broadIntent.putExtra("musicList", (Serializable) musicList);
			broadIntent.putExtra("control", 5);
			context.sendBroadcast(broadIntent);

			// musicInfoLayout.setVisibility(view.VISIBLE);
			// showOrHideMusicInfo();
		}

		@Override
		public void onComplete(boolean result) {
			// TODO Auto-generated method stub

		}
	};

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		// handler.removeCallbacks(hideInfoRunnable);
		// showOrHideMusicInfo();
		Intent intent = null;
		singleButton.setTextColor(Color.WHITE);
		repeatButton.setTextColor(Color.WHITE);
		listPlayButton.setTextColor(Color.WHITE);
		shuffleButton.setTextColor(Color.WHITE);
		switch (arg0.getId()) {
		// case R.id.previous_music:
		// playMusic(listPosition, ClearConstant.PRIVIOUS_MSG);
		// break;
		case R.id.single_repeat:
			singleButton.setTextColor(Color.YELLOW);
			playMode = 1;
			intent = new Intent(CTL_ACTION);
			intent.putExtra("control", 1);
			context.sendBroadcast(intent);
			break;
		case R.id.list_repeat_music:
			repeatButton.setTextColor(Color.YELLOW);
			playMode = 2;
			intent = new Intent(CTL_ACTION);
			intent.putExtra("control", 2);
			context.sendBroadcast(intent);
			break;
		case R.id.list_play_music:
			listPlayButton.setTextColor(Color.YELLOW);
			playMode = 3;
			intent = new Intent(CTL_ACTION);
			intent.putExtra("control", 3);
			context.sendBroadcast(intent);
			break;
		case R.id.shuffle_music:
			shuffleButton.setTextColor(Color.YELLOW);
			playMode = 4;
			intent = new Intent(CTL_ACTION);
			intent.putExtra("control", 4);
			context.sendBroadcast(intent);
			break;
		default:
			break;
		}
		// handler.postDelayed(hideInfoRunnable, 5000);
	}

	public void playMusic(int musicPosition, int message) {
		Intent intent = new Intent(context, PerfectPlayerService.class);
		Log.e("xb", musicList.get(musicPosition).getPlayURL());
		intent.putExtra("url", musicList.get(musicPosition).getPlayURL());
		intent.putExtra("listPosition", musicPosition);
		intent.putExtra("MSG", message);
		context.startService(intent);

		int dur = musicList.get(musicPosition).getDuration();
		int degree = dur / 10 * 360;
		rotateMusicImage(dur * 1000, degree);

		// 保存当前播放的音乐，用于插播计划播结束后还原
		saveStatue(1, musicList.get(musicPosition).getName());

//		mApp.viewType = "音频";
//		mApp.timeInS = DateUtil.getCurrentTimeSecond();
//		Log.e("aaa", "interrupt:" + mApp.timeInS);
//		mApp.resourceName = musicList.get(musicPosition).getName();
//		mApp.interruptProgramTimeInS = DateUtil.getCurrentTimeSecond();
//		Log.e("aaa", "interrupt:" + mApp.interruptProgramTimeInS);
//		String logInsert = mApp.combinatePostParasString("start", "0", "点播", "音频", mApp.interruptProgramResourceName,
//				"");
//		ClearLog.logInsert(logInsert);
//		mApp.SendLogMode = 1;
//		mApp.isInterruptProgram = false;
	}

	// private String formatterTime(long time) {
	// Date dates = new Date(time);
	// SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");//
	// 初始化Formatter的转换格式。
	// String ms = formatter.format(dates);
	// return ms;
	// }

	// private void showOrHideMusicInfo() {
	// changeInfo();
	// musicInfoLayout.setPivotX(musicInfoLayout.getWidth()/2);
	// musicInfoLayout.setPivotY(musicInfoLayout.getHeight());
	// musicInfoLayout.animate().alpha(1.0f).scaleY(1.0f).start();
	// Animator anim = AnimatorInflater.loadAnimator(context,
	// R.anim.music_info_anim);

	// 显示的调用invalidate
	// musicInfoLayout.invalidate();
	// anim.setTarget(musicInfoLayout);
	// anim.start();
	// isShow = true;
	// handler.postDelayed(hideInfoRunnable, 5000);
	// }

	// private void changeInfo() {
	// currentPlay.setText("当前播放:" + musicList.get(listPosition).getName());
	// currentSinger.setText("当前歌手:" + musicList.get(listPosition).getSinger());
	// currentMode.setText("播放模式:" + playMode);
	// }

	// 控制播放信息面板关闭
	// class HideInfoRunnable implements Runnable {
	// public void run() {
	// musicInfoLayout.animate().alpha(0.0f).scaleY(0.0f).start();
	// }
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
				Log.e("xb", "listposition:" + arg1.getIntExtra("current", 0));
				isRefresh = arg1.getBooleanExtra("refresh", false);
				// changeInfo();
				saveMusicPosition();
				setPlayState(listPosition);
				saveStatue(1, musicList.get(listPosition).name);
				break;
			default:
				break;
			}
		}
	}

	private void saveMusicPosition() {
		Editor editor = sp.edit();
		editor.putInt("musicPos", listPosition);
		editor.putString("url", musicList.get(listPosition).getPlayURL());
		editor.putString("music_name", musicList.get(listPosition).getName());
		editor.commit();
	}

	// 保存当前播放状态 用于给后台展示终端状态
	private void saveStatue(int state, String movieName) {
		Editor editor = activitySharePre.edit();
		editor.putInt(ClearConstant.Play_Statue, state);
		editor.putString(ClearConstant.Movie_NAME, movieName);
		editor.commit();
	}

	// 旋转左侧的音乐图标
	private void rotateMusicImage(long duration, int degree) {
		ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(musicRoundImageView, "rotation", 0, degree);
		objectAnimator.setDuration(duration);
		objectAnimator.start();
	}
}
