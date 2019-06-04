package com.clearcrane.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * listview adapter父类
 * @author SlientLeaves
 * 2016年8月19日  下午4:14:16
 * @param <T>
 */
public abstract class ListViewDataAdapterBase<T> extends BaseAdapter{

	protected  ViewHolderCreator<T> mViewHolderCreator;
	
	public void setViewHolderClass(final Object enclosingInstance,final 
			Class<?> clazz,final Object... args){
		mViewHolderCreator = ViewHolderCreator.create(enclosingInstance, clazz, args);
	}
	
	@Override
	public abstract T getItem(int position);
	
	public abstract void update(List<T> list);

	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		T itemData = getItem(position);
		ViewHolderBase<T> viewHolder = null;
		
		if(convertView == null || (!(convertView.getTag() instanceof ViewHolderBase<?>))){
			viewHolder = createViewHolder(position);
			if(viewHolder != null){
				convertView = viewHolder.createView(LayoutInflater.from(parent.getContext()));
				if(convertView != null){
					convertView.setTag(viewHolder);
				}
			}
			
		}else{
			viewHolder = (ViewHolderBase<T>) convertView.getTag();
		}
		
		if(viewHolder != null){
			viewHolder.setItemData(position, convertView);
			viewHolder.showData(position, itemData);
		}
		return convertView;
	}

	private ViewHolderBase<T> createViewHolder(int position) {
		
		if(mViewHolderCreator == null){
			throw new RuntimeException("view holder creator is null");
		}
		
		if(mViewHolderCreator != null){
			
			return mViewHolderCreator.createViewHolder(position);
		}
		
		return null;
	}

}
