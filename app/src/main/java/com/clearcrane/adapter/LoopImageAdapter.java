package com.clearcrane.adapter;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * viewpager循环轮播适配器
 * 
 * @author 朱健山 2016年1月25日 下午4:37:13
 */
public class LoopImageAdapter extends PagerAdapter
{

	protected ArrayList<ImageView> mObjects;
	protected Activity mActivity;

	public LoopImageAdapter(Activity mActivity, ArrayList<ImageView> mObjects)
	{
		this.mActivity = mActivity;
		this.mObjects = mObjects;
	}

	@Override
	public int getCount()
	{
		return mObjects.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1)
	{
		return arg0 == arg1;
	}

	@Override
	public ImageView instantiateItem(ViewGroup container, int position)
	{
		ImageView imageView = mObjects.get(position);

		if (imageView != null)
		{
			container.addView((View) imageView);
		}

		return imageView;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object)
	{
		container.removeView((View) object);
	}

	public ArrayList<ImageView> getObjects() {
		return mObjects;
	}

	public void setObjects(ArrayList<ImageView> objects) {
		this.mObjects = objects;
	}
	
}
