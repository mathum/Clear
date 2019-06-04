/**
 * @author xujifu
 * @copyright clear
 * @date 2014-06-20
 * @description 天气趋势视图，用于画趋势线
 */
package com.clearcrane.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

import com.clearcrane.log.L;

import java.util.ArrayList;

public class TrendView extends View {
	
	private Context mContext;
	private Bitmap[] topBmps;
	private Bitmap[] bkBmps;
	
	private Paint mPointPaint;
	private Paint mTextPaint;
	private Paint mTopLinePaint;
	private Paint mLowLinePaint;
	
	private int x[];
	private ArrayList<String> topTem;
	private ArrayList<String> lowTem;
	private ArrayList<String> week;
	private int count = 0;
	private int h;
	private float radius = 4;
	
	public TrendView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
	}
	
	public TrendView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	public TrendView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mContext = context;
	}
	
	public void init(int count){
		this.count = count;
		
		x = new int[count];
		topBmps = new Bitmap[count];
		bkBmps = new Bitmap[count];
		
		topTem = new ArrayList<String>();
		lowTem = new ArrayList<String>();
		
		mPointPaint = new Paint();
		mPointPaint.setAntiAlias(true);
		mPointPaint.setColor(Color.WHITE);
		
		mTopLinePaint = new Paint();
		mTopLinePaint.setColor(Color.YELLOW);
		mTopLinePaint.setAntiAlias(true);
		mTopLinePaint.setStrokeWidth(2);
		mTopLinePaint.setStyle(Style.FILL);
		
		mLowLinePaint = new Paint();
		mLowLinePaint.setColor(Color.YELLOW);
		mLowLinePaint.setAntiAlias(true);
		mLowLinePaint.setStrokeWidth(2);
		mLowLinePaint.setStyle(Style.FILL);
		
		mTextPaint = new Paint();
		mTextPaint.setAntiAlias(true);
		mTextPaint.setColor(Color.WHITE);
		mTextPaint.setTextSize(15f);
		mTextPaint.setTextAlign(Align.CENTER);
	}
	
	public void setWidthHeight(int w, int h){
		for(int i = 0; i < this.count * 2; i ++){
			if((i % 2) != 0){
				x[i / 2] = (w * i) / (count * 2);
				L.i("args=x =" + x[i / 2]);
			}
		}
		this.h = h;
	}
	
	public void setTemperature(ArrayList<String> top, ArrayList<String> low){
		this.topTem = top;
		this.lowTem = low;
		
		postInvalidate();
	}
	
	public void setBitmap(ArrayList<Bitmap> pic){
		for(int i = 0; i < this.count; i ++){
			topBmps[i] = pic.get(i);
		}
	}
	
	public void setWeek(ArrayList<String> week){
		this.week = week;
	}
	
	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		float space = 0f;
		float space1 = 0f;
		int temspace = 2;
		
		if(0 == count)
			return;
		FontMetrics fontMetrics = mTextPaint.getFontMetrics();
		float fontHeight = fontMetrics.bottom - fontMetrics.top;
		
		int h = this.h / 2 + 60;
		int h2 = (int)(h - fontHeight / 2);
		int h3 = (int)(h - fontHeight - 100);
		
		int h4 = (int)(h + fontHeight);
		int h5 = (int)(h + fontHeight);
		
		L.i("h:" + h + ",h2:" + h2 + ",h3:" + h3 + ",h4:" + h4 + ",h5:" + h5);
		
		for(int i = 0; i < topTem.size(); i ++){
			space = ( - Integer.parseInt(topTem.get(i))) * temspace;
			if(Integer.parseInt(topTem.get(i)) != 100){
				if( i != topTem.size() - 1){
					space1 = (- Integer.parseInt(topTem.get(i + 1)) * temspace);
					canvas.drawLine(x[i], h + space, x[i + 1], h + space1, mTopLinePaint);
				}
				canvas.drawText(topTem.get(i) + "°", x[i], h2 + space, mTextPaint);
				canvas.drawCircle(x[i], h + space, radius , mPointPaint);
				Matrix matrix = new Matrix();
				int width = topBmps[i].getWidth();
				int height = topBmps[i].getHeight();
			    float scaleWidth = ((float) 40 / width);   
			    float scaleHeight = ((float) 40 / height);
			    matrix.postScale(scaleWidth, scaleHeight);
			    Bitmap newbmp = Bitmap.createBitmap(topBmps[i], 0, 0, width, height,   
			            matrix, true);   
				canvas.drawBitmap(newbmp, x[i] - newbmp.getWidth() / 2, 30, null);
				canvas.drawText(week.get(i), x[i], 20, mTextPaint);
			}
		}
		
		
		for(int i = 0; i < topTem.size(); i ++){
			if((i % 2) == 0){
				Paint mRectPaint = new Paint();
				mRectPaint.setAntiAlias(true);
				mRectPaint.setColor(Color.WHITE);
				mRectPaint.setAlpha(125);
				canvas.drawRect((x[i] - x[0]), 60, (x[i] + x[0]), this.h, mRectPaint);
			}else{
				Paint mRectPaint = new Paint();
				mRectPaint.setAntiAlias(true);
				mRectPaint.setColor(Color.WHITE);
				mRectPaint.setAlpha(225);
				canvas.drawRect((x[i] - x[0]), 60, (x[i] + x[0]), this.h , mRectPaint);
			}
		}
		
		for (int i = 0; i < lowTem.size(); i++) {
			space = (-Integer.parseInt(lowTem.get(i))) * temspace;
			if (i != lowTem.size() - 1) {
				space1 = ( - Integer.parseInt(lowTem.get(i+1))) * temspace;
				canvas.drawLine(x[i], h + space, x[i+1], h + space1, mLowLinePaint);
			} 
			canvas.drawText(lowTem.get(i) + "°", x[i], h4 + space, mTextPaint);
			canvas.drawCircle(x[i], h + space, radius, mPointPaint);
			//canvas.drawBitmap(lowBmps[i], x[i]-lowBmps[i].getWidth()/2, h5 + space, null);
		}
	}

}
