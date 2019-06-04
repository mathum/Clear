/**
 * @author xujifu
 * @copyright clear
 * @date 2014-06-20
 * @description 消息界面
 */
package com.clearcrane.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.pushmessage.FloatViewService;
import com.clearcrane.util.MessageAdapter;
import com.clearcrane.vod.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class VodMessageView extends VoDBaseView {
	
	private ArrayList<MessageInfo> message = new ArrayList<MessageInfo>();
	private String msgJson;
	private int curFocusIndex = -1;
	private long lastTime = 0;
	public final String MessageFile = "messageFile";
	public SharedPreferences sharePre;
	
	private ListView msgList;
	private MessageAdapter adapter;
	private Animation titleAnim;
	
	private LinearLayout mMsgLinearLayout;
	
	private RelativeLayout mTitleLayout;
	private ImageView msgTitlePic;
	private TextView msgTitleName;
	private TextView msgName;
	private TextView msgCurFocus;//当前选择的是第几条
	private LinearLayout lastFocus = null;
	private ImageView baseline;
	private ImageView msgSelect;
	public boolean allReaded = false;
	
	String TAG = "VodMessageView";
	
	public class MessageInfo {
		public int Versionid;
		public String title;
		public String time;
		public String message;
		public boolean isRead = false;
		public LinearLayout msgLayout;
	}
	
	/* data provider */
	/* build the view layout/element */
	/* start animation */
	public void init(Context ctx, String u){
		super.init(ctx, u);
		
		url = u;
		view = LayoutInflater.from(context).inflate(R.layout.message_view, null);
		initLayoutInXml();
		
	}
	
	public void init(Context ctx, String u, LinearLayout layout){
		super.init(ctx, u, layout);
		
		url = u;
		view = LayoutInflater.from(context).inflate(R.layout.message_view, null);
		initLayoutInXml();
		sharePre = ctx.getSharedPreferences(ClearConstant.MESSAGE_FILE, Context.MODE_PRIVATE);
		readMessage();
		checkStat();
	}
	
	public void readMessage(){
		String messageJson = sharePre.getString(ClearConstant.MESSAGE_JSON, null);
		if(messageJson == null){
			return;
		}
		try {
			JSONObject jObject = new JSONObject(messageJson);
			JSONArray array = jObject.getJSONArray("messages");
			if(array.length() > 0){
				for(int i = 0; i < array.length(); i++){
					MessageInfo messageinfo = new MessageInfo();
					messageinfo.Versionid = array.getJSONObject(i).getInt("versionid");
					messageinfo.isRead = sharePre.getBoolean("versionid"+messageinfo.Versionid, false);
					JSONObject messageObj = array.getJSONObject(i).getJSONObject("commandContent");
			    	messageinfo.time = messageObj.getString("createtime");
			    	messageinfo.message = messageObj.getString("content");
					message.add(messageinfo);
				}
				adapter = new MessageAdapter(context, message);
			    msgList.setAdapter(adapter);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	private AnimationListener titleListener = new AnimationListener(){
		
		@Override
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			// TODO Auto-generated method stub
			msgList.setVisibility(View.VISIBLE);
			msgName.setVisibility(View.VISIBLE);
			baseline.setVisibility(View.VISIBLE);
			msgSelect.setVisibility(View.VISIBLE);
			msgCurFocus.setVisibility(View.VISIBLE);
			
			baseline.animate()
				.scaleX(1.0f)
				.setDuration(300)
				.start();
			msgList.requestFocus();
		}
	};
	
	private OnItemSelectedListener msgSelectListener =  new OnItemSelectedListener(){

		@Override
		public void onItemSelected(AdapterView<?> adapter, View view,
				int position, long id) {
			// TODO Auto-generated method stub
			((TextView)view.findViewById(R.id.message_item_title)).setTypeface(null, Typeface.BOLD);
			((TextView)view.findViewById(R.id.message_item_msg)).setTypeface(null, Typeface.BOLD);
			message.get(position).isRead = true;
			Editor editor = sharePre.edit();
			editor.putBoolean("versionid"+message.get(position).Versionid, true);
			editor.commit();
			((ImageView)view.findViewById(R.id.message_item_status)).setImageResource(R.drawable.msg_open);
			//((TextView)view.findViewById(R.id.message_item_msg)).setTextSize(25);
			//((TextView)view.findViewById(R.id.message_item_title)).setTextColor(Color.YELLOW);
			if(lastFocus != null && lastFocus != view){
				((TextView)lastFocus.findViewById(R.id.message_item_msg)).setTypeface(null, Typeface.NORMAL);
				((TextView)lastFocus.findViewById(R.id.message_item_title)).setTypeface(null, Typeface.NORMAL);
			}
			
			//msgCurFocus.setText("第" + (position + 1) + "条/共" + message.size() + "条");
			msgCurFocus.setText((position + 1) + "/" + message.size());
			lastFocus = (LinearLayout) view;
			checkStat();
		}

		@Override
		public void onNothingSelected(AdapterView<?> adapter) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	public synchronized void checkStat(){
		if(allReaded)
			return;
		Log.i(TAG,"checkstat");
		allReaded = true;
		for(int i = 0;i < message.size(); i++){
			if(message.get(i).isRead == false){
				allReaded = false;
				return;
			}
		}
		if(allReaded){
			FloatViewService.actionHideMessage(context);
		}
	}
	
	private void initLayoutInXml(){
		msgList = (ListView)view.findViewById(R.id.message_context);
		mTitleLayout = (RelativeLayout)view.findViewById(R.id.message_title);
		msgTitlePic = (ImageView)view.findViewById(R.id.message_title_pic);
		//msgTitleName = (TextView)view.findViewById(R.id.message_title_name);
		msgName = (TextView)view.findViewById(R.id.message_title_msgname);
		msgCurFocus = (TextView)view.findViewById(R.id.message_curFocus);
		baseline = (ImageView)view.findViewById(R.id.message_baseline);
		msgSelect = (ImageView)view.findViewById(R.id.message_select);
		
		titleAnim = AnimationUtils.loadAnimation(context, R.anim.message_title_anim);
		
		ImageView iconView = ((ImageView)menuLinearLayout.
				findViewById(R.id.sub_pic_text_wrapcontent_pic));
		iconView.setDrawingCacheEnabled(true);  
		msgTitlePic.setImageBitmap(
				Bitmap.createBitmap(iconView.getDrawingCache()));
		iconView.setDrawingCacheEnabled(false);
		//msgTitleName.setText(((TextView)menuLinearLayout.
				//findViewById(R.id.sub_pic_text_wrapcontent_text)).getText());
		msgName.setText(((TextView)menuLinearLayout.
				findViewById(R.id.sub_pic_text_wrapcontent_text)).getText());
		msgList.setVisibility(View.GONE);
		msgName.setVisibility(View.GONE);
		baseline.setVisibility(View.GONE);
		msgSelect.setVisibility(View.GONE);
		msgCurFocus.setVisibility(View.GONE);
		
		titleAnim.setAnimationListener(titleListener);
		mTitleLayout.startAnimation(titleAnim);
		
		msgList.setOnItemSelectedListener(msgSelectListener);
	}
	
}
