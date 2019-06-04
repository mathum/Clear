package com.clearcrane.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clearcrane.adapter.ListViewDataAdapter;
import com.clearcrane.adapter.MusicPlayListViewHolder;
import com.clearcrane.constant.PlayerFinal;
import com.clearcrane.entity.MusicInfo;
import com.clearcrane.entity.MusicResult;
import com.clearcrane.interfaces.AudioVolumeChangeListener;
import com.clearcrane.provider.MaterialRequest;
import com.clearcrane.provider.MaterialRequest.OnCompleteListener;
import com.clearcrane.service.OnModeChangeListener;
import com.clearcrane.service.OnPlayerStateChangeListener;
import com.clearcrane.service.OnSeekChangeListener;
import com.clearcrane.service.PlayerService;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.TipDialog;
import com.clearcrane.vod.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 音乐播放页面
 * 
 * @author SlientLeaves 2016年8月3日 下午2:39:17
 */
public class VodMusicPlayView extends VoDBaseView {

	private static final String TAG = VodMusicPlayView.class.getSimpleName();
	private static final int[] mPlayModeDrawable = { R.drawable.ic_media_mode_loop, R.drawable.ic_media_mode_random,
			R.drawable.ic_media_mode_single, R.drawable.ic_media_mode_all };
	public static final int DEFAULT_MSG_DELAY_TIME = 2000;
	private static final int IB_PLAYMODE = -2;
	private static final int IB_PLAYPRE = -1;
	private static final int IB_PLAYRESUME = 0;
	private static final int IB_PLAYNEXT = 1;
	private static final int IB_IBPLAYLIST = 2;
	private static final int DISPPER_THE_VOLUME_LAYOUT = 4;
	private static final int ITEM_CLICK = 5;
	private static final int ITEM_SELECT = 6;

	private TextView mTvPlayCurrentTime;
	private TextView mTvPlayDuration;
	// 播放进度条
	private ProgressBar mPbPlayingProgress;
	private TextView mTvPlayTrackName;

	private TextView mTvPlayArtist;

	private TextView mTvPlayAlbum;

	private MediaBarViewGroup mMediaBarViewGroup;

	private MediaBtnScrollAnimView mMediaBtnScrollAnimView;

	private LinearLayout mLLPlayVolume;

	private ProgressBar mPbPlayVolume;

	private TextView mVodMusicLyricView;
	private TextView mTvPlayListTitle;
	private ListView mLvPlayList;
	private ListViewDataAdapter<MusicInfo> mListViewDataAdapter;
	private ArrayList<MusicInfo> mMusicInfoList;

	private ImageButton mIbPlayLoopMode;
	private ImageButton mIbPlayPre;
	private ImageButton mIbPlayResume;
	private ImageButton mIbPlayNext;
	private ImageButton mIbPlayList;
	private RelativeLayout mRlPlayResume;
	private ImageView mIvPlayAnim;

	private int mMediaBarFocusPosition;
	public static boolean mIsShowPlayList;
	private float mMinScale = 1.125f; // 设置一个缩放的常量

	public enum PlaybackMode {
		MEDIA_MODE_LOOP, MEDIA_MODE_RANDOM, MEDIA_MODE_SINGLE, MEDIA_MODE_ALL
	}

	private int mPlayMode;// 循环播放、随机播放、单曲循环、顺序播放

	private Calendar begin = null;

	// 用于开启服务
	private Intent mServiceIntent;

	// 回调函数更新UI
	private OnPlayerStateChangeListener stateChangeListener;
	private OnSeekChangeListener seekChangeListener;
	private OnModeChangeListener modeChangeListener;

	private int mListViewItemClickPos;
	private int mHistoryListViewItemClickPos = -1;
	private View mLastSelectedItemView;
	private boolean isFirstLoad = true;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			mLLPlayVolume.setVisibility(View.GONE);

			switch (msg.what) {
			case DISPPER_THE_VOLUME_LAYOUT:

				if (mLLPlayVolume.getVisibility() == View.VISIBLE)
					mLLPlayVolume.setVisibility(View.GONE);

				break;

			default:
				break;
			}
		};
	};

	public void init(Context context, String url) {
		begin = Calendar.getInstance();
		this.context = context;
		this.url = url;
		initView();
		initData();

		// 播放模式改变监听事件
		modeChangeListener = new OnModeChangeListener() {

			@Override
			public void onModeChange(int mode) {

			}

		};

		// 进度条监听
		seekChangeListener = new OnSeekChangeListener() {

			@Override
			public void onSeekChange(int progress, int max, String time, String duration) {

				mTvPlayCurrentTime.setText(time);
				mPbPlayingProgress.setMax(max);
				mPbPlayingProgress.setProgress(progress);
				mTvPlayDuration.setText(duration);
			}

		};

		// 播放状态改变监听
		stateChangeListener = new OnPlayerStateChangeListener() {

			@Override
			public void onStateChange(int state, int mode, List<MusicInfo> musicList, int position) {

				if (mMusicInfoList != null) {

					MusicInfo musicInfo = mMusicInfoList.get(position);

					String dur = spiltPlayTimeDuration(musicInfo.getDuration());
					mTvPlayDuration.setText(dur);
					mTvPlayTrackName.setText(musicInfo.getAlbum());
					mTvPlayArtist.setText(musicInfo.getSinger());
					mTvPlayAlbum.setText(musicInfo.getSummary());

//					ImageUtil.displayImage(musicInfo.getPicurl_abs_path(), mIbPlayCover);
//					mLvPlayList.setSelection(position);

				}

			}

		};

		PlayerService.registerStateChangeListener(stateChangeListener);
		PlayerService.registerSeekChangeListener(seekChangeListener);
		PlayerService.registerModeChangeListener(modeChangeListener);

		mLvPlayList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mListViewItemClickPos = position;
				sendPlayerServiceBroadCase(PlayerService.ACTION_PLAY_ITEM);
			}

		});

		mLvPlayList.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				changeItemStatus(view, position, true);
				if (mLastSelectedItemView != null) {
					changeItemStatus(mLastSelectedItemView, position, false);
				}

				mLastSelectedItemView = view;
//
//				if (isFirstLoad) {
//					mHistoryListViewItemClickPos = 0;
//					isFirstLoad = false;
//					showPlayingListImage(position, false);
//				}
				
				
//				if (mHistoryListViewItemClickPos != position) {
//					showPlayingListImage(mHistoryListViewItemClickPos, true);
//					mHistoryListViewItemClickPos = position;
//					showPlayingListImage(position, false);
//				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		startPlayerService();

		mIbPlayResume.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {

				mMediaBtnScrollAnimView.scrollAnimTo(mRlPlayResume, true);
				mIbPlayResume.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}

		});

//		String requestUrl = "http://192.168.0.30/nativevod/now/Music/json/Music_48.json";
		getDataFromServer(this.url);
	}

	/**
	 * 显示播放图片
	 * 
	 * @param position
	 */
	protected void showPlayingListImage(int position, boolean isHistoryItemView) {
		View child = mLvPlayList.getChildAt(position);
		TextView mTvItemPlayListIndex = (TextView) child.findViewById(R.id.tv_item_play_list_index);
		ImageView mIvItemPlayListPlaying = (ImageView) child.findViewById(R.id.iv_item_play_list_playing);

		if (isHistoryItemView) {
			mTvItemPlayListIndex.setVisibility(View.VISIBLE);
			mIvItemPlayListPlaying.setVisibility(View.INVISIBLE);
			AnimationDrawable animation = (AnimationDrawable) mIvItemPlayListPlaying.getBackground();
			if (animation != null && animation.isRunning()) {
				animation.stop();
			}

		} else {
			mTvItemPlayListIndex.setVisibility(View.INVISIBLE);
			mIvItemPlayListPlaying.setVisibility(View.VISIBLE);
			mIvItemPlayListPlaying.setBackgroundResource(R.anim.item_play_list_playing_focus);
			AnimationDrawable animation = (AnimationDrawable) mIvItemPlayListPlaying.getBackground();
			animation.setOneShot(false);
			animation.start();
		}
	}

	/**
	 * 改变listview选中item的状态
	 * 
	 * @param paramView
	 * @param type
	 * @param flag
	 */
	protected void changeItemStatus(View paramView, int position, boolean flag) {

		TextView mTvItemPlayListIndex = (TextView) paramView.findViewById(R.id.tv_item_play_list_index);
		TextView mTvItemPlayListPoint = (TextView) paramView.findViewById(R.id.tv_item_play_list_point);
		TextView mTvItemPlayListTrackName = (TextView) paramView.findViewById(R.id.tv_item_play_list_track_name);
		TextView mTvItemPlayListArtists = (TextView) paramView.findViewById(R.id.tv_item_play_list_artists);

		if (flag) {

			mTvItemPlayListIndex.setTextColor(context.getResources().getColor(R.color.grass_green));
			mTvItemPlayListPoint.setTextColor(context.getResources().getColor(R.color.grass_green));
			mTvItemPlayListTrackName.setTextColor(context.getResources().getColor(R.color.grass_green));
			mTvItemPlayListArtists.setTextColor(context.getResources().getColor(R.color.grass_green));

		} else {

			mTvItemPlayListIndex.setTextColor(context.getResources().getColor(R.color.white));
			mTvItemPlayListPoint.setTextColor(context.getResources().getColor(R.color.white));
			mTvItemPlayListTrackName.setTextColor(context.getResources().getColor(R.color.white));
			mTvItemPlayListArtists.setTextColor(context.getResources().getColor(R.color.white));
		}

	}
	//
	// /**
	// * 改变listview选中item的状态
	// *
	// * @param paramView
	// * @param type
	// * @param flag
	// */
	// protected void changeItemStatus(View paramView, int type, int position,
	// boolean flag) {
	//
	// TextView mTvItemPlayListIndex = (TextView)
	// paramView.findViewById(R.id.tv_item_play_list_index);
	// ImageView mIvItemPlayListPlaying = (ImageView)
	// paramView.findViewById(R.id.iv_item_play_list_playing);
	// TextView mTvItemPlayListPoint = (TextView)
	// paramView.findViewById(R.id.tv_item_play_list_point);
	// TextView mTvItemPlayListTrackName = (TextView)
	// paramView.findViewById(R.id.tv_item_play_list_track_name);
	// TextView mTvItemPlayListArtists = (TextView)
	// paramView.findViewById(R.id.tv_item_play_list_artists);
	//
	// if (type == ITEM_CLICK) {
	//
	// mListViewItemPos = position;
	//
	// if (flag) {
	// mTvItemPlayListIndex.setVisibility(View.INVISIBLE);
	// mIvItemPlayListPlaying.setBackgroundResource(R.anim.item_play_list_playing_focus);
	// AnimationDrawable animation = (AnimationDrawable)
	// mIvItemPlayListPlaying.getBackground();
	// animation.setOneShot(false);
	// animation.start();
	// // mIvItemPlayListPlaying.setVisibility(View.VISIBLE);
	//
	// } else {
	// mTvItemPlayListIndex.setVisibility(View.VISIBLE);
	// AnimationDrawable animation = (AnimationDrawable)
	// mIvItemPlayListPlaying.getBackground();
	// animation.stop();
	// // mIvItemPlayListPlaying.setVisibility(View.INVISIBLE);
	// }
	//
	// } else if (type == ITEM_SELECT) {
	//
	// if (flag) {
	//
	// mTvItemPlayListIndex.setTextColor(context.getResources().getColor(R.color.grass_green));
	// mTvItemPlayListPoint.setTextColor(context.getResources().getColor(R.color.grass_green));
	// mTvItemPlayListTrackName.setTextColor(context.getResources().getColor(R.color.grass_green));
	// mTvItemPlayListArtists.setTextColor(context.getResources().getColor(R.color.grass_green));
	//
	// } else {
	//
	// mTvItemPlayListIndex.setTextColor(context.getResources().getColor(R.color.white));
	// mTvItemPlayListPoint.setTextColor(context.getResources().getColor(R.color.white));
	// mTvItemPlayListTrackName.setTextColor(context.getResources().getColor(R.color.white));
	// mTvItemPlayListArtists.setTextColor(context.getResources().getColor(R.color.white));
	// }
	// }
	//
	// }

	/**
	 * 对歌曲的秒数进行分割
	 * 
	 * @param duration
	 * @return
	 */
	protected String spiltPlayTimeDuration(String sedonds) {
		int[] dur = new int[2];
		String result = null;

		int totalSeconds = Integer.parseInt(sedonds);
		dur[0] = totalSeconds / 60;
		result = dur[0] + ":";
		dur[1] = totalSeconds - dur[0] * 60;
		if (dur[1] < 10) {
			result += "0";
		}

		result += dur[1];
		return result;
	}

	/**
	 * 开启播放器服务
	 */
	private void startPlayerService() {
		mServiceIntent = new Intent(context, PlayerService.class);
		context.startService(mServiceIntent);
	}

	/**
	 * 从服务器获取数据
	 * 
	 * @param requestUrl
	 */
	private void getDataFromServer(String requestUrl) {
		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(DataJsonListen);
		mr.execute(requestUrl);
	}

	private OnCompleteListener DataJsonListen = new OnCompleteListener() {

		@Override
		public void onDownloaded(Object result) {

			String mResultJson = (String) result;
			if (mResultJson == null) {
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
				Gson gson = new Gson();
				java.lang.reflect.Type type = new TypeToken<MusicResult>() {
				}.getType();

				MusicResult musicResult = gson.fromJson(mResultJson, type);

				if (musicResult.getContent() != null) {
					mMusicInfoList = musicResult.getContent();

					for (int i = 0; i < mMusicInfoList.size(); i++) {
						MusicInfo musicInfo = mMusicInfoList.get(i);
						musicInfo.setSeq(String.valueOf(musicInfo.getAudioNum()));
						musicInfo.setPlayURL(mMusicInfoList.get(i).getAudioPath());
						musicInfo.setPlayURL_abs_path(mMusicInfoList.get(i).getAudioPath());
						musicInfo.setDuration(String.valueOf(309));
						musicInfo.setAlbum("未命名"+i);
						musicInfo.setSinger("未知"+i);
						musicInfo.setSummary("未命名"+i);
					}

					// 因为arraylist 有序，上述代码生成升序的序号，此段代码可以注释，如果需要复杂排序，可以解开，自定义实现
					// Collections.sort(mMusicInfoList, new
					// Comparator<MusicInfo>(){
					//
					// @Override
					// public int compare(MusicInfo lhs, MusicInfo rhs) {
					//
					// int lhsId = Integer.parseInt(lhs.getSeq());
					// int rhsId = Integer.parseInt(rhs.getSeq());
					//
					// if(lhsId - lhsId > 0){
					// return 1;
					// }else if(lhsId - rhsId < 0){
					// return -1;
					// }else{
					// return 0;
					// }
					//
					// }
					//
					// });

//					sendPlayerServiceBroadCase(PlayerService.ACTION_PLAY_ITEM);
					mLvPlayList.setAdapter(mListViewDataAdapter);
					mListViewDataAdapter.update(mMusicInfoList);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		public void onComplete(boolean result) {

		}

	};
	private ImageButton mIbPlayCover;

	/**
	 * 初始化布局
	 */
	private void initView() {
		view = View.inflate(this.context, R.layout.play, null);
		mMediaBarViewGroup = (MediaBarViewGroup) view.findViewById(R.id.layout_play_bar_ctrl);
		mMediaBtnScrollAnimView = (MediaBtnScrollAnimView) view.findViewById(R.id.mbsav_play_btn_anim);

		mIbPlayLoopMode = (ImageButton) view.findViewById(R.id.ibt_play_loop_mode);
		mIbPlayPre = (ImageButton) view.findViewById(R.id.ibt_play_pre);

		mRlPlayResume = (RelativeLayout) view.findViewById(R.id.layout_play_playbtn);
		mIvPlayAnim = (ImageView) view.findViewById(R.id.ibt_play_play_anim);
		mIbPlayResume = (ImageButton) view.findViewById(R.id.ibt_play_resume);
		mIbPlayNext = (ImageButton) view.findViewById(R.id.ibt_play_next);
//		mIbPlayLyric = (ImageButton) view.findViewById(R.id.ibt_play_lyric);
		mIbPlayList = (ImageButton) view.findViewById(R.id.ibt_play_list);

		mTvPlayCurrentTime = (TextView) view.findViewById(R.id.tv_play_current_time);
		mTvPlayDuration = (TextView) view.findViewById(R.id.tv_play_duration);
		mPbPlayingProgress = (ProgressBar) view.findViewById(R.id.pb_play_playing_progress);

		mTvPlayTrackName = (TextView) view.findViewById(R.id.tv_play_track_name);
		mTvPlayArtist = (TextView) view.findViewById(R.id.tv_play_artists);
		mTvPlayAlbum = (TextView) view.findViewById(R.id.tv_play_album);

		mLLPlayVolume = (LinearLayout) view.findViewById(R.id.layout_play_volume);
		mPbPlayVolume = (ProgressBar) view.findViewById(R.id.pb_play_volume);

		// mVodMusicLyricView = (VodMusicLyricView)
		// view.findViewById(R.id.lv_play_lyric);
		mVodMusicLyricView = (TextView) view.findViewById(R.id.lv_play_lyric);
		mTvPlayListTitle = (TextView) view.findViewById(R.id.tv_play_list_title);
		mLvPlayList = (ListView) view.findViewById(R.id.lv_play_list);

		mIbPlayCover = (ImageButton) view.findViewById(R.id.iv_play_cover);
	}

	/**
	 * 初始化数据
	 */
	private void initData() {

		// mTvPlayTrackName.setText("落叶无声");
		// mTvPlayArtist.setText("追梦痴子心");
		// mTvPlayAlbum.setText("落叶无声");

		mListViewDataAdapter = new ListViewDataAdapter<MusicInfo>(null);
		mListViewDataAdapter.setViewHolderClass(null, MusicPlayListViewHolder.class, context);

		mMediaBarViewGroup.setAudioVolumeListener(new AudioVolumeChangeListener() {

			@Override
			public void updateVolume(int currentVolume, int maxVolume) {

				mLLPlayVolume.setVisibility(View.VISIBLE);
				mPbPlayVolume.setProgress(currentVolume);
				mPbPlayVolume.setMax(maxVolume);
				mHandler.sendEmptyMessageDelayed(DISPPER_THE_VOLUME_LAYOUT, DEFAULT_MSG_DELAY_TIME);
			}

		});

		mVodMusicLyricView.setText("随心而动，音悦人生");
		mVodMusicLyricView.setTextSize(24);

	}

	/**
	 * 切换播放模式
	 * 
	 * @param paramInt
	 * @return
	 */
	public int changePlayMode() {
		Log.d(TAG, "切换播放模式前的模式是" + mPlayMode);

		if (mPlayMode >= 0 && mPlayMode < 3) {
			mPlayMode++;

			return mPlayMode;
		}

		mPlayMode = 0;
		return mPlayMode;
	}

	/**
	 * 设置播放按钮选中状态，用不同图片区别是否选中状态
	 */
	public void setPlayResumeBgStatus(boolean isFocus) {

		if (isFocus)
			mIvPlayAnim.setImageResource(R.drawable.bg_play_resume_bg_1);
		else
			mIvPlayAnim.setImageResource(R.drawable.bg_play_resume_bg_1_normal);

	}

	/**
	 * 改变底部媒体栏焦点变化时图片的改变
	 * 
	 * @param paramImageButton
	 * @param normalDrawable
	 * @param focusDrawable
	 * @param isFocus
	 */
	private void changeMediaBarImageButtonStatus(ImageButton paramImageButton, int normalDrawable, int focusDrawable,
			boolean isFocus) {

		if (isFocus)
			paramImageButton.setImageDrawable(context.getResources().getDrawable(focusDrawable));
		else
			paramImageButton.setImageDrawable(context.getResources().getDrawable(normalDrawable));
	}

	@Override
	public boolean onKeyEnter() {

		if (!mIsShowPlayList) {
			responseImageButtonClickEvent(mMediaBarFocusPosition);
		}
		return true;
	}

	@Override
	public boolean onKeyBack() {

		if (mIsShowPlayList) {

			mIsShowPlayList = false;
			mMediaBarFocusPosition--;
			moveScrollAnim(mMediaBarFocusPosition);
			showRightAreaContent(false);
			return true;
		}

		PlayerService.unRegisterStateChangeListener(stateChangeListener);
		PlayerService.unRegisterSeekChangeListener(seekChangeListener);
		PlayerService.unRegisterModeChangeListener(modeChangeListener);
        
		return super.onKeyBack();
	}

	/**
	 * 移动背景滑块到目标位置
	 * 
	 * @param targetPosition
	 */
	public void moveScrollAnim(int targetPosition) {
		switch (targetPosition) {

		case IB_PLAYMODE:

			mMediaBtnScrollAnimView.scrollAnimTo(mIbPlayLoopMode, false);
			setPlayResumeBgStatus(false);

			break;
		case IB_PLAYPRE:

			mMediaBtnScrollAnimView.scrollAnimTo(mIbPlayPre, false);
			setPlayResumeBgStatus(false);

			break;
		case IB_PLAYRESUME:

			mMediaBtnScrollAnimView.scrollAnimTo(mRlPlayResume, false);
			setPlayResumeBgStatus(true);

			break;
		case IB_PLAYNEXT:

			mMediaBtnScrollAnimView.scrollAnimTo(mIbPlayNext, false);
			setPlayResumeBgStatus(false);

			break;
		case IB_IBPLAYLIST:

			mMediaBtnScrollAnimView.scrollAnimTo(mIbPlayList, false);
			setPlayResumeBgStatus(false);

			break;

		default:

			break;
		}
	}

	/**
	 * 响应底部媒体栏点击响应事件
	 * 
	 * @param targetPosition
	 */
	public void responseImageButtonClickEvent(int targetPosition) {
		switch (targetPosition) {

		case IB_PLAYMODE:

			int playMode = changePlayMode();
			mIbPlayLoopMode.setImageDrawable(context.getResources().getDrawable(mPlayModeDrawable[playMode]));
			sendPlayerServiceBroadCase(PlayerService.ACTION_MODE);

			break;
		case IB_PLAYPRE:

			sendPlayerServiceBroadCase(PlayerService.ACTION_PLAY_PREVIOUS);

			break;
		case IB_PLAYRESUME:

			// AnimationSet animationSet = new AnimationSet(true);
			final ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.0f, 1.0f, mMinScale,
					Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			scaleAnimation.setDuration(200);
			scaleAnimation.setFillAfter(false);
			mIvPlayAnim.startAnimation(scaleAnimation);

			Boolean localBoolean = (Boolean) mIbPlayResume.getTag();

			if (localBoolean == null || !localBoolean) {
				mIbPlayResume.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_media_pause));
				localBoolean = Boolean.valueOf(true);
				mIbPlayResume.setTag(localBoolean);
			} else {
				mIbPlayResume.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_media_resume));
				localBoolean = Boolean.valueOf(false);
				mIbPlayResume.setTag(localBoolean);
			}

			sendPlayerServiceBroadCase(PlayerService.ACTION_PLAY_BUTTON);

			break;
		case IB_PLAYNEXT:

			sendPlayerServiceBroadCase(PlayerService.ACTION_PLAY_NEXT);

			break;
		case IB_IBPLAYLIST:

			mIsShowPlayList = true;
			showRightAreaContent(true);
//			changeMediaBarImageButtonStatus(mIbPlayLyric, R.drawable.ic_media_lyric, R.drawable.ic_media_lyric_checked,
//					false);
			break;

		default:

			break;
		}
	}

	/**
	 * 发送给PlayerService广播，根据action不同，在service执行不同操作
	 * 
	 * @param actionMode
	 */
	private void sendPlayerServiceBroadCase(String actionMode) {
		Intent intent = new Intent();
		intent.setAction(actionMode);

		if (actionMode.equals(PlayerService.ACTION_PLAY_ITEM)) {

			intent.putParcelableArrayListExtra(PlayerFinal.PLAYER_LIST, mMusicInfoList);
			intent.putExtra(PlayerFinal.PLAYER_WHERE, "internet");
			intent.putExtra(PlayerFinal.PLAYER_POSITION, mListViewItemClickPos);
		}

		context.sendBroadcast(intent);
	}

	/**
	 * 展示右边界面内容
	 * 
	 * @param isShowPlayList
	 */
	public void showRightAreaContent(boolean isShowPlayList) {

		if (isShowPlayList) {
			mVodMusicLyricView.setVisibility(View.GONE);
			mTvPlayListTitle.setVisibility(View.VISIBLE);
			mLvPlayList.setVisibility(View.VISIBLE);
			mLvPlayList.requestFocus();
		} else {
			mVodMusicLyricView.setVisibility(View.VISIBLE);
			mTvPlayListTitle.setVisibility(View.GONE);
			mLvPlayList.setVisibility(View.GONE);
			mLvPlayList.clearFocus();
		}
	}

	@Override
	public boolean onKeyDpadLeft() {

		if (!mIsShowPlayList) {
			if (mMediaBarFocusPosition > -2) {
				mMediaBarFocusPosition--;

				moveScrollAnim(mMediaBarFocusPosition);
			} else {
				mMediaBarFocusPosition = -2;
			}
		}

		return true;

	}

	@Override
	public boolean onKeyDpadRight() {

		if (!mIsShowPlayList) {
			if (mMediaBarFocusPosition < 2) {

				mMediaBarFocusPosition++;

				moveScrollAnim(mMediaBarFocusPosition);
			} else {
				mMediaBarFocusPosition = 2;
			}

		}

		return true;
	}

}
