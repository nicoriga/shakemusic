package com.acceleraudio.util;

import java.util.ArrayList;

import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class Util{
	
	/**** Seleziona item dello spinner in base al valore passato ****/
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
    
}

