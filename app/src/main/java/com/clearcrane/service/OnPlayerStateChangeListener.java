package com.clearcrane.service;

import com.clearcrane.entity.MusicInfo;

import java.util.List;

public interface OnPlayerStateChangeListener {
	void onStateChange(int state, int mode, List<MusicInfo> musicList,
                       int position);
}
