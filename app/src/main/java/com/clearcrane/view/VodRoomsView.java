/**
 * @author xujifu
 * @copyright clear
 * @date 2014-06-15
 * @description 房间介绍View
 */
package com.clearcrane.view;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clearcrane.log.ClearLog;
import com.clearcrane.log.L;
import com.clearcrane.provider.MaterialRequest;
import com.clearcrane.provider.MaterialRequest.OnCompleteListener;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.TipDialog;
import com.clearcrane.vod.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Calendar;


/**
 * @author JieFengZhiMeng
 *
 */

/**
 * @author JieFengZhiMeng
 *
 */
public class VodRoomsView extends VoDBaseView {
	
	private ArrayList<RoomInfo> room = new ArrayList<RoomInfo>();//房间信息列表
	private String RoomJson;//房间信息获取到的json数据
	private LinearLayout mTitleLeftLinearLayout;//从submenu获取到的图片文字布局
	private RelativeLayout mTitleRelativeLayout;//白线顶端的整个布局
	private LinearLayout mRoomPicLinearLayout;//图片展示列表的layout布局
	private ImageView roomTitlePic;//左上角图片，从submenu获取到
	//private TextView roomTitleName;//左上角菜单名称，从submenu获取到
	private TextView roomKindName;//当前view的名称
	private ImageView baseLine;//布局上的白线
	//private TextView roomFacility;//设施介绍
	private TextView roomIntroduce;//图片介绍
	private TextView roomFocus;//左上角当前图片是第几个图片
	private ImageView select;//低端的提示信息
	private ImageView room_shuiwu_bg_pic;
	
	
	private int curFocusIndex = -1;//当前图片的序号，从0开始
	private long lastTime = 0;//上一次按键时间
	float x;//图片布局移动的距离
	private boolean isLoaded = false;//是否加载完成
	
	private Animation item_anim;//下方文字平移进入动画
	private Animation title_anim;//右上角的submenu进入动画
	public Calendar begin = null;
	private String TAG = "roomsview";
	private int checkNum = 5;
	private boolean roomAdded = false;
	
	public DisplayImageOptions options = new DisplayImageOptions.Builder()  
								.cacheInMemory(false)  
							    .cacheOnDisk(true)  
							    .bitmapConfig(Bitmap.Config.RGB_565)
							    .imageScaleType(ImageScaleType.NONE)
							    .build();
	
	class RoomInfo{
		String url = null;
		String introduce = null;
		String facility = null;
		boolean isDownload = false;
		LinearLayout picLayout;
	}
	/* data provider */
	/* build the view layout/element */
	/* start animation */
	public void init(Context ctx, String u){
		begin = Calendar.getInstance();
		super.init(ctx, u);
		view = LayoutInflater.from(context).inflate(R.layout.room_introduce_view, null);
		initLayoutInXml();
		url = u;
		/* trigger to get data sources */
		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(RoomJsonListen);
		mr.execute(url);
	}
	/**
	 * 在生成子View时调用该方法，该方法需要传入上级菜单项的布局，以供子View使用 
	 */
	public void init(Context ctx, String u, LinearLayout layout){
		begin = Calendar.getInstance();
		super.init(ctx, u, layout);
		url = u;
		view = LayoutInflater.from(context).inflate(R.layout.room_introduce_view, null);
		initLayoutInXml();
		/* trigger to get data sources */
		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(RoomJsonListen);
		mr.execute(url);
	}
	
	/* has to set text after image download completed */
	/**
	 * @author xujifu
	 * 判断房间图片列表是否加载完成，目前只判断5个，以免图片过多导致加载缓慢
	 */
	class OnRoomPicCompleteListerner implements OnCompleteListener {
		RoomInfo roominfo;
		
		public OnRoomPicCompleteListerner(RoomInfo info) {
			roominfo = info;
		}

		@Override
		public void onDownloaded(Object result) {
		}

		@Override
		public void onComplete(boolean result) {
			roominfo.isDownload = true;
			/* check if all image downloaded, if so, show it */
			boolean allDownloaded = true;
			for(int i = 0; i < room.size() && i < 5; i++) {
				if(room.get(i).isDownload == false) {
					allDownloaded = false;
					break;
				}
			}
			
			if(allDownloaded) {
				ResetAllLayoutInView();
			}
			
		}
	}
	
	 class onImageLoadListener implements ImageLoadingListener {
		 	RoomInfo roominfo;
			
			public onImageLoadListener(RoomInfo info) {
				roominfo = info;
			}

			@Override
			public void onLoadingStarted(String imageUri, View view) {
				// TODO Auto-generated method stub
				Log.i(TAG,"START"+roominfo.url);
				roominfo.isDownload = false;
			}

			@Override
			public void onLoadingFailed(String imageUri, View view,
					FailReason failReason) {
				// TODO Auto-generated method stub
				Log.i(TAG,"fail"+roominfo.url);
			}

			@Override
			public void onLoadingComplete(String imageUri, View view,
					Bitmap loadedImage) {
				// TODO Auto-generated method stub
				if(room.size() >= checkNum){
		    		roomAdded = true;
		    	}
				final ImageView iv = (ImageView)roominfo.picLayout.findViewById(R.id.room_item_pic);
				iv.setImageBitmap(loadedImage);
				roominfo.isDownload = true;
				/* check if all image downloaded, if so, show it */
				boolean allDownloaded = true;
				Log.i(TAG,"COMPLETE,movie size:" + room.size()+roominfo.url);
				for(int i = 0; i < room.size() && i < checkNum; i++) {
					if(room.get(i).isDownload == false) {
						allDownloaded = false;
						break;
					}
				}
				
				if(allDownloaded && roomAdded) {
					Log.i(TAG,"all cownloaded");
					ResetAllLayoutInView();
				}
			}

			@Override
			public void onLoadingCancelled(String imageUri, View view) {
				// TODO Auto-generated method stub
				
			}
		}
	
	/**
	 * 所有图片下载完成后重新设置布局内容
	 */
	public void ResetAllLayoutInView() {
		// TODO Auto-generated method stub
		//roomFacility.setVisibility(View.VISIBLE);
		Log.i(TAG,"rest all layoutL:" + room.size());
		roomIntroduce.setVisibility(View.VISIBLE);
		baseLine.setVisibility(View.VISIBLE);
		select.setVisibility(View.VISIBLE);
		roomKindName.setVisibility(View.VISIBLE);
		mRoomPicLinearLayout.setVisibility(View.VISIBLE);
		roomFocus.setVisibility(View.VISIBLE);
		
		baseLine.animate()
			.scaleX(1.0f)
			.setDuration(300)
			.start();
		//roomFacility.startAnimation(item_anim);
		roomIntroduce.startAnimation(item_anim);
		
		for(int i = 0; i < room.size(); i++){
			LayoutParams lp = new LayoutParams(
					LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			lp.bottomMargin = 60;
			mRoomPicLinearLayout.addView(room.get(i).picLayout, lp);
			TranslateAnimation transanim = new TranslateAnimation(-10, 0, 10, 0);
			AlphaAnimation alphaanim = new AlphaAnimation(0.0f, 1.0f);
			AnimationSet set = new AnimationSet(true);
			set.addAnimation(transanim);
			set.addAnimation(alphaanim);
			set.setDuration(300);
			set.setStartOffset(100 * i);
			room.get(i).picLayout.startAnimation(set);
		}
		if(curFocusIndex < 0){
			//first focus
			onFocusChanged(-1, 0);
			curFocusIndex = 0;
			roomFocus.setText("" + (curFocusIndex + 1) + "/" + room.size());
			room_shuiwu_bg_pic.setVisibility(View.VISIBLE);
		}
		isLoaded = true;
	}
	
	/**
	 * Json获取完成监听
	 */
	private OnCompleteListener RoomJsonListen = new OnCompleteListener(){

		@Override
		public void onDownloaded(Object result) {
			// TODO Auto-generated method stub
			RoomJson = (String)result;
			if(RoomJson == null){
				TipDialog.Builder builder = new TipDialog.Builder(context);
				builder.setMessage("当前网络不可用，请检查网络");  
		        builder.setTitle("提示");  
		        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {  
		            public void onClick(DialogInterface dialog, int which) {  
		                dialog.dismiss();  
		                //设置你的操作事项  
		            }  
		        });  
		  
		        builder.create().show();  
				return;
			}
			try{
				JSONTokener jsonParser = new JSONTokener(RoomJson);  
			    JSONObject mainViewObj = (JSONObject) jsonParser.nextValue();  
			    JSONArray contentArray = (JSONArray)mainViewObj.getJSONArray("Content");
			    String introduceCHN = null;
			    String introduceENG = null;
			    for(int i = 0; i < contentArray.length(); i ++){
			    	JSONObject objecttmp = (JSONObject)contentArray.opt(i);
			    	RoomInfo roominfo = new RoomInfo();
			    	roominfo.url = ClearConfig.getJsonUrl(context, objecttmp.getString("Picurl"));
			    	Log.i("in jsonparser",url);
			    	roominfo.facility = objecttmp.getString("Facility");
			    	introduceCHN = objecttmp.getString("Introduce");
			    	introduceENG = objecttmp.getString("Introduce_eng");
			    	roominfo.introduce = ClearConfig.getStringByLanguageId(introduceCHN,introduceENG);
			    	roominfo.picLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.room_item, null);
					/*ImageView iv = (ImageView)roominfo.picLayout.findViewById(R.id.room_item_pic);
			    	MaterialRequest mr = new MaterialRequest(context, iv, ClearConfig.TYPE_IMAGE);
			    	if(i < 5)
			    		mr.setOnCompleteListener(new OnRoomPicCompleteListerner(roominfo));
					mr.execute(roominfo.url);*/
			    	String url = roominfo.url;
			    	if(!url.startsWith("http"))
						url = Scheme.FILE.wrap(roominfo.url);
					if(i < checkNum){
						
			    		ImageLoader.getInstance().loadImage(url, options, new onImageLoadListener(roominfo));
			    	}else{
			    		ImageView iv = (ImageView)roominfo.picLayout.findViewById(R.id.room_item_pic);
			    		ImageLoader.getInstance().displayImage(url, iv, options);
			    	}
					ImageView ivborder = (ImageView)roominfo.picLayout.findViewById(R.id.room_item_border);
					ivborder.setAlpha(0.0f);
			    	room.add(roominfo);
			    	
			    }
			    roomAdded =true;
			    if(!room.isEmpty()){
					//roomFacility.setText(room.get(0).facility);
					roomIntroduce.setText(room.get(0).introduce);
					((ImageView)room.get(0).picLayout.findViewById(R.id.room_item_border)).setAlpha(1.0f);
					((ImageView)room.get(0).picLayout.findViewById(R.id.room_item_background)).setAlpha(0.0f);
					
					if(room.size()>1){
					x = room.get(0).picLayout.getX() + room.get(0).picLayout.getWidth()
							- (room.get(1).picLayout.getX() + room.get(1).picLayout.getWidth());
					}else{
						x = room.get(0).picLayout.getX() + room.get(0).picLayout.getWidth();
					}
				}
			    if(begin != null) {
                	long between = (Calendar.getInstance()).getTimeInMillis()-begin.getTimeInMillis(); 
                    ClearLog.LogInfo("BROSWER\tLoad\tSUCC\t" + between +"ms\t" + url + "\t"
                    		+ "language");
                } 
			}catch (JSONException e) {
				ClearLog.LogError("BROSWER\tLoad\tFAIL\t0ms\t" + url);
				e.printStackTrace();
			}
		}

		@Override
		public void onComplete(boolean result) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	private void initLayoutInXml(){
		mTitleRelativeLayout = (RelativeLayout)view.findViewById(R.id.room_title);
		mTitleLeftLinearLayout = (LinearLayout)view.findViewById(R.id.room_title_left);
		mRoomPicLinearLayout = (LinearLayout)view.findViewById(R.id.room_pic);
		//roomTitleName = (TextView)view.findViewById(R.id.room_title_name);
		
		
		roomFocus = (TextView)view.findViewById(R.id.room_curFocus);
		//roomFacility = (TextView)view.findViewById(R.id.room_facility);
		roomIntroduce = (TextView)view.findViewById(R.id.room_introduce);
		baseLine = (ImageView)view.findViewById(R.id.room_baseline);
		select = (ImageView)view.findViewById(R.id.room_select);
		room_shuiwu_bg_pic = (ImageView)view.findViewById(R.id.room_shuiwu_bg_pic);
		
		roomTitlePic = (ImageView)view.findViewById(R.id.room_title_pic);
		roomKindName = (TextView)view.findViewById(R.id.room_title_kindname);
		
		roomKindName.setTextColor(Color.rgb(255, 255, 255));
		roomKindName.setVisibility(View.GONE);
		if(menuLinearLayout != null){
			ImageView iconView = ((ImageView)menuLinearLayout.
					findViewById(R.id.sub_pic_text_wrapcontent_pic));
			
			iconView.setDrawingCacheEnabled(true); 
			roomTitlePic.setImageBitmap(
					Bitmap.createBitmap(iconView.getDrawingCache()));
			iconView.setDrawingCacheEnabled(false);
				
			roomKindName.setText(((TextView)menuLinearLayout.
					findViewById(R.id.sub_pic_text_wrapcontent_text)).getText());
		}
		else{
			Log.i("key", "in room view name:"+nameInIcon);
			roomKindName.setText(nameInIcon);
			Log.i("key", "get text view name:"+roomKindName.getText());
		}
		
		item_anim = AnimationUtils.loadAnimation(context, R.anim.room_item_anim);
		title_anim = AnimationUtils.loadAnimation(context, R.anim.room_title_anim);
		
		mRoomPicLinearLayout.setVisibility(View.GONE);
		//roomFacility.setVisibility(View.GONE);
		roomIntroduce.setVisibility(View.GONE);
		roomFocus.setVisibility(View.GONE);
		baseLine.setVisibility(View.GONE);
		select.setVisibility(View.GONE);
		
		mTitleRelativeLayout.startAnimation(title_anim);
	}
	
	private void onFocusChanged(int oldIndex, int newIndex) {
		// TODO Auto-generated method stub
		if(oldIndex >= 0){
			((ImageView)room.get(oldIndex).picLayout.findViewById(R.id.room_item_border))
				.animate()
				.alpha(0.0f)
				.setDuration(200)
				.start();
			((ImageView)room.get(oldIndex).picLayout.findViewById(R.id.room_item_background))
				.animate()
				.alpha(1.0f)
				.setDuration(200)
				.start();
	    	final int index = oldIndex;
			//ValueAnimator.ofInt(int,int)设定了值的变化范围
	    	final ValueAnimator valueAnimator=ValueAnimator.ofInt(0, 60);
	    	//利用AnimatorUpdateListener监听动画的执行
	    	valueAnimator.addUpdateListener(new AnimatorUpdateListener() {	
				@Override
				public void onAnimationUpdate(ValueAnimator va) {
					//得到动画当前执行的值
					//这些值均在(20,100)之间
					Integer animatedValue = (Integer)va.getAnimatedValue();
					MarginLayoutParams marginLayoutParams = (MarginLayoutParams) room.get(index).picLayout.getLayoutParams();
					//不断修改Button的Margin值
					marginLayoutParams.bottomMargin = animatedValue;
					room.get(index).picLayout.setLayoutParams(marginLayoutParams);
				}
			});			
	    	valueAnimator.setDuration(200);
	    	valueAnimator.setTarget(room.get(oldIndex).picLayout);
	    	valueAnimator.start();
		}
		if(newIndex >= 0){
			
			//roomFacility.setText(room.get(newIndex).facility);
			roomIntroduce.setText(room.get(newIndex).introduce);
			//roomFacility.startAnimation(item_anim);
			roomIntroduce.startAnimation(item_anim);
			((ImageView)room.get(newIndex).picLayout.findViewById(R.id.room_item_border))
				.animate()
				.alpha(1.0f)
				.setDuration(200)
				.start();
			((ImageView)room.get(newIndex).picLayout.findViewById(R.id.room_item_background))
				.animate()
				.alpha(0.0f)
				.setDuration(200)
				.start();

	    	final int index = newIndex;
			//ValueAnimator.ofInt(int,int)设定了值的变化范围
			final ValueAnimator valueAnimator=ValueAnimator.ofInt(60, 0);
			//利用AnimatorUpdateListener监听动画的执行
			valueAnimator.addUpdateListener(new AnimatorUpdateListener() {	
				@Override
				public void onAnimationUpdate(ValueAnimator va) {
					Integer animatedValue = (Integer)va.getAnimatedValue();
					MarginLayoutParams marginLayoutParams = 
							(MarginLayoutParams) room.get(index).picLayout.getLayoutParams();
					if(marginLayoutParams != null){
						marginLayoutParams.bottomMargin = animatedValue;
						room.get(index).picLayout.setLayoutParams(marginLayoutParams);
						
					}
				}
			});			
			valueAnimator.setDuration(200);
			valueAnimator.setTarget(room.get(newIndex).picLayout);
			valueAnimator.start();
		}
	}
	
	public boolean onKeyDpadLeft(){
		if(isLoaded == false){
			return true;
		}
		long last = System.currentTimeMillis();
		if(last - lastTime < 300){
			return true;
		}
		lastTime = last;
		if(0 == curFocusIndex){
			return true;
		}
		int newFocusIndex = -1;
		L.i("args" + curFocusIndex);
		if(curFocusIndex < 0){
			newFocusIndex = 0;
		}else{
			float x = (room.get(curFocusIndex - 1).picLayout.getX())
					- room.get(curFocusIndex).picLayout.getX();
			mRoomPicLinearLayout.animate()
				.translationXBy(-x)
				.setDuration(200)
				.start();
			newFocusIndex = (curFocusIndex + room.size() - 1) % room.size();
		}
		
		onFocusChanged(curFocusIndex, newFocusIndex);
		curFocusIndex = newFocusIndex;
		roomFocus.setText("" + (curFocusIndex + 1) + "/" + room.size());
		return true;
	}

	public boolean onKeyDpadRight(){
		if(isLoaded == false){
			return true;
		}
		long last = System.currentTimeMillis();
		if(last - lastTime < 300){
			return true;
		}
		lastTime = last;
		if((room.size() - 1) == curFocusIndex)
			return true;
		int newFocusIndex = -1;
		L.i("args" + curFocusIndex);
		if(curFocusIndex < 0){
			newFocusIndex = 0;
		}else{
			float x = room.get(curFocusIndex).picLayout.getX() + room.get(curFocusIndex).picLayout.getWidth()
					- (room.get(curFocusIndex + 1).picLayout.getX() + room.get(curFocusIndex + 1).picLayout.getWidth());
			mRoomPicLinearLayout.animate()
			.translationXBy(x)
			.setDuration(200)
			.start();
			newFocusIndex = (curFocusIndex + 1) % room.size();
		}
		onFocusChanged(curFocusIndex, newFocusIndex);
		curFocusIndex = newFocusIndex;
		roomFocus.setText("" + (curFocusIndex + 1) + "/" + room.size());
		return true;
	}
	
	public boolean onKeyEnter(){
		if(isLoaded == false){
			return true;
		}
		
		if(curFocusIndex < 0 || curFocusIndex >= room.size()) {
			return true;
		}
		
		VodFullImageShowView newView = (VodFullImageShowView) VoDViewManager.newViewByType("FullImageShow");
		if(newView != null) {
			
			newView.init(context, "");	
			newView.setPicURL(room.get(curFocusIndex).url);
			/* show the sub view */
			VoDViewManager.getInstance().pushForegroundView(newView);
		}
		
		return true;
	}
}
