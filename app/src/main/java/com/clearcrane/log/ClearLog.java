package com.clearcrane.log;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.clearcrane.util.ClearConfig;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

/**
 * @author wgq
 * 收集日志提交给云服务器
 */
public class ClearLog {
	private static final String TAG = "ClearLog";

	/* 上传接口地址 */
	private static String uploadURL = "";
	private static String upPlayerURL = "";
	private static String uploadURLHeartbeatParameter = "msgType=heartbeat";
	private static String uploadURLVersionLocParameter = "msgType=version_location";
	private static boolean initialized = false;
	private static String mac = "";
	private static String localIP = "";
	private static String projectName = "";
	private static boolean runFlag = true;
	
	private static Context mCtx = null;
	
	private static Object lock_t = new Object();
	private static Object lock_log = new Object();
	private static final int MAX_LOG_BUF = 1000;
	private static LinkedList<String> infoList = new LinkedList<String>();
	private static LinkedList<String> logList = new LinkedList<String>();

	private static final int CONSTANT_TAB_COUNT = 12;
	private static final int RES_UPDATE_PERIOD = 30 * 60; //2 minute
	private static final int HEARTBEAT_PERIOD = 20; //20 secs
	
	public static void init(Context ctx, String projectName, String remoteURL, String upPlayURL) {
		if(initialized == true)
			return ;
		mCtx = ctx;
		ClearLog.projectName = projectName;
		uploadURL = remoteURL;
		upPlayerURL = upPlayURL;
		Log.e("eee", uploadURL);
		Log.e("eee", upPlayerURL);
		mac = ClearConfig.getMac();
		try {
			Log.i("ClearLog", "get ip " );
			localIP = ClearConfig.getLocalIPAddres();
			Log.i("ClearLog", "after get ip " );
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			localIP = "0.0.0.0";
		}
//		postThread.start();
//		resUpdateThread.start();
//		heartbeatThread.start();
//		versionLocThread.start();
		logSentThread.start();
		
		initialized = true;
	}
	
	public static void finit() {
		runFlag = false;
	}
	
	private static Thread heartbeatThread = new Thread(new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(runFlag) {
				
				try {
					Thread.sleep(HEARTBEAT_PERIOD * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
				
				Log.d(TAG, "POST a heartbeat: ");
				doPost(uploadURL + "?" + uploadURLHeartbeatParameter, 
						mac + "," + TimeStamp());
			}
		}
	});
	
	private static Thread versionLocThread = new Thread(new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			int i = 0; 
			while(runFlag) {
				
				try {
					if(i < 3) {
						Thread.sleep(HEARTBEAT_PERIOD * 3 * 1000);
					}
					else {
						Thread.sleep(3600 * 1000);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
				
				Log.d(TAG, "POST a version location: ");
				doPost(uploadURL + "?" + uploadURLVersionLocParameter, 
						mac 
						+ "," + TimeStamp() 
						+ "," + ClearConfig.getVersionName()
						+ "," + ClearConfig.getVersionCode()
						+ "," + "Unknown"
						+ "," + "Unknown");
				i ++;
			}
		}
	});
	
	private static Thread resUpdateThread = new Thread(new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(runFlag) {
				updateCPUMemDiskUsage();
				
				try {
					Thread.sleep(RES_UPDATE_PERIOD * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		}
	});
	
	private static Thread postThread = new Thread(new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String info;
			while(runFlag) {
				info = null;
				synchronized(lock_t){
					if(infoList.isEmpty() == false) {
						info = infoList.pop();
					}
				}
				if(info != null) {
					Log.d(TAG, "POST: " + info);
					doPost(info);
				}
				else {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	});
	
	private static void doPost(String url, String info) {
		Log.d(TAG, "info : " + info);
		Log.d(TAG, " url : " + url);
		HttpClient hc = new DefaultHttpClient();
		HttpPost hp = new HttpPost(url);
		
		try {
			hp.setEntity(new StringEntity(info));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return ;
		}
		
		HttpResponse hr = null;
		try {
			hr = hc.execute(hp);
		} catch (Exception e) {
			Log.d(TAG, "POST fail: " + info);
			e.printStackTrace();
			return ;
		} 
		
		if(hr.getStatusLine().getStatusCode() == 200){
		
		}
	}
	
	private static void doPost(String info) {
		doPost(uploadURL, info);
	}
	
	private static void Log_Inter(String info) {
		Log.d(TAG, "Log_Inter: " + info);
		
		/* check if info has 12 \t and end with \n */
		if(info == null || info.length() <= 0) {
			return ;
		}
		
		int tabCount = 0;
		for(int i = 0; i < info.length(); i++) {
			if (info.charAt(i) == '\t') {
				tabCount ++;
			}
		}
		while(tabCount < CONSTANT_TAB_COUNT) {
			info = info + "\t";
			tabCount ++;
		}
		
		info = info + "\n";
		
		/* 尽量不要阻塞 ，提交到队列就返回 */
		synchronized(lock_t){
			if(infoList.size() < MAX_LOG_BUF) {
				infoList.add(info);
			}
			else {
				Log.d("ClearLog", "Too many log in upload buf, ignore one");
			}
		}
	}
	
	/* 不为每个事件提供接口，只提供一个传输接口 */
	public static void LogInfo(String info) {
		if(initialized == false)
			return ;
		String context = TimeStamp() + "\t" + projectName + "\t" +  mac + "\t"
				+ localIP + "\t0.0.0.0\tINFO\t" + info;
		Log_Inter(context);
	}
	
	public static void LogWarn(String info) {
		if(initialized == false)
			return ;
		String context = TimeStamp() + "\t" + projectName + "\t" +  mac + "\t"
				+ localIP + "\t0.0.0.0\tWARN\t" + info;
		Log_Inter(context);
	}
	
	public static void LogError(String info) {
		if(initialized == false)
			return ;
		String context = TimeStamp() + "\t" + projectName + "\t" +  mac + "\t"
				+ localIP + "\t0.0.0.0\tIERROR\t" + info;
		Log_Inter(context);
	}
	
	public static String TimeStamp() { 
		Date now = new Date(); 
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss"); 
		return sdf.format(now); 
	}
	
	@SuppressLint("NewApi")
	public static void updateCPUMemDiskUsage() {
		
		String cpuUsage = ((int)(readCPUUsage() * 100)) + "%";
		
		ActivityManager activityManager = (ActivityManager) mCtx.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		activityManager.getMemoryInfo(mi);
		String memInfo = mi.availMem/1024/1024 + "MB/" + mi.totalMem/1024/1024 + "MB";
		
		File root = Environment.getRootDirectory();  
		StatFs sf = new StatFs(root.getPath());  
        long blockSize = sf.getBlockSize();  
        long availCount = sf.getAvailableBlocks(); 
        long totalCount = sf.getBlockCount();
        String storageInfo = (availCount * blockSize / 1024 / 1024) + "MB/" 
        		+ (totalCount * blockSize / 1024 / 1024) + "MB";
		
		Log.i(TAG, "CPU: " + cpuUsage);
		Log.i(TAG, "Memory: " + memInfo);
		Log.i(TAG, "Storage: " + storageInfo);
		
		LogInfo("RES\tREPORT\t"+ cpuUsage+"\t"+memInfo+"\t"+storageInfo);
	}
	
	public static float readCPUUsage() {
	    try {
	        RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
	        String load = reader.readLine();

	        String[] toks = load.split(" ");

	        long idle1 = Long.parseLong(toks[5]);
	        long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
	              + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

	        try {
	            Thread.sleep(360);
	        } catch (Exception e) {}

	        reader.seek(0);
	        load = reader.readLine();
	        reader.close();

	        toks = load.split(" ");

	        long idle2 = Long.parseLong(toks[5]);
	        long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
	            + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

	        return (float)(cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

	    } catch (IOException ex) {
	        ex.printStackTrace();
	    }

	    return 0;
	}
	
	
    public static void logInsert(String info) {
         Log.d("eee", "Log_Inter: " + info);

        /* check if info has 12 \t and end with \n */
        if (info == null || info.length() <= 0) {
            return;
        }

/*        int tabCount = 0;
        for (int i = 0; i < info.length(); i++) {
            if (info.charAt(i) == '\t') {
                tabCount++;
            }
        }
        while (tabCount < CONSTANT_TAB_COUNT) {
            info = info + "\t";
            tabCount++;
        }

        info = info + "\n";*/

        /* 尽量不要阻塞 ，提交到队列就返回 */
        synchronized (lock_log) {
            if (logList.size() < MAX_LOG_BUF) {
                logList.add(info);
            } else {
                Log.i(TAG, "Too many log in upload buf, ignore one");
            }
        }
    }
	
    private static Thread logSentThread = new Thread(new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            String info;
            while (runFlag) {
                info = null;
                synchronized (lock_log) {
                    if (logList.isEmpty() == false) {
                        info = logList.pop();
                        Log.e("eee", "运行中："+info);
                    }
                }
                if (info != null) {
                    Log.e("eeee", "POST: " + info);
                    doLogPost(upPlayerURL,info);
                } else {
                    try {
                        Thread.sleep(1000);
                        //Log.i(TAG, "lala");
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    });
	
    private static void doLogPost(String upPlayerURL,String info) {
        LogSent(upPlayerURL, info);
    }
    
    public static void LogSent(String url,String paras) {

        // TODO Auto-generated method stub
        try {
            HttpClient mHttpClient = new DefaultHttpClient();
            StringEntity entity = new StringEntity(paras,"utf-8");
            HttpResponse mHttpRes = null;
            HttpPost mHttpPost = new HttpPost(url);
            mHttpPost.setEntity(entity);
            mHttpRes = mHttpClient.execute(mHttpPost);
            if (mHttpRes.getStatusLine().getStatusCode() == 200) {
                Log.i("Clear", "Post OK");
            } else {
                Log.e("Clear", "Post error!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
