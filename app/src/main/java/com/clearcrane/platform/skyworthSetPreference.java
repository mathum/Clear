package com.clearcrane.platform;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.TextView;

import com.clearcrane.constant.clearKey;
import com.clearcrane.vod.R;

import java.util.ArrayList;

public class skyworthSetPreference extends PreferenceActivity implements OnPreferenceClickListener,OnPreferenceChangeListener{

	//private final int[] ipaddrIds = {R.id.et_ipaddr1, R.id.et_ipaddr2, R.id.et_ipaddr3, R.id.et_ipaddr4};
	//private final int[] netmaskIds = {R.id.et_netmask1, R.id.et_netmask2, R.id.et_netmask3, R.id.et_netmask4};
	//private final int[] gatewayIds = {R.id.et_gateway1, R.id.et_gateway2, R.id.et_gateway3, R.id.et_gateway4};
	//private final int[] dns1Ids = {R.id.et_dns11, R.id.et_dns12, R.id.et_dns13, R.id.et_dns14};
	
	private static String TAG = "skyworthSetPreference";            
    private CheckBoxPreference subtitlePreference;       //滚动字幕  
    private PreferenceScreen subtitleSettingPreference;//滚动字幕设置
    private EditTextPreference mainPagePreference;       //设置主页   
    private ListPreference mainPageSelectPreference;     //选择主页地址 
    private EditTextPreference backupMainPagePreference; //设置备用主页
    private ListPreference backupMainPageSelectPreference; //选择备选主页地址
    //private CheckBoxPreference loadInfoPreference; 		//视频加载信息提示
    private CheckBoxPreference defaultVolumePrefence;			//开机默认音量设置
    private Context context;
    private TextView tv;
    public RadioButton rbDhcp,rbStatic;
    private static final int LEFT2RIGHT = 1;
	private static final int RTGHT2LEFT = 2;
	private int mEffectType = 2;
	
	private boolean otherSetting = false;
	
	private ArrayList<CharSequence> acsValues = new ArrayList<CharSequence>();
	private ArrayList<CharSequence> backupAcsValues = new ArrayList<CharSequence>();
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.my_set_preference);
		context = this;
		
        PreferenceManager.setDefaultValues(context, R.xml.my_set_preference, false);
       
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	@Override
	protected void onStart() {
		super.onStart();
	}
	
	
   
    //控件点击事件触发   
    @Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,  
            Preference preference) {  
        Log.i(TAG, "onPreferenceTreeClick----->"+preference.getKey());  
        // 对控件进行操作   
        
        //其他设置
        if(preference.getKey() != null && preference.getKey().equals("otherSettings"))
        {
        	otherSetting = true;
        	final AlertDialog dialog2 = new  AlertDialog.Builder(skyworthSetPreference.this).setTitle("提示" )
                	.setInverseBackgroundForced(true)
        			.setMessage("请按下设置键")
                	.create();
					dialog2.show();
		}
        
        
        //跳转到工厂菜单
        else if(preference.getKey() != null && preference.getKey().equals("skyfactory"))
        {
        	PackageManager packageManager1 = getPackageManager();
			Intent intent1 = null;
			intent1 = packageManager1
					.getLaunchIntentForPackage("com.skyworth.tvos.factory");
			startActivity(intent1);
        }
        //酒店模式复制菜单
        else if(preference.getKey() != null && preference.getKey().equals("skyhotel"))
        {
        	PackageManager packageManager1 = getPackageManager();
			Intent intent1 = null;
			intent1 = packageManager1
					.getLaunchIntentForPackage("com.skyworth.hotel");
			startActivity(intent1);
        }
        	
        else if(preference.getKey() != null && preference.getKey().equals("autosearch"))
        {
    		final Window window=this.getWindow();
    		WindowManager.LayoutParams wl = window.getAttributes();
    		wl.flags=WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
    		wl.alpha=0.0f;//这句就是设置窗口里控件的透明度的．０.０全透明．１.０不透明．          
    		window.setAttributes(wl);
    		LayoutInflater inflater = LayoutInflater.from(this);
        	View view = inflater.inflate(R.layout.auto, null);
        	tv = (TextView)view.findViewById(R.id.textView);
        	tv.setText("搜索准备中...");
    		final AutoScan as = new AutoScan(this,tv);

    		new  AlertDialog.Builder(skyworthSetPreference.this).setTitle("频道搜索" )
        	.setView(view).setPositiveButton("退出搜索" ,  
        		new DialogInterface.OnClickListener() {  
                @Override  
                public void onClick(DialogInterface dialog,  
                        int which) {
                	if(!as.isFinished())
                		as.requestStop();
            		WindowManager.LayoutParams wl = window.getAttributes();
            		wl.flags=WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            		wl.alpha=1.0f;//这句就是设置窗口里控件的透明度的．０.０全透明．１.０不透明．
            		window.setAttributes(wl);
                }  
            } ).show();
        	as.start();
        }
        return false;  
    }  

	@Override
	public boolean onPreferenceClick(Preference preference) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onPreferenceClick----->"+String.valueOf(preference.getKey()));  
        // 对控件进行操作   

		return false;
	}
	
 
	/**
	 * 主要是负责按键屏蔽，return true就是屏蔽；false就是交给电视机处理
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		int valKey = 0;
		KeyEvent newEvent = null;
		
		Log.i("Key Press", "keyCode:"+ keyCode);
		mEffectType = skyworthSetPreference.RTGHT2LEFT;
		
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_VOLUME_UP:
		case KeyEvent.KEYCODE_VOLUME_MUTE:
		case KeyEvent.KEYCODE_MENU:
		case KeyEvent.KEYCODE_HOME:
		case clearKey.SKYWORTH_LOCAL_MEDIA:
		case clearKey.SKYWORTH_TIME_SPOT:
		case clearKey.SKYWORTH_SEARCH:
		case KeyEvent.KEYCODE_TV_INPUT:
			return true;
		case KeyEvent.KEYCODE_PROG_RED:
			
			break;
		case KeyEvent.KEYCODE_PROG_GREEN:
			PackageManager packageManager2 = getPackageManager();
			Intent intent2 = null;
			intent2 = packageManager2
					.getLaunchIntentForPackage("com.example.clearreinstall");
			startActivity(intent2);
			break;
		case KeyEvent.KEYCODE_PROG_YELLOW:
			
			break;
		case KeyEvent.KEYCODE_PROG_BLUE:
			PackageManager packageManager1 = getPackageManager();
			Intent intent1 = null;
			intent1 = packageManager1
					.getLaunchIntentForPackage("com.skyworth.myapps");
			startActivity(intent1);
			break;
		case clearKey.SKYWORTH_SETTING:
			if(otherSetting)
				return super.onKeyDown(keyCode, event);
			else
				return true;
		case KeyEvent.KEYCODE_BACK:
			break;
		default:
			break;	
		}
		if (valKey != 0) {
			newEvent = new KeyEvent(KeyEvent.ACTION_DOWN, valKey);
			super.onKeyDown(valKey, newEvent);
			return true;
		}
		super.onKeyDown(keyCode, event);
		return true;
	}

	@Override
	public boolean onPreferenceChange(Preference arg0, Object arg1) {
		// TODO Auto-generated method stub
		return false;
	}
}


