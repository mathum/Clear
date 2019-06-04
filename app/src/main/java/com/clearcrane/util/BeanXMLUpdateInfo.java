package com.clearcrane.util;

import java.util.HashMap;

public class BeanXMLUpdateInfo {
	private int versionCode;
	private String description;
	private String url;
	private String md5;
	private String apkName;
	private boolean acl;
    private HashMap<String, String> hashMap;

    public BeanXMLUpdateInfo() {
        acl = false;
        hashMap = new HashMap<String, String>();
    }
    
	public int getVersionCode() {
		return versionCode;
	}
	
	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public String getApkName() {
		return apkName;
	}

	public void setApkName(String apkName) {
		this.apkName = apkName;
	}

    public boolean isAcl() {
        return acl;
    }

    public void setAcl(boolean acl) {
        this.acl = acl;
    }

    public void add(String key) {
        if (hashMap == null) {
            return;
        }
        if (key == null || key.trim().length() <= 0) {
            return;
        }
        hashMap.put(key, "yes");
    }
    
    public boolean isInSet(String key) {
        if (hashMap == null) {
            return false;
        }
        if (hashMap.get(key) != null) {
            return true;
        }
        return false;
    }
}
