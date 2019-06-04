package com.clearcrane.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.internal.widget.SizeAdaptiveLayout.LayoutParams;
import com.clearcrane.constant.ClearConstant;
import com.clearcrane.entity.Picture;
import com.clearcrane.entity.PictureCategory;
import com.clearcrane.log.ClearLog;
import com.clearcrane.provider.MaterialRequest;
import com.clearcrane.provider.MaterialRequest.OnCompleteListener;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.ImageUtil;
import com.clearcrane.util.TipDialog;
import com.clearcrane.vod.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.Calendar;

public class VodPDFView extends VoDBaseView {

	protected static final String TAG = "VodPDFView";
	private static final int MSG_DELAY_TIME = 5000;// 延时5秒
	private static final int POSITIVE_MOVE_INCREMENT = 100;//每次移动的次数
	private static final int NEGATIVE_MOVE_INCREMENT = -100;
	private static final int DOWN_MOVE_INCREMENT = 300;
	private static final int UP_MOVE_INCREMENT = -300;
	private String dataJson;// json数据
	public Calendar begin = null;
	private int mCurrentIndex = 0;// 当前位置索引
	private PictureCategory pc;
	private FrameLayout mFlContent;
	private TextView mTvProportion;// 显示比例 nm,m 如：5/6
	private DisplayImageOptions options;
	private long lastTime = 0;// 按键事件控制
	private int moveDis = 0;
	private int moveUpDownDis = 0;
	private double bigSize = 1.25;//
	private boolean isSettingZoom;
	double size = 1;
	double pixel = 30.00;
	private Bitmap resizeBitmap = null;
	private long mLastOperationTime;

	private Matrix matrix = new Matrix();
	public static SharedPreferences activitySharePre;

	public void init(Context ctx, String u) {
		begin = Calendar.getInstance();
		context = ctx;
		url = u;
		view = LayoutInflater.from(context).inflate(R.layout.pdf_view, null);
		initLayoutInXml();

		activitySharePre = context.getSharedPreferences(ClearConstant.Activity_FILE, Context.MODE_PRIVATE);

		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(dataJsonListenr);
		Log.i("xb", url);
		mr.execute(url);

		saveStatue(1, "图文");
	}
	//
	// public void init(Context ctx, String u, LinearLayout layout) {
	// begin = Calendar.getInstance();
	// context = ctx;
	// url = u;
	// menuLinearLayout = layout;
	// view = LayoutInflater.from(context).inflate(R.layout.pdf_view, null);
	// initLayoutInXml();
	//
	// MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
	// mr.setOnCompleteListener(dataJsonListenr);
	// Log.i("xb", url);
	// mr.execute(url);
	//
	// }

	private void initLayoutInXml() {
		mFlContent = (FrameLayout) view.findViewById(R.id.fl_content);
		mTvProportion = (TextView) view.findViewById(R.id.tv_proportion);
	}

	/**
	 * Json获取完成监听
	 */
	private OnCompleteListener dataJsonListenr = new OnCompleteListener() {

		@Override
		public void onDownloaded(Object result) {
			dataJson = (String) result;
			if (dataJson == null) {
				TipDialog.Builder builder = new TipDialog.Builder(context);
				builder.setMessage("当前网络不可用，请检查网络");
				builder.setTitle("提示");
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

				builder.create().show();
				return;
			}
			try {
				JSONTokener jsonParser = new JSONTokener(dataJson);
				JSONObject mainViewObj = (JSONObject) jsonParser.nextValue();
				pc = new PictureCategory();
				pc.count = Integer.parseInt(mainViewObj.getString("Count"));
				pc.label = mainViewObj.getString("Label");
				pc.type = mainViewObj.getString("Type");

				JSONArray contentArray = (JSONArray) mainViewObj.getJSONArray("Content");

				for (int i = 0; i < contentArray.length(); i++) {
					JSONObject objecttmp = (JSONObject) contentArray.opt(i);
					Picture p = new Picture();
					p.index = objecttmp.getString("index");
					p.path = objecttmp.getString("path");
					pc.picList.add(p);
				}

				if (pc.picList.size() == 1) {
					mTvProportion.setVisibility(View.INVISIBLE);
				}
				if (begin != null) {
					long between = (Calendar.getInstance()).getTimeInMillis() - begin.getTimeInMillis();
					ClearLog.LogInfo("BROSWER\tLoad\tSUCC\t" + between + "ms\t" + url + "\t" + "picturListView");
				}

				// onLoadResource(mCurrentIndex);
				if (pc.picList.size() > 0) {
					switchPicDown(mCurrentIndex);
				}

			} catch (JSONException e)

			{
				ClearLog.LogError("BROSWER\tLoad\tFAIL\t0ms\t" + url);
				e.printStackTrace();
			}

		}

		@Override
		public void onComplete(boolean result) {
		}

	};

	/**
	 * 加载当前位置图片
	 * 
	 * @param currentIndex
	 */
	private void onLoadResource(int currentIndex) {
		// 如果当页图片不为空的情况下，直接加载并且设置imageview全屏显示
		if (pc.picList.get(currentIndex).bitmap != null) {
			if (pc.picList.get(currentIndex).imageView == null) {
				pc.picList.get(currentIndex).imageView = new ImageView(context);
				pc.picList.get(currentIndex).imageView.setScaleType(ScaleType.FIT_CENTER);
				pc.picList.get(currentIndex).imageView.setLayoutParams(
						new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			}
			pc.picList.get(currentIndex).imageView.setImageBitmap(pc.picList.get(currentIndex).bitmap);
		} else {
			// 如果图片为空，则立即下载
			downloadImage(currentIndex);
		}
	}

	// 向下切换的方法
	private void switchPicDown(int position) {
		if (position >= pc.picList.size()) {
			mCurrentIndex = 0;
			position = 0;
		}
		mFlContent.removeAllViews();
		// 删除上上张的缓存数据
		if (mCurrentIndex == 0) {
			removeImage(pc.picList.size() - 2);
		}
		if (mCurrentIndex == 1) {
			removeImage(pc.picList.size() - 1);
		}
		if (mCurrentIndex >= 2) {
			removeImage(position - 2);
		}
		// 加载当前位置的图片
		onLoadResource(position);

		// 预加载下一张图片
		if (mCurrentIndex == pc.picList.size() - 1) {
			downloadImage(0);
		} else {
			downloadImage(position + 1);
		}
		mFlContent.addView(pc.picList.get(position).imageView);
		// 动态添加页码
		mTvProportion.setText(mCurrentIndex + 1 + "/" + pc.picList.size());
	}

	private void switchPicUp(int position) {
		if (position < 0) {
			mCurrentIndex = pc.picList.size() - 1;
			position = pc.picList.size() - 1;
		}
		mFlContent.removeAllViews();
		// 删除上一张的缓存数据
		if (mCurrentIndex == pc.picList.size() - 1) {
			removeImage(1);
		}
		if (mCurrentIndex == pc.picList.size() - 2) {
			removeImage(0);
		}
		if (mCurrentIndex < pc.picList.size() - 1) {
			removeImage(position + 2);
		}
		// 加载当前位置的图片
		onLoadResource(position);
		// 预加载上一张图片
		if (mCurrentIndex == 0) {
			downloadImage(pc.picList.size() - 1);
		} else {
			downloadImage(position - 1);
		}
		mFlContent.addView(pc.picList.get(position).imageView);
		// 动态添加页码
		mTvProportion.setText(mCurrentIndex + 1 + "/" + pc.picList.size());
	}

	private void removeImage(int index) {
		if (index >= 0 && index <= pc.picList.size() - 1) {
			pc.picList.get(index).imageView = null;
			if (pc.picList.get(index).bitmap != null) {
				pc.picList.get(index).bitmap.recycle();
				pc.picList.get(index).bitmap = null;
			}
		}
	}

	private void downloadImage(int index) {
		if (index < 0 || index > pc.picList.size() - 1) {
			return;
		}

		final Picture picture = pc.picList.get(index);
		picture.imageView = new ImageView(context);
		picture.imageView.setScaleType(ScaleType.FIT_CENTER);
		picture.imageView
				.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		ImageUtil.displayImage(ClearConfig.SERVER_URI_PREFIX + picture.path, picture.imageView,
				new ImageLoadingListener() {

					@Override
					public void onLoadingStarted(String imageUri, View view) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
						// TODO Auto-generated method stub
						picture.bitmap = loadedImage;
					}

					@Override
					public void onLoadingCancelled(String imageUri, View view) {
						// TODO Auto-generated method stub

					}
				});
	}

	@Override
	public boolean onKeyDpadLeft() {
		long last = System.currentTimeMillis();
		if (last - lastTime < 300) {
			return true;
		}
		lastTime = last;
		if (isSettingZoom) {
			// 向左的平移量
			moveDis = moveDis + NEGATIVE_MOVE_INCREMENT;
			// 回收上一张bitmap
			resizeBitmap.recycle();
			resizeBitmap = null;
			// 获取原图片的宽和高
			int width = pc.picList.get(mCurrentIndex).bitmap.getWidth();
			int height = pc.picList.get(mCurrentIndex).bitmap.getHeight();
			// 预转换成的图片的宽度和高度
			int newWidth = (int) (width * bigSize);
			Log.e("zoom", "width" + newWidth);
			int newHeight = (int) (height * bigSize);

			matrix.setScale((float) bigSize, (float) bigSize, (float) newWidth / 2 + moveDis, (float) newHeight / 2+moveUpDownDis);
			resizeBitmap = Bitmap.createBitmap(pc.picList.get(mCurrentIndex).bitmap.getWidth(),
					pc.picList.get(mCurrentIndex).bitmap.getHeight(), pc.picList.get(mCurrentIndex).bitmap.getConfig());
			Canvas canvas = new Canvas(resizeBitmap);
			Paint paint = new Paint();
			canvas.drawBitmap(pc.picList.get(mCurrentIndex).bitmap, matrix, paint);

			pc.picList.get(mCurrentIndex).imageView.setImageBitmap(resizeBitmap);
		}

		return true;
	}

	@Override
	public boolean onKeyDpadRight() {

		long last = System.currentTimeMillis();
		if (last - lastTime < 300) {
			return true;
		}
		lastTime = last;
		// 在缩放模式下
		if (isSettingZoom) {
			// 向右的平移量
			moveDis = moveDis + POSITIVE_MOVE_INCREMENT;
			// 回收上一张bitmap
			resizeBitmap.recycle();
			resizeBitmap = null;
			// 获取这个图片的宽和高
			int width = pc.picList.get(mCurrentIndex).bitmap.getWidth();
			int height = pc.picList.get(mCurrentIndex).bitmap.getHeight();
			// 预转换成的图片的宽度和高度
			int newWidth = (int) (width * bigSize);
			Log.e("zoom", "width" + newWidth);
			int newHeight = (int) (height * bigSize);

			Matrix matrix = new Matrix();
			matrix.setScale((float) bigSize, (float) bigSize, (float) newWidth / 2 + moveDis, (float) newHeight / 2 + moveUpDownDis);
			resizeBitmap = Bitmap.createBitmap(pc.picList.get(mCurrentIndex).bitmap.getWidth(),
					pc.picList.get(mCurrentIndex).bitmap.getHeight(), pc.picList.get(mCurrentIndex).bitmap.getConfig());
			Canvas canvas = new Canvas(resizeBitmap);
			Paint paint = new Paint();
			canvas.drawBitmap(pc.picList.get(mCurrentIndex).bitmap, matrix, paint);
			pc.picList.get(mCurrentIndex).imageView.setImageBitmap(resizeBitmap);
		}

		return true;
	}

	/**
	 * 显示上一页图片
	 */
	private final void showPreviousImage() {
		mCurrentIndex--;
		if (mCurrentIndex < 0) {
			mCurrentIndex = pc.picList.size() - 1;
		}
		switchPicUp(mCurrentIndex);
	}

	/**
	 * 显示下一页图片
	 */
	private final void showNextImage() {

		mCurrentIndex++;
		if (mCurrentIndex >= pc.picList.size()) {
			mCurrentIndex = 0;
		}
		switchPicDown(mCurrentIndex);
	}
	//
	// public void setLoopStatus(boolean isLooping) {
	// this.isLooping = isLooping;
	// }

	// 重写翻页键进行放大与缩小
	@Override
	public boolean onKeyDpadPageUp() {
		Log.e("xb", "tcl onkeyDpadPageUp");
		isSettingZoom = true;
		if (bigSize < 1.5f) {
			bigSize = bigSize + 0.1f;
		} else {
			bigSize = 1.5f;
		}
		// 获取这个图片的宽和高
		int width = pc.picList.get(mCurrentIndex).bitmap.getWidth();
		int height = pc.picList.get(mCurrentIndex).bitmap.getHeight();
		// 预转换成的图片的宽度和高度
		int newWidth = (int) (width * bigSize);
		Log.e("zoom", "width" + newWidth);
		int newHeight = (int) (height * bigSize);
		Log.e("zoom", "height" + newHeight);
		// 创建操作图片用的matrix对象
		matrix.setScale((float) bigSize, (float) bigSize, (float) newWidth / 2, (float) newHeight / 2);
		resizeBitmap = Bitmap.createBitmap(pc.picList.get(mCurrentIndex).bitmap.getWidth(),
				pc.picList.get(mCurrentIndex).bitmap.getHeight(), pc.picList.get(mCurrentIndex).bitmap.getConfig());
		Canvas canvas = new Canvas(resizeBitmap);
		Paint paint = new Paint();
		canvas.drawBitmap(pc.picList.get(mCurrentIndex).bitmap, matrix, paint);
		
		mFlContent.removeAllViews();
		pc.picList.get(mCurrentIndex).imageView = null;
		pc.picList.get(mCurrentIndex).imageView = new ImageView(context);
		pc.picList.get(mCurrentIndex).imageView.setScaleType(ScaleType.FIT_XY);
		pc.picList.get(mCurrentIndex).imageView
				.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		pc.picList.get(mCurrentIndex).imageView.setImageBitmap(resizeBitmap);
		mFlContent.addView(pc.picList.get(mCurrentIndex).imageView);

		return true;
	}

	@Override
	public boolean onKeyDpadPageDown() {
		Log.e("xb", "tcl onkeyDpadPageUp");
		isSettingZoom = true;
		if (bigSize > 1.0f) {
			bigSize = bigSize - 0.1f;
		} else {
			bigSize = 1.0f;
		}
		// 获取这个图片的宽和高
		int width = pc.picList.get(mCurrentIndex).bitmap.getWidth();
		int height = pc.picList.get(mCurrentIndex).bitmap.getHeight();
		// 预转换成的图片的宽度和高度
		int newWidth = (int) (width * bigSize);
		Log.e("zoom", "width" + newWidth);
		int newHeight = (int) (height * bigSize);
		Log.e("zoom", "height" + newHeight);
		// 创建操作图片用的matrix对象
		matrix.setScale((float) bigSize, (float) bigSize, (float) newWidth / 2, (float) newHeight / 2);
		resizeBitmap = Bitmap.createBitmap(pc.picList.get(mCurrentIndex).bitmap.getWidth(),
				pc.picList.get(mCurrentIndex).bitmap.getHeight(), pc.picList.get(mCurrentIndex).bitmap.getConfig());
		Canvas canvas = new Canvas(resizeBitmap);
		Paint paint = new Paint();
		canvas.drawBitmap(pc.picList.get(mCurrentIndex).bitmap, matrix, paint);
		
		mFlContent.removeAllViews();
		pc.picList.get(mCurrentIndex).imageView = null;
		pc.picList.get(mCurrentIndex).imageView = new ImageView(context);
		pc.picList.get(mCurrentIndex).imageView.setScaleType(ScaleType.FIT_XY);
		pc.picList.get(mCurrentIndex).imageView
				.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		pc.picList.get(mCurrentIndex).imageView.setImageBitmap(resizeBitmap);
		mFlContent.addView(pc.picList.get(mCurrentIndex).imageView);

		return true;
	}

	@Override
	public boolean onKeyBack() {

		if (isSettingZoom) {
			//退出缩放状态
			isSettingZoom = false;
			resizeBitmap.recycle();
			resizeBitmap = null;
			pc.picList.get(mCurrentIndex).imageView.setScaleType(ScaleType.FIT_CENTER);
			switchPicDown(mCurrentIndex);
			return true;
		} else {
			//退出浏览文档页面
			//清除所有的bitmap和imageview资源
			for (Picture p : pc.picList) {
				p.bitmap = null;
				p.imageView = null;
			}
			saveStatue(3, "");
			return super.onKeyBack();
		}
	}

	@Override
	public boolean onKeyDpadUp() {

		if (!isPermitOperation()) {
			return false;
		}
		if (isSettingZoom) {
			// 向左的平移量
			moveUpDownDis = moveUpDownDis + UP_MOVE_INCREMENT;
			// 回收上一张bitmap
			resizeBitmap.recycle();
			resizeBitmap = null;
			// 获取原图片的宽和高
			int width = pc.picList.get(mCurrentIndex).bitmap.getWidth();
			int height = pc.picList.get(mCurrentIndex).bitmap.getHeight();
			// 预转换成的图片的宽度和高度
			int newWidth = (int) (width * bigSize);
			Log.e("zoom", "width" + newWidth);
			int newHeight = (int) (height * bigSize);
			// 不断更改中心点坐标截取新的缩放后的图片
			matrix.setScale((float) bigSize, (float) bigSize, (float) newWidth / 2 + moveDis,
					(float) newHeight / 2 + moveUpDownDis);
			resizeBitmap = Bitmap.createBitmap(pc.picList.get(mCurrentIndex).bitmap.getWidth(),
					pc.picList.get(mCurrentIndex).bitmap.getHeight(), pc.picList.get(mCurrentIndex).bitmap.getConfig());
			Canvas canvas = new Canvas(resizeBitmap);
			Paint paint = new Paint();
			canvas.drawBitmap(pc.picList.get(mCurrentIndex).bitmap, matrix, paint);

			pc.picList.get(mCurrentIndex).imageView.setImageBitmap(resizeBitmap);
		}else {
			showPreviousImage();
		}
		return true;
	}

	@Override
	public boolean onKeyDpadDown() {

		if (!isPermitOperation()) {
			return false;
		}
		if (isSettingZoom) {
			// 向左的平移量
			moveUpDownDis = moveUpDownDis + DOWN_MOVE_INCREMENT;
			// 回收上一张bitmap
			resizeBitmap.recycle();
			resizeBitmap = null;
			// 获取原图片的宽和高
			int width = pc.picList.get(mCurrentIndex).bitmap.getWidth();
			int height = pc.picList.get(mCurrentIndex).bitmap.getHeight();
			// 预转换成的图片的宽度和高度
			int newWidth = (int) (width * bigSize);
			Log.e("zoom", "width" + newWidth);
			int newHeight = (int) (height * bigSize);
			// 不断更改中心点坐标截取新的缩放后的图片
			matrix.setScale((float) bigSize, (float) bigSize, (float) newWidth / 2 + moveDis,
					(float) newHeight / 2 + moveUpDownDis);
			resizeBitmap = Bitmap.createBitmap(pc.picList.get(mCurrentIndex).bitmap.getWidth(),
					pc.picList.get(mCurrentIndex).bitmap.getHeight(), pc.picList.get(mCurrentIndex).bitmap.getConfig());
			Canvas canvas = new Canvas(resizeBitmap);
			Paint paint = new Paint();
			canvas.drawBitmap(pc.picList.get(mCurrentIndex).bitmap, matrix, paint);

			pc.picList.get(mCurrentIndex).imageView.setImageBitmap(resizeBitmap);
		}else {
			showNextImage();
		}

		return true;

	}
	/**
	 * 操作时间间隔判断
	 * 
	 * @return
	 */
	private boolean isPermitOperation() {

		boolean result = false;

		long currentOperationTime = System.currentTimeMillis();

		if (currentOperationTime - mLastOperationTime >= ClearConstant.LIVE_QUICK_INSERT_TIME) {
			result = true;
			mLastOperationTime = currentOperationTime;
		}

		return result;
	}

	// 保存当前播放状态 用于给后台展示终端状态
	private void saveStatue(int state, String movieName) {
		Editor editor = activitySharePre.edit();
		editor.putInt(ClearConstant.Play_Statue, state);
		editor.putString(ClearConstant.Movie_NAME, movieName);
		editor.commit();
	}
}
