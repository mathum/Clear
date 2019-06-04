package com.clearcrane.interfaces;

import com.clearcrane.apkupdate.UpdateManagerService;

public interface IMyBinder {
	void invokeMethodInMyService();

	UpdateManagerService getService();
}
