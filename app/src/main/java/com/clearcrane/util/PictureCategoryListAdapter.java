/**
 * @author xujifu
 * @copyright clear
 * @date 2014-06-17
 * @description 账单Adapter
 */
package com.clearcrane.util;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.clearcrane.view.VodPictureListView.PictureCategory;
import com.clearcrane.vod.R;

import java.util.ArrayList;

public class PictureCategoryListAdapter extends BaseAdapter {

	private ArrayList<PictureCategory> picCategoryList = new ArrayList<PictureCategory>();
	private Context mContext;
	
	public PictureCategoryListAdapter(Context context, ArrayList<PictureCategory> info){
		mContext = context;
		picCategoryList = info;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return picCategoryList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return picCategoryList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		Log.i("Leo test", "getView by pic");
		
		if(convertView == null){
			convertView = (View)LayoutInflater.from(mContext).inflate(R.layout.picture_list_view_list_item, null);
		}
		
		PictureCategory pc = (PictureCategory) getItem(position);
		TextView text = ((TextView)convertView.findViewById(R.id.pic_list_item_name));
		text.setTextColor(Color.rgb(255, 255, 255));
		text.setText(pc.name);

		return convertView;
	}

}
