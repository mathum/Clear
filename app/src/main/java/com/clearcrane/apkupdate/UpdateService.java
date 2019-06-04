package com.clearcrane.apkupdate;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.databean.DoomUpDateParser;
import com.clearcrane.databean.UpDate;
import com.clearcrane.util.BeanXMLUpdateInfo;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.FileUtil;
import com.clearcrane.util.MD5Util;
import com.clearcrane.util.PlatformSettings;
import com.clearcrane.util.PlatformSettings.Platform;
import com.clearcrane.vod.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("HandlerLeak")
public class UpdateService extends Service {
	private static final String TAG = "update";

	private static final String ACTION_START = "action_start";
	private static final String ACTION_STOP = "action_stop";
	private static final String ACTION_RESTART = "action_restart";

	public final static int UP_DOWNLOADING = 1;
	public final static int UP_DOWNLOADED = 2;
	public final static int UP_DOWNLOADFAIL = 3;

	// private static final String UPDATE_URL =
	// "http://vodtest.cleartv.cn/update/hoteltv/update.xml";
	private static final int TIMESTEP = 1 * 60 * 1000; // ms
	// private static final int TIMESTEP = 5 * 1000; // ms test

	private Timer mTimer = null;
	// private ClearApplication mApp = null;
	private BeanXMLUpdateInfo mUpInfo = null;

	private boolean mStarted;
	private SharedPreferences mPrefs;

	private int mUpdateState;

	HttpClient mHttpClient = null;
	HttpGet mHttpGet;

	private AlertDialog updateDialog = null;
	private Builder mBuilder;
	private Timer updateTimer = null;
	private int mWaitTime;
	private TimerTask mTimerTask;
	private List<UpDate> updates;//更新文件中所有的设备更新列表
    private UpDate curUpdate = null;//当前设备需要更新的内容
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UP_DOWNLOADED:
				updateDialog.show();
				break;
			case UP_DOWNLOADFAIL:
				start();
				break;
			}
		}
	};

	// Static method to start the service
	public static void actionStart(Context ctx) {
		Intent i = new Intent(ctx, UpdateService.class);
		i.setAction(ACTION_START);
		ctx.startService(i);
	}

	// Static method to stop the service
	public static void actionStop(Context ctx) {
		Intent i = new Intent(ctx, UpdateService.class);
		i.setAction(ACTION_STOP);
		ctx.startService(i);
	}

	public static void actionRestart(Context ctx) {
		Intent i = new Intent(ctx, UpdateService.class);
		i.setAction(ACTION_RESTART);
		ctx.startService(i);
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		mPrefs = getSharedPreferences(ClearConfig.STR_NETWORK, Context.MODE_PRIVATE);
		// 创建下载框
		createDialog();
		handleCrashedService();
	}

	public void onDestroy() {
		Log.d(TAG, "Service destroyed (started=" + mStarted + ")");

		// Stop the services, if it has been started
		if (mStarted == true) {
			stop();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onStart(final Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d(TAG, "Service started with intent=" + intent);

		if (intent == null) {
			return;
		}

		// Do an appropriate action based on the intent.
		if (intent.getAction().equals(ACTION_STOP) == true) {
			stop();
			stopSelf();
		} else if (intent.getAction().equals(ACTION_START) == true) {
			start();
		} else if (intent.getAction().equals(ACTION_RESTART) == true) {
			stop();
			start();
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 本地升级 0, ok, ready to upgrade -1, ok, no need to upgrade
	 */
	private synchronized int upgradeFromLocalNet() {
		try {
			String xmlUrl = ClearConfig.LOCAL_UPDATE_SERVER;
			Log.e(TAG, "xmlUrl locat " + xmlUrl);
			mHttpGet.setURI(new URI(xmlUrl));
			mHttpGet.setHeader("Connection", "close");
			HttpResponse httpResponse = mHttpClient.execute(mHttpGet);
			if (httpResponse.getStatusLine().getStatusCode() >= 400) {
				Log.e(TAG, "xml not found");
				return -1;
			} else {
				//读取xml文件的内容
//				BeanAdapter xmlAdapter = new XMLBeanAdapter();
//				BeanXMLUpdateInfo info = (BeanXMLUpdateInfo) xmlAdapter.getBean(httpResponse.getEntity().getContent(),
//						BeanXMLUpdateInfo.class);
//				if (info == null || info.getVersionCode() <= ClearConfig.getVersionCode() || info.getUrl() == null
//						|| info.getUrl().trim().length() <= 0) {
//					Log.e(TAG, "no need to update");
//					return -1;
//				}
//				if (info.isAcl() && !info.isInSet(ClearConfig.getMac())) {
//					Log.e(TAG, "check acl, no need to update");
//					return -1;
//				}
//
//				mUpInfo = info;
				DoomUpDateParser doomUpDateParser = new DoomUpDateParser();
				updates = doomUpDateParser.parse(httpResponse.getEntity().getContent());
				for(UpDate update : updates){
				   Log.i(TAG,update.toString());
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "[" + Log.getStackTraceString(e) + "]");
			return -1;
		}

		return 0;
	}

	public String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;

		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/**
	 * 在线升级 0, ok, ready to upgrade 1, ok, no need to upgrade -1, try local
	 * upgrade later
	 */
	private synchronized int upgradeFromCloud() {
		Log.i(TAG, "upgradefrom cloud");
		HttpClient mCloudClient = new DefaultHttpClient();
		HttpGet mCloudHttpGet = new HttpGet();

		String url = ClearConfig.CLOUD_UPDATE_SERVER;
		url = url + "?protocolversion=1&project=Unknown&method=upgrade&entitytype=nativevod";
		url = url + "&entityid=" + ClearConfig.getMac();
		url = url + "&version=" + ClearConfig.getVersionCode();

		Log.e(TAG, "cloud upgrade request: " + url);

		try {
			mCloudHttpGet.setURI(new URI(url));
			mCloudHttpGet.setHeader("Connection", "close");

			HttpResponse httpResponse = mCloudClient.execute(mCloudHttpGet);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				Log.e(TAG, "cloud upgrade service unavailable");
				return -1;
			}

			JSONObject jObject = new JSONObject(convertStreamToString(httpResponse.getEntity().getContent()));

			String code = jObject.getString("code");
			if (code.equals("201") == false) {
				// no need to upgrade or request failed
				Log.i(TAG, "cloud upgrade response: " + code);
				Log.i(TAG, "no need to upgrade or request failed");
				return 1;
			}

			String version = jObject.getString("version");
			String upgradeFileURL = "";
			String md5sum = "";

			JSONArray upgradeFiles = jObject.getJSONArray("upgradeFileURL");
			if (upgradeFiles.length() > 0) {
				upgradeFileURL = upgradeFiles.getJSONObject(0).getString("url");
				md5sum = upgradeFiles.getJSONObject(0).getString("md5sum");
			}

			BeanXMLUpdateInfo info = new BeanXMLUpdateInfo();
			info.setVersionCode(Integer.parseInt(version));
			info.setDescription("CloudUpgrade");
			info.setUrl(upgradeFileURL);
			info.setMd5(md5sum);
			info.setApkName("ClearNativeVoD.apk");
			info.setAcl(false);

			Log.i(TAG, "cloud upgrade response:\n" + "version=" + version + "\n" + "upgradeFileURL=" + upgradeFileURL
					+ "\n" + "md5sum=" + md5sum);

			mUpInfo = info;
		} catch (Exception e) {
			Log.e(TAG, "[" + Log.getStackTraceString(e) + "]");
			return -1;
		}

		return -1;
	}

	private synchronized void start() {
		if (mStarted) {
			Log.d(TAG, "update service is started");
			return;
		}
		mHttpClient = new DefaultHttpClient();
		mHttpGet = new HttpGet();

		TimerTask task = new TimerTask() {

			@Override
			public void run() {

				/* first check cloud upgrade */
				int res = upgradeFromCloud();
				if (res == 1) {
					return;
				} else if (res == 0) {
					doUpdate();
//					stop();
				} else if (res == -1) {
					Log.e(TAG, "进行本地网络升级");
					/* second check the local server upgrade */
					if (upgradeFromLocalNet() == 0) {
						doUpdate();
//						stop();
					}
				}
			}

		};
		if (mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
			mTimer = null;
		}
		mTimer = new Timer(true);
		mTimer.schedule(task, 20000, TIMESTEP);
		setStarted(true);
	}

	private synchronized void stop() {
		Log.i("in update", "update service is stop");
		if (!mStarted) {
			Log.d(TAG, "update service is stoped");
			return;
		}
		if (mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
			mTimer = null;
		}

		setStarted(false);
	}

	// This method does any necessary clean-up need in case the server has been
	// destroyed by the system
	// and then restarted
	private void handleCrashedService() {
		if (wasStarted() == true) {
			Log.i("in update", "handling cranshed service");
			Log.d(TAG, "Handling crashed service...");
			// Do a clean start
			start();
		}
	}

	private boolean wasStarted() {
		return mPrefs.getBoolean(ClearConstant.PREF_UPDATE_STARTED, false);
	}

	private void setStarted(boolean started) {
		mPrefs.edit().putBoolean(ClearConstant.PREF_UPDATE_STARTED, started).commit();
		mStarted = started;
	}

	private void doUpdate() {
		Log.i("update", "do update");
		if(updates == null || updates.isEmpty()){
			Log.e(TAG, "读取的列表数据为空,不做任何操作");
			return;
		}
		Platform platform= PlatformSettings.getPlatform();
		Log.e(TAG, "设备型号:"+platform);
		//接下来需要具体升级哪种设备
		curUpdate = null;
		for (UpDate upDate : updates) {
		if(upDate.getModel().equals("unknown")){
			curUpdate = upDate;
		}
		if(upDate.getModel().equals(platform.toString())){
			curUpdate = upDate;
			Log.e(TAG, "创维368w"+curUpdate.toString());
			break;
		  }
		}
		if(curUpdate == null || Integer.parseInt(curUpdate.getVersion())<= ClearConfig.getVersionCode()|| 
		curUpdate.getApkurl().equals("")){
//			Log.e(TAG, curUpdate.toString());
			return;
		}
//		if (mUpInfo == null || mUpInfo.getVersionCode() <= ClearConfig.getVersionCode() || mUpInfo.getUrl() == null
//				|| mUpInfo.getUrl().trim().length() <= 0) {
//			Log.e(TAG, "no need to update");
//			return;
//		}

		Log.e(TAG, "do update " + ClearConfig.getVersionCode() + " ---> " + curUpdate.getVersion());

		new UpdateThread().start();
	}

	public void installApk() {
		Log.d(TAG, "install apk");
		if (curUpdate == null || curUpdate.getApkname() == null || curUpdate.getApkname().trim().length() <= 0) {
			Log.e(TAG, "name error");
			return;
		}

		try {
			if(PlatformSettings.getPlatform() == Platform.skyworth_368W
					|| PlatformSettings.getPlatform() == Platform.skyworth_32d5){
				Log.e(TAG, "skyworth_368W upgrade");
		        boolean ret = FileUtil.appendFile(ClearConstant.SKYWORTH_368_UPDATELIST,
		        		getFileStreamPath(curUpdate.getApkname()).toString() + "\n");
		        Log.e(TAG, getFileStreamPath(curUpdate.getApkname()).toString());
		        FileUtil.chmod("777", ClearConstant.SKYWORTH_368_UPDATELIST);     
		        Log.e(TAG, "ret:"+ret);
		        return ;
			}
			// skyworth use packageinstall
			if (PlatformSettings.getPlatform() == Platform.skyworth
					|| PlatformSettings.getPlatform() == Platform.skyworth_3RT84) {
				Intent intent = new Intent("android.intent.action.CLEAR_VIEW");
				intent.setDataAndType(Uri.parse("file://" + getFileStreamPath(curUpdate.getApkname())),
						"application/vnd.android.package-archive");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			} else if(PlatformSettings.getPlatform() == Platform.BaoFeng_50F1) {
				//baofeng apkinstall 
				Log.e(TAG, "BaoFeng upgrade");
				Intent intent = new Intent();
				intent.setAction("com.baofengtv.ACTION.SILENT_INSTALL");
				intent.putExtra("FILE_NAME", getFileStreamPath(curUpdate.getApkname()).toString());
				Log.d(TAG, "packageUri:"+"file://" + getFileStreamPath(curUpdate.getApkname()));
				intent.putExtra("REMOVE_APK_AFTER_INSTALL", true);
				intent.putExtra("INSTALL_LOCATION", 0);
				intent.putExtra("NEED_HINT_INSTALL_RESULT ", true);
				intent.putExtra("CALL_INSTALLER_APP_PACKAGE_NAME","com.clearcrane.vod");
				intent.putExtra("CALL_INSTALLER_APP_ACTIVITY_NAME", "com.clearcrane.apkupdate.UpdataService");
				sendBroadcast(intent);        
			}else {
				// himedia philips clearinstall
				Intent intent = getPackageManager().getLaunchIntentForPackage("com.clearcrane.install");
				// clearinstall need special packagename
				intent.putExtra("packageName", "com.clearcrane.cleartv");
				intent.putExtra("packageUri", "file://" + getFileStreamPath(curUpdate.getApkname()).toString());
				Log.d(TAG, "packageUri:" + "file://" + getFileStreamPath(curUpdate.getApkname()).toString());

//				Intent intent = new Intent(Intent.ACTION_VIEW);
//				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				intent.setDataAndType(Uri.parse("file://" + getFileStreamPath(mUpInfo.getApkName())),"application/vnd.android.package-archive");	
				startActivity(intent);
			}
		} catch (Exception e) {
			Log.e(TAG, "ClearInstall does not exist");
			try {
				Intent installIntent = new Intent(Intent.ACTION_VIEW);
				installIntent.setDataAndType(Uri.parse("file://" + getFileStreamPath(curUpdate.getApkname())),
						"application/vnd.android.package-archive");
				installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				this.startActivity(installIntent);
			} catch (Exception e2) {

				Log.e(TAG, "[" + Log.getStackTraceString(e2) + "]");
			}
		}
	}
	// 启动下载
	public class UpdateThread extends Thread {
		@SuppressWarnings("deprecation")
		@SuppressLint("WorldReadableFiles")
		public void run() {
			Log.i("in update", "updatethread run");
			// Log.d(TAG, "download:" + mUpInfo.getUrl());
			if (curUpdate == null || curUpdate.getApkname() == null || curUpdate.getApkname().trim().length() <= 0) {
				String apkurl = curUpdate.getApkurl();
				String apkname;
				int last = apkurl.lastIndexOf('/');
				if (last >= 0 && last + 1 < apkurl.length()) {
					apkname = apkurl.substring(last + 1);
					curUpdate.setApkname(apkname);
				} else {
					curUpdate.setApkname("ClearTV.apk");
				}
			}
			try {

				if (mUpdateState == UP_DOWNLOADING) {
					Log.d(TAG, "other thread is downloading");
					return;
				}

				URL url = new URL(curUpdate.getApkurl());
				// 创建连接
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.connect();

				int length = conn.getContentLength();
				int count = 0;
				// 创建输入流
				InputStream is = conn.getInputStream();
				FileOutputStream fos = openFileOutput(curUpdate.getApkname(), Context.MODE_WORLD_READABLE);

				Log.e(TAG, "start downloading");
				mUpdateState = UP_DOWNLOADING;
				byte buf[] = new byte[1024];
				// 写入到文件中
				do {
					int numread = is.read(buf);
					if (numread <= 0) {
						// 下载完成
						break;
					} else {
						count += numread;
					}
					// 写入文件
					fos.write(buf, 0, numread);
				} while (true);
				fos.close();
				is.close();
				conn.disconnect();

				// continue original progress
				if (count < length) {
					mUpdateState = UP_DOWNLOADFAIL;
					Log.e(TAG, "file not completely downloaded " + count + " / " + length);
					mHandler.sendEmptyMessage(UP_DOWNLOADFAIL);
				}
				// download completed, start install
				else {
					mUpdateState = UP_DOWNLOADED;
					if (checkMd5(curUpdate)) {
						Log.d(TAG, "download:" + "downloaded");
						mHandler.sendEmptyMessage(UP_DOWNLOADED);
					} else {
						Log.d(TAG, "download:" + "downloadedfail");
						mHandler.sendEmptyMessage(UP_DOWNLOADFAIL);
					}
				}
			} catch (Exception e) {
				Log.e(TAG, "[" + Log.getStackTraceString(e) + "]");
				mUpdateState = UP_DOWNLOADFAIL;
				mHandler.sendEmptyMessage(UP_DOWNLOADFAIL);
			}
		}
	}

	private boolean checkMd5(UpDate upDate) {
		// Log.e(TAG, "file path=" + getFileStreamPath(mUpInfo.getApkName()));
		File file = new File(getFileStreamPath(upDate.getApkname()).toString());
		try {
			String fileMd5 = MD5Util.getFileMD5String(file);
			Log.e(TAG, "download file md5 = " + fileMd5);
			Log.e(TAG, "update   file md5 = " + upDate.getMd5());
			if (fileMd5 != null && fileMd5.trim().equalsIgnoreCase(upDate.getMd5().trim())) {
				return true;
			}
		} catch (Exception e) {
			Log.e(TAG, "[" + Log.getStackTraceString(e) + "]");
		}
		Log.e(TAG, "check md5 failed");
		return false;
	}

	// 下载进度dialog
	private void createDialog() {
		Log.i("in update", "create dialog");
		mBuilder = new Builder(this);
		mBuilder.setTitle(R.string.app_update_alert_title);

		updateTimer = new Timer(true);
		mWaitTime = 5;
		mBuilder.setMessage(R.string.app_update_alert_body);
		mBuilder.setPositiveButton(R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				updateTimer.cancel();
				updateTimer.purge();
				updateTimer = null;
				// start install
				installApk();
			}
		});
		updateDialog = mBuilder.create();
		updateDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				dialog.dismiss();
				updateTimer.cancel();
				updateTimer.purge();
				updateTimer = null;
				// start install
				if (ClearConfig.autoUpdate) {
					installApk();
				}
			}

		});

		updateDialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				mWaitTime = 5;
				if (updateTimer == null) {
					updateTimer = new Timer(true);
				}
				if (mTimerTask != null) {
					mTimerTask.cancel();
				}
				mTimerTask = new UpdateTimerTask();
				updateTimer.schedule(mTimerTask, 1000, 1000);
			}
		});

		updateDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

		mTimerTask = new UpdateTimerTask();
	}

	// 五秒之后自动取消升级dialog
	class UpdateTimerTask extends TimerTask {
		@Override
		public void run() {
			mWaitTime--;
			if (mWaitTime <= 0) {
				if (updateDialog != null && updateDialog.isShowing()) {
					// never cancel updatedialog manue update
					Log.i(TAG, "autoUpdate falg:" + ClearConfig.autoUpdate);
					if (ClearConfig.autoUpdate) {
						updateDialog.cancel();
					}
				}
			}
		}
	}
}
