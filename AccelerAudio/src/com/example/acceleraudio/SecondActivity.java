package com.example.acceleraudio;

import com.example.acceleraudio.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

public class SecondActivity extends Activity {
	
	Button firstButton, secondButton;
	ListView list;
	String[] upsamplingRate = {
			  "44000",
		      "48000"
		  } ;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_2);
        
        Spinner spinner = (Spinner) findViewById(R.id.UI2spinner1);
        // creo un custom adapter
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.upsampling_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter); // applico adapter allo spinner
	       
	    
    }
}