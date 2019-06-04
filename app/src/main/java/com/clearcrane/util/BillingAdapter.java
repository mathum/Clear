/**
 * @author xujifu
 * @copyright clear
 * @date 2014-06-17
 * @description 账单Adapter
 */
package com.clearcrane.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clearcrane.vod.R;

import java.util.ArrayList;

public class BillingAdapter extends BaseAdapter {

	private ArrayList<BillingInfo> billing = new ArrayList<BillingInfo>();
	private Context mContext;
	
	public BillingAdapter(Context context, ArrayList<BillingInfo> info){
		mContext = context;
		billing = info;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return billing.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return billing.get(position);
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
			convertView = (View)LayoutInflater.from(mContext).inflate(R.layout.billing_item, null);
		}
		TextView itemName = (TextView)convertView.findViewById(R.id.billing_item_name);
		itemName.setText(billing.get(position).title + "(" + billing.get(position).time + ")");
		billing.get(position).detialsLayout = (LinearLayout)convertView.findViewById(R.id.billing_item_detials);
		for(int i = 0; i < billing.get(position).details.size(); i ++){
			billing.get(position).details.get(i).detailsLayout = (LinearLayout)LayoutInflater.from(mContext)
					.inflate(R.layout.billingdetials_item, null);
			TextView detailsTitle = (TextView)billing.get(position).details.get(i).detailsLayout.findViewById(
					R.id.billing_detials_item_title);
			TextView detailsCount = (TextView)billing.get(position).details.get(i).detailsLayout.findViewById(
					R.id.billing_detials_item_count);
			TextView detailsPrice = (TextView)billing.get(position).details.get(i).detailsLayout.findViewById(
					R.id.billing_detials_item_price);
			detailsTitle.setText(billing.get(position).details.get(i).title);
			detailsCount.setText(billing.get(position).details.get(i).count);
			detailsPrice.setText(mContext.getString(R.string.mark) + billing.get(position).details.get(i).price);
			billing.get(position).detialsLayout.addView(billing.get(position).details.get(i).detailsLayout);
		}
		return convertView;
	}

}
