package com.clearcrane.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clearcrane.adapter.ClearVideoSetsAdapterLand;
import com.clearcrane.constant.ClearConstant;
import com.clearcrane.log.ClearLog;
import com.clearcrane.provider.MaterialRequest;
import com.clearcrane.provider.MaterialRequest.OnCompleteListener;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.LogUtils;
import com.clearcrane.util.MarqueeTextView;
import com.clearcrane.util.VideoInfo;
import com.clearcrane.vod.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;

public class VideoSetsView extends VoDBaseView {

	
	private final String VIDEO_URL = "Video_URL";
	private String videoSetJson;
	ImageView ivCover;
	TextView tvIntroduction, tvVideosetsName;
	GridView gvVideosets;
	ArrayList<VideoInfo> videoSets;//接收的所有电影数据
	ArrayList<VideoInfo> videoPerPageShow; // 每页需要显示的影片集合
	LinearLayout llDivide;
	ClearVideoSetsAdapterLand videoSetsAdapter;
	TextView[] tv_labels;
	private int label_index = 0; // 菜单的位置指针
//	private int abs_label_index = 0; // 主菜单的绝对位置序号
	private int LABELS_ENABLED = 5;  //菜单最多有几个分页标签
	private final int MAX_COUNTS_PER_PAGE = 20;//每页最多多少条数据
//	private boolean isGridViewFocused;
//	private int curPage = 1;
	private int maxPage = 0; //最大页数 
//	private int videoSize = 0;

	private String ViewName = "VideoSetsView";
	private static final String TAG = "VideoSetsView";
	public SharedPreferences activitySharePre;
	private SharedPreferences sp;
//	private long lastTime;

	private int currentPagePosition = 0;

	private int currentPageColumn = 0; //记录在哪一列，主要用于判断是否到达gridview左右边界，控制切换下一页或上一页
	
	public void init(Context ctx, String u) {
		super.init(ctx, u);

		url = u;
		view = LayoutInflater.from(context).inflate(R.layout.prison_videosets_view, null);
		initLayoutInXml();

		activitySharePre = context.getSharedPreferences(ClearConstant.Activity_FILE, Context.MODE_PRIVATE);
        sp =context.getSharedPreferences(ClearConstant.VIDEOSETS, Context.MODE_PRIVATE);
		saveStatue();

		videoSets = new ArrayList<VideoInfo>();
		videoPerPageShow = new ArrayList<VideoInfo>();
		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(videoSetsListener);
		mr.execute(url);
	}
    
	private void saveStatue() {
		Editor editor = activitySharePre.edit();
		editor.putInt(ClearConstant.Play_Statue, 3);
		editor.putString(ClearConstant.VIEW_NAME, ViewName);
		editor.commit();
	}
	private void saveVideoSets(String videoSetJson){
    	Editor editor = sp.edit();
		editor.putString(ClearConstant.VIDEOSETSJSON, videoSetJson);
		editor.commit();
		Log.e("zxb", "保存json完毕");
    }
	//初始化控件操作
	private void initLayoutInXml() {

		ivCover = (ImageView) view.findViewById(R.id.iv_cover);
		tvIntroduction = (TextView) view.findViewById(R.id.tv_introduction);
		tvVideosetsName = (TextView) view.findViewById(R.id.tv_videosets_name);
		gvVideosets = (GridView) view.findViewById(R.id.gv_videosets);

		//设置gridview中心点在屏幕的下方  控件的中点  以这个位置进行缩放
		gvVideosets.setPivotX(gvVideosets.getWidth()/2);
		gvVideosets.setPivotY(ClearConfig.getScreenHeight());
		
		//数据进入动画
		ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(gvVideosets,"scaleY", 0f, 1.0f);
		objectAnimator.setDuration(500);
		objectAnimator.start();
		
		//gridview的item点击事件监听
		gvVideosets.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				currentPagePosition = arg2;
				onKeyEnter();
			}
		});

		llDivide = (LinearLayout) view.findViewById(R.id.ll_divide);
		llDivide.setFocusable(true);
		llDivide.requestFocus();

		tv_labels = new TextView[5];
		tv_labels[0] = (TextView) view.findViewById(R.id.tv_label1);
		tv_labels[1] = (TextView) view.findViewById(R.id.tv_label2);
		tv_labels[2] = (TextView) view.findViewById(R.id.tv_label3);
		tv_labels[3] = (TextView) view.findViewById(R.id.tv_label4);
		tv_labels[4] = (TextView) view.findViewById(R.id.tv_label5);

		for (int i = 0; i < tv_labels.length; i++) {
			tv_labels[i].setFocusable(true);
		}

		cancelAllLablesActived();
	}

	//从vodmovieview返回到本页面时会调用该方法
	@Override
	public void back() {
		// TODO Auto-generated method stub
			gvVideosets.requestFocus();
			gvVideosets.setSelection(currentPagePosition);
//			LogUtils.sendLogEnd(mApp, "点播", "视频", videoSets.get(currentPagePosition + (label_index * 20)).name);
	}

	@Override
	public boolean onKeyBack() {
		if (!gvVideosets.hasFocus()) {
			return super.onKeyBack();
		} else {
			gvVideosets.clearFocus();
			setLabelFocused();
		}
		return true;
	}

	@Override
	public boolean onKeyDpadLeft() {
		// 当焦点在tab上时,控制tab切换
		if (!gvVideosets.hasFocus()) {
			handleLeft();
		} else {
			// 当焦点在gridview上时
			currentPageColumn--;
			Log.i("currentPageColumn", currentPageColumn+"");
			if(currentPageColumn <= 0){
				currentPageColumn = 0;
				handleLeft();
				gvVideosets.requestFocus();
				gvVideosets.setSelection(currentPagePosition);
			}

		}
		return true;
	}

	@Override
	public boolean onKeyDpadRight() {    
		// 当焦点在tab上时
		if (!gvVideosets.hasFocus()) {
			handleRight();
		} else {
			// 当焦点在gridview上时
			currentPageColumn++;
			Log.e("eeeee", "onkeyDpadRight:我监听到了");
			Log.i("currentPageColumn", currentPageColumn+"");
			if(currentPageColumn >= 1){
				currentPageColumn = 0;
				handleRight();
				gvVideosets.requestFocus();
				gvVideosets.setSelection(currentPagePosition);
			}
		}
		return true;
	}

	@Override
	public boolean onKeyDpadUp() {
		// 当gridview到达上面的边界的时候  切换焦点到label菜单
		if (gvVideosets.getSelectedItemPosition() - 1 < 0 || gvVideosets.getSelectedItemPosition() - 2 < 0) {
			setLabelFocused();
			gvVideosets.setSelection(-1);
			scrollControl(-1);
		} 
		return true;
	}

	@Override
	public boolean onKeyDpadDown() {
		// 当焦点不在gridview上时
		if (!gvVideosets.hasFocus()) {
			// gridview获取焦点
			gvVideosets.requestFocus();
			gvVideosets.setSelection(0);
		}
		return true;
	}
  
	// @Override
	public boolean onKeyEnter() {
		// TODO Auto-generated method stub
		Log.e(TAG, "on key enter!");
		// && isGridViewFocused
		if (!videoSets.isEmpty() && gvVideosets.hasFocus()) {
			VoDViewManager.getInstance().stopBackgroundVideo();
			VoDViewManager.getInstance().hideBackgroundVideo();
			VoDMovieView movieView = new VoDMovieView();

			int position = currentPagePosition + (label_index * 20);
			movieView.init(context, videoSets.get(position).source_url, videoSets.get(position).name);
			// mApp.resourceName = videoSetsAdapter.getSelectedVideo().name;
//			mApp.resourceName = videoSets.get(position).name;
//			String categoryPath = mApp.content + mApp.contentleft;
//			mApp.timeInS = DateUtil.getCurrentTimeSecond();
//			String logInsert = mApp.combinatePostParasString("start", "0", "点播", mApp.viewType, mApp.resourceName,
//					categoryPath);
//			Log.i(TAG, " categoryPath START: " + logInsert);
//			ClearLog.logInsert(logInsert);
//			mApp.SendLogMode = 1;
			LogUtils.sendLogStart(mApp, "点播", "视频", videoSets.get(position).name);

			VoDViewManager.getInstance().pushForegroundView(movieView);
		}
		return false;
	}

	// 切换lab时 控制gridview刷新 并且设置相应的lab高亮
	private void handleLeft() {
		label_index--;
		if (label_index < 0) {
			label_index = 0;
			return;
		}
		showGridViewAnim();  //展示切换动画
		addToPerPage(label_index); //向videoPerPageShow添加需要显示的电影集合
		setLabelFocused();//设置lab切换
		currentPagePosition = gvVideosets.getSelectedItemPosition();//记录用户在本界面按到的位置
//		currentPageColumn = 0; //将列数统计归零  方便下一次使用
	}

	// lab切换时，控制焦点移动，并且刷新gridview
	private void handleRight() {
		label_index++;
		if (label_index > maxPage - 1) {
			label_index = maxPage - 1;
			return;
		}
		showGridViewAnim();
		Log.i("abs", "" + maxPage);
		addToPerPage(label_index);
		setLabelFocused();
		currentPagePosition = gvVideosets.getSelectedItemPosition();
	}

	//取消所有lab的点中状态
	private void cancelAllLablesActived() {
		for (int i = 0; i < tv_labels.length; i++) {
			tv_labels[i].setActivated(false);
		}
	}

	// 设置相应的lab高亮
	private void setLabelActived() {
		cancelAllLablesActived();
		// if (tv_labels.length > label_index) {
		if (tv_labels[label_index] != null) {
			tv_labels[label_index].setActivated(true);
		}
		// }
	}

	// 设置相应的lab获得焦点
	private void setLabelFocused() {
		cancelAllLablesActived();
		setLabelActived();
		// if (tv_labels.length > label_index) {
		if (tv_labels[label_index] != null) {
			tv_labels[label_index].requestFocus();
		}
		// }
	}

	// 设置lab显示多少个
	private void setLabelVisibilty(int max) {
		// 设置需要显示的lab
		for (int i = 0; i < max; i++) {
			tv_labels[i].setVisibility(view.VISIBLE);
		}
		// 设置需要关闭的lab
		for (int i = max; i < 5; i++) {
			tv_labels[i].setVisibility(view.GONE);
		}
	}
    //对网络数据的处理
	private OnCompleteListener videoSetsListener = new OnCompleteListener() {

		@Override
		public void onDownloaded(Object result) {
			// TODO Auto-generated method stub
			videoSetJson = (String) result;
			saveVideoSets(videoSetJson);
			Log.i("Json", "Json:" + videoSetJson);
			if (videoSetJson == null) {
//				TipDialog.Builder builder = new TipDialog.Builder(context);
//				builder.setMessage("当前网络不可用，请检查网络");
//				builder.setTitle("提示");
//				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//						// 设置你的操作事项
//					}
//				});
//
//				builder.create().show();
				Log.i("Json", "Json: is null check internet!");
				return;
			}
			try {
				JSONTokener jsonParser = new JSONTokener(videoSetJson);
				JSONObject mainViewObj = (JSONObject) jsonParser.nextValue();
				String labelString = mainViewObj.getString("Label");
				String introduction = mainViewObj.getString("Introduction");
				String Cover_URL = ClearConfig.getJsonUrl(context, mainViewObj.getString("Cover_URL"));
				tvVideosetsName.setText(labelString);
				tvIntroduction.setText(introduction);
				new MaterialRequest(context, ivCover, ClearConfig.TYPE_IMAGE).execute(Cover_URL);

				JSONArray contentArray = (JSONArray) mainViewObj.getJSONArray("Content");

				for (int i = 0; i < contentArray.length(); i++) {
					JSONObject objecttmp = (JSONObject) contentArray.opt(i);
					VideoInfo videoInfo = new VideoInfo();
					videoInfo.name = objecttmp.getString("name");
					videoInfo.videoId = objecttmp.getInt("index");
					videoInfo.nextVideoId = objecttmp.getInt("Next_Video_index");
					//due to the distributed,use abs path
					if(objecttmp.getString(VIDEO_URL).startsWith("http"))
						videoInfo.source_url = objecttmp.getString(VIDEO_URL);
					else					
						videoInfo.source_url = ClearConfig.getJsonUrl(context, objecttmp.getString(VIDEO_URL));
					videoSets.add(videoInfo);
					// or add with index?
					// videoSets.add(i,videoInfo);
				}
				LABELS_ENABLED = ((contentArray.length() - 1) / MAX_COUNTS_PER_PAGE + 1);
				maxPage = ((contentArray.length() - 1) / MAX_COUNTS_PER_PAGE) + 1;
				Log.i("navi", ":" + maxPage + " :" + LABELS_ENABLED);
				// 设置需要多少个lab、同时设置每隔lab显示集数
				setLabelVisibilty(maxPage);
				setLabelFocused();

				videoSetsAdapter = new ClearVideoSetsAdapterLand(videoPerPageShow, context);
				gvVideosets.setAdapter(videoSetsAdapter);
				gvVideosets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
						scrollControl(position);
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						scrollControl(-1);
					}
				});
				addToPerPage(0);
			} catch (JSONException e) {
				ClearLog.LogError("BROSWER\tLoad\tFAIL\t0ms\t" + url);
				e.printStackTrace();
			}
		}

		@Override
		public void onComplete(boolean result) {
			// TODO Auto-generated method stub

		}
	};

	public void scrollControl(int position){
		try{
			for(int i=0;i<gvVideosets.getChildCount();i++){
				if(position != i)
					((MarqueeTextView)gvVideosets.getChildAt(i).findViewById(R.id.tv_video_item_content)).stopScroll();
				else
					((MarqueeTextView)gvVideosets.getChildAt(i).findViewById(R.id.tv_video_item_content)).startFor0();
			}
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}

	public void addToPerPage(int labelPos) {
		videoPerPageShow.clear();
		int videoPos = labelPos * 20;
		for (int i = videoPos; i < 20 * labelPos + 20; i++) {
			if (i <= videoSets.size() - 1)
				videoPerPageShow.add(videoSets.get(i));
		}
		videoSetsAdapter.notifyDataSetChanged();
	}
	//gridview切换数据的动画
	public void showGridViewAnim(){
		
		//数据退出动画
		ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(gvVideosets,"scaleY", 1.0f, 0f);
		objectAnimator1.setDuration(500);
		objectAnimator1.start();
		
		objectAnimator1.addListener(new AnimatorListenerAdapter(){
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				super.onAnimationEnd(animation);
				//数据进入动画
				ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(gvVideosets,"scaleY", 0f, 1.0f);
				objectAnimator.setDuration(500);
				objectAnimator.start();
			}
		});
	}
	
}
