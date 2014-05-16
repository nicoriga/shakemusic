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
    			return 1;
    		case "lineare":
    			return 2;
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
    		case 1:
    			return "Note";
    		case 2:
    			return "Lineare";
    		default:
    			return "";
        } 
    }
    
    /**** Copia Object[] in int[] ****/
    public static int[] copyArrayInt(Object[] obA)
    {
    	int[] intA = new int[obA.length];
    	
    	for(int i = 0; i < obA.length; i++)
    	{
    		intA[i] = (int) obA[i];
    	}
    	
    	return intA;
    }
    
    /**** Copia Object[] in Float[] ****/
    public static float[] copyArrayFloat(Object[] obA)
    {
    	float[] floatA = new float[obA.length];
    	
    	for(int i = 0; i < obA.length; i++)
    	{
    		floatA[i] = (int) obA[i];
    	}
    	
    	return floatA;
    }
    
    /**** Copia float[] in ArrayList<Float> ****/
    public static ArrayList<Float> copyListFloat(float[] obA)
    {
    	ArrayList<Float> floatL = new ArrayList<Float>();
    	
    	for(int i = 0; i < obA.length; i++)
    	{
    		floatL.add((float)obA[i]);
    	}
    	
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

