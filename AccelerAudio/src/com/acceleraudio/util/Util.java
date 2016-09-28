package com.acceleraudio.util;

import java.util.ArrayList;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

/**
 * @author Nicola Rigato
 * @author Luca Del Salvador
 * @author Marco Tessari
 * @author Gruppo: Malunix
 *
 * classe contenente metodi utili alle altre classi
 */
public class Util{
	
    /**
     * Seleziona elemento dello spinner in base al valore passato
     * 
     * @param spinner 
     * @param value valore da impostare come selezionato
     */
    public static void selectSpinnerItemByValue(Spinner spinner, String value)
    {
    	SpinnerAdapter adapter = spinner.getAdapter();
        for (int position = 0; position < adapter.getCount(); position++)
        {
        	String v = adapter.getItem(position).toString();
            if(v.equals(value))
            {
            	spinner.setSelection(position);
                return;
            }
        } 
    }
    
    /**
     * Copia ArrayList<Float> in Float[]
     * 
     * @param alf array list float
     * @return array float
     */
    public static float[] toArrayFloat(ArrayList<Float> alf)
    {
    	float[] floatA = new float[alf.size()];
    	for(int i = 0; i < alf.size(); i++)
    		floatA[i] = alf.get(i);
    	return floatA;
    }
    
    /**
     * Copia float[] in ArrayList<Float>
     * 
     * @param f array float
     * @return arrayList float
     */
    public static ArrayList<Float> toArrayListFloat(float[] f)
    {
    	ArrayList<Float> floatL = new ArrayList<Float>();
    	for(int i = 0; i < f.length; i++)
    		floatL.add(f[i]);
    	return floatL;
    }
    
    /**
     * ritorna il numero univoco della velocita del sensore in base al nome
     * 
     * @param n nome delay sensore
     * @return delay sensore
     */
    public static int sensorRateByString(String n)
    {
    	if(n.equalsIgnoreCase("lento")) return SensorManager.SENSOR_DELAY_NORMAL;
    	if(n.equalsIgnoreCase("normale")) return SensorManager.SENSOR_DELAY_UI;
    	if(n.equalsIgnoreCase("veloce")) return SensorManager.SENSOR_DELAY_GAME;
    	return SensorManager.SENSOR_DELAY_NORMAL;
    }
    
    /**
     * ritorna il nome corrispondente alla velocita del sensore
     * 
     * @param r sensor delay
     * @return nome delay sensore
     */
    public static String sensorRateName(int r)
    {
    	if(r == SensorManager.SENSOR_DELAY_NORMAL) return "Lento";
    	if(r == SensorManager.SENSOR_DELAY_UI) return "Normale";
    	if(r == SensorManager.SENSOR_DELAY_GAME) return "Veloce";
    	return "Normale";
    }
    
    /**
     * blocca la rotazione dello schermo
     * 
     * @param a activity da bloccare
     * @param v la view corrente
     * @return il valore di orientamento
     */
    public static int lockOrientation(Activity a, View v)
    {
    	int orientation = v.getResources().getConfiguration().orientation;
		
    	// se lo schermo si trova in una delle posizione conosciute allora si blocca in quella
    	switch(orientation){
    		case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
    		case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
    		case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
    		case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
    			a.setRequestedOrientation(orientation);
    			return orientation;
    		default:
    			// nel caso il dispositivo si trovi in reverse-landscape viene bloccato in landscape
    			// qusto perchè il reverse non è disponibile nelle api 8
    			a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    			return orientation;
    	}
    }
    
    /**
     * sblocca la rotazione dello schermo
     * 
     * @param a activity da sbloccare
     */
    public static void unlockOrientation(Activity a)
    {
    	a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    /**
     * converte i millisecondi in una stringa nel formato minuti:secondi.decimi
     * 
     * @param milliseconds
     * @return stringa mm:ss.dd
     */
    public static String millisecondsToMinutesSeconds (long milliseconds)
    {
	    int seconds = (int) (milliseconds / 1000) % 60 ;
	    int minutes = (int) ((milliseconds / (1000*60)) % 60);
	    int decimalSeconds = (int) ((((milliseconds / 1000.0)+0.001)*10)%10);
	    String m = (minutes>9? ""+minutes: "0"+minutes);
	    String s = (seconds>9? ""+seconds: "0"+seconds);
	    return m + ":" + s + "." + decimalSeconds;
    }
}

