package com.clearcrane.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.clearcrane.log.L;
import com.clearcrane.provider.MaterialRequest;
import com.clearcrane.provider.MaterialRequest.OnCompleteListener;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.vod.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;

public class VoDLanguageView extends VoDBaseView {

	private FrameLayout mFrameLayout;
	private ArrayList<ImageView> mLanguageImage;
	private ImageView centerbackground, bodybackground, transbackground1, transbackground2, 
	transbackground3, transbackground4, railbackground1, railbackground2, railbackground3, 
	railbackground4, okbackground;
	private TextView mLanguageText;
	
	private int mScreenWidth;
	private int mCurrent = 3;
	private String mMainJsonUrl;
	private String mLanguageJson;
	Animation centerAnim, centerAnimSec, bodyAnim, railDisplayAnim, DisplayAnim2Sec;
	
	private class mLanguageInfo{
		public String name;
		public String uri;
		public Bitmap pic;
	}
	
	/* data provider */
	/* build the view layout/element */
	/* animation */	
	@Override
	public void init(Context ctx, String u) {
		super.init(ctx, u);
		
		view = LayoutInflater.from(context).inflate(R.layout.language_view, null);
		//show view on xml
		showOriginalView();

		//for test, remove me
		//VoDViewManager.getInstance().startBackgroundVideo("http://101.231.164.210/hls/demo/demo.m3u8");
		VoDViewManager.getInstance().startBackgroundVideo("http://192.168.17.248:8080/movie/ZI_Final_Rated_1080_ESRB.mkv");
		
		//trigger data get and register callback
		mLanguageImage = new ArrayList<ImageView>();
		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(mLJCompleteListener);
		 if(!VoDViewManager.getInstance().isBackupUri){
	        	mr.execute(ClearConfig.MAIN_URI);
	        }else{
	        	mr.execute(ClearConfig.BACKUP_URI);
	        }

		//for test, remove me
		VoDViewManager.getInstance().startBackgroundVideo("http://101.231.164.210/hls/demo/demo.m3u8");
		//mLanguageImage = new ArrayList<ImageView>();

	}
	
	private void showOriginalView(){
		mScreenWidth = ClearConfig.getScreenWidth();
		mFrameLayout = (FrameLayout)view.findViewById(R.id.language_tab);
		centerAnim =  AnimationUtils.loadAnimation(context, R.anim.center_anim);
		centerAnimSec =  AnimationUtils.loadAnimation(context, R.anim.center_anim_second);
		bodyAnim = AnimationUtils.loadAnimation(context, R.anim.body_anim);
		railDisplayAnim = AnimationUtils.loadAnimation(context, R.anim.rail_display_anim);
		DisplayAnim2Sec = AnimationUtils.loadAnimation(context, R.anim.display_anim_2sec);
		centerbackground = (ImageView)view.findViewById(R.id.center_background);
		bodybackground = (ImageView)view.findViewById(R.id.body_background);
		transbackground1 = (ImageView)view.findViewById(R.id.trans_background1);
		transbackground2 = (ImageView)view.findViewById(R.id.trans_background2);
		transbackground3 = (ImageView)view.findViewById(R.id.trans_background3);
		transbackground4 = (ImageView)view.findViewById(R.id.trans_background4);
		railbackground1  = (ImageView)view.findViewById(R.id.rail_background1);
		railbackground2 = (ImageView)view.findViewById(R.id.rail_background2);
		railbackground3 = (ImageView)view.findViewById(R.id.rail_background5);
		railbackground4 = (ImageView)view.findViewById(R.id.rail_background6);
		okbackground = (ImageView)view.findViewById(R.id.ok_background);
		mLanguageText = (TextView)view.findViewById(R.id.language_text);
		
		bodybackground.setVisibility(View.INVISIBLE);
		railbackground1.setVisibility(View.INVISIBLE);
		railbackground2.setVisibility(View.INVISIBLE);
		railbackground3.setVisibility(View.INVISIBLE);
		railbackground4.setVisibility(View.INVISIBLE);
		transbackground1.setVisibility(View.INVISIBLE);
		transbackground2.setVisibility(View.INVISIBLE);
		transbackground3.setVisibility(View.INVISIBLE);
		transbackground4.setVisibility(View.INVISIBLE);
		okbackground.setVisibility(View.INVISIBLE);
		mLanguageText.setVisibility(View.INVISIBLE);
		
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
			// TODO Auto-generated method stub
			mLanguageJson = (String)result;
			L.i("on" + mLanguageJson);
			try {
				JSONTokener jsonParser = new JSONTokener(mLanguageJson);  
			    // 此时还未读取任何json文本，直接读取就是一个JSONObject对象。  
			    // 如果此时的读取位置在"name":了，那么nextValue就是String  
			    JSONObject object = (JSONObject) jsonParser.nextValue();  
			    // 接下来的就是JSON对象的操作了
			    JSONArray array = (JSONArray)object.getJSONArray("Content");
				for(int i = 0; i < array.length(); i ++){
					JSONObject objecttmp = (JSONObject)array.opt(i);
					objecttmp.getString("Name");
					objecttmp.getString("URL");
					L.i("json url" + objecttmp.getString("URL"));
					ImageView iv = new ImageView(context);
					//iv.setImageURI(Uri.parse(objecttmp.getString("URL")));
					//iv.setImageResource(R.drawable.ic_launcher);
					MaterialRequest image = new MaterialRequest(context, iv, ClearConfig.TYPE_IMAGE);
					image.execute(objecttmp.getString("URL"));
					mLanguageImage.add(iv);
					//mFrameLayout.addView(iv);
				}
				for(int i = 0; i < mLanguageImage.size(); i ++){
					AnimationSet animationset = new AnimationSet(true);
					if(i < 3){
						ObjectAnimator animX = ObjectAnimator.ofFloat(mLanguageImage.get(i), "x", -mScreenWidth * (3 - i) / 8);
						animX.setDuration(700);
						animX.setStartDelay((7 - i) * 200);
						animX.start();
						//image.get(i).animate().translationX(-width * (3 - i) / 8).setDuration(2000).setStartDelay(700 + (7 - i) * 1000).start();
						//TranslateAnimation translateAnimation = new TranslateAnimation(0, -width * (3 - i) / 8, 0, 0);
						//translateAnimation.setDuration(3700);
						//translateAnimation.setFillAfter(true);
						ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f,1.0f,1.0f,1.0f,
								Animation.RELATIVE_TO_SELF,0.5f,
								Animation.RELATIVE_TO_SELF,0.5f);
						scaleAnimation.setStartOffset((7 - i) * 200);
						scaleAnimation.setDuration(700);
						scaleAnimation.setFillAfter(true);
						AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
						alphaAnimation.setStartOffset((7 - i) * 200);
						alphaAnimation.setDuration(700);
						alphaAnimation.setFillAfter(true);
						
						//animationset.addAnimation(translateAnimation);
						animationset.addAnimation(scaleAnimation);
						animationset.addAnimation(alphaAnimation);
						animationset.setStartOffset(700);
						animationset.setFillAfter(true);
					}else if(i == 3){
						ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f,1.2f,0.0f,1.2f,   
								Animation.RELATIVE_TO_SELF,0.5f,
								Animation.RELATIVE_TO_SELF,0.5f);
						scaleAnimation.setStartOffset((i + 2) * 200);
						scaleAnimation.setDuration(700);
						scaleAnimation.setFillAfter(true);
						AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
						alphaAnimation.setStartOffset((i + 2) * 200);
						alphaAnimation.setDuration(700);
						alphaAnimation.setFillAfter(true);
						
						animationset.addAnimation(scaleAnimation);
						animationset.addAnimation(alphaAnimation);
						animationset.setStartOffset(700);
						animationset.setFillAfter(true);
						animationset.setAnimationListener(new AnimationListener(){

							@Override
							public void onAnimationEnd(Animation animation) {
								// TODO Auto-generated method stub
								railbackground2.setVisibility(View.VISIBLE);
								railbackground3.setVisibility(View.VISIBLE);
								//railDisplayAnim.setStartOffset(3000);
								railbackground2.startAnimation(railDisplayAnim);
								railbackground3.startAnimation(railDisplayAnim);
								
								mLanguageText.setVisibility(View.VISIBLE);
								AnimationSet animationset = new AnimationSet(true);
								DisplayAnim2Sec.setFillAfter(true);
								animationset.addAnimation(DisplayAnim2Sec);
								animationset.setStartOffset(700);
								animationset.setFillAfter(true);
								mLanguageText.startAnimation(animationset);
								
								railbackground1.setVisibility(View.VISIBLE);
								railbackground4.setVisibility(View.VISIBLE);
								transbackground1.setVisibility(View.VISIBLE);
								transbackground2.setVisibility(View.VISIBLE);
								transbackground3.setVisibility(View.VISIBLE);
								transbackground4.setVisibility(View.VISIBLE);
								okbackground.setVisibility(View.VISIBLE);
								railbackground1.startAnimation(DisplayAnim2Sec);
								railbackground4.startAnimation(DisplayAnim2Sec);
								transbackground1.startAnimation(DisplayAnim2Sec);
								transbackground2.startAnimation(DisplayAnim2Sec);
								transbackground3.startAnimation(DisplayAnim2Sec);
								transbackground4.startAnimation(DisplayAnim2Sec);
								okbackground.startAnimation(DisplayAnim2Sec);
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
					}else{
						//image.get(i).animate().translationX( width * (i - 3) / 8).setDuration(2000).setStartDelay(700 + (i + 1) * 1000).start();
//						TranslateAnimation translateAnimation = new TranslateAnimation(0, width * (i - 3) / 8, 0, 0);
//						translateAnimation.setDuration(3700);
//						translateAnimation.setFillAfter(true);
						
						ObjectAnimator animX = ObjectAnimator.ofFloat(mLanguageImage.get(i), "x", mScreenWidth * (i - 3) / 8);
						animX.setDuration(700);
						animX.setStartDelay((i + 1) * 200);
						animX.start();
						ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f,1.0f,1.0f,1.0f,   
								Animation.RELATIVE_TO_SELF,0.5f,
								Animation.RELATIVE_TO_SELF,0.5f);
						scaleAnimation.setStartOffset((i + 1) * 200);
						scaleAnimation.setDuration(700);
						scaleAnimation.setFillAfter(true);
						AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
						alphaAnimation.setStartOffset((i + 1) * 200);
						alphaAnimation.setDuration(700);
						alphaAnimation.setFillAfter(true);
						
						//animationset.addAnimation(translateAnimation);
						animationset.addAnimation(scaleAnimation);
						animationset.addAnimation(alphaAnimation);
						animationset.setStartOffset(700);
						animationset.setFillAfter(true);
					}
					
					mFrameLayout.addView(mLanguageImage.get(i));
					mLanguageImage.get(i).startAnimation(animationset);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void onComplete(boolean result) {
			// TODO Auto-generated method stub
		}
		
	}; 
	

	public boolean onKeyDpadLeft() {

		if(mCurrent < mLanguageImage.size() - 1)
			mCurrent ++;
		else
			mCurrent = 0;
		
		for(int j = -4; j <= 2; j++){
			int i = mCurrent + j;
			ObjectAnimator animX = ObjectAnimator.ofFloat(mLanguageImage.
					get((mLanguageImage.size() + i) % mLanguageImage.size()), "translationX", mScreenWidth * j / 8);
			animX.setDuration(2000);
			animX.start();
			
			L.i("image num1:" + (mLanguageImage.size() + i) % mLanguageImage.size() +
					":" + mLanguageImage.get((mLanguageImage.size() + i) % mLanguageImage.size()).getX());
		}
		if(mLanguageImage.get((mCurrent + 3) % mLanguageImage.size()).getParent() != null)
			mFrameLayout.removeView(mLanguageImage.get((mCurrent + 3) % mLanguageImage.size()));
		mFrameLayout.addView(mLanguageImage.get((mCurrent + 3) % mLanguageImage.size()));
		mLanguageImage.get((mCurrent + 3) % mLanguageImage.size()).setX(mScreenWidth * 4 / 8);
		
		ObjectAnimator animX = ObjectAnimator.ofFloat(mLanguageImage.
				get((mCurrent + 3) % mLanguageImage.size()), "translationX", mScreenWidth * 3 / 8);
		animX.setDuration(2000);
		animX.start();

		return false;
	}
	
	public boolean onKeyDpadRight() {
		return false;
	}
	
	public boolean onKeyEnter() {
		VoDMainMenuView mainMenuView = new VoDMainMenuView();
		mainMenuView.init(context, null);
		
		VoDViewManager.getInstance().pushForegroundView(mainMenuView);
		return true;
	}
	
	public boolean onKeyBack() {
		return false;
	}

}
