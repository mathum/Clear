 package com.clearcrane.logic.version;

 import android.content.Context;
 import android.content.SharedPreferences.Editor;
 import android.util.Log;

 import com.clearcrane.constant.ClearConstant;
 import com.clearcrane.util.ClearConfig;

 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;

 public class AccessTimeVersion extends PrisonBaseVersion {



     @Override
     public void init(int remoteVersion, Context ctx,
             VersionChangeListener listener) {
         // TODO Auto-generated method stub
         super.init(remoteVersion, ctx, listener);
         versionUrl  = ClearConfig.SERVER_URI + ClearConstant.STR_BACKEND_PORT + ClearConstant.URL_ACCESS_TIME_SUFFIX;
     }

     @Override
     protected void initSharedPreferences() {
         // TODO Auto-generated method stub
         mSharedPreferences = mContext.getSharedPreferences(ClearConstant.STR_ACCESS_TIME, Context.MODE_PRIVATE);
     }

     @Override
     protected void parseReturnJson(String result) {
         // TODO Auto-generated method stub
         Log.e("xb", "zxb:"+result);
         JSONTokener jsonTokener = new JSONTokener(result);
         try {
             JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
             if(!jsonObject.getString(ClearConstant.STR_RESCODE).equals("200")){
                 return;
             }
             int remoteVersion = jsonObject.getInt(ClearConstant.STR_NEWEST_VERSION);
 //			String start_time ="";
 //			String end_time = "";
 //			int count = 0;
 //			if(remoteVersion == -1){
 //				return;
 //			}
 //			else{
 //				/*
 //				 * TODO,FIXME
 //				 * store jsonArray
 //				 * use a count simplely
 //				 */
 //				JSONArray jsonArray = jsonObject.getJSONArray(ClearConstant.STR_ACCESS_TIME);
 //				count = jsonArray.length();
 //				Log.i(TAG,"access array length " + count);
 //
 //
 //			/*
 //			 * TODO,FIXME
 //			 * why here is a array!!@!@!
 //			 */
 //				for(int i = 0; i < count; i++){
 //					JSONObject atObj = (JSONObject) jsonArray.opt(i);
 //					start_time = atObj.getString("start");
 //					end_time = atObj.getString("end");
 //					editor.putString(ClearConstant.STR_START_TIME + i, start_time);
 //					editor.putString(ClearConstant.STR_END_TIME + i, end_time);
 //				}
 //			}
 //
 //			editor.putInt(ClearConstant.STR_COUNT, count);
             Editor editor = mSharedPreferences.edit();
             editor.putInt(ClearConstant.STR_NEWEST_VERSION,remoteVersion);
 //			editor.putString(ClearConstant.STR_START_TIME, start_time);
 //			editor.putString(ClearConstant.STR_END_TIME, end_time);
             editor.putString(ClearConstant.STR_ACCESSTIME_CONTENT, result);
             editor.commit();
             notifyVersionChange();

         }catch (JSONException e) {
             // TODO Auto-generated catch block
             Log.e(TAG,"parseReturnJson error!");
         }
     }

 }
