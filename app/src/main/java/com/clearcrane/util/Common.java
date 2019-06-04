package com.clearcrane.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class Common {
	/** 
	* 打印日志时获取当前的程序文件名、行号、方法名 输出格式为：[FileName | LineNumber | MethodName] 
	* 
	* @return 
	*/ 
	public static String getFileLineMethod() { 
		StackTraceElement traceElement = ((new Exception()).getStackTrace())[1]; 
		StringBuffer toStringBuffer = new StringBuffer("[").append( 
		traceElement.getFileName()).append(" | ").append( 
		traceElement.getLineNumber()).append(" | ").append( 
		traceElement.getMethodName()).append("]"); 
		return toStringBuffer.toString(); 
	} 

	// 当前文件名 
	public static String _FILE_() { 
		StackTraceElement traceElement = ((new Exception()).getStackTrace())[1]; 
		return traceElement.getFileName(); 
	} 

	// 当前方法名 
	public static String _FUNC_() { 
		StackTraceElement traceElement = ((new Exception()).getStackTrace())[1]; 
		return traceElement.getMethodName(); 
	} 

	// 当前行号 
	public static int _LINE_() { 
		StackTraceElement traceElement = ((new Exception()).getStackTrace())[1]; 
		return traceElement.getLineNumber(); 
	} 

	// 当前时间 
	public static String _TIME_() { 
		Date now = new Date(); 
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); 
		return sdf.format(now); 
	}
	
	public static long readClearServerTime() {
    	try {
			URL url = new URL("http://101.231.164.210/tools/time.php");
			HttpURLConnection conn = (HttpURLConnection) url
					.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "text/xml");
			conn.setRequestProperty("charset", "utf-8");
			conn.setConnectTimeout(10000);
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				BufferedReader ins = new BufferedReader(
						new InputStreamReader(conn.getInputStream(),
								"utf-8"));
				String retData = null;
				String time = "";
				while ((retData = ins.readLine()) != null) {
					time += retData;
				}
				ins.close();

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				
				Date date = sdf.parse(time);
				
				return date.getTime();
			} else {
				Log.e("Date", "response code=" + conn.getResponseCode());
			}
		} catch (Exception e) {
			Log.e("Date", "Exception");
			e.printStackTrace();
		}
    	return 0;
	}
	
	public static String readContentFromGet(String uri) {
    	try {
			URL url = new URL(uri);
			HttpURLConnection conn = (HttpURLConnection) url
					.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "text/xml");
			conn.setRequestProperty("charset", "utf-8");
			conn.setConnectTimeout(10000);
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				BufferedReader ins = new BufferedReader(
						new InputStreamReader(conn.getInputStream(),
								"utf-8"));
				String retData = null;
				String content = "";
				while ((retData = ins.readLine()) != null) {
					content += retData;
				}
				ins.close();
				conn.disconnect();
				return content;
			} else {
				Log.e("Date", "response code=" + conn.getResponseCode());
			}
			conn.disconnect();
		} catch (Exception e) {
			Log.e("Date", "Exception");
			e.printStackTrace();
		}
    	return null;
	}
	
	static boolean completeSetted = false;
	public static void setVoDStartComplete() {
		if(completeSetted == false) {
			Log.i("Leo", "set vod load complete");
			do_exec("touch /data/local/tmp/vod_start_complete");
			completeSetted = true;
		}
	}
	
	public static String do_exec(String cmd) {  
        String s = cmd + "\n";  
        try {  
            Process p = Runtime.getRuntime().exec(cmd);  
            BufferedReader in = new BufferedReader(  
                                new InputStreamReader(p.getInputStream()));  
            String line = null;  
            while ((line = in.readLine()) != null) {  
                s += line + "\n";                 
            }  
            
            Log.d("Leo cmd out:", s);
            
            p.waitFor();
            Log.d("Ex.Value",Integer.toString(p.exitValue()));
            
        } catch (IOException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
        return cmd;       
    }  
	
	public static String getPageFileNameFromURL(String url) {
		String urlWithOutPara = url;
		if(url.indexOf('?') > 0) {
			urlWithOutPara = url.substring(0, url.indexOf('?'));
		}

		return urlWithOutPara.substring(urlWithOutPara.lastIndexOf('/') + 1);
	}

}
