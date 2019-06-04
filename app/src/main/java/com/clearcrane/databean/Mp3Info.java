package com.clearcrane.databean;

import java.io.Serializable;

/**
 * 2013/5/7 mp3ʵ����
 * 
 * @author wwj
 * 
 */
public class Mp3Info implements Serializable{
	// private long id; // ����ID 3
	// private String title; // �������� 0
	// private String album; // ר�� 7
	// private long albumId;//ר��ID 6
	// private String displayName; //��ʾ���� 4
	// private String artist; // �������� 2
	// private long duration; // ����ʱ�� 1
	// private long size; // ������С 8
	// private String url; // ����·�� 5
	// private String lrcTitle; // �������
	// private String lrcSize; // ��ʴ�С
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

	public Mp3Info() {
		super();
	}

	@Override
	public String toString() {
		return "Mp3Info [name=" + name + ", nameEng=" + nameEng + ", picURL=" + picURL + ", playURL=" + playURL
				+ ", singer=" + singer + ", singerEng=" + singerEng + ", album=" + album + ", albumEng=" + albumEng
				+ ", summary=" + summary + ", summaryEng=" + summaryEng + ", duration=" + duration + ", isPlay="
				+ isPlay + "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameEng() {
		return nameEng;
	}

	public void setNameEng(String nameEng) {
		this.nameEng = nameEng;
	}

	public String getPicURL() {
		return picURL;
	}

	public void setPicURL(String picURL) {
		this.picURL = picURL;
	}

	public String getPlayURL() {
		return playURL;
	}

	public void setPlayURL(String playURL) {
		this.playURL = playURL;
	}

	public String getSinger() {
		return singer;
	}

	public void setSinger(String singer) {
		this.singer = singer;
	}

	public String getSingerEng() {
		return singerEng;
	}

	public void setSingerEng(String singerEng) {
		this.singerEng = singerEng;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getAlbumEng() {
		return albumEng;
	}

	public void setAlbumEng(String albumEng) {
		this.albumEng = albumEng;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getSummaryEng() {
		return summaryEng;
	}

	public void setSummaryEng(String summaryEng) {
		this.summaryEng = summaryEng;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public boolean isPlay() {
		return isPlay;
	}

	public void setPlay(boolean isPlay) {
		this.isPlay = isPlay;
	}

	// @Override
	// public String toString() {
	// return "Mp3Info [id=" + id + ", title=" + title + ", album=" + album
	// + ", albumId=" + albumId + ", displayName=" + displayName
	// + ", artist=" + artist + ", duration=" + duration + ", size="
	// + size + ", url=" + url + ", lrcTitle=" + lrcTitle
	// + ", lrcSize=" + lrcSize + "]";
	// }

}