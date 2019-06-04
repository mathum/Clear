package com.clearcrane.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clearcrane.entity.BootAdvertisement;
import com.clearcrane.entity.Picture;
import com.clearcrane.util.UnzipBootAnimation;
import com.clearcrane.vod.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Timer;

/**
 * 开机闪屏页
 * 
 * @author SlientLeaves 2016年6月6日 下午4:19:24
 */
public class SplashActivity extends Activity {

	protected static int BOOT_IMAGE_DISPLAY_DELAY_TIME = 3000;
	protected static final int BOOT_LOAD_ANIM_DELAY_TIME = 500;
	protected static final String TAG = "SplashActivity";
	private static final int SHOW_CAROUSEL_IMAGE = 0;
	private static final int REFRESH_COUNTDOWN_TIME = 1;
	private String mJsonResult;
	private int mCurrentIndex;// 当前位置索引
	private int mBitmapDrawablesIndex;// 开机动画图片索引
	private BootAdvertisement mBootAdvertisement;
	private FrameLayout mFlContent;
	private LinearLayout mLlCountdownTimer;
	private Context mContext;
	private DisplayImageOptions options;
	private boolean isFirstLoad = true;
	private boolean hasStartCountDown;
	private boolean isFirstDisplayAnim = true;
	private boolean isFirstDisplayImage = true;
	private byte downloadImageCount;
	private int[] mDates;

	private TextView mTvMinute;
	private TextView mTvSecond;

	private int hour_decade;
	private int hour_unit;
	private int min_decade;
	private int min_unit;
	private int sec_decade;
	private int sec_unit;

	private Timer mTimer;

	private ArrayList<BitmapDrawable> mBitmapDrawableList;

//	private Handler mHandler = new Handler() {
//		public void handleMessage(android.os.Message msg) {
//
//			if (msg.what == SHOW_CAROUSEL_IMAGE) {
//				carouselImage();
//			} else if (msg.what == REFRESH_COUNTDOWN_TIME) {
//				countDown();
//			}
//		}
//
//	};;

//	private OnCompleteListener completeListener = new OnCompleteListener() {
//
//		@Override
//		public void onDownloaded(Object result) {
//			mJsonResult = (String) result;
//			if (mJsonResult == null) {
//				launchNewActivity();
//			}

//			try {
//				parseData(mJsonResult);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//
//		@Override
//		public void onComplete(boolean result) {
//
//		}
//
//	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getApplicationContext();
		setContentView(R.layout.activity_splash);
		Log.e("xb", "splashactivity啓動");
//		initBootAnimation();
//		initView();
//		options = new DisplayImageOptions.Builder().cacheInMemory(true) // 设置下载的图片是否缓存在内存中
//				.cacheOnDisk(true) // 设置下载的图片是否缓存在SD卡中
//				// .displayer(new FadeInBitmapDisplayer(500))// 图片加载好后渐入的动画时间
//				.imageScaleType(ImageScaleType.EXACTLY)// 设置图片的缩放方式
//				.bitmapConfig(Bitmap.Config.ARGB_8888).build();
//
//		Log.i(TAG, "SplshsActivity onCreate 执行了……");
//
//		SharedPreferences settings = mContext.getSharedPreferences(ClearConfig.NAME, Context.MODE_PRIVATE);
//		Editor editor = settings.edit();
//
//		// 当前主页地址
//		String MAIN_URI = settings.getString(ClearConstant.MAIN_SERVER, " ");
//
//		String requestURl = null;
//
//		if (MAIN_URI != null && !"".equals(MAIN_URI)) {
//
//			requestURl = MAIN_URI.substring(0, MAIN_URI.indexOf("/nativevod/now/main.json"));
//			requestURl += ClearConfig.BOOT_ADVERTISEMENT_URL;
//
//		} else {
//			requestURl = ClearConfig.DEFAULT_MAIN_URI.substring(0,
//					ClearConfig.DEFAULT_MAIN_URI.indexOf("/nativevod/now/main.json"));
//			requestURl += ClearConfig.BOOT_ADVERTISEMENT_URL;
//		}
//
//		Log.i(TAG, requestURl);
//		getDataFromServer(requestURl);

	}

	/**
	 * 请求服务器获取数据
	 * 
	 * @param url
	 */
//	private void getDataFromServer(String url) {
//		MaterialRequest mr = new MaterialRequest(this, ClearConfig.TYPE_JSON);
//		mr.setOnCompleteListener(completeListener);
//		mr.execute(url);
//	}

	/**
	 * 初始化布局
	 */
	private void initView() {

		mFlContent = (FrameLayout) findViewById(R.id.fl_content);
		mLlCountdownTimer = (LinearLayout) findViewById(R.id.ll_countdown_timer);
		mTvMinute = (TextView) findViewById(R.id.tv_minute_pre);
		mTvSecond = (TextView) findViewById(R.id.tv_second_pre);
	}

	/**
	 * 解析数据
	 * 
	 * @param mJsonResult
	 */
	private void parseData(String mJsonResult) throws Exception {

		Log.i(TAG, "parseData 执行了……  " + "mJsonResult:" + mJsonResult);
		JSONTokener jsonParser = new JSONTokener(mJsonResult);
		JSONObject mainViewObj = (JSONObject) jsonParser.nextValue();
		mBootAdvertisement = new BootAdvertisement();
		mBootAdvertisement.count = Integer.parseInt(mainViewObj.getString("count"));
		mBootAdvertisement.totalDuration = Integer.parseInt(mainViewObj.getString("total_duration"));
		mBootAdvertisement.perDuration = Integer.parseInt(mainViewObj.getString("per_duration"));

		if (mBootAdvertisement.count <= 0) {
//			launchNewActivity();
		} else {
			JSONArray contentArray = (JSONArray) mainViewObj.getJSONArray("content");

			for (int i = 0; i < contentArray.length(); i++) {
				JSONObject objecttmp = (JSONObject) contentArray.opt(i);
				Picture p = new Picture();
				p.index = objecttmp.getString("id");
				p.path = objecttmp.getString("material");
				mBootAdvertisement.picList.add(p);
			}

			if (mBootAdvertisement.perDuration > 3) {
				BOOT_IMAGE_DISPLAY_DELAY_TIME = mBootAdvertisement.perDuration * 1000;
			}

			onLoadResource(0);

//			mHandler.sendEmptyMessage(SHOW_CAROUSEL_IMAGE);

		}

	}

	/**
	 * 根据当前索引位置加载下一页数据
	 * 
	 * @param currentIndex
	 */
	private void onLoadResource(int currentIndex) {

		if (currentIndex < 0 || currentIndex >= mBootAdvertisement.count) {
			return;
		}

		int suffIndex = currentIndex + 1;

		if (isFirstLoad) {
			isFirstLoad = false;
			downloadImage(currentIndex);
		}

		if (suffIndex <= mBootAdvertisement.count - 1) {
			downloadImage(suffIndex);
		} else {
			downloadImage(0);
		}
	}

	/**
	 * 移除图片
	 * 
	 * @param currentIndex
	 */
	private void removeImage(int index) {
		if (index != 0) {
			Picture picture = mBootAdvertisement.picList.get(index - 1);
			picture.imageView = null;
			picture.bitmap = null;
		}
	}

	/**
	 * 添加内容
	 * 
	 * @param isLoadAnim
	 */
	private void addContentView(boolean isLoadAnim) {

		mFlContent.removeAllViews();

		if (isLoadAnim) {
			ImageView imageView = new ImageView(this);
			imageView.setImageDrawable(mBitmapDrawableList.get(mBitmapDrawablesIndex));
			mFlContent.addView(imageView);
		} else {
			Picture picture = mBootAdvertisement.picList.get(mCurrentIndex);
			picture.imageView.setScaleType(ScaleType.FIT_XY);
			mFlContent.addView(picture.imageView);
		}

	}

	/**
	 * 启动新的Activity
	 */
	private void launchNewActivity() {
		Log.i(TAG, "SplshsActivity launchNewActivity 执行了……");
		startActivity(new Intent(this, VoDActivity.class));
		finish();
	}

	/**
	 * 下载图片
	 * 
	 * @param index
	 */
	private void downloadImage(int index) {
		if (index < 0 || index >= mBootAdvertisement.count) {
			return;
		}

		final Picture picture = mBootAdvertisement.picList.get(index);
		picture.imageView = new ImageView(mContext);
		String url = picture.path;
		Log.i(TAG, "url:" + url);
		ImageLoader.getInstance().displayImage(url, picture.imageView, options, new ImageLoadingListener() {

			@Override
			public void onLoadingStarted(String imageUri, View view) {

			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				picture.bitmap = loadedImage;
				downloadImageCount++;
			}

			@Override
			public void onLoadingCancelled(String imageUri, View view) {

			}
		});
	}

	/**
	 * 根据传进的总秒数转换成时分秒格式
	 * 
	 * @param t
	 * @return
	 */
	public static int[] getHourMinSec(long t) {
		int[] dates = new int[3];
		dates[0] = Integer.valueOf(t / 3600 + "");
		dates[1] = Integer.valueOf((t - dates[0] * 3600) / 60 + "");
		dates[2] = Integer.valueOf(t % 60 + "");
		return dates;
	}

	/**
	 * 开启倒计时
	 */
	private void startCountDown() {

		mDates = getHourMinSec(mBootAdvertisement.totalDuration);
		mLlCountdownTimer.setVisibility(View.VISIBLE);
		setTime(mDates[0], mDates[1], mDates[2]);
	}

	/**
	 * 开机动画
	 */
	private void initBootAnimation() {
		UnzipBootAnimation unzipBootAnimation = new UnzipBootAnimation();
		mBitmapDrawableList = new ArrayList<BitmapDrawable>();
		mBitmapDrawableList = unzipBootAnimation.getBitmapDrawables();
	}

	/**
	 * 轮播图片
	 */
//	private void carouselImage() {
//
//		if (downloadImageCount > 1) {
//
//			if (!hasStartCountDown) {
//				hasStartCountDown = !hasStartCountDown;
//				startCountDown();
//				start();
//			}
//
//			mCurrentIndex++;
//
//			if (mCurrentIndex >= mBootAdvertisement.picList.size()) {
//				mCurrentIndex = 0;
//			}
//
//			if (downloadImageCount == 2) {
//				mCurrentIndex = 0;
//			}
//
//			onLoadResource(mCurrentIndex);
//			addContentView(false);
//			removeImage(mCurrentIndex);
//
//			if (isFirstDisplayImage) {
//
//				isFirstDisplayImage = !isFirstDisplayImage;
//				mHandler.sendEmptyMessage(SHOW_CAROUSEL_IMAGE);
//			} else {
//
//				mHandler.sendEmptyMessageDelayed(SHOW_CAROUSEL_IMAGE, BOOT_IMAGE_DISPLAY_DELAY_TIME);
//			}
//
//		} else {
//
//			addContentView(true);
//			if (++mBitmapDrawablesIndex >= mBitmapDrawableList.size()) {
//				mBitmapDrawablesIndex = 0;
//			}
//
//			if (isFirstDisplayAnim) {
//				isFirstDisplayAnim = !isFirstDisplayAnim;
//				mHandler.sendEmptyMessage(SHOW_CAROUSEL_IMAGE);
//			} else {
//				mHandler.sendEmptyMessageDelayed(SHOW_CAROUSEL_IMAGE, BOOT_LOAD_ANIM_DELAY_TIME);
//			}
//
//		}
//
//	}

	/**
	 * @throws Exception
	 * 
	 * @Description: 设置倒计时的时长 @param @return void @throws
	 */
	public void setTime(int hour, int min, int sec) {

		if (hour >= 60 || min >= 60 || sec >= 60 || hour < 0 || min < 0 || sec < 0) {
			throw new RuntimeException("Time format is error,please check out your code");
		}

		hour_decade = hour / 10;
		hour_unit = hour - hour_decade * 10;

		min_decade = min / 10;
		min_unit = min - min_decade * 10;

		sec_decade = sec / 10;
		sec_unit = sec - sec_decade * 10;

		if (0 == min_decade && min_decade == min_unit) {
			mTvMinute.setText("0");
		} else {
			mTvMinute.setText(min_decade + "" + min_unit);
		}

		if (0 == sec_decade && sec_decade == sec_unit) {
			mTvSecond.setText("0");
		} else {
			mTvSecond.setText(sec_decade + "" + sec_unit);
		}

	}

	/**
	 * 
	 * @Description: 开始计时 @param @return void @throws
	 */
//	public void start() {
//
//		if (mTimer == null) {
//			mTimer = new Timer();
//			mTimer.schedule(new TimerTask() {
//
//				@Override
//				public void run() {
//					mHandler.sendEmptyMessage(REFRESH_COUNTDOWN_TIME);
//				}
//			}, 0, 1000);
//		}
//	}

	/**
	 * 
	 * @Description: 停止计时 @param @return void @throws
	 */
//	public void stop() {
//		if (mTimer != null) {
//			mTimer.cancel();
//			mTimer = null;
//
//			mHandler.removeMessages(SHOW_CAROUSEL_IMAGE);
//			mHandler.removeMessages(REFRESH_COUNTDOWN_TIME);
//			
//			mBootAdvertisement.picList.clear();
//			mBootAdvertisement = null;
			
//			launchNewActivity();
//		}
//	}

	/**
	 * 
	 * @Description: 倒计时 @param @return boolean @throws
	 */
//	private void countDown() {
//
//		if (isCarrry4Time(mTvSecond)) {
//			if (isCarrry4Time(mTvMinute)) {
//				mTvSecond.setText("0");
//				mTvMinute.setText("0");
//				stop();
//			}
//		}
//	}

	/**
	 * 进位变化
	 * 
	 * @param tv
	 * @return
	 */
	private boolean isCarrry4Time(TextView tv) {

		int time = Integer.valueOf(tv.getText().toString());
		time = time - 1;
		if (time < 0) {
			time = 59;
			tv.setText(time + "");
			return true;
		} else {
			tv.setText(time + "");
			return false;
		}

	}

}
