/**
 * Created on 2015-12-12
 *
 * @author: wgq
 */
package com.clearcrane.pushmessage;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

//import com.clearcrane.util.CommonUtils;

public class MarqueeTextView extends TextView {
	private static final String TAG = "MarqueeTextView";  
	private float textLength = (float) -1.0;
	
	protected Timer scrollTimer;
	protected TimerTask scrollTimerTask;
	
	private int marqueeX;
	private int scrollSpeed;
	
	private boolean textSetted;

	public MarqueeTextView(Context context) {
		super(context);
	}
	
	public MarqueeTextView(Context context, AttributeSet attrs) {  
		super(context, attrs);  
	}  
	
	public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {  
		super(context, attrs, defStyle);  
	}  
	
	public void init() {
		setBackgroundColor(Color.TRANSPARENT);
		setVerticalScrollBarEnabled(false);
		setHorizontalScrollBarEnabled(false);
		setClickable(false);
		setFocusable(false);
		setFocusableInTouchMode(false);
		setSingleLine(true);
		
		marqueeX = 0;
		scrollSpeed = 0;
		textSetted = false;
	}
	
	public void startWork() {
		startScrollTimer();
	}
	
	public void stopWork() {
		stopScrollTimer();
	}
	
	public float getTextLength() {
		if(textLength < 0) {
			Paint paint = getPaint();
			textLength = paint.measureText(getText().toString());
		}
		return textLength;
	}
	
	/** 
	public void setTextColor(String colorStr) {
		this.setTextColor(CommonUtils.getColorFromStr(colorStr));
	}
	**/
	
	public void setHTMLText(String htmlStr) {
		super.setText(android.text.Html.fromHtml(htmlStr));
		
		Paint paint = getPaint();
		textLength = paint.measureText(getText().toString());
		
		textSetted = true;
		
		scrollTo(0, 0);
		marqueeX = 0;
	}
	
	public void setScrollSpeed(int speed) {
		this.scrollSpeed = speed;
	}

	protected void startScrollTimer() {
		/* start a timer to check curprogram's end and detect timed program */
		scrollTimer = new Timer();
		scrollTimerTask = new TimerTask() {
			@Override
			public void run() {
				doScroll();
			}
		};
		
		/**
		 * TODO, FIXME, we may need graphic's knowledge
		 * 25fps???
		 */
		scrollTimer.schedule(scrollTimerTask, 1000, 1000/25);
	}

	protected void stopScrollTimer() {
		if(scrollTimer != null) {
			scrollTimer.cancel();
			scrollTimer = null;
		}
	}
	
	private void doScroll() {
		if(textSetted == false)
			return ;
		
		scrollBy(scrollSpeed, 0);
		marqueeX += scrollSpeed;
		if(marqueeX > textLength) {
			scrollTo( 0 - this.getWidth(), 0);
			
			int delta = this.getWidth()/10 > 20 ? 20 : this.getWidth()/10;
			marqueeX = 0 - this.getWidth() + delta;
		}
	}
}
