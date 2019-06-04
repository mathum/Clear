package com.clearcrane.pushmessage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.TextView;

@SuppressLint("NewApi")
public class AlwaysMarqueeTextView extends TextView {

    private float textLength = 0f;
    private float viewWidth = 0f;
    private float step = 0f;
    private float y = 0f;
    private float temp_view_plus_text_length = 0.0f;
    private float temp_view_plus_two_text_length = 0.0f;
    public boolean isStarting = false;
    private Paint paint = null;
    private String text = "";
    private int mHeight = 0;
	
	public AlwaysMarqueeTextView(Context context) {
		super(context);
	}
	
	public AlwaysMarqueeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public AlwaysMarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
    public void init(WindowManager windowManager) {
        paint = getPaint();
        paint.setColor(FloatViewService.updateFontColor);
        text = getText().toString();
        textLength = paint.measureText(text);
        viewWidth = getWidth();
        if(viewWidth == 0)
        {
            if(windowManager != null)
            {
            	DisplayMetrics metrics = new DisplayMetrics();
            	windowManager.getDefaultDisplay().getMetrics(metrics);
                viewWidth = metrics.widthPixels;
            }
        }
        step = textLength;
        temp_view_plus_text_length = viewWidth + textLength;
        temp_view_plus_two_text_length = viewWidth + textLength * 2;
        y = getTextSize() + getPaddingTop();
    }
	
	@Override
	public boolean isFocused() {
		return true;
	}

    public void startScroll() {
        isStarting = true;
        postInvalidate();
    }

    public void stopScroll() {
        isStarting = false;
        postInvalidate();
    }
    
    @Override
    public void onDraw(Canvas canvas) {
    	if (paint == null) {
    		return;
    	}
    	canvas.drawText(text, temp_view_plus_text_length - step, y, paint);
        if (!isStarting) {
            return;
        }
        step += FloatViewService.updateSpeed;
        if (step > temp_view_plus_two_text_length) {
            step = textLength;
        }
        postInvalidate();
    }
    
    public void setMHeight(int height) {
    	mHeight = height;
    }
}
