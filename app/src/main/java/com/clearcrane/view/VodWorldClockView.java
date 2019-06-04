/**
 * @author xujifu
 * @copyright clear
 * @date 2014-06-20
 * @description 世界时钟界面
 */
package com.clearcrane.view;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

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

public class VodWorldClockView extends VoDBaseView {
	
	private ImageView mClockSelect;
	private String WorldClockJson;
	private ArrayList<TimeZone> mTimeZone = new ArrayList<TimeZone>();
	
	class TimeZone{
		String zone;
		String city;
	}

	public void init(Context ctx, String u){
		super.init(ctx, u);
		
		url = new String("http://192.168.18.249/nativevod/json/worldclock.json");
		view = LayoutInflater.from(context).inflate(R.layout.worldclock_view, null);
		initLayoutInXml();
		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(ClockJsonListener);
		if(ClearConfig.checkNetwork(context) == ClearConfig.TYPE_LOCAL_STB)
			mr.execute("/nativevod/json/worldclock.json");
		else{
			mr.execute(url);
			VoDViewManager.getInstance().playBackgroundVideo();
		}
	}
	
	public void init(Context ctx, String u, LinearLayout layout){
		super.init(ctx, u, layout);
		
		url = new String("http://192.168.18.249/nativevod/json/worldclock.json");
		view = LayoutInflater.from(context).inflate(R.layout.worldclock_view, null);
		initLayoutInXml();
		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(ClockJsonListener);
		if(ClearConfig.checkNetwork(context) == ClearConfig.TYPE_LOCAL_STB)
			mr.execute("/nativevod/json/worldclock.json");
		else{
			mr.execute(url);
			VoDViewManager.getInstance().playBackgroundVideo();
		}
	}

	private void initLayoutInXml() {
		// TODO Auto-generated method stub
		mClockSelect = (ImageView)view.findViewById(R.id.world_clock_select);
	}
	
	private OnCompleteListener ClockJsonListener = new OnCompleteListener(){

		@Override
		public void onDownloaded(Object result) {
			// TODO Auto-generated method stub
			WorldClockJson = (String)result;
			if(WorldClockJson == null){
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
			try{
				JSONTokener jsonParser = new JSONTokener(WorldClockJson);
				JSONObject object = (JSONObject)jsonParser.nextValue();
				JSONArray array = (JSONArray)object.getJSONArray("Content");
				for(int i = 0; i < array.length(); i ++){
					JSONObject tmp = (JSONObject)array.opt(i);
					TimeZone zone = new TimeZone();
					zone.zone = tmp.getString("TimeZone");
					zone.city = tmp.getString("City");
					mTimeZone.add(zone);
				}
			}catch(JSONException e){
				
			}
		}

		@Override
		public void onComplete(boolean result) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	public boolean onKeyDpadLeft(){
		mClockSelect.animate()
			.translationXBy(mClockSelect.getWidth() - 5)
			.start();
		return true;
	}
	
	public boolean onKeyDpadRight(){
		mClockSelect.animate()
		.translationXBy(-mClockSelect.getWidth() + 5)
		.start();
		return true;
	}
}
