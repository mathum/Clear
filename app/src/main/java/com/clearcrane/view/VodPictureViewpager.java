package com.clearcrane.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clearcrane.adapter.LoopImageAdapter;
import com.clearcrane.log.ClearLog;
import com.clearcrane.provider.MaterialRequest;
import com.clearcrane.provider.MaterialRequest.OnCompleteListener;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.TipDialog;
import com.clearcrane.vod.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Calendar;

public class VodPictureViewpager extends VoDBaseView {
	
	protected static final String TAG = "VodPictureViewpager";
	private static final int HEADER_IMAGE_CHANGE_TIME = 3000;// 延时3秒
	private static final int TOP_MARGIN_HEIGHT_PX = 0;
	private static final int BOTTOM_MARGIN_HEIGHT_PX = 0;
	private static final int LEFT_MARGIN_HEIGHT_PX = 64;
	private static final int RIGHT_MARGIN_HEIGHT_PX = 64;
	private String dataJson;//json数据
	public Calendar begin = null;
	private ViewPager mVpContent;
	private LoopImageAdapter loopImageAdapter;
	private TextView mTvPropotion;//显示比例 如：5/6
	private ArrayList<ImageView> mImageViewList = new ArrayList<ImageView>();;
	private Handler mHandler;
	private int mPlayPosition;
	private PictureCategory pc;
	private boolean isFirstLoad = true;
	
	public void init(Context ctx, String u) {
		begin = Calendar.getInstance();
		context = ctx;
		url = u;
		view = LayoutInflater.from(context).inflate(R.layout.picture_viewpager_view, null);
		initLayoutInXml();
		
		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(DataJsonListen);
		mr.execute(url);
	}
	
	public void init(Context ctx, String u, LinearLayout layout) {
		begin = Calendar.getInstance();
		context = ctx;
		url = u;
		menuLinearLayout = layout;
		view = LayoutInflater.from(context).inflate(R.layout.picture_viewpager_view, null);
		initLayoutInXml();
		
		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(DataJsonListen);
		mr.execute(url);
		
	}
	
	public class PictureCategory {
		public String count;
		public String label;
		public String type;
		ArrayList<Picture> picList = new ArrayList<Picture>();
	}
	
	public class Picture {
		String index;
		String path;
	}
	
	private void initLayoutInXml() {
		mVpContent = (ViewPager) view.findViewById(R.id.vp_content);

		ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mVpContent.getLayoutParams();
		lp.topMargin += TOP_MARGIN_HEIGHT_PX;
		lp.bottomMargin += BOTTOM_MARGIN_HEIGHT_PX;
		lp.leftMargin += LEFT_MARGIN_HEIGHT_PX;
		lp.rightMargin += RIGHT_MARGIN_HEIGHT_PX;
		
//		DisplayMetrics dm = new DisplayMetrics();
//		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		mVpContent.setLayoutParams(lp);
		mTvPropotion = (TextView) view.findViewById(R.id.tv_propotion);
	}
	
	/**
	 * Json获取完成监听
	 */
	private OnCompleteListener DataJsonListen = new OnCompleteListener(){


		@Override
		public void onDownloaded(Object result) {
			dataJson = (String)result;
			if(dataJson == null){
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
			try{
				JSONTokener jsonParser = new JSONTokener(dataJson);  
			    JSONObject mainViewObj = (JSONObject) jsonParser.nextValue(); 
			    pc = new PictureCategory();
			    pc.count =  mainViewObj.getString("Count");
			    pc.label =  mainViewObj.getString("Label");
			    pc.type =  mainViewObj.getString("Type");
			    
			    JSONArray contentArray = (JSONArray)mainViewObj.getJSONArray("Content");

			    for(int i = 0; i < contentArray.length(); i ++){
			    	JSONObject objecttmp = (JSONObject)contentArray.opt(i);
		    		Picture p = new Picture();
		    		p.index = objecttmp.getString("index");
		    		p.path = objecttmp.getString("path");
		    		pc.picList.add(p);
			    }
			    
			    if(begin != null) {
                	long between = (Calendar.getInstance()).getTimeInMillis()-begin.getTimeInMillis(); 
                    ClearLog.LogInfo("BROSWER\tLoad\tSUCC\t" + between +"ms\t" + url + "\t"
                    		+ "picturListView");
                } 
			}catch (JSONException e) {
				ClearLog.LogError("BROSWER\tLoad\tFAIL\t0ms\t" + url);
				e.printStackTrace();
			}
		}

		@Override
		public void onComplete(boolean result) {
			carousel();
		}
		
	};
	
	/**
	 * 轮播图片逻辑
	 */
	public void carousel() {
			mTvPropotion.setText(1+"/"+pc.picList.size());
			LoadNextImageView();
			
		if (mHandler == null) {
			// 创建轮播条的Handler
			mHandler = new Handler() {
				public void handleMessage(android.os.Message msg) {
					int item = mVpContent.getCurrentItem();

					if (item < mImageViewList.size() - 1) {
						item++;
					} else {// 判断是否到达最后一个
						item = 0;
					}
					mTvPropotion.setText((item+1)+"/"+pc.picList.size());
					Log.d(TAG, "轮播条:" + item);
					mVpContent.setCurrentItem(item);
					long startTime = SystemClock.uptimeMillis();
					if(mPlayPosition != -1){
						LoadNextImageView();
					}
					int deltaTime = (int)(SystemClock.uptimeMillis() - startTime);
					int realDelayTime = HEADER_IMAGE_CHANGE_TIME - deltaTime;
					if(realDelayTime>0){
						mHandler.sendEmptyMessageDelayed(0,
								realDelayTime);
					}else{
						mHandler.sendEmptyMessage(0);
					}
					
				};
			};

			mHandler.sendEmptyMessageDelayed(0, HEADER_IMAGE_CHANGE_TIME);// 延时3s发送消息
		}
	}

	/**加载下一张展示图片*/
	private void LoadNextImageView() {
		
		if(mPlayPosition<pc.picList.size()){
			
			final String url = ClearConfig.getJsonUrl(context, pc.picList.get(mPlayPosition).path);
			if (!TextUtils.isEmpty(url)) {
				
				MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_IMAGE);
				mr.setOnCompleteListener(new OnCompleteListener() {
					
					@Override
					public void onDownloaded(Object result) {
						final ImageView imageView = new ImageView((Activity)context);
						imageView.setScaleType(ScaleType.CENTER_CROP);
						imageView.setImageBitmap((Bitmap)result);
						mImageViewList.add(imageView);
						mTvPropotion.setTextColor(Color.BLACK);
						mPlayPosition ++;
						if (mImageViewList != null && mImageViewList.size() > 0) {
							
							if(isFirstLoad){
								isFirstLoad = false;
								loopImageAdapter = new LoopImageAdapter((Activity)context,
										mImageViewList);
								mVpContent.setAdapter(loopImageAdapter);
							}else{
								loopImageAdapter.setObjects(mImageViewList);
								loopImageAdapter.notifyDataSetChanged();
							}
						}
					}
					
					@Override
					public void onComplete(boolean result) {
					}
				});
				mr.execute(url);
			}
		}else{
			mPlayPosition = -1;
		}
	}
}
