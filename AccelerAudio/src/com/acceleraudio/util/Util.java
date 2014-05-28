package com.acceleraudio.util;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.hardware.SensorManager;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class Util{
	
	/**** Seleziona elemento dello spinner in base al valore passato ****/
    public static void SelectSpinnerItemByValue(Spinner spinner, String value)
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
    
	/**** restituisce id in base al nome dell'upsampling ****/
    @SuppressLint("DefaultLocale")
	public static int getUpsamplingID(String value)
    {
    	switch(value.toLowerCase())
        {
    		case "note":
    			return MusicUpsampling.NOTE;
    		case "lineare":
    			return MusicUpsampling.LINEAR;
    		default:
    			return 0;
        } 
    }
    
    /**** restituisce nome dell'upsampling in base all'id****/
    @SuppressLint("DefaultLocale")
	public static String getUpsamplingName(int value)
    {
    	switch(value)
        {
    		case MusicUpsampling.NOTE:
    			return "Note";
    		case MusicUpsampling.LINEAR:
    			return "Lineare";
    		default:
    			return "";
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
}

