package com.clearcrane.tool;


import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.net.SntpClient;
import android.util.Log;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.constant.clearProject;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.view.VoDViewManager;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;


public class DateUtil {
	public static final String TAG = "Dateutil";
    private static long serverTime = 0;
    private static SntpClient ntpClient = new SntpClient();
    private static DateUtil instance = null;
    private static String ntpServer = ClearConfig.serverIp;
    private static final int timeout = 1 * 1000;
    private static final long period = 10 * 1000;//自增周期
    
    private static final long round = 60 * 30 * 1000 / period;//每三十分钟，去服务器同步一次
    private int loop = 0;
    private boolean connected = false;
    private Timer rebootTimer =null;
    private TimerTask rebootTask = null;
    
    public final static String DATE_ZONE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public final static String REBOOT_DATE_ZONE_FORMAT = "HH:mm";
    
    public static SharedPreferences accessTimeSharePre;
	public static long systemMillsTimeDelta = 0;
	
    
    private DateUtil(){
    	init();
    }
    
    
   
    
    public long getDate(){
    	//直接获取init中对时间处理后的serverTime
		return serverTime;
    }
    
   
    
    public static DateUtil getInstance(){
    	if(instance == null){
    		instance = new DateUtil();
    	}
    	return instance;
    }
    
    public long getCurServerTime(){
    	if(ntpClient.requestTime(ntpServer, timeout)){
    		return ntpClient.getNtpTime();
    	}
    	return serverTime;
    }
    
    

	/* 获取当前时间戳, 微秒 */
	public static long getCurrentTimeMillSecond() {
		return Calendar.getInstance().getTimeInMillis() + systemMillsTimeDelta; 
	}
	
	
	/* 解析时间字符串，返回绝对微秒 */
	public static long getTimeMillSecondFromDateStr(String dateStr) {
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_ZONE_FORMAT); 
		try {
			Date parseDate = formatter.parse(dateStr);
			return (parseDate.getTime());
		} catch (ParseException e) {
			Log.i(TAG, "parse date string error: " + dateStr);
			e.printStackTrace();
			return Long.MAX_VALUE;
		}
	}
	
	
	public static void setSystemTime(String dateStr) {
		/**
		accessTimeSharePre = ctx.getSharedPreferences(ClearConstant.MODULEGROUP_FILE,
				Context.MODE_PRIVATE);
		String str = accessTimeSharePre.getString("ServerTime", null);
		**/
		long curSystemMillTime = getCurrentTimeMillSecond();
		long newSystemMillTime = getTimeMillSecondFromDateStr(dateStr);
		
		/* 如果差距超过 10 分钟，我们就重设 “系统时间 ”  存在一个问题，str不变，导致在校对时间时 */
		long curDelta = Math.abs(newSystemMillTime - curSystemMillTime);
		if(curDelta > 10 * 60 * 1000) {
			Log.i(TAG, "set system time " + dateStr);
			long realSystemTime = Calendar.getInstance().getTimeInMillis();
			systemMillsTimeDelta = newSystemMillTime - realSystemTime;
			Log.i("serverTime","："+newSystemMillTime);
			serverTime = newSystemMillTime;
		}else{
			Log.i("heziTime","h："+curSystemMillTime+"  s:"+newSystemMillTime);
			serverTime = curSystemMillTime;
		}
	}
	
	
	
	
	public long getTime( String dateStr){
		setSystemTime(dateStr);
		return serverTime;
	}
	
    public void init(){
    	
    	
    	
    	serverTime = System.currentTimeMillis();
    	
    	Log.i(TAG,"local time: " + serverTime);
    	if(ntpClient.requestTime(ntpServer, timeout)){
    		serverTime = ntpClient.getNtpTime();
    		connected = true;
    		Log.i(TAG,"get ntp time success:"+ serverTime);
    	}
    	
    	Timer timer = new Timer();
    	timer.schedule(new TimerTask(){
        
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(loop < round && connected){
					loop++;
		    		serverTime += period;
		    		Log.i(TAG,"timer adjust itself"+ serverTime);
				}else{
					loop = 0;
					if(ntpClient.requestTime(ntpServer, timeout)){
			    		serverTime = ntpClient.getNtpTime();
			    		connected = true;
			    		Log.i(TAG,"get ntp server time:"+ serverTime);
			    	}else{
			    		//serverTime += period;
			    		serverTime = System.currentTimeMillis();
			    		
			    		//setSystemTime(str);
			    		
			    		
			    	}
				}
			}
    		
    	}, 0,period);
    	
    	
    	
    	rebootTimer = new Timer(true);
    	
    }
    
	
	@SuppressLint("SimpleDateFormat")
    public long parseDateToLong(String str) {
		if (str == null || str.trim().equals("")) {
			return 0;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_ZONE_FORMAT);
		try {
			Date date = sdf.parse(str);
			Log.i(TAG, "parse date:" + date);
			Log.i(TAG, "local date:" + new Date(serverTime));
			return date.getTime();
		} catch (ParseException e) {
			Log.e(TAG," parse date error:"+ str);
			return 0;
		}
	}
	
	private boolean doReboot() {
        String uri = "http://127.0.0.1:19003/index.html?op=reboot";
        Log.i(TAG,"do reboot ");
        try {
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "text/xml");
            conn.setRequestProperty("charset", "utf-8");
            conn.setConnectTimeout(10000);
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return true;
            } else {
                Log.e(TAG, "response code=" + conn.getResponseCode());
            }
        } catch (Exception e) {
            Log.e(TAG, "[" + Log.getStackTraceString(e) + "]");
        }
        return false;
    }
	
	public void setRebootSchdule(boolean enable, int hour, int minute){
		Log.i(TAG,"set reboot schdule");
        if (rebootTask != null) {
        	rebootTask.cancel();
        }
        
        rebootTask = new rebootTimerTask(hour,minute);
        rebootTimer.schedule(rebootTask, 0, period);
		if(!enable){
			Log.i(TAG,"cancel reboot schdule");
			rebootTask.cancel();
		}
	}

	class rebootTimerTask extends TimerTask {
		int schduleHour;
		int schduleMin;
		rebootTimerTask(int h, int m){
			schduleHour = h;
			schduleMin = m;
		}
	    @Override
	    public void run() {
	    	Calendar mCalendar = null;
	    	if(VoDViewManager.getInstance().project_name.contains("Nigeria") 
	    			|| VoDViewManager.getInstance().project_name.contains(clearProject.Nigeria_BLACKDIAMOND)){
	    		Log.i(TAG,"set gmt +1");
	    		mCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"));
	    	}else{
	    		mCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
	    	}
	    	mCalendar.setTimeInMillis(getCurServerTime());
	        int serverHour =  mCalendar.get(Calendar.HOUR_OF_DAY);
	        int serverMin = mCalendar.get(Calendar.MINUTE);
	        
	        if(serverHour == schduleHour && serverMin == ClearConstant.MINUTE[schduleMin] ){
	        	Log.i(TAG,"will reboot");
	        	doReboot();
	        }else{
	        	Log.i(TAG,"not schdule time,serverHour:" + serverHour + "serverMin:" +serverMin + "schduleHour:" + schduleHour + "schdulemin:"+ClearConstant.MINUTE[schduleMin]);
	        }
	    }
	}
    
}
    	
    	
   
