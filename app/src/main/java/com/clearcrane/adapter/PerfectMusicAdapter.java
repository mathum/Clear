package com.clearcrane.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.clearcrane.databean.Mp3Info;
import com.clearcrane.vod.R;
import com.clearcrane.vod.R.layout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PerfectMusicAdapter extends BaseAdapter{
    private Context context;
    private List<Mp3Info> list;
    public PerfectMusicAdapter(Context context, List<Mp3Info> list) {
		super();
		this.context = context;
		this.list = list;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list == null?0:list.size();
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
		if(arg1 == null){
			holder = new Holder();
		    arg1 = LayoutInflater.from(context).inflate(R.layout.perfect_music_item, null);
		    holder.songNum = (TextView) arg1.findViewById(R.id.perfect_music_number);
		    holder.songName = (TextView) arg1.findViewById(R.id.perfect_music_name);
		    holder.songSinger = (TextView) arg1.findViewById(R.id.perfect_music_singer);
		    holder.songDuration = (TextView) arg1.findViewById(R.id.perfect_music_duration);
		    holder.isPlay = (ImageView) arg1.findViewById(R.id.perfect_music_isPlay);
		    arg1.setTag(holder);
		}else{
		    holder = (Holder) arg1.getTag();
		}
		holder.songNum.setText(String.valueOf(arg0+1));
		holder.songName.setText(list.get(arg0).getName());
		holder.songSinger.setText(list.get(arg0).getSinger());
		holder.songSinger.setVisibility(View.INVISIBLE);
		Long times = (long) list.get(arg0).duration * 1000;
		Date dates = new Date(times);
		SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");// 初始化Formatter的转换格式。
		String ms = formatter.format(dates);
		holder.songDuration.setText(ms);
		
		if (list.get(arg0).isPlay) {
			holder.isPlay.setVisibility(View.VISIBLE);
			holder.songNum.setTextColor(Color.YELLOW);
			holder.songName.setTextColor(Color.YELLOW);
			holder.songDuration.setTextColor(Color.YELLOW);
		}else{
			holder.isPlay.setVisibility(View.INVISIBLE);		
			holder.songNum.setTextColor(Color.WHITE);
			holder.songName.setTextColor(Color.WHITE);
			holder.songDuration.setTextColor(Color.WHITE);
		}
		return arg1;
	}
    class Holder{
    	private TextView songNum;
    	private TextView songName;
    	private TextView songSinger;
    	private TextView songDuration;
    	private ImageView isPlay;
    }
}
