package com.clearcrane.util;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.List;

public class XMLBeanAdapter implements BeanAdapter {

	@Override
	public Object getBean(Object from, Class<?> to) {
		if (to == BeanXMLUpdateInfo.class) {
			return getXMLUpdateInfo((InputStream) from);
		} else {
			return null;
		}
	}

	@Override
	public List<?> getBeanList(Object from, Class<?> to) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private BeanXMLUpdateInfo getXMLUpdateInfo(InputStream is) {
		BeanXMLUpdateInfo info = new BeanXMLUpdateInfo();
		try {
			XmlPullParser xmlPullParser = Xml.newPullParser();
			xmlPullParser.setInput(is, "utf-8");
			int type = xmlPullParser.getEventType();
			while (type != XmlPullParser.END_DOCUMENT) {
				switch (type) {
				case XmlPullParser.START_TAG:
					if (xmlPullParser.getName().equals("version")) {
						info.setVersionCode(Integer.valueOf(xmlPullParser
								.nextText()));
					} else if (xmlPullParser.getName().equals("description")) {
						info.setDescription(xmlPullParser.nextText());
					} else if (xmlPullParser.getName().equals("apkurl")) {
						info.setUrl(xmlPullParser.nextText());
					} else if (xmlPullParser.getName().equals("md5")) {
						info.setMd5(xmlPullParser.nextText());
					} else if (xmlPullParser.getName().equals("apkname")) {
						info.setApkName(xmlPullParser.nextText());
					} else if (xmlPullParser.getName().equals("acl")) {
                        String acl = xmlPullParser.nextText();
                        if (acl != null && (acl.equalsIgnoreCase("y")
                                || acl.equalsIgnoreCase("yes")
                                || acl.equalsIgnoreCase("1"))) {
                            info.setAcl(true);
                        } else {
                            info.setAcl(false);
                        }
                    } else if (xmlPullParser.getName().equals("mac")) {
                        info.add(xmlPullParser.nextText());
                    }
					break;

				default:
					break;
				}
				type = xmlPullParser.next();
			}
		} catch (Exception e) {
			Log.e("XMLBeanAdapter", "fail to parse xml:[" + Log.getStackTraceString(e) + "]");
			info.setVersionCode(0);
		}
		return info;
	}
}
