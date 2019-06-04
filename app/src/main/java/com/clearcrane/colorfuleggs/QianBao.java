package com.clearcrane.colorfuleggs;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.clearcrane.util.ClearConfig;

import java.util.Random;

public class QianBao extends ColorfulEggs {

	private Bitmap bitmap;
	private int x;
	private int y;
	private boolean isOver = false;

	Random random = new Random();
	private int numberY, numberX;

	public QianBao(Bitmap bitmap) {
		this.bitmap = bitmap;
		x = random.nextInt(ClearConfig.getScreenWidth());
		if (x > 300 && x < 900)
			x = random.nextInt(ClearConfig.getScreenWidth());
		y = -30;
		numberY = 2 - random.nextInt(5);
		numberX = 4 - random.nextInt(9);
	}

	public boolean isOver() {
		return isOver;
	}

	int number = 0;
	int flag = 0;

	@Override
	public void move(Canvas canvas) {
		if (y <= ClearConfig.getScreenHeight()) {
			y = y + numberY;
			if (number % 10 == 0) {
				numberY = numberY + 1;

			}
			if (number % 50 == 0) {
				numberX = 4 - random.nextInt(9);
				flag = numberX;
			}
			number++;
			if (numberX > 0) {
				x += flag / 50 * numberX;
			} else
				x += numberX;
			canvas.drawBitmap(bitmap, x, y, null);
		} else {
			isOver = true;
		}
	}
}
