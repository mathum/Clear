package com.clearcrane.logic.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.WindowManager;
import android.widget.TextView;

public class MarqueeTextView extends TextView {

	private final float TEXT_MARQUEE_STEP = 1.8f;// 每一次的偏移量
	private float textLength = 0f;
	private float viewWidth = 0f;
	private float viewHeight = 0f;
	private float stepX = 0f;// x轴的偏移量
	private float stepY = 0f;// y轴的偏移量
	private float text_Start_X = 0f;
	private float text_Start_Y = 0f;
	private float textY = 0f;
	private float textX = 0f;
	private float offsetX = 0;
	private float offsetY = 0;
	private float text_End_X = 0.0f;
	private float text_End_Y = 0.0f;
	public boolean isStarting = false;
	private Paint mPaint = null;
//	private TextPaint mPaint = null;
	private String mText = "";
	private int mHeight = 0;
	private int interval = 0;
    private String direction = "r2l";

	public MarqueeTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public void init(WindowManager wmanager, String color, String interval, String textDirection) {
        direction = textDirection;
        mPaint = getPaint();
//		mPaint = new TextPaint();
//		mPaint.setTextAlign(Align.CENTER);
		this.interval = Integer.parseInt(interval);
		mText = this.getText().toString();
		setVerticalScrollBarEnabled(false);
		setHorizontalScrollBarEnabled(false);
		setClickable(false);
		setFocusable(false);
		setFocusableInTouchMode(false);
		// 设置字体颜色
		mPaint.setColor(Color.parseColor(color));
		textLength = mPaint.measureText(mText);
		stepX = TEXT_MARQUEE_STEP;
//		stepY = TEXT_MARQUEE_STEP;
		viewWidth = getWidth();
//		viewHeight = getHeight();
        // if (viewWidth == 0) {
        // if (wmanager != null) {
        // DisplayMetrics metrics = new DisplayMetrics();
        // wmanager.getDefaultDisplay().getMetrics(metrics);
        // viewWidth = metrics.widthPixels;
        // }
        // }
        textY = getTextSize() + getPaddingTop();
        stepX = TEXT_MARQUEE_STEP;
        if (TextUtils.isEmpty(direction) || "r2l".equals(direction)) {
            text_Start_X = viewWidth;
            text_End_X = 0 - textLength;
            textX = text_Start_X;
        } else {
            text_Start_X = 0 - textLength;
            text_End_X = viewWidth;
            textX = 0 - textLength;
        }
        //左右滚动字幕
//		textX = getTextSize()+getPaddingRight();
//		text_Start_Y = viewHeight;
//		text_End_Y = 0 - textLength*2;
//		stepY = TEXT_MARQUEE_STEP;
//		textY = text_Start_Y;
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
        if (mPaint == null || viewWidth == 0) {
            return;
        }
        /*
         * TODO,FIXME just from right to left now 每次从右向左位移，并且重绘
         */
        if(TextUtils.isEmpty(direction) || "r2l".equals(direction)){
            textX = text_Start_X - offsetX;
        }else{
            textX = text_Start_X + offsetX;
        }

        canvas.drawText(mText, textX, textY, mPaint);

        // 左右垂直滚动
//		textY = text_Start_Y - offsetY;
//		StaticLayout currentLayout = new StaticLayout(mText, mPaint, getWidth() / 2,
//				Alignment.ALIGN_NORMAL, 1.0f, 0f, false);
//	    canvas.translate(textX, textY);
//	    currentLayout.draw(canvas);

        if (!isStarting) {
            return;
        }
        // 当文字末尾位移到最左侧的时候，还原偏移量为0.
        if(TextUtils.isEmpty(direction) || "r2l".equals(direction)){
            if (textX < text_End_X) {
                offsetX = -interval * 100;
            } else {
                offsetX += stepX;
            }
        }else{
            if (textX > text_End_X) {
                offsetX = -interval * 100;
            } else {
                offsetX += stepX;
            }
        }

		// 左右垂直滚动
//		if (textY < text_End_Y) {
//			offsetY = -500;
//		} else {
//			offsetY += stepY;
//		}
		postInvalidate();
	}

	public void drawText(Canvas canvas, String text, float x, float y, Paint paint, float angle) {
		if (angle != 0) {
			canvas.rotate(angle, x, y);
		}
		canvas.drawText(text, x, y, paint);
		if (angle != 0) {
			canvas.rotate(-angle, x, y);
		}
	}

	public String appendSeprator(String srcStr, String seprator, int count){
    	StringBuffer sb = new StringBuffer(srcStr);
    	int index = count;
    	while (sb.length() > count && index < sb.length() - 1){
        sb.insert(index, seprator);
    	index += count+1;
    	}
    	return sb.toString();
    	}
}
