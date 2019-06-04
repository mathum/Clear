package com.clearcrane.logic.version;

public class VersionFatory {

	
	public static PrisonBaseVersion createVersion(String versionName){
		PrisonBaseVersion prisonBaseVersion = null;
		try {
			prisonBaseVersion = (PrisonBaseVersion) Class.forName(versionName).newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return prisonBaseVersion;
	}
}
