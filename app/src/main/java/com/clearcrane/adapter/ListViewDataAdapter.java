package com.clearcrane.adapter;

import java.util.List;

public class ListViewDataAdapter<T> extends ListViewDataAdapterBase<T> {
	
	protected List<T> mItemDataList;

	public ListViewDataAdapter(List<T> mItemDataList) {
		this.mItemDataList = mItemDataList;
	}
	
	@Override
	public void update(List<T> list) {
		this.mItemDataList = list;
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		
		if(mItemDataList == null)
			return 0;
		else
			return mItemDataList.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public T getItem(int position) {
		if(mItemDataList.size() <= position || position < 0)
			return null;	
		
		return mItemDataList.get(position);
	}

}
