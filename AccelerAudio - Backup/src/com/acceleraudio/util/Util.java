package com.acceleraudio.util;

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
    
    
}

