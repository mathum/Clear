package com.clearcrane.view;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.clearcrane.adapter.ClearLivePrisonAdapter;
import com.clearcrane.constant.ClearConstant;
import com.clearcrane.databean.ChannelInfoData;
import com.clearcrane.log.ClearLog;
import com.clearcrane.provider.MaterialRequest;
import com.clearcrane.provider.MaterialRequest.OnCompleteListener;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.LogUtils;
import com.clearcrane.vod.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;

public class VoDPrisonLiveView extends VoDBaseView
        implements OnCompleteListener, OnItemSelectedListener, OnKeyListener {

    private final String TAG = "VodPrisonLiveVIew";
    private ListView prisonListView;
    private TextView showChannelInfo;
    private ClearLivePrisonAdapter prisonAdapter;
    private LinearLayout linearLayout;
    private long lastTime = System.currentTimeMillis();
    private ArrayList<ChannelInfoData> channel = new ArrayList<ChannelInfoData>();
    // 每页显示列表
    private ArrayList<ChannelInfoData> pageChannel = new ArrayList<ChannelInfoData>();
    // 判断电视菜单是否显示 true是显示 false是隐藏
    private boolean isShow = false;
    // 控制频道列表延时时间
    public static final long DURATION = 5000;
    // 控制频道显示延时时间
    public static final long CHANNNEL_DURATION = 5000;

    private static final long CLICK_JIAN_GE = 500;
    // 电视菜单显示隐藏的线程

    //设置当前是否是数字按键选台数字（ -1即为初始化状态）
    private int insertFocusIndex = -1;
    private boolean quickClicked = false;

    private int lastPosition = -1;

    private Runnable myRun = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			linearLayout.animate().alpha(0.0f).scaleY(0.0f).setListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationRepeat(Animator arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animator arg0) {
					// TODO Auto-generated method stub
					prisonListView.clearFocus();
					prisonListView.setVisibility(View.GONE);
					linearLayout.setVisibility(View.GONE);
				}

				@Override
				public void onAnimationCancel(Animator arg0) {
					// TODO Auto-generated method stub

				}
			}).start();

			// 设置动画结束后 listview失去焦点，让view获取焦点，保证用户可以无频道列表也可以切换频道

			isShow = false;
			// channelPosition = prisonListView.getSelectedItemPosition();

			// handler.sendEmptyMessage(0);
		}
	};

	private Runnable channelNumberRun = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			channelNum.setVisibility(view.GONE);

            if (insertFocusIndex != -1) {
                //开始判断是否需要更换频道
                if (channel != null && (channel.size()-1) >= insertFocusIndex) {
                    //更换频道
                    page = insertFocusIndex / pageSize;
                    pagePosition = insertFocusIndex % pageSize;
                    //设置频道列表信息
                    showInfo();
                    getPageChannel(page);
                    prisonListView.requestFocus();
                    prisonAdapter.notifyDataSetChanged();
                    //虽然我觉得没啥用处，代码延续，可能某些机型有问题。
                    prisonListView.setAdapter(prisonAdapter);
                    quickClicked = true;
                    Log.i(TAG,"pagePosition: " + pagePosition);
                    insertFocusIndex = -1;
                    changeChannel(pagePosition);
                }else{
                    insertFocusIndex = -1;
                }
            }
        }
    };

    //ListView隐藏的时候，设置选项方法
    private void changeChannel(int index){
        Log.i("Clear","---------------->"+ "index: " + index + "  " +pageChannel.size());
        Log.i(TAG,"---------------->"+ "index: " + index + "  " +pageChannel.get(index).getSrc().get(0));
        if (ClearConfig.checkNetwork(context) == ClearConfig.TYPE_LOCAL_STB) {
            VoDViewManager.getInstance()
                    .startBackgroundVideo(ClearConfig.getTFCard() + pageChannel.get(index).getSrc().get(0));
        } else {
            VoDViewManager.getInstance().stopLiveVideo();
            VoDViewManager.getInstance().startLiveVideo(pageChannel.get(index).getSrc().get(0));
            // Log.e("zxb", "movie start");
            // VoDViewManager.getInstance().showMovieVideo();
            // VoDViewManager.getInstance().startMovieVideo(pageChannel.get(arg2).getSrc().get(0));
            // 切换电视频道时，上传结束日志
            if (this.position != index) {
                LogUtils.sendLogEnd(mApp, "点播", "直播", channel.get(position).getName());
            }
            // 同步直接切换的位置和列表页面的位置，解决列表切换后，直接切换乱跳的bug
            position = index + page * pageSize;
            // 每次切换皮电脑都存储频道信息到本地，当被插播时，返回后可以继续播放。
            saveChannel(position);
            saveStatue(1, channel.get(position).getName());

            LogUtils.sendLogStart(mApp, "点播", "直播", channel.get(position).getName());
        }

        channelNum.setText(String.valueOf((pagePosition + page * pageSize + 1)));
        channelNum.setVisibility(view.VISIBLE);
        handler.postDelayed(channelNumberRun, CHANNNEL_DURATION);
    }
    // 记录用户点击了哪个频道 默认选中第一个频道
    // private int channelPosition = 1;
    // 记录用户在非面板操作下的频道位置,默认第一个
    private int position = 0;
    // 为了能够实现定时功能
    private Handler handler = new Handler();
    // 这是频道列表头
    // private View headView;
    // 右上角频道号码
    private TextView channelNum;

	private SharedPreferences sp;

	private int pageSize = 9;// 每页显示多少个频道
	private int page = 0;// 记录翻到第几页,默認為第一頁
	private int pageTotal = -1;// 总共有多少页
	private int pagePosition = 0;
	private OnInfoListener onInfoListener = new OnInfoListener() {
		@Override
		public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
			Log.e("winter","winter onInfoListener " + arg1 );
			// TODO Auto-generated method stub
			if (arg1 == MediaPlayer.MEDIA_INFO_BUFFERING_START || arg1 == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
				if (VoDViewManager.getInstance().isInLiveView && getChannel().endsWith("m3u8")) {
					Log.e("winter","winter start " + arg1 );
//					VoDViewManager.getInstance().showLiveVideo();
//					VoDViewManager.getInstance().startLiveVideo(getChannel());
					VoDViewManager.getInstance().startLiveViewRightNow();
					return true;
				}
			}
				//TODO,FIXME,himedia hls断流后再续上会出现1002.。。。
			else if(arg1 == 1002){
				Log.e("winter", "1002 chu xian le");
					if (VoDViewManager.getInstance().isInLiveView && getChannel().endsWith("m3u8")) {
						VoDViewManager.getInstance().stopLiveVideo();
						VoDViewManager.getInstance().showLiveVideo();
						VoDViewManager.getInstance().startLiveVideo(getChannel());
						return true;
				}
			}
			return false;
		}
	};
	
	private OnErrorListener mOnErrorListener = new OnErrorListener() {
		
		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			// TODO Auto-generated method stub
			try{
			VoDViewManager.getInstance().startLiveVideo(getChannel());
			}catch (Exception e){
				e.printStackTrace();
			}
			return false;
		}
	};
	

	public static SharedPreferences activitySharePre;
	public static SharedPreferences INTERCUTSharePre;

	@Override
	public void init(Context ctx, String u) {
		super.init(ctx, u);
		view = LayoutInflater.from(ctx).inflate(R.layout.prison_live_view, null);
		// headView =
		// LayoutInflater.from(ctx).inflate(R.layout.prison_live_view_head,
		// null);
		type = "live";
		url = u;
		initSharePre();
		initLayout();
	}

	public void initLayout() {
		linearLayout = (LinearLayout) view.findViewById(R.id.prison_live_view_LiearLayout);
		prisonListView = (ListView) view.findViewById(R.id.prison_live_view_listView);
		channelNum = (TextView) view.findViewById(R.id.prison_live_view_channelNumber);
		showChannelInfo = (TextView) view.findViewById(R.id.prison_live_view_title);

		prisonAdapter = new ClearLivePrisonAdapter(context, pageChannel);
		prisonListView.setAdapter(prisonAdapter);
		// prisonListView.setOnItemClickListener(this);
		prisonListView.setOnKeyListener(this);
		prisonListView.setOnItemSelectedListener(this);

		MaterialRequest mr = new MaterialRequest(context, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(this);
		mr.execute(url);

		VoDViewManager.getInstance().setLiveVideoDisplayArea(0, 0, ClearConfig.getScreenWidth(),
				ClearConfig.getScreenHeight());
		VoDViewManager.getInstance().showLiveVideo();

//		VoDViewManager.getInstance().getVideoView().setOnInfoListener(onInfoListener);
//		VoDViewManager.getInstance().getVideoView().setOnErrorListener(mOnErrorListener);

		linearLayout.setPivotX(0.0f);
		linearLayout.setPivotY(0.0f);

		linearLayout.setVisibility(View.VISIBLE);
		prisonListView.setVisibility(View.VISIBLE);
		prisonListView.requestFocus();

		isShow = true;

		channelNum.setText(String.valueOf(pagePosition + page * pageSize + 1));
		sp = context.getSharedPreferences("liveUrl", Context.MODE_PRIVATE);

		handler.postDelayed(myRun, DURATION);
	}

	// 直接摁上下切换频道
	@Override
	public boolean onKeyDpadUp() {
		// TODO Auto-generated method stub
		if(isClickToFast()) return true;
		position--;
		handler.removeCallbacks(channelNumberRun);
		if (position >= 0) {
			VoDViewManager.getInstance().stopLiveVideo();
			VoDViewManager.getInstance().startLiveVideo(channel.get(position).getSrc().get(0));
			Log.i("xb1", position + "onkeydpadup");
		} else {
			position = channel.size() - 1;
			VoDViewManager.getInstance().stopLiveVideo();
			VoDViewManager.getInstance().startLiveVideo(channel.get(position).getSrc().get(0));
			Log.i("xb1", position + "onkeydpadup");
		}
		saveChannel(position);
		saveStatue(1, channel.get(position).getName());
        channelNum.setText(String.valueOf(position + 1));
		channelNum.setVisibility(view.VISIBLE);
		handler.postDelayed(channelNumberRun, CHANNNEL_DURATION);
		return true;
	}

	private boolean isClickToFast(){
		long last = System.currentTimeMillis();
		if (last - lastTime < CLICK_JIAN_GE) {
//			lastTime = last;
			return true;
		}
		lastTime = last;
		return false;
	}
	
	@Override
	public boolean onKeyDpadDown() {
		// TODO Auto-generated method stub
		if(isClickToFast()) return true;
		position++;
		handler.removeCallbacks(channelNumberRun);
		if (position <= channel.size() - 1) {
			VoDViewManager.getInstance().stopLiveVideo();
			VoDViewManager.getInstance().startLiveVideo(channel.get(position).getSrc().get(0));
			Log.i("xb1", position + "onkeydpaddown");
		} else {
			position = 0;
			VoDViewManager.getInstance().stopLiveVideo();
			VoDViewManager.getInstance().startLiveVideo(channel.get(position).getSrc().get(0));
			Log.i("xb1", position + "onkeydpaddown");
		}
		saveChannel(position);
		saveStatue(1, channel.get(position).getName());
        channelNum.setText(String.valueOf(position + 1));
		channelNum.setVisibility(view.VISIBLE);
		handler.postDelayed(channelNumberRun, CHANNNEL_DURATION);
		return true;
	}

    // 呼出直播菜单列表
    @Override
    public boolean onKeyEnter() {
        if (insertFocusIndex != -1) {
            handler.post(channelNumberRun);
            return true;
        }
        long last = System.currentTimeMillis();
        if (last - lastTime < 300) {
            return true;
        }
        lastTime = last;
        // TODO Auto-generated method stub
        if (!isShow) {
            handler.removeCallbacks(myRun);

			// 电视列表打开
			linearLayout.animate().alpha(1.0f).scaleY(1.0f).setListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator arg0) {
					// TODO Auto-generated method stub
					linearLayout.setVisibility(View.VISIBLE);
					prisonListView.setVisibility(View.VISIBLE);
					prisonListView.requestFocus();

					// 每次打开都与直接切换频道的位置进行对齐
					if (position % pageSize == 0) {
						pagePosition = position;
					} else {
						page = position / pageSize;
						pagePosition = position % pageSize;
					}
					getPageChannel(page);
					prisonAdapter.notifyDataSetChanged();
				}

				@Override
				public void onAnimationRepeat(Animator arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animator arg0) {
					// TODO Auto-generated method stub
					prisonListView.setSelection(pagePosition);
				}

				@Override
				public void onAnimationCancel(Animator arg0) {
					// TODO Auto-generated method stub

				}
			}).start();

			isShow = true;
			showInfo();

			handler.postDelayed(myRun, DURATION);
		}
		return true;
	}
    public void turnOverTv(String Keys) {
    	Log.e("zol", Keys);
    };
	// 对返回键的操作
	@Override
	public boolean onKeyBack() {
		// TODO Auto-generated method stub
		// VoDViewManager.getInstance().stopLiveVideo();
		if (isShow) {
			linearLayout.animate().alpha(0.0f).scaleY(0.0f).setListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator arg0) {
					// TODO Auto-generated method stub
				}

				@Override
				public void onAnimationRepeat(Animator arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animator arg0) {
					// TODO Auto-generated method stub
					prisonListView.clearFocus();
					prisonListView.setVisibility(View.GONE);
					linearLayout.setVisibility(View.GONE);
					isShow = false;
				}

				@Override
				public void onAnimationCancel(Animator arg0) {
					// TODO Auto-generated method stub

				}
			}).start();
		} else {
			if (channel.size() > 0) {
				LogUtils.sendLogEnd(mApp, "点播", "直播", channel.get(position).getName());
			}
			saveStatue(3, "");
			VoDViewManager.getInstance().isInLiveView = false;
			VoDViewManager.getInstance().getVideoView().setOnInfoListener(null);
			VoDViewManager.getInstance().getVideoView().setOnErrorListener(null);
			handler.removeCallbacks(channelNumberRun);
			handler.removeCallbacks(myRun);
			handler.removeCallbacks(checkPlayerIsPlaying);
			VoDViewManager.getInstance().hideLiveVideo();
			VoDViewManager.getInstance().popForegroundView();
		}
		return true;
	}

	// 走接口获取频道数据
	@Override
	public void onDownloaded(Object result) {
		// TODO Auto-generated method stub
		String ChannelJson = (String) result;
		if (ChannelJson == null) {
//			TipDialog.Builder builder = new TipDialog.Builder(context);
//			builder.setMessage("当前网络不可用，请检查网络");
//			builder.setTitle("提示");
//			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int which) {
//					dialog.dismiss();
//					// 设置你的操作事项
//				}
//			});
//
//			builder.create().show();
			Log.e(TAG,"获取数据失败！网络有问题");
			return;
		}
		try {
			JSONTokener jsonParser = new JSONTokener(ChannelJson);
			JSONObject mainViewObj = (JSONObject) jsonParser.nextValue();
			JSONArray contentArray = (JSONArray) mainViewObj.getJSONArray("Content");
			for (int i = 0; i < contentArray.length(); i++) {
				JSONObject objecttmp = (JSONObject) contentArray.opt(i);
				ChannelInfoData channelinfo = new ChannelInfoData();
				channelinfo.setName(objecttmp.getString("ChannelName"));

				channelinfo.setNum(objecttmp.getString("ChannelNum"));
				channelinfo.setPicUrl(ClearConfig.getJsonUrl(context, objecttmp.getString("ChannelPic")));
				JSONArray secondArray = (JSONArray) objecttmp.getJSONArray("ChannelSrc");
				for (int j = 0; j < secondArray.length(); j++) {
					JSONObject secondtmp = (JSONObject) secondArray.opt(j);
					channelinfo.getSrc().add(secondtmp.getString("Src"));
				}
				channel.add(channelinfo);
			}

			if (channel.size() % pageSize == 0) {
				pageTotal = channel.size() / pageSize;
			} else {
				pageTotal = channel.size() / pageSize + 1;
			}
			// 默认显示第一页
			getPageChannel(0);
			// 刷新数据
			prisonAdapter.notifyDataSetChanged();
			// 设置位置为第一个
			prisonListView.setSelection(position);
			// 显示列表头信息
			showInfo();
			// 默认播放第一个频道
			if (channel.size() > 0) {
				VoDViewManager.getInstance().startLiveVideo(channel.get(0).getSrc().get(0));
				handler.post(checkPlayerIsPlaying);
			}

		} catch (JSONException e) {
			ClearLog.LogError("BROSWER\tLoad\tFAIL\t0ms\t" + url);
			e.printStackTrace();
		}
	}

	@Override
	public void onComplete(boolean result) {
		// TODO Auto-generated method stub

	}

    // 处理listview点击item播放
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub
        handler.removeCallbacks(myRun);
        handler.removeCallbacks(channelNumberRun);
        if(lastPosition != arg2){
            lastPosition = arg2;
            if (ClearConfig.checkNetwork(context) == ClearConfig.TYPE_LOCAL_STB) {
                VoDViewManager.getInstance()
                        .startBackgroundVideo(ClearConfig.getTFCard() + pageChannel.get(arg2).getSrc().get(0));
            } else {
                VoDViewManager.getInstance().stopLiveVideo();
                VoDViewManager.getInstance().startLiveVideo(pageChannel.get(arg2).getSrc().get(0));
                // Log.e("zxb", "movie start");
                // VoDViewManager.getInstance().showMovieVideo();
                // VoDViewManager.getInstance().startMovieVideo(pageChannel.get(arg2).getSrc().get(0));
                // 切换电视频道时，上传结束日志
                if (position != arg2) {
                    LogUtils.sendLogEnd(mApp, "点播", "直播", channel.get(position).getName());
                }
                // 同步直接切换的位置和列表页面的位置，解决列表切换后，直接切换乱跳的bug
                position = arg2 + page * pageSize;
                // 每次切换皮电脑都存储频道信息到本地，当被插播时，返回后可以继续播放。
                saveChannel(arg2 + page * pageSize);
                saveStatue(1, channel.get(position).getName());

                LogUtils.sendLogStart(mApp, "点播", "直播", channel.get(position).getName());
            }
        }
        channelNum.setText(String.valueOf((pagePosition + page * pageSize + 1)));
        channelNum.setVisibility(view.VISIBLE);

		handler.postDelayed(myRun, DURATION);
		handler.postDelayed(channelNumberRun, CHANNNEL_DURATION);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		Log.e("xb", "onNothingSelected");
	}

	// 设置频道列表的信息栏
	private void showInfo() {
		StringBuilder s = new StringBuilder(
				"频道列表" + "(" + (pagePosition + page * pageSize + 1) + "/" + channel.size() + ")");
		showChannelInfo.setText(s);
	}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Log.i(TAG, "OnKeyDown: " + keyCode + "  event: " +event);
        if (super.onKeyDown(keyCode, event)) {
            return true;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_0) {
                insertChannelIndex(0);
            } else if (keyCode == KeyEvent.KEYCODE_1) {
                insertChannelIndex(1);
            } else if (keyCode == KeyEvent.KEYCODE_2) {
                insertChannelIndex(2);
            } else if (keyCode == KeyEvent.KEYCODE_3) {
                insertChannelIndex(3);
            } else if (keyCode == KeyEvent.KEYCODE_4) {
                insertChannelIndex(4);
            } else if (keyCode == KeyEvent.KEYCODE_5) {
                insertChannelIndex(5);
            } else if (keyCode == KeyEvent.KEYCODE_6) {
                insertChannelIndex(6);
            } else if (keyCode == KeyEvent.KEYCODE_7) {
                insertChannelIndex(7);
            } else if (keyCode == KeyEvent.KEYCODE_8) {
                insertChannelIndex(8);
            } else if (keyCode == KeyEvent.KEYCODE_9) {
                insertChannelIndex(9);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    // 对listview上下按键的单独处理，主要是为了解决与view的上下按键处理的冲突,尽量对按键进行单独处理，否则容易导致bug
    @Override
    public boolean onKey(View arg0, int arg1, KeyEvent arg2) {

        Log.i(TAG, "arg1: " + arg1 + "  arg2: " + arg2);
        // TODO Auto-generated method stub
        Boolean value = false;
        // 判断按下和弹起两个时间动作，主要是为了处理onkey监听会被走两遍的问题，我们只需要在按键按下时处理一次。
        if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
            if (isClickToFast()) return true;
            // 向上
            if (arg1 == KeyEvent.KEYCODE_DPAD_UP) {
                handler.removeCallbacks(myRun);
                handler.postDelayed(myRun, DURATION);
                // 为了让频道列表有上下的界限
                pagePosition--;
                Log.e("xb", "pagePosition-before:" + pagePosition);
                if (pagePosition >= 0) {
                    showInfo();
                    value = false;
                } else {
                    page--;
                    if (page < 0) {
                        page = pageTotal - 1;
                    }
                    getPageChannel(page);

					prisonListView.requestFocus();
					prisonAdapter.notifyDataSetChanged();
					prisonListView.setAdapter(prisonAdapter);

					pagePosition = pageChannel.size() - 1;
					Log.e("xb", "pagePosition-after:" + pagePosition);
					prisonListView.setSelection(pagePosition);
					showInfo();
				}
				value = false;
			} else if (arg1 == KeyEvent.KEYCODE_DPAD_DOWN) {// 向下
				handler.removeCallbacks(myRun);
				handler.postDelayed(myRun, DURATION);

				pagePosition++;
				Log.e("xb", "pagePosition-before:" + pagePosition);
				if (pagePosition <= pageChannel.size() - 1) {
					prisonListView.requestFocus();
					showInfo();
					value = false;
				} else {
					page++;
					if (page > pageTotal - 1) {
						page = 0;
					}
					getPageChannel(page);
					prisonListView.requestFocus();
					prisonAdapter.notifyDataSetChanged();
					// 重新绑定适配器，是为了解决翻页后，setselection方法不能讲焦点指向第一个而是指向第二个的问题。
					prisonListView.setAdapter(prisonAdapter);

					pagePosition = 0;
					prisonListView.setSelection(pagePosition);
					showInfo();
				}
				value = false;
			} else if (arg1 == KeyEvent.KEYCODE_DPAD_LEFT) {// 向左
				// 向左翻页
				handler.removeCallbacks(myRun);
				handler.postDelayed(myRun, DURATION);
				page--;
				if (page < 0) {
					page = 0;
				}
				getPageChannel(page);
				prisonAdapter.notifyDataSetChanged();
				prisonListView.setAdapter(prisonAdapter);
				pagePosition = 0;

				prisonListView.setSelection(pagePosition);
				showInfo();

			} else if (arg1 == KeyEvent.KEYCODE_DPAD_RIGHT) {// 向右
				// 向右翻页
				handler.removeCallbacks(myRun);
				handler.postDelayed(myRun, DURATION);
				page++;
				if (page > pageTotal - 1) {
					page = pageTotal - 1;
				}
				getPageChannel(page);
				prisonAdapter.notifyDataSetChanged();
				prisonListView.setAdapter(prisonAdapter);
				pagePosition = 0;
				prisonListView.setSelection(pagePosition);
				showInfo();
			}
		} else if (arg2.getAction() == KeyEvent.ACTION_UP) {
			Log.e("xb1", "ACTION_UP");
			return true;
		}
		return value;
	}

    private void insertChannelIndex(int index) {

        //减一的目的是：台号为12 ，实际channel索引为11.
        handler.removeCallbacks(channelNumberRun);

        if (insertFocusIndex == -1) {
            insertFocusIndex = index - 1;
        } else if (insertFocusIndex < 100) {
            insertFocusIndex = 10 * (insertFocusIndex + 1) + index - 1;
        } else if (insertFocusIndex > 100) {
            insertFocusIndex = 10 * ((insertFocusIndex + 1) % 100) + index - 1;
        }

        Log.i(TAG, "insertFocusIndex :" + insertFocusIndex);

        channelNum.setText("" + (insertFocusIndex + 1));
        channelNum.setVisibility(View.VISIBLE);

        handler.postDelayed(channelNumberRun, ClearConstant.LIVE_INSERT_SHOW_TIME);
    }

    // 保存频道 防止插播之后返回黑屏
    private void saveChannel(int pos) {
        VoDViewManager.getInstance().isInLiveView = true;
        Editor editor = sp.edit();
        editor.putString("url", channel.get(pos).getSrc().get(0));
        editor.putString("live_name", channel.get(pos).getName());
        editor.commit();
    }

	// 返回上次保存的频道
	public String getChannel() {
		String cl = sp.getString("url", "");
		return cl;
	}

	// 根据页码获取对应的频道
	private void getPageChannel(int page) {
		pageChannel.clear();
		// 如果是只有一页
		if (pageTotal <= 1) {
			for (ChannelInfoData c : channel) {
				pageChannel.add(c);
			}
		} else {// 如果多于一页
			int j = page * pageSize;// 每页的第一个频道
			for (int i = 0; i < pageSize; i++) {
				if (j < channel.size()) {
					ChannelInfoData channelInfoData = channel.get(j);
					pageChannel.add(channelInfoData);
					j++;
				}
			}
		}
	}

	private void initSharePre() {

		activitySharePre = context.getSharedPreferences(ClearConstant.Activity_FILE, Context.MODE_PRIVATE);
		INTERCUTSharePre = context.getSharedPreferences(ClearConstant.INTERCUT_FILE, Context.MODE_PRIVATE);

	}

	// 保存当前播放状态 用于给后台展示终端状态
	private void saveStatue(int state, String movieName) {
		Editor editor = activitySharePre.edit();
		editor.putInt(ClearConstant.Play_Statue, state);
		editor.putString(ClearConstant.Movie_NAME, movieName);
		editor.commit();
	}
	
private Runnable checkPlayerIsPlaying = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.e("lilei","check check mp is " + VoDViewManager.getInstance().isVideoViewStatues() + " " + VoDViewManager.getInstance().getVideoDuration());
			handler.postDelayed(checkPlayerIsPlaying, 8000);
		}
	};
}
