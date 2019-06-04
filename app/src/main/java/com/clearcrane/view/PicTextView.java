package com.clearcrane.view;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.clearcrane.provider.MaterialRequest;
import com.clearcrane.provider.MaterialRequest.OnCompleteListener;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.LogUtils;
import com.clearcrane.util.MarqueeTextView;
import com.clearcrane.util.TipDialog;
import com.clearcrane.vod.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class PicTextView extends VoDBaseView {

	private String MainiconJson;
	// 主菜单布局
	private LinearLayout MainiconLayout;
	// 子菜单布局
	private LinearLayout SubiconLayout;

	private TextView picTextName;

	private LayoutTransition mLayoutTransition;

	private String PicText_label;

	private long lastTime = 0;// 按键事件控制
	private boolean isGoToSubView = false;// 主界面是否开始跳转到子界面，如果是防止多次按Enter键导致布局混乱

	private boolean menuRowAdded = false;

	private boolean Gesture = false;
	// 左侧主菜单
	private ArrayList<Mainicon> MainiconList = new ArrayList<Mainicon>();
	private ArrayList<ImageView> upPageIconList = new ArrayList<ImageView>();
	private ArrayList<ImageView> downPageIconList = new ArrayList<ImageView>();

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

	private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");

	public static final String TAG = "PicTextView";

	private Subicon currentSub = null;
	// private Timer timer = new Timer();

	/**
	 * private Handler mHandler = new Handler(){
	 * 
	 * @Override public void handleMessage(Message msg) { // TODO Auto-generated
	 *           method stub
	 *           timetv.setTextSize(TypedValue.COMPLEX_UNIT_PX,ClearConfig.
	 *           getScreenHeight()/30); timetv.setTextColor(Color.rgb(255, 255,
	 *           255)); timetv.setText(timetext); super.handleMessage(msg); }
	 *           };;
	 **/

	class Mainicon {
		String label; // 页面名字
		String type;
		String category;
		int subCount;
		Button video_type;
		ArrayList<Subicon> SubiconList = new ArrayList<Subicon>();
	}

	class Subicon {
		String name;
		String jsonURL;
		String create_time;
		long duration;
		String type;
		LinearLayout SubiconView;// 单个视频集的图标

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

	/**
	 * private void updateTimeZone(){ long currentTime
	 * =DateUtil.getInstance().getDate(); Date date = new Date(currentTime);
	 * timetext = formatter.format(date); }
	 **/

	public void init(Context ctx, String u) {
		super.init(ctx, u);
		view = LayoutInflater.from(ctx).inflate(R.layout.pictext_view, null);
		Log.i("getview", "" + view);
		initLayoutInXml();

		/* trigger to get data sources */
		MaterialRequest mr = new MaterialRequest(ctx, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(PicTextListen);
		mr.execute(this.url);// 暂时指定一个存在的url
	}

	OnCompleteListener PicTextListen = new OnCompleteListener() {

		@Override
		public void onDownloaded(Object result) {
			// TODO Auto-generated method stub
			MainiconJson = (String) result;
			Log.i("PicJson", "Json:" + MainiconJson);
			if (MainiconJson == null) {
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
				JSONTokener jsonParser = new JSONTokener(MainiconJson);
				JSONObject objectjson = (JSONObject) jsonParser.nextValue();

				String type = objectjson.getString("Type");
				PicText_label = objectjson.getString("Label");

				Log.i("xyl12", "" + PicText_label);

				picTextName.setText(PicText_label);
				picTextName.setTextSize(TypedValue.COMPLEX_UNIT_PX, ClearConfig.getScreenHeight() / 20);

				JSONArray contentArray = objectjson.getJSONArray("Content");
				for (int i = 0; i < contentArray.length(); i++) {
					int index = i;

					JSONObject objecttmp = (JSONObject) contentArray.opt(i);
					Mainicon mainicon = new Mainicon();
					mainicon.category = objecttmp.getString("category");
					mainicon.subCount = objecttmp.getInt("count");
					Log.i("XYL_pt", "" + contentArray.length() + ":" + mainicon.category);
					JSONArray secondArray = objecttmp.getJSONArray("pictures");
					for (int j = 0; j < mainicon.subCount; j++) {
						Log.i("XYL_pt", "" + mainicon.subCount);
						JSONObject secondTmp = (JSONObject) secondArray.opt(j);
						Subicon subicon = new Subicon();
						subicon.name = secondTmp.getString("name");
						Log.i("XYL_pt", "" + subicon.name);
						subicon.jsonURL = ClearConfig.getJsonUrl(context, secondTmp.getString("Json_URL"));
						Log.i("XYL_pt", "" + subicon.jsonURL);
						subicon.create_time = secondTmp.getString("create_time");
						subicon.duration = secondTmp.getLong("duration");
						Log.i("XYL_pt", "" + subicon.duration);
						subicon.type = secondTmp.getString("Type");
						Log.i("XYL_pt", "" + subicon.type);
						mainicon.SubiconList.add(subicon);
					}

					MainiconList.add(mainicon);
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
							 * ,ClearConfig.getScreenHeight()/37);
							 **/
						}
					}
				}, 100);

				/**
				 * //每隔1000ms更新一次时间 timer.schedule(new TimerTask() {
				 * 
				 * @Override public void run() { // TODO Auto-generated method
				 *           stub updateTimeZone();
				 *           Log.i("xyltime",""+timetext); Message msg =
				 *           mHandler.obtainMessage(); msg.sendToTarget(); } },
				 *           0,ClearConfig.UPDATE_TIME);
				 *           Log.i("xyl","time:"+timetext);
				 **/

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
		SubiconLayout = (LinearLayout) view.findViewById(R.id.picText_sets_layout);
		MainiconLayout = (LinearLayout) view.findViewById(R.id.picText_list_layout);
		picTextName = (TextView) view.findViewById(R.id.picText_title);
		pagination = (TextView) view.findViewById(R.id.pagination_sign);
		timetv = (TextView) view.findViewById(R.id.time_clock);
		mLayoutTransition = new LayoutTransition();
		// 为GridLayout设置mLayoutTransition对象
		SubiconLayout.setLayoutTransition(mLayoutTransition);
		// 设置每个动画持续的时间
		mLayoutTransition.setDuration(300);
	}

	@Override
	public boolean onKeyDpadUp() {
		Gesture = false;
		long last = System.currentTimeMillis();
		if (last - lastTime < 300) {
			return true;
		}
		lastTime = last;
		int newFocusIndex = -1;
		int newSubFocusIndex = -1;
		int newPage = 1;
		int newMpage = 1;
		if (curSubFocusIndex != -1 && !MainiconList.get(curFocusIndex).SubiconList.isEmpty()) {
			// 非最后一行
			if (curSubFocusIndex > curPage * 7 - 7 && curSubFocusIndex <= 6 + curPage * 7 - 7) {
				newSubFocusIndex = curSubFocusIndex < 1 ? curSubFocusIndex : curSubFocusIndex - 1;
				if (curSubFocusIndex != newSubFocusIndex) {
					onSubIconFocusChanged(curSubFocusIndex, newSubFocusIndex);
					curSubFocusIndex = newSubFocusIndex;
				}
			}
			// 处理分页
			else if (curSubFocusIndex == curPage * 7 - 7 && curPage != 1) {
				newPage = curPage > 1 ? curPage - 1 : curPage;
				newSubFocusIndex = newPage * 7 - 1;
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
		// int add_SubFocusIndex = -100;
		long last = System.currentTimeMillis();
		if (last - lastTime < 300) {
			return true;
		}
		lastTime = last;
		int newFocusIndex = -1;
		int newSubFocusIndex = -1;
		int newPage = 1;
		int newMpage = 1;
		if (curFocusIndex != -1 && MainiconList.size() != 0) {
			int subListSize = MainiconList.get(curFocusIndex).subCount;
			if (curSubFocusIndex != -1 && curSubFocusIndex < subListSize && !(subListSize == 0)) {

				// 处理分页
				if (curSubFocusIndex == curPage * 7 - 1 && subListSize > 7 * curPage) {

					newPage = curPage < getAllPage(curFocusIndex) ? curPage + 1 : curPage;
					newSubFocusIndex = newPage * 7 - 7;
					temp_SubFocusIndex = curSubFocusIndex;
					onSubIconPageChange(curPage, newPage, curSubFocusIndex, newSubFocusIndex);
					curPage = newPage;
					curSubFocusIndex = newSubFocusIndex;

				} else if (curSubFocusIndex >= curPage * 7 - 7 && curSubFocusIndex < 6 + curPage * 7 - 7) {
					// 默认一页

					newSubFocusIndex = curSubFocusIndex < subListSize - 1 ? curSubFocusIndex + 1 : curSubFocusIndex;
					if (curSubFocusIndex != newSubFocusIndex) {
						onSubIconFocusChanged(curSubFocusIndex, newSubFocusIndex);
						curSubFocusIndex = newSubFocusIndex;
					}

					/**
					 * temp_SubFocusIndex = curSubFocusIndex; add_SubFocusIndex
					 * =curSubFocusIndex +1;
					 * Log.i("down1",""+add_SubFocusIndex); //处理越界情况
					 * newSubFocusIndex = add_SubFocusIndex <
					 * subListSize?add_SubFocusIndex:curSubFocusIndex;
					 * temp_SubFocusIndex =
					 * temp_SubFocusIndex==newSubFocusIndex?-3:
					 * temp_SubFocusIndex; if(temp_SubFocusIndex !=
					 * newSubFocusIndex) {
					 * onSubIconFocusChanged(temp_SubFocusIndex,newSubFocusIndex
					 * ); curSubFocusIndex = newSubFocusIndex; }
					 **/
				}

			} else if (curSubFocusIndex == -1) {
				// down,焦点一直往下移动到底部,移动到底部后无反应
				int AllPage = MainiconList.size() % 10 != 0 ? MainiconList.size() / 10 + 1 : MainiconList.size() / 10;
				newFocusIndex = (curFocusIndex + 1) < MainiconList.size() ? curFocusIndex + 1 : curFocusIndex;
				temp_FoucsIndex = curFocusIndex;
				if (temp_FoucsIndex != MainiconList.size() - 1) {
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
		if (curSubFocusIndex != -1) {
			temp_SubFocusIndex = curSubFocusIndex;
			curSubFocusIndex = -1;
			// up,焦点切换到第一个主菜单
			onSubIconFocusChanged(temp_SubFocusIndex, curSubFocusIndex);
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
		if (curSubFocusIndex == -1 && MainiconList.size() != 0) {
			if (MainiconList.get(curFocusIndex).SubiconList.size() != 0) {
				// 向右移动到子菜单
				newSubFocusIndex = curPage * 7 - 7;
				temp_SubFocusIndex = curSubFocusIndex;
				onSubIconFocusChanged(temp_SubFocusIndex, newSubFocusIndex);
				curSubFocusIndex = newSubFocusIndex;
			}

		}
		/**
		 * //页面最后一个视频集屏蔽右点击事件 else if(curSubFocusIndex !=
		 * curPage*10-10+getPageIconCount(curPage)-1 ) { newSubFocusIndex =
		 * curSubFocusIndex + 1; temp_SubFocusIndex = curSubFocusIndex;
		 * 
		 * onSubIconFocusChanged(temp_SubFocusIndex,newSubFocusIndex);
		 * curSubFocusIndex = newSubFocusIndex;
		 * 
		 * }
		 **/

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

		if (curSubFocusIndex == -1 && !MainiconList.isEmpty()
				&& !MainiconList.get(curFocusIndex).SubiconList.isEmpty()) {
			// enter,焦点切换到第一个主菜单的第一个子菜单
			final int newSubFocusIndex = 0;
			temp_SubFocusIndex = curSubFocusIndex;
			new Handler().postDelayed(new Runnable() {
				public void run() {

					onSubIconFocusChanged(temp_SubFocusIndex, newSubFocusIndex);
					curSubFocusIndex = newSubFocusIndex;
				}
			}, 100);

		} else if (curSubFocusIndex != -1) {
			Subicon focusIcon = MainiconList.get(curFocusIndex).SubiconList.get(curSubFocusIndex);
			VoDBaseView newView = VoDViewManager.newViewByType(focusIcon.type);
			// 写死focusicon的jsonurl，调试页面跳转
			// mApp.contentleft = "-"+MainiconList.get(curFocusIndex).category;
			// mApp.resourceName = focusIcon.name;
			// String categoryPath = mApp.content + mApp.contentleft;
			// mApp.timeInS = DateUtil.getCurrentTimeSecond();
			// String logInsert = mApp.combinatePostParasString("start", "0",
			// "点播", mApp.viewType, focusIcon.name, categoryPath);
			// Log.i(TAG, " categoryPath START: " + logInsert);
			// ClearLog.logInsert(logInsert);
			currentSub = focusIcon;
			LogUtils.sendLogStart(mApp, "点播", "图文", focusIcon.name);
			// mApp.SendLogMode = 1;
			if (newView != null) {
				newView.init(context, focusIcon.jsonURL);
				Log.i("newview", "" + focusIcon.jsonURL);
				newView.setName(focusIcon.name);

				VoDViewManager.getInstance().pushForegroundView(newView);
			} else {
				isGoToSubView = false;
			}
		}
		return true;
	}

	@Override
	public void back() {
		// TODO Auto-generated method stub
		if (currentSub != null) {
			LogUtils.sendLogEnd(mApp, "点播", "图文", currentSub.name);
		}
		super.back();
	}

	class ViewHolder {
		private ImageView img;
		private TextView date;
		private MarqueeTextView title;
	}

	private void attachView(Subicon subicon) {

		if (subicon.SubiconView == null) {
			vh = new ViewHolder();
			subicon.SubiconView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.pictext_sub_page, null);

			vh.date = (TextView) subicon.SubiconView.findViewById(R.id.picText_date);
			vh.title = (MarqueeTextView) subicon.SubiconView.findViewById(R.id.picText_name);
			subicon.SubiconView.setTag(vh);
		} else {
			vh = (ViewHolder) subicon.SubiconView.getTag();
		}
		vh.title .setAlignType(Gravity.CENTER|Gravity.LEFT);
		// 图文创建时间
		vh.date.setPadding(0, 10, 0, 0);
		vh.date.setTextSize(TypedValue.COMPLEX_UNIT_PX, ClearConfig.getScreenHeight() / 42);
		vh.date.setTextColor(Color.rgb(255, 255, 255));
		vh.date.setText(subicon.create_time);

		// 图文名
		vh.title.setPadding(0, 10, 0, 0);
		vh.title.setTextSize(TypedValue.COMPLEX_UNIT_PX, ClearConfig.getScreenHeight() / 36);
		vh.title.setTextColor(Color.rgb(255, 255, 255));
		vh.title.setText(subicon.name);

		// SubiconLayout.setPadding(0,5,0,0);
		SubiconLayout.addView(subicon.SubiconView);
		subicon.SubiconView.setVisibility(android.view.View.VISIBLE);

	}

	public void onSubIconPageChange(int oldPage, int newPage, int oldPos, int newPos) {
		customLayoutTransition(Gesture);
		if (curFocusIndex != -1) {
			Mainicon FocusIcon = MainiconList.get(curFocusIndex);
			int pageSize = getAllPage(curFocusIndex);

			// 更改分页符
			pagination.setText(newPage + "/" + pageSize + "页");
			pagination.setTextSize(TypedValue.COMPLEX_UNIT_PX, ClearConfig.getScreenHeight() / 37);

			if (oldPos >= 0) {
				int pageIconCount = oldPage < pageSize ? 7 : getLastPageSubIcon(curFocusIndex);

				// remove the old focus’s border
				Subicon oldSubFocusIcon = FocusIcon.SubiconList.get(oldPos);

				ImageView focus_iv_border = (ImageView) oldSubFocusIcon.SubiconView
						.findViewById(R.id.picText_pic_border);
				focus_iv_border.setVisibility(android.view.View.INVISIBLE);
				((MarqueeTextView) oldSubFocusIcon.SubiconView.findViewById(R.id.picText_name)).stopScroll();

				// 隐藏之前页的子菜单
				for (int i = 0; i < pageIconCount; i++) {
					Subicon subicon = FocusIcon.SubiconList.get(i + oldPage * 7 - 7);
					subicon.SubiconView.setVisibility(android.view.View.INVISIBLE);
					SubiconLayout.removeView(subicon.SubiconView);
				}

			}

			if (newPos >= 0) {
				int pageIconCount = newPage < pageSize ? 7 : getLastPageSubIcon(curFocusIndex);

				// 显示下一页的子菜单,子菜单焦点改变并刷新子菜单布局显示,json数据不解析完全，考虑用回调，等json解析完再执行
				for (int i = 0; i < pageIconCount; i++) {

					Subicon subicon = FocusIcon.SubiconList.get(i + newPage * 7 - 7);
					attachView(subicon);
					if (i == 0 && newPos > oldPos || i == pageIconCount - 1 && newPos < oldPos) {
						ImageView focus_iv_border = (ImageView) subicon.SubiconView
								.findViewById(R.id.picText_pic_border);
						focus_iv_border.setVisibility(android.view.View.VISIBLE);
						((MarqueeTextView) subicon.SubiconView.findViewById(R.id.picText_name)).startFor0();
					}
				}
			}

		}
	}

	@SuppressLint("NewApi")
	public void onSubIconFocusChanged(int oldPos, int newPos) {
		if (curFocusIndex != -1 && MainiconList.size() != 0) {

			if (oldPos >= 0) {

				Subicon oldFocusIcon = MainiconList.get(curFocusIndex).SubiconList.get(oldPos);
				// remove the old focus’s border
				ImageView focus_iv_border = (ImageView) oldFocusIcon.SubiconView.findViewById(R.id.picText_pic_border);
				focus_iv_border.setVisibility(android.view.View.INVISIBLE);
				((MarqueeTextView) oldFocusIcon.SubiconView.findViewById(R.id.picText_name)).stopScroll();
			}

			else {
				for (Mainicon mainIconName : MainiconList) {
					if (mainIconName.equals(MainiconList.get(curFocusIndex))) {
						Button mainicon_name = MainiconList.get(curFocusIndex).video_type;
						mainicon_name.setBackground(context.getResources().getDrawable(R.drawable.videotypebutton_tp));
					}
				}
			}

			if (newPos >= 0) {

				Subicon newFocusIcon = MainiconList.get(curFocusIndex).SubiconList.get(newPos);
				vh.img = (ImageView) newFocusIcon.SubiconView.findViewById(R.id.picText_pic_border);
				vh.img.setVisibility(android.view.View.VISIBLE);
                ((MarqueeTextView) newFocusIcon.SubiconView.findViewById(R.id.picText_name)).startFor0();
			}

			else {

				// 传入的newpos小于0时，代表着当前焦点停留在子菜单，向主菜单焦点切换
				for (Mainicon mainIconName : MainiconList) {
					if (mainIconName.equals(MainiconList.get(curFocusIndex))) {
						Button mainicon_name = MainiconList.get(curFocusIndex).video_type;
						mainicon_name.setBackground(context.getResources().getDrawable(R.drawable.left_bar_box));
					}
				}
			}

		}
	}

	@SuppressLint("NewApi")
	public void onMainIconFocusPageChange(int oldMpage, int newMpage, int oldPage, int newPage, int oldPos,
			int newPos) {
		// customLayoutTransition(Gesture);
		Log.i(",", ":" + MainiconList.size());
		if (getAllPage(newPos) == 0) {
			newPage = 0;
		}

		pagination.setText(newPage + "/" + getAllPage(newPos) + "页");
		pagination.setTextSize(TypedValue.COMPLEX_UNIT_PX, ClearConfig.getScreenHeight() / 37);

		// 主菜单项的总页数，10个为一页
		int mainPageSize = MainiconList.size() % 10 != 0 ? MainiconList.size() / 10 + 1 : MainiconList.size() / 10;
		if (oldPos >= 0 && oldMpage >= 0 && MainiconList.size() != 0) {
			Mainicon oldFocusIcon = MainiconList.get(oldPos);
			int subPageSize = getAllPage(oldPos);
			int pageIconCount = oldPage < subPageSize ? 7 : getLastPageSubIcon(oldPos);

			int oldPageSize = oldMpage < mainPageSize ? 10 : getLastPageMainIcon();

			for (int i = 0; i < oldPageSize; i++) {
				Mainicon mainIcon = MainiconList.get(i + oldMpage * 10 - 10);
				mainIcon.video_type.setVisibility(android.view.View.INVISIBLE);
				MainiconLayout.removeView(mainIcon.video_type);
			}

			for (int i = 0; i < pageIconCount; i++) {
				if (oldFocusIcon.SubiconList.size() != 0) {
					Subicon subicon = oldFocusIcon.SubiconList.get(i + oldPage * 7 - 7);
					subicon.SubiconView.setVisibility(android.view.View.INVISIBLE);
					SubiconLayout.removeView(subicon.SubiconView);
				}
			}

			/** 管理保存两个分页按钮的list **/

			for (int i = 0; i < upPageIconList.size(); i++) {
				ImageView upButton = upPageIconList.get(i);
				upButton.setVisibility(android.view.View.INVISIBLE);
				MainiconLayout.removeView(upButton);
				upPageIconList.remove(upPageIconList.size() - 1);
			}

			for (int i = 0; i < downPageIconList.size(); i++) {
				ImageView downButton = downPageIconList.get(i);
				downButton.setVisibility(android.view.View.INVISIBLE);
				MainiconLayout.removeView(downButton);
				downPageIconList.remove(downPageIconList.size() - 1);
			}

		}

		if (newPos >= 0 && newMpage >= 0 && MainiconList.size() != 0) {
			if (MainiconList.size() != 0) {
				Mainicon newFocusIcon = MainiconList.get(newPos);
				int pageSize = getAllPage(newPos);
				Log.i("k123", ":" + newPage + "   bb:" + pageSize);
				int pageIconCount = newPage < pageSize ? 7 : getLastPageSubIcon(newPos);

				int newPageSize = newMpage < mainPageSize ? 10 : getLastPageMainIcon();
				Log.i("edf", "newPageSize:" + newPageSize);
				if (newMpage != 1) {
					ImageView upButton = new ImageView(context);
					upButton.setBackgroundResource(R.drawable.up);
					LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					lp.setMargins(0, 0, 0, 20);
					lp.gravity = Gravity.CENTER;
					MainiconLayout.addView(upButton, lp);
					upPageIconList.add(upButton);
				}

				for (int i = 0; i < newPageSize; i++) {
					Mainicon mainIcon = MainiconList.get(i + newMpage * 10 - 10);

					mainIcon.video_type = new Button(context);
					mainIcon.video_type.getBackground().setAlpha(0);
					mainIcon.video_type.setTextSize(TypedValue.COMPLEX_UNIT_PX, ClearConfig.getScreenHeight() / 30);
					mainIcon.video_type.setTextColor(Color.rgb(255, 255, 255));
					mainIcon.video_type.setText(mainIcon.category);
					mainIcon.video_type.setGravity(Gravity.CENTER_VERTICAL);
					mainIcon.video_type.setSingleLine();
					mainIcon.video_type.setEllipsize(TruncateAt.END);
					mainIcon.video_type.setPadding(40, 0, 50, 15);
					LayoutParams lp = new LayoutParams(ClearConfig.getScreenWidth() / 5,
							ClearConfig.getScreenHeight() / 12);

					// if(i == 0){
					// lp.setMargins(7,0,0,0);
					// }
					// else {
					// lp.setMargins(7,2,0,0);
					// }
					lp.setMargins(7, 0, 0, 0);
					MainiconLayout.addView(mainIcon.video_type, lp);
					Log.i("egt", "text:" + mainIcon.video_type);
					mainIcon.video_type.setVisibility(android.view.View.VISIBLE);

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
					MainiconLayout.addView(downButton, lp);
					downPageIconList.add(downButton);
				}

				for (int i = 0; i < pageIconCount; i++) {

					if (newFocusIcon.SubiconList.size() != 0) {
						Subicon subicon = newFocusIcon.SubiconList.get(i + newPage * 7 - 7);
						attachView(subicon);
					}
				}
			}
		}
	}

	@SuppressLint("NewApi")
	public void onMainIconFocusChange(int curMpage, int oldPage, int newPage, int oldPos, int newPos) {
		customLayoutTransition(Gesture);

		// 更改分页符
		Log.i("fly", "" + MainiconList.size());
		if (getAllPage(newPos) == 0) {
			newPage = 0;
		}
		pagination.setText(newPage + "/" + getAllPage(newPos) + "页");
		pagination.setTextSize(TypedValue.COMPLEX_UNIT_PX, ClearConfig.getScreenHeight() / 37);
		int mainPageSize = MainiconList.size() % 10 != 0 ? MainiconList.size() / 10 + 1 : MainiconList.size() / 10;
		if (oldPos >= 0 && oldPage >= 0) {
			Mainicon oldFocusIcon = MainiconList.get(oldPos);
			int pageSize = getAllPage(oldPos);
			int pageIconCount = oldPage < pageSize ? 7 : getLastPageSubIcon(oldPos);

			// 隐藏之前页的子菜单
			if (oldFocusIcon.SubiconList.size() != 0) {

				for (int i = 0; i < pageIconCount; i++) {
					Subicon subicon = oldFocusIcon.SubiconList.get(i + oldPage * 7 - 7);
					oldFocusIcon.SubiconList.get(i).SubiconView.setVisibility(android.view.View.INVISIBLE);
					SubiconLayout.removeView(subicon.SubiconView);
				}
			}

		}

		if (newPos >= 0 && newPage >= 0) {
			// make the new focus icon, add its focus border
			Mainicon newFocusIcon = MainiconList.get(newPos);
			int pageSize = getAllPage(newPos);
			int pageIconCount = newPage < pageSize ? 7 : getLastPageSubIcon(newPos);
			Log.i("ray", ":" + curMpage + " :" + mainPageSize);
			int curPageSize = curMpage < mainPageSize ? 10 : getLastPageMainIcon();
			if (newFocusIcon.SubiconList.size() != 0) {
				// 显示下一页的子菜单,主菜单焦点改变并刷新子菜单布局显示,json数据不解析完全，考虑用回调，等json解析完再执行
				for (int i = 0; i < pageIconCount; i++) {
					Subicon subicon = newFocusIcon.SubiconList.get(i + newPage * 7 - 7);
					attachView(subicon);
					Log.i("in focuschanged", "after focuschanged" + curFocusIndex);
				}
			}
			for (int i = 0; i < curPageSize; i++) {
				Mainicon mainIcon = MainiconList.get(i + curMpage * 10 - 10);
				if (mainIcon.equals(newFocusIcon)) {
					newFocusIcon.video_type.setBackground(context.getResources().getDrawable(R.drawable.left_bar_box));
				} else {
					Log.i("ewr", "size:" + curPageSize + "i:" + i + "n:" + mainIcon.video_type);
					mainIcon.video_type
							.setBackground(context.getResources().getDrawable(R.drawable.remove_focus_border));
				}
			}
			Log.i("size", "size:" + newFocusIcon.SubiconList.size() + "newPos:" + newPos);
		}
	}

	private int getPageIconCount(int page) {
		pageSize = getAllPage(curFocusIndex);
		int pageIconCount = page < pageSize ? 7 : getLastPageSubIcon(curFocusIndex);
		return pageIconCount;
	}

	private int getAllPage(int index) {
		// 往制定的Linearlayout添加分页按钮，显示两页
		if (MainiconList.size() != 0) {
			int Count = MainiconList.get(index).subCount;
			int Maxpage = Count % 7 == 0 ? Count / 7 : (Count / 7) + 1;
			return Maxpage;
		} else {
			return 0;
		}
	}

	private int getLastPageSubIcon(int index) {
		if (MainiconList.size() != 0) {
			int Count = MainiconList.get(index).subCount;
			int LastPageIcon = Count % 7 == 0 ? 7 : Count % 7;
			return LastPageIcon;
		} else {
			return 0;
		}
	}

	private int getLastPageMainIcon() {
		int Count = MainiconList.size();
		int LastPageIcon = Count % 10 == 0 ? 10 : Count % 10;
		return LastPageIcon;
	}

}
