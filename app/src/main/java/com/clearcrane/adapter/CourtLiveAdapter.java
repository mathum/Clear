package com.clearcrane.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.clearcrane.databean.MediaInfo;
import com.clearcrane.vod.R;
import com.clearcrane.vod.R.color;

import java.util.List;

public class CourtLiveAdapter extends BaseAdapter{
    private List<MediaInfo> list;
    private Context context;
    public CourtLiveAdapter(Context context,List<MediaInfo> list) {
		// TODO Auto-generated constructor stub
    	this.list = list;
    	this.context = context;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list == null ? 0:list.size();
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

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		Holder holder = null;
		if(arg1 == null){
			holder = new Holder();
			arg1 = LayoutInflater.from(context).inflate(R.layout.court_live_item, null);
			holder.button = (Button) arg1.findViewById(R.id.court_live_item_button);
			arg1.setTag(holder);
		}else{
			holder = (Holder) arg1.getTag();
		}
		holder.button.setText(list.get(arg0).getName());
		if(list.get(arg0).isLive()){
			holder.button.setBackground(context.getResources().getDrawable(color.grass_green));
		}else{
			holder.button.setBackground(context.getResources().getDrawable(color.court_live_item_unfocus));

		}
		return arg1;
	}
    class Holder{
    	private Button button;
    }
}
