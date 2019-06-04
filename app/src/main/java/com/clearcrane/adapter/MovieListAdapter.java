package com.clearcrane.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clearcrane.view.VodMovieListView;
import com.clearcrane.vod.R;

import java.util.List;

public class MovieListAdapter extends BaseAdapter{
	private Context context;
    private List<VodMovieListView.MovieClassData> list;
    public MovieListAdapter(Context context,List<VodMovieListView.MovieClassData> list) {
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
			arg1 = LayoutInflater.from(context).inflate(R.layout.movie_list_item,null);
			holder.itemName = (TextView) arg1.findViewById(R.id.itemName);
			holder.focuse = (LinearLayout) arg1.findViewById(R.id.movie_item_focuse);
			arg1.setTag(holder);
		}else{
			holder = (Holder) arg1.getTag();
			}
		    holder.itemName.setText(list.get(arg0).category);
//		    if(list.get(arg0).isFocuse){
//		    	holder.focuse.setBackground(context.getResources().getDrawable(R.drawable.videotypebutton));
//		    }else{
//		    	holder.focuse.setBackground(null);
//		    }
		return arg1;
	}
	public class Holder{
    	public TextView itemName; //分类名称
    	private LinearLayout focuse;
//    	private ImageView rightArrow;//右箭头提示该item被选中
    }
}
