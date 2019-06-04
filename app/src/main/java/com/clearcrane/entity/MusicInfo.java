package com.clearcrane.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * 歌曲实体类
 * 
 * @author SlientLeaves 2016年8月25日 下午1:50:35
 */
public class MusicInfo implements Serializable, Parcelable {

	 private static final long serialVersionUID = 3920914447197516318L;

	private int id;
	private String seq;
	private String Album;
	private String AlbumEng;
	private String Duration;//必须
	private String Music_size;
	private String Name;
	private String NameEng;
	private String Pic_size;
	private String Picurl;
	private String Picurl_abs_path;
	private String PlayURL;
	private String PlayURL_abs_path;
	private String Singer;//必须
	private String SingerEng;
	private String Summary;
	private String SummaryEng;

	private String AudioName;
	private String AudioPic_abs_path;
	private String AudioPic;
	private String AudioPath;
	private int AudioNum;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAudioName() {
		return AudioName;
	}

	public void setAudioName(String audioName) {
		AudioName = audioName;
	}

	public String getAudioPic_abs_path() {
		return AudioPic_abs_path;
	}

	public void setAudioPic_abs_path(String audioPic_abs_path) {
		AudioPic_abs_path = audioPic_abs_path;
	}

	public String getAudioPic() {
		return AudioPic;
	}

	public void setAudioPic(String audioPic) {
		AudioPic = audioPic;
	}

	public String getAudioPath() {
		return AudioPath;
	}

	public void setAudioPath(String audioPath) {
		AudioPath = audioPath;
	}

	public int getAudioNum() {
		return AudioNum;
	}

	public void setAudioNum(int audioNum) {
		AudioNum = audioNum;
	}

	
	public String getSeq() {
		return seq;
	}

	public void setSeq(String seq) {
		this.seq = seq;
	}

	public String getAlbum() {
		return Album;
	}

	public void setAlbum(String album) {
		Album = album;
	}

	public String getAlbumEng() {
		return AlbumEng;
	}

	public void setAlbumEng(String albumEng) {
		AlbumEng = albumEng;
	}

	public String getDuration() {
		return Duration;
	}

	public void setDuration(String duration) {
		Duration = duration;
	}

	public String getMusic_size() {
		return Music_size;
	}

	public void setMusic_size(String music_size) {
		Music_size = music_size;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getNameEng() {
		return NameEng;
	}

	public void setNameEng(String nameEng) {
		NameEng = nameEng;
	}

	public String getPic_size() {
		return Pic_size;
	}

	public void setPic_size(String pic_size) {
		Pic_size = pic_size;
	}

	public String getPicurl() {
		return Picurl;
	}

	public void setPicurl(String picurl) {
		Picurl = picurl;
	}

	public String getPicurl_abs_path() {
		return Picurl_abs_path;
	}

	public void setPicurl_abs_path(String picurl_abs_path) {
		Picurl_abs_path = picurl_abs_path;
	}

	public String getPlayURL() {
		return PlayURL;
	}

	public void setPlayURL(String playURL) {
		PlayURL = playURL;
	}

	public String getPlayURL_abs_path() {
		return PlayURL_abs_path;
	}

	public void setPlayURL_abs_path(String playURL_abs_path) {
		PlayURL_abs_path = playURL_abs_path;
	}

	public String getSinger() {
		return Singer;
	}

	public void setSinger(String singer) {
		Singer = singer;
	}

	public String getSingerEng() {
		return SingerEng;
	}

	public void setSingerEng(String singerEng) {
		SingerEng = singerEng;
	}

	public String getSummary() {
		return Summary;
	}

	public void setSummary(String summary) {
		Summary = summary;
	}

	public String getSummaryEng() {
		return SummaryEng;
	}

	public void setSummaryEng(String summaryEng) {
		SummaryEng = summaryEng;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.id);
		dest.writeString(this.seq);
		dest.writeString(this.Album);
		dest.writeString(this.AlbumEng);
		dest.writeString(this.Duration);
		dest.writeString(this.Music_size);
		dest.writeString(this.Name);
		dest.writeString(this.NameEng);
		dest.writeString(this.Pic_size);
		dest.writeString(this.Picurl);
		dest.writeString(this.Picurl_abs_path);
		dest.writeString(this.PlayURL);
		dest.writeString(this.PlayURL_abs_path);
		dest.writeString(this.Singer);
		dest.writeString(this.SingerEng);
		dest.writeString(this.Summary);
		dest.writeString(this.SummaryEng);
	}

	public static final Creator<MusicInfo> CREATOR = new Creator<MusicInfo>() {

		@Override
		public MusicInfo[] newArray(int size) {
			return new MusicInfo[size];
		}

		@Override
		public MusicInfo createFromParcel(Parcel source) {

			MusicInfo musicInfo = new MusicInfo();
			musicInfo.setId(source.readInt());
			musicInfo.setSeq(source.readString());
			musicInfo.setAlbum(source.readString());
			musicInfo.setAlbumEng(source.readString());
			musicInfo.setDuration(source.readString());
			musicInfo.setMusic_size(source.readString());
			musicInfo.setName(source.readString());
			musicInfo.setNameEng(source.readString());
			musicInfo.setPic_size(source.readString());
			musicInfo.setPicurl(source.readString());
			musicInfo.setPicurl_abs_path(source.readString());
			musicInfo.setPlayURL(source.readString());
			musicInfo.setPlayURL_abs_path(source.readString());
			musicInfo.setSinger(source.readString());
			musicInfo.setSingerEng(source.readString());
			musicInfo.setSummary(source.readString());
			musicInfo.setSummaryEng(source.readString());

			return musicInfo;
		}
	};
}
