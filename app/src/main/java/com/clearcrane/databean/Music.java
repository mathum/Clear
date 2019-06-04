package com.clearcrane.databean;

public class Music extends Mp3Info{
	public String name;
	public String nameEng;
	public String picURL;
	public String playURL;
	public String singer;
	public String singerEng;
	public String album;
	public String albumEng;
	public String summary;
	public String summaryEng;
	public int duration; // seconds
	public boolean isPlay;
	
	public Music() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "Music [name=" + name + ", nameEng=" + nameEng + ", picURL=" + picURL + ", playURL=" + playURL
				+ ", singer=" + singer + ", singerEng=" + singerEng + ", album=" + album + ", albumEng=" + albumEng
				+ ", summary=" + summary + ", summaryEng=" + summaryEng + ", duration=" + duration + ", isPlay="
				+ isPlay + "]";
	}
	
	
}
