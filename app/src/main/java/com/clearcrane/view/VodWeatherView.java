/**
 * @auther xjf
 * @copyright:Clear
 * @date 2014-07-09
 * @description 天气界面
 * Others:need chenged
 */
package com.clearcrane.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clearcrane.provider.MaterialRequest;
import com.clearcrane.provider.MaterialRequest.OnCompleteListener;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.TipDialog;
import com.clearcrane.util.TrendView;
import com.clearcrane.util.WeatherCityAdapter;
import com.clearcrane.vod.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;

public class VodWeatherView extends VoDBaseView {
		
	private String weatherJson;
	private WeatherInfo mWeatherInfo = new WeatherInfo();
	private TrendView trend;
	private RelativeLayout mWeatherTitle;//天气title栏
	private LinearLayout mWeatherLeftTitle;//天气title栏左边部分
	private ImageView mWeatherTitlePic;//天气title栏图标
	private TextView mWeatherTitleName;//天气名称
	private TextView mWeatherWName;//天气查询名称
	private ImageView mWeatherBaseLine;//横线
	private LinearLayout mWeatherCountry;//城市选择
	private TextView mWeatherTabLeft;//左侧城市分类
	private TextView mWeatherTabRight;//右侧城市分类
	private ListView mWeatherCity;//城市选择列表
	private LinearLayout mWeatherContent;//天气信息
	
	private WeatherCityAdapter adapter;
	private ArrayList<String> mCity = new ArrayList<String>();
	
	class WeatherInfo{
		String Data;
		String City;
		String Temperature;
		String Weather;
		String Url;
		String WindSpeed;
		String WindLevel;
		String Dressing;
		String UltravioletRays;
		String Travel;
		String Comfortable;
		ArrayList<NextInfo> info = new ArrayList<NextInfo>(); 
	}
	
	class NextInfo{
		String Top;
		String Low;
		String Week;
		Bitmap url;
		boolean isDownload = false;
	}
	

	public void init(Context ctx, String u){
		super.init(ctx, u);
		
		url = new String("http://192.168.18.249/nativevod/json/weathersearch.json");
		view = LayoutInflater.from(context).inflate(R.layout.weather_view, null);
		
		//for test, remove me
		initLayoutInXml();
		
		/* trigger to get data sources */
		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(WeatherJsonListen);
		if(ClearConfig.checkNetwork(context) == ClearConfig.TYPE_LOCAL_STB)
			mr.execute("/nativevod/json/weathersearch.json");
		else{
			VoDViewManager.getInstance().playBackgroundVideo();
			mr.execute(url);
		}
		
	}

	public void init(Context ctx, String u, LinearLayout layout){
		super.init(ctx, u, layout);
		
		url = new String("http://192.168.18.249/nativevod/json/weathersearch.json");
		view = LayoutInflater.from(context).inflate(R.layout.weather_view, null);
		
		//for test, remove me
		//VoDViewManager.getInstance().startBackgroundVideo("http://192.168.18.249/nativevod/movie/starrynight01.mp4");
		initLayoutInXml();
		
		/* trigger to get data sources */
		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(WeatherJsonListen);
		if(ClearConfig.checkNetwork(context) == ClearConfig.TYPE_LOCAL_STB)
			mr.execute("/nativevod/json/weathersearch.json");
		else
			mr.execute(url);
	}
	
	private void initLayoutInXml() {
		// TODO Auto-generated method stub
		trend = (TrendView)view.findViewById(R.id.weather_trend);
		mWeatherCity = (ListView)view.findViewById(R.id.weather_city);
	}
	
	class OnNextCompleteListerner implements OnCompleteListener{
		NextInfo info;
		
		public OnNextCompleteListerner(NextInfo info){
			this.info = info;
		}
		
		@Override
		public void onDownloaded(Object result) {
			// TODO Auto-generated method stub
			this.info.url = (Bitmap)result;
			mWeatherInfo.info.add(this.info);
		}

		@Override
		public void onComplete(boolean result) {
			// TODO Auto-generated method stub
			info.isDownload = true;
			boolean allDownloaded = true;
			for(int i = 0; i < mWeatherInfo.info.size(); i++) {
				if(mWeatherInfo.info.get(i).isDownload == false) {
					allDownloaded = false;
					break;
				}
			}
			
			if(allDownloaded) {
				trend.init(mWeatherInfo.info.size());
				trend.setWidthHeight(trend.getWidth(), trend.getHeight());
				ArrayList<String> top = new ArrayList<String>();
				ArrayList<String> low = new ArrayList<String>();
				ArrayList<Bitmap> bmp = new ArrayList<Bitmap>();
				ArrayList<String> week = new ArrayList<String>();
				for(int i = 0; i < mWeatherInfo.info.size(); i ++){
					top.add(mWeatherInfo.info.get(i).Top);
					low.add(mWeatherInfo.info.get(i).Low);
					bmp.add(mWeatherInfo.info.get(i).url);
					week.add(mWeatherInfo.info.get(i).Week);
				}
				trend.setWeek(week);
				trend.setBitmap(bmp);
				trend.setTemperature(top, low);
			}
		}
		
	}
	
	private OnCompleteListener WeatherJsonListen = new OnCompleteListener(){

		@Override
		public void onDownloaded(Object result) {
			// TODO Auto-generated method stub
			weatherJson = (String)result;
			if(weatherJson == null){
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
				JSONTokener jsonParser = new JSONTokener(weatherJson);  
			    JSONObject weatherViewObj = (JSONObject) jsonParser.nextValue();  
			    JSONArray contentArray = (JSONArray)weatherViewObj.getJSONArray("Weather");
			    for(int i = 0; i < contentArray.length(); i ++){
			    	JSONObject object = (JSONObject)contentArray.opt(i);
			    	mWeatherInfo.Data = object.getString("Date");
					mWeatherInfo.Temperature = object.getString("Temperature");
					mWeatherInfo.Url = object.getString("URL");
					//time = object.getString("Time");
//					weatherDay.setText(mWeatherInfo.Data);
//					weatherText.setText(mWeatherInfo.Temperature);
//					MaterialRequest weatherMR = new MaterialRequest(mContext, weatherPic, ClearConfig.TYPE_IMAGE);
//					weatherMR.execute(mWeatherInfo.Url);
			    }
			    JSONArray nextArray = (JSONArray)weatherViewObj.getJSONArray("Next");
			    for(int i = 0; i < nextArray.length(); i ++){
			    	JSONObject object = (JSONObject)nextArray.opt(i);
			    	NextInfo info = new NextInfo();
			    	info.Top = object.getString("Top");
			    	info.Low = object.getString("Low");
			    	info.Week = object.getString("Week");
			    	MaterialRequest weatherMR = new MaterialRequest(context, ClearConfig.TYPE_IMAGE);
			    	weatherMR.setOnCompleteListener(new OnNextCompleteListerner(info));
			    	weatherMR.execute(object.getString("Uri"));
			    }
			    JSONArray cityArray = (JSONArray)weatherViewObj.getJSONArray("City");
			    for(int i = 0; i < cityArray.length(); i ++){
			    	JSONObject object = (JSONObject)cityArray.opt(i);
			    	mCity.add(object.getString("Name"));
			    }
			    adapter = new WeatherCityAdapter(context, mCity);
				mWeatherCity.setAdapter(adapter);
			    
			}catch(JSONException e){
				
			}
		}

		@Override
		public void onComplete(boolean result) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	public boolean onKeyBack(){
		return false;
	}
}
