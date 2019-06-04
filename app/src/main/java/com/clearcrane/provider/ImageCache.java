/**
 * @author xujifu
 * @copyright clear
 * @date 2014-06-15
 * @description 将图片存入本地
 */
package com.clearcrane.provider;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.StatFs;

import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.MD5;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;

public class ImageCache {
	public ImageCache(){
		
	}
	
	public boolean createImageCache(){
		if(ClearConfig.checkSDCard()){
			File file = new File(ClearConfig.SDCARD_CACHE);
			if(!file.exists()){
				file.mkdirs();
			}
			removeCache(getDirectory());
			return true;
		}else{
			return false;
		}
	}
	
	public Bitmap getImage(final String url){
		String filename = getDirectory() + "/" + convertUrlToFileName(url);
		File file = new File(filename);
		if(file.exists()){
			Bitmap bmp = BitmapFactory.decodeFile(filename);
            if (bmp == null) {
                file.delete();
            } else {
                updateFileTime(filename);
                return bmp;
            }
		}
		return null;
	}
	
	/**
	 * 将图片存入文件缓存
	 */
	public void saveBitmap(Bitmap bm, String url){
		if(bm == null){
			return;
		}
		if(ClearConfig.MIN_SD_SPACE_NEED > freeSpaceOnSd()){
			return;
		}
		String filename = convertUrlToFileName(url);
		String dir = getDirectory();
		File dirFile = new File(dir);
		if(!dirFile.exists()){
			dirFile.mkdirs();
		}
		File file = new File(dir + "/" + filename);
		try{
			file.createNewFile();
            OutputStream outStream = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
		}catch(FileNotFoundException e){
			
		}catch(IOException e){
			
		}
	}
	
	/**
     * 计算存储目录下的文件大小，
     * 当文件总大小大于规定的CACHE_SIZE或者sdcard剩余空间小于FREE_SD_SPACE_NEEDED_TO_CACHE的规定
     * 那么删除40%最近没有被使用的文件
     */
	private boolean removeCache(String dirPath){
		File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null) {
            return true;
        }
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return false;
        }
                                                            
        int dirSize = 0;
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().contains(ClearConfig.WHOLESALE_CONV)) {
                dirSize += files[i].length();
            }
        }
                                                            
        if ((dirSize > (ClearConfig.MIN_SD_SPACE_NEED)*(ClearConfig.MB)) || ClearConfig.MIN_SD_SPACE_NEED > freeSpaceOnSd()) {
        	int removeFactor = (int) ((0.4 * files.length) + 1);
            Arrays.sort(files, new FileLastModifSort());
            for (int i = 0; i < removeFactor; i++) {
                if (files[i].getName().contains(ClearConfig.WHOLESALE_CONV)) {
                    files[i].delete();
                }
            }
        }
        if (freeSpaceOnSd() <= ClearConfig.MIN_SD_SPACE_NEED) {
            return false;
        }
        return true;
	}
	
	/** 
	 * 修改文件的最后修改时间 
	 */
    public void updateFileTime(String path) {
        File file = new File(path);
        long newModifiedTime = System.currentTimeMillis();
        file.setLastModified(newModifiedTime);
    }

	/** 
	 * 获得缓存目录 
	 */
	private String getDirectory() {
		// TODO Auto-generated method stub
		return ClearConfig.SDCARD_CACHE;
	}

	/**
	 * 将url转成文件名
	 * @param url
	 * @return
	 */
	private String convertUrlToFileName(String url) {
		// TODO Auto-generated method stub
		return MD5.getMD5(url) + ClearConfig.WHOLESALE_CONV;
	}

	/**
	 * 计算sdcard上的剩余空间
	 * @return
	 */
	private int freeSpaceOnSd() {
		// TODO Auto-generated method stub
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
		double sdFreeMB = ((double)stat.getAvailableBlocks() * (double)stat.getBlockSize()) / ClearConfig.MB;
		return (int)sdFreeMB;
	}
	
	/**
     * 根据文件的最后修改时间进行排序
     */
	private class FileLastModifSort implements Comparator<File>{

		@Override
		public int compare(File file, File file1) {
			// TODO Auto-generated method stub
			if(file.lastModified() > file1.lastModified()){
				return 1;
			}else if(file.lastModified() == file1.lastModified()){
				return 0;
			}else{
				return -1;
			}
		}
	}
}
