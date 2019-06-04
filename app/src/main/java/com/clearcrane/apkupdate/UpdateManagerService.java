package com.clearcrane.apkupdate;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.Binder;
import android.os.IBinder;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.interfaces.IMyBinder;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 更新apk服务
 * @author SlientLeaves
 * 2016年12月5日  下午4:54:02
 */
public class UpdateManagerService extends Service{

	private UpdateManager mUpdateManager;
	private Timer mUpdateTimer;
	private Context mContext;
	
	public UpdateManagerService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		
		//返回MyBind对象
		return new MyBinder();
	}

	private class MyBinder extends Binder implements IMyBinder {

		@Override
		public void invokeMethodInMyService() {
			mUpdateManager = new UpdateManager(mContext);
			
			TimerTask updateTimerTask = new TimerTask() {

				@Override
				public void run() {
					try {
						mUpdateManager.checkUpdate();
					} catch (NotFoundException | IOException e) {
						e.printStackTrace();
					}
				}

			};
			
			mUpdateTimer = new Timer();
			mUpdateTimer.scheduleAtFixedRate(updateTimerTask, 0, ClearConstant.UPDATE_APK_DURATION);
			
		}
		
		public UpdateManagerService getService()

		{
			return UpdateManagerService.this;
		}
		
	}
	
	/**
	 * 
	 * @param context
	 */
	public void setContext(Context context){

		this.mContext=context;

	}
	
}
