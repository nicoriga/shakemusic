package com.acceleraudio.util;

/**
 * @author Nicola Rigato
 * @author Luca Del Salvador
 * @author Marco Tessari
 * @author Gruppo: Malunix
 *
 * classe che gestisce la sessione
 */

public class RecordedSession{
	private long id;
	private String name, modified_date, image;
	private int n_sample; // numero campioni
	private boolean selected;
	
	/**
	 * Sessione di registrazione
	 * 
	 * @param id identificativo sessione
	 * @param name nome sessione
	 * @param modified_date data ultima modifica
	 * @param image immagine codificata in base64
	 * @param n_sample numero campioni totali
	 */
	public RecordedSession(long id, String name, String modified_date, String image, int n_sample){
		this.id = id;
		this.name = name;
		this.modified_date = modified_date;
		this.image = image;
		this.n_sample = n_sample;
		selected = false;
	}
	
	/**
	 * @return id della sessione
	 */
	public long getId(){
		return id;
	}
	
	/**
	 * @return nome della sessione
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * @return immagine in formato stringa
	 */
	public String getImage(){
		return image;
	}
	
	/**
	 * @return la data di modifica
	 */
	public String getModifiedDate(){
		return modified_date;
	}
	
	/**
	 * @return il numero di campioni
	 */
	public int getNumSample(){
		return n_sample;
	}
	
	/**
	 * @param name il nuovo nome della sessione
	 * @return
	 */
	public void setName(String name){
		this.name = name;
	}
	
	/**
	 * @param modified_date la nuova data di modifica
	 * @return
	 */
	public void setModifiedDate(String modified_date){
		this.modified_date = modified_date;
	}
	
	/**
	 * @param image immagine in formato stringa
	 * @return
	 */
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
	
	/**
	 * @return lo stato di selezione della sessione
	 */
	public boolean isSelected(){
		return selected;
	}
	
}