package com.clearcrane.view;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.provider.MaterialRequest;
import com.clearcrane.provider.MaterialRequest.OnCompleteListener;
import com.clearcrane.schedule.DateUtil;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.MarqueeTextView;
import com.clearcrane.vod.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("NewApi")
public class VideoOnDemandView extends VoDBaseView {

	
	//按键响应间隔，不能太长
	private final int MIN_WAIT_TIME_UP_AND_DOWN = 500;
	
	private String VideoSetsTemplateJson;
	// 主菜单布局
	private LinearLayout allVideoLayout;
	// 子菜单布局
	private GridLayout videoSetsLayout;
	// 界面布局
	private LinearLayout videoSetsTemplateLayout;

	private TextView videologoName;

	private LayoutTransition mLayoutTransition;

	private String VideoView_bg_url; // 页面背景图url

	private String VideoView_label; // 页面名字

	private long lastTime = 0;// 按键事件控制
	private boolean isGoToSubView = false;// 主界面是否开始跳转到子界面，如果是防止多次按Enter键导致布局混乱

	private boolean menuRowAdded = false;

	private boolean Gesture = false;

	private ArrayList<VideoSetsTemplate> VideoSetsTemplateList = new ArrayList<VideoSetsTemplate>();
	private ArrayList<ImageView> upPageIconList = new ArrayList<ImageView>();
	private ArrayList<ImageView> downPageIconList = new ArrayList<ImageView>();
	private ArrayList<ImageView> videosetsImageList = new ArrayList<ImageView>();

	int curFocusIndex = -1;// 顶层menu当前位置
	int curSubFocusIndex = -1;// 子menu当前位置
	int temp_FoucsIndex = -2;// 存放主菜单焦点变换前的位置
	int temp_SubFocusIndex = -3;// 存放子菜单焦点变换前的位置

	int curPage = -1; // 当前页序号
	int pageSize = 0; // 总页数

	int curMpage = -1;

	ViewHolder vh;

	// 时间显示的textview
	private TextView timetv;

	// 分页显示的textview
	private TextView pagination;

	// 时间字符串信息
	private String timetext;

	private String ViewName = "VideoOnDemandView";

	private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");

	public SharedPreferences activitySharePre;

	private Timer timer = new Timer();

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			timetv.setTextSize(TypedValue.COMPLEX_UNIT_PX, ClearConfig.getScreenHeight() / 20);
			timetv.setTextColor(Color.rgb(255, 255, 255));
			timetv.setText(timetext);
			super.handleMessage(msg);
		}
	};;

	class VideoSetsTemplate {
		String label; // 页面名字
		String type;
		String category;
		int subCount;
		Button video_type;
		ArrayList<VideoSets> VideoSetsList = new ArrayList<VideoSets>();
	}

	class VideoSets {
		String name;
		String episodes;// 级数
		String iconURL;
		String jsonURL;
		String type;
		boolean iconDownloaded;
		LinearLayout videoSetsView;// 单个视频集的图标

	}

	private void customLayoutTransition(boolean Gesture) {

		if (Gesture == true) {
			/**
			 * 向下翻页 view出现时 view自身的动画效果，上移
			 */
			ObjectAnimator animator1 = ObjectAnimator.ofFloat(null, "translationY", ClearConfig.getScreenHeight(), 0F)
					.setDuration(mLayoutTransition.getDuration(LayoutTransition.APPEARING));
			mLayoutTransition.setAnimator(LayoutTransition.APPEARING, animator1);

			/**
			 * 
			 * view消失时 view自身的动画效果，上移
			 */

			ObjectAnimator animator2 = ObjectAnimator.ofFloat(null, "translationY", 0F, -ClearConfig.getScreenHeight())
					.setDuration(mLayoutTransition.getDuration(LayoutTransition.DISAPPEARING));
			mLayoutTransition.setAnimator(LayoutTransition.DISAPPEARING, animator2);

		} else {
			ObjectAnimator animator1 = ObjectAnimator.ofFloat(null, "translationY", -ClearConfig.getScreenHeight(), 0F)
					.setDuration(mLayoutTransition.getDuration(LayoutTransition.APPEARING));
			mLayoutTransition.setAnimator(LayoutTransition.APPEARING, animator1);

			ObjectAnimator animator2 = ObjectAnimator.ofFloat(null, "translationY", 0F, ClearConfig.getScreenHeight())
					.setDuration(mLayoutTransition.getDuration(LayoutTransition.DISAPPEARING));
			mLayoutTransition.setAnimator(LayoutTransition.DISAPPEARING, animator2);

		}

	}

	private void updateTimeZone() {
		long currentTime = DateUtil.getCurrentTimeMillSecond();
		Date date = new Date(currentTime);
		timetext = formatter.format(date);
	}

	private void saveStatue() {
		Editor editor = activitySharePre.edit();
		editor.putInt(ClearConstant.Play_Statue, 3);
		editor.putString(ClearConstant.VIEW_NAME, ViewName);
		editor.commit();
	}

	public void init(Context ctx, String u) {
		super.init(ctx, u);
		view = LayoutInflater.from(ctx).inflate(R.layout.videotemplate_page, null);
		Log.i("getview", "" + view);
		initLayoutInXml();

		activitySharePre = context.getSharedPreferences(ClearConstant.Activity_FILE, Context.MODE_PRIVATE);

		saveStatue();

		/* trigger to get data sources */
		MaterialRequest mr = new MaterialRequest(ctx, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(VideoOnDemandListen);
		mr.execute(this.url);// 暂时指定一个存在的url
	}

//	OnCompleteListener MainIconListen = new OnCompleteListener() {
//
//		@Override
//		public void onDownloaded(Object result) {
//			// TODO Auto-generated method stub
//
//		}
//
//		@Override
//		public void onComplete(boolean result) {
//			// TODO Auto-generated method stub
//
//		}
//	};

	OnCompleteListener VideoOnDemandListen = new OnCompleteListener() {

		@Override
		public void onDownloaded(Object result) {
			// TODO Auto-generated method stub
			VideoSetsTemplateJson = (String) result;
			Log.i("Json", "Json:" + VideoSetsTemplateJson);
			if (VideoSetsTemplateJson == null) {
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
				Log.e("Json", "VideoSetsTemplateJson is null check internet ");
				return;
			}
			try {
				JSONTokener jsonParser = new JSONTokener(VideoSetsTemplateJson);
				JSONObject objectjson = (JSONObject) jsonParser.nextValue();

				String type = objectjson.getString("Type");
				VideoView_label = objectjson.getString("Label");

				videologoName.setText(VideoView_label);
				videologoName.setTextSize(TypedValue.COMPLEX_UNIT_PX, ClearConfig.getScreenHeight() / 20);

				JSONArray contentArray = objectjson.getJSONArray("Content");
				for (int i = 0; i < contentArray.length(); i++) {
					int index = i;
					Log.i("XYL_video", "" + index);
					JSONObject objecttmp = (JSONObject) contentArray.opt(i);
					VideoSetsTemplate mainicon = new VideoSetsTemplate();
					mainicon.category = objecttmp.getString("category");
					mainicon.subCount = objecttmp.getInt("count");

					JSONArray secondArray = objecttmp.getJSONArray("VideoSets");
					for (int j = 0; j < secondArray.length(); j++) {
						Log.i("XYL_video", "" + j);
						JSONObject secondTmp = (JSONObject) secondArray.opt(j);
						VideoSets subicon = new VideoSets();
						subicon.name = secondTmp.getString("name");
						Log.i("XYL_video", "" + subicon.name);
						subicon.iconURL = ClearConfig.getJsonUrl(context, secondTmp.getString("Icon_URL"));
						Log.i("XYL_video", "" + subicon.iconURL);
						subicon.episodes = secondTmp.getString("episodes");
						subicon.jsonURL = ClearConfig.getJsonUrl(context, secondTmp.getString("Json_URL"));
						Log.i("XYL_video", "" + subicon.jsonURL);
						subicon.type = secondTmp.getString("Type");
						mainicon.VideoSetsList.add(subicon);
					}

					VideoSetsTemplateList.add(mainicon);
				}

				// 默认当前焦点为第一个主菜单
				new Handler().postDelayed(new Runnable() {
					public void run() {
						if (curFocusIndex < 0) {
							// first focus
							onMainIconFocusPageChange(-1, 1, -1, 1, -1, 0);
							curFocusIndex = 0;
							curPage = 1;
							curMpage = 1;

							/**
							 * //更改分页符 pageSize = getAllPage(curFocusIndex);
							 * pagination.setText(curPage+"/"+pageSize+"页");
							 * pagination.setTextSize(TypedValue.COMPLEX_UNIT_PX
							 * ,ClearConfig.getScreenHeight()/36);
							 **/
						}
					}
				}, 100);

				// 每隔1000ms更新一次时间
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						updateTimeZone();
						Log.i("xyltime", "" + timetext);
						Message msg = mHandler.obtainMessage();
						msg.sendToTarget();
					}
				}, 0, ClearConfig.UPDATE_TIME);
				Log.i("xyl", "time:" + timetext);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onComplete(boolean result) {

			// TODO Auto-generated method stub
		}

	};

	private void initLayoutInXml() {
		videoSetsLayout = (GridLayout) view.findViewById(R.id.videoTemplate_sets_layout);
		allVideoLayout = (LinearLayout) view.findViewById(R.id.videoTemplate_list_layout);
		videologoName = (TextView) view.findViewById(R.id.videoTemplate_name);
		pagination = (TextView) view.findViewById(R.id.pagination_symbol);
		timetv = (TextView) view.findViewById(R.id.time_clock);
		mLayoutTransition = new LayoutTransition();
		// 为GridLayout设置mLayoutTransition对象
		videoSetsLayout.setLayoutTransition(mLayoutTransition);
		// 设置每个动画持续的时间
		mLayoutTransition.setDuration(300);
	}

	@Override
	public boolean onKeyDpadUp() {
		Gesture = false;
		long last = System.currentTimeMillis();
		if (last - lastTime < MIN_WAIT_TIME_UP_AND_DOWN) {
			return true;
		}
		lastTime = last;
		int newFocusIndex = -1;
		int newSubFocusIndex = -1;
		int newPage = 1;
		int newMpage = 1;
		if (curSubFocusIndex != -1 && !VideoSetsTemplateList.get(curFocusIndex).VideoSetsList.isEmpty()) {
			// 非最后一行
			if (curSubFocusIndex > 3 + 12 * curPage - 12 && curSubFocusIndex <= 11 + 12 * curPage - 12) {
				temp_SubFocusIndex = curSubFocusIndex;
				curSubFocusIndex = curSubFocusIndex - 4;
				onSubIconFocusChanged(temp_SubFocusIndex, curSubFocusIndex);
			}
			// 处理分页
			else if (curSubFocusIndex >= curPage * 12 - 12 && curSubFocusIndex <= 3 + curPage * 12 - 12
					&& curPage != 1) {
				newPage = curPage > 1 ? curPage - 1 : curPage;
				newSubFocusIndex = newPage * 12 - 1;
				onSubIconPageChange(curPage, newPage, curSubFocusIndex, newSubFocusIndex);
				curPage = newPage;
				curSubFocusIndex = newSubFocusIndex;
			}
		} else if (curSubFocusIndex == -1) {
			// up,焦点一直往上移动到底部,移动到顶部后无反应
			newFocusIndex = (curFocusIndex - 1) < 0 ? curFocusIndex : curFocusIndex - 1;
			temp_FoucsIndex = curFocusIndex;
			if (temp_FoucsIndex % 10 == 0 && temp_FoucsIndex != 0) {
				newMpage = curMpage > 1 ? curMpage - 1 : curMpage;
				onMainIconFocusPageChange(curMpage, newMpage, curPage, newPage, temp_FoucsIndex, newFocusIndex);
				curMpage = newMpage;
				curFocusIndex = newFocusIndex;
			} else if (temp_FoucsIndex % 10 != 0) {
				onMainIconFocusChange(curMpage, curPage, newPage, temp_FoucsIndex, newFocusIndex);
				curFocusIndex = newFocusIndex;
				curPage = newPage;
			}
		}
		Log.i("pageup", "" + temp_FoucsIndex + " " + curFocusIndex + " " + temp_SubFocusIndex + " " + curSubFocusIndex
				+ " " + curPage);
		return true;
	}

	@Override
	public boolean onKeyDpadDown() {
		Gesture = true;
		int add_SubFocusIndex = -120;
		long last = System.currentTimeMillis();
		if (last - lastTime < MIN_WAIT_TIME_UP_AND_DOWN) {
			return true;
		}
		lastTime = last;
		int newFocusIndex = -1;
		int newSubFocusIndex = -1;
		int newPage = 1;
		int newMpage = 1;
		if (curFocusIndex != -1 && VideoSetsTemplateList.size() != 0) {
			int subListSize = VideoSetsTemplateList.get(curFocusIndex).subCount;
			if (curSubFocusIndex != -1 && curSubFocusIndex < subListSize && !(subListSize == 0)) {
				// 默认一页
				if (curSubFocusIndex >= curPage * 12 - 12 && curSubFocusIndex < 8 + curPage * 12 - 12) {

					temp_SubFocusIndex = curSubFocusIndex;
					add_SubFocusIndex = curSubFocusIndex + 4;
					Log.i("down1", "" + add_SubFocusIndex);
					// 处理越界情况
					newSubFocusIndex = add_SubFocusIndex < subListSize ? add_SubFocusIndex : curSubFocusIndex;
					temp_SubFocusIndex = temp_SubFocusIndex == newSubFocusIndex ? -3 : temp_SubFocusIndex;
					if (temp_SubFocusIndex != newSubFocusIndex) {
						onSubIconFocusChanged(temp_SubFocusIndex, newSubFocusIndex);
						curSubFocusIndex = newSubFocusIndex;
					}
				}
				// 处理分页
				else if (curSubFocusIndex >= 8 + curPage * 12 - 12 && curSubFocusIndex <= 11 + curPage * 12 - 12
						&& getAllPage(curFocusIndex) > 1) {

					newPage = curPage < getAllPage(curFocusIndex) ? curPage + 1 : curPage;
					newSubFocusIndex = newPage * 12 - 12;
					temp_SubFocusIndex = curSubFocusIndex;
					onSubIconPageChange(curPage, newPage, curSubFocusIndex, newSubFocusIndex);
					curPage = newPage;
					curSubFocusIndex = newSubFocusIndex;

				}
			} else if (curSubFocusIndex == -1) {
				// down,焦点一直往下移动到底部,移动到底部后无反应
				int AllPage = VideoSetsTemplateList.size() % 10 != 0 ? VideoSetsTemplateList.size() / 10 + 1
						: VideoSetsTemplateList.size() / 10;
				newFocusIndex = (curFocusIndex + 1) < VideoSetsTemplateList.size() ? curFocusIndex + 1 : curFocusIndex;
				temp_FoucsIndex = curFocusIndex;
				if (temp_FoucsIndex != VideoSetsTemplateList.size() - 1) {
					if (temp_FoucsIndex % 10 == 9) {
						newMpage = curMpage < AllPage ? curMpage + 1 : curMpage;
						onMainIconFocusPageChange(curMpage, newMpage, curPage, newPage, temp_FoucsIndex, newFocusIndex);
						curMpage = newMpage;
						curFocusIndex = newFocusIndex;
					} else {
						onMainIconFocusChange(curMpage, curPage, newPage, temp_FoucsIndex, newFocusIndex);
						curFocusIndex = newFocusIndex;
						curPage = newPage;
					}
				}
			}
		}
		Log.i("down", "" + temp_FoucsIndex + " " + curFocusIndex + " " + temp_SubFocusIndex + " " + curSubFocusIndex
				+ " " + curPage);
		return true;
	}

	@Override
	public boolean onKeyDpadLeft() {
		// TODO Auto-generated method stub
		Gesture = false;
		Log.i("in mainIconleft", "curFocusIndex:" + curFocusIndex);
		long last = System.currentTimeMillis();
		if (last - lastTime < 300) {
			return true;
		}
		lastTime = last;
		int newFocusIndex = -1;
		int newSubFocusIndex = -1;
		if (curSubFocusIndex != -1 && curSubFocusIndex != curPage * 12 - 12) {
			// 从主菜单焦点跳转到对应的子菜单焦点
			newSubFocusIndex = curSubFocusIndex - 1;
			temp_SubFocusIndex = curSubFocusIndex;

			onSubIconFocusChanged(temp_SubFocusIndex, newSubFocusIndex);
			curSubFocusIndex = newSubFocusIndex;
		}

		else if (curSubFocusIndex == curPage * 12 - 12) {
			temp_SubFocusIndex = curSubFocusIndex;
			curSubFocusIndex = -1;
			// up,焦点切换到第一个主菜单
			new Handler().postDelayed(new Runnable() {
				public void run() {
					onSubIconFocusChanged(temp_SubFocusIndex, curSubFocusIndex);

				}
			}, 100);

		}
		Log.i("left", "" + temp_FoucsIndex + " " + curFocusIndex + " " + temp_SubFocusIndex + " " + curSubFocusIndex);

		return true;
	}

	@Override
	public boolean onKeyDpadRight() {

		Log.i("in mainmenuright", "curFocusIndex:" + curFocusIndex);
		// if(isLoaded == false){
		// return true;
		// }
		long last = System.currentTimeMillis();
		if (last - lastTime < 300) {
			return true;
		}
		lastTime = last;
		int newFocusIndex = -1;
		int newSubFocusIndex = -1;
		if (curSubFocusIndex == -1 && VideoSetsTemplateList.size() != 0) {
			if (VideoSetsTemplateList.get(curFocusIndex).VideoSetsList.size() != 0) {
				// 向右移动到子菜单
				newSubFocusIndex = curPage * 12 - 12;
				temp_SubFocusIndex = curSubFocusIndex;
				onSubIconFocusChanged(temp_SubFocusIndex, newSubFocusIndex);
				curSubFocusIndex = newSubFocusIndex;
			}

		}
		// 页面最后一个视频集屏蔽右点击事件
		else if (curSubFocusIndex != curPage * 12 - 12 + getPageIconCount(curPage) - 1) {
			newSubFocusIndex = curSubFocusIndex + 1;
			temp_SubFocusIndex = curSubFocusIndex;

			onSubIconFocusChanged(temp_SubFocusIndex, newSubFocusIndex);
			curSubFocusIndex = newSubFocusIndex;

		}

		Log.i("right", "" + temp_FoucsIndex + " " + curFocusIndex + " " + temp_SubFocusIndex + " " + curSubFocusIndex);
		return true;
	}

	@Override
	public boolean onKeyEnter() {
		long last = System.currentTimeMillis();
		if (last - lastTime < 300) {
			return true;
		}
		lastTime = last;

		if (curFocusIndex < 0) {
			// first focus
			// onFocusChanged(-1, 0);
			curFocusIndex = 0;
			return true;
		}
		if (isGoToSubView == true)
			return true;

		if (curSubFocusIndex == -1 && !VideoSetsTemplateList.isEmpty()
				&& !VideoSetsTemplateList.get(curFocusIndex).VideoSetsList.isEmpty()) {
			// enter,焦点切换到第一个主菜单的第一个子菜单
			final int newSubFocusIndex = 0;
			temp_SubFocusIndex = curSubFocusIndex;
			new Handler().postDelayed(new Runnable() {
				public void run() {

					onSubIconFocusChanged(temp_SubFocusIndex, newSubFocusIndex);
					curSubFocusIndex = newSubFocusIndex;
				}
			}, 120);

		} else if (curSubFocusIndex != -1) {
			VideoSets focusIcon = VideoSetsTemplateList.get(curFocusIndex).VideoSetsList.get(curSubFocusIndex);
			VoDBaseView newView = VoDViewManager.newViewByType(focusIcon.type);
			// 写死focusicon的jsonurl，调试页面跳转
            Log.i("eeeee", "你猜对了");
			if (newView != null) {
				newView.init(context, focusIcon.jsonURL);
				Log.i("newview", "" + focusIcon.type);
				newView.setName(focusIcon.name);

				mApp.contentleft = "-" + VideoSetsTemplateList.get(curFocusIndex).category + "-" + focusIcon.name;
				VoDViewManager.getInstance().pushForegroundView(newView);
			} else {
				isGoToSubView = false;
			}
		}
		return true;
	}

	@Override
	public boolean onKeyBack() {
		// TODO Auto-generated method stub

		long last = System.currentTimeMillis();
		if (last - lastTime < 300) {
			return true;
		}
		lastTime = last;
		if (curFocusIndex != -1) {
			curFocusIndex = -1;
		}
		Log.i("back", "curFocusIndex:" + curFocusIndex);
		return super.onKeyBack();
	}

	class ViewHolder {
		private ImageView img;
		private TextView epsoide;
		private MarqueeTextView title;
	}

	class OnSubIconCompletelistener implements OnCompleteListener {
		VideoSets subicon;
		ViewHolder vholder;

		public OnSubIconCompletelistener(VideoSets icon, ViewHolder vh) {
			subicon = icon;
			vholder = vh;
		}

		@Override
		public void onDownloaded(Object result) {
		}

		@Override
		public void onComplete(boolean result) {
			subicon.iconDownloaded = true;

			// 视频级数
			vholder.epsoide.setTextSize(TypedValue.COMPLEX_UNIT_PX, ClearConfig.getScreenHeight() / 36);
			vholder.epsoide.setTextColor(Color.rgb(255, 255, 255));
			vholder.epsoide.setText("共" + subicon.episodes + "集");

			// 视频名字
			vholder.title.setTextSize(TypedValue.COMPLEX_UNIT_PX, ClearConfig.getScreenHeight() / 30);
			vholder.title.setTextColor(Color.rgb(255, 255, 255));
			vholder.title.setText(subicon.name);
			vholder.title.getTextWidth();


			/*
			 * add by winter
			 * record imageviews 
			 * which will be recycled 
			 * while they are not displaying
			 *  
			 */
			videosetsImageList.add(vholder.img);
			
			
			
			/*
			 * check if all image downloaded, if so, show it if(menuRowAdded ==
			 * false) { boolean allDownloaded = true; for(int i = 0; i <
			 * VideoSetsTemplateList.get(curFocusIndex).VideoSetsList.size();
			 * i++) {
			 * if(VideoSetsTemplateList.get(curFocusIndex).VideoSetsList.get(i).
			 * iconDownloaded == false) { allDownloaded = false; break; } }
			 * 
			 * if(allDownloaded) { //默认当前焦点为第一个主菜单 if(menuRowAdded == false) {
			 * L.d("add menu row"); menuRowAdded = true; if(curFocusIndex < 0){
			 * //first focus onMainIconFocusChange(-1,1,-1,0); curFocusIndex =
			 * 0; curPage =1 ; } } }
			 **/
		}
	}

	
	
	/*
	 * add by winter
	 * for avoid oom
	 * to recycle image bitmap
	 * 
	 */
	private void releaseRequstsBefore(){
		if(videosetsImageList.size() ==0) return;
		for(ImageView iv : videosetsImageList){
//			iv.setImageDrawable(null);
			Log.e("winter","release ~~");
			iv.setImageDrawable(null);
			iv.setBackground(null);
			iv.setBackgroundDrawable(null);
//			drawable.setCallback(null);
		}
		videosetsImageList.clear();
	}
	
	
	private void attachView(VideoSets subicon) {

		if (subicon.videoSetsView == null) {
			vh = new ViewHolder();
			subicon.videoSetsView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.videotemlate_sub_page,
					null);
			vh.img = (ImageView) subicon.videoSetsView.findViewById(R.id.videoTemlateset_pic);
			vh.epsoide = (TextView) subicon.videoSetsView.findViewById(R.id.videoTemlateset_text);
			vh.title = (MarqueeTextView) subicon.videoSetsView.findViewById(R.id.videoTemlateset_name);
			subicon.videoSetsView.setTag(vh);
		} else {
			vh = (ViewHolder) subicon.videoSetsView.getTag();
		}

		MaterialRequest image = new MaterialRequest(context, vh.img, ClearConfig.TYPE_IMAGE_BG_SCALE);
		image.setOnCompleteListener(new OnSubIconCompletelistener(subicon, vh));
		image.execute(subicon.iconURL);
		
		/*
		 * add by winter
		 * for avoid oom
		 * to cancel request list
		 * 
		 */
		
		videoSetsLayout.setPadding(0, 5, 0, 0);
		subicon.videoSetsView.setPadding(1, 0, 1, 15);
		videoSetsLayout.addView(subicon.videoSetsView);
		subicon.videoSetsView.setVisibility(View.VISIBLE);

	}

	public void onSubIconPageChange(int oldPage, int newPage, int oldPos, int newPos) {
		customLayoutTransition(Gesture);
		if (curFocusIndex != -1) {
			VideoSetsTemplate FocusIcon = VideoSetsTemplateList.get(curFocusIndex);
			int pageSize = getAllPage(curFocusIndex);

			// 更改分页符
			pagination.setText(newPage + "/" + pageSize + "页");
			pagination.setTextSize(TypedValue.COMPLEX_UNIT_PX, ClearConfig.getScreenHeight() / 36);

			if (oldPos >= 0) {
				int pageIconCount = oldPage < pageSize ? 12 : getLastPageSubIcon(curFocusIndex);

				// remove the old focus’s border
				VideoSets oldSubFocusIcon = FocusIcon.VideoSetsList.get(oldPos);
				ImageView focus_iv_border = (ImageView) oldSubFocusIcon.videoSetsView
						.findViewById(R.id.videoTemlateset_pic_border);
				focus_iv_border.setVisibility(View.INVISIBLE);
				((MarqueeTextView)oldSubFocusIcon.videoSetsView.findViewById(R.id.videoTemlateset_name)).stopScroll();

				// 隐藏之前页的子菜单
				for (int i = 0; i < pageIconCount; i++) {
					VideoSets subicon = FocusIcon.VideoSetsList.get(i + oldPage * 12 - 12);
					subicon.videoSetsView.setVisibility(View.GONE);

					videoSetsLayout.removeView(subicon.videoSetsView);
				}

			}

			if (newPos >= 0) {
				int pageIconCount = newPage < pageSize ? 12 : getLastPageSubIcon(curFocusIndex);

				// 显示下一页的子菜单,子菜单焦点改变并刷新子菜单布局显示,json数据不解析完全，考虑用回调，等json解析完再执行
				releaseRequstsBefore();
				for (int i = 0; i < pageIconCount; i++) {

					VideoSets subicon = FocusIcon.VideoSetsList.get(i + newPage * 12 - 12);
					attachView(subicon);
					if (i == 0 && newPos > oldPos || i == pageIconCount - 1 && newPos < oldPos) {
						ImageView focus_iv_border = (ImageView) subicon.videoSetsView
								.findViewById(R.id.videoTemlateset_pic_border);
						focus_iv_border.setVisibility(View.VISIBLE);
						// v.requestFocus后会自动跑马灯，会和back后获取页面标题焦点冲突，故采用这种方式
						((MarqueeTextView)subicon.videoSetsView.findViewById(R.id.videoTemlateset_name)).startScroll();
					}
				}
			}

		}
	}

	@SuppressLint("NewApi")
	public void onSubIconFocusChanged(int oldPos, int newPos) {
		if (curFocusIndex != -1 && VideoSetsTemplateList.size() != 0) {

			if (oldPos >= 0) {

				VideoSets oldFocusIcon = VideoSetsTemplateList.get(curFocusIndex).VideoSetsList.get(oldPos);
				// remove the old focus’s border
				ImageView focus_iv_border = (ImageView) oldFocusIcon.videoSetsView
						.findViewById(R.id.videoTemlateset_pic_border);
				focus_iv_border.setVisibility(View.INVISIBLE);
				((MarqueeTextView)oldFocusIcon.videoSetsView.findViewById(R.id.videoTemlateset_name)).stopScroll();

				// ((MarqueeTextView) vh.title).stopScroll();
				Log.i("truthly", ":" + vh.title.getVisibility());
			}

			else {
				for (VideoSetsTemplate mainIconName : VideoSetsTemplateList) {
					if (mainIconName.equals(VideoSetsTemplateList.get(curFocusIndex))) {
						Button mainicon_name = VideoSetsTemplateList.get(curFocusIndex).video_type;
						mainicon_name.setBackground(context.getResources().getDrawable(R.drawable.videotypebutton_tp));
					}
				}
			}

			if (newPos >= 0) {

				VideoSets newFocusIcon = VideoSetsTemplateList.get(curFocusIndex).VideoSetsList.get(newPos);
				ImageView focus_iv_border = (ImageView) newFocusIcon.videoSetsView
						.findViewById(R.id.videoTemlateset_pic_border);
				focus_iv_border.setVisibility(View.VISIBLE);
				((MarqueeTextView)newFocusIcon.videoSetsView.findViewById(R.id.videoTemlateset_name)).startScroll();
				// ((MarqueeTextView) vh.title).startScroll();
				Log.i("truth", ":" + vh.title.getVisibility());
			}

			else {

				// 传入的newpos小于0时，代表着当前焦点停留在子菜单，向主菜单焦点切换
				for (VideoSetsTemplate mainIconName : VideoSetsTemplateList) {
					if (mainIconName.equals(VideoSetsTemplateList.get(curFocusIndex))) {
						Button mainicon_name = VideoSetsTemplateList.get(curFocusIndex).video_type;
						mainicon_name.setBackground(context.getResources().getDrawable(R.drawable.left_bar_box));
					}
				}
			}

		}
	}

	public void onMainIconFocusPageChange(int oldMpage, int newMpage, int oldPage, int newPage, int oldPos,
			int newPos) {
		// customLayoutTransition(Gesture);
		if (getAllPage(newPos) == 0) {
			newPage = 0;
		}

		pagination.setText(newPage + "/" + getAllPage(newPos) + "页");
		pagination.setTextSize(TypedValue.COMPLEX_UNIT_PX, ClearConfig.getScreenHeight() / 36);

		// 主菜单项的总页数，10个为一页
		int mainPageSize = VideoSetsTemplateList.size() % 10 != 0 ? VideoSetsTemplateList.size() / 10 + 1
				: VideoSetsTemplateList.size() / 10;
		if (oldPos >= 0 && oldMpage >= 0 && VideoSetsTemplateList.size() != 0) {
			VideoSetsTemplate oldFocusIcon = VideoSetsTemplateList.get(oldPos);
			int subPageSize = getAllPage(oldPos);
			int pageIconCount = oldPage < subPageSize ? 12 : getLastPageSubIcon(oldPos);

			int oldPageSize = oldMpage < mainPageSize ? 10 : getLastPageMainIcon();

			for (int i = 0; i < oldPageSize; i++) {
				VideoSetsTemplate mainIcon = VideoSetsTemplateList.get(i + oldMpage * 10 - 10);
				mainIcon.video_type.setVisibility(View.INVISIBLE);
				allVideoLayout.removeView(mainIcon.video_type);
			}

			for (int i = 0; i < pageIconCount; i++) {
				if (oldFocusIcon.VideoSetsList.size() != 0) {
					VideoSets subicon = oldFocusIcon.VideoSetsList.get(i + oldPage * 12 - 12);
					subicon.videoSetsView.setVisibility(View.GONE);
					videoSetsLayout.removeView(subicon.videoSetsView);
				}
			}

			/** 管理保存两个分页按钮的list **/

			for (int i = 0; i < upPageIconList.size(); i++) {
				ImageView upButton = upPageIconList.get(i);
				upButton.setVisibility(View.INVISIBLE);
				allVideoLayout.removeView(upButton);
				upPageIconList.remove(upPageIconList.size() - 1);
			}

			for (int i = 0; i < downPageIconList.size(); i++) {
				ImageView downButton = downPageIconList.get(i);
				downButton.setVisibility(View.INVISIBLE);
				allVideoLayout.removeView(downButton);
				downPageIconList.remove(downPageIconList.size() - 1);
			}

		}

		if (newPos >= 0 && newMpage >= 0 && VideoSetsTemplateList.size() != 0) {
			if (VideoSetsTemplateList.size() != 0) {
				VideoSetsTemplate newFocusIcon = VideoSetsTemplateList.get(newPos);
				int pageSize = getAllPage(newPos);
				int pageIconCount = newPage < pageSize ? 12 : getLastPageSubIcon(newPos);

				int newPageSize = newMpage < mainPageSize ? 10 : getLastPageMainIcon();
				Log.i("edf", "newPageSize:" + newPageSize);
				if (newMpage != 1) {
					ImageView upButton = new ImageView(context);
					upButton.setBackgroundResource(R.drawable.up);
					LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					lp.setMargins(0, 0, 0, 20);
					lp.gravity = Gravity.CENTER;
					allVideoLayout.addView(upButton, lp);
					upPageIconList.add(upButton);
				}

				for (int i = 0; i < newPageSize; i++) {
					VideoSetsTemplate mainIcon = VideoSetsTemplateList.get(i + newMpage * 10 - 10);

					mainIcon.video_type = new Button(context);
					mainIcon.video_type.getBackground().setAlpha(0);
					mainIcon.video_type.setTextSize(TypedValue.COMPLEX_UNIT_PX, ClearConfig.getScreenHeight() / 30);
					mainIcon.video_type.setTextColor(Color.rgb(255, 255, 255));
					mainIcon.video_type.setSingleLine();
					mainIcon.video_type.setGravity(Gravity.CENTER_VERTICAL);
					mainIcon.video_type.setEllipsize(TruncateAt.END);
					mainIcon.video_type.setText(mainIcon.category);
					mainIcon.video_type.setPadding(40, 0, 50, 15);
					LayoutParams lp = new LayoutParams(ClearConfig.getScreenWidth()/5,
							ClearConfig.getScreenHeight()/12);
//					if (i == 0) {
//						lp.setMargins(7, 0, 0, 0);
//					} else {
//						lp.setMargins(7, 0, 0, 0);
//					}

					lp.setMargins(7, 0, 0, 0);
					allVideoLayout.addView(mainIcon.video_type, lp);
					Log.i("egt", "text:" + mainIcon.video_type);
					mainIcon.video_type.setVisibility(View.VISIBLE);

					if (mainIcon.equals(newFocusIcon)) {
						newFocusIcon.video_type
								.setBackground(context.getResources().getDrawable(R.drawable.left_bar_box));
					} else {
						Log.i("ewr", "m:" + mainIcon + "n:" + mainIcon.video_type);
						mainIcon.video_type
								.setBackground(context.getResources().getDrawable(R.drawable.remove_focus_border));
					}
				}

				// 非最后一页并包含10项，显示向下箭头
				if (newPageSize == 10 && newMpage != mainPageSize) {
					ImageView downButton = new ImageView(context);
					downButton.setBackgroundResource(R.drawable.down);
					LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					lp.setMargins(0, 20, 0, 0);
					lp.gravity = Gravity.CENTER;
					allVideoLayout.addView(downButton, lp);
					downPageIconList.add(downButton);
				}

				releaseRequstsBefore();
				for (int i = 0; i < pageIconCount; i++) {
					if (newFocusIcon.VideoSetsList.size() != 0) {
						VideoSets subicon = newFocusIcon.VideoSetsList.get(i + newPage * 12 - 12);
						attachView(subicon);
					}
				}
			}
		}
	}

	public void onMainIconFocusChange(int curMpage, int oldPage, int newPage, int oldPos, int newPos) {
		customLayoutTransition(Gesture);

		// 更改分页符
		if (getAllPage(newPos) == 0) {
			newPage = 0;
		}
		pagination.setText(newPage + "/" + getAllPage(newPos) + "页");
		pagination.setTextSize(TypedValue.COMPLEX_UNIT_PX, ClearConfig.getScreenHeight() / 36);
		int mainPageSize = VideoSetsTemplateList.size() % 10 != 0 ? VideoSetsTemplateList.size() / 10 + 1
				: VideoSetsTemplateList.size() / 10;
		if (oldPos >= 0 && oldPage >= 0) {
			VideoSetsTemplate oldFocusIcon = VideoSetsTemplateList.get(oldPos);
			int pageSize = getAllPage(oldPos);
			int pageIconCount = oldPage < pageSize ? 12 : getLastPageSubIcon(oldPos);

			// 隐藏之前页的子菜单
			if (oldFocusIcon.VideoSetsList.size() != 0) {

				for (int i = 0; i < pageIconCount; i++) {
					VideoSets subicon = oldFocusIcon.VideoSetsList.get(i + oldPage * 12 - 12);
					oldFocusIcon.VideoSetsList.get(i).videoSetsView.setVisibility(View.GONE);
					videoSetsLayout.removeView(subicon.videoSetsView);
				}
			}

		}

		if (newPos >= 0 && newPage >= 0) {
			// make the new focus icon, add its focus border
			VideoSetsTemplate newFocusIcon = VideoSetsTemplateList.get(newPos);
			int pageSize = getAllPage(newPos);
			int pageIconCount = newPage < pageSize ? 12 : getLastPageSubIcon(newPos);
			Log.i("ray", ":" + curMpage + " :" + mainPageSize);
			int curPageSize = curMpage < mainPageSize ? 10 : getLastPageMainIcon();
			if (newFocusIcon.VideoSetsList.size() != 0) {
				// 显示下一页的子菜单,主菜单焦点改变并刷新子菜单布局显示,json数据不解析完全，考虑用回调，等json解析完再执行
				releaseRequstsBefore();
				for (int i = 0; i < pageIconCount; i++) {
					VideoSets subicon = newFocusIcon.VideoSetsList.get(i + newPage * 12 - 12);
					attachView(subicon);
					Log.i("in focuschanged", "after focuschanged" + curFocusIndex);
				}
			}
			for (int i = 0; i < curPageSize; i++) {
				VideoSetsTemplate mainIcon = VideoSetsTemplateList.get(i + curMpage * 10 - 10);
				if (mainIcon.equals(newFocusIcon)) {
					newFocusIcon.video_type
							.setBackground(context.getResources().getDrawable(R.drawable.left_bar_box));
				} else {
					Log.i("ewr", "size:" + curPageSize + "i:" + i + "n:" + mainIcon.video_type);
					mainIcon.video_type
							.setBackground(context.getResources().getDrawable(R.drawable.remove_focus_border));
				}
			}
			Log.i("size", "size:" + newFocusIcon.VideoSetsList.size() + "newPos:" + newPos);
		}
	}

	private int getPageIconCount(int page) {
		pageSize = getAllPage(curFocusIndex);
		int pageIconCount = page < pageSize ? 12 : getLastPageSubIcon(curFocusIndex);
		return pageIconCount;
	}

	private int getAllPage(int index) {
		// 往制定的Linearlayout添加分页按钮，显示两页
		if (VideoSetsTemplateList.size() != 0) {
			int Count = VideoSetsTemplateList.get(index).subCount;
			int Maxpage = Count % 12 == 0 ? Count / 12 : (Count / 12) + 1;
			return Maxpage;
		} else {
			return 0;
		}
	}

	private int getLastPageSubIcon(int index) {
		if (VideoSetsTemplateList.size() != 0) {
			int Count = VideoSetsTemplateList.get(index).subCount;
			int LastPageIcon = Count % 12 == 0 ? 12 : Count % 12;
			return LastPageIcon;
		} else {
			return 0;
		}
	}

	private int getLastPageMainIcon() {
		int Count = VideoSetsTemplateList.size();
		int LastPageIcon = Count % 10 == 0 ? 10 : Count % 10;
		return LastPageIcon;
	}

	@Override
	public void back() {

		videologoName.post(new Runnable() {
			@Override
			public void run() {
				videologoName.requestFocus();
				Log.i("back2", "focus" + videologoName.requestFocus());
			}
		});

	}

}
