package com.clearcrane.util;

import android.util.Log;

import com.clearcrane.activity.ClearApplication;
import com.clearcrane.log.ClearLog;
import com.clearcrane.schedule.DateUtil;

public class LogUtils {
	   public static void sendLogStart(ClearApplication ctx, String play_type, String resource_type, String resourceName){
		    ctx.interruptProgramResourceName = resourceName;
		    ctx.timeInS = DateUtil.getCurrentTimeSecond();
			String logInsert = (ctx.combinatePostParasString("start", "0", play_type, resource_type,resourceName,
					""));
//			ClearLog.logInsert(logInsert);
			((ClearApplication)ctx.getApplicationContext()).SendLogMode = 1;
			((ClearApplication)ctx.getApplicationContext()).isInterruptProgram = true;
	   }
	   
	   public static void sendLogEnd(ClearApplication ctx, String play_type, String resource_type, String resource_name){
		    ctx.isInterruptProgram = true;
//			String categoryPath = mApp.interruptProgramContent;
			String logInsert = (ctx.combinatePostParasString("stop", "0", play_type, resource_type, resource_name,
					""));
			if(resource_type.equals("直播") && Math.abs(ctx.timeInS- DateUtil.getCurrentTimeSecond())<30){
				Log.e("xb", "播放时间短，不上传日志");
				return;
			}
			ClearLog.logInsert(logInsert);
	   }
}
