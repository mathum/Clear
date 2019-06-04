package com.clearcrane.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.clearcrane.activity.VoDActivity;
import com.clearcrane.entity.MusicInfo;
import com.clearcrane.vod.R;

public class MusicPlayListViewHolder extends ViewHolderBase<MusicInfo> {

	private Context mContext;
	private TextView mTvItemPlayListIndex;
	private ImageView mIvItemPlayListPlaying;
	private TextView mTvItemPlayListPoint;
	private TextView mTvItemPlayListTrackName;
	private TextView mTvItemPlayListArtists;

	public MusicPlayListViewHolder(VoDActivity mContext) {
		this.mContext = mContext;
	}

	@Override
	public View createView(LayoutInflater layoutInflater) {
		
		View view = LayoutInflater.from(mContext).inflate(R.layout.item_play_list, null);
		mTvItemPlayListIndex = (TextView) view.findViewById(R.id.tv_item_play_list_index);
		mIvItemPlayListPlaying = (ImageView) view.findViewById(R.id.iv_item_play_list_playing);
		mTvItemPlayListPoint = (TextView) view.findViewById(R.id.tv_item_play_list_point);
		mTvItemPlayListTrackName = (TextView) view.findViewById(R.id.tv_item_play_list_track_name);
		mTvItemPlayListArtists = (TextView) view.findViewById(R.id.tv_item_play_list_artists);

		return view;
	}

	@Override
	public void showData(int position, MusicInfo itemData) {
		int itemSeq = Integer.parseInt(itemData.getSeq());
		String _itemSeq = itemSeq >= 10 ? ""+itemSeq : "0"+itemSeq;
		mTvItemPlayListIndex.setText(_itemSeq);	
		mTvItemPlayListTrackName.setText(itemData.getAlbum());
		mTvItemPlayListArtists.setText(itemData.getSinger());
	}


}
