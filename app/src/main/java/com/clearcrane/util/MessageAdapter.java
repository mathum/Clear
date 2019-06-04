/**
 * @author xujifu
 * @copyright clear
 * @date 2014-06-17
 * @description 消息Adapter
 */
package com.clearcrane.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.clearcrane.view.VodMessageView.MessageInfo;
import com.clearcrane.vod.R;

import java.util.ArrayList;

public class MessageAdapter extends BaseAdapter {
	
	private Context mContext;
	private ArrayList<MessageInfo> list = new ArrayList<MessageInfo>();
	
	public MessageAdapter(Context context, ArrayList<MessageInfo> msg){
		mContext = context;
		list = msg;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
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
			convertView = (View)LayoutInflater.from(mContext).inflate(R.layout.message_item, null);
		}
		ImageView status = (ImageView)convertView.findViewById(R.id.message_item_status);
		TextView title = (TextView)convertView.findViewById(R.id.message_item_title);
		TextView msg = (TextView)convertView.findViewById(R.id.message_item_msg);
		
		if(list.get(position).isRead)
			status.setImageResource(R.drawable.msg_open);
		else
			status.setImageResource(R.drawable.msg_close);
		msg.setText(list.get(position).message);
		title.setText(list.get(position).time);
		
		return convertView;
	}

}
