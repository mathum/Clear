package com.clearcrane.logic.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.log.L;
import com.clearcrane.logic.ProgramObject;
import com.clearcrane.provider.MaterialRequest;
import com.clearcrane.provider.MaterialRequest.OnCompleteListener;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.view.VoDBaseView;
import com.clearcrane.vod.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * 计划播放的View
 * 
 * @author SlientLeaves 2016年4月28日 上午9:59:00
 */
public class VodSchedulePlayView extends VoDBaseView {

	public static final String TAG = "VodSchedulePlayView";
	private static final long DELAY_MILLIS = 6000;

	private String jsonResult;
	private ImageView mWaitView;// 等待初始化时的背景图片
	private TextView mTextInfoView;
	private FrameLayout mBaseView;// 帧布局，往里面动态添加ControlBox
	private Context mContext;
	private ProgramObject mProgramObject;
	private String mPathStr;
	private JSONObject joPlayList;

	public void init(Context ctx, String u) {
		super.init(ctx, u);
		this.mContext = ctx;

		// url = new
		// String("http://192.168.18.249/nativevod/json/weathersearch.json");
		view = LayoutInflater.from(context).inflate(R.layout.schedule_play_view, null);
//		ImageUtil.initImageLoader(ctx);
		initLayoutInXml();

//		String result = readPlayListFromLocal();
		String localPlayList = getFileFormAsset();

		if (localPlayList != null && !"".equals(localPlayList)) {
//			writePlayListToLocal(localPlayList);
//			String result = readPlayListFromLocal();
			completeListener.onDownloaded(localPlayList);
		} else {
			MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
			mr.setOnCompleteListener(completeListener);
			if (ClearConfig.checkNetwork(context) == ClearConfig.TYPE_LOCAL_STB)
				mr.execute("/nativevod/json/weathersearch.json");
			else
				mr.execute(url);
		}

	}

	public void init(Context ctx, String u, LinearLayout layout) {
		super.init(ctx, u, layout);

		// url = new
		view = LayoutInflater.from(context).inflate(R.layout.schedule_play_view, null);
		initLayoutInXml();
		
		jsonResult = getFileFormAsset();

		if (jsonResult != null && !"".equals(jsonResult)) {
			completeListener.onDownloaded(jsonResult);
		} else {
			MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
			mr.setOnCompleteListener(completeListener);
			if (ClearConfig.checkNetwork(context) == ClearConfig.TYPE_LOCAL_STB)
				mr.execute("/nativevod/json/weathersearch.json");
			else
				mr.execute(url);
		}

	}
	
	
	public void init(Context ctx, JSONObject json,String url){
		super.init(ctx,url);
		
	}

	/**
	 * 从本地SD卡读取播放列表信息的文本信息
	 * 
	 * @return
	 */
	private String readPlayListFromLocal() {
		/* read playlist from local file */
		try {
			BufferedReader br = new BufferedReader(new FileReader(mPathStr + "/" + ClearConstant.ACCESSTIME_FILE));
			String line = "";
			StringBuffer sbuf = new StringBuffer();
			while ((line = br.readLine()) != null) {
				sbuf.append(line);
			}
			br.close();

			L.i(TAG, "read local commandfile list:\n" + sbuf.toString());

			return sbuf.toString();

		} catch (IOException e) {
			// TODO FIXME, double safety, return playlist from hardcode
			L.e(TAG, "Can not read local commandfile list");
			e.printStackTrace();
			return null;
		}
	}

	/** 
	* 写入内容到SD卡中的txt文本中 
	* str为内容 
	*/ 
	private void writePlayListToLocal(String playList) { 
		if (Environment.getExternalStorageState().endsWith(Environment.MEDIA_MOUNTED)) {
			String sdCardRootDir = Environment.getExternalStorageDirectory().getPath();
//			mPathStr = sdCardRootDir +"/"+ClearConstant.ResourceDir;
			
			try {

//				File file = new File(mPathStr + "/" + ClearConstant.FileName);

//				if (!file.exists()) {
//					file.createNewFile();
//				}
				
//				FileWriter fileWriter = new FileWriter(mPathStr + "/" + ClearConstant.FileName);
//				BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
//				bufferWriter.write(playList);
				
//				fileWriter.flush();
//				fileWriter.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void hideWaitView() {
		if (mWaitView.getVisibility() == View.VISIBLE) {
			mWaitView.setVisibility(View.GONE);
		}
	}

	private void initLayoutInXml() {
		mTextInfoView = (TextView) view.findViewById(R.id.text_info);
		mWaitView = (ImageView) view.findViewById(R.id.waitImage);
		ImageLoader.getInstance().displayImage("assets://boot_welcome.image", mWaitView);
		Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay(); // 为获取屏幕宽、高
		Point size = new Point();
		display.getSize(size);
//		ClearConstant.screenWidth = size.x;
//		ClearConstant.screenHeight = size.y;

		mBaseView = (FrameLayout) view.findViewById(R.id.ims_baseview);

	}

	private OnCompleteListener completeListener = new OnCompleteListener() {


		@Override
		public void onDownloaded(Object result) {
			jsonResult = (String) result;
			if (jsonResult == null) {
//				TipDialog.Builder builder = new TipDialog.Builder(context);
//				builder.setMessage("当前网络不可用，请检查网络");
//				builder.setTitle("提示");
//				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//						// 设置你的操作事项
//					}
//				});
//
//				builder.create().show();
				Log.e(TAG,"internet is not available");
				return;
			}
			try {
//				writePlayListToLocal(jsonResult);
				JSONTokener jsonParser = new JSONTokener(jsonResult);
				joPlayList = (JSONObject) jsonParser.nextValue();
//				mProgramObject = new ImsProgram(context, mBaseView, joPlayList);
				hideWaitView();
//				mProgramObject.play();

			} catch (JSONException e) {

			}
		}

		@Override
		public void onComplete(boolean result) {

		}

	};

	private String getFileFormAsset() {

		String playListStr = null;
		try {
			// 获得AssetManger 对象, 调用其open 方法取得 对应的inputStream对象
			InputStream is = context.getAssets().open("playli22st.txt");
			// 取得数据流的数据大小
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			playListStr = new String(buffer);
			return playListStr;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonResult;
	}
}
