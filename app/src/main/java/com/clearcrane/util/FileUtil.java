package com.clearcrane.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.text.TextUtils;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.log.L;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


public class FileUtil {
    private static final String TAG = "FileUtil";

    public static boolean isExitSdcard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * get local file name from remote url
     * http://x.x.x.x/resource/abc.jpg
     * file://xx/xx/xx/abc.jpg
     *
     * @param url
     * @return
     */
    public static String getLocalFileURLByRemoteUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        String filename = getFileNameByUrl(url);
        return ClearConstant.ResourceDir
                + File.separator
                + filename;
    }

    /**
     * get file name from url
     *
     * @param url
     * @return
     */
    public static String getFileNameByUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        int index = url.lastIndexOf('?');
        int index2 = url.lastIndexOf("/");
        if (index > 0 && index2 >= index) {
            L.e(TAG, "Wrong URL format for a download file " + url);
            return UUID.randomUUID().toString();
        }
        return url.substring(index2 + 1, index < 0 ? url.length() : index);
    }



    /**
     * get file extend name
     *
     * @param fileName
     * @return
     */
    public static String getFileExtendName(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        int index = fileName.lastIndexOf('.');
        if (index < 0) {
            return "unknown";
        } else {
            return fileName.substring(index + 1);
        }
    }

    public static boolean isFileExists(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }

        return new File(filePath).exists();
    }

    public static long getFileSize(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return -1;
        }

        return new File(filePath).length();
    }

    public static String readFileContent(String filePath) {
        String content = null;
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(filePath));
            String line = "";
            StringBuffer sbuf = new StringBuffer();
            while ((line = br.readLine()) != null) {
                sbuf.append(line);
            }
            br.close();
            content = sbuf.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            L.w(TAG, "Read " + filePath + " content failed");
            e.printStackTrace();
        }

        return content;
    }

    public static int copyAssetFile2OtherPlace(Context ctx, String src, String dst) {
        AssetManager assetMgr = ctx.getAssets();
        try {
            InputStream is = assetMgr.open(src);
            FileOutputStream dstFile = new FileOutputStream(
                    new File(dst));
            byte[] buffer = new byte[ClearConstant.TEMP_BUF_LEN];
            int nread;
            while ((nread = is.read(buffer)) != -1) {
                if (nread == 0) {
                    nread = is.read();
                    if (nread < 0)
                        break;
                    dstFile.write(nread);
                    continue;
                }
                dstFile.write(buffer, 0, nread);
            }
            dstFile.close();
            is.close();
        } catch (IOException e) {
            L.e(TAG, "Can not copy file from asset to external storage");
            L.e(TAG, src + " -> " + dst);
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public static void deleteAll(String file) {
        if (file == null || file.length() <= 0) {
            L.d(TAG, file + " is null");
            return;
        }

        deleteAll(new File(file));
    }

    /**
     * 递归删除文件
     *
     * @param file 要删除的文件或者文件夹
     * @throws IOException 文件找不到或者删除错误的时候抛出
     */
    public static void deleteAll(File file) {
        // 文件夹不存在不存在
        if (!file.exists()) {
            L.d(TAG, file.getAbsolutePath() + " not exists");
            return;
        }
        boolean rslt = file.delete();// 保存中间结果
        if (!rslt) {// 先尝试直接删除
            // 若文件夹非空。枚举、递归删除里面内容
            File subs[] = file.listFiles();
            for (int i = 0; i <= subs.length - 1; i++) {
                if (subs[i].isDirectory())
                    deleteAll(subs[i]);// 递归删除子文件夹内容
                rslt = subs[i].delete();// 删除子文件夹本身
            }
            rslt = file.delete();// 删除此文件夹本身
        }

        if (!rslt) {
            L.d(TAG, "delete " + file.getAbsolutePath() + " failed");
        }

        return;
    }

    public static void chmod(String permission, String path) {
        try {
            String command = "chmod " + permission + " " + path;
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(command);
        } catch (Exception e) {
            L.d(TAG, "[" + e.getMessage() + "]");
        }
    }

    public static boolean appendFile(String filename, String content) {
        if (TextUtils.isEmpty(filename)) {
            return false;
        }

        try {
            FileWriter writer = new FileWriter(filename, true);
            writer.write(content);
            writer.close();

            return true;
        } catch (Exception e) {
            L.d(TAG, "[" + e.getMessage() + "]");
        }

        return false;
    }
}
