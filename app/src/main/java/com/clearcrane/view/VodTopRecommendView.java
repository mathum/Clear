/**
 * @author xujifu
 * @copyright clear
 * @date 2014-06-20
 * @description 点播推荐页面
 */
package com.clearcrane.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clearcrane.log.ClearLog;
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

public class VodTopRecommendView extends VoDBaseView {
	
	private ArrayList<MovieInfo> movie = new ArrayList<MovieInfo>();
	private String topRecommendJson;
	private LinearLayout mLinearLayout, mMoviePicLinearLayout;
	private ImageView topTitlePic;
	private TextView topTitleName;
	private TextView movieName, movieActor, movieDirector, movieType, movieIntroduce;
	private ImageView baseLine, select, shuiwu_bg_pic;
	private RelativeLayout mRelativeLayout;
	public String TAG = "vodtop";
	
	private int curFocusIndex = -1;
	private long lastTime = 0;
	
	private Animation item_anim, title_anim, pic_anim;
	public Calendar begin = null;
	
	private boolean isLoaded = false;//是否加载完毕，如果动画未能加载完毕，就进行按键会导致布局混乱
	private final int checkNum = 8;
	private boolean movieAdded = false;
	
	public DisplayImageOptions options = new DisplayImageOptions.Builder()  
								.cacheInMemory(false)  
							    .cacheOnDisk(true)  
							    .bitmapConfig(Bitmap.Config.RGB_565)
							    .imageScaleType(ImageScaleType.NONE)
							    .build();

	class MovieInfo{
		String name;
		String nameEng;
		String id;
		String picurl;
		String address;
		String score;
		String duration;
		String actor;
		String actorEng;
		String director;
		String directorEng;
		String introduce;
		String introduceEng;
		
		boolean isDownload = false;
		LinearLayout picLayout;
	}
	/* data provider */
	/* build the view layout/element */
	/* start animation */
	public void init(Context ctx, String u){
		begin = Calendar.getInstance();
		super.init(ctx, u);
		url = u;
		view = LayoutInflater.from(context).inflate(R.layout.top_recommend_view, null);
		initLayoutInXml();
		/* trigger to get data sources */
		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(TopRecommendJsonListen);
		mr.execute(url);
	}

	/* data provider */
	/* build the view layout/element */
	/* start animation */
	public void init(Context ctx, String u, LinearLayout layout) {
		Log.i("toprecommend","init");
		begin = Calendar.getInstance();
		super.init(ctx, u, layout);
		url = u;
		view = LayoutInflater.from(context).inflate(R.layout.top_recommend_view, null);
		initLayoutInXml();
		/* trigger to get data sources */
		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(TopRecommendJsonListen);
		mr.execute(url);
	}
	
	
	
	/* has to set text after image download completed */
	class OnTopPicCompleteListerner implements OnCompleteListener {
		MovieInfo movieinfo;
		
		public OnTopPicCompleteListerner(MovieInfo info) {
			movieinfo = info;
		}

		@Override
		public void onDownloaded(Object result) {
		}

		@Override
		public void onComplete(boolean result) {
			movieinfo.isDownload = true;
			/* check if all image downloaded, if so, show it */
			boolean allDownloaded = true;
			for(int i = 0; i < movie.size() && i < 2; i++) {
				if(movie.get(i).isDownload == false) {
					allDownloaded = false;
					break;
				}
			}
			
			if(allDownloaded) {
				movieName.setVisibility(View.VISIBLE);
				movieActor.setVisibility(View.VISIBLE);
				movieDirector.setVisibility(View.VISIBLE);
				movieType.setVisibility(View.VISIBLE);
				movieIntroduce.setVisibility(View.VISIBLE);
				baseLine.setVisibility(View.VISIBLE);
				select.setVisibility(View.VISIBLE);
				
				baseLine.animate()
					.scaleX(1.0f)
					.setDuration(300)
					.start();
				movieName.startAnimation(item_anim);
				movieActor.startAnimation(item_anim);
				movieDirector.startAnimation(item_anim);
				movieType.startAnimation(item_anim);
				movieIntroduce.startAnimation(item_anim);
				mMoviePicLinearLayout.setVisibility(View.VISIBLE);
				for(int i = 0; i < movie.size(); i++){
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT, 
							LinearLayout.LayoutParams.WRAP_CONTENT);
					mMoviePicLinearLayout.addView(movie.get(i).picLayout, lp);
					TranslateAnimation transanim = new TranslateAnimation(-10, 0, 10, 0);
					AlphaAnimation alphaanim = new AlphaAnimation(0.0f, 1.0f);
					AnimationSet set = new AnimationSet(true);
					set.addAnimation(transanim);
					set.addAnimation(alphaanim);
					//set.addAnimation(scaleanim);
					set.setDuration(300);
					set.setStartOffset(100 * i);
					movie.get(i).picLayout.startAnimation(set);
				}
				
				shuiwu_bg_pic.setVisibility(View.VISIBLE);
				
				isLoaded = true;
			}
			
		}
	}
	
	 class onImageLoadListener implements ImageLoadingListener {
		MovieInfo movieinfo;
		
		public onImageLoadListener(MovieInfo info) {
			movieinfo = info;
		}

		@Override
		public void onLoadingStarted(String imageUri, View view) {
			// TODO Auto-generated method stub
			Log.i(TAG,"START");
			movieinfo.isDownload = false;
		}

		@Override
		public void onLoadingFailed(String imageUri, View view,
				FailReason failReason) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			// TODO Auto-generated method stub
			if(movie.size() >= checkNum){
	    		movieAdded = true;
	    	}
			final ImageView iv = (ImageView)movieinfo.picLayout.findViewById(R.id.top_movie_item_pic);
			iv.setImageBitmap(loadedImage);
			movieinfo.isDownload = true;
			/* check if all image downloaded, if so, show it */
			boolean allDownloaded = true;
			Log.i(TAG,"COMPLETE,movie size:" + movie.size());
			for(int i = 0; i < movie.size() && i < checkNum; i++) {
				if(movie.get(i).isDownload == false) {
					allDownloaded = false;
					break;
				}
			}
			
			if(allDownloaded && movieAdded) {
				Log.i(TAG,"all cownloaded");
				movieName.setVisibility(View.VISIBLE);
				movieActor.setVisibility(View.VISIBLE);
				movieDirector.setVisibility(View.VISIBLE);
				movieType.setVisibility(View.VISIBLE);
				movieIntroduce.setVisibility(View.VISIBLE);
				baseLine.setVisibility(View.VISIBLE);
				select.setVisibility(View.VISIBLE);
				
				baseLine.animate()
					.scaleX(1.0f)
					.setDuration(300)
					.start();
				movieName.startAnimation(item_anim);
				movieActor.startAnimation(item_anim);
				movieDirector.startAnimation(item_anim);
				movieType.startAnimation(item_anim);
				movieIntroduce.startAnimation(item_anim);
				mMoviePicLinearLayout.setVisibility(View.VISIBLE);
				for(int i = 0; i < movie.size(); i++){
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT, 
							LinearLayout.LayoutParams.WRAP_CONTENT);
					mMoviePicLinearLayout.addView(movie.get(i).picLayout, lp);
					TranslateAnimation transanim = new TranslateAnimation(-10, 0, 10, 0);
					AlphaAnimation alphaanim = new AlphaAnimation(0.0f, 1.0f);
					AnimationSet set = new AnimationSet(true);
					set.addAnimation(transanim);
					set.addAnimation(alphaanim);
					//set.addAnimation(scaleanim);
					set.setDuration(300);
					set.setStartOffset(100 * i);
					movie.get(i).picLayout.startAnimation(set);
				}
				
				shuiwu_bg_pic.setVisibility(View.VISIBLE);
				
				isLoaded = true;
			}
		}

		@Override
		public void onLoadingCancelled(String imageUri, View view) {
			// TODO Auto-generated method stub
			
		}
	}
	
	
	
	private OnCompleteListener TopRecommendJsonListen = new OnCompleteListener(){

		@Override
		public void onDownloaded(Object result) {
			// TODO Auto-generated method stub
			topRecommendJson = (String)result;
			if(topRecommendJson == null){
				TipDialog.Builder builder = new TipDialog.Builder(context);
				builder.setMessage("当前页面出错");  
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
				JSONTokener jsonParser = new JSONTokener(topRecommendJson);  
			    JSONObject mainViewObj = (JSONObject) jsonParser.nextValue();  
			    JSONArray contentArray = (JSONArray)mainViewObj.getJSONArray("Content");
			    for(int i = 0; i < contentArray.length(); i ++){
			    	JSONObject objecttmp = (JSONObject)contentArray.opt(i);
			    	MovieInfo movieinfo = new MovieInfo();
			    	movieinfo.name = objecttmp.getString("Name");
			    	movieinfo.nameEng = objecttmp.getString("NameEng");
			    	movieinfo.picurl = ClearConfig.getJsonUrl(context, objecttmp.getString("Picurl"));
			    	movieinfo.address = ClearConfig.getJsonUrl(context, objecttmp.getString("Address"));
			    	movieinfo.score = objecttmp.getString("Score");
			    	movieinfo.duration = objecttmp.getString("Duration");
			    	movieinfo.actor = objecttmp.getString("Actor");
			    	movieinfo.actorEng = objecttmp.getString("ActorEng");
			    	movieinfo.director = objecttmp.getString("Director");
			    	movieinfo.directorEng = objecttmp.getString("DirectorEng");
			    	movieinfo.id = objecttmp.getString("id");
			    	movieinfo.introduce = objecttmp.getString("Introduce");
			    	movieinfo.introduceEng = objecttmp.getString("IntroduceEng");
			    	movieinfo.picLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.top_movie_item, null);
			    	/*MaterialRequest mr = new MaterialRequest(context, iv, ClearConfig.TYPE_IMAGE);
			    	if(i < 2)
			    		mr.setOnCompleteListener(new OnTopPicCompleteListerner(movieinfo));
					mr.execute(movieinfo.picurl);*/
			    	if(!movieinfo.picurl.startsWith("http"))
			    		movieinfo.picurl = Scheme.FILE.wrap(movieinfo.picurl);
			    	if(i < checkNum){
			    		ImageLoader.getInstance().loadImage(movieinfo.picurl, options, new onImageLoadListener(movieinfo));
			    	}else{
			    		ImageView iv = (ImageView)movieinfo.picLayout.findViewById(R.id.top_movie_item_pic);
			    		ImageLoader.getInstance().displayImage(movieinfo.picurl, iv, options);
			    	}
			    		
					ImageView ivborder = (ImageView)movieinfo.picLayout.findViewById(R.id.top_movie_item_border);
					ivborder.setImageResource(R.drawable.nigeria_movie);
					ivborder.setAlpha(0.0f);
			    	movie.add(movieinfo);
			    	
			    }
			    movieAdded = true;
			    if(!movie.isEmpty()){
				    //movieName.setText(movie.get(0).name  + "(" + movie.get(0).score + ")");
			    	movieName.setText(movie.get(0).name);
					movieActor.setText(context.getString(R.string.actor) + " : " + movie.get(0).actor);
					movieDirector.setText(context.getString(R.string.director) + " : " + movie.get(0).director);
					//movieType.setText(context.getString(R.string.type) + " : " + movie.get(0).id + "  " +
							//context.getString(R.string.duration) + " : " + movie.get(0).duration);
					movieType.setText(context.getString(R.string.duration) + " : " + movie.get(0).duration);
			
					movieIntroduce.setText(context.getString(R.string.introduce) + " : " + movie.get(0).introduce);
					((ImageView)movie.get(0).picLayout.findViewById(R.id.top_movie_item_border)).setAlpha(1.0f);
					((ImageView)movie.get(0).picLayout.findViewById(R.id.top_movie_item_background)).setAlpha(0.0f);
					
					if(curFocusIndex < 0){
						//first focus
						onFocusChanged(-1, 0);
						curFocusIndex = 0;
					}
					
			    }
			    if(begin != null) {
                	long between = (Calendar.getInstance()).getTimeInMillis()-begin.getTimeInMillis(); 
                    ClearLog.LogInfo("BROSWER\tLoad\tSUCC\t" + between +"ms\t" + url + "\t"
                    		+ "topRecommendView");
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
		mRelativeLayout = (RelativeLayout)view.findViewById(R.id.top_recommend_title);
		mLinearLayout = (LinearLayout)view.findViewById(R.id.top_title_left);
		mMoviePicLinearLayout = (LinearLayout)view.findViewById(R.id.top_recommend_movie_pic);
		topTitlePic = (ImageView)view.findViewById(R.id.top_title_pic);
		topTitleName = (TextView)view.findViewById(R.id.top_title_name);
		movieName = (TextView)view.findViewById(R.id.top_title_moviename);
		movieActor = (TextView)view.findViewById(R.id.top_recommend_movie_actor);
		movieDirector = (TextView)view.findViewById(R.id.top_recommend_movie_director);
		movieType = (TextView)view.findViewById(R.id.top_recommend_movie_type);
		movieIntroduce = (TextView)view.findViewById(R.id.top_recommend_movie_introduce);
		baseLine = (ImageView)view.findViewById(R.id.top_recommend_baseline);
		select = (ImageView)view.findViewById(R.id.top_recommend_select);
		if(ClearConfig.LanguageID == 1){
			select.setImageResource(R.drawable.top_recommend_command);
		}else{
			select.setImageResource(R.drawable.top_recommend_command_eng);
		}
		shuiwu_bg_pic = (ImageView)view.findViewById(R.id.shuiwu_bg_pic);
		
		item_anim = AnimationUtils.loadAnimation(context, R.anim.top_recommend_item_anim);
		title_anim = AnimationUtils.loadAnimation(context, R.anim.top_recommend_title_anim);
		pic_anim = AnimationUtils.loadAnimation(context, R.anim.top_recommend_pic_anim);
		
		
		/*ImageView iconView = ((ImageView)menuLinearLayout.
				findViewById(R.id.sub_pic_text_wrapcontent_pic));
		iconView.setDrawingCacheEnabled(true);  
		topTitlePic.setImageBitmap(
				Bitmap.createBitmap(iconView.getDrawingCache()));
		topTitlePic.setX(iconView.getX());
		topTitlePic.setY(iconView.getY());
		iconView.setDrawingCacheEnabled(false);
		topTitleName.setText(((TextView)menuLinearLayout.
				findViewById(R.id.sub_pic_text_wrapcontent_text)).getText());*/
		
		mMoviePicLinearLayout.setVisibility(View.GONE);
		movieName.setVisibility(View.GONE);
		movieActor.setVisibility(View.GONE);
		movieDirector.setVisibility(View.GONE);
		movieType.setVisibility(View.GONE);
		movieIntroduce.setVisibility(View.GONE);
		baseLine.setVisibility(View.GONE);
		select.setVisibility(View.GONE);
		mRelativeLayout.startAnimation(title_anim);
	}
	
	private void onFocusChanged(int oldIndex, int newIndex) {
		// TODO Auto-generated method stub
		if(oldIndex >= 0){
			((ImageView)movie.get(oldIndex).picLayout.findViewById(R.id.top_movie_item_border))
				.animate()
				.alpha(0.0f)
				.setDuration(200)
				.start();
			((ImageView)movie.get(oldIndex).picLayout.findViewById(R.id.top_movie_item_background))
				.animate()
				.alpha(1.0f)
				.setDuration(200)
				.start();;
		}
		movieName.setText(
				ClearConfig.getStringByLanguageId(movie.get(newIndex).name, movie.get(newIndex).nameEng));
		
		movieActor.setText(
				ClearConfig.getStringByLanguageId(context.getString(R.string.actor), context.getString(R.string.actor_eng)) + " : " +
				ClearConfig.getStringByLanguageId(movie.get(newIndex).actor, movie.get(newIndex).actorEng));
		
		movieDirector.setText(
				ClearConfig.getStringByLanguageId(context.getString(R.string.director), context.getString(R.string.director_eng)) + " : " +
				ClearConfig.getStringByLanguageId(movie.get(newIndex).director, movie.get(newIndex).directorEng));
		/*
		movieType.setText(
				ClearConfig.getStringByLanguageId(context.getString(R.string.type), context.getString(R.string.type_eng)) 
				+ " : " + movie.get(newIndex).id + "  " +
				ClearConfig.getStringByLanguageId(context.getString(R.string.duration), context.getString(R.string.duration_eng)) 
				+ " : " + movie.get(newIndex).duration);*/
		movieType.setText(
				ClearConfig.getStringByLanguageId(context.getString(R.string.duration), context.getString(R.string.duration_eng))
				+ " : " + movie.get(newIndex).duration);
		
		
		movieIntroduce.setText(
				ClearConfig.getStringByLanguageId(context.getString(R.string.introduce), context.getString(R.string.introduce_eng)) + " : " +
				ClearConfig.getStringByLanguageId(movie.get(newIndex).introduce, movie.get(newIndex).introduceEng));
		
		movieName.startAnimation(item_anim);
		movieActor.startAnimation(item_anim);
		movieDirector.startAnimation(item_anim);
		movieType.startAnimation(item_anim);
		movieIntroduce.startAnimation(item_anim);
		
		((ImageView)movie.get(newIndex).picLayout.findViewById(R.id.top_movie_item_border))
			.animate()
			.alpha(1.0f)
			.setDuration(200)
			.start();
		((ImageView)movie.get(newIndex).picLayout.findViewById(R.id.top_movie_item_background))
			.animate()
			.alpha(0.0f)
			.setDuration(200)
			.start();;
	}
	
	public boolean onKeyDpadLeft(){
		long last = System.currentTimeMillis();
		if(last - lastTime < 300){
			return true;
		}
		lastTime = last;
		if(isLoaded == false){
			return true;
		}
		if(0 == curFocusIndex){
			return true;
		}
		int newFocusIndex = -1;
		if(curFocusIndex < 0){
			newFocusIndex = 0;
		}else{
			if(movie.size() > 1)
				mMoviePicLinearLayout.animate()
					.translationXBy(movie.get((1) % movie.size()).picLayout.getX()
							- movie.get(0).picLayout.getX())
					.setDuration(300)
					.start();
			newFocusIndex = (curFocusIndex + movie.size() - 1) % movie.size();
		}
		
		onFocusChanged(curFocusIndex, newFocusIndex);
		curFocusIndex = newFocusIndex;
		return true;
	}

	public boolean onKeyDpadRight(){
		long last = System.currentTimeMillis();
		if(last - lastTime < 300){
			return true;
		}
		lastTime = last;
		if(isLoaded == false){
			return true;
		}
		if((movie.size() - 1) == curFocusIndex)
			return true;
		int newFocusIndex = -1;
		if(curFocusIndex < 0){
			newFocusIndex = 0;
		}else{
			if(movie.size() > 1)
				mMoviePicLinearLayout.animate()
					.translationXBy(movie.get(0).picLayout.getX()
							- movie.get((1) % movie.size()).picLayout.getX())
					.setDuration(300)
					.start();
			newFocusIndex = (curFocusIndex + 1) % movie.size();
		}
		onFocusChanged(curFocusIndex, newFocusIndex);
		curFocusIndex = newFocusIndex;
		return true;
	}
	
	public boolean onKeyEnter(){
		if(isLoaded == false){
			return true;
		}
		if(!movie.isEmpty() || curFocusIndex >= 0){
			VoDViewManager.getInstance().stopBackgroundVideo();
			VoDViewManager.getInstance().hideBackgroundVideo();
			VoDMovieView movieView = new VoDMovieView();
			movieView.init(context, movie.get(curFocusIndex).address, 
					ClearConfig.getStringByLanguageId(movie.get(curFocusIndex).name, movie.get(curFocusIndex).nameEng));
			VoDViewManager.getInstance().pushForegroundView(movieView);
		}
		return false;
	}
	
//	public boolean onKeyBack(){
//		return true;
//	}
	@Override
	public void back(){
		/* TODO, FIXME, use the real background video url */
		VoDViewManager.getInstance().showBackgroundVideo();
		VoDViewManager.getInstance().setVideoPreparedListener(null);
		VoDViewManager.getInstance().playBackgroundVideo();
	}
}
