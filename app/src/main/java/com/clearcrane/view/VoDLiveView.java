/**
 * @author xujifu
 * @copyright clear
 * @date 2014-06-20
 * @description 直播界面
 */
package com.clearcrane.view;

import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.constant.clearKey;
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

public class VoDLiveView extends VoDBaseView {
	
	public final static String TAG = "VoDLiveView";
	protected static final int MEDIA_ERROR_MALFORMED = -1007;
	protected static final int MEDIA_ERROR_IO = -1004;
	protected static final int MEDIA_ERROR_TIMED_OUT = -100;
	protected static final int MEDIA_ERROR_UNKNOWN = -110;
	protected static final int MEDIA_ERROR_UNSUPPORTED = -1010;
	
	private FrameLayout mFrameLayout;
	private ImageView liveViewPic, liveViewBK;
	private TextView liveViewTitle;
	private TextView liveChannelNum;
	private TextView tvInsertNum;
	private LinearLayout mLiveViewTitle;
	private LinearLayout mInsertLayout;//台号
	private LinearLayout mPrometLayout;//台号不存在
	private LinearLayout channelLayout;//频道列表
	private LinearLayout channelCommingLayout;//节目预告
	private ArrayList<ChannelInfo> channel = new ArrayList<ChannelInfo>();
	private long lastTime = 0;
	private int curFocusIndex = -1;	
	private int curChannelNum = 0;
	private int insertFocusIndex = -2;	
	private Handler handler;
	private int page = 0;
	private int position = 0;
	private int channelChangeDelay = 5;
	private boolean channelIndexStartWithZero = false;//判断频道号从0还是1开始
	
	
	private boolean channelListShowed = false; 
	
	
	
	private String ChannelJson;
	public Calendar begin = null;
	
	class ChannelInfo{
		String Name = null;
		String NameEng = null;
		String Num = null;
		String picUrl = null;
		ArrayList<String> src = new ArrayList<String>();
		FrameLayout layout;
	}
	
//	private ClearVideoView videoView;
	/* data provider */
	/* build the view layout/element */
	/* start animation */
	public void init(Context ctx, String u) {
		begin = Calendar.getInstance();
		super.init(ctx, u);
		type = "live";
		url = u;
		view = LayoutInflater.from(context).inflate(R.layout.live_view, null);
		initLayoutInXml();
		/* trigger to get data sources */
		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(ChannelJsonListen);
		mr.execute(url);
	}
	
	/* data provider */
	/* build the view layout/element */
	/* start animation */
	public void init(Context ctx, String u, LinearLayout layout) {
		begin = Calendar.getInstance();
		super.init(ctx, u, layout);
		type = "live";
		url = u;
		view = LayoutInflater.from(context).inflate(R.layout.live_view, null);
		initLayoutInXml();
		Log.i("XYL1234","123");
		/* trigger to get data sources */
		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(ChannelJsonListen);
		mr.execute(url);
	}
	
	

	
	private OnCompleteListener ChannelJsonListen = new OnCompleteListener() {
		
		@Override
		public void onDownloaded(Object result) {
			// TODO Auto-generated method stub
			ChannelJson = (String)result;
			if(ChannelJson == null){
				TipDialog.Builder builder = new TipDialog.Builder(context);
				builder.setMessage("Network is not available, please check the network!");  
		        builder.setTitle("TIP");  
		        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {  
		            public void onClick(DialogInterface dialog, int which) {  
		                dialog.dismiss();  
		                //设置你的操作事项  
		            }  
		        });  
		  
		        builder.create().show();  
				return;
			}
			try{
				JSONTokener jsonParser = new JSONTokener(ChannelJson);  
			    JSONObject mainViewObj = (JSONObject) jsonParser.nextValue();  
			    JSONArray contentArray = (JSONArray)mainViewObj.getJSONArray("Content");
			    for(int i = 0; i < contentArray.length(); i ++){
			    	JSONObject objecttmp = (JSONObject)contentArray.opt(i);
			    	ChannelInfo channelinfo = new ChannelInfo();
			    	channelinfo.Name = objecttmp.getString("ChannelName");
			    	channelinfo.NameEng = objecttmp.getString("ChannelNameEng");
			    	channelinfo.Num = objecttmp.getString("ChannelNum");
			    	channelinfo.picUrl = ClearConfig.getJsonUrl(context, objecttmp.getString("ChannelPic"));
			    	JSONArray secondArray = (JSONArray)objecttmp.getJSONArray("ChannelSrc");
			    	for(int j = 0; j < secondArray.length(); j ++){
			    		JSONObject secondtmp = (JSONObject)secondArray.opt(j);
			    		channelinfo.src.add( secondtmp.getString("Src"));
			    	}
			    	channelinfo.layout = (FrameLayout)LayoutInflater.from(context).inflate(R.layout.channel_item, null);
			    	TextView numTv = (TextView)channelinfo.layout.findViewById(R.id.channel_item_num);
			    	numTv.setText(channelinfo.Num);
			    	ImageView logoIv = (ImageView)channelinfo.layout.findViewById(R.id.channel_item_pic);
			    	MaterialRequest logoMR = new MaterialRequest(context, logoIv, ClearConfig.TYPE_IMAGE);
					logoMR.execute(channelinfo.picUrl);
					TextView nameTv = (TextView)channelinfo.layout.findViewById(R.id.channel_item_name);
					nameTv.setText(ClearConfig.getStringByLanguageId(channelinfo.Name, channelinfo.NameEng));
					channel.add(channelinfo);
					
					
			    }
			    if(channel.size() > 0){
			    	if(channel.get(0).Num.equalsIgnoreCase("0")){
			    		channelIndexStartWithZero = true;
			    	}
			    	changePage(0);
					onFocusChanged(-1, 0);
					curFocusIndex = 0;
				}
			    if(begin != null) {
                	long between = (Calendar.getInstance()).getTimeInMillis()-begin.getTimeInMillis(); 
                    ClearLog.LogInfo("BROSWER\tLoad\tSUCC\t" + between +"ms\t" + url + "\t"
                    		+ "liveView");
                } 
			}catch(JSONException e){
				ClearLog.LogError("BROSWER\tLoad\tFAIL\t0ms\t" + url);
				e.printStackTrace();
			}
		}
		
		@Override
		public void onComplete(boolean result) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private OnPreparedListener videoPreparedListener = new OnPreparedListener(){

		@Override
		public void onPrepared(MediaPlayer mp) {
			// TODO Auto-generated method stub
		}
	};
	
	private OnInfoListener videoInfoListener = new OnInfoListener(){

		@Override
		public boolean onInfo(MediaPlayer mp, int what, int extra) {
			// TODO Auto-generated method stub
			
			if(710 == what){
				mLiveViewTitle.animate()
					.alpha(0.0f)
					.scaleX(10.0f)
					.scaleY(10.0f)
					.setDuration(3000)
					.start();
				liveViewBK.animate()
					.alpha(0.0f)
					.setDuration(3000)
					.start();
				
			}
			return false;
		}
		
	};
	
	private OnErrorListener mErrorListener = new OnErrorListener() {
		
		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			// TODO Auto-generated method stub
			switch(extra){
			case MEDIA_ERROR_MALFORMED://-1007
				break;
			case MEDIA_ERROR_IO://-1004
				break;
			case MEDIA_ERROR_UNSUPPORTED://-1010
				break;
			case MEDIA_ERROR_TIMED_OUT://-100
				break;
			case MEDIA_ERROR_UNKNOWN://-110
			default:
				break;
			}
			return false;
		}
	};
	//频道列表展开线程
	private Runnable r = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			channelLayout.setPivotX(1.0f);
			channelLayout.setPivotY(1.0f);
			channelCommingLayout.setPivotX(ClearConfig.getScreenWidth());
			channelLayout.animate()
				.alpha(0.0f)
				.scaleY(0.0f)
				.start();
			channelCommingLayout.animate()
				.alpha(0.0f)
				.scaleX(0.0f)
				.start();
			channelListShowed = false;
		}
	};
	
	private Runnable channelChangeRunnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (insertFocusIndex < channel.size() && insertFocusIndex >=0){
				onFocusChanged(curFocusIndex, insertFocusIndex);
				curFocusIndex = insertFocusIndex;
				changePage2(insertFocusIndex);
			}else{
//				testanimate();
//				String toastStr = "频道"+(insertFocusIndex + 1)+"不存在";
//				Toast.makeText(context,toastStr , Toast.LENGTH_SHORT).show();
				showPromat();
			}
			insertFocusIndex = -2;
		}
	};
	
	private void showPromat(){
		mPrometLayout.setVisibility(View.VISIBLE);
		runDelay(hidePromatRunnable, ClearConstant.LIVE_PROMET_SHOW_LENGTH);
	}
	
	
	private Runnable checkPlayerIsPlaying = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.e("lilei","check check mp is " + VoDViewManager.getInstance().isVideoViewStatues());
			runDelay(checkPlayerIsPlaying, 8000);
		}
	};



	private Runnable hidePromatRunnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			mPrometLayout.setVisibility(View.GONE);
		}
	};
	
	private void showInsertChannelIndex()
	{
		//for huangting project
		if(channelIndexStartWithZero){
			tvInsertNum.setText("" + curChannelNum);
		}else{
			tvInsertNum.setText("" + (curChannelNum + 1));
		}
		mInsertLayout.setVisibility(View.VISIBLE);
		runDelay(hideInsertChannelIndexRunnable,ClearConstant.LIVE_INSERT_SHOW_TIME);
	}
	
	private void hideInsertChannelIndex(){
		mInsertLayout.setVisibility(View.GONE);
	}
	
	private Runnable hideInsertChannelIndexRunnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			hideInsertChannelIndex();
		}
	};
	
	private Runnable changeVideoSourceRunnable = new Runnable() {
		
		@Override
		public void run() {
			Log.i(TAG,"change channel:"+curChannelNum);
			// TODO Auto-generated method stub
			if(ClearConfig.checkNetwork(context) == ClearConfig.TYPE_LOCAL_STB)
				VoDViewManager.getInstance().startLiveVideo(ClearConfig.SERVER_URI_PREFIX + channel.get(curChannelNum).src.get(0));
			else
				VoDViewManager.getInstance().startLiveVideo(channel.get(curChannelNum).src.get(0));
		}
	};
	
	private void insertChannelIndex(int index){
		if(channelIndexStartWithZero){
			if (insertFocusIndex == -2){
				insertFocusIndex = index;
			}else if (insertFocusIndex < 100){
				insertFocusIndex = 10 * insertFocusIndex + index ;
			}else if(insertFocusIndex > 100){
				insertFocusIndex = 10 * ((insertFocusIndex)%100) + index ;
			}
		}else{
			if (insertFocusIndex == -2){
				insertFocusIndex = index - 1;
			}else if (insertFocusIndex < 100){
				insertFocusIndex = 10 * (insertFocusIndex +1) + index -1;
			}else if(insertFocusIndex > 100){
				insertFocusIndex = 10 * ((insertFocusIndex + 1)%100) + index -1;
			}
		}
		curChannelNum = insertFocusIndex;
		Log.i(TAG,"change channel:"+curChannelNum);
		showInsertChannelIndex();
		runDelay(channelChangeRunnable,ClearConstant.LIVE_INSERT_OVERTIME);
	}
	



	
	private void initLayoutInXml() {
		VoDViewManager.getInstance().isInLiveView = true;
		// TODO Auto-generated method stub
		mFrameLayout = (FrameLayout)view.findViewById(R.id.liveview);
		mInsertLayout = (LinearLayout)view.findViewById(R.id.insert_index);
		tvInsertNum = (TextView)view.findViewById(R.id.tv_insert_num);
		mLiveViewTitle = (LinearLayout)view.findViewById(R.id.live_title);
		liveViewPic = (ImageView)view.findViewById(R.id.live_title_pic);
		liveViewTitle = (TextView)view.findViewById(R.id.live_title_name);
		liveChannelNum = (TextView)view.findViewById(R.id.channellist_title);
		liveViewBK = (ImageView)view.findViewById(R.id.liveview_bk);
		channelLayout = (LinearLayout)view.findViewById(R.id.channellist);
		channelCommingLayout = (LinearLayout)view.findViewById(R.id.channel_coming);
		mPrometLayout = (LinearLayout)view.findViewById(R.id.ll_prompt);
		
		VoDViewManager.getInstance().setLiveVideoDisplayArea(0, 0, 
				ClearConfig.getScreenWidth(), ClearConfig.getScreenHeight());
		VoDViewManager.getInstance().showLiveVideo();

		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				if(msg.what == ClearConstant.JUMP_LIVE_VIEW){
					Log.i(TAG, "VoDLiveView handleMessage");
						curChannelNum = 0;
						runDelay(changeVideoSourceRunnable,channelChangeDelay);
				}
			}
		};
		handler.postDelayed(r, 5000);
		/*ImageView iconView = ((ImageView)menuLinearLayout.
				findViewById(R.id.pic_text_wrapcontent_pic));
		iconView.setDrawingCacheEnabled(true);  
		liveViewPic.setImageBitmap(
				Bitmap.createBitmap(iconView.getDrawingCache()));
		iconView.setDrawingCacheEnabled(false); 
		liveViewTitle.setText(((TextView)menuLinearLayout.
				findViewById(R.id.pic_text_wrapcontent_text)).getText());*/
		
		
		liveViewPic.setScaleX(2.0f);
		liveViewPic.setScaleY(2.0f);
		
		mLiveViewTitle.animate()
			.scaleX(3.0f)
			.scaleY(3.0f)
			.alpha(0.0f)
			.setStartDelay(2000)
			.setDuration(300)
			.start();
		liveViewBK.animate()
			.scaleX(3.0f)
			.scaleY(3.0f)
			.alpha(0.0f)
			.setStartDelay(2000)
			.setDuration(300)
			.start();
		channelListShowed = true;
		//mFrameLayout.addView(menuLinearLayout);
	}

	public void changePage(int num){
		int tempPage = -1; 
		if(num < 0){
			tempPage = (page + num + (channel.size() / 9 + 1)) % (channel.size() / 9 + 1) ;
		}else{
			tempPage = (page + num) % (channel.size() / 9 + 1) ;
		}
		//对于channel频道号小于9个时的情况作规避
		if(channel.size() <= 9){
			for(int i = 0; i < channel.size(); i ++){
				channelLayout.removeView(channel.get(i).layout);
			}
			for(int i = 0; i < channel.size(); i++){
				channelLayout.addView(channel.get(i).layout);
			}	
		}
		else{
		for(int i = page * 9; i < (page + 1) * 9 && i < channel.size(); i ++){
			channelLayout.removeView(channel.get(i).layout);
		}
		Log.i("XYL1234","tempPage:"+tempPage+" channel.size():"+channel.size());
		for(int i = tempPage * 9; i < (tempPage + 1) * 9 && i < channel.size(); i++){
			channelLayout.addView(channel.get(i).layout);
		}
		}
		page = tempPage;
		
	}
	
	public void changePage2(int chennleIndex){
		int pageIndex = chennleIndex / 9;
		int tempPage = pageIndex;
		for(int i = page * 9; i < (page + 1) * 9 && i < channel.size(); i ++){
			channelLayout.removeView(channel.get(i).layout);
		}
		for(int i = tempPage * 9; i < (tempPage + 1) * 9 && i < channel.size(); i++){
			channelLayout.addView(channel.get(i).layout);
		}
		page = tempPage;
		position =  chennleIndex % 9;
	}
	
	
	
	private boolean isQuickClick(long now){
		boolean value = false;
		channelChangeDelay = 5;
		if ((now - lastTime) < ClearConstant.LIVE_QUICK_INSERT_TIME){
			value = true;
		}
		lastTime = now;
		return value;
	}
	
	/* change channel */
	public boolean onKeyDpadUp(){
		long last = System.currentTimeMillis();
		handler.removeCallbacks(r);
		handler.postDelayed(r, 5000);
		if(last - lastTime < 150){
			return true;
		}
		if (isQuickClick(last)){
			channelChangeDelay = ClearConstant.LIVE_QUICK_INSERT_DELAY;
		}
		lastTime = last;
//		if(0 == curFocusIndex){
//			return true;
//		}
		int newFocusIndex = -1;
		if(curFocusIndex < 0){
			newFocusIndex = 0;
		}else{
			newFocusIndex = (curFocusIndex + channel.size() - 1) % channel.size();
		}
		position --;
		if(position < 0){
			changePage(-1);
			position = newFocusIndex - page * 9;
		}
		curChannelNum = newFocusIndex;
		showInsertChannelIndex();
		onFocusChanged(curFocusIndex, newFocusIndex);
//		channelChangeDelay = 5;
		curFocusIndex = newFocusIndex;
		return true;
	}

	public boolean onKeyDpadDown(){
		long last = System.currentTimeMillis();
		handler.removeCallbacks(r);
		handler.postDelayed(r, 5000);
		if(last - lastTime < 150){
			return true;
		}
		if (isQuickClick(last)){
			channelChangeDelay = ClearConstant.LIVE_QUICK_INSERT_DELAY;
		}
		lastTime = last;
//		if((channel.size() - 1) == curFocusIndex)
//			return true;
		int newFocusIndex = -1;
		if(curFocusIndex < 0){
			newFocusIndex = 0;
		}else{
			newFocusIndex = (curFocusIndex + 1) % channel.size();
		}
		position ++;
		if(position > 8 || curFocusIndex == (channel.size() - 1)){
			changePage(1);
			position = 0;
		}
		curChannelNum = newFocusIndex;
		showInsertChannelIndex();
		onFocusChanged(curFocusIndex, newFocusIndex);
//		channelChangeDelay = 5;
		curFocusIndex = newFocusIndex;
		return true;
	}
	
	private void onFocusChanged(int oldIndex, int newIndex) {
		// TODO Auto-generated method stub
		handler.removeCallbacks(r);
		handler.postDelayed(r, 5000);
		if(oldIndex >= 0){
//			channel.get(oldIndex).layout.animate()
//				.alpha(0.6f)
//				.scaleX(1.0f)
//				.scaleY(1.0f)
//				.start();
			((ImageView)channel.get(oldIndex).layout.findViewById(R.id.channel_item_focus)).setVisibility(View.INVISIBLE);
		}
//		channel.get(newIndex).layout.animate()
//			.alpha(1.0f)
//			.scaleX(1.2f)
//			.scaleY(1.2f)
//			.start();
		if(channel.size() != 0){
		((ImageView)channel.get(newIndex).layout.findViewById(R.id.channel_item_focus)).setVisibility(View.VISIBLE);
		liveChannelNum.setText(
				ClearConfig.getStringByLanguageId(context.getString(R.string.channellist_num), context.getString(R.string.channellist_num_eng))
				+ "(" + (newIndex + 1) + "/" + channel.size() + ")");
		
		showInsertChannelIndex();
		runDelay(changeVideoSourceRunnable,channelChangeDelay);
		
		//************************
		runDelay(checkPlayerIsPlaying,8000);
		
		
//		channelChangeDelay = 5;
//		if(ClearConfig.checkNetwork(context) == ClearConfig.TYPE_LOCAL_STB)
//			VoDViewManager.getInstance().startBackgroundVideo(ClearConfig.getTFCard() + channel.get(newIndex).src.get(0));
//		else
//			VoDViewManager.getInstance().startLiveVideo(channel.get(newIndex).src.get(0));
		}
	}
	
	public boolean onKeyEnter(){
		if(!channelListShowed){
			Log.i(TAG,"enter cannel r");
			channelLayout.animate()
				.alpha(1.0f)
				.scaleY(1.0f)
				.start();
			channelCommingLayout.animate()
				.alpha(1.0f)
				.scaleX(1.0f)
				.start();
			handler.removeCallbacks(r);
			handler.postDelayed(r, 5000);
			channelListShowed = true;
		}else{
			Log.i(TAG,"enter post r");
			handler.removeCallbacks(r);
			handler.postDelayed(r, 0);
		}
		
		return true;
	}
	
	
	
	private void runDelay(Runnable runnable,int Delayedtime){
		handler.removeCallbacks(runnable);
		handler.postDelayed(runnable, Delayedtime);
		Log.e("lilei","delaytime = "+channelChangeDelay);
	}
	//*****************20150508----LILEI***********************
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		Log.d("key","live view keycode: "+keyCode);
		if(super.onKeyDown(keyCode, event)){
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_0){
			insertChannelIndex(0);
		} else if (keyCode == KeyEvent.KEYCODE_1){
			insertChannelIndex(1);
		}else if (keyCode == KeyEvent.KEYCODE_2){
			insertChannelIndex(2);
		}else if (keyCode == KeyEvent.KEYCODE_3){
			insertChannelIndex(3);
		}else if (keyCode == KeyEvent.KEYCODE_4){
			insertChannelIndex(4);
		}else if (keyCode == KeyEvent.KEYCODE_5){
			insertChannelIndex(5);
		}else if (keyCode == KeyEvent.KEYCODE_6){
			insertChannelIndex(6);
		}else if (keyCode == KeyEvent.KEYCODE_7){
			insertChannelIndex(7);
		}else if (keyCode == KeyEvent.KEYCODE_8){
			insertChannelIndex(8);
		}else if (keyCode == KeyEvent.KEYCODE_9){
			insertChannelIndex(9);
		}else if (keyCode == KeyEvent.KEYCODE_PAGE_UP || keyCode == 167){
			onKeyDpadDown();
		}else if (keyCode == KeyEvent.KEYCODE_PAGE_DOWN || keyCode == 166){
			onKeyDpadUp();
		}else if (keyCode == clearKey.HIMEDIA_TV_LIST){
			onKeyEnter();
		}else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
			onKeyDpadLeft();
		}else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
			onKeyDpadRight();
		}
		
		
		return super.onKeyDown(keyCode, event);
	}

	/*
	 * handle Letf input 
	 * page--
	 * 
	 */
	public boolean onKeyDpadLeft(){
		long last = System.currentTimeMillis();
		handler.removeCallbacks(r);
		handler.postDelayed(r, 5000);
		if(last - lastTime < 150){
			return true;
		}
		lastTime = last;
		int temp = curChannelNum;
		int size = channel.size();
		int offset = ((size - 1) / 9 +1) * 9;
//		if(channelIndexStartWithZero){
			
//		}else{
		if(size < 9 || size <= 0){
			return false;
		}
			if(curChannelNum < 0){
				return false;
			}else if(curChannelNum - 9 < 0 ){
					
					curChannelNum = offset + curChannelNum - 9;
					curChannelNum = curChannelNum > (size - 1)?(size -1):curChannelNum;
			}else{
				curChannelNum -= 9;
			}	
				insertFocusIndex = curChannelNum;
				showInsertChannelIndex();
				runDelay(channelChangeRunnable,50);
		
//		}
		return false;
	}
	
	
	/*
	 * handle Right input
	 * page ++
	 * 
	 */
	public boolean onKeyDpadRight(){
		long last = System.currentTimeMillis();
		handler.removeCallbacks(r);
		handler.postDelayed(r, 5000);
		if(last - lastTime < 150){
			return true;
		}
		lastTime = last;
		int size = channel.size();
		int offset = ((size - 1) / 9 + 1) * 9;
		if(size < 9 ){
			return false;
		}
		if ((curChannelNum + 9) >= offset){
			curChannelNum = curChannelNum + 9 - offset;
		} else if ((curChannelNum + 9 ) > (size - 1)){
			curChannelNum = size - 1;
		}else {
			curChannelNum += 9; 
		}
		
		insertFocusIndex = curChannelNum;
		showInsertChannelIndex();
		runDelay(channelChangeRunnable,50);
		return false;
	}
	
	
	
	public boolean onKeyBack() {
		Log.i("in key press","live on key back");
		//super.onKeyBack();
		Log.i("in key press","after live on key back");
		if(channelListShowed){
        	handler.removeCallbacks(r);
            handler.postDelayed(r, 0);
            channelListShowed = false;
            if(!VoDViewManager.getInstance().quickShow)//避免按首页键
            	return true;
        }
		
		//定制项目只有直播界面
		if(VoDViewManager.getInstance().onlyLive)
			return true;
		Log.i("in key press","hidelivevido");
		VoDViewManager.getInstance().hideLiveVideo();
		VoDViewManager.getInstance().popForegroundView();
		VoDViewManager.getInstance().showBackgroundVideo();
		VoDViewManager.getInstance().playBackgroundVideo();
		VoDViewManager.getInstance().isInLiveView = false;
		handler.removeCallbacks(checkPlayerIsPlaying);
		return true;

	}
	
	

	@Override
	public void show() {
		// TODO Auto-generated method stub
		super.show();
	}

	@Override
	public void back() {
		VoDViewManager.getInstance().showLiveVideo();
		handler.postDelayed(changeVideoSourceRunnable,500);
	}

	@Override
	public void hide() {
		VoDViewManager.getInstance().hideLiveVideo();
//		VoDViewManager.getInstance().popForegroundView();
//		VoDViewManager.getInstance().showBackgroundVideo();
//		VoDViewManager.getInstance().playBackgroundVideo();
		VoDViewManager.getInstance().isInLiveView = false;
		handler.removeCallbacks(checkPlayerIsPlaying);
	}

	//选中第一个频道
	public void selectFirstChannel(){
		if(channel.size() > 0){
			changePage(0);
			onFocusChanged(-1, 0);
			curFocusIndex = 0;
		}
	}
	
	public Handler getHandler() {
		return handler;
	}
}
