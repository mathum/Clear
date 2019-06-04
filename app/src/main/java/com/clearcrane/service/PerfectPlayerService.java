package com.clearcrane.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.databean.Mp3Info;

import java.util.List;

/***
 * 2013/5/25
 * 
 * @author wwj ���ֲ��ŷ���
 */
@SuppressLint("NewApi")
public class PerfectPlayerService extends Service {
	private MediaPlayer mediaPlayer; // 媒体播放器对象
	private String path; // 音乐文件路径
	private int msg; // 播放信息
	private boolean isPause; // 暂停状态
	private int current = 0; // 记录当前正在播放的音乐
	private List<Mp3Info> mp3Infos; // 存放Mp3Info对象的集合
	private int status = 1; // 播放状态，默认为顺序播放
	private MyReceiver myReceiver; // 自定义广播接收器
	private int currentTime; // 当前播放进度
	private int duration; // 播放长度ֵ

	//// 服务要发送的一些Action广播
	public static final String UPDATE_ACTION = "com.zxb.action.UPDATE_ACTION"; // 更新动作
	public static final String CTL_ACTION = "com.zxb.action.CTL_ACTION"; // 控制动作
	public static final String MUSIC_CURRENT = "com.zxb.action.MUSIC_CURRENT"; // 当前音乐播放时间更新动作
	public static final String MUSIC_DURATION = "com.zxb.action.MUSIC_DURATION";// 新音乐长度更新动作
	public static final String SHOW_LRC = "com.zxb.action.SHOW_LRC"; // 通知显示歌词
	/**
	 * handler用来接收消息，不停发送广播更新播放时间
	 */
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				if (mediaPlayer != null) {
					currentTime = mediaPlayer.getCurrentPosition(); // 获取当前音乐播放的位置
					Intent intent = new Intent();
					intent.setAction(MUSIC_CURRENT);
					intent.putExtra("duration", mediaPlayer.getDuration());
					intent.putExtra("currentTime", currentTime);
					sendBroadcast(intent);
					handler.sendEmptyMessageDelayed(1, 1000);
				}
			}
		};
	};

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("service", "service created");
        if (mediaPlayer == null) {
        	status = 1;
        	mediaPlayer = new MediaPlayer();
        	mediaPlayer.setOnCompletionListener(new MediaCompleteListener());
		}
//        currentTime = 0;
		// mp3Infos = MediaUtil.getMp3Infos(PlayerService.this);
		/**
		 * 设置音乐播放完成时的监听器
		 */
		mediaPlayer.setOnCompletionListener(new MediaCompleteListener());

		myReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(CTL_ACTION);
		// filter.addAction(SHOW_LRC);
		registerReceiver(myReceiver, filter);
	}

	/**
	 * ��ȡ���λ��
	 *
	 * @param end
	 * @return
	 */
	protected int getRandomIndex(int end) {
		int index = (int) (Math.random() * end);
		return index;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		if (intent != null) {
			path = intent.getStringExtra("url"); // 歌曲路径
			current = intent.getIntExtra("listPosition", -1); // 当前播放歌曲的在mp3Infos的位置
			msg = intent.getIntExtra("MSG", 0); // 播放信息
			if (msg == ClearConstant.PLAY_MSG) { // 直接播放音乐
				play(0, path);
			} else if (msg == ClearConstant.PAUSE_MSG) { // 暂停
				Log.e("zxb", "音乐停止播放");
				pause();
			} else if (msg == ClearConstant.STOP_MSG) { // 停止
				stop();
			} else if (msg == ClearConstant.CONTINUE_MSG) { // 继续播放
				resume();
			} else if (msg == ClearConstant.PRIVIOUS_MSG) { // 上一首
				previous();
			} else if (msg == ClearConstant.NEXT_MSG) { // 下一首
				next();
			} else if (msg == ClearConstant.PROGRESS_CHANGE) { // 进度更新
				currentTime = intent.getIntExtra("progress", -1);
				play(currentTime, path);
			} else if (msg == ClearConstant.PLAYING_MSG) {
				handler.sendEmptyMessage(1);
			}
		}
	}

	// /**
	// * ��ʼ���������
	// */
	// public void initLrc(){
	// mLrcProcess = new LrcProcess();
	// //��ȡ����ļ�
	// mLrcProcess.readLRC(mp3Infos.get(current).getUrl());
	// //���ش����ĸ���ļ�
	// lrcList = mLrcProcess.getLrcList();
	// PlayerActivity.lrcView.setmLrcList(lrcList);
	// //�л���������ʾ���
	// PlayerActivity.lrcView.setAnimation(AnimationUtils.loadAnimation(PlayerService.this,R.anim.alpha_z));
	// handler.post(mRunnable);
	// }
	// Runnable mRunnable = new Runnable() {
	//
	// @Override
	// public void run() {
	// PlayerActivity.lrcView.setIndex(lrcIndex());
	// PlayerActivity.lrcView.invalidate();
	// handler.postDelayed(mRunnable, 100);
	// }
	// };

	// /**
	// * ����ʱ���ȡ�����ʾ������ֵ
	// * @return
	// */
	// public int lrcIndex() {
	// if(mediaPlayer.isPlaying()) {
	// currentTime = mediaPlayer.getCurrentPosition();
	// duration = mediaPlayer.getDuration();
	// }
	// if(currentTime < duration) {
	// for (int i = 0; i < lrcList.size(); i++) {
	// if (i < lrcList.size() - 1) {
	// if (currentTime < lrcList.get(i).getLrcTime() && i == 0) {
	// index = i;
	// }
	// if (currentTime > lrcList.get(i).getLrcTime()
	// && currentTime < lrcList.get(i + 1).getLrcTime()) {
	// index = i;
	// }
	// }
	// if (i == lrcList.size() - 1
	// && currentTime > lrcList.get(i).getLrcTime()) {
	// index = i;
	// }
	// }
	// }
	// return index;
	// }
	/**
	 * 播放音乐
	 *
	 * @param position
	 */
	private void play(int currentTime, String url) {
		Log.e("xb", "play搞一次");
		try {
			if (mediaPlayer == null) {
//				status = 1;
				mediaPlayer = new MediaPlayer();
				mediaPlayer.setOnCompletionListener(new MediaCompleteListener());
			}
			// initLrc();
			mediaPlayer.setOnCompletionListener(null);
			mediaPlayer.stop();
			mediaPlayer.reset();// 把各项参数恢复到初始状态
			mediaPlayer.setDataSource(url);
//			mediaPlayer.prepareAsync();// 进行异步缓冲
			mediaPlayer.prepare();
			mediaPlayer.setOnPreparedListener(new PreparedListener(currentTime));// 注册一个监听器
			handler.sendEmptyMessage(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 暂停音乐
	 */
	private void pause() {
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			isPause = true;
		}
	}

	private void resume() {
		if (isPause) {
			mediaPlayer.start();
			isPause = false;
		}
	}

	/**
	 * 上一首
	 */
	private void previous() {
		current--;
		if (current < 0) {
			current = 0;
		}
		Intent sendIntent = new Intent(UPDATE_ACTION);
		sendIntent.putExtra("current", current);
		// 发送广播，将被Activity组件中的BroadcastReceiver接收到
		sendBroadcast(sendIntent);
		play(0, mp3Infos.get(current).getPlayURL());
	}

	/**
	 * 下一首
	 */
	private void next() {
		current++;
		if (current > mp3Infos.size() - 1) {
			current = mp3Infos.size() - 1;
		}
		Intent sendIntent = new Intent(UPDATE_ACTION);
		sendIntent.putExtra("current", current);
		// 发送广播，将被Activity组件中的BroadcastReceiver接收到
		sendBroadcast(sendIntent);
		play(0, mp3Infos.get(current).getPlayURL());
	}

	/**
	 * 停止音乐
	 */
	private void stop() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			try {
				mediaPlayer.prepare(); // 在调用stop后如果需要再次通过start进行播放,需要之前调用prepare函数
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(myReceiver);
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
		}
		handler.removeMessages(1);
	}

	/**
	 *
	 * 实现一个OnPrepareLister接口,当音乐准备好的时候开始播放
	 *
	 */
	private final class PreparedListener implements OnPreparedListener {
		private int currentTime;

		public PreparedListener(int currentTime) {
			this.currentTime = currentTime;
		}

		@Override
		public void onPrepared(MediaPlayer mp) {
			mediaPlayer.setOnCompletionListener(new MediaCompleteListener());
			mediaPlayer.start(); // 开始播放
			if (currentTime > 0) { // 如果音乐不是从头播放
				mediaPlayer.seekTo(currentTime);
			}
			Intent intent = new Intent();
			intent.setAction(MUSIC_DURATION);
			duration = mediaPlayer.getDuration();
			intent.putExtra("duration", duration); // 通过Intent来传递歌曲的总长度
			sendBroadcast(intent);
		}
	}

	public class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.e("xb", "action:" + intent.getAction());
			Log.e("xb", "Component: " + intent.getComponent());
			Log.e("xb", "Categories: " + intent.getCategories());
			Log.e("xb", "Data: " + intent.getData());
			Log.e("xb", "DataType: " + intent.getType());
			Log.e("xb", "Component: " + intent.getComponent());
			Log.e("xb", "DataSchema:" + intent.getScheme());

			int control = intent.getIntExtra("control", -1);
			Log.e("xb", control+"");
			switch (control) {
			case 1:
				status = 1; // 将播放状态置为1表示：单曲循环
				break;
			case 2:
				status = 2; // 将播放状态置为2表示：全部循环
				break;
			case 3:
				status = 3; // 将播放状态置为3表示：顺序播放
				break;
			case 4:
				status = 4; // 将播放状态置为4表示：随机播放
				break;
			case 5: // 首次進入服務初始化操作
				mp3Infos = (List<Mp3Info>) intent.getSerializableExtra("musicList");
				break;
			case 0:
				//停止后台的音乐播放
				if (mediaPlayer != null) {					
					mediaPlayer.stop();
					mediaPlayer.release();
					mediaPlayer = null;
				}
				break;
			}

			// String action = intent.getAction();
			// if(action.equals(SHOW_LRC)){
			// current = intent.getIntExtra("listPosition", -1);
			// initLrc();
			// }
		}
	}
   class MediaCompleteListener implements OnCompletionListener{

	@Override
	public void onCompletion(MediaPlayer arg0) {
		Log.e("xb", "播放完成");
		// TODO Auto-generated method stub
		
				if (status == 1) { // // 单曲循环
//					mediaPlayer.start();
//					Log.e("xb", "播放完成");
					mediaPlayer.seekTo(0);
					mediaPlayer.start();
//				    play(0, mp3Infos.get(current).getPicURL());
				} else if (status == 2) { // 全部循环
					current++;
					if (current > mp3Infos.size() - 1) { // 变为第一首的位置继续播放
						current = 0;
					}
					Intent sendIntent = new Intent(UPDATE_ACTION);
					sendIntent.putExtra("current", current);
					// 发送广播，将被Activity组件中的BroadcastReceiver接收到
					sendBroadcast(sendIntent);
					Log.e("xb", "播放完成："+current);
					path = mp3Infos.get(current).getPlayURL();
					Log.e("xb", "mp3Infos："+mp3Infos.size());
					play(0, path);
				} else if (status == 3) { // 顺序播放
					current++; // 下一首位置
					if (current <= mp3Infos.size() - 1) {
						Intent sendIntent = new Intent(UPDATE_ACTION);
						sendIntent.putExtra("current", current);
						// 发送广播，将被Activity组件中的BroadcastReceiver接收到
						sendBroadcast(sendIntent);
						
						path = mp3Infos.get(current).getPlayURL();
						play(0, path);
						Log.e("xb", "status:3__"+path);
					} else {
						mediaPlayer.seekTo(0);
						current = 0;
						Intent sendIntent = new Intent(UPDATE_ACTION);
						sendIntent.putExtra("current", current);
						
						sendIntent.putExtra("refresh", true);
						// 发送广播，将被Activity组件中的BroadcastReceiver接收到
						sendBroadcast(sendIntent);

					}
				} else if (status == 4) { // 随机播放
					current = getRandomIndex(mp3Infos.size() - 1);
					System.out.println("currentIndex ->" + current);
					Intent sendIntent = new Intent(UPDATE_ACTION);
					sendIntent.putExtra("current", current);
					// 发送广播，将被Activity组件中的BroadcastReceiver接收到
					sendBroadcast(sendIntent);
					
					path = mp3Infos.get(current).getPlayURL();
					play(0, path);
				}
			}   
   }
}
