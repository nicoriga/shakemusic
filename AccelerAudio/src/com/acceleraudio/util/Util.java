package com.acceleraudio.util;

import java.util.ArrayList;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class Util{
	
	/**** Seleziona elemento dello spinner in base al valore passato ****/
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
    
    /**** Copia ArrayList<Float> in Float[] ****/
    public static float[] toArrayFloat(ArrayList<Float> alf)
    {
    	float[] floatA = new float[alf.size()];
    	for(int i = 0; i < alf.size(); i++)
    		floatA[i] = alf.get(i);
    	return floatA;
    }
    
    /**** Copia float[] in ArrayList<Float> ****/
    public static ArrayList<Float> toArrayListFloat(float[] f)
    {
    	ArrayList<Float> floatL = new ArrayList<Float>();
    	for(int i = 0; i < f.length; i++)
    		floatL.add(f[i]);
    	return floatL;
    }
    
    /**** ritorna il numero univoco della velocita del sensore in base al nome  ****/
    public static int sensorRateByString(String n)
    {
    	if(n.equalsIgnoreCase("lento")) return SensorManager.SENSOR_DELAY_NORMAL;
    	if(n.equalsIgnoreCase("normale")) return SensorManager.SENSOR_DELAY_UI;
    	if(n.equalsIgnoreCase("veloce")) return SensorManager.SENSOR_DELAY_GAME;
    	return SensorManager.SENSOR_DELAY_NORMAL;
    }
    
    /**** ritorna il nome corrispondente alla velocita del sensore  ****/
    public static String sensorRateName(int r)
    {
    	if(r == SensorManager.SENSOR_DELAY_NORMAL) return "Lento";
    	if(r == SensorManager.SENSOR_DELAY_UI) return "Normale";
    	if(r == SensorManager.SENSOR_DELAY_GAME) return "Veloce";
    	return "Normale";
    }
    
    /*** blocca la rotazione dello schermo ***/
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
    
    /*** sblocca la rotazione dello schermo ***/
    public static void unlockOrientation(Activity a)
    {
    	a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
    
    public static String millisecondsToMinutesSeconds (long milliseconds)
    {
	    int seconds = (int) (milliseconds / 1000) % 60 ;
	    int minutes = (int) ((milliseconds / (1000*60)) % 60);
	    double temp = milliseconds / 10000;
	    int decimalSeconds = (int) (((temp+0.001)*100)%100);
	    return minutes + ":" + seconds + "." + decimalSeconds;
    }
}

