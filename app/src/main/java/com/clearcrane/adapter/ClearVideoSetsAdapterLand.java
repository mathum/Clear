package com.clearcrane.adapter;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.clearcrane.util.MarqueeTextView;
import com.clearcrane.util.VideoInfo;
import com.clearcrane.vod.R;

import java.util.ArrayList;

public class ClearVideoSetsAdapterLand extends BaseAdapter {

	ArrayList<VideoInfo> list;
	Context mContext;
	int pageID = 0; // page number
	int offset = 1; // video index offset
	int index = 0;
	boolean focusable = false;
	private final int MAX_COUNT_PER_PAGE = 20;

	public int getSelectedIndex() {
		return index;
	}

	public VideoInfo getSelectedVideo() {
		return list.get(index + MAX_COUNT_PER_PAGE * pageID);
	}

	public void setSelectedIndex(int id) {
		this.index = id;
	}

	public int getPageId() {
		return pageID;
	}

	public boolean handleLeft() {
		if (index % 2 == 0) {
			return handlePageLeft();
		} else {
			index = index - 1;
			return true;
		}
	}

	public boolean handleRight() {
		if (index % 2 == 1) {
			return handlePageRight();
		} else {
			if (index < getCount() - 1)
				// 加入分页逻辑，每页页数
				index = index + 1;
			else {
				return false;
			}
		}

		return true;
	}

	public boolean handleUp() {
		Log.i("xuhonghua", "index:" + index);
		if (index > 1) {
			index -= 2;
			return true;
		} else {
			return false;
		}
	}

	public boolean handleDown() {
		Log.i("xuh", "index:" + index);
		if (index < getCount() - 2) {
			index += 2;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		int temp = list.size() - pageID * MAX_COUNT_PER_PAGE;
		return temp > MAX_COUNT_PER_PAGE ? MAX_COUNT_PER_PAGE : temp;
	}

	public boolean setPageId(int pId) {
		if (pId < 0) {
			return false;
		}
		// size -1 just for when size == MAX_COUNT_PER_PAGE*x
		if (pId > ((list.size() - 1) / MAX_COUNT_PER_PAGE)) {
			return false;
		}
		if (pageID == pId) {
			return false;
		}
		pageID = pId;
		offset = 1 + pageID * MAX_COUNT_PER_PAGE;
		return true;
	}

	private boolean handlePageRight() {
		if (setPageId(pageID + 1)) {
			if (index >= getCount() - 1) {
				index = getCount() % 2 == 0 ? getCount() - 2 : getCount() - 1;
			} else {
				index -= 1;
			}
			return true;
		}
		return false;
	}

	private boolean handlePageLeft() {
		if (setPageId(pageID - 1)) {
			index += 1;
			return true;
		}
		return false;
	}

	@Override
	public VideoInfo getItem(int arg0) {
		// TODO Auto-generated method stub
		return list.get(arg0);
	}

	public void setFocusable(boolean value) {
		this.index = 0;
		this.focusable = value;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {

		ViewHolder viewHolder = null;
		if (null == arg1) {
			viewHolder = new ViewHolder();
			LayoutInflater layoutInflater = LayoutInflater.from(mContext);
			arg1 = layoutInflater.inflate(R.layout.video_item_line, null);
			viewHolder.index = (TextView) arg1.findViewById(R.id.tv_video_item_index);
			viewHolder.content = (MarqueeTextView) arg1.findViewById(R.id.tv_video_item_content);
			arg1.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) arg1.getTag();
		}
		viewHolder.index.setText(String.valueOf(list.get(arg0).videoId));
		viewHolder.content.setAlignType(Gravity.LEFT);
		viewHolder.content.setText(list.get(arg0 + offset - 1).name);
//		arg1.setActivated(false);
//		if (index == arg0 && focusable) {
			// arg1.setSelected(true);
			//控制gridvidw中哪个view被选中
//			arg1.setActivated(true);
//		}
		return arg1;

	}

	public ClearVideoSetsAdapterLand(ArrayList<VideoInfo> list, Context context) {
		this.list = list;
		this.mContext = context;
	}

	class ViewHolder {
		TextView index;
		MarqueeTextView content;
	}
}
