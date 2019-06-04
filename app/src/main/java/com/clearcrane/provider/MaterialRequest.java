/**
 * @author xujifu
 * @copyright clear
 * @date 2014-06-17
 * @description 资源下载类
 */
package com.clearcrane.provider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.util.ClearConfig;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@SuppressLint("NewApi")
public class MaterialRequest extends AsyncTask<String, Integer, Object> {

    public interface OnProgressListener {
        public void onProgress(int progress);
    }

    public interface OnCompleteListener {
        public void onDownloaded(Object result);

        public void onComplete(boolean result);
    }

    public interface OnDownLoadCompleListener {
        public void onDownloadedComplete(Object result, String nextViewID);
    }

    private Context mContext = null;
    private ImageView mImageView = null;
    private int mType = -1;
    private int mProgress = 0;
    private String mJsonData;
    private OnCompleteListener mCompleteListener;
    private OnProgressListener mProgressListener;
    private OnDownLoadCompleListener mDownloadCompleteListener;
    private int mSTBType = -1;
    private ImageCache icache;
    public String TAG = "materialrequest";
    private String nextViewID;
//	private MyProgressBarView myProgressBarView;
//	private boolean isShow = true;

    // @TargetApi(11)
    public MaterialRequest(Context context, ImageView imageView, int type) {
        mImageView = imageView;
        mType = type;
        mContext = context;

    }

    public MaterialRequest(Context context, int type) {
        mContext = context;
        mType = type;

    }

    public void setOnCompleteListener(OnCompleteListener listener) {
        mCompleteListener = listener;
    }

    public void setOnProgressListener(OnProgressListener listener) {
        mProgressListener = listener;
    }

    public void setOnDownloadCompleteListener(OnDownLoadCompleListener listener) {
        mDownloadCompleteListener = listener;
    }

    public int GetProgress() {
        return mProgress;
    }

    public boolean isReady() {
        if (100 == mProgress)
            return true;
        return false;
    }

    public ImageView GetImageView() {
        return mImageView;
    }

    public String GetJsonData() {
        return mJsonData;
    }

    /**
     * 这里的Integer参数对应AsyncTask中的第一个参数 这里的Bitmap返回值对应AsyncTask的第三个参数
     * 该方法并不运行在UI线程当中，主要用于异步操作，所有在该方法中不能对UI当中的空间进行设置和修改
     * 但是可以调用publishProgress方法触发onProgressUpdate对UI进行操作
     */
    @Override
    protected Object doInBackground(String... params) {
        if (params.length > 1) {
            nextViewID = params[1];
        }

        // TODO Auto-generated method stub
        Log.i(TAG, "in do in background");
        Log.e("1111111111111111", "params[0] = " + params[0]);
        if (mSTBType == ClearConfig.TYPE_LOCAL_STB) {
            // Log.i(TAG,"stb type params[0]"+params[0]);
            // if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            try {
                Log.i(TAG, "SD card" + Environment.getExternalStorageDirectory().getCanonicalPath());
                // FileInputStream inputStream = new
                // FileInputStream("/mnt/mmcblk1/mmcblk1p1" + params[0]);//TF
                // card
                // FileInputStream inputStream = new
                // FileInputStream(ClearConfig.getTFCard() + params[0]);//北京张斌
                // Log.i(TAG,"url:"+params[0]);
                String url = params[0];
                if (params[0].startsWith("udisk"))
                    url = params[0].replace("udisk", ClearConfig.getTFCard());
                FileInputStream inputStream = new FileInputStream(url);
                byte[] data = new byte[inputStream.available()];
                inputStream.read(data);
                inputStream.close();
                switch (mType) {
                    case ClearConfig.TYPE_IMAGE:
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                        options.inSampleSize = calculateInSampleSize(options, 1280, 700);
                        options.inJustDecodeBounds = false;
                        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                        return bitmap;
                    case ClearConfig.TYPE_IMAGE_SUB_ICON:
                        BitmapFactory.Options option = new BitmapFactory.Options();
                        option.inJustDecodeBounds = true;
                        Bitmap bitmapSub = BitmapFactory.decodeByteArray(data, 0, data.length, option);
                        option.inSampleSize = calculateInSampleSize(option, ClearConstant.SUB_VIEW_IAMGEVIEW_WIDTH,
                                ClearConstant.SUB_VIEW_IAMGEVIEW_HEIGHT);
                        option.inJustDecodeBounds = false;
                        bitmapSub = BitmapFactory.decodeByteArray(data, 0, data.length, option);
                        return bitmapSub;

                    case ClearConfig.TYPE_JSON:
                        return new String(data, "utf-8");
                    case ClearConfig.TYPE_IMAGE_BG:
                        BitmapFactory.Options options2 = new BitmapFactory.Options();
                        options2.inJustDecodeBounds = true;
                        Bitmap bitmap2 = BitmapFactory.decodeByteArray(data, 0, data.length, options2);
                        options2.inSampleSize = calculateInSampleSize(options2, 1280, 700);
                        options2.inJustDecodeBounds = false;
                        bitmap2 = BitmapFactory.decodeByteArray(data, 0, data.length, options2);
                        BitmapDrawable bd = new BitmapDrawable(bitmap2);
                        Drawable d = (Drawable) bd;
                        return d;
                    case ClearConfig.TYPE_IMAGE_BG_SCALE:
                        BitmapFactory.Options options3 = new BitmapFactory.Options();
                        options3.inJustDecodeBounds = true;
                        Bitmap bitmap3 = BitmapFactory.decodeByteArray(data, 0, data.length, options3);
                        options3.inSampleSize = calculateInSampleSize(options3, 300, 168);
                        options3.inJustDecodeBounds = false;
                        bitmap2 = BitmapFactory.decodeByteArray(data, 0, data.length, options3);
                        BitmapDrawable bd2 = new BitmapDrawable(bitmap3);
                        Drawable d2 = (Drawable) bd2;
                        return d2;


                    default:
                        break;
                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // }
        } else {
            // Bitmap bitmap1 = icache.getImage(params[0]);
            // if(bitmap1 != null){
            // return bitmap1;
            // }

            /**
             * 内存图片软引用缓冲
             *
             * HashMap<String,SoftReference<Bitmap>>imageCache=null;
             *
             * imageCache=new HashMap<String,SoftReference<Bitmap>>();
             *
             * //在内存缓存中，则返回Bitmap对象 if(imageCache.containsKey(params[0])) {
             *
             * SoftReference<Bitmap>reference=imageCache.get(params[0]); Bitmap
             * bitmap=reference.get(); if(bitmap!=null) {
             * Log.i("leige",":"+params[0]+"   :"
             * +imageCache.containsKey(params[0])); Drawable d = (Drawable)new
             * BitmapDrawable(bitmap); return d; } } else {
             *
             * /** 加上一个对本地缓存的查找
             *
             * String
             * bitmapName=params[0].substring(params[0].lastIndexOf("/")+1);
             * File cacheDir=new
             * File(mContext.getFilesDir().getAbsolutePath()+"/");
             * File[]cacheFiles=cacheDir.listFiles(); int i=0;
             * if(null!=cacheFiles){ for(;i<cacheFiles.length;i++) {
             * if(bitmapName.equals(cacheFiles[i].getName())) {
             * Log.i("leige2",":"+mContext.getFilesDir().getAbsolutePath());
             * break; } } if(i<cacheFiles.length) {
             * Log.i("leige1",":"+mContext.getFilesDir().getAbsolutePath());
             * Bitmap bitmap =
             * BitmapFactory.decodeFile(mContext.getFilesDir().getAbsolutePath()
             * +"/"+bitmapName); Drawable d = (Drawable)new
             * BitmapDrawable(bitmap); return d; } } }
             *
             **/

            Log.i(TAG, "net type params[0]" + params[0]);
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(params[0]);

            if (params[0].equalsIgnoreCase(ClearConfig.MAIN_URI) || params[0].equalsIgnoreCase(ClearConfig.BACKUP_URI)) {
                httpClient.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);
                httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);// 设置首页超时
            }


            // Log.i("in http ",params[0]);
            InputStream is = null;
            HttpResponse httpResponse = null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                httpResponse = httpClient.execute(httpGet);

                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    // Log.i("in http", "get ok"+params[0]);
                    HttpEntity httpEntity = httpResponse.getEntity();
                    is = httpEntity.getContent();
                    long total = httpEntity.getContentLength();
                    // Log.i("xb", "get ok"+total);
                    byte[] buf = new byte[1024];
                    int count = 0;
                    int length = -1;
                    while ((length = is.read(buf)) != -1) {
                        baos.write(buf, 0, length);
                        count += length;
                        publishProgress((int) (count * 100.0f / total));
                    }
                    switch (mType) {
                        case ClearConfig.TYPE_IMAGE_SUB_ICON:
                            if (is != null) {
                                byte[] data = baos.toByteArray();
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inJustDecodeBounds = true;
                                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                                options.inSampleSize = calculateInSampleSize(options,
                                        ClearConstant.SUB_VIEW_IAMGEVIEW_WIDTH, ClearConstant.SUB_VIEW_IAMGEVIEW_HEIGHT);
                                options.inJustDecodeBounds = false;
                                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

                                return bitmap;
                            }
                            break;
                        case ClearConfig.TYPE_IMAGE:
                            // Bitmap bitmap = null;
                            if (is != null) {
                                byte[] data = baos.toByteArray();
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inJustDecodeBounds = true;
                                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                                options.inSampleSize = calculateInSampleSize(options, ClearConfig.getScreenWidth(),
                                        ClearConfig.getScreenHeight());
                                options.inJustDecodeBounds = false;
                                // Log.i("xb", "TYPE_IMAGE"+data.length);
                                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                                // icache.saveBitmap(bitmap, params[0]);

                                return bitmap;
                            }
                            break;
                        case ClearConfig.TYPE_JSON:
                            if (is != null) {
                                return new String(baos.toByteArray(), "utf-8");
                            }
                            break;
                        case ClearConfig.TYPE_IMAGE_BG:
                            if (is != null) {
                                byte[] data = baos.toByteArray();
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inJustDecodeBounds = true;
                                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                                options.inSampleSize = calculateInSampleSize(options, ClearConfig.getScreenWidth(),
                                        ClearConfig.getScreenHeight());
                                options.inJustDecodeBounds = false;
                                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                                BitmapDrawable bd = new BitmapDrawable(bitmap);

                                /**
                                 * imageCache.put(params[0],new SoftReference
                                 * <Bitmap>(bitmap));
                                 *
                                 * File dir=new
                                 * File(mContext.getFilesDir().getAbsolutePath()+"/"
                                 * ); if(!dir.exists()) { dir.mkdirs(); } File
                                 * bitmapFile=new
                                 * File(mContext.getFilesDir().getAbsolutePath()+
                                 * "/"+
                                 * params[0].substring(params[0].lastIndexOf("/")+1)
                                 * ); if(!bitmapFile.exists()) { try {
                                 * bitmapFile.createNewFile(); } catch(IOException
                                 * e) { //TODOAuto-generatedcatchblock
                                 * e.printStackTrace(); } }
                                 *
                                 * FileOutputStream fos; try { fos=new
                                 * FileOutputStream(bitmapFile);
                                 * bitmap.compress(Bitmap.CompressFormat.PNG,
                                 * 100,fos); fos.close(); }
                                 * catch(FileNotFoundException e) {
                                 * //TODOAuto-generatedcatchblock
                                 * e.printStackTrace(); } catch(IOException e) {
                                 * //TODOAuto-generatedcatchblock
                                 * e.printStackTrace(); }
                                 **/

                                Drawable d = (Drawable) bd;
                                return d;
                            }
                            break;
                        case ClearConfig.TYPE_IMAGE_BG_SCALE:
                            if (is != null) {
                                byte[] data = baos.toByteArray();
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inJustDecodeBounds = true;
                                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                                options.inSampleSize = calculateInSampleSize(options, ClearConfig.getScreenWidth() / 6,
                                        ClearConfig.getScreenHeight() / 6);
                                options.inJustDecodeBounds = false;
                                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                                BitmapDrawable bd = new BitmapDrawable(bitmap);
                                Drawable d = (Drawable) bd;
                                return d;

                            }
                            break;
                        default:
                            break;
                    }
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (baos != null) {
                        baos.close();
                    }
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 这里的String参数对应AsyncTask中的第三个参数（也就是接收doInBackground的返回值）
     * 在doInBackground方法执行结束之后在运行，并且运行在UI线程当中 可以对UI空间进行设置
     */
    @Override
    protected void onPostExecute(Object result) {
        Log.i("xb", result + "");
        switch (mType) {
            case ClearConfig.TYPE_IMAGE_SUB_ICON:
                if (mImageView != null) {
                    mImageView.setImageBitmap((Bitmap) result);
                }
                break;
            case ClearConfig.TYPE_IMAGE:
                if (mImageView != null) {
                    mImageView.setImageBitmap((Bitmap) result);
                }
                break;
            case ClearConfig.TYPE_JSON:
                mJsonData = (String) result;
                break;
            case ClearConfig.TYPE_IMAGE_BG:
                Log.e("LL", "setbak");
                mImageView.setBackground((Drawable) result);
                Log.e("LL", "setbak2");
                break;
            case ClearConfig.TYPE_IMAGE_BG_SCALE:
                mImageView.setBackground((Drawable) result);
                break;
            default:
                break;
        }
        // ((Bitmap)result).recycle();
        if (mCompleteListener != null) {
//			if (myProgressBarView != null || !isShow) {
//				myProgressBarView.clearFocus();
//				myProgressBarView.setVisibility(View.GONE);
//				myProgressBarView = null;
//			}
            mCompleteListener.onDownloaded(result);
            if (result != null) {
                mCompleteListener.onComplete(true);
            } else {
                mCompleteListener.onComplete(false);
            }
        }

        if (mDownloadCompleteListener != null) {
            mDownloadCompleteListener.onDownloadedComplete(result, nextViewID);
        }
    }

    /**
     * 线程开始执行时最先执行的函数 该方法运行在UI线程当中,并且运行在UI线程当中 可以对UI空间进行设置
     */
    @Override
    protected void onPreExecute() {
        if (mImageView == null && ClearConfig.TYPE_IMAGE == mType) {
            mImageView = new ImageView(mContext);
        } else if (mImageView == null && ClearConfig.TYPE_IMAGE_SUB_ICON == mType) {
            mImageView = new ImageView(mContext);
        }
        mProgress = 0;
        mJsonData = "";
        mSTBType = ClearConfig.checkNetwork(mContext);
        icache = new ImageCache();
        icache.createImageCache();
    }

    /**
     * 这里的Integer参数对应AsyncTask中的第二个参数
     * 在doInBackground方法当中，，每次调用publishProgress方法都会触发onProgressUpdate执行
     * onProgressUpdate是在UI线程中执行，所有可以对UI空间进行操作
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        mProgress = values[0];
        if (mProgressListener != null) {
            mProgressListener.onProgress(values[0]);
        }
    }

    // 该方法用于取消执行中的任务时修改UI
    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    public Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

}
