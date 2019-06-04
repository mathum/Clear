package com.clearcrane.schedule;

import android.content.Context;
import android.util.Log;

import com.clearcrane.provider.MaterialRequest;
import com.clearcrane.provider.MaterialRequest.OnCompleteListener;
import com.clearcrane.util.ClearConfig;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;

public class Channel {
	
	private final String TAG = "Channel";
	private Context mContext;
	
	/* TODO,FIXME,channel jsons should not store in /Main/json  */
	private final String CHANNEL_JSON_URL_PATH = "/Channel/channel_";
	private final String CHANNEL_JSON_URL_REAR = ".json";
	private int channel_id;
	private String channel_json_url;
	private String channelJson;
	
	public ProgramSegment curPlayProgramSegment;
	public Program curValidProgram;
	
	
	public ArrayList<Program> programList;
	
	public Channel(Context ctx,int cid){
		this.mContext = ctx;
		this.channel_id = cid;
		this.programList = new ArrayList<Program>();
		this.channel_json_url = ClearConfig.getJsonUrl(mContext, CHANNEL_JSON_URL_PATH+channel_id + CHANNEL_JSON_URL_REAR);
		Log.e(TAG,"lilei channel_json :" + channel_json_url);
//		this.channel_json_url = "http://172.16.1.34/versions/beixinjing/5/Channel/channel_6.json";
//		this.channel_json_url = "http://172.16.1.34/versions/beixinjing/6/Channel/channel_148.json";
	}
	
	public void init(){
		MaterialRequest mr = new MaterialRequest(mContext, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(mCompleteListener);
		mr.execute(channel_json_url);
	}
	
	OnCompleteListener mCompleteListener = new OnCompleteListener() {
		
		@Override
		public void onDownloaded(Object result) {
			// TODO Auto-generated method stub
			channelJson = (String) result;
			if (result == null){
				Log.e(TAG,"error!could not get json: "+channel_json_url);
				return;
		    }
			try{
				JSONTokener jsonTokener = new JSONTokener(channelJson);
				JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
				JSONArray jsonArray = jsonObject.getJSONArray("programs");
				for(int i = 0; i < jsonArray.length(); i++){
					JSONObject programJson = (JSONObject) jsonArray.opt(i);
					Program program = new Program(programJson);
					program.init();
					/*TODO,FIXME,rank by program's priority,bigger first */
					programList.add(program);
				}
			Log.e("channel","init channel OK");
			}catch(Exception e){
				Log.e(TAG,"read channelJson failed " + channelJson);
			}
		}
		
		@Override
		public void onComplete(boolean result) {
			// TODO Auto-generated method stub
			
		}
	};
	
}
