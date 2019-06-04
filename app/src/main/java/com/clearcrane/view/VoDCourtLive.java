package com.clearcrane.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.clearcrane.adapter.CourtLiveAdapter;
import com.clearcrane.databean.MediaInfo;
import com.clearcrane.logic.view.InterCutView;
import com.clearcrane.vod.R;

import java.util.ArrayList;
import java.util.List;

public class VoDCourtLive extends VoDBaseView {
	private GridView courtLiveGridView;
	private CourtLiveAdapter courtLiveAdapter;
	private VideoView clearHiPlayerView;
	private TextView iconText;
	private TextView courtLiveAlreadyText;
	private TextView courtLiveLivingText;
	private TextView courtLiveTodayText;
	
	private TextView courtLiveAlreadyShowText;
	private TextView courtLiveLivingShowText;
	private TextView courtLiveTodayShowText;
	private FrameLayout container;
	private List<MediaInfo> list = new ArrayList<>();
	private int columnPosition = 0;// 用来判断gridview的右边界
	private int position = 0;
	private int location = 0;
    private String ViewType = "0"; // 0：庭审直播 1：会议直播 2:重点区域
    private LinearLayout courtLiveTempButton;
	@Override
	public void init(Context ctx, String u,String type) {
		// TODO Auto-generated method stub
		super.init(ctx, u);
		view = LayoutInflater.from(ctx).inflate(R.layout.court_live, null);
		view.setFocusable(false);
//		view.setClickable(false);
		this.ViewType = type;
		initLayout();
		initData();
		initListener();

		courtLiveGridView.requestFocus();
		courtLiveGridView.setSelection(0);

//		Uri videoUri = Uri.parse(list.get(0).getSrc());
//		clearHiPlayerView.setVideoURI(videoUri);
//		http://192.168.0.66/nativevod/resource/cctv1.mp4
		clearHiPlayerView.setVideoPath("http://192.168.0.66/nativevod/resource/cctv1.mp4");
//		clearHiPlayerView.setVideoPath("http://192.168.0.66/nativevod/resource/cctv1.mp4");
		clearHiPlayerView.start();
	}

	public void initLayout() {
		courtLiveGridView = (GridView) view.findViewById(R.id.court_live_gridView);
		container = (FrameLayout) view.findViewById(R.id.court_live_container);
		clearHiPlayerView = (VideoView) view.findViewById(R.id.court_live_videoView);
		clearHiPlayerView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {  
			  
            @Override  
            public void onPrepared(MediaPlayer mp) {  
                mp.start();  
                mp.setLooping(true);  
            }  
        }); 
		clearHiPlayerView  
         .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {  

             @Override  
             public void onCompletion(MediaPlayer mp) {  
            	 clearHiPlayerView.setVideoPath(list.get(location).getSrc());  
            	 clearHiPlayerView.start();  

             }  
         });  
		iconText = (TextView) view.findViewById(R.id.court_live_icon);
		courtLiveAlreadyText = (TextView) view.findViewById(R.id.court_live_already);
		courtLiveLivingText = (TextView) view.findViewById(R.id.court_live_living);
		courtLiveTodayText = (TextView) view.findViewById(R.id.court_live_today);
		
		courtLiveAlreadyShowText = (TextView) view.findViewById(R.id.court_live_already_show);
		courtLiveLivingShowText = (TextView) view.findViewById(R.id.court_live_living_show);
		courtLiveTodayShowText = (TextView) view.findViewById(R.id.court_live_today_show);
		
		courtLiveTempButton = (LinearLayout) view.findViewById(R.id.court_live_tempButton);
		
		if (ViewType.equals("1")) {
			iconText.setText("会议直播");
			courtLiveAlreadyText.setText("2");
			courtLiveLivingText.setText("3");
			courtLiveTodayText.setText("6");
			
			courtLiveAlreadyShowText.setText("已开会议");
			courtLiveLivingShowText.setText("会议中");
			courtLiveTodayShowText.setText("今日会议");
		}
		if(ViewType.equals("2")){
			iconText.setText("重点区域");
			courtLiveAlreadyText.setVisibility(view.INVISIBLE);
			courtLiveLivingText.setVisibility(view.INVISIBLE);
			courtLiveTodayText.setVisibility(view.INVISIBLE);
			
			courtLiveAlreadyShowText.setVisibility(view.INVISIBLE);
			courtLiveLivingShowText.setVisibility(view.INVISIBLE);
			courtLiveTodayShowText.setVisibility(view.INVISIBLE);
		}
	}

	public void initData() {
		if (ViewType.equals("0")) {
			for (int i = 0; i < 16; i++) {
				MediaInfo mediaInfo = new MediaInfo();
				mediaInfo.setName("第" + (i + 1) + "法庭");
				mediaInfo.setSrc("http://192.168.0.66/nativevod/resource/cctv1.mp4");
				if (i == 6 || i == 10 || i == 12) {
					mediaInfo.setLive(true);
					switch (i) {
					case 6:
						mediaInfo.setSrc("/mnt/sda/sda1/nativevod/now/Main/resource/cctv1.mp4");
						break;
					case 10:
						mediaInfo.setSrc("/mnt/sda/sda1/nativevod/now/Main/resource/jiangsu.mp4");
						break;
					case 12:
						mediaInfo.setSrc("/mnt/sda/sda1/nativevod/now/Main/resource/shenzhen.mp4");
						break;
					default:
						break;
					}
				} else {
					mediaInfo.setLive(false);
				}
				list.add(mediaInfo);
			}
		}
		if (ViewType.equals("1")) {
			for (int i = 0; i < 7; i++) {
				MediaInfo mediaInfo = new MediaInfo();
				mediaInfo.setName("第" + (i + 1) + "会议室");
				mediaInfo.setSrc("/mnt/sda/sda1/nativevod/now/Main/resource/cctv1.mp4");
				if (i == 1 || i == 3 || i == 6) {
					mediaInfo.setLive(true);
					switch (i) {
					case 1:
						mediaInfo.setSrc("/mnt/sda/sda1/nativevod/now/Main/resource/cctv1.mp4");
						break;
					case 3:
						mediaInfo.setSrc("/mnt/sda/sda1/nativevod/now/Main/resource/jiangsu.mp4");
						break;
					case 6:
						mediaInfo.setSrc("/mnt/sda/sda1/nativevod/now/Main/resource/shenzhen.mp4");
						break;
					default:
						break;
					}
				} else {
					mediaInfo.setLive(false);
				}
				list.add(mediaInfo);
			}
		}
		
		if (ViewType.equals("2")) {
			for (int i = 0; i < 9; i++) {
				MediaInfo mediaInfo = new MediaInfo();
				mediaInfo.setName("第" + (i + 1) + "区域");
				mediaInfo.setSrc("/mnt/sda/sda1/nativevod/now/Main/resource/cctv1.mp4");
				if (i == 2 || i == 5 || i == 7) {
					mediaInfo.setLive(true);
					switch (i) {
					case 2:
						mediaInfo.setSrc("/mnt/sda/sda1/nativevod/now/Main/resource/cctv1.mp4");
						break;
					case 5:
						mediaInfo.setSrc("/mnt/sda/sda1/nativevod/now/Main/resource/jiangsu.mp4");
						break;
					case 7:
						mediaInfo.setSrc("/mnt/sda/sda1/nativevod/now/Main/resource/shenzhen.mp4");
						break;
					default:
						break;
					}
				} else {
					mediaInfo.setLive(false);
				}
				list.add(mediaInfo);
			}
		}
		courtLiveAdapter = new CourtLiveAdapter(context, list);
		courtLiveGridView.setAdapter(courtLiveAdapter);
	}

	public void initListener() {
		courtLiveGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				location = arg2;
				if (list.get(arg2).isLive()) {
//					Uri videoUri = Uri.parse(list.get(arg2).getSrc());
//					clearHiPlayerView.setVideoURI(videoUri);
					clearHiPlayerView.setVideoPath(list.get(location).getSrc());
					clearHiPlayerView.start();
//					clearHiPlayerView.requestFocus(); 
				}
			}
		});
		container.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Toast.makeText(context, list.get(3).getName() + "", 0).show();
			}
		});
		
	}

	@Override
	public boolean onKeyDpadRight() {
		// TODO Auto-generated method stub
//		columnPosition++;
//		Log.e("123123", columnPosition + "");
//		if (columnPosition >= 1) {
//			columnPosition = 1;
//		}
		Log.e("eeeee", "onKeyDpadRight");
//		if (columnPosition >= 1) {
//			position = courtLiveGridView.getSelectedItemPosition();
//			courtLiveGridView.setSelection(-1);
//			courtLiveGridView.clearFocus();
//			courtLiveGridView.setFocusable(false);
//			container.setSelected(true);
//		}
		courtLiveGridView.clearFocus();
		courtLiveTempButton.requestFocus();
//		courtLiveTempButton.requestDisallowInterceptTouchEvent(true);
//		courtLiveTempButton.setClickable(true);
//		courtLiveTempButton.setSelected(true);
//		container.requestFocus();
		
		if(courtLiveTempButton.hasFocus()){
			Toast.makeText(context, "21312313", 0).show();
		}
		return true;
	}

	@Override
	public boolean onKeyDpadLeft() {
		// TODO Auto-generated method stub
		columnPosition--;
		Log.e("123123", columnPosition + "");
		if (columnPosition < 0) {
			columnPosition = 0;
		}
		Log.e("eeeee", "onKeyDpadLeft 到了");
		if (columnPosition < 1) {
			container.clearFocus();
			courtLiveGridView.setFocusable(true);
			courtLiveGridView.requestFocus();
			courtLiveGridView.setSelection(position);

			container.setSelected(false);
		}
		return true;
	}

	@Override
	public boolean onKeyDpadUp() {
		// TODO Auto-generated method stub
		position = courtLiveGridView.getSelectedItemPosition();
		courtLiveGridView.setFocusable(true);
		courtLiveGridView.setSelection(position);
		return true;
	}

	@Override
	public boolean onKeyDpadDown() {
		// TODO Auto-generated method stub
		Log.e("eeeee", "onKeyDpadLeft 到了");
		position = courtLiveGridView.getSelectedItemPosition();
		courtLiveGridView.setFocusable(true);
		courtLiveGridView.setSelection(position);
		return true;
	}

	@Override
	public boolean onKeyEnter() {
		// TODO Auto-generated method stub
		if (!courtLiveGridView.hasFocus()) {
			clearHiPlayerView.setVisibility(view.INVISIBLE);

			InterCutView interCutView = new InterCutView();
			interCutView.init(context, list.get(location).getSrc(), "");
			VoDViewManager.getInstance().pushForegroundView(interCutView);
		}
		return true;
	}

	@Override
	public boolean onKeyBack() {
		// TODO Auto-generated method stub
		clearHiPlayerView.pause();
		clearHiPlayerView.stopPlayback();
		clearHiPlayerView = null;
		
		VoDViewManager.getInstance().popForegroundView();
		return true;
	}

	@Override
	public void back() {
		// TODO Auto-generated method stub
		super.back();
		Log.e("aaaaa", "我被执行了");
		VoDViewManager.getInstance().hideLiveVideo();

		clearHiPlayerView.setVisibility(view.VISIBLE);

//		Uri videoUri = Uri.parse(list.get(location).getSrc());
//		clearHiPlayerView.setVideoURI(videoUri);
		clearHiPlayerView.setVideoPath(list.get(location).getSrc());
//		clearHiPlayerView.setVideoPath("http://192.168.0.66/nativevod/resource/cctv1.mp4");
		clearHiPlayerView.start();
//		clearHiPlayerView.requestFocus();  
	}
}
