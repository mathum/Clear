package com.clearcrane.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.clearcrane.vod.R;

/**
 * 页面加载对话框
 * @author SlientLeaves
 * 2016年6月29日  下午3:03:17
 */
public class MyProgressBarView extends LinearLayout {

	private Context mContext;
	
	public MyProgressBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		
		initView();
	}
	
	public MyProgressBarView(Context context) {
		super(context,null);
	}
	
	private View initView() {
		View view = View.inflate(mContext, R.layout.page_loading_progress_bar, this);
		return view;
	}

}
