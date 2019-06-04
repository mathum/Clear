package com.clearcrane.pushmessage;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.text.method.SingleLineTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clearcrane.activity.ClearApplication;
import com.clearcrane.colorfuleggs.EggsFactory.Eggs;
import com.clearcrane.constant.ClearConstant;
import com.clearcrane.log.ClearLog;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.view.InsertionView;
import com.clearcrane.view.VoDMovieView;
import com.clearcrane.view.VoDViewManager;
import com.clearcrane.vod.R.drawable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;

@SuppressLint("NewApi")
public class FloatViewService extends Service {

	final static String TAG = "FloatViewService";

	public final static String ACTION_SHOW = "action_show";
	public final static String ACTION_HIDE = "action_hide";
	public final static String ACTION_STOP = "action_stop";
	public final static String ACTION_RESTART = "action_restart";
	public final static String SHOW_MESSAGE = "show_message";
	public final static String HIDE_MESSAGE = "hide_message";
	public final static String SHOW_BLACK_SCREEN = "show_black_screen";
	public final static String HIDE_BLACK_SCREEN = "hide_black_screen";
	public final static String SHOW_FULLSCREEN_CHANNEL = "show_fullscreen_channel";
	public final static String SHOW_INTERCUT_VIDEO = "show_INTERCUT_video";
	public final static String HIDE_INTERCUT_VIDEO = "hide_INTERCUT_video";

	private AlwaysMarqueeTextView mTextView;
	private WindowManager mWinMgr;
	private boolean isShowing = false;

	public static float updateSpeed = 1.8f;
	public static int updateFontColor = 0xFFF0F0F0;
	private int updateBgColor = Color.BLACK;
	private int updateBgAlpha = 120;
	private int updatePosY = 0;
	private int updateFontSize = 30;

	public SharedPreferences sharePre;
	public static SharedPreferences accessTimeSharePre;
	public static SharedPreferences INTERCUTSharePre;

	public static SharedPreferences messageSharePre;
	public long secDuration = 0;
	private Handler handler;
	private Eggs curEggs;

	private static ImageView messageImageView;
	private static LinearLayout newMessageView;
	private static TextView messageTextView;
	private static boolean messageAllRead = true;

	private static LinearLayout newBlackView;
	private static TextView promptTextView;

	private ArrayList<TextView> promptList = new ArrayList();

	private long playingTime = 0;

	public ClearApplication mApp;
	public Timer mCheckTimer;
	private VoDMovieView movieView;
	private InsertionView istView;

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		mApp = (ClearApplication) getApplication();
		sharePre = getSharedPreferences(ClearConstant.SCROLLTEXT_FILE, Context.MODE_PRIVATE);
		messageSharePre = getSharedPreferences(ClearConstant.MESSAGE_FILE, Context.MODE_PRIVATE);
		accessTimeSharePre = getSharedPreferences(ClearConstant.ACCESSTIME_FILE, Context.MODE_PRIVATE);
		INTERCUTSharePre = getSharedPreferences(ClearConstant.INTERCUT_FILE, Context.MODE_PRIVATE);

		handler = new Handler();

		createView();
		// 处理插播
		// createINTERCUTView();
	}

	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (getSecDuration() < 2) {
				hideFloatView();
			} else {
				handler.postDelayed(runnable, getSecDuration() * 1000);
			}
		}
	};

	private long getSecDuration() {
		return 0;
	}


//	public void showInterCutView() {
//		curInterCutCommand.orginTime = INTERCUTSharePre.getString(ClearConstant.ORIGIN_TIME, "");
//		curInterCutCommand.terminateTime = INTERCUTSharePre.getString(ClearConstant.TERMINATE_TIME, "");
//		curInterCutCommand.version = INTERCUTSharePre.getInt(ClearConstant.INTERCUT_VERSION, -1);
//		curInterCutCommand.url = INTERCUTSharePre.getString(ClearConstant.INTERCUT_URL, "");
//		curInterCutCommand.type = INTERCUTSharePre.getString(ClearConstant.INTERCUT_TYPE, "");
//		curInterCutCommand.title = INTERCUTSharePre.getString(ClearConstant.INTERCUT_TITLE, "");
//		playingTime = INTERCUTSharePre.getLong(ClearConstant.INTERCUT_PLAYINGTIME, 0);
//
//		if (curInterCutCommand.version > 0) {
//			Log.i(TAG, ":" + curInterCutCommand.type);
//			// FIXME
//			VoDViewManager voDViewManager = VoDViewManager.getInstance();
//			VoDBaseView voDBaseView = voDViewManager.getTopView();
//			
//			if (voDBaseView instanceof VoDMovieView) {
//				int pos = (int) voDViewManager.getVideoView().getCurrentPosition();
//				((VoDMovieView) voDBaseView).setPosition(pos);
//			} 
//			if (curInterCutCommand.type.equalsIgnoreCase("live")) {
//				// 插播的视频是直播源
//				istView = new InsertionView();
//				if (istView != null) {
//					// 传入直播页面布局的Json的URL地址和直播源的视频地址
//					istView.init(this, curInterCutCommand.url, true);
//					VoDViewManager.getInstance().pushForegroundView(istView);
//					try {
//						mApp.interruptProgramResourceName = curInterCutCommand.title;
//						mApp.interruptviewType = "直播";
//						mApp.catePath = "插播直播模块";
//						mApp.interruptProgramTimeInS = DateUtil.getTimeMillSecondFromDateStr(mApp.curServTime) / 1000;
//						String logInsert = mApp.combinatePostParasString("start", "0", "插播", mApp.interruptviewType,
//								mApp.interruptProgramResourceName, mApp.catePath);
//						ClearLog.logInsert(logInsert);
//						Log.i(TAG, "开始直播 : " + logInsert);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			} else if (curInterCutCommand.type.equalsIgnoreCase("video")) {
//				Log.i(TAG, "已播放时间:" + playingTime);
//				// 插播的视频是点播源
//				VoDViewManager.getInstance().stopBackgroundVideo();
//				VoDViewManager.getInstance().hideBackgroundVideo();
//				movieView = new VoDMovieView();
//				if (movieView != null) {
//					try {
//						mApp.interruptProgramResourceName = curInterCutCommand.title;
//						mApp.interruptviewType = "视频";
//						mApp.catePath = "插播视频模块";
//						mApp.interruptProgramTimeInS = DateUtil.getTimeMillSecondFromDateStr(mApp.curServTime) / 1000;
//						String logInsert = mApp.combinatePostParasString("start", "0", "插播", mApp.interruptviewType,
//								mApp.interruptProgramResourceName, mApp.catePath);
//						ClearLog.logInsert(logInsert);
//						Log.i(TAG, "开始插播视频 : " + logInsert);
//
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//					// 传入点播的视频的URL地址和需要点播的视频名称
//					movieView.init(this, curInterCutCommand.url, curInterCutCommand.title, true, playingTime);
//					VoDViewManager.getInstance().pushForegroundView(movieView);
//
//				}
//			}
//		}
//
//	}

	private Runnable r = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (getSecDuration() > 0 && getSecDuration() < 2) {
				// 当前播放的view消失
				Log.i(TAG, "Here I go.");
				try {
					String logInsert = mApp.combinatePostParasString("stop", "0", "插播", mApp.interruptviewType,
							mApp.interruptProgramResourceName, mApp.catePath);
					ClearLog.logInsert(logInsert);
					Log.i(TAG, "stop : " + logInsert);
				} catch (Exception e) {
					e.printStackTrace();
				}

				VoDViewManager.getInstance().popForegroundView();

			} else {
				handler.postDelayed(r, getSecDuration() * 1000);
			}
		}
	};

//	public void hideInterCutView() {
//		// handler.postDelayed(r, secDuration * 1000);
//		try {
//			if (INTERCUTSharePre.getString(ClearConstant.INTERCUT_TYPE, "").equalsIgnoreCase("video")) {
//				if (movieView != null) {
//					movieView.stopInterruptProgram();
//				}
//
//			} else if (curInterCutCommand.type.equalsIgnoreCase("live")) {
//				istView.stopInterruptProgram();
//			}
//		} catch (Exception e) {
//			Log.e(TAG, "hideInterCutView error： " + e.getMessage());
//		}
//		Log.i(TAG, "hideInterCutView here!");
//	}


	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d(TAG, "Service started with intent=" + intent);

		if (intent == null || intent.getAction() == null) {
			Log.d(TAG, "no action");
			return;
		}

		if (intent.getAction().equals(ACTION_STOP) == true) {
			stopSelf();
		} else if (intent.getAction().equals(ACTION_SHOW) == true) {
			showFloatView();
		} else if (intent.getAction().equals(ACTION_HIDE) == true) {
			hideFloatView();
		} else if (intent.getAction().equals(ACTION_RESTART) == true) {
			hideFloatView();
		} else if (intent.getAction().equals(SHOW_MESSAGE) == true) {
			showMessageView();
		} else if (intent.getAction().equals(HIDE_MESSAGE) == true) {
			hideMessageView();
		} else if (intent.getAction().equals(SHOW_BLACK_SCREEN) == true) {
			showBlackView();
		} else if (intent.getAction().equals(HIDE_BLACK_SCREEN) == true) {
			hideBlackView();
		} else if (intent.getAction().equals(SHOW_FULLSCREEN_CHANNEL) == true) {
			// showFullScreenChannel();
		} else if (intent.getAction().equals(SHOW_INTERCUT_VIDEO) == true) {
		} else if (intent.getAction().equals(HIDE_INTERCUT_VIDEO) == true) {
		}
	}

	public static void actionRestart(Context context) {
		Intent i = new Intent(context, FloatViewService.class);
		i.setAction(FloatViewService.ACTION_RESTART);
		context.startService(i);
	}

	public static void actionShowMessage(Context context) {
		Intent i = new Intent(context, FloatViewService.class);
		i.setAction(FloatViewService.SHOW_MESSAGE);
		context.startService(i);
	}

	public static void actionHideMessage(Context context) {
		Intent i = new Intent(context, FloatViewService.class);
		i.setAction(FloatViewService.HIDE_MESSAGE);
		context.startService(i);
	}

	public static void actionShowBlackScreen(Context context) {
		Intent i = new Intent(context, FloatViewService.class);

		i.setAction(FloatViewService.SHOW_BLACK_SCREEN);
		context.startService(i);
	}

	public static void actionHideBlackScreen(Context context) {
		Intent i = new Intent(context, FloatViewService.class);
		i.setAction(FloatViewService.HIDE_BLACK_SCREEN);
		context.startService(i);
	}

	public static void actionShowInterCut(Context context, long time) {
		Intent i = new Intent(context, FloatViewService.class);
		i.putExtra("duration", time);
		i.setAction(FloatViewService.SHOW_INTERCUT_VIDEO);
		context.startService(i);
	}

	public static void actionShowInterCut(Context context) {
		Intent i = new Intent(context, FloatViewService.class);
		i.setAction(FloatViewService.SHOW_INTERCUT_VIDEO);
		context.startService(i);
	}

	public static void actionHideInterCut(Context context) {
		Intent i = new Intent(context, FloatViewService.class);

		i.setAction(FloatViewService.HIDE_INTERCUT_VIDEO);
		context.startService(i);
	}

	@Override
	public void onDestroy() {
		// Stop the services, if it has been started
		mTextView.stopScroll();
		mWinMgr.removeView(mTextView);
		handler.removeCallbacks(runnable);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	protected void createView() {
		mTextView = new AlwaysMarqueeTextView(this);
		mTextView.setTextSize(updateFontSize);
		mTextView.setTextColor(updateFontColor);
		mTextView.setBackgroundColor(updateBgColor);
		mTextView.setTransformationMethod(SingleLineTransformationMethod.getInstance());
		mTextView.setText("");
		mTextView.getBackground().setAlpha(0);
		mTextView.setGravity(Gravity.CENTER);
		mTextView.setPadding(0, 5, 0, 4);

		DisplayMetrics metrics = new DisplayMetrics();
		mWinMgr = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		mWinMgr.getDefaultDisplay().getMetrics(metrics);

		WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
						| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
				PixelFormat.TRANSLUCENT);
		// params.gravity = Gravity.LEFT | Gravity.TOP;
		params.gravity = Gravity.CENTER;
		// params.gravity = Gravity.TOP;
		params.y = ClearConfig.getScreenHeight() * 4 / 10;

		// set screen size
		params.width = ClearConfig.getScreenWidth();

		Paint pFont = new Paint();
		pFont.setTextSize(updateFontSize);
		FontMetrics fm = pFont.getFontMetrics();
		params.height = ClearConfig.getPlatformHeight((int) Math.ceil(fm.descent - fm.ascent) + 15);

		params.setTitle("TopTextView");
		mWinMgr.addView(mTextView, params);
		// new message attention imageview
		createMessageView();

		createBlackView();
	}

	protected void createBlackView() {
		Log.i("black", "black");

		newBlackView = new LinearLayout(this);
		newBlackView.setBackgroundColor(Color.BLACK);
		newBlackView.setOrientation(LinearLayout.VERTICAL);

		// 添加blackView
		WindowManager.LayoutParams blackViewParams = new WindowManager.LayoutParams(
				ClearConfig.getPlatformWidth(ClearConfig.getScreenWidth()),
				ClearConfig.getPlatformHeight(ClearConfig.getScreenHeight()),
				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
						| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
				PixelFormat.TRANSLUCENT);
		blackViewParams.gravity = Gravity.CENTER;
		mWinMgr.addView(newBlackView, blackViewParams);

		hideBlackView();
	}

	public void showBlackView() {
		Log.i(TAG, "show black view");
		for (TextView promptTextView : promptList) {
			if (promptTextView.getText() != null) {
				// promptTextView.setText("");
				newBlackView.removeView(promptTextView);
			}
		}

		// 在show的时候加上textview，在onstart时，sharepre里的值才被记录，oncreate里会有时序问题
		int a = accessTimeSharePre.getInt("NUM", 0);

		for (int i = 0; i < a; i++) {
			promptTextView = new TextView(this);
			promptList.add(promptTextView);
		}

		for (int i = 0; i < a; i++) {
			promptTextView = promptList.get(i);
			if (i == 0) {
				promptTextView.setText("系统可用时间为:" + accessTimeSharePre.getString(ClearConstant.START_TIME + i, null)
						+ "~" + accessTimeSharePre.getString(ClearConstant.END_TIME + i, null));
			} else {
				promptTextView.setText("                             "
						+ accessTimeSharePre.getString(ClearConstant.START_TIME + i, null) + "~"
						+ accessTimeSharePre.getString(ClearConstant.END_TIME + i, null));
			}
			promptTextView.setTextSize(ClearConfig.getScreenHeight() / 10);
			promptTextView.setSingleLine(true);
			promptTextView.setFocusable(true);
			promptTextView.requestFocus();

			// blackView中添加文字
			newBlackView.setPadding(ClearConfig.getScreenHeight() / 6, ClearConfig.getScreenHeight() * 5 / 12, 0, 0);
			Log.i(TAG, "add black view");
			newBlackView.addView(promptTextView);
			newBlackView.setVisibility(View.VISIBLE);
		}
	}

	public void hideBlackView() {
		Log.i(TAG, "hide black view");
		for (TextView promptTextView : promptList) {
			if (promptTextView.getText() != null) {
				// promptTextView.setText("");
				newBlackView.removeView(promptTextView);
			}
		}
		newBlackView.setVisibility(View.INVISIBLE);
	}

	protected void createMessageView() {
		newMessageView = new LinearLayout(this);
		// add image view
		messageImageView = new ImageView(this);
		messageImageView.setImageResource(drawable.message_pic);
		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ClearConfig.getPlatformWidth(60),
				ClearConfig.getPlatformHeight(40));
		params1.gravity = Gravity.CENTER_VERTICAL;
		newMessageView.addView(messageImageView, params1);
		// add text view
		messageTextView = new TextView(this);
		messageTextView.setText("New Message");
		messageTextView.setSingleLine(true);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ClearConfig.getPlatformWidth(150),
				LinearLayout.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER_VERTICAL;
		newMessageView.addView(messageTextView, params);

		WindowManager.LayoutParams messageViewParams = new WindowManager.LayoutParams(ClearConfig.getPlatformWidth(250),
				ClearConfig.getPlatformHeight(60), WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
						| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
				PixelFormat.TRANSLUCENT);
		messageViewParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
		mWinMgr.addView(newMessageView, messageViewParams);
		hideMessageView();
		checkMessageStat();
	}

	public void showFloatView() {
		mTextView.getBackground().setAlpha(updateBgAlpha);
		mTextView.init(mWinMgr);
		mTextView.startScroll();
		isShowing = true;
		handler.postDelayed(runnable, secDuration * 1000);
	}

	public void hideFloatView() {
		if (!isShowing) {
			return;
		}
		isShowing = false;
		mTextView.setText("");
		mTextView.getBackground().setAlpha(0);
		mTextView.init(mWinMgr);
		mTextView.stopScroll();
		handler.removeCallbacks(runnable);
	}

	public void showMessageView() {
		/*
		 * Bitmap bitmap = BitmapFactory.decodeResource( this.getResources(),
		 * R.drawable.message_pic); Log.i(TAG,"show message view");
		 * //messageImageView.setImageResource(drawable.message_pic);
		 * messageImageView.setImageBitmap(bitmap);
		 */
		newMessageView.setVisibility(View.VISIBLE);
		// messageImageView.setImageResource(drawable.message_pic);
	}

	public void hideMessageView() {
		Log.i(TAG, "hide message view");
		/*
		 * Bitmap bitmap = BitmapFactory.decodeResource( this.getResources(),
		 * R.drawable.nomessage); messageImageView.setImageBitmap(bitmap);
		 */
		// messageImageView.setImageResource(drawable.nomessage);
		newMessageView.setVisibility(View.INVISIBLE);
	}

	public synchronized void checkMessageStat() {
		String messageJson = messageSharePre.getString(ClearConstant.MESSAGE_JSON, null);
		if (messageJson == null) {
			return;
		}
		try {
			JSONObject jObject = new JSONObject(messageJson);
			JSONArray array = jObject.getJSONArray("messages");
			if (array.length() > 0) {
				for (int i = 0; i < array.length(); i++) {
					int Versionid = array.getJSONObject(i).getInt("versionid");
					boolean statu = messageSharePre.getBoolean("versionid" + Versionid, false);
					if (statu == false) {
						messageAllRead = false;
						showMessageView();
						return;
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
