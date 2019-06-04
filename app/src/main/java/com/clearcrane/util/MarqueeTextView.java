package com.clearcrane.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

public class MarqueeTextView extends TextView implements Runnable{
    //当前滚动的位置
    private int currentScrollX;
    private boolean isStop = false;
    private int textWidth;
    private boolean isMeasure = false;
    private int scrollSpeed = 4;

    public int getAlignType() {
        return alignType;
    }

    public void setAlignType(int alignType) {
        this.alignType = alignType;
    }

    private int alignType = Gravity.CENTER;

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setSingleLine();
        // TODO Auto-generated constructor stub
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSingleLine();
        // TODO Auto-generated constructor stub
    }

    public MarqueeTextView(Context context) {
        super(context);
        setSingleLine();
        // TODO Auto-generated constructor stub
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        //滚动速度
        if (textWidth > this.getWidth()) {
            setGravity(Gravity.LEFT|Gravity.CENTER);
            currentScrollX += scrollSpeed;
            scrollTo(currentScrollX, 0);
            if (isStop) {
                scrollTo(0, 0);
                return;
            }
            int delta = this.getWidth() / 10 > 20 ? 20 : this.getWidth() / 10;
            if (currentScrollX >= (textWidth - delta)) {
                scrollTo(0, 0);
                currentScrollX = 0;
                //   return;
            }
            postDelayed(this, 40);
        } else {
            setGravity(alignType);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        if (!isMeasure) {
            //问自己宽度只需获取一次就可以了
            getTextWidth();
            isMeasure = true;
        }
    }

    public void getTextWidth() {
        Paint paint = this.getPaint();
        String str = this.getText().toString();
        textWidth = (int) paint.measureText(str);
        //startScroll();
    }

    //开始滚动
    public void startScroll() {
        isStop = false;
        getTextWidth();
        this.removeCallbacks(this);
        post(this);
    }

    //停止滚动
    public void stopScroll() {
        isStop = true;
    }

    //从头开始滚动
    public void startFor0() {
        currentScrollX = 0;
        startScroll();
    }
}