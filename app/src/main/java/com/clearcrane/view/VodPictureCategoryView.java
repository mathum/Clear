package com.clearcrane.view;

import android.content.Context;
import android.content.DialogInterface;
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
import com.clearcrane.util.PictureSubCategoryListAdapter;
import com.clearcrane.util.TipDialog;
import com.clearcrane.vod.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Calendar;

public class VodPictureCategoryView extends VoDBaseView {
	
	private String dataJson;//json数据
	private ImageView pictureView ;
	private TextView pictureTitleText;
	private FrameLayout pictureListTextIntroduceFL;
	private TextView introduceText;
	private ListView categoryListView;
	private ImageView okButtonView;

	private PictureSubCategoryListAdapter picAdapter = null;
	
	private int curFocusCategoryIdx = -1;
	private View lastFocusView = null;
	private int lastFocusCategoryIdx = -1;
	public Calendar begin = null;
	
	
	public class Picture {
		String url;
		String facility;
		String introduce = "";
		String introduceEng = "";
	}
	
	public class PictureSubCategory {
		public String name;
		public String nameEng;
		public String type;
		public String Json_URL;
		ArrayList<Picture> picList = new ArrayList<Picture>();
	}
	
	ArrayList<PictureSubCategory> picCategoryList = new ArrayList<PictureSubCategory>();
	
	/* data provider */
	/* build the view layout/element */
	/* start animation */
	public void init(Context ctx, String u) {
		begin = Calendar.getInstance();
		context = ctx;
		url = u;
		view = LayoutInflater.from(context).inflate(R.layout.picture_category_view, null);
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
		view = LayoutInflater.from(context).inflate(R.layout.picture_category_view, null);
		initLayoutInXml();
		
		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(DataJsonListen);
		mr.execute(url);
	}
	
	private void initLayoutInXml() {
		// TODO Auto-generated method stub
		pictureView = (ImageView) view.findViewById(R.id.pictureView_category);
		pictureTitleText = (TextView) view.findViewById(R.id.pictureTitleText_category);
		categoryListView = (ListView)view.findViewById(R.id.pictureSubCategoryList);
		introduceText = (TextView) view.findViewById(R.id.picturCategoryText);
		pictureListTextIntroduceFL = (FrameLayout) view.findViewById(R.id.pictureSubCategoryTextIntroduceFL);
		okButtonView = (ImageView) view.findViewById(R.id.bottom_notice_category);
		if(ClearConfig.LanguageID == 1){
			okButtonView.setImageResource(R.drawable.top_recommend_command);
		}else{
			okButtonView.setImageResource(R.drawable.top_recommend_command_eng);
		}
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
			  
			
			    		
			    for(int i = 0; i < contentArray.length(); i ++){
			    	JSONObject objecttmp = (JSONObject)contentArray.opt(i);
			    	
			    	PictureSubCategory psc = new PictureSubCategory();
			    	String name = objecttmp.getString("Name");
			    	String nameEng = objecttmp.getString("NameEng");
			    	psc.name = ClearConfig.getStringByLanguageId(name, nameEng);
			    	psc.type = objecttmp.getString("Type");
			    	psc.Json_URL = ClearConfig.getJsonUrl(context, objecttmp.getString("Json_URL"));
			    	
			    	Log.i("Leo test", "name " + psc.name);
			    	JSONArray SubContent = (JSONArray)objecttmp.getJSONArray("SubContent");
			    	for(int j = 0; j < SubContent.length(); j ++){
			    		JSONObject picTmp = (JSONObject)SubContent.opt(j);
			    		Picture p = new Picture();
			    		p.url = ClearConfig.getJsonUrl(context,picTmp.getString("Picurl"));
			    		p.facility = picTmp.getString("Facility");
			    		p.introduce = picTmp.getString("Introduce");
			    		p.introduceEng = picTmp.getString("IntroduceEng");
			    		Log.i("Leo test", "url " + p.url);
			    		psc.picList.add(p);
			    	}
			    	
			    	
			    	picCategoryList.add(psc);
			    }
			    if(begin != null) {
                	long between = (Calendar.getInstance()).getTimeInMillis()-begin.getTimeInMillis(); 
                    ClearLog.LogInfo("BROSWER\tLoad\tSUCC\t" + between +"ms\t" + url + "\t"
                    		+ "picturCategoryView");
                } 
			}catch (JSONException e) {
				ClearLog.LogError("BROSWER\tLoad\tFAIL\t0ms\t" + url);
				e.printStackTrace();
			}
			
			pictureTitleText.setText(nameInIcon);
			
			PictureSubCategoryListAdapter picAdapter = new PictureSubCategoryListAdapter(
					context, picCategoryList);
			categoryListView.setAdapter(picAdapter);
			categoryListView.requestFocus();
			
			categoryListView.setOnItemSelectedListener(itemFocusListner);
			categoryListView.setOnItemClickListener(new OnItemClickListener() {
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
				((ImageView)(lastFocusView.findViewById(R.id.sub_category_item_focus))).setVisibility(View.INVISIBLE);
				ImageView iv2 = (ImageView)lastFocusView.findViewById(R.id.sub_category_item_selected_pic);
				iv2.setImageResource(R.drawable.empty_circle);
				
			}
			
			lastFocusView = arg1;
			
			ImageView iv = (ImageView)arg1.findViewById(R.id.sub_category_item_focus);
			iv.setVisibility(View.VISIBLE);
			
			ImageView iv2 = (ImageView)arg1.findViewById(R.id.sub_category_item_selected_pic);
			iv2.setImageResource(R.drawable.ordering_detail_focus_bg);
			
			curFocusCategoryIdx = arg2;
			updateCurPicIdx();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	
	
	private void updateCurPicIdx() {
		if(curFocusCategoryIdx >= 0 && curFocusCategoryIdx < picCategoryList.size()) {
			if(lastFocusView != null && picCategoryList.get(curFocusCategoryIdx).picList.isEmpty() == false) {	
				MaterialRequest mr = new MaterialRequest(context, pictureView, ClearConfig.TYPE_IMAGE);
				mr.execute(picCategoryList.get(curFocusCategoryIdx).picList.get(0).url);
				lastFocusCategoryIdx = curFocusCategoryIdx;
				
	
			}
		}
	}
	
	public boolean onKeyDpadLeft() {
		Log.i("Leo test", "key left ");
		return true;
	}
	
	public boolean onKeyDpadRight() {
		Log.i("Leo test", "key right ");
		return true;
	}
	
	public boolean onKeyEnter() {
		Log.i("Leo test", "key eneter");
		
		if(curFocusCategoryIdx >= 0 && curFocusCategoryIdx < picCategoryList.size()) {
			Log.i("key pictur", "view type:"+picCategoryList.get(curFocusCategoryIdx).type);
			VoDBaseView newView = VoDViewManager.newViewByType(
					picCategoryList.get(curFocusCategoryIdx).type);
			Log.i("key pictur", "after newview:");
			
			if(newView != null) {
				newView.setName(picCategoryList.get(curFocusCategoryIdx).name);
				Log.i("key pictur", "newvidw name:"+newView.nameInIcon);
				Log.i("key pictur", "befor init :");
				newView.init(context, picCategoryList.get(curFocusCategoryIdx).Json_URL);	
				Log.i("key pictur", "after init:");
				
				/* show the sub view */
				
				VoDViewManager.getInstance().pushForegroundView(newView);
			}
			
		}
		
		return true;
	}
	
	public boolean onKeyBack() {
		
		//VoDViewManager.getInstance().playBackgroundVideo();
		VoDViewManager.getInstance().popForegroundView();
		return true;
	}
}
