package com.clearcrane.util;

import android.graphics.drawable.BitmapDrawable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class UnzipBootAnimation {
	
	private final String BOOTANIMA_ZIP_NAME = "bootanimation.zip";
	private final String PATH_BOOT_ANIMATION_DATA = "/data/local/tmp";
	private final String PATH_BOOT_ANIMATION_SYSTEM = "/system/media";
	private final String TAG = "unzipbootanimation";
	
	ArrayList<BitmapDrawable> bitmaps = new ArrayList<BitmapDrawable>();
	String file_path;
	BitmapDrawable[] bitmapDrawables;
	File bootanimaFile = null;
	ZipFile zipFile = null;
	ArrayList<ZipEntry> picts = new ArrayList<ZipEntry>();
	
	public UnzipBootAnimation(){
		getBootanimationFile();
		initZipFile();
		unZip();
	}
	
	public ArrayList<BitmapDrawable> getBitmapDrawables(){
		return bitmaps;
	}
	public void getBootanimationFile(){
		File file = getBootanimationFile(PATH_BOOT_ANIMATION_DATA);
		file_path = PATH_BOOT_ANIMATION_DATA + File.separator + BOOTANIMA_ZIP_NAME;
		if(file == null){
			file = getBootanimationFile(PATH_BOOT_ANIMATION_SYSTEM);
			if(file != null)
				file_path = PATH_BOOT_ANIMATION_SYSTEM + File.separator + BOOTANIMA_ZIP_NAME;
		}
		bootanimaFile = file;
	}
	public void initZipFile(){
		if(bootanimaFile != null){
			try {
				zipFile = new ZipFile(file_path);
			} catch (ZipException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private File getBootanimationFile(String path){
		File file = new File(path);
		if(file.isDirectory()){
			File[] files = file.listFiles();
			if(files != null)
			for(File temp : files){
				if(temp.getName().equals(BOOTANIMA_ZIP_NAME))
					return temp;
			}
		}
		return null;
	}
	
	public void unZip(){
		//			fis = new FileInputStream(bootanimaFile);
		//			zis = new ZipInputStream(new BufferedInputStream(fis));
		Enumeration<ZipEntry> enumeration = (Enumeration<ZipEntry>) zipFile.entries();
		ZipEntry zipEntry;
		while(enumeration.hasMoreElements()){
			zipEntry = enumeration.nextElement();
			if(!zipEntry.isDirectory() && zipEntry.getName().endsWith(".jpg"))
			picts.add(zipEntry);
		}
		for(int i = 0; i < picts.size(); i++){
			BitmapDrawable bd = createBitmapDrawable(i);
			if(bd != null){
				bitmaps.add(bd);
			}
		}

	}
	
	private BitmapDrawable createBitmapDrawable(int index){
		BitmapDrawable bitmapDrawable = null;
		try {
			bitmapDrawable = new BitmapDrawable(zipFile.getInputStream(picts.get(index)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bitmapDrawable;
	}
	
}
