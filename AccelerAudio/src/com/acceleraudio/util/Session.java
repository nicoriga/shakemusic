package com.acceleraudio.util;



public class Session{
	private long id;
	private String name, modified_date, image;
	private int n_sample;
	private boolean selected;
	
	public Session(long id, String name, String modified_date, String image, int n_sample){
		this.id = id;
		this.name = name;
		this.modified_date = modified_date;
		this.n_sample = n_sample;
		selected = false;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setModifiedDate(String modified_date){
		this.modified_date = modified_date;
	}
	
	public void setImage(String image){
		this.image = image;
	}
	
	public void selected(boolean b){
		this.selected = b;
	}
	
}