package com.clearcrane.entity;

import java.util.ArrayList;

public class PictureCategory {
	public int count;//图片的总数
	public String label;
	public String type;
	public ArrayList<Picture> picList = new ArrayList<Picture>();//图片地址列表
}
