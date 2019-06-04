package com.clearcrane.apkupdate;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.InstallApkUtils;
import com.clearcrane.util.MD5Util;
import com.clearcrane.vod.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class UpdateManager {

	public final String TAG = this.getClass().getSimpleName();
	/* 下载中 */
	private static final int DOWNLOAD = 1;
	/* 下载结束 */
	private static final int DOWNLOAD_FINISH = 2;

	/** 自动更新 */
	private static final int AUTO_UPDATE = 3;

	/* 保存解析的XML信息 */
	HashMap<String, String> mHashMap;
	/* 下载保存路径 */
	private String mSavePath;
	/* 记录进度条数量 */
	private int progress;
	/* 是否取消更新 */
	private boolean cancelUpdate = false;

	private Context mContext;
	/* 更新进度条 */
	private ProgressBar mProgress;
	private Dialog mDownloadDialog;

	private Dialog mNoticeDialog;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// 正在下载
			case DOWNLOAD:
				// 设置进度条位置
				mProgress.setProgress(progress);
				break;
			case DOWNLOAD_FINISH:
				// 安装文件
				Log.i("jason", "DOWNLOAD_FINISH");

				new AsyncTask<Void, Void, Void>() {

					protected Void doInBackground(Void... arg0) {

						File apkfile = new File(mSavePath, mHashMap.get("apkname"));

						if (!apkfile.exists())
							return null;

						int currentVersionCode = getVersionCode(mContext);
						int remoteVersionCode = InstallApkUtils.getVersionNameFromApk(mContext, apkfile.toString());

						if (currentVersionCode >= remoteVersionCode) {
							Log.e(TAG, "远程apk版本号低于当前版本号 " + "currentVersionCode=" + currentVersionCode
									+ ",remoteVersionCode=" + remoteVersionCode);
							return null;
						}

						if (!checkMd5(apkfile.toString())) {
							Log.e(TAG, "md5值不匹配，apk文件完整性缺失！");
							return null;
						}

						int result = InstallApkUtils.installApk(apkfile.toString());

						if (9 == result || -1 == result) {

							Log.e(TAG, "root权限获取失败，将进行普通安装");
							Intent intent = new Intent();
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent.setAction(Intent.ACTION_VIEW);
							intent.setDataAndType(Uri.fromFile(apkfile), "application/vnd.android.package-archive");
							mContext.startActivity(intent);
						}

						return null;
					}
				}.execute();

				break;

			case AUTO_UPDATE:

				if (mNoticeDialog.isShowing()) {
					mNoticeDialog.dismiss();
				}

				// 显示下载对话框
				showDownloadDialog();

				break;

			default:
				break;
			}
		};
	};

	public UpdateManager(Context context) {
		this.mContext = context;
	}

	/**
	 * 检测软件更新
	 *
	 * @throws IOException
	 * @throws NotFoundException
	 */
	public void checkUpdate() throws NotFoundException, IOException {
		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					if (isUpdate()) {
						// 显示提示对话框
						Looper.prepare();
						showNoticeDialog();
						Looper.loop();
						Log.d("消息", "有新版本");

					} else {
						Log.d("消息", "已是最新版本");
					}
				} catch (NotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}).start();
	}

	/**
	 * 检查软件是否有更新版本
	 *
	 * @return
	 * @throws IOException
	 */
	private boolean isUpdate() throws IOException {

		// 获取当前软件版本
		int versionCode = getVersionCode(mContext);

		// 把version.xml放到网络上，然后获取文件信息
		Log.i(TAG, "updateApkUrl="+ ClearConfig.CLOUD_UPDATE_SERVER);
		URL url = new URL(ClearConfig.CLOUD_UPDATE_SERVER);

		HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
		InputStream inStream = urlConn.getInputStream();

		// 解析XML文件。 由于XML文件比较小，因此使用DOM方式进行解析
		ParseXmlService service = new ParseXmlService();

		try {
			mHashMap = service.parseXml(inStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (null != mHashMap) {
			int serviceCode = Integer.valueOf(mHashMap.get("version"));
			Log.i("-----versionCode", "" + versionCode);

			// 版本判断
			if (serviceCode > versionCode) {
				return true;
			}
		} else {
			Log.i("-----null == mHashMap", "null == mHashMap");
		}

		return false;
	}

	/**
	 * 获取软件版本号
	 *
	 * @param context
	 * @return
	 */
	private int getVersionCode(Context context) {

		int versionCode = 0;
		try {
			// 获取软件版本号，对应AndroidManifest.xml下android:versionCode
			versionCode = context.getPackageManager().getPackageInfo("com.clearcrane.vod", 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}

	/**
	 * 显示软件更新对话框
	 */
	public void showNoticeDialog() {

		mHandler.sendEmptyMessageDelayed(AUTO_UPDATE, 5000);

		if (mNoticeDialog == null) {

			// 构造对话框
			Builder builder = new Builder(mContext);
			builder.setTitle(R.string.app_update_alert_title);
			builder.setMessage(R.string.app_update_alert_body);

			// 更新
			builder.setPositiveButton(R.string.soft_update, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mHandler.removeMessages(AUTO_UPDATE);
					dialog.dismiss();
					// 显示下载对话框
					showDownloadDialog();
				}
			});
			// 取消
			builder.setNegativeButton(R.string.cancel, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mHandler.removeMessages(AUTO_UPDATE);
					dialog.dismiss();
				}
			});

			mNoticeDialog = builder.create();
		}

		mNoticeDialog.show();

	}

	/**
	 * 显示软件下载对话框
	 */
	private void showDownloadDialog() {

		if (mDownloadDialog == null) {
			// 构造软件下载对话框
			Builder builder = new Builder(mContext);
			builder.setTitle(R.string.soft_updating);

			// 给下载对话框增加进度条
			final LayoutInflater inflater = LayoutInflater.from(mContext);
			View v = inflater.inflate(R.layout.softupdate_progress, null);
			mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
			builder.setView(v);
			// 取消更新
			builder.setNegativeButton(R.string.soft_update_cancel, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					// 设置取消状态
					cancelUpdate = true;
				}
			});

			mDownloadDialog = builder.create();
		}

		mDownloadDialog.show();
		// 下载文件
		downloadApk();
	}

	/**
	 * 下载apk文件
	 */
	private void downloadApk() {
		// 启动新线程下载软件
		new downloadApkThread().start();
	}

	/**
	 * 下载文件线程
	 */
	private class downloadApkThread extends Thread {
		@Override
		public void run() {
			try {
				// 判断SD卡是否存在，并且是否具有读写权限
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					// 获得存储卡的路径
					String sdpath = Environment.getExternalStorageDirectory() + "/";
					mSavePath = sdpath + "download";
					URL url = new URL(mHashMap.get("apkurl"));
					// 创建连接
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.connect();
					// 获取文件大小
					int length = conn.getContentLength();
					// 创建输入流
					InputStream is = conn.getInputStream();

					File file = new File(mSavePath);
					// 判断文件目录是否存在
					if (!file.exists()) {
						file.mkdir();
					}
					File apkFile = new File(mSavePath, mHashMap.get("apkname"));
					FileOutputStream fos = new FileOutputStream(apkFile);
					int count = 0;
					// 缓存
					byte buf[] = new byte[1024];
					// 写入到文件中
					do {
						int numread = is.read(buf);
						count += numread;
						// 计算进度条位置
						progress = (int) (((float) count / length) * 100);
						// 更新进度
						mHandler.sendEmptyMessage(DOWNLOAD);
						if (numread <= 0) {
							// 下载完成
							mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
							break;
						}
						// 写入文件
						fos.write(buf, 0, numread);
					} while (!cancelUpdate);// 点击取消就停止下载.
					fos.close();
					is.close();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// 取消下载对话框显示
			mDownloadDialog.dismiss();
		}
	};

	/**
	 * MD5检测，确保apk文件完整性
	 * 
	 * @return
	 */
	private boolean checkMd5(String archiveApkPath) {

		File file = new File(archiveApkPath);
		try {
			String fileMd5 = MD5Util.getFileMD5String(file).trim();
			Log.e(TAG, "download file md5 = " + fileMd5);
			Log.e(TAG, "update   file md5 = " + mHashMap.get("md5"));
			if (fileMd5 != null && fileMd5.trim().equalsIgnoreCase(mHashMap.get("md5").trim())) {
				return true;
			}
		} catch (Exception e) {
			Log.e(TAG, "[" + Log.getStackTraceString(e) + "]");
		}
		Log.e(TAG, "check md5 failed");
		return false;
	}

}
