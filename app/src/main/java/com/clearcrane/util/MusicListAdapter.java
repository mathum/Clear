/**
 * @author xujifu
 * @copyright clear
 * @date 2014-06-17
 * @description 账单Adapter
 */
package com.clearcrane.util;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.clearcrane.databean.Mp3Info;
import com.clearcrane.vod.R;

import java.util.ArrayList;

public class MusicListAdapter extends BaseAdapter {

	private ArrayList<Mp3Info> musicList = new ArrayList<Mp3Info>();
	private Context mContext;

	public MusicListAdapter(Context context, ArrayList<Mp3Info> info) {
		mContext = context;
		musicList = info;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return musicList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return musicList.get(position);
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
        Holder holder = null;
		if (convertView == null) {
		    holder = new Holder();
			convertView = (View) LayoutInflater.from(mContext).inflate(R.layout.music_item, null);
			holder.seq = (TextView) convertView.findViewById(R.id.music_seq);
			holder.name = (TextView) convertView.findViewById(R.id.music_name);
			holder.duration = (TextView) convertView.findViewById(R.id.music_duration);
			holder.img = (ImageView) convertView.findViewById(R.id.music_state);
			convertView.setTag(holder);
		}else{
			holder = (Holder) convertView.getTag();
		}

		holder.seq.setText("" + (position + 1) + ".");

		holder.name.setText(musicList.get(position).name);

//
//		Long times = (long) musicList.get(position).duration * 1000;
//		Date dates = new Date(times);
//		SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");// 初始化Formatter的转换格式。
//
//		String ms = formatter.format(dates);

		holder.duration.setText(musicList.get(position).getNameEng());

		if (musicList.get(position).isPlay) {
			holder.img.setVisibility(View.VISIBLE);
			holder.img.setImageResource(R.drawable.music_play);
		} else {
			holder.img.setVisibility(View.INVISIBLE);
		}
		return convertView;
	}
     class Holder{
    	 private TextView seq;
    	 private TextView name;
    	 private TextView duration;
    	 private ImageView img;
     }
}
