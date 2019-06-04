package com.clearcrane.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.clearcrane.provider.MaterialRequest;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.vod.R;

public class VodFullImageShowView extends VoDBaseView {
	
	private String picURL = "";
	private ImageView iv;
	
	public void init(Context ctx, String u) {
		context = ctx;
		url = u;
		
		view = LayoutInflater.from(context).inflate(R.layout.fullscreen_image, null);
		initLayoutInXml();
	}
	
	public void init(Context ctx, String u, LinearLayout layout) {
		context = ctx;
		url = u;
		menuLinearLayout = layout;
		
		view = LayoutInflater.from(context).inflate(R.layout.fullscreen_image, null);
		initLayoutInXml();
	}
	
	public void setPicURL(String u) {
		picURL = u;
		MaterialRequest mr = new MaterialRequest(context, iv, ClearConfig.TYPE_IMAGE);
		mr.execute(picURL);
	}
	
	private void initLayoutInXml() {
		iv = (ImageView)view.findViewById(R.id.full_image_pic);
	}
	
	public boolean onKeyEnter() {
		return onKeyBack();
	}	
}
