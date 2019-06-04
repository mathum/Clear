package com.clearcrane.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.widget.ImageView.ScaleType;

/**
 *此处函数是得到剪切的图片
 * @author ZhangJianLin
 *
 */
public class MyBitMap {

	
	public MyBitMap() {
	// TODO Auto-generated constructor stub
}
	/**
	 * 
	 * @param unscaledBitmap the bitmap of source
	 * @param dstWidth what width you want to set
	 * @param dstHeight	What width you want to set
	 * @param scalingLogic it is ScaleType 
	 * @return the scaled bitmap
	 */
	
	public static Bitmap createScaledBitmap(Bitmap unscaledBitmap, int dstWidth, int dstHeight, ScaleType scalingLogic) {
		  Rect srcRect = calculateSrcRect(unscaledBitmap.getWidth(), unscaledBitmap.getHeight(), dstWidth, dstHeight, scalingLogic);
		  Rect dstRect = calculateDstRect(unscaledBitmap.getWidth(), unscaledBitmap.getHeight(), dstWidth, dstHeight, scalingLogic);
		  Bitmap scaledBitmap = Bitmap.createBitmap(dstRect.width(), dstRect.height(), Config.ARGB_8888);
		  Canvas canvas = new Canvas(scaledBitmap);
		  canvas.drawBitmap(unscaledBitmap, srcRect, dstRect, new Paint(Paint.FILTER_BITMAP_FLAG));
		  return scaledBitmap;
		  }
	public static Rect calculateSrcRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight, ScaleType scalingLogic) {
		  if (scalingLogic == ScaleType.CENTER_CROP) {
		    final float srcAspect = (float)srcWidth / (float)srcHeight;
		    final float dstAspect = (float)dstWidth / (float)dstHeight;
		    if (srcAspect > dstAspect) {
		      final int srcRectWidth = (int)(srcHeight * dstAspect);
		      final int srcRectLeft = (srcWidth - srcRectWidth) / 2;
		      return new Rect(srcRectLeft, 0, srcRectLeft + srcRectWidth, srcHeight);
		    } else {
		      final int srcRectHeight = (int)(srcWidth / dstAspect);
		      final int scrRectTop = (int)(srcHeight - srcRectHeight) / 2;
		      return new Rect(0, scrRectTop, srcWidth, scrRectTop + srcRectHeight);
		    }
		  } else {
		    return new Rect(0, 0, srcWidth, srcHeight);
		  }
		}
	
		public static Rect calculateDstRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight, ScaleType scalingLogic) {
		  if (scalingLogic == ScaleType.FIT_XY) {
		    final float srcAspect = (float)srcWidth / (float)srcHeight;
		    final float dstAspect = (float)dstWidth / (float)dstHeight;
		    if (srcAspect > dstAspect) {
		      return new Rect(0, 0, dstWidth, (int)(dstWidth / srcAspect));
		    } else {
		      return new Rect(0, 0, (int)(dstHeight * srcAspect), dstHeight);
		    }
		  } else {
		    return new Rect(0, 0, dstWidth, dstHeight);
		  }
		}
		/**
		 * 
		 * @param unscaledBitmap the bitmap of source
		 * @param scale the scale you want
		 * @param scalingLogic it is ScaleType 
		 * @return the scaled bitmap
		 */
		public static Bitmap createBMScaleBitmap(Bitmap unscaledBitmap, Double scale, ScaleType scalingLogic){
		int dstWidth = (int)(unscaledBitmap.getWidth()* scale);
		int dstHeight = (int)(unscaledBitmap.getHeight()*scale);
		return createScaledBitmap(unscaledBitmap, dstWidth, dstHeight, scalingLogic);
	}
	
}
