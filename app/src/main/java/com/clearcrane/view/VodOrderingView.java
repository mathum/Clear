/**
 * @author xujifu
 * @copyright clear
 * @date 2014-06-20
 * @description 订餐界面
 */
package com.clearcrane.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.util.ArrayList;

public class VodOrderingView extends VoDBaseView {
	
	private String orderingJson;
	private ArrayList<FoodInfo> food = new ArrayList<FoodInfo>();
	private ArrayList<DetailsInfo> details = new ArrayList<DetailsInfo>();
	
	private LinearLayout mLinearLayout, mFoodPicLinearLayout, mDetailsLayout, 
						 mShopCarLayout, detailslistLayout;;
	private ImageView orderingTitlePic;
	private TextView orderingTitleName;
	private TextView foodName, foodFocus;
	private TextView orderingCarCount;//当前所有选择物品的数量
	private TextView orderTotal;//当前所有选择物品的总价
	private ImageView baseLine, select;
	private ImageView detailbaseline;
	private RelativeLayout mRelativeLayout;
	private Animation titleAnim;
	private int curFocusIndex = -1;
	private int subFocusIndex = -1;
	private int FocusPos = 0;
	private long lastTime = 0;
	
	private boolean isLoaded = false;
	
	class DetailsInfo{
		String name;
		int price;
		int count;
		RelativeLayout detailsLayout;
	}
	
	class FoodInfo{
		String name;
		String picurl;
		String price;
		boolean isDownload = false;
		LinearLayout picLayout;
	}

	public void init(Context ctx, String u){
		super.init(ctx, u);
		view = LayoutInflater.from(context).inflate(R.layout.ordering_view, null);
		initLayoutInXml();
		url = u;
		/* trigger to get data sources */
		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(OrderingJsonListen);
		mr.execute(url);
	}
	
	public void init(Context ctx, String u, LinearLayout layout){
		super.init(ctx, u, layout);
		view = LayoutInflater.from(context).inflate(R.layout.ordering_view, null);
		initLayoutInXml();
		url = u;
		/* trigger to get data sources */
		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(OrderingJsonListen);
		mr.execute(url);
	}
	
	private OnCompleteListener OrderingJsonListen = new OnCompleteListener(){

		@Override
		public void onDownloaded(Object result) {
			// TODO Auto-generated method stub
			orderingJson = (String)result;
			if(orderingJson == null){
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
				JSONTokener jsonParser = new JSONTokener(orderingJson);  
			    JSONObject mainViewObj = (JSONObject) jsonParser.nextValue();  
			    JSONArray contentArray = (JSONArray)mainViewObj.getJSONArray("Content");
			    for(int i = 0; i < contentArray.length(); i ++){
			    	JSONObject objecttmp = (JSONObject)contentArray.opt(i);
			    	FoodInfo foodinfo = new FoodInfo();
			    	foodinfo.name = objecttmp.getString("Name");
			    	foodinfo.picurl = objecttmp.getString("Picurl");
			    	foodinfo.price = objecttmp.getString("Price");
			    	
			    	foodinfo.picLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.ordering_food_item, null);
			    	ImageView iv = (ImageView)foodinfo.picLayout.findViewById(R.id.ordering_food_item_pic);
			    	MaterialRequest mr = new MaterialRequest(context, iv, ClearConfig.TYPE_IMAGE);
			    	if(i < 8)
			    		mr.setOnCompleteListener(new OnFoodPicCompleteListerner(foodinfo));
					mr.execute(foodinfo.picurl);
					ImageView ivborder = (ImageView)foodinfo.picLayout.findViewById(R.id.ordering_food_item_border);
					ivborder.setAlpha(0.0f);
			    	food.add(foodinfo);
			    }
			    if(!food.isEmpty()){
			    	foodName.setText(food.get(0).name + "(" + context.getString(R.string.mark) + food.get(0).price + ")");
			    	
					((ImageView)food.get(0).picLayout.findViewById(R.id.ordering_food_item_border)).setAlpha(1.0f);
					((ImageView)food.get(0).picLayout.findViewById(R.id.ordering_food_item_background)).setAlpha(0.0f);
					if(curFocusIndex < 0){
						//first focus
						onFocusChanged(-1, 0);
						curFocusIndex = 0;
						foodFocus.setText("" + (curFocusIndex + 1) + "/" + food.size());
					}
					
			    }
			}catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onComplete(boolean result) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	/* has to set text after image download completed */
	class OnFoodPicCompleteListerner implements OnCompleteListener {
		FoodInfo foodinfo;
		
		public OnFoodPicCompleteListerner(FoodInfo info) {
			foodinfo = info;
		}

		@Override
		public void onDownloaded(Object result) {
		}

		@Override
		public void onComplete(boolean result) {
			foodinfo.isDownload = true;
			/* check if all image downloaded, if so, show it */
			boolean allDownloaded = true;
			for(int i = 0; i < food.size() && i < 8; i++) {
				if(food.get(i).isDownload == false) {
					allDownloaded = false;
					break;
				}
			}
			
			if(allDownloaded) {
				mShopCarLayout.setVisibility(View.VISIBLE);
				orderingTitlePic.setVisibility(View.VISIBLE);
				orderingTitleName.setVisibility(View.VISIBLE);
				foodName.setVisibility(View.VISIBLE);
				foodFocus.setVisibility(View.VISIBLE);
				baseLine.setVisibility(View.VISIBLE);
				select.setVisibility(View.VISIBLE);
				detailbaseline.setVisibility(View.VISIBLE);
				mDetailsLayout.setVisibility(View.VISIBLE);
				detailslistLayout.setVisibility(View.VISIBLE);
				baseLine.animate()
				.scaleX(1.0f)
				.setDuration(300)
				.start();
				mFoodPicLinearLayout.setVisibility(View.VISIBLE);
				for(int i = 0; i < food.size(); i++){
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT, 
							LinearLayout.LayoutParams.WRAP_CONTENT);
					mFoodPicLinearLayout.addView(food.get(i).picLayout, lp);
					TranslateAnimation transanim = new TranslateAnimation(-10, 0, 10, 0);
					AlphaAnimation alphaanim = new AlphaAnimation(0.0f, 1.0f);
					//ScaleAnimation scaleanim = new ScaleAnimation(0.9f, 1.0f, 0.9f, 1.0f);
					AnimationSet set = new AnimationSet(true);
					set.addAnimation(transanim);
					set.addAnimation(alphaanim);
					//set.addAnimation(scaleanim);
					set.setDuration(300);
					set.setStartOffset(100 * i);
					food.get(i).picLayout.startAnimation(set);
				}
				isLoaded = true;
			}
		}
	}

	private void initLayoutInXml() {
		// TODO Auto-generated method stub
		mLinearLayout = (LinearLayout)view.findViewById(R.id.ordering_title_left);
		mFoodPicLinearLayout = (LinearLayout)view.findViewById(R.id.ordering_food_pic);
		mRelativeLayout = (RelativeLayout)view.findViewById(R.id.ordering_title);
		mShopCarLayout = (LinearLayout)view.findViewById(R.id.ordering_shopcar);
		orderingTitlePic = (ImageView)view.findViewById(R.id.ordering_title_pic);
		orderingTitleName = (TextView)view.findViewById(R.id.ordering_title_name);
		foodName = (TextView)view.findViewById(R.id.ordering_title_foodname);
		foodFocus = (TextView)view.findViewById(R.id.ordering_curFocus);
		baseLine = (ImageView)view.findViewById(R.id.ordering_baseline);
		select = (ImageView)view.findViewById(R.id.ordering_select);
		orderingCarCount = (TextView)view.findViewById(R.id.ordering_car_count);
		orderTotal = (TextView)view.findViewById(R.id.ordering_total);
		detailbaseline = (ImageView)view.findViewById(R.id.ordering_detail_baseline);
		mDetailsLayout = (LinearLayout)view.findViewById(R.id.ordering_details);
		detailslistLayout = (LinearLayout)view.findViewById(R.id.ordering_details_list);
		
		titleAnim = AnimationUtils.loadAnimation(context, R.anim.ordering_title_anim);
		
		ImageView iconView = ((ImageView)menuLinearLayout.
				findViewById(R.id.sub_pic_text_wrapcontent_pic));
		iconView.setDrawingCacheEnabled(true);  
		orderingTitlePic.setImageBitmap(
				Bitmap.createBitmap(iconView.getDrawingCache()));
		iconView.setDrawingCacheEnabled(false);
		orderingTitleName.setText(((TextView)menuLinearLayout.
				findViewById(R.id.sub_pic_text_wrapcontent_text)).getText());
		
		mFoodPicLinearLayout.setVisibility(View.GONE);
		mShopCarLayout.setVisibility(View.GONE);
		foodName.setVisibility(View.GONE);
		foodFocus.setVisibility(View.GONE);
		baseLine.setVisibility(View.GONE);
		select.setVisibility(View.GONE);
		detailbaseline.setVisibility(View.GONE);
		mDetailsLayout.setVisibility(View.GONE);
		detailslistLayout.setVisibility(View.GONE);
		
		mRelativeLayout.startAnimation(titleAnim);
	}
	
	private void onFocusChanged(int oldIndex, int newIndex) {
		// TODO Auto-generated method stub
		if(oldIndex >= 0){
			((ImageView)food.get(oldIndex).picLayout.findViewById(R.id.ordering_food_item_border))
				.animate()
				.alpha(0.0f)
				.setDuration(200)
				.start();
			((ImageView)food.get(oldIndex).picLayout.findViewById(R.id.ordering_food_item_background))
				.animate()
				.alpha(1.0f)
				.setDuration(200)
				.start();;
		}
		foodName.setText(food.get(newIndex).name + "(" + context.getString(R.string.mark) + food.get(newIndex).price + ")");
		
		((ImageView)food.get(newIndex).picLayout.findViewById(R.id.ordering_food_item_border))
			.animate()
			.alpha(1.0f)
			.setDuration(200)
			.start();
		((ImageView)food.get(newIndex).picLayout.findViewById(R.id.ordering_food_item_background))
			.animate()
			.alpha(0.0f)
			.setDuration(200)
			.start();
		foodFocus.setText("" + (newIndex + 1) + "/" + food.size());
	}
	private int scrollpos = 0;
	private void onSubFocusChange(int oldPos, int newPos) {
		// TODO Auto-generated method stub
		if(oldPos >= 0) {
			//修改子菜单项的上一焦点动画
			((ImageView)details.get(oldPos).detailsLayout.findViewById(R.id.ordering_details_focus))
				.setVisibility(View.INVISIBLE);
		}
		if(newPos >= 0 && !details.isEmpty()){
		//make the new focus icon, add its focus image
			((ImageView)details.get(newPos).detailsLayout.findViewById(R.id.ordering_details_focus))
				.setVisibility(View.VISIBLE);
			if(scrollpos == 4 && scrollpos < newPos){
				mDetailsLayout.animate()
					.translationYBy(details.get(oldPos).detailsLayout.getY() 
							- details.get(newPos).detailsLayout.getY())
					.start();
			}else if(scrollpos == 0 && scrollpos < newPos){
				mDetailsLayout.animate()
					.translationYBy(details.get(oldPos).detailsLayout.getY() 
							- details.get(newPos).detailsLayout.getY())
					.start();
			}
		}
	}
	
	public boolean onKeyDpadLeft(){
		long last = System.currentTimeMillis();
		if(last - lastTime < 300){
			return true;
		}
		lastTime = last;
		if(isLoaded == false){
			return true;
		}
		if(FocusPos == 2){
			if(details.get(subFocusIndex).count == 1){
				((TextView)details.get(subFocusIndex).detailsLayout.findViewById(R.id.numpick_four)).
					setText("X");
			}else{
				((TextView)details.get(subFocusIndex).detailsLayout.findViewById(R.id.numpick_four)).
					setText("" + (Integer.parseInt(((TextView)details.get(subFocusIndex).
							detailsLayout.findViewById(R.id.numpick_four)).getText().toString()) - 1));
				details.get(subFocusIndex).count --;
			}
			return true;
		}
		if(subFocusIndex >= 0)
			return false;
		if(0 == curFocusIndex){
			return true;
		}
		int newFocusIndex = -1;
		if(curFocusIndex < 0){
			newFocusIndex = 0;
		}else{
			if(food.size() > 1)
				mFoodPicLinearLayout.animate()
					.translationXBy(food.get((1) % food.size()).picLayout.getX()
							- food.get(0).picLayout.getX())
					.setDuration(300)
					.start();
			newFocusIndex = (curFocusIndex + food.size() - 1) % food.size();
		}
		
		onFocusChanged(curFocusIndex, newFocusIndex);
		curFocusIndex = newFocusIndex;
		return true;
	}

	public boolean onKeyDpadRight(){
		long last = System.currentTimeMillis();
		if(last - lastTime < 300){
			return true;
		}
		lastTime = last;
		if(isLoaded == false){
			return true;
		}
		if(FocusPos == 2){
			if(((TextView)details.get(subFocusIndex).detailsLayout.findViewById(R.id.numpick_four)).getText().equals("X")){
				((TextView)details.get(subFocusIndex).detailsLayout.findViewById(R.id.numpick_four)).
					setText("1");
			}else{
				((TextView)details.get(subFocusIndex).detailsLayout.findViewById(R.id.numpick_four)).
					setText("" + (Integer.parseInt(((TextView)details.get(subFocusIndex).
							detailsLayout.findViewById(R.id.numpick_four)).getText().toString()) + 1));
				details.get(subFocusIndex).count ++;
			}
			return true;
		}
		if(subFocusIndex >= 0)
			return false;
		if((food.size() - 1) == curFocusIndex)
			return true;
		int newFocusIndex = -1;
		if(curFocusIndex < 0){
			newFocusIndex = 0;
		}else{
			if(food.size() > 1)
				mFoodPicLinearLayout.animate()
					.translationXBy(food.get(0).picLayout.getX()
							- food.get((1) % food.size()).picLayout.getX())
					.setDuration(300)
					.start();
			newFocusIndex = (curFocusIndex + 1) % food.size();
		}
		onFocusChanged(curFocusIndex, newFocusIndex);
		curFocusIndex = newFocusIndex;
		return true;
	}
	
	public boolean onKeyDpadUp() {
		if(isLoaded == false){
			return true;
		}
		long last = System.currentTimeMillis();
		if(last - lastTime < 300){
			return true;
		}
		lastTime = last;
		switch(FocusPos){
		case 0:
			return true;
		case 1:
			if(subFocusIndex == 0){
				mFoodPicLinearLayout.animate()
				.translationYBy(300.0f)
				.alpha(1.0f)
				.setDuration(300)
				.start();
				mRelativeLayout.animate()
				.translationYBy(300.0f)
				.setDuration(300)
				.start();
				baseLine.animate()
				.translationYBy(300.0f)
				.setDuration(300)
				.start();
				mShopCarLayout.animate()
				.translationYBy(300.0f)
				.setDuration(300)
				.start();
				detailbaseline.animate()
				.translationYBy(300.0f)
				.setDuration(300)
				.start();
				detailslistLayout.animate()
				.translationYBy(300.0f)
				.setDuration(300)
				.start();
				FocusPos = 0;
				subFocusIndex = -1;
				if(details.isEmpty())
					return true;
				onSubFocusChange(0, -1);
				return true;
			}
			break;
		case 2:
			return true;
		}
		int newSubFocusIndex = 0;
		if(subFocusIndex < 0){
			newSubFocusIndex = 0;
		}else{
			if(details.isEmpty())
				return true;
			newSubFocusIndex = (subFocusIndex + details.size() - 1) % details.size();
		}
		if(scrollpos > 0)
			scrollpos --;
		onSubFocusChange(subFocusIndex, newSubFocusIndex);
		subFocusIndex = newSubFocusIndex;
		return false;
	}
	
	public boolean onKeyDpadDown() {
		if(isLoaded == false){
			return true;
		}
		long last = System.currentTimeMillis();
		if(last - lastTime < 300){
			return true;
		}
		lastTime = last;
		switch(FocusPos){
		case 0:
			mFoodPicLinearLayout.animate()
			.translationYBy(-300.0f)
			.alphaBy(0.5f)
			.setDuration(300)
			.start();
			mRelativeLayout.animate()
			.translationYBy(-300.0f)
			.setDuration(300)
			.start();
			baseLine.animate()
			.translationYBy(-300.0f)
			.setDuration(300)
			.start();
			mShopCarLayout.animate()
			.translationYBy(-300.0f)
			.setDuration(300)
			.start();
			detailbaseline.animate()
			.translationYBy(-300.0f)
			.setDuration(300)
			.start();
			detailslistLayout.animate()
			.translationYBy(-300.0f)
			.setDuration(300)
			.start();
			FocusPos = 1;
			break;
		case 2:
			return true;
		}
		if(details.isEmpty())
			return true;
		if((subFocusIndex + 1) == details.size())
			return true;
		int newSubFocusIndex = -1;
		if(subFocusIndex < 0){
			newSubFocusIndex = 0;
		}else{
			newSubFocusIndex = (subFocusIndex + 1) % details.size();
		}
		onSubFocusChange(subFocusIndex, newSubFocusIndex);
		if(scrollpos < 4)
			scrollpos ++;
		subFocusIndex = newSubFocusIndex;
		return true;
	}
	
	public boolean onKeyEnter() {
		if(isLoaded == false){
			return true;
		}
		
		switch(FocusPos){
		/* 焦点在菜单上 */
		case 0:{
			/*need check same,FIXME, TODO*/
			int i = 0;
			for(i = 0; i < details.size(); i ++){
				if(details.get(i).name.equals(food.get(curFocusIndex).name)){
					details.get(i).price += Integer.parseInt(food.get(curFocusIndex).price);
					details.get(i).count += 1;
					TextView name = (TextView)details.get(i).detailsLayout.findViewById(R.id.ordering_details_name);
					TextView count = (TextView)details.get(i).detailsLayout.findViewById(R.id.ordering_details_count);
					name.setText(details.get(i).name + "(" + context.getString(R.string.mark) + details.get(i).price + ")");
					count.setText("" + details.get(i).count);
					break;
				}
			}
			//当名称不在列表中时，新加一个
			if(i == details.size()){
				DetailsInfo info = new DetailsInfo();
				info.name = food.get(curFocusIndex).name;
				info.price = Integer.parseInt(food.get(curFocusIndex).price);
				info.count = 1;
				info.detailsLayout = (RelativeLayout)LayoutInflater.from(context).inflate(R.layout.ordering_details_item, null);
				TextView name = (TextView)info.detailsLayout.findViewById(R.id.ordering_details_name);
				TextView count = (TextView)info.detailsLayout.findViewById(R.id.ordering_details_count);
				name.setText(info.name + "(" + context.getString(R.string.mark) + info.price + ")");
				count.setText("" + info.count);
				details.add(info);
				mDetailsLayout.addView(info.detailsLayout);
			}
			int count = 0;
			int total = 0;
			for(i = 0; i < details.size(); i ++){
				count += details.get(i).count;
				total += details.get(i).price;
			}
			orderingCarCount.setText("" + count);
			orderTotal.setText(context.getString(R.string.orderingtotal) + context.getString(R.string.mark) + total);
			break;
		}
		/* 焦点在订单上 */
		case 1:	{
			if(details.size() > 0){
				((LinearLayout)details.get(subFocusIndex).detailsLayout.findViewById(R.id.ordering_numpick)).setVisibility(View.VISIBLE);
				((TextView)details.get(subFocusIndex).detailsLayout.findViewById(R.id.ordering_details_count)).setVisibility(View.INVISIBLE);
				((TextView)details.get(subFocusIndex).detailsLayout.findViewById(R.id.numpick_four)).
					setText(((TextView)details.get(subFocusIndex).detailsLayout.findViewById(R.id.ordering_details_count)).getText());
				FocusPos = 2;
			}
			break;
		}
		/* 焦点在单个食物上 */
		case 2:{
			if(!((TextView)details.get(subFocusIndex).detailsLayout.findViewById(R.id.numpick_four)).getText().toString().equals("X")){
				((LinearLayout)details.get(subFocusIndex).detailsLayout.findViewById(R.id.ordering_numpick)).setVisibility(View.INVISIBLE);
				((TextView)details.get(subFocusIndex).detailsLayout.findViewById(R.id.ordering_details_count)).setVisibility(View.VISIBLE);
				((TextView)details.get(subFocusIndex).detailsLayout.findViewById(R.id.ordering_details_count)).
				setText(((TextView)details.get(subFocusIndex).detailsLayout.findViewById(R.id.numpick_four)).getText());
				int price = details.get(subFocusIndex).price / details.get(subFocusIndex).count;
				int curCount = Integer.parseInt(
						((TextView)details.get(subFocusIndex).detailsLayout.
								findViewById(R.id.ordering_details_count)).getText().toString());
				details.get(subFocusIndex).price = price * curCount;
				details.get(subFocusIndex).count = curCount;
				
				TextView name = (TextView)details.get(subFocusIndex).
						detailsLayout.findViewById(R.id.ordering_details_name);
				name.setText(details.get(subFocusIndex).name + "(" + 
						context.getString(R.string.mark) + details.get(subFocusIndex).price + ")");
				
				int count = 0;
				int total = 0;
				for(int i = 0; i < details.size(); i ++){
					count += details.get(i).count;
					total += details.get(i).price;
				}
				orderingCarCount.setText("" + count);
				orderTotal.setText(context.getString(R.string.orderingtotal) + context.getString(R.string.mark) + total);
			}else{
				mDetailsLayout.removeView(details.get(subFocusIndex).detailsLayout);
				details.remove(subFocusIndex);
				int count = 0;
				int total = 0;
				for(int i = 0; i < details.size(); i ++){
					count += details.get(i).count;
					total += details.get(i).price;
				}
				orderingCarCount.setText("" + count);
				orderTotal.setText(context.getString(R.string.orderingtotal) + context.getString(R.string.mark) + total);
				subFocusIndex--;
				if(subFocusIndex < 0)
					onSubFocusChange(-1, 0);
				else
					onSubFocusChange(-1, subFocusIndex);
			}

			FocusPos = 1;
			break;
		}
		default:
			break;
		}
		return true;
	}
	
}
