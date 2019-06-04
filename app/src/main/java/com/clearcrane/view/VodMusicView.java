package com.clearcrane.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.clearcrane.databean.Mp3Info;
import com.clearcrane.log.ClearLog;
import com.clearcrane.provider.MaterialRequest;
import com.clearcrane.provider.MaterialRequest.OnCompleteListener;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.MusicListAdapter;
import com.clearcrane.util.TipDialog;
import com.clearcrane.vod.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Calendar;

public class VodMusicView extends VoDBaseView {

	private String background_pic = null;
	private ListView musicListView;
	private View lastFocusView = null;
	private int curFocusMusicIdx = -1;// 当前在播放的音乐

	private ImageView musicAlbumImage, backgroundImage;
	private ImageView musicOkBurron;
	private TextView musicNameInTitle;
	private TextView musicSingerInTitle;
	private TextView musicAlbumInTitle;
	private TextView songListName;
	private TextView singerListName;
	public Calendar begin = null;
	private MusicListAdapter musicAdapter;

	ArrayList<Mp3Info> musicList = new ArrayList<Mp3Info>();

	private DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true)
			.bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.NONE).build();

	/* data provider */
	/* build the view layout/element */
	/* start animation */
	public void init(Context ctx, String u) {
		begin = Calendar.getInstance();
		super.init(ctx, u);
		view = LayoutInflater.from(context).inflate(R.layout.music_view, null);
		initLayoutInXml();

		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(DataJsonListen);
		mr.execute(u);
	}

	// public void init(Context ctx, String u, LinearLayout layout) {
	// begin = Calendar.getInstance();
	// super.init(ctx, u, layout);
	// view = LayoutInflater.from(context).inflate(R.layout.music_view, null);
	// initLayoutInXml();
	//
	// MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
	// mr.setOnCompleteListener(DataJsonListen);
	// mr.execute(u);
	// }

	private void initLayoutInXml() {

		musicListView = (ListView) view.findViewById(R.id.musicList);
		musicListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				curFocusMusicIdx = arg2;
				for (Mp3Info music : musicList) {
					music.isPlay = false;
				}
				musicList.get(curFocusMusicIdx).isPlay = true;
				onKeyEnter();
				musicAdapter.notifyDataSetChanged();
				musicListView.setSelection(curFocusMusicIdx);
			}
		});
		songListName = (TextView) view.findViewById(R.id.music_name_and_count);
		singerListName = (TextView) view.findViewById(R.id.music_singer_list);
		backgroundImage = (ImageView) view.findViewById(R.id.music_backgroud_image);
		musicOkBurron = (ImageView) view.findViewById(R.id.music_ok_button);
		musicOkBurron.setVisibility(View.VISIBLE);
		if (ClearConfig.LanguageID == 1) {
			Log.i("music", "ok button chinese");
			musicOkBurron.setImageResource(R.drawable.ok_background);
			songListName.setText("音频列表");
			singerListName.setText(R.string.music_singer);
		} else {
			Log.i("music", "ok button english");
			musicOkBurron.setImageResource(R.drawable.ok_background);
			songListName.setText("音频列表");
			singerListName.setText(R.string.music_singer_eng);
		}
		musicAlbumImage = (ImageView) view.findViewById(R.id.music_album_image);
		musicNameInTitle = (TextView) view.findViewById(R.id.music_name_in_title);
		musicSingerInTitle = (TextView) view.findViewById(R.id.music_singer_in_title);
		musicAlbumInTitle = (TextView) view.findViewById(R.id.music_album_in_title);

		/* view initialize more */
		musicAdapter = new MusicListAdapter(context, musicList);
		musicListView.setAdapter(musicAdapter);
		musicListView.requestFocus();
	}

	private OnCompleteListener DataJsonListen = new OnCompleteListener() {

		@Override
		public void onDownloaded(Object result) {
			// TODO Auto-generated method stub
			String dataJson = (String) result; 
			Log.e("aaaa", "VodMusic:"+ dataJson);
			if (dataJson == null) {
				TipDialog.Builder builder = new TipDialog.Builder(context);
				builder.setMessage("当前网络不可用，请检查网络");
				builder.setTitle("提示");
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// 设置你的操作事项
					}
				});

				builder.create().show();
				return;
			}
			try {
				JSONTokener jsonParser = new JSONTokener(dataJson);
				JSONObject mainViewObj = (JSONObject) jsonParser.nextValue();
				// background_pic =
				// "http://vodoperate.cleartv.cn/resource/42d385383cb9ab0059b6e29f2507c1e3_143694552915.jpg";
				// MaterialRequest image = new MaterialRequest(context,
				// backgroundImage,ClearConfig.TYPE_IMAGE);
				// image.execute(background_pic);

				// if(!background_pic.startsWith("http"))
				// background_pic = Scheme.FILE.wrap(background_pic);
				// ImageLoader.getInstance().displayImage(background_pic,
				// backgroundImage,options);

				JSONArray contentArray = (JSONArray) mainViewObj.getJSONArray("Content");
				for (int i = 0; i < contentArray.length(); i++) {
					JSONObject objecttmp = (JSONObject) contentArray.opt(i);

					Mp3Info m = new Mp3Info();
					StringBuilder str = new StringBuilder(objecttmp.getString("AudioName"));

					int indexEnd = str.indexOf(".");

					m.name = str.substring(0, indexEnd);
					m.playURL = objecttmp.getString("AudioPath");
					// Log.i("music", "playurl:" + m.playURL);

					m.nameEng = "weizhi";
					// m.picURL = "";
					m.singer = "singer";
					m.singerEng = "singerEng";
					m.album = "album";
					m.albumEng = "albumEng";
					m.summary = "summary";
					m.summaryEng = "summaryEng";
					if (objecttmp.getString("duration") != null) {
						m.duration = Integer.parseInt(objecttmp.getString("duration"));
					} else {
						m.duration = 309;
					}
					musicList.add(m);
					musicAdapter.notifyDataSetChanged();
				}

				if (begin != null) {
					long between = (Calendar.getInstance()).getTimeInMillis() - begin.getTimeInMillis();
					ClearLog.LogInfo("BROSWER\tLoad\tSUCC\t" + between + "ms\t" + url + "\t" + "musicView");
				}
			} catch (JSONException e) {
				ClearLog.LogError("BROSWER\tLoad\tFAIL\t0ms\t" + url);
				e.printStackTrace();
			}

			// musicListView.setOnItemSelectedListener(itemFocusListner);

			// first time play the first one, without enter key
			// curFocusMusicIdx = 0;
			// musicList.get(curFocusMusicIdx).isPlay = true;
			// onKeyEnter();
			// musicAdapter.notifyDataSetChanged();
		}

		@Override
		public void onComplete(boolean result) {
			// TODO Auto-generated method stub

		}
	};

	// OnItemSelectedListener itemFocusListner = new OnItemSelectedListener() {
	//
	// @Override
	// public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long
	// arg3) {
	// // TODO Auto-generated method stub
	// if (lastFocusView != null) {
	// ((ImageView)
	// (lastFocusView.findViewById(R.id.music_item_focus))).setVisibility(View.INVISIBLE);
	// ((ImageView)
	// (lastFocusView.findViewById(R.id.music_xuanlvtubiao))).setVisibility(View.INVISIBLE);
	// }
	//
	// lastFocusView = arg1;
	// curFocusMusicIdx = arg2;
	//
	// ((ImageView)
	// (arg1.findViewById(R.id.music_item_focus))).setVisibility(View.VISIBLE);
	// ((ImageView)
	// (arg1.findViewById(R.id.music_xuanlvtubiao))).setVisibility(View.VISIBLE);
	// }
	//
	// @Override
	// public void onNothingSelected(AdapterView<?> arg0) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// };

	public boolean onKeyEnter() {
		if (curFocusMusicIdx >= 0 && curFocusMusicIdx < musicList.size()) {
			VoDViewManager.getInstance().startMusic(musicList.get(curFocusMusicIdx).playURL);
			// MaterialRequest mr = new MaterialRequest(context,
			// musicAlbumImage, ClearConfig.TYPE_IMAGE);
			// mr.execute(musicList.get(curFocusMusicIdx).picURL);
			musicNameInTitle.setText(ClearConfig.getStringByLanguageId(musicList.get(curFocusMusicIdx).name,
					musicList.get(curFocusMusicIdx).name));

			musicSingerInTitle.setText(ClearConfig.getStringByLanguageId(musicList.get(curFocusMusicIdx).singer,
					musicList.get(curFocusMusicIdx).singer));

			musicAlbumInTitle.setText(ClearConfig.getStringByLanguageId(musicList.get(curFocusMusicIdx).album,
					musicList.get(curFocusMusicIdx).albumEng));

		}
		return true;
	}

	public boolean onKeyBack() {
		VoDViewManager.getInstance().stopMusic();
		VoDViewManager.getInstance().popForegroundView();
		return true;
	}

}
