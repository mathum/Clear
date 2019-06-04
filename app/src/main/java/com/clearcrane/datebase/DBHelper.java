package com.clearcrane.datebase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.SensorManager;

import com.clearcrane.entity.ApkInstallInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * sqlite数据库操作辅助类
 * 
 * @author SlientLeaves 2016年5月27日 下午1:59:42
 */
public class DBHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "app_installed_info_db";
	private static DBHelper mDBHelper;

	/* step 1 :重写构造函数中，继承super的构造函数，创建database */
	public DBHelper(Context context) {

		/*
		 * 第一个参数 为当前环境 。 第二个参数 String name为数据库文件，如果数据存放在内存 ，则为null。
		 * 第三个参数为SQLiteDatabase.CursorFactory factory，存放cursor，缺省设置为null,
		 * 第四个参数为int
		 * version数据库的版本，从1开始，如果版本旧，则通过onUpgrade()进行更新，如果版本新则通过onDowngrade()
		 * 进行发布。例如，我要更改mytable表格，增加一列，或者修改初始化的数据，或者程序变得复杂，我需要增加一个表，
		 * 这时我需要在版本的数字增加， 在加载时，才会对SQLite中的数据库个更新，这点非常重要，同时参见onUpgrade()的说明。
		 */
		super(context, DATABASE_NAME, null, 1);
	}

	/*
	 * step 2：重写onCreate()，如果Android系统中第一次创建我们的数据库时（即后面介绍调用getWritableDatabase()
	 * 或者getReadbleDatabase()时），将调用onCreate()，这这里创建数据库（虽然在构造函数中填入数据库名，
	 * 但数据库的创建实在onCreate()中自动进行。在这里一般进行创建table和写入初始数据
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		/**
		 * 创建table：SQL的语句是
		 * "CREATE TABLE constants(_id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT, value REAL);"
		 * ，
		 * 我们可以直接通过db.execSQL(SQLCommand)来执行没有返回值的SQL语言，例如CREATE，DELETE，UPDATE，
		 * INSERT，DROP。
		 */
		db.execSQL("CREATE TABLE appInfo(id INTEGER PRIMARY KEY AUTOINCREMENT, packageName TEXT, mainIndex INTEGER,subIndex INTEGER,nextViewId TEXT,hasInstalled INTEGER); ");
		db.execSQL("CREATE TABLE downloadInfo( nextViewId TEXT,startPosition INTEGER,endPosition INTEGER,subIndex INTEGER,completeSize INTEGER); ");

		/**
		 * 下面的几个数据来自Android自带的重力表，据说是为了传感器管理用，
		 * Android已经考虑到我们在月球和火星上使用Android手机的情况。
		 */
		ContentValues cv = new ContentValues();

		cv.put("Name", "Gravity, Earth");
		cv.put("Weight", SensorManager.GRAVITY_EARTH);
		db.insert("mytable", "Name", cv);

		cv.put("Name", "Gravity, Mars");
		cv.put("Weight", SensorManager.GRAVITY_MARS);
		db.insert("mytable", "Name", cv);

		cv.put("Name", "Gravity, Moon");
		cv.put("Weight", SensorManager.GRAVITY_MOON);
		db.insert("mytable", "Name", cv);
	}

	/*
	 * step 3： 重写onUpgrade()，如果版本比原来的高，将调用onUpgrade()，在这个例子中，我们删除原来的表格，根据新需求创建
	 * 
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		// 这次同样通过db.execSQL(SQLCommand)来执行没有返回值的SQL语言，将表格删除
		db.execSQL("DROP TABLE IF EXISTS mytable");
		onCreate(db);
	}

	/**
	 * 获取实例
	 * 
	 * @param context
	 * @return
	 */
	public static synchronized DBHelper getInstance(Context context) {

		if (mDBHelper == null) {
			mDBHelper = new DBHelper(context);
		}

		return mDBHelper;
	}

	/**
	 * 插入操作
	 * 
	 * @param packageName
	 * @param mainIndex
	 * @param subIndex
	 * @param nextViewId
	 * @param hasInstalled
	 * @return
	 */
	public boolean insert(String packageName, int mainIndex, int subIndex, String nextViewId, int hasInstalled) {

		boolean flag = false;
		SQLiteDatabase database = null;
		try {
			database = mDBHelper.getWritableDatabase();
			String sql = "INSERT INTO appInfo(packageName,mainIndex,subIndex,nextViewId,hasInstalled) VALUES(?,?,?,?,?)";
			database.execSQL(sql, new Object[] { packageName, mainIndex, subIndex, nextViewId, hasInstalled });
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (database != null) {
				database.close();
			}
		}

		return flag;
	}
	
	/**
	 * 插入apk下载信息
	 * @param nextViewId
	 * @param startPosition
	 * @param endPosition
	 * @param completeSize
	 * @return
	 */
	public boolean insertDownloadInfo(String nextViewId, int startPosition, int endPosition, int completeSize) {

		boolean flag = false;
		SQLiteDatabase database = null;
		try {
			database = mDBHelper.getWritableDatabase();
			String sql = "INSERT INTO downloadInfo(nextViewId,startPosition,endPosition,completeSize) VALUES(?,?,?,?)";
			database.execSQL(sql, new Object[] { nextViewId, startPosition, endPosition, completeSize});
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (database != null) {
				database.close();
			}
		}

		return flag;
	}
	

	/**
	 * 通过包名删除记录
	 * 
	 * @param packageName
	 * @return
	 */
	public boolean deleteByPakageName(String packageName) {
		boolean flag = false;
		SQLiteDatabase database = null;
		try {
			String sql = "DELETE FROM appInfo WHERE packageName=?";
			database = mDBHelper.getWritableDatabase();
			database.execSQL(sql, new Object[] { packageName });
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (database != null) {
				database.close();
			}
		}
		return flag;
	}

	/**
	 * 删除apk下载信息
	 * @param nextViewId
	 * @return
	 */
	public boolean deleteDownloadInfo(String nextViewId) {

		boolean flag = false;
		SQLiteDatabase database = null;
		try {
			database = mDBHelper.getWritableDatabase();
			String sql = "DELETE FROM downloadInfo WHERE nextViewId=? ";
			database.execSQL(sql, new Object[] {nextViewId});
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (database != null) {
				database.close();
			}
		}

		return flag;
	}
	
	/**
	 * 更新apk下载信息操作
	 * 
	 * @param nextViewId
	 * @return
	 */
	public boolean updateDownloadInfoByNextViewId(String nextViewId, int startPosition, int endPosition, int completeSize) {
		boolean flag = false;
		SQLiteDatabase database = null;
		try {
			String sql = "UPDATE downloadInfo set startPosition=?,endPosition=?,completeSize=? where nextViewId=?";
			database = mDBHelper.getWritableDatabase();
			database.execSQL(sql, new Object[] { startPosition,endPosition,completeSize,nextViewId });
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (database != null) {
				database.close();
			}
		}
		return flag;
	}
	
	public boolean updateByNextViewId(String nextViewId) {
		boolean flag = false;
		SQLiteDatabase database = null;
		try {
			String sql = "UPDATE appInfo set hasInstalled=? where nextViewId=?";
			database = mDBHelper.getWritableDatabase();
			database.execSQL(sql, new Object[] { nextViewId });
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (database != null) {
				database.close();
			}
		}
		return flag;
	}

	/**
	 * 更新操作
	 * 
	 * @param packageName
	 * @return
	 */
	public boolean updateByPackageName(String packageName) {
		boolean flag = false;
		SQLiteDatabase database = null;
		try {

			String sql = "UPDATE appInfo set hasInstalled=1 where packageName=?";
			database = mDBHelper.getWritableDatabase();
			database.execSQL(sql, new Object[] { packageName });
			flag = true;

		} catch (Exception e)

		{
			e.printStackTrace();
		} finally

		{
			if (database != null) {
				database.close();
			}
		}
		return flag;

	}

	public String queryByNextViewId(String nextViewId) {
		String packageName = null;
		SQLiteDatabase database = null;
		try {
			String sql = "SELECT * FROM appInfo where hasInstalled = 1 and nextViewId=?";
			database = mDBHelper.getReadableDatabase();
			Cursor cursor = database.rawQuery(sql, new String[] { nextViewId });

			if (cursor.moveToNext()) {
				packageName = cursor.getString(cursor.getColumnIndex("packageName"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (database != null) {
				database.close();
			}
		}
		return packageName;
	}

	public List<ApkInstallInfo> queryByPackageName(String packageName) {
		SQLiteDatabase database = null;
		List<ApkInstallInfo> list = new ArrayList<ApkInstallInfo>();
		try {
			String sql = "SELECT * FROM appInfo where hasInstalled = 1 and packageName=?";
			database = mDBHelper.getReadableDatabase();
			Cursor cursor = database.rawQuery(sql, new String[] { packageName });
			while (cursor.moveToNext()) {
				ApkInstallInfo info = new ApkInstallInfo();
				info.id = cursor.getInt(cursor.getColumnIndex("id"));
				info.packageName = cursor.getString(cursor.getColumnIndex("packageName"));
				info.mainIndex = cursor.getInt(cursor.getColumnIndex("mainIndex"));
				info.subIndex = cursor.getInt(cursor.getColumnIndex("subIndex"));
				info.nextViewId = cursor.getString(cursor.getColumnIndex("nextViewId"));
				info.hasInstalled = cursor.getInt(cursor.getColumnIndex("hasInstalled"));
				list.add(info);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (database != null) {
				database.close();
			}
		}
		return list;
	}
	

	/**
	 * 查询下载信息
	 * @param nextViewId
	 * @return
	 */
	public boolean queryDownloadInfoByNextViewId(String nextViewId) {
		SQLiteDatabase database = null;
		boolean flag = false;
		try {
			String sql = "SELECT * FROM downloadInfo where nextViewId = ? ";
			database = mDBHelper.getReadableDatabase();
			Cursor cursor = database.rawQuery(sql, new String[] {nextViewId});
			if(cursor.moveToNext()){
				flag = true;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (database != null) {
				database.close();
			}
		}
		return flag;
	}
	public boolean queryByParams(String packageName, int mainIndex, int subIndex, String nextViewId) {
		SQLiteDatabase database = null;
		boolean flag = false;
		try {
			String sql = "SELECT * FROM appInfo where packageName = ? and mainIndex = ? and subIndex = ? and nextViewId = ? ";
			database = mDBHelper.getReadableDatabase();
			String mainIndexStr = String.valueOf(mainIndex);
			String subIndexStr = String.valueOf(subIndex);
			Cursor cursor = database.rawQuery(sql, new String[] { packageName,mainIndexStr,subIndexStr,nextViewId});
			if(cursor.moveToNext()){
				flag = true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (database != null) {
				database.close();
			}
		}
		return flag;
	}

}
