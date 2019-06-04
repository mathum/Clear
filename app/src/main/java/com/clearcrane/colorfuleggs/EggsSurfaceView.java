package com.clearcrane.colorfuleggs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.clearcrane.colorfuleggs.EggsFactory.Eggs;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class EggsSurfaceView extends SurfaceView implements Callback {
	public static final String TAG = "eggs";
	private LoopThread thread;
	private boolean isInited = false;
	private Eggs curEggs;
	private boolean isRunning = false;

	public EggsSurfaceView(Context context) {
		super(context);
		init();
	}

	public EggsSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public EggsSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		if (isInited) {
			return;
		}
		setZOrderOnTop(true);
		SurfaceHolder holder = getHolder();
		holder.setFormat(PixelFormat.TRANSPARENT);
		holder.addCallback(this); // 设置Surface生命周期回调
		isRunning = false;
		isInited = true;
	}
	
	public void startEggs(Eggs egg) {
		Log.e(TAG, "startEggs");
		curEggs = egg;
		if (curEggs == Eggs.none) {
			stopEggs();
			return;
		}
		isRunning = true;
		thread = new LoopThread(getHolder(), getContext());
		thread.start();
		Timer timer = new Timer();
		timer.schedule(new TimerTask(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				stopEggs();
			}
			
		}, 30*1000);
	}
	
	private void stopEggs() {
		Log.e(TAG, "stopEggs");
		isRunning = false;
		if (thread != null) {
			try {
				thread.join();
			} catch (Exception e) {
				Log.e(TAG, "[" + Log.getStackTraceString(e) + "]");
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.e(TAG, "surfaceChanged");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.e(TAG, "surfaceCreated");
		if (isRunning) {
			thread = new LoopThread(holder, getContext());
			thread.start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.e(TAG, "surfaceDestroyed");
		stopEggs();
	}
	
	private Random random = new Random();

	/**
	 * 执行绘制的绘制线程
	 * 
	 * @author Administrator
	 * 
	 */
	class LoopThread extends Thread {

		SurfaceHolder surfaceHolder;
		Context context;
		float radius = 10f;
		Paint paint;
		private int number = 0;

		public LoopThread(SurfaceHolder surfaceHolder, Context context) {

			this.surfaceHolder = surfaceHolder;
			this.context = context;

			paint = new Paint();
			paint.setColor(Color.YELLOW);
			paint.setStyle(Paint.Style.STROKE);
		}

		@Override
		public void run() {

			Canvas c = null;

			while (isRunning) {

				try {
					synchronized (surfaceHolder) {

						c = surfaceHolder.lockCanvas();
						doDraw(c);
						surfaceHolder.unlockCanvasAndPost(c);
						// 通过它来控制帧数执行一次绘制后休息50ms
						Thread.sleep(50);
					}
				} catch (Exception e) {
					Log.e(TAG, "[" + Log.getStackTraceString(e) + "]");
					return;
				}
			}
			try {
				c = surfaceHolder.lockCanvas();
				c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
				surfaceHolder.unlockCanvasAndPost(c);
			} catch (Exception e) {
				Log.e(TAG, "[" + Log.getStackTraceString(e) + "]");
				return;
			}

		}

		public void doNewEggs(Canvas canvas) {
			for (int i = 0; i < random.nextInt(10) + 1; i++) {
				EggsFactory.instance().newEggs(context, curEggs);
			}
		}

		public void doDraw(Canvas c) {
			if (c == null) {
				Log.e(TAG, "Canvas is null");
				return;
			}
			c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
			if (number % 10 == 0) {
				doNewEggs(c);
				number = 0;
			}
			number++;
			EggsFactory.instance().moveEggs(c);
		}
	}
}
