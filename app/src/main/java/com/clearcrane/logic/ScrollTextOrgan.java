package com.clearcrane.logic;

public class ScrollTextOrgan extends Organism{
	
	private String textContent;
	private String textTitle;
	private String color="";
	private String interval=""; 
	private String location =""; 
	private String font_family = "";
	private String direction = "";

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}
	
	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getInterval() {
		return interval;
	}

	public void setInterval(String interval) {
		this.interval = interval;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getFont_family() {
		return font_family;
	}

	public void setFont_family(String font_family) {
		this.font_family = font_family;
	}

	public void setTextContent(String textContent) {
		this.textContent = textContent;
	}

	public void setTextTitle(String textTitle) {
		this.textTitle = textTitle;
	}

	public String getTextContent() {
		return textContent;
	}

	public String getTextTitle() {
		return textTitle;
	}


	public ScrollTextOrgan(String content,String title){
		this.textContent = content;
		this.textTitle = title;
	}
	
	

}
