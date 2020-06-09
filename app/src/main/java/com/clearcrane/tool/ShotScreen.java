package com.clearcrane.tool;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import com.clearcrane.util.ClearConfig;
import com.clearcrane.util.UpLoadFile;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ShotScreen {
    private static final String TAG = "ShotScreen";
    private static int resource_id = -1;
    private static String fileDir = null;

    public static Bitmap shot(Activity activity) {
        //View是你需要截图的View
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        fileDir = activity.getFilesDir().toString();
        Bitmap b1 = view.getDrawingCache();
        // 获取状态栏高度
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        Log.i("TAG", "" + statusBarHeight);
        // 获取屏幕长和高
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay().getHeight();
        // 去掉标题栏
//		Bitmap b = Bitmap.createBitmap(b1,0,25,320,455);
        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight);
        view.destroyDrawingCache();
        return b;
    }

    public static int saveBitmap(Bitmap bm) {
        Log.e(TAG, "保存图片");
        File f = new File(fileDir, "tempshot.png");
        Log.e(TAG, "保存图片:" + f.getAbsolutePath());
        if (!f.exists()) {
            boolean ms = f.mkdir();
            Log.e(TAG, "创建目录" + ms);
        }
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Log.i(TAG, "已经保存");
            String result = UpLoadFile.uploadFile(f, "http://" + ClearConfig.MAINSERVER_IP + ":8000/upload");
            JSONTokener jsonParser = new JSONTokener(result);
            try {
                JSONObject jsonObject = (JSONObject) jsonParser.nextValue();
                if (jsonObject.getString("rescode").equals("200")) {
                    resource_id = jsonObject.getInt("resource_id");
                } else {
                    resource_id = -1;
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        bm.recycle();
        return resource_id;
    }
//	public static String getSDPath(){ 
//		String sdDir = null; 
//		boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);//判断sd卡是否存在
//		if(sdCardExist){
//			sdDir = Environment.getExternalStorageDirectory().toString()+"/Pictures";//获取根目录
//		}else{
//			//为一些特殊的电视，因为很多目录没有都写权限，需要特殊处理
//			if (PlatformSettings.getPlatform() == Platform.skyworth_368W) {
//				sdDir = Environment.getDataDirectory().toString()+"/local/tmp";
//			}else{
//			}
//		}
//		return sdDir.toString(); 
//		} 
}
