/**
 * @author xujifu
 * @copyright clear
 * @data 2014-06-17
 * @description 账单界面
 */
package com.clearcrane.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clearcrane.provider.MaterialRequest;
import com.clearcrane.provider.MaterialRequest.OnCompleteListener;
import com.clearcrane.util.BillingAdapter;
import com.clearcrane.util.BillingDetailsInfo;
import com.clearcrane.util.BillingInfo;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.TipDialog;
import com.clearcrane.vod.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;

public class VodBillingInquiriesView extends VoDBaseView {
	
	private String billingJson = null;
	private ArrayList<BillingInfo> billing = new ArrayList<BillingInfo>();
	private ListView billingList;
	private BillingAdapter adapter;
	
	private OnCompleteListener BillingJsonListener = new OnCompleteListener(){

		@Override
		public void onDownloaded(Object result) {
			// TODO Auto-generated method stub
			billingJson = (String)result;
			if(billingJson == null){
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
				
				JSONTokener jsonParser = new JSONTokener(billingJson); 
				//L.i(billingJson);
			    JSONObject mainViewObj = (JSONObject) jsonParser.nextValue();  
			    JSONArray contentArray = (JSONArray)mainViewObj.getJSONArray("Content");
			    for(int i = 0; i < contentArray.length(); i ++){
			    	JSONObject objecttmp = (JSONObject)contentArray.opt(i);
			    	BillingInfo billinginfo = new BillingInfo();
			    	billinginfo.title = objecttmp.getString("Name");
			    	billinginfo.time = objecttmp.getString("Time");
			    	if(objecttmp.has("Second")){
			    		JSONArray secondArray = objecttmp.getJSONObject("Second").getJSONArray("Content");
			    		for(int j = 0; j < secondArray.length(); j ++){
			    			JSONObject secondtmp = (JSONObject)secondArray.opt(j);
			    			BillingDetailsInfo detialsinfo = new BillingDetailsInfo();
			    			detialsinfo.title = secondtmp.getString("Name");
			    			detialsinfo.count = secondtmp.getString("Count");
			    			detialsinfo.time = secondtmp.getString("Time");
			    			detialsinfo.price = secondtmp.getString("Price");
			    			billinginfo.details.add(detialsinfo);
			    		}
			    	}
			    	
			    	billing.add(billinginfo);
			    }
			    adapter = new BillingAdapter(context, billing);
			    billingList.setAdapter(adapter);
			    int total = 0;
			    for(int i = 0; i < billing.size(); i ++){
			    	for(int j = 0; j < billing.get(i).details.size(); j ++){
			    		total += Integer.parseInt(billing.get(i).details.get(j).price);
			    	}
			    }
			    billingTotal.setText(context.getString(R.string.billingtotal) +
			    		context.getString(R.string.mark) + total);
			}catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onComplete(boolean result) {
			// TODO Auto-generated method stub
			
		}
		
	};

	public void init(Context ctx, String u){
		super.init(ctx, u);
		
		url = u;
		view = LayoutInflater.from(context).inflate(R.layout.billing_view, null);
		
		initLayoutInXml();
		/* trigger to get data sources */
		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(BillingJsonListener);
		mr.execute(url);
	}
	
	public void init(Context ctx, String u, LinearLayout layout){
		super.init(ctx, u, layout);
		
		url = u;
		view = LayoutInflater.from(context).inflate(R.layout.billing_view, null);
		
		//VoDViewManager.getInstance().startBackgroundVideo("http://192.168.18.249/nativevod/movie/starrynight01.mp4");
		initLayoutInXml();
		/* trigger to get data sources */
		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(BillingJsonListener);
		mr.execute(url);
	}
	private RelativeLayout billingTitle;
	private LinearLayout billingTitleLeft;
	private ImageView billingTitlePic, baseline, select;
	private TextView billingTitleName, billingName, billingTotal;
	private Animation titleAnim;
	private AnimationListener billingListener = new AnimationListener() {
		
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
			billingList.setVisibility(View.VISIBLE);
			baseline.setVisibility(View.VISIBLE);
			select.setVisibility(View.VISIBLE);
			billingTotal.setVisibility(View.VISIBLE);
			billingName.setVisibility(View.VISIBLE);
			
			baseline.animate()
				.scaleX(1.0f)
				.setDuration(300)
				.start();
			billingList.requestFocus();
		}
	};

	private void initLayoutInXml() {
		// TODO Auto-generated method stub
		billingList = (ListView)view.findViewById(R.id.billing_context);
		billingTitle = (RelativeLayout)view.findViewById(R.id.billing_title);
		billingTitleLeft = (LinearLayout)view.findViewById(R.id.billing_title_left);
		billingTitlePic = (ImageView)view.findViewById(R.id.billing_title_pic);
		baseline = (ImageView)view.findViewById(R.id.billing_baseline);
		select = (ImageView)view.findViewById(R.id.billing_select);
		billingTitleName = (TextView)view.findViewById(R.id.billing_title_name);
		billingName = (TextView)view.findViewById(R.id.billing_name);
		billingTotal = (TextView)view.findViewById(R.id.billing_total);
		
		titleAnim = AnimationUtils.loadAnimation(context, R.anim.billing_title_anim);
		
		billingList.setVisibility(View.GONE);
		baseline.setVisibility(View.GONE);
		select.setVisibility(View.GONE);
		billingTotal.setVisibility(View.GONE);
		billingName.setVisibility(View.GONE);
		
		ImageView iconView = ((ImageView)menuLinearLayout.
				findViewById(R.id.sub_pic_text_wrapcontent_pic));
		iconView.setDrawingCacheEnabled(true);  
		billingTitlePic.setImageBitmap(
				Bitmap.createBitmap(iconView.getDrawingCache()));
		iconView.setDrawingCacheEnabled(false);
		billingTitleName.setText(((TextView)menuLinearLayout.
				findViewById(R.id.sub_pic_text_wrapcontent_text)).getText());
		
		titleAnim.setAnimationListener(billingListener);
		billingTitle.startAnimation(titleAnim);
	}
	
}
