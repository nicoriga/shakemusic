package com.acceleraudio.util;


public class RecordedSession{
	private long id;
	private String name, modified_date, image;
	private int n_sample;
	private boolean selected;
	
	/**
	 * Sessione di registrazione
	 * 
	 * @param id identificativo sessione
	 * @param name nome sessione
	 * @param modified_date data ultima modifica
	 * @param image immagine codificata in stringa
	 * @param n_sample numero campioni totali
	 */
	public RecordedSession(long id, String name, String modified_date, String image, int n_sample){
		this.id = id;
		this.name = name;
		this.modified_date = modified_date;
		this.n_sample = n_sample;
		selected = false;
	}
	
	public long getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	public String getImage(){
		return image;
	}
	
	public String getModifiedDate(){
		return modified_date;
	}
	
	public int getNumSample(){
		return n_sample;
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
	
	/**
	 * serve per impostare se la sessione è selezionata
	 * 
	 * @param status imposta true per selezionato, false non selezionato
	 */
	public void select(boolean status){
		this.selected = status;
	}
	
	public boolean isSelected(){
		return selected;
	}
	
}