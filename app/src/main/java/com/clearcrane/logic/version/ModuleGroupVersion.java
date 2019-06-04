package com.clearcrane.logic.version;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.util.ClearConfig;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ModuleGroupVersion extends PrisonBaseVersion {

	
	
	@Override
	public void init(int remoteVersion, Context ctx,
			VersionChangeListener listener) {
		// TODO Auto-generated method stub
		super.init(remoteVersion, ctx, listener);
		this.versionUrl = ClearConfig.SERVER_URI + ClearConstant.STR_BACKEND_PORT + ClearConstant.URL_MODULE_GROUP_SUFFIX;
	}

	@Override
	protected void initSharedPreferences() {
		// TODO Auto-generated method stub
		mSharedPreferences = mContext.getSharedPreferences(ClearConstant.STR_MODULE_GROUP, Context.MODE_PRIVATE);
	}

	@Override
	protected void parseReturnJson(String result) {
		// TODO Auto-generated method stub
		Log.e("zxb","parseReturnJson " + result);
		JSONTokener jsonTokener = new JSONTokener(result);
		try {
			JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
			int remoteVersion = jsonObject.getInt(ClearConstant.STR_NEWEST_VERSION);
			int module_group_id = -1;
			Editor editor = mSharedPreferences.edit();
			module_group_id = jsonObject.getInt(ClearConstant.STR_MODULE_GROUP_ID);
			editor.putInt(ClearConstant.STR_MODULE_GROUP_ID, module_group_id);
			editor.putInt(ClearConstant.STR_NEWEST_VERSION, remoteVersion);
			editor.commit();
//			notifyVersionChange();
		} catch (JSONException e) {
			Log.e(TAG,"parseReturnJson error!");
		}
	}

}
