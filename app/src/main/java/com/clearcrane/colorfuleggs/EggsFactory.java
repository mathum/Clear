package com.clearcrane.colorfuleggs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.clearcrane.vod.R;

import java.util.ArrayList;
import java.util.Random;

public class EggsFactory {

	private ArrayList<Snows> snows;
	private ArrayList<YuanBao> yuanbaos;
	private ArrayList<QianBao> qianbaos;
	private ArrayList<QiPao> qipaos;
	private Random random;
	
	private Eggs curEggs;
	
	public static enum Eggs {
		snows, yuanbao, qianbao, qipao, none
	}
	
	private static EggsFactory eggsFactory = null;
	
	public static EggsFactory instance() {
		if (eggsFactory == null) {
			eggsFactory = new EggsFactory();
		}
		return eggsFactory;
	}
	
	private EggsFactory() {
		snows = new ArrayList<Snows>();
		yuanbaos = new ArrayList<YuanBao>();
		qianbaos = new ArrayList<QianBao>();
		qipaos = new ArrayList<QiPao>();
		random = new Random();
	}
	
	public ColorfulEggs newEggs(Context context, Eggs egg) {
		if (egg == Eggs.snows) {
			curEggs = Eggs.snows;
			return newSnows(context);
		} else if (egg == Eggs.yuanbao) {
			curEggs = Eggs.yuanbao;
			return newYuanBao(context);
		} else if (egg == Eggs.qianbao) {
			curEggs = Eggs.qianbao;
			return newQianBao(context);
		} else if (egg == Eggs.qipao) {
			curEggs = Eggs.qipao;
			return newQiPao(context);
		} else {
			return null;
		}
	}

	private ColorfulEggs newSnows(Context context) {
		int number = random.nextInt(3);
		Snows s = null;
		if (number == 0) {
			Bitmap bitmap = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.s1);
			s = new Snows(bitmap);
		} else if (number == 1) {
			Bitmap bitmap = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.s2);
			s = new Snows(bitmap);
		} else if (number == 2) {
			Bitmap bitmap = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.s3);
			s = new Snows(bitmap);
		}
		snows.add(s);
		return s;
	}

	private ColorfulEggs newYuanBao(Context context) {
		int number = random.nextInt(3);
		YuanBao s = null;
		if (number == 0) {
			Bitmap bitmap = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.s7);
			s = new YuanBao(bitmap);
		} else if (number == 1) {
			Bitmap bitmap = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.s8);
			s = new YuanBao(bitmap);
		} else if (number == 2) {
			Bitmap bitmap = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.s9);
			s = new YuanBao(bitmap);
		}
		yuanbaos.add(s);
		return s;
	}

	private ColorfulEggs newQianBao(Context context) {
		int number = random.nextInt(3);
		QianBao s = null;
		if (number == 0) {
			Bitmap bitmap = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.s4);
			s = new QianBao(bitmap);
		} else if (number == 1) {
			Bitmap bitmap = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.s5);
			s = new QianBao(bitmap);
		} else if (number == 2) {
			Bitmap bitmap = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.s6);
			s = new QianBao(bitmap);
		}
		qianbaos.add(s);
		return s;
	}

	private ColorfulEggs newQiPao(Context context) {
		int number = random.nextInt(5);
		QiPao s = null;
		if (number == 0) {
			Bitmap bitmap = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.s12);
			s = new QiPao(bitmap);
		} else if (number == 1) {
			Bitmap bitmap = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.s13);
			s = new QiPao(bitmap);
		} else if (number == 2) {
			Bitmap bitmap = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.s12);
			s = new QiPao(bitmap);
		} else if (number == 3) {
			Bitmap bitmap = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.s13);
			s = new QiPao(bitmap);
		} else if (number == 4) {
			Bitmap bitmap = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.s14);
			s = new QiPao(bitmap);
		}
		qipaos.add(s);
		return s;
	}

//	public void moveSnows(Canvas canvas) {
//		if (snows.size() > 250) {
//			for (int i = 0; i < 100; i++) {
//				snows.remove(i);
//			}
//		}
//		for (int i = 0; i < snows.size(); i++) {
//			if (!snows.get(i).isOver()) {
//				snows.get(i).move(canvas);
//			}
//
//		}
//	}

	public void moveEggs(Canvas canvas) {
		if (curEggs == Eggs.snows) {
			if (snows.size() > 250) {
				for (int i = 0; i < 100; i++) {
					snows.remove(i);
				}
			}
			for (int i = 0; i < snows.size(); i++) {
				if (!snows.get(i).isOver()) {
					snows.get(i).move(canvas);
				}

			}
		} else if (curEggs == Eggs.yuanbao) {
			if (yuanbaos.size() > 250) {
				for (int i = 0; i < 100; i++) {
					yuanbaos.remove(i);
				}
			}
			for (int i = 0; i < yuanbaos.size(); i++) {
				if (!yuanbaos.get(i).isOver()) {
					yuanbaos.get(i).move(canvas);
				}

			}
		} else if (curEggs == Eggs.qianbao) {
			if (qianbaos.size() > 250) {
				for (int i = 0; i < 100; i++) {
					qianbaos.remove(i);
				}
			}
			for (int i = 0; i < qianbaos.size(); i++) {
				if (!qianbaos.get(i).isOver()) {
					qianbaos.get(i).move(canvas);
				}

			}
		} else if (curEggs == Eggs.qipao) {
			if (qipaos.size() > 250) {
				for (int i = 0; i < 100; i++) {
					qipaos.remove(i);
				}
			}
			for (int i = 0; i < qipaos.size(); i++) {
				if (!qipaos.get(i).isOver()) {
					qipaos.get(i).move(canvas);
				}

			}
		}
	}
}
