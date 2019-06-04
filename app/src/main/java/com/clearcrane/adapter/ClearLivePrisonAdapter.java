package com.clearcrane.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.clearcrane.databean.ChannelInfoData;
import com.clearcrane.util.ImageUtil;
import com.clearcrane.vod.R;

import java.util.List;

public class ClearLivePrisonAdapter extends BaseAdapter{
    private Context context;
    private List<ChannelInfoData> list;
    public ClearLivePrisonAdapter(Context context,List<ChannelInfoData> list) {
		// TODO Auto-generated constructor stub
    	this.context = context;
    	this.list = list;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list == null?null:list.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		Holder holder = null;
		if(holder == null){
			holder = new Holder();
			arg1 = LayoutInflater.from(context).inflate(R.layout.prison_channel_item,null);
			holder.liveNumber = (TextView) arg1.findViewById(R.id.channel_item_num);
			holder.livePic = (ImageView) arg1.findViewById(R.id.channel_item_pic);
			holder.liveName = (TextView) arg1.findViewById(R.id.channel_item_name);
			arg1.setTag(holder);
		}else{
			holder = (Holder) arg1.getTag();
			}
		    holder.liveNumber.setText(list.get(arg0).getNum());
		    ImageUtil.displayImage(list.get(arg0).getPicUrl(), holder.livePic);
		    holder.liveName.setText(list.get(arg0).getName());
		return arg1;
	}
    class Holder{
    	private TextView liveNumber;
    	private ImageView livePic;
    	private TextView liveName;
    }
}
