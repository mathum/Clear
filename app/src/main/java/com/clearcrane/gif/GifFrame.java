package com.clearcrane.gif;
import android.graphics.Bitmap;

/**
 * 里面三个成员：当前图片、延时、下张Frame的链接。
 * 
 * @author Administrator
 * 
 */
public class GifFrame {
    /**
     * 构造函数
     * 
     * @param im
     *            图片
     * @param del
     *            延时
     */
    public GifFrame(Bitmap im, int del) {
        image = im;
        delay = del;
    }

    /** 图片 */
    public Bitmap image;
    /** 延时 */
    public int delay;
    /** 下一帧 */
    public GifFrame nextFrame = null;
}
