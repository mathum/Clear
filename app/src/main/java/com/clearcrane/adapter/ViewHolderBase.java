package com.clearcrane.adapter;

import android.view.LayoutInflater;
import android.view.View;

/**
 * viewHolder 父类
 * @author SlientLeaves
 * 2016年8月19日  下午4:16:04
 * @param <T>
 */
public abstract class ViewHolderBase<T> {
	
	protected int mPostion = -1;
	protected View mCurrentView;
	
	public void setItemData(int position,View view){
		mPostion = position;
		mCurrentView = view;
	}
	
	/*
	 * create a view from resource Xml file,and hold the view that
	 * may be used in displaying data.
	 */
	public abstract View createView(LayoutInflater layoutInflater );
	
	/**
	 * using the held views to display data
	 */
	public abstract void showData(int position,T itemData);

}
