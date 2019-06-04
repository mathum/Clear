/**
 * @author xujifu
 * @copyright clear
 * @date 2014-06-20
 * @description 语言栏界面
 */

package com.clearcrane.view;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.widget.TextView;

import com.clearcrane.activity.VoDActivity;
import com.clearcrane.log.ClearLog;
import com.clearcrane.log.L;
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

public class VoDLanguageCHNENGView extends VoDBaseView {

	ArrayList<LanguageIcon> iconList = new ArrayList<LanguageIcon>();
	LanguageIconPosMapping [] iconPosMap = new LanguageIconPosMapping[7];
	private int mScreenWidth;
	private FrameLayout mFrameLayout;
	private long lastTime = 0;
	private ImageView centerbackground, bodybackground, transbackground1, transbackground2, 
	transbackground3, transbackground4, railbackground1, railbackground2, railbackground3, 
	railbackground4, okbackground;
	private Animation centerAnim, centerAnimSec, bodyAnim, railDisplayAnim, DisplayAnim2Sec,
	okAnim, railFlickerAnim, welcomeDisappearAnim, welcomAppearAnim;
	private TextView mLanguageText;
	
	private ImageView logo, logobackground;
	private TextView messageTitle, messageBody, messageManager;
	private String messageTitleCHN, messageBodyCHN, messageManagerCHN,messageTitleENG, messageBodyENG, messageManagerENG;
	
	private String logoUrl, weatherUrl, mainviewUrl, time;
	private String LanguageJson;
	private String MainView_Type,subViewType;
	private String MainView_Json_URL;
	public Calendar begin = null;
	
	private ImageView language_bg_pic;
	public String TAG = "language";
	
	private VoDActivity mCtx = null;
	
	class LanguageIcon {
		String name = null;
		String url = null;
		ImageView iconView = null;
		String Code = null;
	}
	
	class LanguageIconPosMapping {
		int dataIndex = -1;
		int l = 0;
		int r = 0;
		int w = 0;
		int h = 0;
	}
	
	public void calcIconPos() {
		/* caculate position */
		mScreenWidth = ClearConfig.getScreenWidth();
		L.d("screen width: " + mScreenWidth);
		for(int i = 0, j = -3; i < 7; i ++, j ++){
			iconPosMap[i] = new LanguageIconPosMapping();
			iconPosMap[i].l = mScreenWidth / 8 * (j);
		}
	}
	
	/* data provider */
	/* build the view layout/element */
	/* start animation */
	public void init(Context ctx, String u) {
		begin = Calendar.getInstance();
		mCtx = (VoDActivity) ctx;
		super.init(ctx, u);
		
		view = LayoutInflater.from(context).inflate(R.layout.chneng_language_view, null);
		mFrameLayout = (FrameLayout)view.findViewById(R.id.language_tab);
		
		showOriginalView();
		calcIconPos();
		
		/* get all language icons */
		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(mLJCompleteListener);

		Log.i("in languaer view init",ClearConfig.MAIN_URI);
		 if(!VoDViewManager.getInstance().isBackupUri){
	        	mr.execute(ClearConfig.MAIN_URI);
	        }else{
	        	mr.execute(ClearConfig.BACKUP_URI);
	        }
	}
	
	private void showOriginalView(){
		mScreenWidth = ClearConfig.getScreenWidth();
		//mFrameLayout = (FrameLayout)view.findViewById(R.id.language_tab);
		centerAnim =  AnimationUtils.loadAnimation(context, R.anim.center_anim);
		centerAnimSec =  AnimationUtils.loadAnimation(context, R.anim.center_anim_second);
		bodyAnim = AnimationUtils.loadAnimation(context, R.anim.body_anim);
		railDisplayAnim = AnimationUtils.loadAnimation(context, R.anim.rail_display_anim);
		DisplayAnim2Sec = AnimationUtils.loadAnimation(context, R.anim.display_anim_2sec);
		okAnim = AnimationUtils.loadAnimation(context, R.anim.ok_display_anim);
		railFlickerAnim = AnimationUtils.loadAnimation(context, R.anim.rail_flicker_anim);
		welcomeDisappearAnim = AnimationUtils.loadAnimation(context, R.anim.welcome_disappear_anim);
		welcomAppearAnim = AnimationUtils.loadAnimation(context, R.anim.welcome_appear_anim);
		
		centerbackground = (ImageView)view.findViewById(R.id.center_background);
		bodybackground = (ImageView)view.findViewById(R.id.body_background);
		transbackground1 = (ImageView)view.findViewById(R.id.trans_background1);
		transbackground2 = (ImageView)view.findViewById(R.id.trans_background2);
		//transbackground3 = (ImageView)view.findViewById(R.id.trans_background3);
		//transbackground4 = (ImageView)view.findViewById(R.id.trans_background4);
		railbackground1  = (ImageView)view.findViewById(R.id.rail_background1);
		railbackground2 = (ImageView)view.findViewById(R.id.rail_background2);
		railbackground3 = (ImageView)view.findViewById(R.id.rail_background5);
		railbackground4 = (ImageView)view.findViewById(R.id.rail_background6);
		okbackground = (ImageView)view.findViewById(R.id.ok_background);
		mLanguageText = (TextView)view.findViewById(R.id.language_text);
		
		logo = (ImageView)view.findViewById(R.id.logo);
		logobackground = (ImageView)view.findViewById(R.id.logo_background);
		messageTitle = (TextView)view.findViewById(R.id.message_title);
		messageTitle.setTextColor(Color.rgb(255, 255, 255));
		messageBody = (TextView)view.findViewById(R.id.message_body);
		messageBody.setTextColor(Color.rgb(255, 255, 255));
		messageManager = (TextView)view.findViewById(R.id.message_manager);
		messageManager.setTextColor(Color.rgb(255, 255, 255));
		language_bg_pic = (ImageView)view.findViewById(R.id.language_bg_pic);
		
		//Typeface typeFace = Typeface.createFromAsset(context.getAssets(), "fonts/msyhl001.TTF");
		
		messageTitle.setTypeface(VoDViewManager.getInstance().typeFace, Typeface.BOLD);
		messageBody.setTypeface(VoDViewManager.getInstance().typeFace, Typeface.BOLD);
		messageManager.setTypeface(VoDViewManager.getInstance().typeFace, Typeface.BOLD);
		
		
		bodybackground.setVisibility(View.INVISIBLE);
		bodybackground.setAlpha(0.8f);
		railbackground1.setVisibility(View.INVISIBLE);
		railbackground2.setVisibility(View.INVISIBLE);
		railbackground3.setVisibility(View.INVISIBLE);
		railbackground4.setVisibility(View.INVISIBLE);
		transbackground1.setVisibility(View.INVISIBLE);
		transbackground2.setVisibility(View.INVISIBLE);
		//transbackground3.setVisibility(View.INVISIBLE);
		//transbackground4.setVisibility(View.INVISIBLE);
		okbackground.setVisibility(View.INVISIBLE);
		mLanguageText.setVisibility(View.INVISIBLE);
		logo.setVisibility(View.INVISIBLE);
		language_bg_pic.setVisibility(View.INVISIBLE);
		logobackground.setVisibility(View.INVISIBLE);
		messageTitle.setVisibility(View.INVISIBLE);
		messageBody.setVisibility(View.INVISIBLE);
		messageManager.setVisibility(View.INVISIBLE);
		
		centerbackground.startAnimation(centerAnim);
		centerAnim.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				centerbackground.clearAnimation();
				centerbackground.startAnimation(centerAnimSec);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
		});
		centerAnimSec.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				bodybackground.setVisibility(View.VISIBLE);
				bodybackground.startAnimation(bodyAnim);
				
				mLanguageText.setVisibility(View.VISIBLE);
				AnimationSet animationset = new AnimationSet(true);
				DisplayAnim2Sec.setFillAfter(true);
				animationset.addAnimation(DisplayAnim2Sec);
				animationset.setStartOffset(700);
				animationset.setFillAfter(true);
				mLanguageText.startAnimation(animationset);
				
				railbackground1.setVisibility(View.VISIBLE);
				railbackground2.setVisibility(View.VISIBLE);
				railbackground3.setVisibility(View.VISIBLE);
				railbackground4.setVisibility(View.VISIBLE);
				transbackground1.setVisibility(View.VISIBLE);
				transbackground2.setVisibility(View.VISIBLE);
				//transbackground3.setVisibility(View.VISIBLE);
				//transbackground4.setVisibility(View.VISIBLE);
				okbackground.setVisibility(View.VISIBLE);
				
				logo.setVisibility(View.VISIBLE);
				logobackground.setVisibility(View.VISIBLE);
				messageTitle.setVisibility(View.VISIBLE);
				messageBody.setVisibility(View.VISIBLE);
				messageManager.setVisibility(View.VISIBLE);
				
				railbackground1.startAnimation(DisplayAnim2Sec);
				railbackground2.startAnimation(DisplayAnim2Sec);
				railbackground3.startAnimation(DisplayAnim2Sec);
				railbackground4.startAnimation(DisplayAnim2Sec);
				
				transbackground1.startAnimation(DisplayAnim2Sec);
				transbackground2.startAnimation(DisplayAnim2Sec);
				//transbackground3.startAnimation(DisplayAnim2Sec);
				//transbackground4.startAnimation(DisplayAnim2Sec);
				okbackground.startAnimation(okAnim);
				
				logo.startAnimation(DisplayAnim2Sec);
				logobackground.startAnimation(DisplayAnim2Sec);
				language_bg_pic.setVisibility(View.VISIBLE);
				messageTitle.startAnimation(DisplayAnim2Sec);
				messageBody.startAnimation(DisplayAnim2Sec);
				messageManager.startAnimation(DisplayAnim2Sec);
				
				
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	
	/**
	 * LanguageJsonCompleteListener
	 */
	private OnCompleteListener mLJCompleteListener = new OnCompleteListener(){

		@Override
		public void onDownloaded(Object result) {
			
			LanguageJson = (String)result;
			if(LanguageJson == null){
				TipDialog.Builder builder = new TipDialog.Builder(context);
				builder.setMessage("当前网络不可用，请检查网络1");  
		        builder.setTitle("提示");  
		        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {  
		            public void onClick(DialogInterface dialog, int which) {  
		                dialog.dismiss();  
		                //设置你的操作事项  
		                mCtx = (VoDActivity) context;
		        		mCtx.showSettingsDialog();
		            }  
		        });  
		  
		        builder.create().show();  
				return;
			}
			try {
				JSONTokener jsonParser = new JSONTokener(LanguageJson);  
			    JSONObject object = (JSONObject) jsonParser.nextValue();  
			    JSONArray array = (JSONArray)object.getJSONArray("Content");
			    
				for(int i = 0; i < array.length(); i ++){
					JSONObject objecttmp = (JSONObject)array.opt(i);
					LanguageIcon icon = new LanguageIcon();
					icon.name = objecttmp.getString("Name");
					icon.url = ClearConfig.getJsonUrl(context, objecttmp.getString("URL"));
					icon.iconView = new ImageView(context);
					icon.Code = objecttmp.getString("Code");
					MaterialRequest image = new MaterialRequest(context, icon.iconView, ClearConfig.TYPE_IMAGE);
					image.execute(icon.url);
					iconList.add(icon);
				}
				logoUrl = ClearConfig.getJsonUrl(context, object.getString("logo"));
				//weatherUrl =  ClearConfig.getJsonUrl(context, object.getString("weather"));
				mainviewUrl =  ClearConfig.getJsonUrl(context, object.getString("MainView_Json_URL"));
				messageTitleCHN = object.getString("guest_name");
				messageTitleENG = object.getString("guest_name_eng");
				messageBodyCHN = object.getString("welcome_text");
				messageBodyENG = object.getString("welcome_text_eng");
				messageManagerCHN = object.getString("hotel_manager_name");
				if(messageManagerCHN == null){
					messageManagerCHN = "";
				}
				messageManagerENG = object.getString("hotel_manager_name_eng");
				if(messageManagerENG == null){
					messageManagerENG = "";
				}
				//messageTitle.setText(messageTitleCHN);
				//messageBody.setText(messageBodyCHN);
				//messageManager.setText(messageManagerCHN);
				MainView_Type = object.getString("MainViewType");
				subViewType = object.getString("MainView_style_type");
				MainView_Json_URL = ClearConfig.getJsonUrl(context, object.getString("MainView_Json_URL"));
//				VoDViewManager.getInstance().setBackgroundVideoURL(
//						ClearConfig.getJsonUrl(context, object.getString("background_video_url")));
				VoDViewManager.getInstance().showBackgroundVideo();
				VoDViewManager.getInstance().playBackgroundVideo();
				
				MaterialRequest logoMR = new MaterialRequest(context, logo, ClearConfig.TYPE_IMAGE);
				logoMR.execute(logoUrl);
				if(begin != null) {
                	long between = (Calendar.getInstance()).getTimeInMillis()-begin.getTimeInMillis(); 
                    ClearLog.LogInfo("BROSWER\tLoad\tSUCC\t" + between +"ms\t" + url + "\t"
                    		+ "language");
                } 
				
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				ClearLog.LogError("BROSWER\tLoad\tFAIL\t0ms\t" + ClearConfig.MAIN_URI);
				e.printStackTrace();
			}
			
			/* put the possible icon into views. the first on in middle, then left, then right */
			int i = 0;
			//iconPosMap[2].dataIndex = 0;
			//iconPosMap[4].dataIndex = 1;
			
			if(iconList.size() < 6){
				i = (7 - iconList.size()) / 2;
			}
			
			for(int j = 0; i < 7 && j < iconList.size(); i++, j++ ) {
				iconPosMap[i].dataIndex = j;
			}
			
			/* show view and do the animation */
			for(i = 0; i < 7; i ++){
				int posIndex;
				if(i % 2 == 0) {
					posIndex = 7 / 2 + i / 2;	
				}
				else {
					posIndex = 7 / 2 - (i + 1)/ 2;
				}
				if(iconPosMap[posIndex].dataIndex >= 0) {
					/* do the animation */
					FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
							FrameLayout.LayoutParams.WRAP_CONTENT, 
							FrameLayout.LayoutParams.WRAP_CONTENT);
					lp.gravity = android.view.Gravity.CENTER;
					lp.setMargins(0, 20, 0, 20);
					iconList.get(iconPosMap[posIndex].dataIndex).iconView.setAlpha(0.0f);
					mFrameLayout.addView(iconList.get(iconPosMap[posIndex].dataIndex).iconView, lp);
					if(posIndex == 3){
						iconList.get(iconPosMap[posIndex].dataIndex).iconView.animate()
							.translationX(iconPosMap[posIndex].l)
							.translationYBy(-10.0f)
							.scaleX(1.6f)
							.scaleY(1.2f)
							.alpha(1.0f)
							.setStartDelay(1400)
							.setDuration(300)
							.start();
						mLanguageText.setText(iconList.get(iconPosMap[posIndex].dataIndex).name);
						ClearConfig.setLanguageIdByIconName(iconList.get(iconPosMap[3].dataIndex).Code);
						messageTitle.setText(ClearConfig.getStringByLanguageId(messageTitleCHN,messageTitleENG));
						messageBody.setText(ClearConfig.getStringByLanguageId(messageBodyCHN,messageBodyENG));
						messageManager.setText(ClearConfig.getStringByLanguageId(messageManagerCHN,messageManagerENG));
					}else{
						iconList.get(iconPosMap[posIndex].dataIndex).iconView.animate()
							.translationX(iconPosMap[posIndex].l)
							.scaleX(1.0f)
							.alpha(1.0f)
							.setStartDelay(1400)
							.setDuration(300)
							.start();
					}
				}
			}
		}

		@Override
		public void onComplete(boolean result) {
			// TODO Auto-generated method stub
			
		}
	};
	
	public void RailFlicker(){
		railbackground1.startAnimation(railFlickerAnim);
		railbackground2.startAnimation(railFlickerAnim);
		railbackground3.startAnimation(railFlickerAnim);
		railbackground4.startAnimation(railFlickerAnim);
	}
	
	public boolean onKeyDpadLeft() {
		//Log.i("in left","begining");
		long last = System.currentTimeMillis();
		if(last - lastTime < 300 && last - lastTime > 0){
			return true;
		}
		lastTime = last;
		if(iconList.size() <= 1) {
			return true;
		}
		
		for(int i = 0; i < 7; i++ ) {
			int nextPos = i - 1;
			//Log.i("in left","begining"+i);
			//L.i("" + iconList.get(iconPosMap[i].dataIndex).iconView.getX());
			
			if(iconPosMap[i].dataIndex < 0) {
				continue;
			}
			
			if(nextPos < 0 || iconPosMap[nextPos].dataIndex < 0) {
				//Log.i("in right","nextpos"+ nextPos);
				/* the last left */
				//iconList.get(iconPosMap[i].dataIndex).iconView.clearAnimation();
				//iconList.get(iconPosMap[i].dataIndex).iconView.setX(iconPosMap[i].l);
				/*iconList.get(iconPosMap[i].dataIndex).iconView.animate()
				.translationX(iconPosMap[nextPos].l)
				.translationY(0.0f)
				.scaleX(1.0f)
				.scaleY(1.0f)
				.setInterpolator(new android.view.animation.DecelerateInterpolator())
				.setDuration(150)
				.setStartDelay(0)
				.start();*/
				iconList.get(iconPosMap[i].dataIndex).iconView.animate()
					.translationXBy(-(iconPosMap[i + 1].l - iconPosMap[i].l) / 3)
					//.scaleX(0.5f)
					.scaleX(1.6f)
					.scaleY(1.2f)
					.setStartDelay(0)
					.setDuration(50)
					.setListener(new AnimatorListener(){

						@Override
						public void onAnimationCancel(Animator arg0) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onAnimationEnd(Animator arg0) {
							
							// TODO Auto-generated method stub
							if(iconList.size() <= 7) {
								int j;
								for(j = 3; j < ((iconList.size() - 1) / 2 + 3); j++) 
									if(iconPosMap[j + 1].dataIndex < 0) 
										break;
								j = (j >= ((iconList.size() - 1) / 2 + 3) ? j : j + 1);
								//Log.i("in left","j"+ j);
								iconList.get(iconPosMap[j].dataIndex).iconView.setX(iconPosMap[j].l  + mScreenWidth / 2 + mScreenWidth / 56);
								iconList.get(iconPosMap[j].dataIndex).iconView.animate()
									.translationX(iconPosMap[j].l)
									.translationYBy(-8.0f)
									.scaleX(1.6f)
									.scaleY(1.2f)
									.setDuration(100)
									.setStartDelay(0)
									.setListener(null)
									.start();
							}
							else {
								//TODO, FIXME, disappear and make the new 
								ImageView leftview = iconList.get((iconPosMap[0].dataIndex + iconList.size() - 1) % iconList.size()).iconView;
								leftview.setScaleX(1.0f);
								leftview.setVisibility(View.GONE);

								ImageView rightview = iconList.get((iconPosMap[6].dataIndex) % iconList.size()).iconView;
								if(rightview.getParent() == null) {
									FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
											FrameLayout.LayoutParams.WRAP_CONTENT,
											FrameLayout.LayoutParams.WRAP_CONTENT);
									lp.gravity = android.view.Gravity.CENTER;

									lp.setMargins(0, 20, 0, 20);
									mFrameLayout.addView(rightview, lp);
									rightview.setX(iconPosMap[6].l + mScreenWidth / 16);
								}
								else
								{
									rightview.setVisibility(View.VISIBLE);
									rightview.setX(iconPosMap[6].l  + mScreenWidth / 2 + mScreenWidth / 56);
								}
								rightview.setScaleX(0.5f);
								rightview.animate()
									.translationX(iconPosMap[6].l)
									.scaleX(1.0f)
									.setDuration(100)
									.setStartDelay(0)
									.setListener(null)
									.start();

							}
						}

						@Override
						public void onAnimationRepeat(Animator arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onAnimationStart(Animator arg0) {
							// TODO Auto-generated method stub

						}

					})
					.start();

			}
			else {
				/* normal */
				//iconList.get(iconPosMap[i].dataIndex).iconView.clearAnimation();
				//iconList.get(iconPosMap[i].dataIndex).iconView.setX(iconPosMap[i].l);

				L.i("  " + iconPosMap[nextPos].l);
				Log.i("in left","i="+ i);

				if(3 == i) {
					//Log.i("in left","centpic"+ nextPos);
					iconList.get(iconPosMap[i].dataIndex).iconView.animate()
						.translationX(iconPosMap[nextPos].l)
						//.translationYBy(-8.0f)
						.translationY(0.0f)
						.scaleX(1.0f)
						.scaleY(1.0f)
						.setInterpolator(new android.view.animation.DecelerateInterpolator())
						.setDuration(150)
						.setStartDelay(0)
						.start();
				}
				else {
					iconList.get(iconPosMap[i].dataIndex).iconView.animate()
						.translationX(iconPosMap[nextPos].l)
						.translationY(0.0f)
						.scaleX(1.0f)
						.scaleY(1.0f)
						.setInterpolator(new android.view.animation.DecelerateInterpolator())
						.setDuration(150)
						.setStartDelay(0)
						.start();
				}
			}
			iconPosMap[i].dataIndex = (iconPosMap[i].dataIndex + 1) % iconList.size();
		}
		//Log.i("in left","after nextpos");
		mLanguageText.setText(iconList.get(iconPosMap[3].dataIndex).name);
		ClearConfig.setLanguageIdByIconName(iconList.get(iconPosMap[3].dataIndex).Code);
		messageTitle.setText(ClearConfig.getStringByLanguageId(messageTitleCHN,messageTitleENG));
		messageBody.setText(ClearConfig.getStringByLanguageId(messageBodyCHN,messageBodyENG));
		messageManager.setText(ClearConfig.getStringByLanguageId(messageManagerCHN,messageManagerENG));
		TranslateAnimation transanimation = new TranslateAnimation(100, 0, 0, 0);
		AlphaAnimation alphpanimation = new AlphaAnimation(0.0f, 1.0f);
		AnimationSet animationSet = new AnimationSet(true);
		animationSet.addAnimation(transanimation);
		animationSet.addAnimation(alphpanimation);
		animationSet.setDuration(150);
		animationSet.setFillAfter(true);
		mLanguageText.startAnimation(animationSet);
		//RailFlicker();
		return true;
	}

	public boolean onKeyDpadRight() {
		long last = System.currentTimeMillis();
		if(last - lastTime < 300 && last - lastTime > 0 ){
			return true;
		}
		lastTime = last;
		if(iconList.size() <= 1) {
			return true;
		}

		for(int i = 6; i >= 0; i-- ) {
			int nextPos = i + 1;

			//L.i("" + iconList.get(iconPosMap[i].dataIndex).iconView.getX());

			if(iconPosMap[i].dataIndex < 0) {
				continue;
			}

			if(nextPos > 6 || iconPosMap[nextPos].dataIndex < 0) {
				/* the last left */
				//iconList.get(iconPosMap[i].dataIndex).iconView.clearAnimation();
				//iconList.get(iconPosMap[i].dataIndex).iconView.setX(iconPosMap[i].l);
				iconList.get(iconPosMap[i].dataIndex).iconView.animate()
					.translationXBy(-(iconPosMap[i - 1].l - iconPosMap[i].l) / 3)
					.scaleX(1.0f)
					.translationY(0.0f)
					.setDuration(50)
					.setStartDelay(0)
					.setListener(new AnimatorListener(){

						@Override
						public void onAnimationCancel(Animator arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onAnimationEnd(Animator arg0) {
							// TODO Auto-generated method stub
							if(iconList.size() <= 7) {
								int j;
								for(j = 3; j > (3 - iconList.size() / 2); j--)
									if(iconPosMap[j - 1].dataIndex < 0)
										break;
								j = (j <= (3 - iconList.size() / 2) ? j : j - 1);

								iconList.get(iconPosMap[j].dataIndex).iconView.setX(iconPosMap[j].l  + mScreenWidth / 2 - mScreenWidth / 12);
								iconList.get(iconPosMap[j].dataIndex).iconView.animate()
									.translationX(iconPosMap[j].l)
									.scaleX(1.0f)
									.setDuration(100)
									.setStartDelay(0)
									.setListener(null)
									.start();
							}
							else {
								//TODO, FIXME, disappear and make the new
								ImageView rightview = iconList.get((iconPosMap[6].dataIndex + iconList.size() + 1) % iconList.size()).iconView;
								rightview.setScaleX(1.0f);
								rightview.setVisibility(View.GONE);

								ImageView leftview = iconList.get((iconPosMap[0].dataIndex) % iconList.size()).iconView;
								if(leftview.getParent() == null) {
									FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
											FrameLayout.LayoutParams.WRAP_CONTENT,
											FrameLayout.LayoutParams.WRAP_CONTENT);
									lp.gravity = android.view.Gravity.CENTER;

									lp.setMargins(0, 20, 0, 20);
									mFrameLayout.addView(leftview, lp);
									leftview.setX(iconPosMap[0].l - mScreenWidth / 16);
								}
								else {
									leftview.setVisibility(View.VISIBLE);
									leftview.setX(iconPosMap[0].l + mScreenWidth / 2 - mScreenWidth / 10);
								}
								leftview.setScaleX(0.5f);
								
								leftview.animate()
									.translationX(iconPosMap[0].l)
									.scaleX(1.0f)
									.setDuration(100)
									.setStartDelay(0)
									.setListener(null)
									.start();
								
							}
						}

						@Override
						public void onAnimationRepeat(Animator arg0) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onAnimationStart(Animator arg0) {
							// TODO Auto-generated method stub
							
						}
						
					})
					.start();
					
			}
			else {
				/* normal */
				
				L.i("  " + iconPosMap[nextPos].l);
				
				if(2 == i) {
					//Log.i("in right","int i==2");
					iconList.get(iconPosMap[i].dataIndex).iconView.animate()
						.translationX(iconPosMap[nextPos].l)
						.translationYBy(-8.0f)
						.scaleX(1.6f)
						.scaleY(1.2f)
						.setInterpolator(new android.view.animation.DecelerateInterpolator())
						.setDuration(150)
						.setStartDelay(0)
						.start();
				}
				else {
					iconList.get(iconPosMap[i].dataIndex).iconView.animate()
						.translationX(iconPosMap[nextPos].l)
						.translationY(0.0f)
						.scaleX(1.0f)
						.scaleY(1.0f)
						.setInterpolator(new android.view.animation.DecelerateInterpolator())
						.setDuration(150)
						.setStartDelay(0)
						.start();
				}
			}
			iconPosMap[i].dataIndex = (iconPosMap[i].dataIndex + iconList.size() - 1) % iconList.size();
		}
		mLanguageText.setText(iconList.get(iconPosMap[3].dataIndex).name);
		ClearConfig.setLanguageIdByIconName(iconList.get(iconPosMap[3].dataIndex).Code);
		messageTitle.setText(ClearConfig.getStringByLanguageId(messageTitleCHN,messageTitleENG));
		messageBody.setText(ClearConfig.getStringByLanguageId(messageBodyCHN,messageBodyENG));
		messageManager.setText(ClearConfig.getStringByLanguageId(messageManagerCHN,messageManagerENG));
		TranslateAnimation transanimation = new TranslateAnimation(-100, 0, 0, 0);
		AlphaAnimation alphpanimation = new AlphaAnimation(0.0f, 1.0f);
		AnimationSet animationSet = new AnimationSet(true);
		animationSet.addAnimation(transanimation);
		animationSet.addAnimation(alphpanimation);
		animationSet.setDuration(150);
		animationSet.setFillAfter(true);
		mLanguageText.startAnimation(animationSet);
		RailFlicker();
		return true;
	}
	
	public boolean onKeyEnter() {
		Log.i("in key","language enter");

		welcomeDisappearAnim.setFillAfter(true);

		centerbackground.startAnimation(welcomeDisappearAnim);
		bodybackground.startAnimation(welcomeDisappearAnim);
		mLanguageText.startAnimation(welcomeDisappearAnim);
		
		railbackground1.startAnimation(welcomeDisappearAnim);
		railbackground2.startAnimation(welcomeDisappearAnim);
		railbackground3.startAnimation(welcomeDisappearAnim);
		railbackground4.startAnimation(welcomeDisappearAnim);
		
		transbackground1.startAnimation(welcomeDisappearAnim);
		transbackground2.startAnimation(welcomeDisappearAnim);
		//transbackground3.startAnimation(welcomeDisappearAnim);
		//transbackground4.startAnimation(welcomeDisappearAnim);
		okbackground.startAnimation(welcomeDisappearAnim);
		
		logo.startAnimation(welcomeDisappearAnim);
		logobackground.startAnimation(welcomeDisappearAnim);
		messageTitle.startAnimation(welcomeDisappearAnim);
		messageBody.startAnimation(welcomeDisappearAnim);
		messageManager.startAnimation(welcomeDisappearAnim);

		
		for(int i = 6; i >= 0; i-- ) {
			
			if(iconPosMap[i].dataIndex < 0) {
				continue;
			}

			/* normal */

			if(i == 3)
				iconList.get(iconPosMap[i].dataIndex).iconView.animate()
					.translationX(iconPosMap[3].l)
					.translationY(0.0f)
					.alphaBy(0.0f)
					.scaleX(0.0f)
					.scaleY(0.0f)
					.setInterpolator(new android.view.animation.DecelerateInterpolator())
					.setDuration(150)
					.setStartDelay(0)
					.setListener(welcomeCompleteListener)
					.start();
			else
				iconList.get(iconPosMap[i].dataIndex).iconView.animate()
					.translationX(iconPosMap[3].l)
					.translationY(0.0f)
					.alphaBy(0.0f)
					.scaleX(0.0f)
					.scaleY(0.0f)
					.setInterpolator(new android.view.animation.DecelerateInterpolator())
					.setDuration(150)
					.setStartDelay(0)
					.start();
		}
		
		return true;
	}
	
	private AnimatorListener welcomeCompleteListener = new  AnimatorListener(){

		@Override
		public void onAnimationCancel(Animator animation) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			// TODO Auto-generated method stub
			VoDBaseView newView = VoDViewManager.getInstance().newViewByType(MainView_Type,subViewType);
			newView.init(context, MainView_Json_URL);
			
			VoDViewManager.getInstance().pushForegroundView(newView);
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
	
	public boolean onKeyBack() {
		return true;
	}
	
	/*public boolean onKeyDown(int code,KeyEvent event) {
		Log.i(TAG,"language key down");
		return true;
	}*/
	
	@Override
	public void back(){
		welcomAppearAnim.setFillAfter(true);

		centerbackground.startAnimation(welcomAppearAnim);
		bodybackground.startAnimation(welcomAppearAnim);
		mLanguageText.startAnimation(welcomAppearAnim);
		
		railbackground1.startAnimation(welcomAppearAnim);
		railbackground2.startAnimation(welcomAppearAnim);
		railbackground3.startAnimation(welcomAppearAnim);
		railbackground4.startAnimation(welcomAppearAnim);
		
		transbackground1.startAnimation(welcomAppearAnim);
		transbackground2.startAnimation(welcomAppearAnim);
		//transbackground3.startAnimation(welcomAppearAnim);
		//transbackground4.startAnimation(welcomAppearAnim);
		okbackground.startAnimation(welcomAppearAnim);
		
		logo.startAnimation(welcomAppearAnim);
		logobackground.startAnimation(welcomAppearAnim);
		messageTitle.startAnimation(welcomAppearAnim);
		messageBody.startAnimation(welcomAppearAnim);
		messageManager.startAnimation(welcomAppearAnim);

		
		for(int i = 6; i >= 0; i-- ) {
			
			if(iconPosMap[i].dataIndex < 0) {
				continue;
			}

			/* normal */

			if(i == 3)
				iconList.get(iconPosMap[i].dataIndex).iconView.animate()
					.translationX(iconPosMap[i].l)
					.translationY(0.0f)
					.alphaBy(1.0f)
					.scaleX(1.0f)
					.scaleY(1.0f)
					.setInterpolator(new android.view.animation.DecelerateInterpolator())
					.setDuration(150)
					.setStartDelay(0)
					.setListener(null)
					.start();
			else
				iconList.get(iconPosMap[i].dataIndex).iconView.animate()
					.translationX(iconPosMap[i].l)
					.translationY(0.0f)
					.alphaBy(1.0f)
					.scaleX(1.0f)
					.scaleY(1.0f)
					.setInterpolator(new android.view.animation.DecelerateInterpolator())
					.setDuration(150)
					.setStartDelay(0)
					.start();
		}
	}

}
