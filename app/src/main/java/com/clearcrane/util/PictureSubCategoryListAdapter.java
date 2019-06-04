/**
 * @author xujifu
 * @copyright clear
 * @date 2014-06-17
 * @description 账单Adapter
 */
package com.clearcrane.util;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.clearcrane.view.VodPictureCategoryView.PictureSubCategory;
import com.clearcrane.vod.R;

import java.util.ArrayList;

public class PictureSubCategoryListAdapter extends BaseAdapter {

	private ArrayList<PictureSubCategory> picCategoryList = new ArrayList<PictureSubCategory>();
	private Context mContext;
	
	public PictureSubCategoryListAdapter(Context context, ArrayList<PictureSubCategory> info){
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
		
		if(convertView == null){
			convertView = (View)LayoutInflater.from(mContext).inflate(R.layout.picture_category_view_list_item, null);
		}
		
		PictureSubCategory pc = (PictureSubCategory) getItem(position);
		TextView text = ((TextView)convertView.findViewById(R.id.sub_category_item_name));
		text.setTextColor(Color.rgb(255, 255, 255));
		text.setText(pc.name);

		return convertView;
	}

}
