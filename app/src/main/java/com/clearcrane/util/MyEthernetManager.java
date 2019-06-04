package com.clearcrane.util;

import android.content.Context;
import android.net.DhcpInfo;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/*
 * created by Winter on 2018-03-22
 * only for jiulion stb
 * to change Dhcp to manual
 * may cause unexpected error
 * 慎用！！！！
 */
public class MyEthernetManager {

	private final String TAG = "MyEthernetManager";
	
	//=========反射用==========================
	//九联盒子EthernetManager类位置
	private final String ETHERNETMANAGERCLASS = "android.net.ethernet.EthernetManager";
	//两个模式DHCP和手动，EthernetManager的两个常量
	public final String ETHERNET_MODE_DHCP = "ETHERNET_CONNECT_MODE_DHCP";
	public final String ETHERNET_MODE_MANUAL = "ETHERNET_CONNECT_MODE_MANUAL";
	private final String GET_ETHERNET_MODE_METHOD = "getEthernetMode";
	private final String SET_ETHERNET_MODE_METHOD = "setEthernetMode";
	//获取setEthernetMode需要的第二个参数dhcpInfo,DhcpInfo为开放的，在android.net下面有
	private final String GET_DHCP_INFO =  "getDhcpInfo";
	
	
	private Method getEthernetModeMethod;
	private Method getDhcpInfoMethod;
	private Method setEthernetModeMethod;
	private String ethernetModeDhcp, ethernetModeManaul;
	private Class<?> ethernetManagerClass;
	private Context mContext;
	private Object mEthernetManager;
	private DhcpInfo mDhcpInfo = null;
	
	public MyEthernetManager(Context context){
		this.mContext = context;
		init();
	}
	
	//TODO,FIXME
	//this method not only check if it is Dhcp
	//but also when it is Dhcp ,init mDhcpInfo
	//get EthernetMode 
	public boolean isEthernetModeDhcp(){
		if(mEthernetManager == null){
			Log.e(TAG, "init error mEthernetManager is null");
			return false;
		}
		try {
			if(getEthernetModeMethod.invoke(mEthernetManager, null).equals(ethernetModeDhcp)){
				getEthernetIpInfo();
				return true;
			}
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "isEthernetModeDhcp error when invoke ");
		}
		return false;
	}
	
	private void getEthernetIpInfo(){
		if(mEthernetManager == null){
			Log.e(TAG, "init error mEthernetManager is null");
			return;
		}
		try {
			mDhcpInfo = (DhcpInfo) getDhcpInfoMethod.invoke(mEthernetManager, null);
			Log.e(TAG, "getEthernetInfo " + mDhcpInfo.toString());
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "getEthernetIpInfo error when invoke ");
			e.printStackTrace();
		}
	}
	
	//TODO,FIXME
	//call this method when changing ethernetMode dhcp to manual
	//this method must be called when the mDhcpInfo is inited
	//and mDhcpInfo is legal
	public boolean changeDhcpToManual(){
		if(mEthernetManager == null){
			Log.e(TAG, "init error mEthernetManager is null");
			return false;
		}

		if(mDhcpInfo == null)
			return false;
		
		try {
			setEthernetModeMethod.invoke(mEthernetManager, ethernetModeManaul, mDhcpInfo);
			return true;
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "setEhternetModeManual error when invoke ");
		}
		return false;
	}
	
	public boolean changeToDhcp(){
		if(mEthernetManager == null){
			Log.e(TAG, "init error mEthernetManager is null");
			return false;
		}

		
		try {
			setEthernetModeMethod.invoke(mEthernetManager, ethernetModeDhcp, mDhcpInfo);
			return true;
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "setEhternetModeManual error when invoke ");
		}
		return false;
	}
	
	
	//init reflect class and methods
	private void init(){
		try {
			ethernetManagerClass = Class.forName(ETHERNETMANAGERCLASS);
			ethernetModeDhcp = (String) ethernetManagerClass.getField(ETHERNET_MODE_DHCP).get(null);
			ethernetModeManaul = (String) ethernetManagerClass.getField(ETHERNET_MODE_MANUAL).get(null);
			getEthernetModeMethod = ethernetManagerClass.getDeclaredMethod(GET_ETHERNET_MODE_METHOD);
			getDhcpInfoMethod = ethernetManagerClass.getDeclaredMethod(GET_DHCP_INFO);
			setEthernetModeMethod = ethernetManagerClass.getDeclaredMethod(SET_ETHERNET_MODE_METHOD, String.class, DhcpInfo.class);
			//mEthernetManager = mContext.getSystemService(Context.ETHERNET_SERVICE);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
