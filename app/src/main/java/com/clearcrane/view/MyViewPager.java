package com.clearcrane.view;

import android.content.Context;
import android.support.v4.view.ViewPager;

public class MyViewPager extends ViewPager {

	public MyViewPager(Context context) {
		super(context);
	}
	
	@Override
	protected void onMeasure(int arg0, int arg1) {
		super.onMeasure(arg0, arg1);
	}

}
