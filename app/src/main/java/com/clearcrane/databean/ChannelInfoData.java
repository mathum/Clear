package com.clearcrane.databean;

import java.util.ArrayList;

public class ChannelInfoData {
	public String Name;
	String NameEng;
	String Num;
	String picUrl;
	ArrayList<String> src = new ArrayList<String>();

	public String getName() {
		return Name;
	}

	public String getPicUrl() {
		return picUrl;
	}

	public ArrayList<String> getSrc() {
		return src;
	}

	public String getNum() {
		return Num;
	}

	public void setSrc(ArrayList<String> src) {
		this.src = src;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getNameEng() {
		return NameEng;
	}

	public void setNameEng(String nameEng) {
		NameEng = nameEng;
	}

	public void setNum(String num) {
		Num = num;
	}
}
