/**
 * @author xujifu
 * @copyright clear
 * @date 2014-06-20
 * @description:应用的时间布局,所有布局均有时间，因此时间View放在了ViewManager中管理
 */
package com.clearcrane.view;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clearcrane.provider.MaterialRequest;
import com.clearcrane.provider.MaterialRequest.OnCompleteListener;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.TipDialog;
import com.clearcrane.vod.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class VodDateView extends RelativeLayout {
	private Context mContext;
	private WeatherInfo mWeatherInfo = new WeatherInfo();

	private TextView weatherText, weatherDay, weatherTime;
	private ImageView weatherPic;
	private String dateJson;
	
	
	public VodDateView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initView(context);
	}
	
	public VodDateView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initView(context);
	}

	public VodDateView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initView(context);
	}

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
	}
	
	private void initView(Context context){
		mContext = context;
		
		LayoutInflater.from(context).inflate(R.layout.date_view, this);
		
		weatherText = (TextView)findViewById(R.id.weather_text);
		weatherDay = (TextView)findViewById(R.id.weather_day);
		weatherTime = (TextView)findViewById(R.id.weather_time);
		weatherPic = (ImageView)findViewById(R.id.weather_pic);
		
		MaterialRequest weatherMR = new MaterialRequest(mContext, ClearConfig.TYPE_JSON);
		weatherMR.setOnCompleteListener(mWJCompleteListener);
		if(ClearConfig.checkNetwork(mContext) == ClearConfig.TYPE_LOCAL_STB)
			weatherMR.execute("/nativevod/json/weather.json");
		else
			weatherMR.execute(ClearConfig.WEATHER_URI);//net work
	}
	
	/**
	 * Weather json complete listener
	 */
	public OnCompleteListener mWJCompleteListener = new OnCompleteListener(){

		@Override
		public void onDownloaded(Object result) {
			// TODO Auto-generated method stub
			dateJson = (String)result;
			if(dateJson == null){
				TipDialog.Builder builder = new TipDialog.Builder(mContext);
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
			try {
				JSONTokener wJsonToken = new JSONTokener(dateJson);
				JSONObject wObject = (JSONObject)wJsonToken.nextValue();
				JSONArray wArray = (JSONArray)wObject.getJSONArray("Weather");
				for(int i = 0; i < wArray.length(); i ++){
					JSONObject object = (JSONObject)wArray.opt(i);
					mWeatherInfo.Data = object.getString("Date");
					mWeatherInfo.Temperature = object.getString("Temperature");
					mWeatherInfo.Url = object.getString("URL");
					//time = object.getString("Time");
					weatherDay.setText(mWeatherInfo.Data);
					weatherText.setText(mWeatherInfo.Temperature);
					MaterialRequest weatherMR = new MaterialRequest(mContext, weatherPic, ClearConfig.TYPE_IMAGE);
					weatherMR.execute(mWeatherInfo.Url);
				}
				TimeFormat();
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

	public void TimeFormat(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		//SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
		//timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
		monthFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
		dayFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date date = null;
		Calendar calendar = Calendar.getInstance();
		try {
			date = sdf.parse(mWeatherInfo.Data);
			calendar.setTime(sdf.parse(mWeatherInfo.Data));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		weatherDay.setText(ClearConfig.WEEK[calendar.DAY_OF_WEEK - 1] + ","
				+ ClearConfig.MONTH[date.getMonth()] + " " + dayFormat.format(date));
		//weatherTime.setText(timeFormat.format(date));
		date = new Date(System.currentTimeMillis());
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
		//timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		weatherTime.setText(timeFormat.format(date));
		timer.schedule(new TimerTask()  
        {  
  
            @Override  
            public void run()  
            {  
                // TODO Auto-generated method stub  
                Message message = new Message();  
                message.what = 1;  
                handler.sendMessage(message);  
            }  
  
        }, 0, 1000); 
	}
	
	
	Timer timer = new Timer();
	
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			int msgId = msg.what;
			switch(msgId){
			case 1:
				Date date = new Date(System.currentTimeMillis());
				SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
				//timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
				weatherTime.setText(timeFormat.format(date));
				break;
			default:
				break;
			}
		}
	};
}
