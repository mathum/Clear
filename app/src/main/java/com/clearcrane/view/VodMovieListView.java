package com.clearcrane.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.clearcrane.adapter.MovieGridAdapter;
import com.clearcrane.adapter.MovieListAdapter;
import com.clearcrane.constant.ClearConstant;
import com.clearcrane.provider.MaterialRequest;
import com.clearcrane.provider.MaterialRequest.OnCompleteListener;
import com.clearcrane.schedule.DateUtil;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.LogUtils;
import com.clearcrane.util.TipDialog;
import com.clearcrane.vod.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class VodMovieListView extends VoDBaseView {
	private ListView movieList;
	private TouchModeGridView movieGrid;
	private TextView clockView;// 右上角显示时间。
	private TextView listName;
	private TextView pageSymbolText;// 显示页码
	public SharedPreferences activitySharePre;
	// private boolean isLeft = true; //是否在左侧分类列表
	private MovieListAdapter movieListAdapter;
	private MovieGridAdapter movieGridAdapter;
	private ArrayList<MovieClassData> moviesClassData = new ArrayList<MovieClassData>();
	private ArrayList<MovieData> moviesData = new ArrayList<MovieData>();// gridview用来显示的list对象,固定為每頁12個。
	private int listPosition = 0;
	private int gridPosition = 0;
	private int pageTotal = 0;// 每个分类下的视频集页数
	private int currentPage = 0;// 在gridview的当前页 默认首页为0；
	private boolean isUp;
	private boolean isFirst = true;
//	private ImageView whiteBorder;
	float curListX = 50;
	float curListY = 180;
	float curGridX = 370;
	float curGridY = 272;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
				Date curDate = new Date(DateUtil.getCurrentTimeMillSecond());// 获取当前时间       
				String str = formatter.format(curDate);
				clockView.setText(str);
				handler.sendEmptyMessageDelayed(0, 1000);
			}
		};
	};

	public void init(Context ctx, String u) {
		super.init(ctx, u);
		this.context = ctx;
		view = LayoutInflater.from(ctx).inflate(R.layout.movie_list, null);
		initLayoutInXml();
		initListener();
		/* trigger to get data sources */
		MaterialRequest mr = new MaterialRequest(ctx, ClearConfig.TYPE_JSON);
		mr.setOnCompleteListener(movieListOncompleteListener);
		mr.execute(this.url);// 暂时指定一个存在的url

		activitySharePre = context.getSharedPreferences(ClearConstant.Activity_FILE, Context.MODE_PRIVATE);
	
		movieGrid.clearFocus();
		movieList.requestFocus();
		movieList.setSelection(listPosition);
		
	}

	private void initListener() {
		movieGrid.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				// TODO Auto-generated method stub
				if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
					Log.e("xb1", "ACTION_DOWN");
					Log.e("xb1", "arg1:" + arg1 + "KEYCODE_DPAD_CENTER:" + KeyEvent.KEYCODE_DPAD_CENTER);
					if (arg1 == KeyEvent.KEYCODE_DPAD_UP) {
						if (gridPosition <= 3) {
							currentPage--;
							translationGridView(false);
						}
					} else if (arg1 == KeyEvent.KEYCODE_DPAD_DOWN) {
						// 当焦点在最下面一排的时候，需要切换page了
						if (gridPosition >= 8 || gridPosition >= moviesData.size() - 4) {
							currentPage++;
							translationGridView(true);
						}
					} else if (arg1 == KeyEvent.KEYCODE_DPAD_LEFT) {

					} else if (arg1 == KeyEvent.KEYCODE_DPAD_RIGHT) {
						// keyenter是为了适配暴风电视的确定事件
					} else if (arg1 == KeyEvent.KEYCODE_DPAD_CENTER || arg1 == KeyEvent.KEYCODE_ENTER) {
						Log.e("zxb", "gridPosition" + gridPosition);
						VoDMovieView voDMovieView = new VoDMovieView();
						if (moviesData.get(gridPosition).jsonURL != null
								&& !moviesData.get(gridPosition).jsonURL.equals("")) {
							voDMovieView.init(context, moviesData.get(gridPosition).jsonURL,
									moviesData.get(gridPosition).name);
							VoDViewManager.getInstance().pushForegroundView(voDMovieView);
							LogUtils.sendLogStart(mApp, "点播", "视频", moviesData.get(gridPosition).name);
						}
					}
				} else if (arg2.getAction() == KeyEvent.ACTION_UP) {
					Log.e("xb1", "ACTION_UP");
					return true;
				}
				return false;
			}
		});
		movieGrid.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				int i = 0;
				Log.e("dianying", "坐标:"+arg1.getX()+"----"+arg1.getY());
//				flyWhiteBorder(342, 222, arg1.getX(), arg1.getY(),false);
				for (MovieData movieData : moviesData) {
					if (i == arg2) {
						movieData.isFouse = true;
					} else {
						movieData.isFouse = false;
					}
					i++;
				}
				movieGridAdapter.notifyDataSetChanged();
				gridPosition = arg2;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});

		movieList.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				// 先分页，再加载到gridview。
				manualPaging();
				if (!isFirst) {
					Log.e("dianying", "x坐标"+arg1.getX()+"///"+arg1.getY());
//					flyWhiteBorder(240, 90, arg1.getX(), arg1.getY(),true);
					if (isUp) {
						translationGridView(true);
					} else {
						translationGridView(false);
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});
		movieList.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				// TODO Auto-generated method stub
				if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
					//首次操作前 先打开切换动画
					isFirst = false;
					if (arg1 == KeyEvent.KEYCODE_DPAD_UP) {
						listPosition--;
						if (listPosition < 0) {
							listPosition = moviesClassData.size() - 1;
							movieList.setSelection(listPosition);
						}
						isUp = true;
					} else if (arg1 == KeyEvent.KEYCODE_DPAD_DOWN) {
						listPosition++;
						if (listPosition > moviesClassData.size() - 1) {
							listPosition = 0;
							movieList.setSelection(listPosition);
						}
						isUp = false;
					}
				} else if (arg2.getAction() == KeyEvent.ACTION_UP) {
					Log.e("xb1", "ACTION_UP");
					return true;
				}
				return false;
			}
		});
	}

	private void initLayoutInXml() {
		movieList = (ListView) view.findViewById(R.id.movie_list_listview);
		movieGrid = (TouchModeGridView) view.findViewById(R.id.movie_list_gridview);
		listName = (TextView) view.findViewById(R.id.movie_list_name);
		pageSymbolText = (TextView) view.findViewById(R.id.movie_list_pagination_symbol);
		clockView = (TextView) view.findViewById(R.id.movie_list_time_clock);

//		whiteBorder = (ImageView) view.findViewById(R.id.white_boder);
		movieListAdapter = new MovieListAdapter(context, moviesClassData);
		movieList.setAdapter(movieListAdapter);

		movieGridAdapter = new MovieGridAdapter(context, moviesData);
		movieGrid.setAdapter(movieGridAdapter);
	}

	@Override
	public boolean onKeyDpadUp() {
		// TODO Auto-generated method stub
		Log.e("zxb", "onKeyDpadUp:" + movieList.hasFocus());
		return true;
	}

	@Override
	public boolean onKeyDpadDown() {
		// TODO Auto-generated method stub
		Log.e("zxb", "movieGrid+onKeyDpadDown:" + movieGrid.hasFocus());
		Log.e("zxb", "movieList+onKeyDpadDown:" + movieList.hasFocus());
		return true;
	}

	@Override
	public boolean onKeyDpadLeft() {
		// TODO Auto-generated method stub
		Log.e("zxb", "onkeydpadleft");
		if (movieList.hasFocus()) {
			Log.e("zxb", "onkeydpadleft:" + movieList.hasFocus());
			return true;
		} else {
			movieGrid.clearFocus();
			restoreGridView();
            
			movieList.requestFocus();
			movieList.setSelection(listPosition);
//			flyWhiteBorder(240, 90, movieList.getSelectedView().getX(), movieList.getSelectedView().getY(),true);
		}
		return true;
	}

	@Override
	public boolean onKeyDpadRight() {
		// TODO Auto-generated method stub
		Log.e("zxb", "onkeydpadright");
		if (movieList.hasFocus()) {
			Log.e("zxb", "onkeydpadright:" + movieList.hasFocus());
			movieList.clearFocus();
			movieGrid.requestFocus();
			moviesData.get(0).isFouse = true;
			movieGridAdapter.notifyDataSetChanged();
			movieGrid.setSelection(0);
//			flyWhiteBorder(342, 222, 0, 0,false);
		}
		return true;
	}

	@Override
	public void back() {
		// TODO Auto-generated method stub
		movieList.clearFocus();
		movieGrid.requestFocus();
		movieGrid.setSelection(gridPosition);
//		LogUtils.sendLogEnd(mApp, "点播", "视频", moviesData.get(gridPosition).name);		
		super.back();
	}

	@Override
	public boolean onKeyBack() {
		// TODO Auto-generated method stub
		if (movieGrid.hasFocus()) {
			movieGrid.clearFocus();
			movieList.requestFocus();
			movieList.setSelection(listPosition);

			restoreGridView();
			return true;
		} else {
			handler.removeMessages(0);
			return super.onKeyBack();
		}
	}

	public class MovieClassData {
		public String label; // 页面名字
		public String type;
		public String category;
		public boolean isFocuse;
		public int subCount;
		public ArrayList<MovieData> MoviesData = new ArrayList<MovieData>();
	}

	public class MovieData {
		public String name;
		public String episodes;// 集数
		public String iconURL;
		public String jsonURL;
		public String type;
		public boolean isFouse; // 是否被选中状态
		LinearLayout videoSetsView;// 单个视频集的图标
	}

	OnCompleteListener movieListOncompleteListener = new OnCompleteListener() {

		@Override
		public void onDownloaded(Object result) {
			// TODO Auto-generated method stub
			String VideoSetsTemplateJson = (String) result;
			Log.i("Json", "Json:" + VideoSetsTemplateJson);
			if (VideoSetsTemplateJson == null) {
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
				JSONTokener jsonParser = new JSONTokener(VideoSetsTemplateJson);
				JSONObject objectjson = (JSONObject) jsonParser.nextValue();

				String type = objectjson.getString("Type");
				String VideoView_label = objectjson.getString("Label");
				listName.setText(VideoView_label);
				listName.setTextSize(TypedValue.COMPLEX_UNIT_PX, ClearConfig.getScreenHeight() / 20);

				JSONArray contentArray = objectjson.getJSONArray("Content");
				for (int i = 0; i < contentArray.length(); i++) {
					int index = i;
					Log.i("XYL_video", "" + index);
					JSONObject objecttmp = (JSONObject) contentArray.opt(i);
					MovieClassData mainicon = new MovieClassData();
					mainicon.category = objecttmp.getString("category");
					mainicon.subCount = objecttmp.getInt("count");

					JSONArray secondArray = objecttmp.getJSONArray("videos");
					for (int j = 0; j < secondArray.length(); j++) {
						Log.i("XYL_video", "" + j);
						JSONObject secondTmp = (JSONObject) secondArray.opt(j);
						MovieData subicon = new MovieData();
						subicon.name = secondTmp.getString("name");
						Log.i("XYL_video", "" + subicon.name);
						subicon.iconURL = ClearConfig.getJsonUrl(context, secondTmp.getString("poster"));
						Log.i("XYL_video", "" + subicon.iconURL);
						subicon.episodes = secondTmp.getString("create_time");
						// subicon.jsonURL = ClearConfig.getJsonUrl(context,
						// secondTmp.getString("Json_URL"));
						// Log.i("XYL_video", "" + subicon.jsonURL);
						// subicon.type = secondTmp.getString("Type");
						JSONArray thirdArray = secondTmp.getJSONArray("materialContent");
						if (thirdArray.length() > 0) {
							subicon.jsonURL = ((JSONObject) thirdArray.opt(0)).getString("path");
						}
						mainicon.MoviesData.add(subicon);
					}
					//首次加载先暂时关闭切换动画
					isFirst = true;
					handler.sendEmptyMessage(0);
					moviesClassData.add(mainicon);
					movieListAdapter.notifyDataSetChanged();

					refreshMoviesData(moviesClassData.get(0).MoviesData, 0);
					movieGridAdapter.notifyDataSetChanged();
					
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onComplete(boolean result) {

			// TODO Auto-generated method stub
		}
	};

	private void refreshMoviesData(ArrayList<MovieData> tempMoviesData, int currentPage) {
		if (pageTotal == 0) {
			moviesData.clear();
		}
		if (currentPage >= pageTotal) {
			this.currentPage = pageTotal - 1;
			currentPage = pageTotal - 1;
		}
		if (currentPage < 0) {
			this.currentPage = 0;
			currentPage = 0;
		}
		moviesData.clear();
		for (int i = currentPage * 12; i < (currentPage + 1) * 12; i++) {
			if (i <= tempMoviesData.size() - 1) {
				moviesData.add(tempMoviesData.get(i));
			} else {
				break;
			}
		}
		pageSymbolText.setText((currentPage + 1) + "/" + pageTotal + "页");
	}

	private void manualPaging() {
		ArrayList<MovieData> mos = moviesClassData.get(listPosition).MoviesData;
		if (mos.size() % 12 == 0) {
			pageTotal = mos.size() / 12;
		} else {
			pageTotal = mos.size() / 12 + 1;
		}
	}

	// 向上以及向下的切换动画
	private void translationGridView(boolean isDown) {
		if (isDown == true) {
			/**
			 * 向下翻页 view出现时 view自身的动画效果，上移
			 */
			// 打开硬件加速
			movieGrid.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			ObjectAnimator animator1 = ObjectAnimator.ofFloat(movieGrid, "translationY", 0, -movieGrid.getHeight());
			animator1.setDuration(500);
			animator1.setInterpolator(new OvershootInterpolator());
			animator1.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					// TODO Auto-generated method stub
					super.onAnimationEnd(animation);
					// 切换中对gridview的数据逻辑处理
					refreshMoviesData(moviesClassData.get(listPosition).MoviesData, currentPage);
					gridPosition = 0;
					if (movieGrid.hasFocus()) {
						movieGrid.requestFocusFromTouch();
						movieGrid.setSelection(0);
					}

					movieGridAdapter.notifyDataSetChanged();
					ObjectAnimator animator2 = ObjectAnimator.ofFloat(movieGrid, "translationY", movieGrid.getHeight(),
							0f);
					animator2.setDuration(300);
					animator2.setInterpolator(new OvershootInterpolator());
					animator2.addListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							super.onAnimationEnd(animation);

							movieGrid.setLayerType(View.LAYER_TYPE_NONE, null);
						};
					});
					animator2.start();
				}
			});
			animator1.start();

			/**
			 * 
			 * view消失时 view自身的动画效果，上移
			 */

		} else {
			ObjectAnimator animator1 = ObjectAnimator.ofFloat(movieGrid, "translationY", 0f, movieGrid.getHeight());
			animator1.setDuration(500);
			animator1.setInterpolator(new OvershootInterpolator());
			animator1.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					// TODO Auto-generated method stub
					super.onAnimationEnd(animation);

					refreshMoviesData(moviesClassData.get(listPosition).MoviesData, currentPage);
					gridPosition = moviesData.size() - 1;
					if (movieGrid.hasFocus()) {
						movieGrid.requestFocusFromTouch();
						movieGrid.setSelection(gridPosition = moviesData.size() - 1);
					}
					movieGridAdapter.notifyDataSetChanged();

					ObjectAnimator animator2 = ObjectAnimator.ofFloat(movieGrid, "translationY", -movieGrid.getHeight(),
							0);
					animator2.setDuration(300);
					animator2.setInterpolator(new OvershootInterpolator());
					animator2.addListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							super.onAnimationEnd(animation);

							movieGrid.setLayerType(View.LAYER_TYPE_NONE, null);
						};
					});
					animator2.start();
				}
			});
			animator1.start();
		}
	}

	// 用于将gridview还原成所有item未选中状态
	private void restoreGridView() {
		for (MovieData movieData : moviesData) {
			movieData.isFouse = false;
		}
		movieGridAdapter.notifyDataSetChanged();
	}
//	/**
//	 * 白色焦点框飞动、移动、变大
//	 * 
//	 * @param width
//	 *                白色框的宽(非放大后的)
//	 * @param height
//	 *                白色框的高(非放大后的)
//	 * @param paramFloat1
//	 *                x坐标偏移量，相对于初始的白色框的中心点
//	 * @param paramFloat2
//	 *                y坐标偏移量，相对于初始的白色框的中心点
//	 * */
//	private void flyWhiteBorder(int width, int height, float paramFloat1, float paramFloat2,boolean isList) {
//		if ((this.whiteBorder != null)) {
//			int mWidth = this.whiteBorder.getWidth();
//			int mHeight = this.whiteBorder.getHeight();
//
//			if (mWidth == 0 || mHeight == 0) {
//				mWidth = 1;
//				mHeight = 1;
//			}
//			ViewPropertyAnimator localViewPropertyAnimator = whiteBorder.animate();
//			localViewPropertyAnimator.setDuration(150L);
//			localViewPropertyAnimator.scaleX((float) (width * 1.0) / (float) mWidth);
//			localViewPropertyAnimator.scaleY((float) (height * 1.0) / (float) mHeight);
//			if(isList){
//			localViewPropertyAnimator.x(paramFloat1+curListX);
//			localViewPropertyAnimator.y(paramFloat2+curListY);
//			}else{
//			Log.e("dianying", "paramFloat1+curGridX"+(paramFloat1+curGridX)+"----"+(paramFloat2+curGridY));
//			localViewPropertyAnimator.x(paramFloat1+curGridX);
//			localViewPropertyAnimator.y(paramFloat2+curGridY);
//			}
//			localViewPropertyAnimator.start();
//		}
//	}
}
