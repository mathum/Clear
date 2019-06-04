package com.clearcrane.databean;

public class UpDate {
	private String model;
	private String version;
	private String apkurl;
	private String apkname;
	private String md5;

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getApkurl() {
		return apkurl;
	}

	public void setApkurl(String apkurl) {
		this.apkurl = apkurl;
	}

	public String getApkname() {
		return apkname;
	}

	public void setApkname(String apkname) {
		this.apkname = apkname;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	@Override
	public String toString() {
		return "UpData [model=" + model + ", version=" + version + ", apkurl=" + apkurl + ", apkname=" + apkname
				+ ", md5=" + md5 + "]";
	}

}
