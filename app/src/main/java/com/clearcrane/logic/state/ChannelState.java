package com.clearcrane.logic.state;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.logic.ProgramObject;
import com.clearcrane.logic.ProgramOrgan;
import com.clearcrane.logic.view.ProgramView;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.view.VoDViewManager;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import org.apache.http.entity.StringEntity;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ChannelState extends PrisonBaseModeState {

    private String mChannelInfoUrl;
    //private String channelUrl1 = "/nativevod/now/Channel/channel_";
    private String channelUrl1 = "/backend/GetPlayList/";
    private String channelUrl2 = ".json";
    private int mVersion;
    private int moviePosition;
    private ProgramView pv;
    //储存计划列表
    private List<ProgramObject> mProgramObjects;


    public ChannelState() {
        this.mStateCode = ClearConstant.CODE_CHANNEL_STATE;
    }


    @Override
    protected void updateVersionInfo() {
        // TODO Auto-generated method stub
        reinit();
    }

    @Override
    public void initPrisonOrganism() {
        // TODO Auto-generated method stub
        mProgramObjects = new ArrayList<ProgramObject>();

//		ProgramOrgan organ = new ProgramOrgan(null);
//		List<ProgramOrgan> list = new ArrayList<>();
//		list.add(organ);
//		mPrisonOrganism = new ProgramObject(null, list);
//		mPrisonOrganism.init("ddd");
    }

    @Override
    public void initStateParams() {
        // TODO Auto-generated method stub
        mVersion = mPreference.getInt(ClearConstant.STR_NEWEST_VERSION, -1);
        if (-1 == mVersion)
            return;
        String channel_id = mPreference.getString(ClearConstant.STR_CHANNEL_ID, "-1");
//        mChannelInfoUrl = ClearConfig.SERVER_URI + channelUrl1 + channel_id + channelUrl2;
        mChannelInfoUrl = ClearConfig.SERVER_URI.concat(":8000").concat(channelUrl1);

        if (mVersion == -1)
            return;
        //获取实际的显示数据
        getChannelInfo();
        Log.e("xb", "initprisonOrganism mChannelInfoUrl " + mChannelInfoUrl);
    }

    private void getChannelInfo() {
        new Thread(getRemoteVersion).start();
    }


    @Override
    public void init(Context context) {
        // TODO Auto-generated method stub
        this.init(context, null);
    }

    @Override
    public void init(Context context, Handler handler) {
        // TODO Auto-generated method stub
        super.init(context, handler);
        mPreference = mContext.getSharedPreferences(ClearConstant.STR_CHANNEL, Context.MODE_PRIVATE);
        initStateParams();
        initPrisonOrganism();
    }


    protected Runnable getRemoteVersion = new Runnable() {

        @Override
        public void run() {
            HttpUtils httpUtils = new HttpUtils();
            httpUtils.configCurrentHttpCacheExpiry(0);
            Log.e("xb", "mChannelInfoUrl" + mChannelInfoUrl);

            //如果是get请求，注释try～catch所有内容
            //httpUtils.send(HttpMethod.GET, mChannelInfoUrl,requestCallBack);
            //post请求
            try {
                RequestParams requestParams = new RequestParams();
                String params = "{\"mac\":\"" + ClearConfig.getMac() + "\"}";
                requestParams.setBodyEntity(new StringEntity(params, "utf-8"));
                httpUtils.send(HttpMethod.POST, mChannelInfoUrl, requestParams, requestCallBack);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

    RequestCallBack<String> requestCallBack = new RequestCallBack<String>() {

        @Override
        public void onSuccess(ResponseInfo<String> arg0) {
            // TODO Auto-generated method stub
            Log.i(TAG, "onSuccess =  " + arg0.result);
            parseReturnJson(arg0.result);

        }

        @Override
        public void onFailure(HttpException arg0, String arg1) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onFailure " + arg1);
            Editor editor = mPreference.edit();
            editor.putInt(ClearConstant.STR_NEWEST_VERSION, -1);
            editor.commit();
        }
    };


    private void parseReturnJson(String result) {
        Log.e("zxb", "什么鬼东西：" + result);
        JSONTokener jsonTokener = new JSONTokener(result);
        try {
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            this.curVersion = jsonObject.getInt("version_num");
            JSONArray jsonArray = jsonObject.getJSONArray("programs");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObjProgram = (JSONObject) jsonArray.opt(i);
                ProgramObject programObject = new ProgramObject(jsonObjProgram, i);
                mProgramObjects.add(programObject);
            }
        } catch (Exception e) {
            Log.e(TAG, "parseReturnJson error json :" + result);
            Editor editor = mPreference.edit();
            editor.putInt(ClearConstant.STR_NEWEST_VERSION, -1);
            editor.commit();
        }

    }

    @Override
    public boolean isReady() {
        if (mVersion == -1)
            return false;
        /*
         * first check is there any program ready
         * second check which is the highest program
         */
        for (int i = 0; i < mProgramObjects.size(); i++) {
            ProgramObject po = mProgramObjects.get(i);
            Log.e("zxb", "poIsAwake:" + po.isAwake());
            if (po.isAwake()) {
                mPrisonOrganism = po;
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.clearcrane.logic.state.PrisonBaseModeState#startPlay()
     * must be called in UI Thread!!!!
     */
    @Override
    public void startPlay() {
        VoDViewManager.getInstance().setActivityMode(mStateCode);
        pv = new ProgramView();
        pv.init(mContext, null);
        //初始化时间段
        pv.setLifeTime(((ProgramOrgan) mPrisonOrganism.getWorkOrgan()).getDayStartTime(), ((ProgramOrgan) mPrisonOrganism.getWorkOrgan()).getDayEndTime());
        //初始化布局
        pv.initWidgets(((ProgramObject) mPrisonOrganism).getmRegionList());
        //初始化资源
        pv.setWidgetResource(((ProgramOrgan) mPrisonOrganism.getWorkOrgan()).getmResourceList());
        Message msg = mHandler.obtainMessage(ClearConstant.MSG_START_CHANNEL_LIST);
        msg.obj = pv;
        msg.sendToTarget();

        super.startPlay();
    }

    @Override
    public void stopPlay() {
        // TODO Auto-generated method stub
        VoDViewManager.getInstance().setActivityMode(ClearConstant.CODE_VOD_STATE);
        Message msg = mHandler.obtainMessage(ClearConstant.MSG_STOP_CHANNEL_LIST);
        msg.obj = pv;
        msg.sendToTarget();
        super.stopPlay();
    }


}
