package com.clearcrane.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Scroller;

import com.clearcrane.vod.R;

public class MediaBtnScrollAnimView extends View {

	private static Bitmap mBitmap;
	private Scroller mScroller;
	private Context mContext;
	private Boolean mHasDraw = Boolean.valueOf(false);
	private int mCoordinateX;
	private int mCoordinateY;
	private Paint mBtnBgPaint;

	public MediaBtnScrollAnimView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		//生成带有变化率的scroll实例
		this.mScroller = new Scroller(context, new OvershootInterpolator());
		this.mBtnBgPaint = new Paint(Paint.FILTER_BITMAP_FLAG);

		if ((mBitmap == null) || mBitmap.isRecycled())
			mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg_media_btn_focus);
	}

	public final void scrollAnimTo(View paramView, boolean isFirstShow) {

		int deltaX = paramView.getLeft() + (paramView.getMeasuredWidth() - mBitmap.getWidth()) / 2;
		int duration = 200;

		if (isFirstShow) {
			duration = 0;
		}

		int offsetX = -this.mScroller.getFinalX() - deltaX;
		this.mScroller.startScroll(this.mScroller.getFinalX(), this.mScroller.getFinalY(), offsetX, 0, duration);

		Log.d("scrollAnimTo", "deltaX=" + deltaX);
		invalidate();
	}

	public void computeScroll() {
		if (this.mScroller.computeScrollOffset()) {
			scrollTo(this.mScroller.getCurrX(), this.mScroller.getCurrY());
			postInvalidate();
		}
	}

	protected void onDraw(Canvas paramCanvas) {
		super.onDraw(paramCanvas);
		if (!this.mHasDraw.booleanValue())
			paramCanvas.drawBitmap(mBitmap, this.mCoordinateX, this.mCoordinateY, this.mBtnBgPaint);
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		this.mCoordinateY = ((getMeasuredHeight() - mBitmap.getHeight()) / 2);
	}

}
