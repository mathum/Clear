package com.clearcrane.logic.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.android.internal.widget.SizeAdaptiveLayout.LayoutParams;
import com.clearcrane.constant.ClearConstant;
import com.clearcrane.entity.Picture;
import com.clearcrane.view.VoDBaseView;
import com.clearcrane.vod.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BannerView extends VoDBaseView {
	private String TAG = this.getClass().getSimpleName();
	private LinearLayout showLayout;
	private DisplayImageOptions options;
	private List<Picture> banners = new ArrayList<>();
	private int currentPosition = -1;
	private String[] strs;
	private long intervalTime = 5000;
	private Handler Handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0) {
				currentPosition++;
				Log.e("xb", "currentPosition:" + currentPosition);
				switchPic(currentPosition);
			}
		};
	};

	@Override
	public void init(Context ctx, String u) {
		// TODO Auto-generated method stub
		super.init(ctx, u);
		context = ctx;
		url = u.trim();
		String string = url.substring(1,url.length()-1);
		strs=string.trim().split(",");
		//设置轮播间隔时间
		SharedPreferences mSharedPreferences = ctx.getSharedPreferences(ClearConstant.STR_INTER_CUT, Context.MODE_PRIVATE);
		String ti = mSharedPreferences.getString(ClearConstant.STR_TIME_INTERVAL,"");
		Log.e("123", "time:"+intervalTime);
		if (ti != null && !ti.equals("")) {
			intervalTime = Integer.parseInt(ti)*1000;
			Log.e("123", "time:"+intervalTime);
		}
		view = LayoutInflater.from(context).inflate(R.layout.view_banner, null);
		options = new DisplayImageOptions.Builder().cacheInMemory(true) // 设置下载的图片是否缓存在内存中
				.cacheOnDisk(true) // 设置下载的图片是否缓存在SD卡中
				// .displayer(new FadeInBitmapDisplayer(500))// 图片加载好后渐入的动画时间
//				.imageScaleType(ImageScaleType.EXACTLY)// 设置图片的缩放方式
				.bitmapConfig(Bitmap.Config.ARGB_8888).build();

		initLayout();
		initData();
	}

	public void initLayout() {
		showLayout = (LinearLayout) view.findViewById(R.id.view_banner_showLayout);
	}

	public void initData() {
		for(int i = 0;i < strs.length;i++){
			Picture picture = new Picture();
			String s = strs[i].replace("\\", "");
			picture.path = s;		
			picture.path = s.replace("\"","");
			banners.add(picture);
		}

		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Handler.sendEmptyMessage(0);
			}
		}, 0, intervalTime);
	}
    //切换的方法
	private void switchPic(int position) {
		if (position >= banners.size()) {
			currentPosition = 0;
			position = 0;
		}
		showLayout.removeAllViews();
		// 删除上一张图片的资源
		if (currentPosition == 0) {
			removeImage(banners.size() - 1);
		}
		if (currentPosition >= 1) {
			removeImage(position - 1);
		}
		// 加载当前位置的图片
		loadCurrentImage(position);
		// 预加载下一张图片
		if (currentPosition == banners.size() - 1) {
			downloadImage(0);
		}else {			
			downloadImage(currentPosition + 1);
		}
		Log.e("xb", banners.get(position).path);
		showLayout.addView(banners.get(position).imageView);
	}

	private void loadCurrentImage(int curPos) {
		if (banners.get(curPos).bitmap != null) {
			banners.get(curPos).imageView = new ImageView(context);
			banners.get(curPos).imageView.setScaleType(ScaleType.FIT_XY);
			banners.get(curPos).imageView.setLayoutParams(
					new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			banners.get(curPos).imageView.setImageBitmap(banners.get(curPos).bitmap);
		} else {
			downloadImage(curPos);
		}
	}

	private void downloadImage(int index) {
		if (index < 0 || index > banners.size() - 1) {
			return;
		}

		final Picture picture = banners.get(index);
		picture.imageView = new ImageView(context);
		picture.imageView.setScaleType(ScaleType.FIT_XY);
		picture.imageView
				.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		// String url = ClearConfig.getJsonUrl(context, picture.path);
		Log.e("123", picture.path);
		ImageLoader.getInstance().displayImage(picture.path, picture.imageView, options, new ImageLoadingListener() {

			@Override
			public void onLoadingStarted(String imageUri, View view) {
			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				picture.bitmap = loadedImage;
			}

			@Override
			public void onLoadingCancelled(String imageUri, View view) {

			}

		});
	}

	private void removeImage(int index) {
		banners.get(index).imageView = null;
		banners.get(index).bitmap = null;
	}
}
