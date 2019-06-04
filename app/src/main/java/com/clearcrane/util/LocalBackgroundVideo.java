package com.clearcrane.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class LocalBackgroundVideo {
	private static String localVideoPath = null;
	public static final String NAME = "videocache";
	private static boolean isDownloading;
	
	private static SharedPreferences mPrefs;
	private static Editor editor;
	private static downloadThread download = null;
	private static String remoteUrl = null;
	public long localFreeSpace = 100000000;//保留至少100M空间
	
	public static String TAG = "httpdownload";
	String path;
	
	public LocalBackgroundVideo(Context ctx){
		isDownloading = false;
		localVideoPath = ctx.getApplicationContext().getFilesDir().getParent() + "/bgvideo";
		Log.d(TAG, "apkPath:"+localVideoPath);
		mPrefs = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE);
		path=ctx.getApplicationContext().getPackageResourcePath();
		Log.d(TAG, "resourcepath:"+path);
		editor = mPrefs.edit();
		Log.d(TAG, "LocalBackgroundVideo()");
	}
	
	private void setRemoteUrl(String url) {
		// TODO Auto-generated method stub
		remoteUrl = url;
	}

	private static Handler mHandler = new Handler() {
	    public void handleMessage(Message msg) {
	    	switch(msg.what){
	    	case 1:
	    		super.handleMessage(msg);
		    	Log.d(TAG, "put localurl true:");
	            editor.putBoolean(remoteUrl, true);
	            editor.commit();
	            break;
	    	default:
	    		break;
	    	}
	    	
    		
	    }
	};
	
	public String getUrl(String remoteUrl){
		Log.d(TAG, "apkPath:"+localVideoPath);
		Log.d(TAG, "resourcepath:"+path);
		setRemoteUrl(remoteUrl);
		String videoName = remoteUrl.substring(remoteUrl.lastIndexOf("/") + 1);
    	String localUrl = localVideoPath + "/" + videoName;
         Log.d(TAG, "geturl:"+remoteUrl+" localurl:" + localUrl);
         File localPath = new File(localVideoPath);
         Log.d(TAG, "freespace:" + localPath.getFreeSpace() + "usablespace:" + localPath.getUsableSpace());
 		
        if(!remoteUrl.startsWith("http://")){
        	return remoteUrl;
        }
		if(mPrefs.getBoolean(remoteUrl, false)){
			Log.d(TAG, "local return true");
			return localUrl;
		}
		Log.d(TAG, "local return false");
		Log.d(TAG, "befor downstart");
		if(!isDownloading){
			download = new downloadThread();
			isDownloading = true;
			download.start();
		}
		
		return remoteUrl;
	}
	
	public void delAllFile(String path){
		 Log.i(TAG, "dell path file:");
		File file = new File(path);
		String[] tmpList = file.list();
		File tmp = null;
		for(int i = 0;i < tmpList.length; i++){
			Log.i(TAG, "i:"+tmpList[i]);
			if(path.endsWith(File.separator)){
				tmp = new File(path + tmpList[i]);
				
			}else{
				tmp = new File(path + "/" + tmpList[i]);
			}
			
			if(tmp.isFile())
				tmp.delete();
		}
	}
	
	
    class downloadThread extends Thread{
    	
    	public void run(){
    		Log.d(TAG, "dwonload thread start" );
    		try{
        		String videoName = remoteUrl.substring(remoteUrl.lastIndexOf("/")+1);
            	
            	File localPath = new File(localVideoPath);
        		if(!localPath.exists() && !localPath.isDirectory())
        			localPath.mkdir();
        			
        		 Log.d(TAG, "freespace:" + localPath.getFreeSpace() +"usablespace:" + localPath.getUsableSpace());
        		long freeSpace = localPath.getFreeSpace();
        		String local = localVideoPath + "/" + videoName;
        		
            	Log.d(TAG, "remote url" + remoteUrl + " localurl:" + local);
                URL url = new URL(remoteUrl);
                Log.d(TAG, "remote url" + remoteUrl );
                File localFile = new File(local);
                Log.d(TAG, " localurl:" + local);
                long nPos = 0;
                if (localFile.exists() && localFile.isFile()) {
                    nPos = localFile.length();
                } else {
                    localFile.createNewFile();
                 }
                RandomAccessFile oSavedFile = new RandomAccessFile(localFile, "rw");
                HttpURLConnection httpConnection = (HttpURLConnection) url
                        .openConnection();
                 httpConnection.setReadTimeout(3 * 1000);
                 httpConnection.setRequestProperty("RANGE", "bytes=" + nPos + "-");
                 httpConnection.connect();
                 
                long length = httpConnection.getContentLength();
                Log.e("downlaoderror6", "download6");
                   Log.e(TAG, "pos=" + nPos + " length=" + length);
                   
                // nginx server return 213
                if (length <= 0 || length == 213 ) {
                     oSavedFile.close();
                    Log.i(TAG, "localfile:" +  "alerady downloaded");
                    Message msg = new Message();
                    msg.what = 1;
                    mHandler.sendMessage(msg);
                    
                    return;
                }
                
                //保留至少100M空间
                
                if( (length - nPos) > (freeSpace - localFreeSpace)){
                	oSavedFile.close();
                	delAllFile(localVideoPath);
                	editor.clear();
                	editor.commit();
                	Log.i(TAG, "delete video ,newspace" +  localFile.getFreeSpace());
                	return;
                	
                }
                
                InputStream input = httpConnection.getInputStream();

                oSavedFile.seek(nPos);
                byte[] b = new byte[1024];

                while (true) {
                    int nRead = input.read(b, 0, 1024);
                    if (nRead <= 0) {
                        break;
                    } else {
                    	//避免耗尽cpu
                    	sleep(1);
                        oSavedFile.write(b, 0, nRead);
                        nPos += nRead;
                    }
                }
                oSavedFile.close();
                input.close();
                httpConnection.disconnect();

                if (nPos == length) {
                    Log.d(TAG, "doDownload success");
                    Message msg = new Message();
                    msg.what = 1;
                    mHandler.sendMessage(msg);
                     return;
                }

                Log.e(TAG, "download fail!");
            } catch (Exception e) {
            	 
            	Log.d(TAG, "doDownload error");
                Log.e(TAG, "[" + Log.getStackTraceString(e) + "]");
            }
    		isDownloading = false;
    	}
    	
    }
    
}
