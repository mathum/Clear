package com.clearcrane.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clearcrane.provider.MaterialRequest;
import com.clearcrane.provider.MaterialRequest.OnCompleteListener;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.TipDialog;
import com.clearcrane.vod.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;

public class PicTextListView extends VoDBaseView {
	private  String PageJson ;
	private  FrameLayout picTextList_layout;
	private  String PicTextLabel ;
	private ArrayList<Mainicon>  MainiconList = new ArrayList<Mainicon>();
	private TextView pagination;
	
	ViewHolder vh;
	int curFocusIndex = -1;//顶层menu当前位置
	int curPage = -1;
	int tempFoucsIndex = -2 ;
	
	private long lastTime = 0;//按键事件控制
	
	 class Mainicon{
			String label = null;  //页面名字
			String type = null ;;
			int subCount = 0;
			String iconURL = null;
			int index = -1;
			RelativeLayout picView;//显示图片的布局
		}
	
	 
		public void init (Context ctx, String u){
			super.init(ctx,u);
			view = LayoutInflater.from(ctx).inflate(R.layout.pictext_sub_page_view, null);
			Log.i("getview",""+u);
			initLayoutInXml();
			
			/* trigger to get data sources */
			MaterialRequest mr = new MaterialRequest(ctx, ClearConfig.TYPE_JSON);
			mr.setOnCompleteListener(PicTextListListen);
			mr.execute(this.url);//暂时指定一个存在的url
		}
	    
		
		
	    OnCompleteListener PicTextListListen = new OnCompleteListener(){

			@Override
			public void onDownloaded(Object result) {
				// TODO Auto-generated method stub
				PageJson = (String)result;
				Log.i("Json","Json:"+PageJson);
				if(PageJson == null){
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
					
					JSONTokener jsonParser = new JSONTokener(PageJson);
					JSONObject objectjson = (JSONObject)jsonParser.nextValue();
					
					String type = objectjson.getString("Type");
					PicTextLabel = objectjson.getString("Label");
				
					Log.i("xyl12",""+PicTextLabel+"  :"+type);
					
					JSONArray contentArray = objectjson.getJSONArray("Content");
					for(int i = 0; i < contentArray.length(); i ++){
						int index=i;
						JSONObject objecttmp = (JSONObject)contentArray.opt(i);
						Mainicon mainicon = new Mainicon();
						mainicon.iconURL = ClearConfig.getJsonUrl(context, objecttmp.getString("path"));
						mainicon.index = objecttmp.getInt("index");
						mainicon.subCount=objectjson.getInt("Count");
						Log.i("XYL_pk",""+mainicon.iconURL);
						MainiconList.add(mainicon);
					}
					
					//默认当前焦点为第一个主菜单
					new Handler().postDelayed(new Runnable(){    
				        public void run() {    
				        	if(curFocusIndex < 0){      
								//first focus
				        		curFocusIndex = 0;
								onMainIconFocusPageChange(-1,curFocusIndex);
								
								
								
								/**
								//更改分页符
						    	pageSize = getAllPage(curFocusIndex);
						    	pagination.setText(curPage+"/"+pageSize+"页");
						    	pagination.setTextSize(TypedValue.COMPLEX_UNIT_PX,ClearConfig.getScreenHeight()/36);
							    **/
							}
				        }    
				     }, 100); 
					
					
					
					/**
					//每隔1000ms更新一次时间
			        timer.schedule(new TimerTask() {
			        	@Override
			            public void run() {
			                // TODO Auto-generated method stub
			        		updateTimeZone();
			        		Log.i("xyltime",""+timetext);
						       Message msg = mHandler.obtainMessage();
						       msg.sendToTarget();
			            }
			        }, 0,ClearConfig.UPDATE_TIME);
			        Log.i("xyl","time:"+timetext);
					**/
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onComplete(boolean result) {

				// TODO Auto-generated method stub
			}
	    	
	    };
	    
	    
	    private void initLayoutInXml(){
	    	picTextList_layout = (FrameLayout) view.findViewById(R.id.picTextList_layout);
	    	//pagination = (TextView)view.findViewById(R.id.pagination_sign);
	    }
	   
	    
	    class ViewHolder {
	        private ImageView  img;
	    }
	    
	    private void attachView(final Mainicon subicon){
	    	
	    	if(subicon.picView == null){
	    		vh = new ViewHolder();
	    		subicon.picView =(RelativeLayout) LayoutInflater.from(context).inflate(R.layout.pictext_sub_page_itemview, null);
	    		vh.img=(ImageView) subicon.picView.findViewById(R.id.picText_sub_page_view_content);
	    		//vh.img.setAdjustViewBounds(false);
				//vh.img.setMaxWidth(1920);
				//vh.img.setMaxHeight(1080);
				//vh.img.setScaleType(ScaleType.FIT_XY);
	    		subicon.picView.setTag(vh);
	    	}
	    	else
	    	{
	    		vh =(ViewHolder)subicon.picView.getTag();
	    	}
	    	
	    	/** 加载图片  
	    	MaterialRequest mr =new MaterialRequest(context, vh.img,
					ClearConfig.TYPE_IMAGE_BG);
	    	mr.execute(subicon.iconURL);
	    	 **/
	    	Log.i("jojo",":"+subicon.iconURL);
	    	
	    	ImageLoaderConfiguration configuration = ImageLoaderConfiguration  
	                .createDefault(context);
			ImageLoader.getInstance().init(configuration);
			
			
			/** 使用DisplayImageOptions.Builder()创建DisplayImageOptions
			 * 
			 */
			final DisplayImageOptions options = new DisplayImageOptions.Builder()
			.cacheInMemory(true) // 设置下载的图片是否缓存在内存中
	        .cacheOnDisk(true) // 设置下载的图片是否缓存在SD卡中
	       // .displayer(new FadeInBitmapDisplayer(100))// 图片加载好后渐入的动画时间
			.imageScaleType(ImageScaleType.EXACTLY)//设置图片的缩放方式
			.bitmapConfig(Bitmap.Config.ARGB_8888)
			.build();// 构建完成
			
			
			
			vh.img.post(new Runnable(){  
			    @Override  
			    public void run(){  
			        ImageLoader.getInstance().displayImage(subicon.iconURL, vh.img, options);  
			    }  
			});  
			
		
			Log.i("gaygay",":"+vh.img.getScaleType());
	    	
	    	
	    	picTextList_layout.addView(subicon.picView);
	    	//动态添加页码
	    	pagination = new TextView(context);
	    	pagination.setText(curFocusIndex+1+"/"+MainiconList.size()+"张");
	    	pagination.setTextSize(TypedValue.COMPLEX_UNIT_PX,ClearConfig.getScreenHeight()/18);
	    	pagination.setPadding(0, 0, 0, 200);
	    	
	    	picTextList_layout.addView(pagination);
			subicon.picView.setVisibility(View.VISIBLE);

	    }

	    public void onMainIconFocusPageChange(int oldPos, int newPos){
	    	//customLayoutTransition(Gesture);
	    	Log.i(",",":"+MainiconList.size());

	    	/**
	    	pagination.setText(curFocusIndex+"/"+MainiconList.size());
	    	pagination.setTextSize(TypedValue.COMPLEX_UNIT_PX,ClearConfig.getScreenHeight()/36);
            **/

	    	if(oldPos >= 0){
	    	Mainicon  oldIcon = MainiconList.get(oldPos);
	    	oldIcon.picView.setVisibility(View.INVISIBLE);
			picTextList_layout.removeView(oldIcon.picView);
			if(pagination != null){
			picTextList_layout.removeView(pagination);
			}
	    	}
	    	
	    	
	    	if(newPos >= 0 && MainiconList.size()>0){
	    	  
	    	  Mainicon newIcon = MainiconList.get(newPos);
	    	
			  attachView(newIcon);
	    	}
       }
	    
	    
	    @Override
	    public boolean onKeyDpadUp() {
	    	// TODO Auto-generated method stub
	    	long last = System.currentTimeMillis();
			if(last - lastTime < 300){
				return true;
			}
			lastTime = last;
			
			tempFoucsIndex = curFocusIndex;
			curFocusIndex = curFocusIndex==0?(MainiconList.size()-1):(curFocusIndex-1);
			onMainIconFocusPageChange(tempFoucsIndex, curFocusIndex);
	    	return true;
	    }
	    
	  @Override
	public boolean onKeyDpadRight() {
		// TODO Auto-generated method stub
		  long last = System.currentTimeMillis();
			if(last - lastTime < 300){
				return true;
			}
			lastTime = last;
			
			tempFoucsIndex = curFocusIndex;    
			curFocusIndex = (curFocusIndex+1)%MainiconList.size();
			onMainIconFocusPageChange(tempFoucsIndex, curFocusIndex);
	    	return true;
	}
	  
	  @Override
	public boolean onKeyDpadLeft() {
		// TODO Auto-generated method stub
		  long last = System.currentTimeMillis();
			if(last - lastTime < 300){
				return true;
			}
			lastTime = last;
			
			tempFoucsIndex = curFocusIndex;
			curFocusIndex = curFocusIndex==0?(MainiconList.size()-1):(curFocusIndex-1);
			onMainIconFocusPageChange(tempFoucsIndex, curFocusIndex);
	    	return true;
	}
	  
	  
	  @Override
	public boolean onKeyDpadDown() {
		// TODO Auto-generated method stub
		  long last = System.currentTimeMillis();
			if(last - lastTime < 300){
				return true;
			}
			lastTime = last;
			
			tempFoucsIndex = curFocusIndex;
			curFocusIndex = (curFocusIndex+1)%MainiconList.size();
			onMainIconFocusPageChange(tempFoucsIndex, curFocusIndex);
	    	return true;
	}
	  
}
