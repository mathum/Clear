/**
 * @author xujifu
 * @copyright clear
 * @date 2014-06-17
 * @description 获取本地应用程序，创建adapter
 */
package com.clearcrane.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.clearcrane.vod.R;

import java.util.List;

public class AppListAdapter extends BaseAdapter {

	private Context mContext;
	private List<AppInfo> mList;
	public AppListAdapter(Context context, List<AppInfo> list){
		mContext = context;
		mList = list;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(convertView == null)
		{
			convertView = (View)LayoutInflater.from(mContext).inflate(R.layout.application_item, null);
		}
		ImageView pic = (ImageView)convertView.findViewById(R.id.application_item_pic);
		TextView tTv = (TextView)convertView.findViewById(R.id.application_item_title);
		TextView sTv = (TextView)convertView.findViewById(R.id.application_item_size);
		
		tTv.setText(mList.get(position).getAppName());
		sTv.setText("" + mList.get(position).getCodesize());
		pic.setImageDrawable((Drawable)mList.get(position).getIcon());
		return convertView;
	}

}
