/**
 * @author xujifu
 * @copyright clear
 * @date 2014-06-20
 * @description 点播播放界面
 */
package com.clearcrane.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.log.ClearLog;
import com.clearcrane.schedule.DateUtil;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.LogUtils;
import com.clearcrane.util.VideoInfo;
import com.clearcrane.vod.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class VoDMovieView extends VoDBaseView {

	private final static int STATUE_PLAY = 1;
	private final static int STATUE_PAUSE = 2;
	private final static int SEEK_SEGMENT_NUM = 100;
	private final static int SEEKBAR_DELAY = 5000;
	private final static int MSG_SEEKBAR_INVADATE = 101;

	private String movieUrl;
	private String movieName;
	private int statue = -1;
	private TextView mMovieName;// 影片名称
	private ImageView mMovieStop;// 暂停图片
	private LinearLayout mMovieControl;// 视频控制条
	private TextView mMovieTakeTime;// 视频播放时间
	private TextView mMovieLeftTime;// 视频剩余时间
	private SeekBar mMovieProgress;// 进度条
	private TextView mMovieSign; // 插播标示
	private Timer timer = null;
	private int mPosition;
	private int currentPosition; //当前播放电影在列表中是第几个
	private ArrayList<VideoInfo> videoSets = new ArrayList<>();//接收的所有电影数据
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == ClearConstant.JUMP_MOVIE_VIEW) {
                //返回的时候重新加载视频并且快进至上次保存的地方
				Log.i(TAG, "handleMessage JUMP_MOVIE_VIEW:" + url + " pos " +mPosition);
				VoDViewManager.getInstance().stopMovieVideo();
				VoDViewManager.getInstance().showMovieVideo();
				VoDViewManager.getInstance().setMoviePreparedListener(listener);
				VoDViewManager.getInstance().getVideoView().setOnCompletionListener(onCompletionListener);
				VoDViewManager.getInstance().startMovieVideo(movieUrl);
				VoDViewManager.getInstance().movieRequestFocus();
				VoDViewManager.getInstance().movieSeekTo((mPosition));
				VoDViewManager.getInstance().pauseMovieVideo();
				statue = STATUE_PAUSE;
				showControlProgress();
				mMovieStop.setVisibility(View.VISIBLE);
				mHandler.sendEmptyMessage(MSG_SEEKBAR_INVADATE);
				saveStatue();
			} else if (msg.what == ClearConstant.JUMP_LIVE_VIEW) {
				Log.i(TAG, "handleMessage JUMP_LIVE_VIEW :" + url);
				VoDViewManager.getInstance().startMovieVideo(movieUrl);
				VoDViewManager.getInstance().movieRequestFocus();
			}
		};
	};

	private boolean controlIsShowed = false;
	private Timer controlTimer = new Timer();
	public controlTask mControlTask = null;
	public static SharedPreferences activitySharePre;
	public static SharedPreferences INTERCUTSharePre;
	public static SharedPreferences VideoSetsSp;
//	private long endTime;// 插播的结束时间
//	private long startTime;// 插播的开始时间
	private long playingTime;// 插播点播资源时已播放的时间
//	private long movieDuration = 0;;// 点播视频时长
	private Timer checkTimer = null;
	public static final String TAG = "VoDMovieView";
    private OnCompletionListener onCompletionListener = null;
	/* data provider */
	/* build the view layout/element */
	/* start animation */
	public void init(Context ctx, String u, String name) {
		super.init(ctx, u);
		
		view = LayoutInflater.from(context).inflate(R.layout.movie_view, null);
		isInserted = false;
		movieUrl = u;
		movieName = name;
		
		
		initLayoutInXml();
		initSharePre();
		String videoSetsJson = VideoSetsSp.getString(ClearConstant.VIDEOSETSJSON,"");
		analyseJson(videoSetsJson);
		saveStatue();
	}

	public void init(Context ctx, String u, String name, Boolean inserted, long time) {
		super.init(ctx, u);
		view = LayoutInflater.from(context).inflate(R.layout.movie_view, null);
		movieUrl = u;
		movieName = name;
		isInserted = false;
//		isInserted = inserted; // 插播点播源的时候,需要在构造函数中初始化插播标识
		playingTime = time; // 默认为0，即从头播放
		initSharePre();
		initLayoutInXml();
		saveStatue();
	}

	private void saveStatue() {
		Editor editor = activitySharePre.edit();
		editor.putString("video_url", movieUrl);
		editor.putInt(ClearConstant.Play_Statue, statue);
		editor.putString(ClearConstant.Movie_NAME, movieName);
		editor.commit();
	}

	private void initSharePre() {

		activitySharePre = context.getSharedPreferences(ClearConstant.Activity_FILE, Context.MODE_PRIVATE);
		INTERCUTSharePre = context.getSharedPreferences(ClearConstant.INTERCUT_FILE, Context.MODE_PRIVATE);
		VideoSetsSp = context.getSharedPreferences(ClearConstant.VIDEOSETS, Context.MODE_PRIVATE);
	}

	private void initLayoutInXml() {
		// TODO Auto-generated method stub
		mMovieName = (TextView) view.findViewById(R.id.movie_name);
		mMovieTakeTime = (TextView) view.findViewById(R.id.movie_take_time);
		mMovieLeftTime = (TextView) view.findViewById(R.id.movie_left_time);
		mMovieProgress = (SeekBar) view.findViewById(R.id.movie_progress);
		mMovieControl = (LinearLayout) view.findViewById(R.id.movie_control);
		mMovieStop = (ImageView) view.findViewById(R.id.movie_stop);

		mMovieName.setText(movieName);
		mMovieStop.setVisibility(View.INVISIBLE);
		mMovieControl.setVisibility(View.GONE);
		
		onCompletionListener = new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer arg0) {
				// TODO Auto-generated method stub
				Log.e("xb", "onCompletion");
//				if(currentPosition < videoSets.size()-1){
//					currentPosition++;
//					arg0.stop();
//					arg0.reset();
//					mMovieName.setText(videoSets.get(currentPosition).name);
//					VoDViewManager.getInstance().startMovieVideo(videoSets.get(currentPosition).source_url);
//				}else{
					onKeyBack();
//				}
			}
		};
		VoDViewManager.getInstance().showMovieVideo();
		Log.i(TAG, "add listener!");
		VoDViewManager.getInstance().setMoviePreparedListener(listener);
		VoDViewManager.getInstance().getVideoView().setOnCompletionListener(onCompletionListener);

		/**
		 * 获取要求开始播放的时间，与现有时间作比较，到指定时间后放
		 * 跳到视频当前进度播（点播），需要进行判断，区分是插播的点播还是普通点播,视频时长需要播放器开始播放后才能获取
		 * 
		 * ;
		 **/
//		if (isInserted == true) {
//			Log.i(TAG, "Start runable");
//			handler.post(runnable);
//		} else {
		//播放视频并且保存状态
			VoDViewManager.getInstance().startMovieVideo(movieUrl);
			VoDViewManager.getInstance().movieRequestFocus();
			statue = STATUE_PLAY;				
//		}

	}

	public long getSecDuration(String time) {
		long curServerTime = DateUtil.getCurrentTimeMillSecond();
		long remainTime = DateUtil.getTimeMillSecondFromDateStr(time) - curServerTime;
//		SimpleDateFormat formatter = new SimpleDateFormat(DateUtil.DATE_ZONE_FORMAT);
//		Date date = new Date(curServerTime);
//		Date date1 = new Date(DateUtil.getInstance().parseDateToLong(time));
//		Log.i(TAG, "serv:" + formatter.format(date) + "endtime:" + date1);
		return remainTime;
	}

	private CharSequence getTimeFormatValue(long time) {
		return MessageFormat.format("{0, number, 00}:{1, number, 00}:{2, number, 00}", time / 1000 / 60 / 60,
				time / 1000 / 60 % 60, time / 1000 % 60);
	}

	private TimerTask task = new TimerTask() {
		public void run() {
			Message message = new Message(); 
			message.what = 1;
			mHandler.sendMessage(message);
		}
	};

	private OnPreparedListener listener = new OnPreparedListener() {

		@Override
		public void onPrepared(MediaPlayer mp) {
			// TODO Auto-generated method stub
			Log.i(TAG, "On prepared!");
			
			//初始化进度条更新任务
//			if (timer == null) {
//				timer = new Timer();
//				timer.schedule(task, 1000, 1000);
//			}
			Message message = new Message(); 
			message.what = 1;
			mHandler.sendMessage(message);

			Log.i(TAG, "On prepared! isInserted: " + isInserted);
				
				mMovieName.setVisibility(View.VISIBLE);
				mMovieControl.setVisibility(View.VISIBLE);
				controlIsShowed = true;
				if (mControlTask == null) {
					mControlTask = new controlTask();
					controlTimer.schedule(mControlTask, 3 * 1000);
				} else {
					mControlTask.cancel();
					mControlTask = new controlTask();
					controlTimer.schedule(mControlTask, 3 * 1000);
				}			 
//			}
		}
	};

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1: //MSG_SEEKBAR_INVADATE
				// super.handleMessage(msg);
				//显示视频的时长和不断更新已播放时长
				long position = VoDViewManager.getInstance().getMovieCurrentPosition();
				long duration = VoDViewManager.getInstance().getMovieDuration();
				mMovieTakeTime.setText(getTimeFormatValue(position));
				mMovieLeftTime.setText(getTimeFormatValue(duration));

				mMovieProgress.setProgress((int) (((double) position / (double) duration) * mMovieProgress.getMax()));
				Log.i(TAG, "position：" + position + " duration:" + duration + " mMovieProgress.getMax（）："
						+ mMovieProgress.getMax());
				break;
			case 2:
				//隐藏播放条
				if (statue == STATUE_PLAY)
					hideControlProgress();
				break;
				//temp seek to 
			case 3:
				VoDViewManager.getInstance().movieSeekTo(msg.arg1);
				break;
			default:
				break;
			}
		}

	};

	
	
	
	@Override
	public void hide() {
		// TODO Auto-generated method stub
		super.hide();
		saveCurrentVideoStatus();
	}

	//当从插播或者计划播放中返回的时候返回继续播放
	@Override
	public void back() {
		// TODO Auto-generated method stub
		super.back();
		handler.sendEmptyMessage(ClearConstant.JUMP_MOVIE_VIEW);
	}

	/*
	 * TODO,FIXME
	 * sometime temp will be 0!!!!
	 * in fact I do not the reason!
	 * there must be someting wrong
	 * maybe getting the total duration of the movie casts too long
	 * here,only if current > old,changes it
	 * 保存视频状态
	 */
	private void saveCurrentVideoStatus(){
		Log.e(TAG,"saveCurrentVideoStatus mPositon old " + mPosition);
		int temp = (int) VoDViewManager.getInstance().getMovieCurrentPosition();
		if (temp > mPosition)
			mPosition = temp;
		Log.e(TAG,"saveCurrentVideoStatus mPositon new " + mPosition);
		VoDViewManager.getInstance().hideMovieVideo();
	}
	
	
	private void seekto(int postion){
		Message msg = mHandler.obtainMessage(3);
		msg.arg1 = postion;
		mHandler.removeMessages(3);
		mHandler.sendMessageDelayed(msg, 500);
	}
	
	public boolean onKeyDpadLeft() {
		Log.i("key", "in video left " + statue);
		showControlProgress();

		long position = VoDViewManager.getInstance().getMovieCurrentPosition();
		long duration = VoDViewManager.getInstance().getMovieDuration();

		long targetPos = position - duration / SEEK_SEGMENT_NUM;
		if (targetPos < 0)
			targetPos = 0;

		VoDViewManager.getInstance().movieSeekTo((int) (targetPos));
		mMovieTakeTime.setText(getTimeFormatValue(targetPos));
		mMovieLeftTime.setText(getTimeFormatValue(duration));
		mMovieProgress.setProgress((int) (((double) targetPos / (double) duration) * mMovieProgress.getMax()));
		return true;
	}

	public boolean onKeyDpadRight() {
		Log.i("key", "in video right " + statue);
		showControlProgress();

		long position = VoDViewManager.getInstance().getMovieCurrentPosition();
		long duration = VoDViewManager.getInstance().getMovieDuration();

		long targetPos = position + duration / SEEK_SEGMENT_NUM;
		if (targetPos > duration)
			targetPos = duration;

		VoDViewManager.getInstance().movieSeekTo((int) (targetPos));
		mMovieTakeTime.setText(getTimeFormatValue(targetPos));
		mMovieLeftTime.setText(getTimeFormatValue(duration));
		mMovieProgress.setProgress((int) (((double) targetPos / (double) duration) * mMovieProgress.getMax()));
		return true;
	}

	public boolean onKeyEnter() {
		// is a Bug, see initLayoutInXml()
		Log.e(TAG,"onKeyEnter state" + statue);
		switch (statue) {
		case STATUE_PLAY:
			VoDViewManager.getInstance().pauseMovieVideo();
			mMovieStop.setVisibility(View.VISIBLE);
			showControlProgress();
			statue = STATUE_PAUSE;
			saveStatue();
			break;
		case STATUE_PAUSE:
			// TODO, FIXME, check is seek happend
			VoDViewManager.getInstance().playMovieVideo();
			mMovieStop.setVisibility(View.INVISIBLE);
			showControlProgress();
			statue = STATUE_PLAY;
			saveStatue();
			break;
		default:
			break;
		}
		return true;
	}

	public boolean onKeyBack() {
		Log.e("winter", "onkeyback");
		VoDViewManager.getInstance().stopMovieVideo();

		VoDViewManager.getInstance().playBackgroundVideo();
		VoDViewManager.getInstance().hideMovieVideo();

		statue = -1;
		saveStatue();
		
//		VoDViewManager.getInstance().popForegroundView();
		
		LogUtils.sendLogEnd(mApp, "点播", "视频", movieName);
		return super.onKeyBack();
	}

	public void showControlProgress() {
		if (!controlIsShowed) {
			Log.i("key", "in video right set visible " + statue);
			mMovieName.setVisibility(View.VISIBLE);
			mMovieControl.setVisibility(View.VISIBLE);
			controlIsShowed = true;
		}

		if (mControlTask == null) {
			mControlTask = new controlTask();
			controlTimer.schedule(mControlTask, 3 * 1000);
		} else {
			mControlTask.cancel();
			mControlTask = new controlTask();
			controlTimer.schedule(mControlTask, 3 * 1000);
		}
	}

	public void hideControlProgress() {
		if (controlIsShowed) {
			mMovieName.setVisibility(View.INVISIBLE);
			mMovieControl.setVisibility(View.INVISIBLE);
			controlIsShowed = false;
		}

	}

	class controlTask extends TimerTask {
		public void run() {
			Message message = new Message();
			message.what = 2;
			mHandler.sendMessage(message);
		}
	};
    private void analyseJson(String videoSetJson){
    	try {
			JSONTokener jsonParser = new JSONTokener(videoSetJson);
			JSONObject mainViewObj = (JSONObject) jsonParser.nextValue();
			String labelString = mainViewObj.getString("Label");
			String introduction = mainViewObj.getString("Introduction");
			JSONArray contentArray = (JSONArray) mainViewObj.getJSONArray("Content");
			for (int i = 0; i < contentArray.length(); i++) {
				JSONObject objecttmp = (JSONObject) contentArray.opt(i);
				VideoInfo videoInfo = new VideoInfo();
				videoInfo.name = objecttmp.getString("name");
				videoInfo.videoId = objecttmp.getInt("index");
				videoInfo.nextVideoId = objecttmp.getInt("Next_Video_index");
				videoInfo.source_url = ClearConfig.getJsonUrl(context, objecttmp.getString("Video_URL"));
				videoSets.add(videoInfo);
				// or add with index?
				// videoSets.add(i,videoInfo);
			}
			//确定当前播放视频在视频集合中得位置索引
			for(int i=0;i<videoSets.size();i++){
				if(movieUrl.equalsIgnoreCase(videoSets.get(i).source_url)){
				    currentPosition = i;
				    break;
				}
			}
    	}catch (JSONException e) {
			ClearLog.LogError("BROSWER\tLoad\tFAIL\t0ms\t" + url);
			e.printStackTrace();
		}
    }

	public Handler getHandler() {
		return handler;
	}

	public void setPosition(int pos) {
		mPosition = pos;
//		VoDViewManager.getInstance().pauseMovieVideo();
//		showControlProgress();
		statue = STATUE_PAUSE;
//		saveStatue();
	}
}
