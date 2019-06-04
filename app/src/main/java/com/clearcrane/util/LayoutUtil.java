package com.clearcrane.util;

import android.graphics.Point;

import com.clearcrane.constant.ClearConstant;

public class LayoutUtil {

	public static int getLeftAfterScale(Point layoutSize, int left) {
		return ((ClearConstant.screenWidth * left) / layoutSize.x);
	}
	
	public static int getTopAfterScale(Point layoutSize, int top) {
		return ((ClearConstant.screenHeight * top) / layoutSize.y);
	}
	
	public static int getWidthAfterScale(Point layoutSize, int w) {
		return ((ClearConstant.screenWidth * w) / layoutSize.x);
	}
	
	public static int getHeightAfterScale(Point layoutSize, int h) {
		return ((ClearConstant.screenHeight * h) / layoutSize.y);
	}
}
