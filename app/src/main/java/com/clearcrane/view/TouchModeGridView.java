package com.clearcrane.view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.GridView;

public class TouchModeGridView extends GridView {
	public TouchModeGridView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public TouchModeGridView(Context context,AttributeSet attrs){
		super(context, attrs);
	}
	/** 
	 * 屏蔽android4.4 setAdapter时View抢焦点的BUG 
	 */
	@Override
	public boolean isInTouchMode(){
		if(19 == Build.VERSION.SDK_INT){
			return!(hasFocus() && !super.isInTouchMode());
		}else{
			return super.isInTouchMode();
		}
}
}
