package com.acceleraudio.activity;

import com.example.acceleraudio.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class SecondActivity extends Activity {
	
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
	    
        Button listSession = (Button) findViewById(R.id.UI1button1M);
        listSession.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// avvio la prima activity
		    	Intent i = new Intent(v.getContext(), FirstActivity.class);
		    	v.getContext().startActivity(i);
			}
		});
        
	    Button playSession = (Button) findViewById(R.id.UI2button1);
	    playSession.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// avvio la quarta activity
		    	Intent i = new Intent(v.getContext(), FourthActivity.class);
		    	v.getContext().startActivity(i);
			}
		});
    }
}