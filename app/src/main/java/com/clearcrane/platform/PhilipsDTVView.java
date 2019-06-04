package com.clearcrane.platform;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clearcrane.activity.VoDActivity;
import com.clearcrane.constant.ClearConstant;
import com.clearcrane.log.ClearLog;
import com.clearcrane.provider.MaterialRequest;
import com.clearcrane.provider.MaterialRequest.OnCompleteListener;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.TipDialog;
import com.clearcrane.view.VoDBaseView;
import com.clearcrane.view.VoDViewManager;
import com.clearcrane.vod.R;
import com.operationservice.DTVPlay;
import com.operationservice.MyListener;
import com.operationservice.ProgramFavoriteObject;
import com.operationservice.Source;
import com.operationservice.Unit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PhilipsDTVView extends VoDBaseView{
    
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
    private ArrayList<ChannelInfo> dtvChannel = new ArrayList<ChannelInfo>();
    private int dtvnum = 0;
    private long lastTime = 0;
    private int curFocusIndex = -1; 
    private int curChannelNum = 0;
    private int insertFocusIndex = -2;
    private Handler handler;
    private int page = 0;
    private int position = 0;
    private int channelChangeDelay = 5;
    private String TAG="PhilipsDTVView";
    private Unit mUnit;
    private DTVPlay mDTVPlay ;
    private int sourceType = 0;//默认直播信号
    private List<ProgramFavoriteObject> mList;
    public MyListener myListener ; 
    //private InitView mInitView ;
    public RelativeLayout surfaceViewLayout ;
    public SurfaceView surfaceView ;
    private VoDActivity iptv = null;
    private int videoW = ClearConfig.getScreenWidth();
    private int videoH = ClearConfig.getVideoHeight();
    
    private boolean channelListShowed = false; 
    private boolean channelIndexStartWithZero = false;//判断频道号从0还是1开始
    private boolean DTVInitFlag = false;//接口初始化完成标志，没有完成前屏蔽遥控器按键
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
    
    public  Handler mHandler = new Handler() {  
		public void handleMessage (Message msg) {//�˷�����ui�߳�����  
			switch(msg.what) {  
			case 1 :
				Log.i(TAG, "unit init");
				mUnit.init() ;
				surfaceViewLayout = (RelativeLayout)view.findViewById(R.id.tv_surfaceview_layout);
				surfaceView = new SurfaceView(context.getApplicationContext());
				surfaceView.setLayoutParams(new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				surfaceViewLayout.addView(surfaceView) ;
				mDTVPlay = new DTVPlay(mUnit ,surfaceView) ;
				mDTVPlay.init() ;
				Log.i(TAG,"dtvplayrsize:"+mDTVPlay.getListInfo().size());
				getchannelList();
				break ;
			case 2:
				DTVInitFlag = true;
				break;
			
			case 3:
				VoDViewManager.getInstance().hideLiveVideo();
		        VoDViewManager.getInstance().popForegroundView();
		        VoDViewManager.getInstance().showBackgroundVideo();
		        VoDViewManager.getInstance().playBackgroundVideo();
		        Log.i(TAG,"DTV　back");
			}

		}
	};
	
	private OnCompleteListener ChannelJsonListen = new OnCompleteListener() {
		
		@Override
		public void onDownloaded(Object result) {
			// TODO Auto-generated method stub
			ChannelJson = (String)result;
			if(ChannelJson == null){
				TipDialog.Builder builder = new TipDialog.Builder(context);
				builder.setMessage("当前网络出错，请联系服务员！");  
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
			    if(channel.size() > 0 && dtvChannel.size() >0){
			    	if(channel.get(0).Num.equalsIgnoreCase("0")){
			    		channelIndexStartWithZero = true;
			    	}
			    	changePage(0);
					onFocusChanged(-1, 0);
					curFocusIndex = 0;
					Log.i(TAG, "channel json pare complete");
				}else{
					Log.e(TAG, "channel json pare no data");
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
	
	public void getchannelList(){
		Log.i(TAG, "get channel list");
		mList = mDTVPlay.getListInfo() ;
		Log.i(TAG, "dtvlist size:"+mList.size() + "json channel size:"+channel.size());
		if(mList!=null&&!"".equals(mList)&&mList.size()>0 && mUnit!=null){
            for(int i = 0; i < mList.size(); i++ ){
                ChannelInfo channelinfo = new ChannelInfo();
                channelinfo.Name = mList.get(i).getChannelName();
                channelinfo.Num = mList.get(i).getChannelId();
                Log.i(TAG, "name:" +"dtvlistseq:" + i +"id:"+channelinfo.Num);
                //初始化名称
                dtvChannel.add(channelinfo); 
            }
            if(channel.size() > 0 && dtvChannel.size() >0){
		    	if(channel.get(0).Num.equalsIgnoreCase("0")){
		    		channelIndexStartWithZero = true;
		    	}
		    	changePage(0);
				onFocusChanged(-1, 0);
				curFocusIndex = 0;
			}
		}
	}
    
//  private ClearVideoView videoView;
    /* data provider */
    /* build the view layout/element */
    /* start animation */
    public void init(Context ctx, String u) {
    	Log.i(TAG, "init");
        begin = Calendar.getInstance();
        super.init(ctx, u);
        iptv = (VoDActivity) ctx;
        
        url = u;
        view = LayoutInflater.from(context).inflate(R.layout.live_view, null);
        initLayoutInXml();
        
        MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(ChannelJsonListen);
		mr.execute(url);
    }
    
    /* data provider */
    /* build the view layout/element */
    /* start animation */
    public void init(Context ctx, String u, LinearLayout layout) {
    	init(ctx,u);
    }
    
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
//              testanimate();
//              String toastStr = "频道"+(insertFocusIndex + 1)+"不存在";
//              Toast.makeText(context,toastStr , Toast.LENGTH_SHORT).show();
                showPromat();
            }
            insertFocusIndex = -2;
        }
    };
    private void showPromat(){
        mPrometLayout.setVisibility(View.VISIBLE);
        runDelay(hidePromatRunnable, ClearConstant.LIVE_PROMET_SHOW_LENGTH);
    }
    
    private Runnable hidePromatRunnable = new Runnable() {
        
        @Override
        public void run() {
            // TODO Auto-generated method stub
            mPrometLayout.setVisibility(View.GONE);
        }
    };
    
    private void showInsertChannelIndex()
    {
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
            
            // TODO Auto-generated method stub
                try {
                	DTVInitFlag = false;//
                	dtvnum = Integer.parseInt(channel.get(curChannelNum).src.get(0)) -1 ;
                	Log.i(TAG,"chang video source,index:"+curChannelNum + "dtvnum:" + (dtvnum+1));
                	Log.i(TAG,"play :"+mList.get(dtvnum).getChannelName());
                	if(mUnit.getInputSource()!=Source.INPUT_SOURCE_DTV){
						mUnit.setSource(Source.INPUT_SOURCE_DTV) ;
					}
					mDTVPlay.play(mList.get(dtvnum).getServiceId(), mList.get(dtvnum).getFrequency(), mList.get(dtvnum).getSR(), mList.get(dtvnum).getModulation()) ;
					mHandler.sendEmptyMessageDelayed(2, 3000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
        showInsertChannelIndex();
        runDelay(channelChangeRunnable,ClearConstant.LIVE_INSERT_OVERTIME);
    }
    
    private void initLayoutInXml() {
        // TODO Auto-generated method stub
        //mFrameLayout = (FrameLayout)view.findViewById(R.id.liveview);
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
        
        handler = new Handler();
        handler.postDelayed(r, 5000);
        
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
        //bindReceiver() ;
        myListener = new MyListener() {
			@Override
			public void bindSuccess() {
				// TODO Auto-generated method stub
				Log.i(TAG, "bind service success");
				mHandler.obtainMessage(1).sendToTarget() ;
			}
		};
		mUnit = new Unit(context , myListener) ;
    }

    public void changePage(int num){
        int tempPage = -1; 
        if(num < 0){
            tempPage = (page + num + (channel.size() / 9 + 1)) % (channel.size() / 9 + 1) ;
        }else{
            tempPage = (page + num) % (channel.size() / 9 + 1) ;
        }
        for(int i = page * 9; i < (page + 1) * 9 && i < channel.size(); i ++){
            channelLayout.removeView(channel.get(i).layout);
        }
        for(int i = tempPage * 9; i < (tempPage + 1) * 9 && i < channel.size(); i++){
            channelLayout.addView(channel.get(i).layout);
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
        try {
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
//          if(0 == curFocusIndex){
//              return true;
//          }
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
//          channelChangeDelay = 5;
            curFocusIndex = newFocusIndex;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public boolean onKeyDpadDown(){
        
        try {
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
//          if((channel.size() - 1) == curFocusIndex)
//              return true;
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
//          channelChangeDelay = 5;
            curFocusIndex = newFocusIndex;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }
    
    private void onFocusChanged(int oldIndex, int newIndex) {
        // TODO Auto-generated method stub
    	Log.i(TAG,"focus changed ...newindex:"+newIndex);
        try {
            handler.removeCallbacks(r);
            handler.postDelayed(r, 5000);
            if(oldIndex >= 0){
//              channel.get(oldIndex).layout.animate()
//                  .alpha(0.6f)
//                  .scaleX(1.0f)
//                  .scaleY(1.0f)
//                  .start();
                ((ImageView)channel.get(oldIndex).layout.findViewById(R.id.channel_item_focus)).setVisibility(View.INVISIBLE);
            }
//          channel.get(newIndex).layout.animate()
//              .alpha(1.0f)
//              .scaleX(1.2f)
//              .scaleY(1.2f)
//              .start();
            ((ImageView)channel.get(newIndex).layout.findViewById(R.id.channel_item_focus)).setVisibility(View.VISIBLE);
            liveChannelNum.setText(
                    ClearConfig.getStringByLanguageId(context.getString(R.string.channellist_num), context.getString(R.string.channellist_num_eng))
                    + "(" + (newIndex + 1) + "/" + channel.size() + ")");
            
            showInsertChannelIndex();
            runDelay(changeVideoSourceRunnable,channelChangeDelay);
        
        } catch (Exception e) {
            e.printStackTrace();
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
        if(channelListShowed){
        	handler.removeCallbacks(r);
            handler.postDelayed(r, 0);
            channelListShowed = false;
            return true;
        }
        
        if(!DTVInitFlag){
        	return true;
        }
        
		mDTVPlay.release() ;
		mUnit.release() ;
		handler.removeCallbacks(r);
        mHandler.removeMessages(1);
        mHandler.removeMessages(2);
        //surfaceViewLayout.setVisibility(View.GONE);
        VoDViewManager.getInstance().hideLiveVideo();
        VoDViewManager.getInstance().popForegroundView();
        VoDViewManager.getInstance().showBackgroundVideo();
        VoDViewManager.getInstance().playBackgroundVideo();
        return true;

    }
    
    private BroadcastReceiver mReceiver;
	private boolean isReceiver;
	private void bindReceiver() {
		IntentFilter mRefreshFilter = new IntentFilter();
		mRefreshFilter.addAction("android.intent.DTV");
		mReceiver = new BroadcastReceiver() {
			public void onReceive(Context context1, Intent intent) {
				handleEventRefresh(context, intent);
			}
		};
		context.registerReceiver(mReceiver, mRefreshFilter);
		isReceiver = true;
	}

	private void handleEventRefresh(Context context, Intent intent) {
		String action = intent.getAction();
		if ("android.intent.DTV".equals(action)) {
			String info = intent.getStringExtra("DTV_Info") ;
			Log.e(TAG, "---�㲥��Ϣ info:"+info);
		} 
	}
}
