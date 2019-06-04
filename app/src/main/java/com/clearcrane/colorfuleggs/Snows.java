package com.clearcrane.colorfuleggs;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.clearcrane.util.ClearConfig;

import java.util.Random;

public class Snows extends ColorfulEggs {

	private Bitmap bitmap;
	private int x;
	private int y;
	private boolean isOver = false;

	Random random = new Random();
	private int numberY, numberX;

	public Snows(Bitmap bitmap) {
		this.bitmap = bitmap;
		x = random.nextInt(ClearConfig.getScreenWidth());
		if (x > 300 && x < 900)
			x = random.nextInt(ClearConfig.getScreenWidth());

		y = -30;
		numberY = random.nextInt(5) + 1;
		numberX = 2 - random.nextInt(5);
	}

	public boolean isOver() {
		return isOver;
	}

	@Override
	public void move(Canvas canvas) {
		if (y <= ClearConfig.getScreenHeight()) {
			y = y + numberY;
			x += numberX;
			canvas.drawBitmap(bitmap, x, y, null);
		} else {
			isOver = true;
		}
	}
}
