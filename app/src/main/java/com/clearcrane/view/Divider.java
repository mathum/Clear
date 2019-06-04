package com.clearcrane.view;

import android.R.color;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.clearcrane.vod.R;

/***
 * 分割线
 * 
 * @author SlientLeaves 2016年8月3日 下午4:09:26
 */
public class Divider extends View {

	private boolean mIsGradient = true;
	private Paint mLeftDividerPaint = new Paint();
	private Paint mRightDividerPaint = new Paint();
	private int mDividerLeftColor;
	private int mDividerRightColor;
	private int mStatus = 1;// 0代表分割线水平方向，1代表竖直方向

	public Divider(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		TypedArray localTypedArray = getContext().obtainStyledAttributes(paramAttributeSet,
				R.styleable.RoundProgressBar);
		if (Boolean.valueOf(localTypedArray.getBoolean(R.styleable.RoundProgressBar_horizontal, false)).booleanValue())
			this.mStatus = 0;
		;

		this.mIsGradient = localTypedArray.getBoolean(R.styleable.RoundProgressBar_ifGradient, false);
		this.mDividerLeftColor = localTypedArray.getColor(R.styleable.RoundProgressBar_color_left,
				getResources().getColor(R.color.divider_left));
		this.mDividerRightColor = localTypedArray.getColor(R.styleable.RoundProgressBar_color_right,
				getResources().getColor(R.color.divider_right));
		localTypedArray.recycle();
		int leftColor = this.mDividerLeftColor;
		int rightColor = this.mDividerRightColor;
		this.mLeftDividerPaint.setColor(leftColor);
		this.mRightDividerPaint.setColor(rightColor);

	}

	/**
	 * 设置画笔的线性渐变渲染对象
	 * 
	 * @param paramPaint
	 * @param paramInt
	 */
	private void setPaintShader(Paint paramPaint, int paramInt) {
		// 创建线性渲染对象
		int[] arrayOfInt = new int[3];
		arrayOfInt[0] = (paramInt + color.black);
		arrayOfInt[1] = paramInt;
		arrayOfInt[2] = (paramInt + color.black);
		// 创建线性渲染对象
		LinearGradient linearGradient = new LinearGradient(0.0F, 0.0F, 0.0F, getHeight(), arrayOfInt, null,
				Shader.TileMode.CLAMP);
		paramPaint.setShader(linearGradient);
	}

	protected void onDraw(Canvas paramCanvas) {
		if (this.mIsGradient) {
			setPaintShader(this.mLeftDividerPaint, this.mDividerLeftColor);
			setPaintShader(this.mRightDividerPaint, this.mDividerRightColor);
		}
		if (this.mStatus == 0) {
			// 水平方向
			paramCanvas.drawLine(0.0F, 0.0F, getMeasuredWidth(), 0.0F, this.mLeftDividerPaint);
			paramCanvas.drawLine(1.0F, 0.0F, getMeasuredWidth(), 1.0F, this.mRightDividerPaint);
			return;
		}

		// 竖直方向方向
		paramCanvas.drawLine(0.0F, 0.0F, 0.0F, getMeasuredHeight(), this.mLeftDividerPaint);
		paramCanvas.drawLine(1.0F, 0.0F, 1.0F, getMeasuredHeight(), this.mRightDividerPaint);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int makeMeasureSpec = MeasureSpec.makeMeasureSpec(2, MeasureSpec.AT_MOST);

		if (this.mStatus == 0) {
			super.onMeasure(widthMeasureSpec, makeMeasureSpec);
			return;
		}

		super.onMeasure(makeMeasureSpec, heightMeasureSpec);
	}

}
