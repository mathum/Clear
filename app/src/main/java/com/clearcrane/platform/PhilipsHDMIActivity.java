package com.clearcrane.platform;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.RelativeLayout;

import com.clearcrane.provider.MaterialRequest.OnCompleteListener;
import com.clearcrane.util.TipDialog;
import com.clearcrane.vod.R;
import com.operationservice.MyListener;
import com.operationservice.Source;
import com.operationservice.Unit;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class PhilipsHDMIActivity extends Activity{
    
    private String TAG="PhilipsHDMIView";
    private Unit mUnit;
    public MyListener myListener ; 
    public RelativeLayout surfaceViewLayout ;
    public long begin;
    private String sourceJson;
    private int SOURCE = Source.INPUT_SOURCE_HDMI;
    private Context context;
    private boolean philipsBack = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.philips_hdmi);
    	Intent intent = getIntent();
    	String url = intent.getStringExtra("jsonUrl");
    	Log.i(TAG,"jsonUrl:" + url);
    	 myListener = new MyListener() {
 			@Override
 			public void bindSuccess() {
 				Log.i(TAG,"bind success");
 				// TODO Auto-generated method stub
 				mHandler.obtainMessage(1).sendToTarget() ;
 			}
 		};
 		mUnit = new Unit(this , myListener) ;
 		
 		/*MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
  		mr.setOnCompleteListener(philipsJsonListen);
  		mr.execute(url);*/
 		context = this;
    }
    
    @Override
    protected void onResume(){
    	Log.d(TAG,"onresume");
    	super.onResume();
    	if(philipsBack){
    		Log.i(TAG, " finish");
    		mUnit.release() ;
    		this.finish();
    	}
    }
    
    @Override
    protected void onDestroy(){
    	Log.d(TAG,"destory");
    	super.onDestroy();
    }
    
    private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 1:
				Log.i(TAG,"bind success");
				mUnit.getInputSource() ;
				mUnit.init() ;
				mUnit.setSource(SOURCE);
				philipsBack = true;
				Log.i(TAG,"set source:" + mUnit.getInputSource());
				break ;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	
private OnCompleteListener philipsJsonListen = new OnCompleteListener() {
		
		@Override
		public void onDownloaded(Object result) {
			// TODO Auto-generated method stub
			sourceJson = (String)result;
			if(sourceJson == null){
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
				JSONTokener jsonParser = new JSONTokener(sourceJson);  
			    JSONObject object = (JSONObject) jsonParser.nextValue();  
			    String source_type = object.getString("source");
			    if(source_type.equalsIgnoreCase("HDMI1")){
			    	SOURCE = Source.INPUT_SOURCE_HDMI;
			    }else if(source_type.equalsIgnoreCase("HDMI2")){
			    	SOURCE = Source.INPUT_SOURCE_HDMI2;
			    }else if(source_type.equalsIgnoreCase("HDMI3")){
			    	SOURCE = Source.INPUT_SOURCE_HDMI3;
			    }else if(source_type.equalsIgnoreCase("VGA")){
			    	SOURCE = Source.INPUT_SOURCE_VGA;
			    }
			    
			    if(begin != 0) {
                	long between = System.currentTimeMillis() - begin;
                   // ClearLog.LogInfo("BROSWER\tLoad\tSUCC\t" + between +"ms\t" + url + "\t"
                    		//+ "liveView");
                } 
			}catch(JSONException e){
				//ClearLog.LogError("BROSWER\tLoad\tFAIL\t0ms\t" + url);
				e.printStackTrace();
			}
		}
		@Override
		public void onComplete(boolean result) {
			// TODO Auto-generated method stub
			
		}
	};

}
