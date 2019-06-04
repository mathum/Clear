/**
 * @author wzz
 * @copyright clear
 * @date 2015-08-27
 * @description 图文界面
 */
package com.clearcrane.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clearcrane.provider.MaterialRequest;
import com.clearcrane.provider.MaterialRequest.OnCompleteListener;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.TipDialog;
import com.clearcrane.vod.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Calendar;

public class VodExchagePictureView extends VoDBaseView {
	
	private ImageView pageImage, main_back_ground;
	private TextView pageNumText,pageTitle;
	private String picPageJson;
	private String TAG = "picturepage";
	private String mainBackUrl = "drawable://" + R.drawable.picture_page;
	private String picurl = null;
	private int curIndex = 0;
	Calendar beginTime = Calendar.getInstance();
	
	private DisplayImageOptions options = new DisplayImageOptions.Builder()  
							    .cacheInMemory(true)  
							    .cacheOnDisk(true)  
							    .bitmapConfig(Bitmap.Config.RGB_565)
							    .imageScaleType(ImageScaleType.NONE)
							    .build();
	
	private ArrayList<Picture> picList = new ArrayList<Picture>();
	public class Picture {
		String url;
		int id;
	}
  
	public void init(Context ctx, String u){
		super.init(ctx, u);
		url = u;
		view = LayoutInflater.from(context).inflate(R.layout.thj_picture_page, null);
		initLayoutInXml();
		
		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(PicPageJsonListener);
		mr.execute(url);
	}
	
	public void init(Context ctx, String u, LinearLayout layout){
		super.init(ctx, u, layout);
		url = u;
		view = LayoutInflater.from(context).inflate(R.layout.thj_picture_page, null);
		initLayoutInXml();
		
		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(PicPageJsonListener);
		mr.execute(url);
	}

	private void initLayoutInXml() {
		// TODO Auto-generated method stub
		pageImage = (ImageView) view.findViewById(R.id.thj_pictur_page_image);
		main_back_ground = (ImageView) view.findViewById(R.id.thj_pictur_page_mainbackground);
		main_back_ground.setVisibility(View.GONE);
		pageNumText = (TextView) view.findViewById(R.id.thj_picture_page_num);
		pageNumText.setVisibility(View.GONE);
		pageTitle = (TextView) view.findViewById(R.id.thj_picture_page_title);
		ImageView icon = (ImageView) view.findViewById(R.id.thj_picture_page_right);
		icon.setVisibility(View.GONE);
		ImageView lefticon = (ImageView) view.findViewById(R.id.thj_picture_page_left);
		lefticon.setVisibility(View.GONE);
		TextView selecttext = (TextView) view.findViewById(R.id.thj_picture_page_select);
		selecttext.setVisibility(View.GONE);
		TextView backtext = (TextView) view.findViewById(R.id.thj_picture_page_back);
		backtext.setVisibility(View.GONE);
		
		//ImageLoader.getInstance().displayImage(mainBackUrl, main_back_ground);
			  
	}
	
	private OnCompleteListener PicPageJsonListener = new OnCompleteListener(){

		@Override
		public void onDownloaded(Object result) {
			// TODO Auto-generated method stub
			picPageJson = (String)result;
			if(picPageJson == null){
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
				JSONTokener jsonParser = new JSONTokener(picPageJson);
				JSONObject object = (JSONObject)jsonParser.nextValue();
				JSONArray array = (JSONArray)object.getJSONArray("Content");
				for(int i = 0; i < array.length(); i ++){
					JSONObject picTmp = (JSONObject)array.opt(i);
					Picture p = new Picture();
		    		String url_chn = ClearConfig.getJsonUrl(context, picTmp.getString("Picurl"));
		    		String url_eng = ClearConfig.getJsonUrl(context, picTmp.getString("Picurl_eng"));
		    		p.url = ClearConfig.getStringByLanguageId(url_chn, url_eng);
		    		p.id = picTmp.getInt("id");
		    		picList.add(p);
				}
			}catch(JSONException e){
				
			}
			
			pageTitle.setText(nameInIcon);
			Log.i(TAG,"nameIncon:"+nameInIcon);
			picurl = picList.get(curIndex).url;
			if(!picurl.startsWith("http"))
				picurl = Scheme.FILE.wrap(picurl);
			ImageLoader.getInstance().displayImage(picurl, pageImage,options);
			pageNumText.setText(pageNum(curIndex));
		}

		@Override
		public void onComplete(boolean result) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	public String pageNum(int index){
		index++;
		String text = "-" + index + "/" + picList.size() + "-";
		return text;
	}
	
	public boolean onKeyDpadLeft(){
		long between = (Calendar.getInstance()).getTimeInMillis() - beginTime.getTimeInMillis();
		if(between < 100 ){
			return true;
		}
		beginTime = Calendar.getInstance();
		if(curIndex > 0){
			curIndex--;
			picurl = picList.get(curIndex).url;
			if(!picurl.startsWith("http"))
				picurl = Scheme.FILE.wrap(picurl);
			ImageLoader.getInstance().displayImage(picurl, pageImage,options);
			pageNumText.setText(pageNum(curIndex));
		}
		
		return true;
	}
	
	public boolean onKeyDpadRight(){
		long between = (Calendar.getInstance()).getTimeInMillis() - beginTime.getTimeInMillis();
		if(between < 100 ){
			return true;
		}
		beginTime = Calendar.getInstance();
		
		if(curIndex < picList.size()-1){
			curIndex++;
			picurl = picList.get(curIndex).url;
			if(!picurl.startsWith("http"))
				picurl = Scheme.FILE.wrap(picurl);
			ImageLoader.getInstance().displayImage(picurl, pageImage,options);
			pageNumText.setText(pageNum(curIndex));
		}
		
		return true;
	}
	
	
}
