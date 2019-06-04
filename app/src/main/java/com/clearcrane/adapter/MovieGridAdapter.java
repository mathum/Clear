package com.clearcrane.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.clearcrane.util.ImageUtil;
import com.clearcrane.util.MarqueeTextView;
import com.clearcrane.view.VodMovieListView;
import com.clearcrane.vod.R;

import java.util.List;

public class MovieGridAdapter extends BaseAdapter{
	private Context context;
    private List<VodMovieListView.MovieData> list;
    public MovieGridAdapter(Context context,List<VodMovieListView.MovieData> list) {
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
			arg1 = LayoutInflater.from(context).inflate(R.layout.movie_grid_item,null);
			holder.MovieImage = (ImageView) arg1.findViewById(R.id.movie_grid_pic);
			holder.movieTotal = (TextView) arg1.findViewById(R.id.movie_grid_text);
			holder.movieName = (MarqueeTextView) arg1.findViewById(R.id.movie_grid_name);
			holder.border = (ImageView) arg1.findViewById(R.id.movie_grid_pic_border);
			arg1.setTag(holder);
		}else{
			holder = (Holder) arg1.getTag();
			}
		    ImageUtil.displayImage(list.get(arg0).iconURL, holder.MovieImage);
		    holder.movieName.setText(list.get(arg0).name);
		    holder.movieTotal.setText("于"+list.get(arg0).episodes+"创建");
		    if(list.get(arg0).isFouse){
		    	holder.border.setVisibility(View.VISIBLE);
				holder.movieName.startFor0();
		    }else{
		    	holder.border.setVisibility(View.INVISIBLE);
				holder.movieName.stopScroll();
		    }
//		    if(list.get(arg0).isFocuse){
//		    	holder.focuse.setBackground(context.getResources().getDrawable(R.drawable.videotypebutton));
//		    }else{
//		    	holder.focuse.setBackground(null);
//		    }
		return arg1;
	}
	class Holder{
    	private ImageView MovieImage; //分类名称
    	private TextView movieTotal;
    	private MarqueeTextView movieName;
    	public ImageView border;
//    	private ImageView rightArrow;//右箭头提示该item被选中
    }
}
