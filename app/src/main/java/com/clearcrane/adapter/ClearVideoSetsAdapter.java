package com.clearcrane.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.clearcrane.util.VideoInfo;
import com.clearcrane.vod.R;

import java.util.ArrayList;

public class ClearVideoSetsAdapter extends BaseAdapter{


		ArrayList<VideoInfo> list;
		Context mContext;
		int pageID = 0;  //page number
		int offset = 1; //video index offset
		int index = 0;
		int max_index_of_last_page = 20;
		int page_counts;
		boolean focusable = false;
		private final int MAX_COUNT_PER_PAGE = 20;
		
		public int getSelectedIndex(){
			return index;
		}
		
		public VideoInfo getSelectedVideo(){
			return list.get(index + MAX_COUNT_PER_PAGE * pageID);
		}
		
		public void setSelectedIndex(int id){
			this.index = id;
		}
		
		public int getPageId(){
			return pageID;
		}
		
		
		public boolean handleLeft(){
			if(pageID == 0){
				if(index < 10){
					return false;
				}else {
					index -= 10;
				}
			}else if(pageID > 0){
				if(index < 10){
					return handlePageLeft();
				}else{
					index -= 10;
					return true;
				}
			}
				return false;
		}
		
		public boolean handleRight(){
			if(index > 10){
				return handlePageRight();
			}else{
				if(pageID == page_counts - 1){
					if(max_index_of_last_page < 10){
						return false;
					}
					index = (index + 10) > max_index_of_last_page ? max_index_of_last_page : (index + 10);
				}else{
					index += 10;
				}
			}
			return true;
		}
		
		public boolean handleUp(){
			if(index == 0){
				if(setPageId(pageID - 1)){
					index = 19;
					return true;
				}else{
					return false;
				}
			}
			if(index > 0){
				index -= 1;
				return true;
			}else{
				return true;
			}
		}
		
		public boolean handleDown(){
			if(page_counts - 1 == pageID){
				if(index >= max_index_of_last_page){
					return false;
				}
				index += 1;
				return true;
			}else{
				if(index == 19){
					if(setPageId(pageID + 1)){ // must be true?
						index = 0;
						return true;
					}
				}else{
					index += 1;
					return true;
				}
				return false;
			}
		}
		
		
		@Override
		public int getCount() {
			int temp = list.size() - pageID * MAX_COUNT_PER_PAGE;
			return temp > MAX_COUNT_PER_PAGE?MAX_COUNT_PER_PAGE:temp;
		}

		public boolean setPageId(int pId){
			if (pId < 0){
				return false;
			}
			//size -1 just for when size == MAX_COUNT_PER_PAGE*x
			if (pId >= page_counts){
				return false;
			}
			if(pageID == pId){
				return false;
			}
			pageID = pId;
			offset = 1 + pageID * MAX_COUNT_PER_PAGE;
			return true;
		}
		
		private boolean handlePageRight(){
			if(setPageId(pageID + 1)){
				if (pageID + 2 == page_counts){//pageID + 1  is last page
					index = (index - 10) > max_index_of_last_page ? max_index_of_last_page : (index - 10); 
				}else{
					index -= 10;
				}
				return true;
			}
			return false;
		}
		
		private boolean handlePageLeft(){
			if(setPageId(pageID - 1)){
				index += 10;
				return true;
			}
			return false;
		}
		
		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		public void setFocusable(boolean value){
			this.index = 0;
			this.focusable = value;
		}
		
		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {

			ViewHolder viewHolder = null;
			if (null == arg1) {
				viewHolder = new ViewHolder();
				LayoutInflater layoutInflater = LayoutInflater.from(mContext);
				arg1 = layoutInflater.inflate(R.layout.video_item_line, null);
				viewHolder.index = (TextView) arg1
						.findViewById(R.id.tv_video_item_index);
				viewHolder.content = (TextView) arg1
						.findViewById(R.id.tv_video_item_content);
				arg1.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) arg1.getTag();
			}
			viewHolder.index.setText((arg0 + offset) + "");
			viewHolder.content.setText(list.get(arg0 + offset - 1).name);
			arg1.setActivated(false);
			if (index == arg0 && focusable){
//				arg1.setSelected(true);
				arg1.setActivated(true);
			}
			return arg1;

		}

		public ClearVideoSetsAdapter(ArrayList<VideoInfo> list, Context context) {
			this.list = list;
			this.mContext = context;
			this.page_counts = (list.size()  - 1)/ 20 + 1;
			this.max_index_of_last_page = list.size() - (page_counts - 1) * MAX_COUNT_PER_PAGE - 1;
		}

		class ViewHolder {
			TextView index;
			TextView content;
		}
}
