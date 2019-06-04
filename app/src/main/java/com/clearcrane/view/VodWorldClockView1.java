package com.clearcrane.view;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clearcrane.tool.DateUtil;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.vod.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class VodWorldClockView1 extends VoDBaseView {
	
	private ImageView mBackground,mClockSelect;
	private TextView mTitleText, mWorldCityText, mWorldTimeText, mWorldDateWeekText;
	private TextView mBeiJingTitleText, mBeiJingTimeText, mBeiJingdateText, mBeiJingWeekText;
	private TextView mTimeZoneText, mSelectText, mBackText;
	private int listIndex = 0;
	private long period = 10 * 1000;
	
	 public Handler mHandler = new Handler();
	 String TAG = "worldclock";
	 
	 public String[] chnWeekList = {"星期日","星期一","星期二","星期三","星期四","星期五","星期六"};
	 public String[] engWeekList = {"SUNDAY","MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY"};
	 
	 public String[] chnZoneList = {"西十二区","西十一区","西十区","西九区","西八区","西七区","西六区","西五区",
			 						"西四区","西三区","西二区","西一区","零时区","东一区","东二区","东三区","东四区",
			 						"东五区","东六区","东七区","东八区","东九区","东十区","东十一区","东十二区"};
	 public String[] engZoneList = {"UTC-12","UTC-11","UTC-10","UTC-9","UTC-8","UTC-7","UTC-6","UTC-5",
									"UTC-4","UTC-3","UTC-2","UTC-1","UTC-0","UTC+1","UTC+2","UTC+3","UTC+4",
									"UTC+5","UTC+6","UTC+7","UTC+8","UTC+9","UTC+10","UTC+11","UTC+12"};
	 
	 public String[] chnCityList = {"夸贾林岛","帕果帕果，努库阿洛法","檀香山","阿拉斯加","蒂华纳，加利福尼亚，旧金山","犹他，亚利桑那","危地马拉，贝尔莫潘",
			 "纽约，渥太华","苏克雷，加拉加斯","帕拉马里博，巴西利亚"," "," ","都伯林，里斯本","斯德哥尔摩，哥本哈根","明斯克，基辅","利雅得，科威特","马斯喀特，路易港",
			 "马累，塔什干","阿斯塔纳，比什凯克","万象，曼谷","乌兰巴托，吉隆坡","首尔，东京","莫尔兹比港，帕利基尔","维拉港，新喀里多尼亚","亚伦，塔拉瓦",};
	 public String[] engCityList = {"Pwajalein","Pago Pago,Nukualofa","Honolulu","Alaska","Tijuana,California,San Francisco","Utah,Arizona","Guatemala,Belmopan",
			 "New York,Ottawa","Caracas","Paramaribo,Brasilia","","","Dublin,Lisbon","Stockholm,Copenhagen","Minsk,Kiev","Riyadh,Kuwait","Muscat,Port Louis",
				"Male,Tashkent","Astana,Bishkek","Vientiane,Bangkok","Ulan Bator,Kuala Lumpur","Seoul,Tokyo","Port Moresby,Palikir","Port Vila,New Caledonia","Araon,Tarawa"};
	 
	 public String[] chnCityTitleList = {"埃尼维托克岛","阿皮亚","夏威夷","安克雷奇","洛杉矶","盐湖城","墨西哥城",
			 "华盛顿","圣地亚哥","乔治敦","普拉亚","亚速尔群岛","伦敦","奥斯陆","赫尔辛基","莫斯科","阿布扎比",
			 "伊斯兰堡","达卡","河内","北京","平壤","堪培拉","堆尼亚拉","惠林顿"};
	 public String[] engCityTitleList = {"Eniwetok","Apia","Hawaii","Anchorage","Los Angeles","Salt Lake City","Mexico City",
			 "Washington","Santiago","Georgetown","Praia","Azores","London","Oslo","Helsinki","Moscow","Abu Dhabi",
			 "Lslamabad","Dacca","Hanoi","Beijing","Pyongyang","Canberra","Honiara","Wellington"};
	 
	 Date mData = new Date(System.currentTimeMillis());//    HH:mm:ss     
     SimpleDateFormat date = new SimpleDateFormat("yyyy/MM/dd");
     SimpleDateFormat time = new SimpleDateFormat("HH:mm");
     
     Calendar btnTime = Calendar.getInstance();
	

	public void init(Context ctx, String u){
		super.init(ctx, u);
		
		view = LayoutInflater.from(context).inflate(R.layout.nigeria_world_clock, null);
		initLayoutInXml();
	}
	
	public void init(Context ctx, String u, LinearLayout layout){
		super.init(ctx, u, layout);
		
		view = LayoutInflater.from(context).inflate(R.layout.nigeria_world_clock, null);
		initLayoutInXml();
	}

	private void initLayoutInXml() {
		// TODO Auto-generated method stub
		mClockSelect = (ImageView)view.findViewById(R.id.thj_worldclock_btn);
		//mClockSelect.setAlpha(99);
		mBackground = (ImageView) view.findViewById(R.id.thj_worldclock_main);
		
		String selUrl = "drawable://" + R.drawable.wordcolock_focus;
		String backgroungUrl = "drawable://" + R.drawable.clolk_bk;
		ImageLoader.getInstance().displayImage(backgroungUrl, mBackground);
		//ImageLoader.getInstance().displayImage(selUrl, mClockSelect);
		
		mTitleText = (TextView) view.findViewById(R.id.thj_worldclock_title);
		mTitleText.setText(ClearConfig.getStringByLanguageId("世界时钟", "World Time"));
		mWorldCityText = (TextView) view.findViewById(R.id.thj_worldclock_world_city);
		mWorldTimeText = (TextView) view.findViewById(R.id.thj_worldclock_world_time);
		mWorldDateWeekText = (TextView) view.findViewById(R.id.thj_worldclock_world_date_week);
		
		mBeiJingTitleText = (TextView) view.findViewById(R.id.thj_worldclock_beijing_title);
		mBeiJingTitleText.setText(ClearConfig.getStringByLanguageId("北京时间","Beijing Time"));
		mBeiJingTimeText = (TextView) view.findViewById(R.id.thj_worldclock_beijing_time);
		mBeiJingdateText = (TextView) view.findViewById(R.id.thj_worldclock_beijing_date);
		mBeiJingWeekText = (TextView) view.findViewById(R.id.thj_worldclock_beijing_week);
		
		mTimeZoneText = (TextView) view.findViewById(R.id.thj_worldclock_time_zone);
		mSelectText = (TextView) view.findViewById(R.id.thj_worldclock_select_text);
		mSelectText.setText(ClearConfig.getStringByLanguageId("选择", "Select"));
		mBackText = (TextView) view.findViewById(R.id.thj_worldclock_back_text);
		mBackText.setText(ClearConfig.getStringByLanguageId("返回", "Back"));

		listIndex = 20;
		//timer update date 
		Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                mHandler.post(new Runnable(){
					@Override
					public void run() {
						// TODO Auto-generated method stub
			            updateTimeZone(listIndex);
					}
                });
            }
        }, 0,period);
		
	}
	

	public boolean onKeyDpadLeft(){
		long between = (Calendar.getInstance()).getTimeInMillis()-btnTime.getTimeInMillis();
		if(between < 100 ){
			return true;
		}
		btnTime = Calendar.getInstance();
		mClockSelect.animate().cancel();
		if(listIndex > 0){
			listIndex--;
			mClockSelect.animate()
				.translationXBy(-ClearConfig.getPlatformWidth(28))
				.setDuration(10)
				.start();
		}else{
			listIndex = 24;
			mClockSelect.animate()
				.translationXBy(ClearConfig.getPlatformWidth(672))
				.setDuration(10)
				.start();
		}
		updateTimeZone(listIndex);
		return true;
	}
	
	public boolean onKeyDpadRight(){
		long between = (Calendar.getInstance()).getTimeInMillis()-btnTime.getTimeInMillis();
		if(between < 100 ){
			return true;
		}
		btnTime = Calendar.getInstance();
		mClockSelect.animate().cancel();
		if(listIndex < 24){
			listIndex++;
			mClockSelect.animate()
			.translationXBy(ClearConfig.getPlatformWidth(28))
			.setDuration(10)
			.start();
		}else{
			listIndex = 0;
			mClockSelect.animate()
			.translationXBy(-ClearConfig.getPlatformWidth(672))
			.setDuration(10)
			.start();
		}
		
		updateTimeZone(listIndex);
		return true;
	}
	
	public void updateTimeZone(int index){
		
    	Calendar mCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
    	mCalendar.setTimeInMillis(DateUtil.getInstance().getDate());
    	date.setTimeZone(TimeZone.getTimeZone("GMT+8"));
    	time.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        int localweekIndex = mCalendar.get(Calendar.DAY_OF_WEEK);
        mBeiJingWeekText.setText(ClearConfig.getStringByLanguageId(chnWeekList[localweekIndex-1],engWeekList[localweekIndex -1]));
        mBeiJingTimeText.setText(time.format(mCalendar.getTime()));
        mBeiJingdateText.setText(date.format(mCalendar.getTime()));
		
		
		int zone = index - 12;
		String timeZoneString = null;
		if(zone < 0){
			timeZoneString = "GMT"+ zone;
		}else if(zone > 0){
			timeZoneString = "GMT+"+ zone;
		}else{
			timeZoneString = "GMT";
		}
		
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timeZoneString));
		cal.setTimeInMillis(DateUtil.getInstance().getDate());
    	date.setTimeZone(TimeZone.getTimeZone(timeZoneString));
    	time.setTimeZone(TimeZone.getTimeZone(timeZoneString));
    	
    	int worldWeekIndex = cal.get(Calendar.DAY_OF_WEEK);
    	String ts = time.format(cal.getTime());
    	String ds = date.format(cal.getTime());
    	
    	mWorldDateWeekText.setText(ds + " " +
    			ClearConfig.getStringByLanguageId(chnWeekList[worldWeekIndex-1],engWeekList[worldWeekIndex -1]));
    	mWorldTimeText.setText(ts);
    	
    	mWorldCityText.setText(
    			ClearConfig.getStringByLanguageId(chnCityTitleList[index], engCityTitleList[index]));
    	mTimeZoneText.setText(
    			ClearConfig.getStringByLanguageId(chnZoneList[index], engZoneList[index])+"（"+
    			ClearConfig.getStringByLanguageId(chnCityList[index], engCityList[index])+")");
	}
	
}
