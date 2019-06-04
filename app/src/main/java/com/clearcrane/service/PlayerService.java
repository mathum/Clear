package com.clearcrane.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.clearcrane.constant.PlayerFinal;
import com.clearcrane.entity.MusicInfo;
import com.clearcrane.util.PlayerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayerService extends Service implements Runnable {

	// 点击了播放/暂停键的时候发这个action的广播
	public static final String ACTION_PLAY_BUTTON = "com.clearcrane.service.ACTION_PALY_BUTTON";

	// 点击了播放列表的时候发这个action的广播
	public static final String ACTION_PLAY_ITEM = "com.clearcrane.service.ACTION_PLAY_ITEM";

	// 定义上一首，下一首action
	public static final String ACTION_PLAY_PREVIOUS = "com.clearcrane.service.ACTION_PLAY_PREVIOUS";
	public static final String ACTION_PLAY_NEXT = "com.clearcrane.service.ACTION_PLAY_NEXT";

	// 更改播放模式mode的action
	public static final String ACTION_MODE = "com.clearcrane.service.ACTION_MODE";

	// seekbar 进度更改的action
	public static final String ACTION_SEEKBAR = "com.clearcrane.service.ACTION_SEEKBAR";

	// 当前音乐播放状态，默认为等待
	public static int state = PlayerFinal.STATE_WAIT;
	// 当前音乐循环模式，默认为随机
	public static int mode = PlayerFinal.MODE_LOOP;
	// 表示播放状态是否改变，进度条是否改变，播放模式时候改变
	public static boolean stateChange, seekChange, modeChange;
	// 常驻线程是否运行
	public static Boolean isRun = true;
	// 播放歌曲帮助类
	public static PlayerHelper player;
	// 当前播放列表
	public static List<MusicInfo> serviceMusicList;
	// 当前播放歌曲位置
	public static int servicePosition = 0;

	// 用一个List保存 客户注册的监听----此监听用于回调更新客户的ui，状态改变监听
	private static List<OnPlayerStateChangeListener> stateListeners = new ArrayList<OnPlayerStateChangeListener>();
	// 用于seekbar进度改变监听
	private static List<OnSeekChangeListener> seekListenerList = new ArrayList<OnSeekChangeListener>();
	// 用于播放模式改变监听
	private static List<OnModeChangeListener> modeListenerList = new ArrayList<OnModeChangeListener>();

	// 当前歌曲播放进度
	private static int progress = 0;
	// 当前歌曲进度条最大值
	private static int max = 0;
	// 当前播放的时间
	private static String time = "0:00";
	// 当前歌曲播放的时长
	private static String duration = "0:00";

	private String where;

	// handler匿名内部类，用于监听器遍历回调
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			// 对List中的所有监听器遍历回调，根据what值判断回调哪个监听器
			switch (msg.what) {
			case 0:
				for (OnPlayerStateChangeListener listener : stateListeners) {
					listener.onStateChange(state, mode, serviceMusicList, servicePosition);
				}
				break;
			case 1:
				for (OnSeekChangeListener listener : seekListenerList) {
					listener.onSeekChange(progress, max, time, duration);
				}
				break;
			case 2:
				for (OnModeChangeListener listener : modeListenerList) {
					listener.onModeChange(mode);
				}
				break;
			}

		};
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// 注册广播，并添加Action
		PlayerReceiver receiver = new PlayerReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_PLAY_ITEM);
		filter.addAction(ACTION_PLAY_BUTTON);
		filter.addAction(ACTION_PLAY_PREVIOUS);
		filter.addAction(ACTION_PLAY_NEXT);
		filter.addAction(ACTION_MODE);
		filter.addAction(ACTION_SEEKBAR);
		registerReceiver(receiver, filter);
		//new 歌曲播放类
		player = new PlayerHelper();
		//开启常驻线程
		new Thread(this).start();

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void run() {
		// 常驻线程，死循环
		while (isRun) {
			// 判断如果state改变，player播放类执行不同的方法
			if (stateChange) {
				Log.e(PlayerFinal.TAG, "stateChange=" + state);
				switch (state) {
				case PlayerFinal.STATE_WAIT:
					break;
				case PlayerFinal.STATE_PLAY:
					if (where.equals("local")) {
						player.play(serviceMusicList.get(servicePosition).getPlayURL_abs_path());
					} else if (where.equals("internet")) {
						Uri uri = Uri.parse(serviceMusicList.get(servicePosition).getPlayURL_abs_path());
						player.playInternet(getApplicationContext(), uri);
//						duration = String.valueOf(player.getPlayDuration());
						int totalSeconds = player.getPlayDuration() / 1000;;
						int minute = totalSeconds / 60;
						int second = totalSeconds % 60;
						
						String result = minute + ":";
						
						if(second < 10){
							result += "0";
						}
						
						result += second;
						duration = result;
						result = null;
					}
					seekChange = true;
					break;
				case PlayerFinal.STATE_PAUSE:
					player.pause();
					break;
				case PlayerFinal.STATE_CONTINUE:
					player.continuePlay();
					// 播放状态要动seekbar
					seekChange = true;
					break;
				case PlayerFinal.STATE_STOP:
					player.stop();
					break;
				}
				// state改变为false
				stateChange = false;
				// 向handler发送一条消息，通知handler执行回调函数
				handler.sendEmptyMessage(0);
			}
			if (player.isPlaying()) {
				seekChange = true;
			} else {
				seekChange = false;
			}
			// 如果进度改变执行以下
			if (seekChange) {
				// 得到当前播放时间，int，毫秒单位，也是进度条的当前进度
				progress = player.getPlayCurrentTime();
				// 得到歌曲播放总时长，为进度条的最大值
				max = player.getPlayDuration();
				
				int totalSeconds = progress / 1000;;
				int minute = totalSeconds / 60;
				int second = totalSeconds % 60;
				
				String result = minute + ":";
				
				if(second < 10){
					result += "0";
				}
				
				result += second;
				time = result;
				result = null;
				// seekChange改回false
				seekChange = false;
				try {
					// 等1s发送消息
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// 发送相应消息给handler
				handler.sendEmptyMessage(1);
			}
			// 如果歌曲播放模式改变，发送消息给handler，modeChange改回false
			if (modeChange) {
				handler.sendEmptyMessage(2);
				modeChange = false;
			}
		}
	}

	/**
	 * 定义一个广播，用于接收客户端发来的播放音乐的广播
	 */
	public class PlayerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.e(PlayerFinal.TAG, "action=================" + action);
			// 如果收到的是点击播放列表时发送的广播
			if (action.equals(ACTION_PLAY_ITEM)) {
				where = intent.getStringExtra(PlayerFinal.PLAYER_WHERE);
				// 得到当前页面传过来的播放列表
				serviceMusicList = intent.getParcelableArrayListExtra(PlayerFinal.PLAYER_LIST);
				// 得到页面点击item的position
				servicePosition = intent.getIntExtra(PlayerFinal.PLAYER_POSITION, 0);
				// state改变为play,播放歌曲
				state = PlayerFinal.STATE_PLAY;
				// stage改变
				stateChange = true;
			} else if (action.equals(ACTION_PLAY_BUTTON)) {
				// 如果接受到的是点击暂停/播放的广播
				// 根据当前状态点击后，进行相应状态改变
				switch (state) {
				case PlayerFinal.STATE_PLAY:
				case PlayerFinal.STATE_CONTINUE:
					state = PlayerFinal.STATE_PAUSE;
					break;
				case PlayerFinal.STATE_PAUSE:
					state = PlayerFinal.STATE_CONTINUE;
					break;
				case PlayerFinal.STATE_STOP:
					state = PlayerFinal.STATE_PLAY;
					break;

				}

				// state改变
				stateChange = true;
			} else if (action.equals(ACTION_PLAY_PREVIOUS)) {
				if (serviceMusicList != null) {
					// 点击上一首按钮，如果当前位置为0，退回歌曲列表最后一曲
					if (servicePosition == 0) {
						servicePosition = serviceMusicList.size();
					} else {
						servicePosition--;
					}
					// state改变
					state = PlayerFinal.STATE_PLAY;
					stateChange = true;
				}
			} else if (action.equals(ACTION_PLAY_NEXT)) {
				if (serviceMusicList != null) {
					// 点击下一首，根据播放模式不同，下一首位置不同
					switch (mode) {
					case PlayerFinal.MODE_LOOP:
						if (servicePosition == serviceMusicList.size() - 1) {
							servicePosition = 0;
						} else {
							servicePosition++;
						}
						state = PlayerFinal.STATE_PLAY;
						break;
					case PlayerFinal.MODE_ORDER:
						if (servicePosition == serviceMusicList.size() - 1) {
							state = PlayerFinal.STATE_STOP;
						} else {
							servicePosition++;
							state = PlayerFinal.STATE_PLAY;
						}
						break;
					case PlayerFinal.MODE_RANDOM:
						Random random = new Random();
						int randomServicePosition = servicePosition;
						while (true) {
							servicePosition = random.nextInt(serviceMusicList.size());
							Log.d(PlayerFinal.TAG, "randomServicePosition:" + randomServicePosition);
							if (randomServicePosition != servicePosition) {
								state = PlayerFinal.STATE_PLAY;
								break;
							}
						}
						break;
					case PlayerFinal.MODE_SINGLE:
						state = PlayerFinal.STATE_PLAY;
						break;
					}

					// state改变
					stateChange = true;
				}
			} else if (action.equals(ACTION_MODE)) {
				Log.i(PlayerFinal.TAG, "收到播放模式更改广播，当前播放模式为" + mode);
				switch (mode) {
				// 根据当前mode，做出mode的更改
				case PlayerFinal.MODE_LOOP:
					mode = PlayerFinal.MODE_RANDOM;
					break;
				case PlayerFinal.MODE_ORDER:
					mode = PlayerFinal.MODE_LOOP;
					break;
				case PlayerFinal.MODE_RANDOM:
					mode = PlayerFinal.MODE_SINGLE;
					break;
				case PlayerFinal.MODE_SINGLE:
					mode = PlayerFinal.MODE_ORDER;
					break;
				}

				// 播放模式改变
				modeChange = true;
			} else if (action.equals(ACTION_SEEKBAR)) {
				// seekbar发送的广播
				// 得到传过来的当前进度条进度，更改歌曲播放位置
				int seekTime = intent.getIntExtra(PlayerFinal.SEEKBAR_PROGRESS, progress);
				Log.i(PlayerFinal.TAG, "接收到seekbar广播了" + " seekTime:" + seekTime);
				player.seekToMusic(seekTime);
				// 进度条改变
				seekChange = true;
			}
		}

	}

	/**
	 * 向service注册一个监听器，用于监听播放状态的改变
	 * 
	 * @param listener
	 */
	public static void registerStateChangeListener(OnPlayerStateChangeListener listener) {
		listener.onStateChange(state, mode, serviceMusicList, servicePosition);
		stateListeners.add(listener);
		Log.e(PlayerFinal.TAG, "注册stateChange的监听，当前一共有" + stateListeners.size() + "个");
	}

	/**
	 * 向service注册一个监听器，用于监听seekbar改变
	 * 
	 * @param seekListener
	 */
	public static void registerSeekChangeListener(OnSeekChangeListener seekListener) {
		seekListener.onSeekChange(progress, max, time, duration);
		seekListenerList.add(seekListener);
		Log.d(PlayerFinal.TAG, "注册seekChange的监听，当前一共有" + seekListenerList.size() + "个");
	}

	/**
	 * 向service注册一个监听器，用于监听mode的改变
	 * 
	 * @param modeListener
	 */
	public static void registerModeChangeListener(OnModeChangeListener modeListener) {
		modeListener.onModeChange(mode);
		modeListenerList.add(modeListener);
		Log.d(PlayerFinal.TAG, "注册ModeChange，当前一共有" + modeListenerList.size() + "个");
	}

	/**
	 * 解除之前注册的监听器
	 * 
	 * @param statelistener
	 */
	public static void unRegisterStateChangeListener(OnPlayerStateChangeListener statelistener) {
		stateListeners.remove(statelistener);
		Log.d(PlayerFinal.TAG, "解除注册listener，当前一共有" + stateListeners.size() + "个");

	}

	/**
	 * 解除之前注册的监听器
	 * 
	 * @param seekListener
	 */
	public static void unRegisterSeekChangeListener(OnSeekChangeListener seekListener) {
		seekListenerList.remove(seekListener);
		Log.d(PlayerFinal.TAG, "解除注册seekChange，当前一共有" + stateListeners.size() + "个");

	}

	/**
	 * 解除之前注册的监听器
	 * 
	 * @param modeListener
	 */
	public static void unRegisterModeChangeListener(OnModeChangeListener modeListener) {
		modeListenerList.remove(modeListener);
		Log.d(PlayerFinal.TAG, "解除注册modeChange，当前一共有" + stateListeners.size() + "个");
	}

}
