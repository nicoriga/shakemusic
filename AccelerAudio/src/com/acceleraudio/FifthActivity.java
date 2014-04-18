package com.acceleraudio;

import com.example.acceleraudio.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class FifthActivity extends Activity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_5);
	    
    	Spinner spinner1 = (Spinner) findViewById(R.id.UI5spinner1);
    	ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.upsampling_array, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1); // applico adapter allo spinner
        
        Spinner spinner2 = (Spinner) findViewById(R.id.UI5spinner2);
        // creo un custom adapter
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.upsampling_array, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2); // applico adapter allo spinner
    }
}