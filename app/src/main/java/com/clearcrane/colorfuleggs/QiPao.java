package com.clearcrane.colorfuleggs;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.clearcrane.util.ClearConfig;

import java.util.Random;

public class QiPao extends ColorfulEggs {

	private Bitmap bitmap;
	private float x;
	private float y;
	private boolean isOver = false;
	private Paint vPaint;
	Random random = new Random();
	private float numberY, numberX;

	public QiPao(Bitmap bitmap) {
		this.bitmap = bitmap;
		x = random.nextInt(ClearConfig.getScreenWidth());
		if (x > 300 && x < 900)
			x = random.nextInt(ClearConfig.getScreenWidth());
		y = ClearConfig.getScreenHeight();
		numberY = 2 - random.nextInt(5) + 8;
		number = (int) numberY;
		numberX = 2 - random.nextInt(5);
		vPaint = new Paint();
		vPaint.setStyle(Paint.Style.STROKE); // 空心
	}

	private int number = 0;
	private int number_flag = 0;

	public boolean isOver() {
		return isOver;
	}

	@Override
	public void move(Canvas canvas) {
		if (y >= 0) {
			y -= numberY;
			// if(number_flag % 50 == 0)
			// numberY = numberY - 1;
			// number_flag++;
			numberY = (float) (numberY - 0.06);
			x += numberX;
			// if(y < 450)
			// {
			// if(number == 0)
			// number = (int)numberY;
			// vPaint.setAlpha((int)((255/number)*(numberY)));
			// }
			// else
			// vPaint.setAlpha(255);
			vPaint.setAlpha((int) ((255 / number) * (numberY)));
			if (numberY > 0) {
				canvas.drawBitmap(bitmap, x, y, vPaint);
			}
		} else {
			isOver = true;
		}
	}
}
