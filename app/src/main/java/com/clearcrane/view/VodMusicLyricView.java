package com.clearcrane.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.clearcrane.vod.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 音乐歌词
 * 
 * @author SlientLeaves 2016年8月3日 下午3:45:58
 */
public class VodMusicLyricView extends View {

	private static final String TAG = VodMusicLyricView.class.getSimpleName();

	private int A = -1;
//	private a a;
	private String b;
	private boolean c;
	private int mNormalColor;
	private int mCurrentColor;
	
	private float mNormalTextSize;
	private float mCurrentTextSize;
	
	private TextPaint mTextPaint1;
	private TextPaint mTextPaint2;
	private Paint mPaint;
	private float k;
	private float l;
	private float m;
	private float n;
	private float mLineSpace;
	private boolean p;
	private boolean mIsSingleLine;
	private long mPeriod;
	private Timer mTimer;
	private TimerTask mTimerTask;
//	private h u;
//	private g v;
	private float w;
	private float x;
	private float y;
	private int z = 200;

	public VodMusicLyricView(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		TypedArray localTypedArray = getContext().obtainStyledAttributes(paramAttributeSet,
				R.styleable.RoundProgressBar);
		
		this.mIsSingleLine = localTypedArray.getBoolean(R.styleable.RoundProgressBar_singleLine, false);
		this.mNormalColor = localTypedArray.getColor(R.styleable.RoundProgressBar_normalColor, getResources().getColor(R.color.text_white));
		this.mCurrentColor = localTypedArray.getColor(R.styleable.RoundProgressBar_currentColor, getResources().getColor(R.color.grass_green));
		this.mNormalTextSize = localTypedArray.getFloat(R.styleable.RoundProgressBar_normalTextSize, 28.0F);
		this.mCurrentTextSize = localTypedArray.getFloat(R.styleable.RoundProgressBar_currentTextSize, 44.0F);
		this.mPeriod = localTypedArray.getInt(R.styleable.RoundProgressBar_period, 50);
		this.mLineSpace = localTypedArray.getDimension(R.styleable.RoundProgressBar_lineSpace, 15.0F);
		this.p = true;
		localTypedArray.recycle();
		
		
		this.mTextPaint1 = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
		this.mTextPaint2 = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
		this.mPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
		
//		SetDisplayLyricTextStyle(this.mPaint, this.mNormalColor, this.mCurrentColor);
//		Paint.FontMetrics localFontMetrics1 = a(this.mTextPaint1, this.d, this.f);
//		this.k = (localFontMetrics1.bottom - localFontMetrics1.top);
//		this.l = localFontMetrics1.bottom;
//		Paint.FontMetrics localFontMetrics2 = a(this.mTextPaint2, this.d, this.g);
//		this.m = (localFontMetrics2.bottom - localFontMetrics2.top);
//		this.n = localFontMetrics2.bottom;
//		this.u = new h(this);
//		this.v = new g(this, (byte) 0);
//		this.v.setDuration(this.z);
//	}
//
//	private static Paint.FontMetrics SetDisplayLyricTextStyle(Paint paint, int textColor, float textSize) {
//		paint.setTextAlign(Paint.Align.CENTER);
//		paint.setColor(textColor);
//		paint.setTextSize(textSize);
//		paint.setAntiAlias(true);
//		return paint.getFontMetrics();
//	}
//
//	public final float a() {
//		return this.g;
//	}
//
//	public final void a(a parama) {
//		c();
//		this.a = parama;
//		this.c = false;
//		if ((parama == null) || (parama.h == null))
//			invalidate();
//	}
//
//	public final void a(String paramString) {
//		c();
//		this.c = true;
//		this.b = paramString;
//		this.a = null;
//		invalidate();
//	}
//
//	public final void b() {
//		c();
//		if ((this.a == null) || (this.a.h == null)) {
//			invalidate();
//			return;
//		}
//		this.mTimer = new Timer("startRefreshLyric", true);
//		this.mTimerTask = new f(this);
//		this.mTimer.schedule(this.mTimerTask, 0L, this.r);
//	}
//
//	public final void c() {
//		if (this.mTimer != null) {
//			this.mTimer.cancel();
//			this.mTimer = null;
//		}
//		if (this.mTimerTask != null) {
//			this.mTimerTask.cancel();
//			this.mTimerTask = null;
//		}
//	}
//
//	protected void onDraw(Canvas paramCanvas)
//	  {
//	    if ((this.a == null) || (this.a.h == null))
//	      if (this.c)
//	      {
//	        float f1 = getMeasuredHeight() / 2.0F;
//	        float f2 = this.f;
//	        float f3 = this.l;
//	        float f4 = f1 + f2 / 2.0F - f3;
//	        float f5 = getMeasuredWidth() / 2.0F;
//	        paramCanvas.drawText(this.b, f5, f4, this.mPaint);
//	      }
//	    int i1;
//	    float f6;
//	    long l2;
//	    int i2;
//	    label256: float f8;
//	    label275: 
//	    do
//	    {
//	      long l1;
//	      do
//	      {
//	        return;
//	        c.d.h();
//	        l1 = aa.l();
//	        i1 = this.a.a(1000L * l1);
//	      }
//	      while ((this.a.h == null) || (this.a.h.length <= i1));
//	      if (this.y == 0.0F)
//	        this.y = (getMeasuredHeight() / 2.0F);
//	      f6 = getMeasuredWidth() / 2.0F;
//	      float f7 = this.mTextPaint2.measureText(this.a.h[i1]);
//	      c.d.h();
//	      l2 = aa.k();
//	      i2 = this.a.a(1000L * l1);
//	      if ((this.a.i == null) || (this.a.i.length <= i2))
//	        break label580;
//	      long l3 = this.a.i[i2];
//	      if (i2 + 1 < this.a.i.length)
//	        break;
//	      f8 = (float)(l1 * 1000L - l3) / (float)(l2 - l3);
//	      float f9 = (getWidth() - f7) / 2.0F;
//	      float f10 = (f7 + getWidth()) / 2.0F;
//	      int[] arrayOfInt = new int[4];
//	      arrayOfInt[0] = this.e;
//	      arrayOfInt[1] = this.e;
//	      arrayOfInt[2] = this.d;
//	      arrayOfInt[3] = this.d;
//	      LinearGradient localLinearGradient = new LinearGradient(f9, 0.0F, f10, 0.0F, arrayOfInt, new float[] { 0.0F, f8, f8, 1.0F }, Shader.TileMode.CLAMP);
//	      float f11 = this.y;
//	      float f12 = this.m;
//	      float f13 = this.n;
//	      float f14 = f11 + f12 / 2.0F - f13;
//	      this.mTextPaint2.setShader(localLinearGradient);
//	      paramCanvas.drawText(this.a.h[i1], f6, f14, this.mTextPaint2);
//	    }
//	    while (this.q);
//	    int i3 = 0;
//	    label449: if (i3 < this.a.h.length)
//	      if (i3 != i1)
//	        if (i3 <= i1)
//	          break label586;
//	    label580: label586: for (float f15 = this.y + (i3 - i1) * (this.k + this.o) + (this.m - this.k) / 2.0F; ; f15 = this.y + (i3 - i1) * (this.k + this.o) - (this.m - this.k) / 2.0F)
//	    {
//	      float f16 = this.k;
//	      float f17 = this.l;
//	      float f18 = f15 + f16 / 2.0F - f17;
//	      paramCanvas.drawText(this.a.h[i3], f6, f18, this.mTextPaint1);
//	      i3++;
//	      break label449;
//	      break;
//	      l2 = this.a.i[(i2 + 1)];
//	      break label256;
//	      f8 = 0.0F;
//	      break label275;
//	    }
	  }

}
