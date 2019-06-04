package com.clearcrane.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class ImageUtil {

	/**
	 * 初始化ImageLoader
	 * 
	 * @param ctx
	 */
	public static void initImageLoader(Context ctx) {
		// 创建配置ImageLoader(所有的选项都是可选的,只使用那些你真的想定制)，这个可以设定在APPLACATION里面，设置为全局的配置参数
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true)
				.cacheOnDisc(true)
				.imageScaleType(ImageScaleType.NONE_SAFE)
				.bitmapConfig(Bitmap.Config.ARGB_8888)
				.build();

		ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(ctx)
				.memoryCacheExtraOptions(1280, 700)
				.memoryCache(new WeakMemoryCache())
				.memoryCacheSize(2*1024*1024)
				.discCacheSize(50*1024*1024)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.defaultDisplayImageOptions(defaultOptions)
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.build();
		ImageLoader.getInstance().init(configuration);
	}

	public static void displayImage(String url, ImageView mImageView) {
		ImageLoader.getInstance().displayImage(url, mImageView);
	}
	public static void displayImage(String url, ImageView mImageView,ImageLoadingListener imageLoadingListener) {
		ImageLoader.getInstance().displayImage(url, mImageView,imageLoadingListener);
	}
}
