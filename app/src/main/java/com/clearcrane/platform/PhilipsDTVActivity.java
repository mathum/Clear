package com.clearcrane.platform;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.clearcrane.vod.R;
import com.operationservice.DTVPlay;
import com.operationservice.MyListener;
import com.operationservice.ProgramFavoriteObject;
import com.operationservice.Source;
import com.operationservice.Unit;

import java.util.List;

public class PhilipsDTVActivity extends Activity{
    
    private String TAG="PhilipsDTVActivity";
    private Unit mUnit;
    public MyListener myListener ; 
    public RelativeLayout surfaceViewLayout ;
    private DTVPlay mDTVPlay ;
    public SurfaceView surfaceView ;
    private List<ProgramFavoriteObject> mList;
    public long begin;
    private String sourceJson;
    private int SOURCE = Source.INPUT_SOURCE_DTV;
    private Context context;
    private boolean philipsBack = false;
    private final int PLAY_START = 0 ;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.philips_dtv);
    	
    	 myListener = new MyListener() {
 			@Override
 			public void bindSuccess() {
 				Log.i(TAG,"bind success");
 				// TODO Auto-generated method stub
 				mHandler.obtainMessage(1).sendToTarget() ;
 			}
 		};
 		mUnit = new Unit(this , myListener) ;
 		
 		context = this;
    }
    
    @Override
    protected void onDestroy(){
    	Log.d(TAG,"destory");
    	super.onDestroy();
    	mDTVPlay.release() ;
		mUnit.release() ;
    }
    
    private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case PLAY_START :
				if(mList!=null&&!"".equals(mList)&&mList.size()>0 && mUnit!=null){
					if(mUnit.getInputSource()!=Source.INPUT_SOURCE_DTV){
						mUnit.setSource(Source.INPUT_SOURCE_DTV) ;
					}
					Log.e(TAG, "--------------- 74 ServiceId:"+mList.get(num).getServiceId());
					Log.e(TAG, "--------------- 74 Frequency:"+mList.get(num).getFrequency());
					Log.e(TAG, "--------------- 74 SR:"+mList.get(num).getSR());
					Log.e(TAG, "--------------- 74 Modulation:"+mList.get(num).getModulation());
					mDTVPlay.play(mList.get(num).getServiceId(), mList.get(num).getFrequency(), mList.get(num).getSR(), mList.get(num).getModulation()) ;
				}
				break ;
			case 1:
				Log.i(TAG,"bind success");
				//mUnit.getInputSource() ;
				mUnit.init() ;
				mUnit.setSource(SOURCE);
				
				surfaceViewLayout = (RelativeLayout)findViewById(R.id.philips_surfaceview_layout);
				surfaceView = new SurfaceView(context.getApplicationContext());
				surfaceView.setLayoutParams(new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				surfaceViewLayout.addView(surfaceView) ;
				mDTVPlay = new DTVPlay(mUnit ,surfaceView) ;
				mDTVPlay.init() ;
				mList = mDTVPlay.getListInfo() ;
				Log.i(TAG,"dtvplayrsize:"+mDTVPlay.getListInfo().size());
				num = 0;
				Playstart();
				break ;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	public void Playstart()
	{
		mHandler.obtainMessage(PLAY_START).sendToTarget() ;
	}
	
	private int num = -1 ;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		Log.e(TAG, "------------------------------ keyCode:"+keyCode) ;
		mHandler.removeMessages(PLAY_START) ;
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_DOWN:
			if(mList == null ||  "".equals(mList) && mList.size() <= 0){
				return true ;
			}
			
			num ++ ;
			if(num >= mList.size()){
				num = 0 ;
			}
			Log.e(TAG, "--------------------- 214 num:"+num) ;
			Playstart() ;
			break ;
		case KeyEvent.KEYCODE_DPAD_UP:
			if(mList == null ||  "".equals(mList) && mList.size() <= 0){
				return true ;
			}
			
			num--;
			if(num <0){
				num = mList.size() -1  ;
			}
			Log.e(TAG, "--------------------- 255 num:"+num) ;
			Playstart() ;
			break ;
		default:
			break;
		}

		return super.onKeyDown(keyCode, event);
	}
	

}
