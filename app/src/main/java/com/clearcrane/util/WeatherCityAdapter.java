/**
 * @author xujifu
 * @copyright clear
 * @date 2014-06-17
 * @description 天气Adapter
 */
package com.clearcrane.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.clearcrane.vod.R;

import java.util.ArrayList;

public class WeatherCityAdapter extends BaseAdapter {

	private ArrayList<String> city = new ArrayList<String>();
	private Context mContext;
	
	public WeatherCityAdapter(Context context, ArrayList<String> city){
		mContext = context;
		this.city = city;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return city.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return city.get(position);
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
			convertView = (View)LayoutInflater.from(mContext).inflate(R.layout.weather_city_item, null);
		}
		TextView itemName = (TextView)convertView.findViewById(R.id.weather_city_name);
		itemName.setText(city.get(position));
		return convertView;
	}

}
