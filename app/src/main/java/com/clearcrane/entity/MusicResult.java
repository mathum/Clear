package com.clearcrane.entity;

import java.io.Serializable;
import java.util.ArrayList;

public class MusicResult implements Serializable{

//	private static final long serialVersionUID = -4819646322676470370L;
	
//	private String BackgroundPic;
//	private String BackgroundPic_abs_path;
//	private String rescode;
	private ArrayList<MusicInfo> Content;

//	public String getBackgroundPic() {
//		return BackgroundPic;
//	}
//
//	public void setBackgroundPic(String backgroundPic) {
//		BackgroundPic = backgroundPic;
//	}
//
//	public String getBackgroundPic_abs_path() {
//		return BackgroundPic_abs_path;
//	}
//
//	public void setBackgroundPic_abs_path(String backgroundPic_abs_path) {
//		BackgroundPic_abs_path = backgroundPic_abs_path;
//	}
//
//	public String getRescode() {
//		return rescode;
//	}
//
//	public void setRescode(String rescode) {
//		this.rescode = rescode;
//	}

	public ArrayList<MusicInfo> getContent() {
		return Content;
	}

	public void setContent(ArrayList<MusicInfo> content) {
		Content = content;
	}
	
}
