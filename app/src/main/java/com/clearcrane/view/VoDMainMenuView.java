/**
 * CopyRight:Clear
 * Auth:wgq
 * Date:2014-06-15
 * Description:主界面View
 * 
 * modify:xjf
 * Date:2014-06-17
 */
package com.clearcrane.view;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.clearcrane.activity.VoDActivity;
import com.clearcrane.log.ClearLog;
import com.clearcrane.log.L;
import com.clearcrane.platform.PhilipsHDMIActivity;
import com.clearcrane.platform.SKyworthATVActivity;
import com.clearcrane.platform.SkyworthHDMIActivity;
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
import java.util.Timer;
import java.util.TimerTask;

public class VoDMainMenuView extends VoDBaseView {

	TableRow menu_row = null;
	boolean menuRowAdded = false;
	private String mainMenuJson;
	private FrameLayout mFrameLayout;//menu最外层布局
	private ImageView forcusBackground, mainBackground;//menu当前选中框、整个menu背景
	private Animation displayAnim, mainBackgroundAnim;//menu出现时动画
	private Animation nextViewAnim, focusMainMenuAnim, focusSubMenuAnim;
	//private Animation focusSubMenuAnim;
	private LinearLayout mLinearLayout;//顶层menu布局
	
	//private LinearLayout mLinearLayout;//顶层menu布局
	/* stores the json results and related view element's reference */
	private ArrayList<MainIcon> mainIconList = new ArrayList<MainIcon>();
	int curFocusIndex = -1;//顶层menu当前位置
	int curSubFocusIndex = -1;//子menu当前位置
	private long lastTime = 0;//按键事件控制
	private float moveLength = 0;
	private Timer timer;
	private Handler handler;
	private TimerTask task;
	private boolean isGoToSubView = false;//主界面是否开始跳转到子界面，如果是防止多次按Enter键导致布局混乱
	private boolean isLoaded = false;
	public Calendar begin = null;
	int movex = 0;
	int movey = 0;
	int width = 0;
	int height = 0;
	private VoDActivity iptv = null;
	private String TAG = "mainmenu";
	private long animDuration = 200;
	
	
	class SubIcon {
		String name = null;
		String iconURL = null;
		String type = null;
		String jsonUrl = null;
		String iconFocusURL = null;
		//layout element reference
		LinearLayout subiconview;
	}
	
	class MainIcon {
		String name = null;
		String type = null;
		String iconURL = null;
		boolean iconDownloaded = false;
		String iconFocusURL = null;
		String subJsonUrl = null;
		int NextViewID = -1;
		
		ArrayList<SubIcon> subIconList = new ArrayList<SubIcon>(); 
		
		//layout element reference
		LinearLayout iconview = null;
	};
	
	
	/* has to set text after image download completed */
	class OnMainIconCompleteListerner implements OnCompleteListener {
		MainIcon mainicon;
		
		public OnMainIconCompleteListerner(MainIcon icon) {
			mainicon = icon;
		}

		@Override
		public void onDownloaded(Object result) {
		}

		@Override
		public void onComplete(boolean result) {
			mainicon.iconDownloaded = true;
			TextView tv = ((TextView)mainicon.iconview.findViewById(R.id.pic_text_wrapcontent_text));
			tv.setTextColor(Color.rgb(255, 255, 255));
			tv.setText(mainicon.name);
			
			/* check if all image downloaded, if so, show it */
			if(menuRowAdded == false) {
				boolean allDownloaded = true;
				for(int i = 0; i < mainIconList.size(); i++) {
					if(mainIconList.get(i).iconDownloaded == false) {
						allDownloaded = false;
						break;
					}
				}
				
				if(allDownloaded) {
					addIconTableRow();
				}
			}
		}
	}
	
	
	OnCompleteListener mainMenuJsonListen = new OnCompleteListener(){
		@Override
		public void onDownloaded(Object result) {
			mainMenuJson = (String) result;
			if(mainMenuJson == null){
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
		
			try {
				JSONTokener jsonParser = new JSONTokener(mainMenuJson);  
			    JSONArray contentArray = ((JSONObject) jsonParser.nextValue()).getJSONArray("Content");
			    String NameCH = null;
			    String NameENG = null;
				
			    for(int i = 0; i < contentArray.length(); i ++){
					JSONObject objecttmp = (JSONObject)contentArray.opt(i);
					MainIcon mainicon = new MainIcon();
					NameCH = objecttmp.getString("Name");
					NameENG = objecttmp.getString("NameEng");
					mainicon.name = ClearConfig.getStringByLanguageId(NameCH,NameENG);
					mainicon.type = objecttmp.getString("Type");
					mainicon.iconURL =  ClearConfig.getJsonUrl(context, objecttmp.getString("Icon_URL"));
					mainicon.iconFocusURL =  ClearConfig.getJsonUrl(context, objecttmp.getString("Icon_focus_URL"));
					mainicon.subJsonUrl =  ClearConfig.getJsonUrl(context, objecttmp.getString("Json_URL"));
					mainicon.NextViewID = objecttmp.getInt("NextViewID");
					LinearLayout iconView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.main_menu_icon_item, null);
					iconView.findViewById(R.id.pic_text_wrapcontent_pic);
					mainicon.iconview = iconView;
					if(mainicon.type.equalsIgnoreCase("Live")){
						VoDViewManager.getInstance().liveViewInit(mainicon.type, context, mainicon.subJsonUrl);
					}
					/* icon image */
					MaterialRequest image = new MaterialRequest(context, 
							(ImageView)iconView.findViewById(R.id.pic_text_wrapcontent_pic),
							ClearConfig.TYPE_IMAGE);
					image.setOnCompleteListener(new OnMainIconCompleteListerner(mainicon));
					image.execute(mainicon.iconURL);
					
					/* focus image */
					((ImageView)iconView.findViewById(R.id.pic_text_wrapcontent_pic_focus))
							.setVisibility(View.INVISIBLE);
					new MaterialRequest(context,
							(ImageView)iconView.findViewById(R.id.pic_text_wrapcontent_pic_focus),
							ClearConfig.TYPE_IMAGE).execute(mainicon.iconFocusURL);

					/* sub icon list */
					if(objecttmp.has("Second")) {
						JSONArray secondArray = objecttmp.getJSONObject("Second").getJSONArray("Content");
						for(int j = 0; j < secondArray.length(); j++) {
							JSONObject secondTmp = (JSONObject)secondArray.opt(j);
							SubIcon subicon = new SubIcon();
							NameCH = secondTmp.getString("Name");
							NameENG = secondTmp.getString("NameEng");
							subicon.name = ClearConfig.getStringByLanguageId(NameCH,NameENG);
							subicon.iconURL = ClearConfig.getJsonUrl(context, secondTmp.getString("Icon_URL"));
							subicon.type = secondTmp.getString("Type");
							subicon.jsonUrl = ClearConfig.getJsonUrl(context, secondTmp.getString("Json_URL"));
							subicon.iconFocusURL = ClearConfig.getJsonUrl(context, secondTmp.getString("Icon_focus_URL"));
							//change by xjf,将原始的代码生成布局改成xml布局，以便之后的动画方便查找控件
							subicon.subiconview = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.sub_menu_icon_item, null);
							ImageView iv = (ImageView)subicon.subiconview.findViewById(R.id.sub_pic_text_wrapcontent_pic);
							new MaterialRequest(context, iv,
									ClearConfig.TYPE_IMAGE).execute(subicon.iconURL);
							((ImageView)(subicon.subiconview.findViewById(R.id.sub_pic_text_wrapcontent_pic_focus)))
								.setVisibility(View.INVISIBLE);
							new MaterialRequest(context,
									(ImageView)subicon.subiconview.findViewById(R.id.sub_pic_text_wrapcontent_pic_focus),
									ClearConfig.TYPE_IMAGE).execute(subicon.iconFocusURL);
							//remove by xujifu
							//subicon.subiconview = new FrameLayout(context);

							subicon.subiconview.setVisibility(View.INVISIBLE);

							FrameLayout.LayoutParams ivlp = new FrameLayout.LayoutParams(
									FrameLayout.LayoutParams.WRAP_CONTENT,
									FrameLayout.LayoutParams.WRAP_CONTENT);
							ivlp.gravity = android.view.Gravity.CENTER_HORIZONTAL;
							//subicon.subiconview.addView(iv, ivlp);

							if(subicon.name != null) {
								FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
										FrameLayout.LayoutParams.WRAP_CONTENT,
										FrameLayout.LayoutParams.WRAP_CONTENT);
								lp.gravity = android.view.Gravity.CENTER_HORIZONTAL | android.view.Gravity.BOTTOM;

								TextView tv = (TextView)subicon.subiconview.findViewById(R.id.sub_pic_text_wrapcontent_text);
								tv.setTextColor(Color.rgb(255, 255, 255));
								tv.setText(subicon.name);
								//tv.setTextSize(10f);
								//subicon.subiconview.addView(tv, lp);
							}

							mainicon.iconview.addView(subicon.subiconview);
							mainicon.subIconList.add(subicon);
						}
					}
					//menu_row.addView(iconView);
					mainIconList.add(mainicon);
				}


			    /* check it is take too long(3 seconds) to download image */
			    new Handler().postDelayed(new Runnable(){
			        public void run() {
			        	addIconTableRow();
			        }
			     }, 2000);

			    if(begin != null) {
                	long between = (Calendar.getInstance()).getTimeInMillis()-begin.getTimeInMillis();
                    ClearLog.LogInfo("BROSWER\tLoad\tSUCC\t" + between +"ms\t" + url + "\t"
                    		+ "mainmenuView");
                }
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


	/* FIXME, remove me */
	int v_i = 0;
	@SuppressLint("HandlerLeak")
	@Override
	public void init(Context ctx, String u) {
		begin = Calendar.getInstance();
		super.init(ctx, u);
		iptv = (VoDActivity) ctx;
		lastTime = System.currentTimeMillis();;
		//for test
		url = u;
		view = LayoutInflater.from(context).inflate(R.layout.main_menu, null);
		menu_row = new TableRow(context);
		initLayoutInXml();

		/* trigger to get data sources */
		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(mainMenuJsonListen);
		mr.execute(url);
	}

	/**
	 * 初始化定义在XML中的布局。
	 */
	private void initLayoutInXml() {
		// TODO Auto-generated method stub
		mFrameLayout = (FrameLayout)view.findViewById(R.id.mainmenu);
		mLinearLayout = (LinearLayout)view.findViewById(R.id.menu);

		forcusBackground = (ImageView)view.findViewById(R.id.focus_background);
		mainBackground = (ImageView)view.findViewById(R.id.main_background);
		mainBackground.setAlpha(0.8f);

		displayAnim = AnimationUtils.loadAnimation(context, R.anim.display_anim_2sec);
		mainBackgroundAnim = AnimationUtils.loadAnimation(context, R.anim.main_background);
		focusMainMenuAnim = AnimationUtils.loadAnimation(context, R.anim.main_menu_display_anim);
		focusSubMenuAnim = AnimationUtils.loadAnimation(context, R.anim.sub_menu_display_anim);

		displayAnim.setFillAfter(true);
		mainBackgroundAnim.setFillAfter(true);

		forcusBackground.startAnimation(displayAnim);
		mainBackground.startAnimation(mainBackgroundAnim);
	}

	/**
	 * 向布局中添加主菜单布局，修改原始的tablelayout，使用LinearLayout添加。
	 * 添加布局生成时动画
	 */
	public void addIconTableRow() {
		if(menuRowAdded == false) {
			L.d("add menu row");
			menuRowAdded = true;

			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//					LinearLayout.LayoutParams.WRAP_CONTENT,
					ClearConfig.getPlatformWidth(170),
					LinearLayout.LayoutParams.WRAP_CONTENT);
			lp.leftMargin = ClearConfig.getPlatformWidth(30);
			lp.rightMargin = ClearConfig.getPlatformWidth(30);
			for(int i = 0; i < mainIconList.size(); i ++){
				mLinearLayout.addView(mainIconList.get(i).iconview, lp);//change by xujifu
				TranslateAnimation anim = new TranslateAnimation(-100 * i, 0, 0, 0);
				AlphaAnimation alpha = new AlphaAnimation(0.0f, 1.0f);
				AnimationSet set = new AnimationSet(true);
				set.addAnimation(anim);
				set.addAnimation(alpha);
				set.setDuration(700);
				mainIconList.get(i).iconview.startAnimation(set);
			}
			if(curFocusIndex < 0){
				//first focus
				onFocusChanged(-1, 0);
				curFocusIndex = 0;
			}
			isLoaded = true;
		}
	}

	public void onFocusChanged(int oldPos, int newPos) {
		Log.i("in focuschanged","focusindex"+curSubFocusIndex);
		//make the old focus icon unfocused, remove its focus image
		if(oldPos >= 0) {
			MainIcon oldFocusIcon = mainIconList.get(oldPos);
			FrameLayout singleIcon = (FrameLayout)oldFocusIcon.iconview.findViewById(R.id.main_menu_icon_fl);
			singleIcon.clearAnimation();
			singleIcon.animate().scaleX(1.0f).scaleY(1.0f).setDuration(animDuration).start();

			((ImageView)oldFocusIcon.iconview.findViewById(R.id.pic_text_wrapcontent_pic_focus))
				.setVisibility(View.INVISIBLE);
			((ImageView)oldFocusIcon.iconview.findViewById(R.id.pic_text_wrapcontent_border))
				.setVisibility(View.INVISIBLE);

			for(int i = 0; i < oldFocusIcon.subIconList.size(); i++) {
				//oldFocusIcon.subIconList.get(i).subiconview.setVisibility(android.view.View.INVISIBLE);
				oldFocusIcon.subIconList.get(i).subiconview.animate().alpha(0.0f).setDuration(animDuration).start();
			}
		}

		//make the new focus icon, add its focus image
		MainIcon newFocusIcon = mainIconList.get(newPos);

		ImageView newFocusImg = (ImageView)newFocusIcon.iconview.findViewById(R.id.pic_text_wrapcontent_pic_focus);
		newFocusImg.setVisibility(View.VISIBLE);
		newFocusImg.setAlpha(0.5f);
		newFocusImg.animate().alpha(1.0f).setDuration(animDuration).start();
		newFocusImg.startAnimation(focusMainMenuAnim);

//		ImageView newBorderImage = ((ImageView)newFocusIcon.iconview.findViewById(R.id.pic_text_wrapcontent_border));
//		newBorderImage.setVisibility(android.view.View.VISIBLE);
//		newBorderImage.setAlpha(0.5f);
//		newBorderImage.animate().alpha(1.0f).setDuration(300).start();

		FrameLayout singleIcon = (FrameLayout)newFocusIcon.iconview.findViewById(R.id.main_menu_icon_fl);
		singleIcon.clearAnimation();
		singleIcon.animate().scaleX(1.4f).scaleY(1.4f).setDuration(animDuration).start();




		//show the new focus icon's sub icon list
		for(int i = 0; i < newFocusIcon.subIconList.size(); i++) {
			newFocusIcon.subIconList.get(i).subiconview.setVisibility(View.VISIBLE);
			newFocusIcon.subIconList.get(i).subiconview.setAlpha(0f);
			newFocusIcon.subIconList.get(i).subiconview.animate().alpha(1f).setDuration(animDuration).start();
		}
		Log.i("in focuschanged","after focuschanged"+curSubFocusIndex);
	}


	public boolean onKeyDpadLeft() {
		if(isLoaded == false){
			return true;
		}
		long last = System.currentTimeMillis();
		if(last - lastTime < 300){
			return true;
		}
		lastTime = last;
		//if(curFocusIndex == 0 || curSubFocusIndex != -1)//防止焦点在子菜单上时左右移动
		if(curFocusIndex == 0)
			return true;
		int newFocusIndex = -1;
		//curSubFocusIndex = -1;//即便移动也要立即将子菜单焦点置为空，否则会产生list越界访问。
		if(curFocusIndex < 0) {
			//first focus
			newFocusIndex = 0;
		}
		else {
			newFocusIndex = (curFocusIndex + mainIconList.size() - 1) %  mainIconList.size();
			//水平移动动画代码
			if(mainIconList.size() > 1)
				mLinearLayout.animate()
					.translationXBy(mainIconList.get(1).iconview.getX() - mainIconList.get(0).iconview.getX())
					.setDuration(animDuration)
					.start();

		}
		Log.i("in mainmenuleft","curSubFocusIndex:"+curSubFocusIndex);
		int newSubFocusIndex = -1;
		if(curSubFocusIndex != -1){

			if(curSubFocusIndex <= mainIconList.get(newFocusIndex).subIconList.size()-1){
				newSubFocusIndex = curSubFocusIndex;
			}else{
				newSubFocusIndex = mainIconList.get(newFocusIndex).subIconList.size() - 1;
			}

			Log.i("in mainmenuleft","newSubFocusIndex:"+newSubFocusIndex);
			//去掉原来焦点子菜单动画
			SubIcon oldSubFocusIcon = mainIconList.get(curFocusIndex).subIconList.get(curSubFocusIndex);
			FrameLayout subIcon = (FrameLayout)oldSubFocusIcon.subiconview.findViewById(R.id.sub_menu_icon_fl);
			subIcon.clearAnimation();
			subIcon.animate().scaleX(1.0f).scaleY(1.0f).setDuration(animDuration).start();
			((ImageView)oldSubFocusIcon.subiconview.findViewById(R.id.sub_pic_text_wrapcontent_pic_focus))
				.setVisibility(View.INVISIBLE);
			((ImageView)oldSubFocusIcon.subiconview.findViewById(R.id.sub_pic_text_wrapcontent_border))
				.setVisibility(View.INVISIBLE);
			//change main menu focus
			onFocusChanged(curFocusIndex, newFocusIndex);
			//新焦点子菜单动画
			if(!mainIconList.get(newFocusIndex).subIconList.isEmpty()){
				Log.i("in mainmenuleft","not empty:"+curSubFocusIndex);
				SubIcon newSubFocusIcon = mainIconList.get(newFocusIndex).subIconList.get(newSubFocusIndex);
				ImageView newFocusImg = (ImageView)newSubFocusIcon.subiconview.findViewById(R.id.sub_pic_text_wrapcontent_pic_focus);
				newFocusImg.setVisibility(View.VISIBLE);
				newFocusImg.setAlpha(0.5f);
				newFocusImg.animate().alpha(1.0f).setDuration(animDuration).start();
				ImageView newBorderImage = ((ImageView)newSubFocusIcon.subiconview.findViewById(R.id.sub_pic_text_wrapcontent_border));
				newBorderImage.setVisibility(View.VISIBLE);
				newBorderImage.setAlpha(0.5f);
				newBorderImage.animate().alpha(1.0f).setDuration(animDuration).start();
				FrameLayout singleIcon = (FrameLayout)newSubFocusIcon.subiconview.findViewById(R.id.sub_menu_icon_fl);
				singleIcon.clearAnimation();
				singleIcon.animate().scaleX(1.2f).scaleY(1.2f).setDuration(animDuration).start();
			}else{
				Log.i("in mainmenuleft","is empty:"+curSubFocusIndex);
				mFrameLayout.animate()
				.translationYBy(120.0f)
				.start();
				for(int i = 0; i < mainIconList.size(); i ++ )
					((FrameLayout)mainIconList.get(i).iconview.findViewById(R.id.main_menu_icon_fl))
						.animate().alpha(1.0f).start();
				forcusBackground.animate().alpha(0.8f).start();
				mainBackground.animate().alpha(0.8f).start();
				removeSubFocus(curFocusIndex, curSubFocusIndex);
				newSubFocusIndex = -1;
			}
		}else{
			onFocusChanged(curFocusIndex, newFocusIndex);
		}


		curFocusIndex = newFocusIndex;
		curSubFocusIndex = newSubFocusIndex;

		return true;
	}

	public boolean onKeyDpadRight() {
		if(isLoaded == false){
			return true;
		}
		long last = System.currentTimeMillis();
		if(last - lastTime < 300){
			return true;
		}
		lastTime = last;
		//if(curFocusIndex == (mainIconList.size() - 1) || curSubFocusIndex != -1)
		if(curFocusIndex == (mainIconList.size() - 1))
			return true;
		int newFocusIndex = -1;
		//curSubFocusIndex = -1;
		if(curFocusIndex < 0) {
			//first focus
			newFocusIndex = 0;
		}
		else {
			newFocusIndex = (curFocusIndex + 1) %  mainIconList.size();
			if(mainIconList.size() > 1)
				mLinearLayout.animate()
					.translationXBy(mainIconList.get(0).iconview.getX() - mainIconList.get(1).iconview.getX())
					.setDuration(300)
					.start();
		}


		Log.i("in mainmenuright","curSubFocusIndex:"+curSubFocusIndex);
		int newSubFocusIndex = -1;
		if(curSubFocusIndex != -1){

			if(curSubFocusIndex <= mainIconList.get(newFocusIndex).subIconList.size()-1){
				newSubFocusIndex = curSubFocusIndex;
			}else{
				newSubFocusIndex = mainIconList.get(newFocusIndex).subIconList.size() - 1;
			}
			Log.i("in mainmenuleft","newSubFocusIndex:"+newSubFocusIndex);

			//去掉原来焦点子菜单动画
			SubIcon oldSubFocusIcon = mainIconList.get(curFocusIndex).subIconList.get(curSubFocusIndex);
			FrameLayout subIcon = (FrameLayout)oldSubFocusIcon.subiconview.findViewById(R.id.sub_menu_icon_fl);
			subIcon.clearAnimation();
			subIcon.animate().scaleX(1.0f).scaleY(1.0f).setDuration(animDuration).start();
			((ImageView)oldSubFocusIcon.subiconview.findViewById(R.id.sub_pic_text_wrapcontent_pic_focus))
				.setVisibility(View.INVISIBLE);
			((ImageView)oldSubFocusIcon.subiconview.findViewById(R.id.sub_pic_text_wrapcontent_border))
				.setVisibility(View.INVISIBLE);

			//change main menu focus
			onFocusChanged(curFocusIndex, newFocusIndex);

			//新焦点子菜单动画
			if(!mainIconList.get(newFocusIndex).subIconList.isEmpty()){
				Log.i("in mainmenuleft","not empty:"+curSubFocusIndex);
				SubIcon newSubFocusIcon = mainIconList.get(newFocusIndex).subIconList.get(newSubFocusIndex);
				ImageView newFocusImg = (ImageView)newSubFocusIcon.subiconview.findViewById(R.id.sub_pic_text_wrapcontent_pic_focus);
				newFocusImg.setVisibility(View.VISIBLE);
				newFocusImg.setAlpha(0.5f);
				newFocusImg.animate().alpha(1.0f).setDuration(animDuration).start();
				ImageView newBorderImage = ((ImageView)newSubFocusIcon.subiconview.findViewById(R.id.sub_pic_text_wrapcontent_border));
				newBorderImage.setVisibility(View.VISIBLE);
				newBorderImage.setAlpha(0.5f);
				newBorderImage.animate().alpha(1.0f).setDuration(animDuration).start();
				FrameLayout singleIcon = (FrameLayout)newSubFocusIcon.subiconview.findViewById(R.id.sub_menu_icon_fl);
				singleIcon.clearAnimation();
				singleIcon.animate().scaleX(1.2f).scaleY(1.2f).setDuration(animDuration).start();
			}else{
				Log.i("in mainmenuleft","is empty:"+curSubFocusIndex);
				mFrameLayout.animate()
				.translationYBy(120.0f)
				.start();
				for(int i = 0; i < mainIconList.size(); i ++ )
					((FrameLayout)mainIconList.get(i).iconview.findViewById(R.id.main_menu_icon_fl))
						.animate().alpha(1.0f).start();
				forcusBackground.animate().alpha(0.8f).start();
				mainBackground.animate().alpha(0.8f).start();
				removeSubFocus(curFocusIndex, curSubFocusIndex);
				newSubFocusIndex = -1;
			}
		}else{
			onFocusChanged(curFocusIndex, newFocusIndex);
		}

		curFocusIndex = newFocusIndex;
		curSubFocusIndex = newSubFocusIndex;
		return true;
	}

	public boolean onGotoSubIcon() {
		/* check if any icon request */
		if(curFocusIndex < 0) {
			//first focus
			onFocusChanged(-1, 0);
			curFocusIndex = 0;
			return true;
		}
		int newSubFocusIndex = -1;
		onSubFocusChange(curFocusIndex, curSubFocusIndex, newSubFocusIndex);

		return true;
	}

	private void onSubFocusChange(int curFocusIndex, int oldPos, int newPos) {
		// TODO Auto-generated method stub
		if(oldPos >= 0) {
			//修改子菜单项的上一焦点动画
			SubIcon oldSubFocusIcon = mainIconList.get(curFocusIndex).subIconList.get(oldPos);
			FrameLayout subIcon = (FrameLayout)oldSubFocusIcon.subiconview.findViewById(R.id.sub_menu_icon_fl);
			subIcon.clearAnimation();
			subIcon.animate().scaleX(1.0f).scaleY(1.0f).setDuration(animDuration).start();
			((ImageView)oldSubFocusIcon.subiconview.findViewById(R.id.sub_pic_text_wrapcontent_pic_focus))
				.setVisibility(View.INVISIBLE);
			((ImageView)oldSubFocusIcon.subiconview.findViewById(R.id.sub_pic_text_wrapcontent_border))
				.setVisibility(View.INVISIBLE);
		}

		//make the new focus icon, add its focus image
		SubIcon newSubFocusIcon = mainIconList.get(curFocusIndex).subIconList.get(newPos);

		ImageView newFocusImg = (ImageView)newSubFocusIcon.subiconview.findViewById(R.id.sub_pic_text_wrapcontent_pic_focus);
		newFocusImg.setVisibility(View.VISIBLE);
		newFocusImg.setAlpha(0.5f);
		newFocusImg.animate().alpha(1.0f).setDuration(animDuration).start();

		ImageView newBorderImage = ((ImageView)newSubFocusIcon.subiconview.findViewById(R.id.sub_pic_text_wrapcontent_border));
		newBorderImage.setVisibility(View.VISIBLE);
		newBorderImage.setAlpha(0.5f);
		newBorderImage.animate().alpha(1.0f).setDuration(animDuration).start();


		FrameLayout singleIcon = (FrameLayout)newSubFocusIcon.subiconview.findViewById(R.id.sub_menu_icon_fl);
		singleIcon.clearAnimation();
		singleIcon.animate().scaleX(1.2f).scaleY(1.2f).setDuration(animDuration).start();

	}

	public boolean onKeyDpadUp(){
		if(isLoaded == false){
			return true;
		}
		long last = System.currentTimeMillis();
		if(last - lastTime < 300){
			return true;
		}
		lastTime = last;
		if(curSubFocusIndex == 0){
			//当前布局向下移动
			mFrameLayout.animate()
				.translationYBy(120.0f)
				.start();
			for(int i = 0; i < mainIconList.size(); i ++ )
				((FrameLayout)mainIconList.get(i).iconview.findViewById(R.id.main_menu_icon_fl))
					.animate().alpha(1.0f).start();
			forcusBackground.animate().alpha(0.8f).start();
			mainBackground.animate().alpha(0.8f).start();
			removeSubFocus(curFocusIndex, curSubFocusIndex);
			curSubFocusIndex = -1;
			return true;
		}
		int newSubFocusIndex = -1;
		if(curSubFocusIndex < 0) {
			//first focus
			newSubFocusIndex = 0;
			return true;
		}
		else {
			newSubFocusIndex = (curSubFocusIndex +
					mainIconList.get(curFocusIndex).subIconList.size() - 1)
					%  mainIconList.get(curFocusIndex).subIconList.size();

		}

		onSubFocusChange(curFocusIndex, curSubFocusIndex, newSubFocusIndex);

		curSubFocusIndex = newSubFocusIndex;

		return true;
	}

	/**
	 * 当从子菜单移动到主菜单时，需要清除子菜单上的所有动画效果。
	 * @param curFocusIndex
	 * @param curSubFocusIndex
	 */
	private void removeSubFocus(int curFocusIndex, int curSubFocusIndex) {
		// TODO Auto-generated method stub
		SubIcon oldSubFocusIcon = mainIconList.get(curFocusIndex).subIconList.get(curSubFocusIndex);
		FrameLayout subIcon = (FrameLayout)oldSubFocusIcon.subiconview.findViewById(R.id.sub_menu_icon_fl);
		subIcon.clearAnimation();
		subIcon.animate().scaleX(1.0f).scaleY(1.0f).setDuration(animDuration).start();
		((ImageView)oldSubFocusIcon.subiconview.findViewById(R.id.sub_pic_text_wrapcontent_pic_focus))
			.setVisibility(View.INVISIBLE);
		((ImageView)oldSubFocusIcon.subiconview.findViewById(R.id.sub_pic_text_wrapcontent_border))
			.setVisibility(View.INVISIBLE);
	}

	public boolean onKeyDpadDown() {
		if(isLoaded == false){
			return true;
		}
		long last = System.currentTimeMillis();
		if(last - lastTime < 300){
			return true;
		}
		lastTime = last;
		//这里目前注释掉主要是为了可以从最后一个移动到第一个
//		if(curFocusIndex == (mainIconList.get(curFocusIndex).subIconList.size() - 1))
//			return true;
		if(mainIconList.get(curFocusIndex).subIconList.isEmpty())
			return true;
		int newSubFocusIndex = -1;
		if(curSubFocusIndex < 0) {
			//first focus
			newSubFocusIndex = 0;
			mFrameLayout.animate()
				.translationYBy(-120.0f)
				.start();
			for(int i = 0; i < mainIconList.size(); i ++ )
				((FrameLayout)mainIconList.get(i).iconview.findViewById(R.id.main_menu_icon_fl))
					.animate().alpha(0.6f).start();
			forcusBackground.animate().alpha(0.6f).start();
			mainBackground.animate().alpha(0.6f).start();
		}
		else {
			newSubFocusIndex = (curSubFocusIndex + 1) %  mainIconList.get(curFocusIndex).subIconList.size();
		}
		onSubFocusChange(curFocusIndex, curSubFocusIndex, newSubFocusIndex);

		curSubFocusIndex = newSubFocusIndex;
		Log.i("in keydown","focusindex"+curSubFocusIndex);
		return true;
	}

	private AnimatorListener goToNextViewListener = new AnimatorListener(){

		@Override
		public void onAnimationCancel(Animator animation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationEnd(Animator animation) {
			// TODO Auto-generated method stub
			MainIcon focusIcon = mainIconList.get(curFocusIndex);
			VoDBaseView newView = VoDViewManager.newViewByType(focusIcon.type);
			if(newView != null) {
				newView.init(context, focusIcon.subJsonUrl, mainIconList.get(curFocusIndex).iconview);
				/* show the sub view */
				VoDViewManager.getInstance().pushForegroundView(newView);
			}
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationStart(Animator animation) {
			// TODO Auto-generated method stub

		}

	};

	private AnimationListener nextViewListener = new AnimationListener(){

		@Override
		public void onAnimationEnd(Animation animation) {
			// TODO Auto-generated method stub
			FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mLinearLayout.getLayoutParams();
			L.i("args" + ClearConfig.getScreenWidth() / 2 + "," +
					mainIconList.get(curFocusIndex).iconview.getX() + "," +
					params.leftMargin);

			mainIconList.get(curFocusIndex).iconview.animate()
				.translationXBy( ClearConfig.getScreenWidth() / 2 -
						mainIconList.get(curFocusIndex).iconview.getX() -
						params.leftMargin - mainIconList.get(curFocusIndex).iconview.getWidth() / 2)
				.translationY( ClearConfig.getScreenHeight() / 2 -
						mainIconList.get(curFocusIndex).iconview.getY() -
						((FrameLayout.LayoutParams) mFrameLayout.getLayoutParams()).topMargin -
						mainIconList.get(curFocusIndex).iconview.getHeight() / 2)
				.scaleX(2.0f)
				.scaleY(2.0f)
				.setDuration(300)
				.setListener(goToNextViewListener)
				.start();
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub

		}

	};
	private AnimationListener subViewListener = new AnimationListener(){

		@Override
		public void onAnimationEnd(Animation animation) {
			// TODO Auto-generated method stub
			SubIcon focusIcon = mainIconList.get(curFocusIndex).subIconList.get(curSubFocusIndex);
			L.i("args" + focusIcon.type);
			VoDBaseView newView = VoDViewManager.newViewByType(focusIcon.type);
			if(newView != null) {
				newView.init(context, focusIcon.jsonUrl,
						mainIconList.get(curFocusIndex).subIconList.get(curSubFocusIndex).subiconview);
				/* show the sub view */
				VoDViewManager.getInstance().pushForegroundView(newView);
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub

		}

	};

	public boolean onKeyEnter() {
		if(isLoaded == false){
			return true;
		}
		long last = System.currentTimeMillis();
		if(last - lastTime < 300){
			return true;
		}
		lastTime = last;
		if(isGoToSubView == true)
			return true;
		if(curFocusIndex < 0 ){
			//first focus
			onFocusChanged(-1, 0);
			curFocusIndex = 0;
			return true;
		}
		if( curSubFocusIndex == -1 && mainIconList.get(curFocusIndex).NextViewID == -1)
			return true;

		if(mainIconList.get(curFocusIndex).type.equals("Live")){
			/* new the sub view, TODO, FIXME */
			isGoToSubView = true;
			if(timer != null){
				timer.cancel();
				timer = null;
				//VoDViewManager.getInstance().hideLiveVideo();
			}
			VoDViewManager.getInstance().stopBackgroundVideo();
			VoDViewManager.getInstance().hideBackgroundVideo();
			nextViewAnim = AnimationUtils.loadAnimation(context, R.anim.nextview_anim);
			TranslateAnimation tranAnim = new TranslateAnimation(0, mainIconList.get(curFocusIndex).iconview.getX(), 0, 0);
			float x = mainIconList.get(curFocusIndex).iconview.getX();
			if(mainIconList.size() > 1)
				moveLength = mainIconList.get(1).iconview.getX()
				- mainIconList.get(0).iconview.getX();
			for(int i = 0; i < mainIconList.size(); i ++){
				if(i == curFocusIndex){
					nextViewAnim.setAnimationListener(nextViewListener);
					mainIconList.get(curFocusIndex).iconview.startAnimation(nextViewAnim);
				}else{
					tranAnim.setDuration(3000);
					mainIconList.get(i).iconview//startAnimation(tranAnim);
						.animate()
						.translationXBy((mainIconList.get(0).iconview.getX() - mainIconList.get(1).iconview.getX()) * i)
						.alpha(0.0f)
						.start();
				}
			}
		}else if(mainIconList.get(curFocusIndex).type.equals("Movie")
				&& mainIconList.get(curFocusIndex).subIconList.get(curSubFocusIndex).type.equals("TopRecommend")){
			if(curSubFocusIndex < 0)
				return true;
			isGoToSubView = true;//请在跳转之前使用该变量，否则会有问题
			 if(timer != null){
				 timer.cancel();
				 timer = null;
				 //VoDViewManager.getInstance().hideLiveVideo();
			 }

			focusSubMenuAnim.setAnimationListener(subViewListener);
			((ImageView)mainIconList.get(curFocusIndex).subIconList.get(curSubFocusIndex).subiconview
				.findViewById(R.id.sub_pic_text_wrapcontent_pic)).startAnimation(focusSubMenuAnim);

		}else if(mainIconList.get(curFocusIndex).subIconList.isEmpty()){//没有二级菜单可直接进入
			isGoToSubView = true;//请在跳转之前使用该变量，否则会有问题
			 if(timer != null){
				 timer.cancel();
				 timer = null;
			 }
			 MainIcon mainIcon = mainIconList.get(curFocusIndex);
			VoDBaseView newView = VoDViewManager.newViewByType(mainIcon.type);
			if(newView != null) {
				newView.init(context, mainIcon.subJsonUrl,
						mainIconList.get(curFocusIndex).iconview);
				newView.setName(mainIcon.name);

				/* show the main view */
				VoDViewManager.getInstance().pushForegroundView(newView);
			}else{
				gotoTVActivity(mainIcon.type, mainIcon.subJsonUrl);//如果没有不是常规的viewtype,进入一体机定制信号通道
				isGoToSubView = false;
			}
		}else {
			if(curSubFocusIndex < 0)
				return true;
			isGoToSubView = true;
			 if(timer != null){
				 timer.cancel();
				 timer = null;
				 //VoDViewManager.getInstance().hideLiveVideo();
			 }
			SubIcon focusIcon = mainIconList.get(curFocusIndex).subIconList.get(curSubFocusIndex);
			VoDBaseView newView = VoDViewManager.newViewByType(focusIcon.type);

			if(newView != null) {
				newView.init(context, focusIcon.jsonUrl,
						mainIconList.get(curFocusIndex).subIconList.get(curSubFocusIndex).subiconview);
				newView.setName(focusIcon.name);

				/* show the sub view */
				VoDViewManager.getInstance().pushForegroundView(newView);
			}else{
				gotoTVActivity(focusIcon.type, focusIcon.jsonUrl);//如果没有不是常规的viewtype,进入一体机定制信号通道
				isGoToSubView = false;
			}
		}
		return true;
	}

	public void gotoTVActivity(String type,String url){
		if(type.equalsIgnoreCase("Skyworth_HDMI_angle")){
			Intent intent_main =  new Intent(iptv, SkyworthHDMIActivity.class);
			iptv.startActivityForResult(intent_main, 0);
			isGoToSubView = false;
		 }else if(type.equalsIgnoreCase("SkyworthATV")){
			 Intent intent_main =  new Intent(iptv, SKyworthATVActivity.class);
			intent_main.putExtra("source", "ATV");
			iptv.startActivityForResult(intent_main, 0);
			isGoToSubView = false;
		 }else if(type.equalsIgnoreCase("PhilipsDTV")){
			Intent intent_main =  new Intent(iptv, PhilipsHDMIActivity.class);
			intent_main.putExtra("source", "ATV");
			iptv.startActivity(intent_main);
			Log.i(TAG,"goto Philips dtv");
			isGoToSubView = false;
		}else if(type.equalsIgnoreCase("PhilipsHdmi")){
			Intent intent_main =  new Intent(iptv, PhilipsHDMIActivity.class);
			intent_main.putExtra("source", "ATV");
			iptv.startActivity(intent_main);
			Log.i(TAG,"goto Philips Hdmi or vga");
			isGoToSubView = false;
		}else{
			Log.i(TAG,"type is null ");
			isGoToSubView = false;
		}
	}

	public boolean onKeyBack() {

		if(isLoaded == false){
			return true;
		}
		long last = System.currentTimeMillis();
		if(last - lastTime < 300){
			return true;
		}
		if(VoDViewManager.getInstance().noWelcome){
			Log.i(TAG,"no welcome return");
			return true;
		}
		lastTime = last;
		return super.onKeyBack();
	}

	@Override
	public void back(){
		isGoToSubView = false;

		if(mainIconList.get(curFocusIndex).type.equals("Live")){
			isLoaded = false;

			VoDViewManager.getInstance().setVideoPreparedListener(null);
			VoDViewManager.getInstance().playBackgroundVideo();

			nextViewAnim = AnimationUtils.loadAnimation(context, R.anim.nextview_anim);
			//TranslateAnimation tranAnim = new TranslateAnimation(0, mainIconList.get(curFocusIndex).iconview.getX(), 0, 0);
			float x = mainIconList.get(curFocusIndex).iconview.getX();
			L.i("***x=" + moveLength);
			for(int i = 0; i < mainIconList.size(); i ++){
				if(i == curFocusIndex){
					nextViewAnim.setAnimationListener(null);
					mainIconList.get(curFocusIndex).iconview.startAnimation(nextViewAnim);
				}else{
					//tranAnim.setDuration(3000);
					mainIconList.get(i).iconview//startAnimation(tranAnim);
						.animate().translationXBy((moveLength) * i)
						.alpha(1.0f)
						.start();
				}

			}

			FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mLinearLayout.getLayoutParams();
			L.i("args" + ClearConfig.getScreenWidth() / 2 + "," +
					mainIconList.get(curFocusIndex).iconview.getX() + "," +
					params.leftMargin);

			mainIconList.get(curFocusIndex).iconview.animate()
				.translationX(0.0f)
				.translationY(0.0f)
				.scaleX(1.0f)
				.scaleY(1.0f)
				.setDuration(300)
				.setListener(null)
				.start();
			/*FIXME, remove me for test */
			FrameLayout.LayoutParams menuparams =
					(FrameLayout.LayoutParams) mFrameLayout.getLayoutParams();
			
			final int margin = menuparams.topMargin;

			if(timer == null)
				timer = new Timer(true);
			movex = 0;
			movey = 0;
			width = 0;
			height = 0;
			handler = new Handler(){  
				 public void handleMessage(Message msg) {
					 VoDViewManager.getInstance().setLiveVideoDisplayArea(movex, movey, 1280 - width, 720 - height);
					 if(movex < (forcusBackground.getX()) 
							 || movey < (forcusBackground.getY() + margin)
							 || (1280 - width) > (forcusBackground.getWidth())
							 || (720 - height) > (forcusBackground.getHeight())){
						 if(movex < (forcusBackground.getX()))
							 movex ++;
						 if(movey < (forcusBackground.getY() + margin))
							 movey ++;
						 if((1280 - width) > (forcusBackground.getWidth()))
							 width ++;
						 if((720 - height) > (forcusBackground.getHeight()))
							 height ++;
						 L.i("width:" + movey + ",height:" + forcusBackground.getY());
					 }else{
						 movex = (int) forcusBackground.getX();
						 movey = (int) forcusBackground.getY() + margin;
						 width =  1280 - forcusBackground.getWidth();
						 height = 720 - forcusBackground.getHeight();
						 if(timer != null){
							 timer.cancel();
							 timer = null;
							 //VoDViewManager.getInstance().hideLiveVideo();
						 }
					 }
				 }  
			};
			task = new TimerTask(){  
		       public void run() {  
		    	  if(isLoaded == false){
		    		  isLoaded = true;
		    	  }
//			      Message message = new Message();      
//			      message.what = 1;      
//				  handler.sendMessage(message);    
			   }  
			};
			timer.schedule(task, 1000, 10);
		}
	}
}
