package com.clearcrane.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.clearcrane.log.ClearLog;
import com.clearcrane.provider.MaterialRequest;
import com.clearcrane.provider.MaterialRequest.OnCompleteListener;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.PictureCategoryListAdapter;
import com.clearcrane.util.TipDialog;
import com.clearcrane.vod.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class VodPictureListView extends VoDBaseView {
	
	private String dataJson;//json数据
	private ImageView pictureView ;
	private TextView pictureTitleText;
	private FrameLayout pictureListTextIntroduceFL;
	private TextView introduceText;
	private ListView pictureCategoryListView;
	private ImageView leftArrow ;
	private ImageView rightArrow ;
	private View picture_list_foreground;
	private PictureCategoryListAdapter picAdapter = null;
	private ImageView okButtonView;
	
	private int curFocusCategoryIdx = -1;
	private int curFocusPicIdx = -1;
	private View lastFocusView = null;
	
	private boolean fullImageShow = false;
	
	private Timer autoImageChangeTimer = null;
	private boolean isInAutoImageChange = false;
	private final int autoImageChangePeriod = 2000; // 3 seconds
	
	private long lastImageChangeTime = 0;
	private long lastKeyEvent = 0;
	
	private int lastFocusCategoryIdx = -1;
	private int lastFocusPicIdx = -1;
	private Timer checkLastKeyTimer = null;  //屏蔽一些过多按键时，用于判断是否最后一次按键
	private boolean  checkLastKeyTimerRun = false;
	public Calendar begin = null;

	
	
	public class Picture {
		String url;
		String facility;
		String introduce = "";
		int id;
	}
	
	public class PictureCategory {
		public String name;
		public String nameEng;
		ArrayList<Picture> picList = new ArrayList<Picture>();
	}
	
	ArrayList<PictureCategory> picCategoryList = new ArrayList<PictureCategory>();
	
	/* data provider */
	/* build the view layout/element */
	/* start animation */
	public void init(Context ctx, String u) {
		begin = Calendar.getInstance();
		context = ctx;
		url = u;
		view = LayoutInflater.from(context).inflate(R.layout.picture_list_view, null);
		initLayoutInXml();
		
		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(DataJsonListen);
		mr.execute(url);
	}
	
	public void init(Context ctx, String u, LinearLayout layout) {
		begin = Calendar.getInstance();
		context = ctx;
		url = u;
		menuLinearLayout = layout;
		view = LayoutInflater.from(context).inflate(R.layout.picture_list_view, null);
		initLayoutInXml();
		
		Log.i("Leo test", "url " + url);
		
		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(DataJsonListen);
		mr.execute(url);
	}
	
	private void initLayoutInXml() {
		// TODO Auto-generated method stub
		pictureView = (ImageView) view.findViewById(R.id.pictureView);
		pictureTitleText = (TextView) view.findViewById(R.id.pictureTitleText);
		pictureCategoryListView = (ListView)view.findViewById(R.id.pictureCategoryList);
		leftArrow = (ImageView)view.findViewById(R.id.leftArrow);
		rightArrow = (ImageView)view.findViewById(R.id.rightArrow);
		picture_list_foreground = view.findViewById(R.id.picture_list_foreground);
		introduceText = (TextView) view.findViewById(R.id.pictureListViewText);
		pictureListTextIntroduceFL = (FrameLayout) view.findViewById(R.id.pictureListTextIntroduceFL);
		okButtonView = (ImageView) view.findViewById(R.id.bottom_notice);
		if(ClearConfig.LanguageID == 1){
			okButtonView.setImageResource(R.drawable.top_recommend_command);
		}else{
			okButtonView.setImageResource(R.drawable.top_recommend_command_eng);
		}
		//VoDViewManager.getInstance().stopBackgroundVideo();
	}
	
	/**
	 * Json获取完成监听
	 */
	private OnCompleteListener DataJsonListen = new OnCompleteListener(){

		@Override
		public void onDownloaded(Object result) {
			// TODO Auto-generated method stub
			dataJson = (String)result;
			if(dataJson == null){
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
				JSONTokener jsonParser = new JSONTokener(dataJson);  
			    JSONObject mainViewObj = (JSONObject) jsonParser.nextValue();  
			    JSONArray contentArray = (JSONArray)mainViewObj.getJSONArray("Content");
			    String introduceCHN = null;
			    String introduceENG = null;
			    String nameCHN = null;
			    String nameENG = null;

			    for(int i = 0; i < contentArray.length(); i ++){
			    	JSONObject objecttmp = (JSONObject)contentArray.opt(i);
			    	
			    	PictureCategory pc = new PictureCategory();
			    	nameCHN = objecttmp.getString("Name");
			    	nameENG = objecttmp.getString("NameEng");
			    	pc.name = ClearConfig.getStringByLanguageId(nameCHN,nameENG);
			    	Log.i("Leo test", "name " + pc.name);
			    	
			    	JSONArray SubContent = (JSONArray)objecttmp.getJSONArray("SubContent");
			    	for(int j = 0; j < SubContent.length(); j ++){
			    		JSONObject picTmp = (JSONObject)SubContent.opt(j);
			    		Picture p = new Picture();
			    		p.url = ClearConfig.getJsonUrl(context, picTmp.getString("Picurl"));
			    		p.facility = picTmp.getString("Facility");
			    		p.id = picTmp.getInt("id");
			    		introduceCHN = picTmp.getString("Introduce");
			    		introduceENG = picTmp.getString("IntroduceEng");
			    		p.introduce = ClearConfig.getStringByLanguageId(introduceCHN,introduceENG);
			    		Log.i("Leo test", "url " + p.url);
			    		pc.picList.add(p);
			    	}
			    	
			    	picCategoryList.add(pc);
			    }
			    
			    if(begin != null) {
                	long between = (Calendar.getInstance()).getTimeInMillis()-begin.getTimeInMillis(); 
                    ClearLog.LogInfo("BROSWER\tLoad\tSUCC\t" + between +"ms\t" + url + "\t"
                    		+ "picturListView");
                } 
			}catch (JSONException e) {
				ClearLog.LogError("BROSWER\tLoad\tFAIL\t0ms\t" + url);
				e.printStackTrace();
			}
			
			pictureTitleText.setText(nameInIcon);
			
			PictureCategoryListAdapter picAdapter = new PictureCategoryListAdapter(
					context, picCategoryList);
			pictureCategoryListView.setAdapter(picAdapter);
			pictureCategoryListView.requestFocus();
			
			pictureCategoryListView.setOnItemSelectedListener(itemFocusListner);
			pictureCategoryListView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					onKeyEnter();
				}
			});
		}

		@Override
		public void onComplete(boolean result) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	OnItemSelectedListener itemFocusListner = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			// TODO Auto-generated method stub
			Log.i("Leo test", arg1 + " " + arg2 + " "  + arg3 );
			if(lastFocusView != null) {
				((ImageView)(lastFocusView.findViewById(R.id.item_focus))).setVisibility(View.INVISIBLE);
				ImageView iv2 = (ImageView)lastFocusView.findViewById(R.id.pic_list_item_selected_pic);
				iv2.setImageResource(R.drawable.empty_circle);
				
				TextView tv = (TextView)lastFocusView.findViewById(R.id.pic_list_item_index);
				tv.setVisibility(View.INVISIBLE);
			}
			
			lastFocusView = arg1;
			
			ImageView iv = (ImageView)arg1.findViewById(R.id.item_focus);
			iv.setVisibility(View.VISIBLE);
			
			ImageView iv2 = (ImageView)arg1.findViewById(R.id.pic_list_item_selected_pic);
			iv2.setImageResource(R.drawable.ordering_detail_focus_bg);
			
			curFocusCategoryIdx = arg2;
			curFocusPicIdx = 0;
			updateCurPicIdx();
			
			/* 启动定时器自动切换图片 */
			if(isInAutoImageChange && autoImageChangeTimer != null) {
				stopAutoImageChangeTimer();
			}
			
			autoImageChangeTimer = new Timer();
			autoImageChangeTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					Message message = new Message();
					message.what = 0;
					handler.sendMessage(message);
				}
			}, 
			autoImageChangePeriod, 
			autoImageChangePeriod);
			isInAutoImageChange = true;
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	private void stopAutoImageChangeTimer() {
		if(autoImageChangeTimer != null) {
			autoImageChangeTimer.cancel();
			isInAutoImageChange = false;
		}
			
		Message message = new Message();
		message.what = 1;
		handler.sendMessage(message);
	}
	
	/* 自动轮播图片 */
	Handler handler = new Handler() {
		int fade = 0;
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what) {
			case 0:
				fade ++;
				if(fade == 4) {
					pictureView.animate()
					.alpha(0.3f)
					.setDuration(2000)
					.start();
					return ;
				}
				
				if(fade == 5) {
					if(curFocusCategoryIdx >= 0 && curFocusCategoryIdx < picCategoryList.size()) {
						if(picCategoryList.get(curFocusCategoryIdx).picList.size() > 0) {
							curFocusPicIdx = (curFocusPicIdx + 1) % 
									picCategoryList.get(curFocusCategoryIdx).picList.size();
							updateCurPicIdx();
						}
					}
					fade = 0;
				}
				break;
			case 1:
				fade = 0;
				break;
			}
		}
	};
	
	TimerTask checkLastKeyTimerTask = new TimerTask() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			long curTime = System.currentTimeMillis();
			if(curTime - lastKeyEvent > 1000 && curTime - lastKeyEvent < 2000){
				if(lastFocusCategoryIdx != curFocusCategoryIdx ||
					lastFocusPicIdx != curFocusPicIdx) {
					
					if(curFocusCategoryIdx >= 0 && curFocusCategoryIdx < picCategoryList.size()) {
						if(lastFocusView != null && curFocusPicIdx >= 0) {
							MaterialRequest mr = new MaterialRequest(context, pictureView, ClearConfig.TYPE_IMAGE);
							mr.execute(picCategoryList.get(curFocusCategoryIdx).picList.get(curFocusPicIdx).url);
							
							lastFocusCategoryIdx = curFocusCategoryIdx;
							lastFocusPicIdx = curFocusPicIdx;
							lastImageChangeTime = curTime;
							if(picCategoryList.get(curFocusCategoryIdx).picList.get(curFocusPicIdx).introduce.equals("")) {
								//set invisiable
								pictureListTextIntroduceFL.post(new Runnable() {
									@Override
									public void run() {
										// TODO Auto-generated method stub
										pictureListTextIntroduceFL.setVisibility(View.INVISIBLE);
									}
								});
								
							}
							else{
								//
								pictureListTextIntroduceFL.post(new Runnable() {
									@Override
									public void run() {
										// TODO Auto-generated method stub
										pictureListTextIntroduceFL.setVisibility(View.VISIBLE);
										introduceText.setText(picCategoryList.get(curFocusCategoryIdx).picList.get(curFocusPicIdx).introduce);
										
									}
								});
							}
						}
					}
				}
			}
		}
	};
	
	private void updateCurPicIdx() {
		if(curFocusCategoryIdx >= 0 && curFocusCategoryIdx < picCategoryList.size()) {
			if(lastFocusView != null && curFocusPicIdx >= 0) {
				TextView tv = (TextView)lastFocusView.findViewById(R.id.pic_list_item_index);
				tv.setText("(" + (curFocusPicIdx + 1) + "/" +
						picCategoryList.get(curFocusCategoryIdx).picList.size() + ")");
				tv.setTextColor(Color.rgb(255, 255, 255));
				tv.setVisibility(View.VISIBLE);
				
				long curTime = System.currentTimeMillis();
				lastKeyEvent = curTime;
				if(curTime - lastImageChangeTime > 800){
					MaterialRequest mr = new MaterialRequest(context, pictureView, ClearConfig.TYPE_IMAGE);
					mr.execute(picCategoryList.get(curFocusCategoryIdx).picList.get(curFocusPicIdx).url);
					
					lastFocusCategoryIdx = curFocusCategoryIdx;
					lastFocusPicIdx = curFocusPicIdx;
					lastImageChangeTime = curTime;
					
					pictureView.animate()
					.alpha(1.0f)
					.setDuration(2000)
					.start();
					
					if(picCategoryList.get(curFocusCategoryIdx).picList.get(curFocusPicIdx).introduce.equals("")) {
						//set invisiable
						pictureListTextIntroduceFL.post(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								pictureListTextIntroduceFL.setVisibility(View.INVISIBLE);
							}
						});
						
					}
					else{
						//
						pictureListTextIntroduceFL.post(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								pictureListTextIntroduceFL.setVisibility(View.VISIBLE);
								introduceText.setText(picCategoryList.get(curFocusCategoryIdx).picList.get(curFocusPicIdx).introduce);
								
							}
						});
					}
				}
				else {
					if(checkLastKeyTimerRun == false) {
						checkLastKeyTimer = new Timer();
						checkLastKeyTimer.schedule(checkLastKeyTimerTask,
								1000, 1000);
						checkLastKeyTimerRun = true;
					}
				}
				
				if(picCategoryList.get(curFocusCategoryIdx).picList.size() > 1){
					leftArrow.setVisibility(View.VISIBLE);
					rightArrow.setVisibility(View.VISIBLE);
				}
			}
		}
	}
	
	public boolean onKeyDpadLeft() {
		Log.i("Leo test", "key left " + curFocusPicIdx);
		
		if(isInAutoImageChange) {
			stopAutoImageChangeTimer();
		}
		
		if(curFocusCategoryIdx >= 0 && curFocusCategoryIdx < picCategoryList.size()) {
			if(picCategoryList.get(curFocusCategoryIdx).picList.size() > 0) {
				curFocusPicIdx = (curFocusPicIdx + picCategoryList.get(curFocusCategoryIdx).picList.size() - 1) % 
						picCategoryList.get(curFocusCategoryIdx).picList.size();
				updateCurPicIdx();
			}
		}
		
		return true;
	}
	
	public boolean onKeyDpadRight() {
		Log.i("Leo test", "key right " + curFocusPicIdx);
		
		if(isInAutoImageChange) {
			stopAutoImageChangeTimer();
		}
		
		if(curFocusCategoryIdx >= 0 && curFocusCategoryIdx < picCategoryList.size()) {
			if(picCategoryList.get(curFocusCategoryIdx).picList.size() > 0) {
				curFocusPicIdx = (curFocusPicIdx + 1) % 
							picCategoryList.get(curFocusCategoryIdx).picList.size();			
				updateCurPicIdx();
			}
		}
		return true;
	}
	
	public boolean onKeyEnter() {
		Log.i("Leo test", "key eneter");
		if(fullImageShow) {
			//restore
			picture_list_foreground.setVisibility(View.VISIBLE);
			fullImageShow = false;
		}
		else {
			//hide
			picture_list_foreground.setVisibility(View.INVISIBLE);
			fullImageShow = true;
		}
		
		return true;
	}
	
	public boolean onKeyBack() {
		if(fullImageShow) {
			picture_list_foreground.setVisibility(View.VISIBLE);
			fullImageShow = false;
			return true;
		}
		
		if(isInAutoImageChange) {
			stopAutoImageChangeTimer();
		}
		
		if(checkLastKeyTimerRun && checkLastKeyTimer != null) {
			checkLastKeyTimer.cancel();
		}
		
		//VoDViewManager.getInstance().playBackgroundVideo();
		VoDViewManager.getInstance().popForegroundView();
		return true;
	}
}
