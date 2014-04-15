package com.example.acceleraudio;


import com.example.acceleraudio.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
 
public class FirstActivity extends Activity {
    /** Called when the activity is first created. */
	Button firstButton, secondButton;
	TextView localText;
	 ListView list;
	  String[] web = {
		  "Sessione 1",
	      "Sessione 2",
	      "Sessione 3",
	      "Sessione 4",
	      "Sessione 5",
	      "Sessione 6",
	      "Sessione 7",
	      "Sessione 8",
	      "Sessione 9",
	      "Sessione 10",
	      "Sessione 11",
	      "Sessione 12",
	      "Sessione 13",
	      "Sessione 14",
	      "Sessione 15",
	      "Sessione 16",
	      "Sessione 17",
	      "Sessione 18",
	      "Sessione 19",
	      "Sessione 20"
	  } ;
	  Integer[] imageId = {
	      R.drawable.ic_launcher,
	      R.drawable.ic_launcher,
	      R.drawable.ic_launcher,
	      R.drawable.ic_launcher,
	      R.drawable.ic_launcher,
	      R.drawable.ic_launcher,
	      R.drawable.ic_launcher,
	      R.drawable.ic_launcher,
	      R.drawable.ic_launcher,
	      R.drawable.ic_launcher,
	      R.drawable.ic_launcher,
	      R.drawable.ic_launcher,
	      R.drawable.ic_launcher,
	      R.drawable.ic_launcher,
	      R.drawable.ic_launcher,
	      R.drawable.ic_launcher,
	      R.drawable.ic_launcher,
	      R.drawable.ic_launcher,
	      R.drawable.ic_launcher,
	      R.drawable.ic_launcher
	  };
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	//TextView localText = new TextView(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landscapelayoutmain);
        //localText.setText("Hello from a TextView");
        //firstButton = (Button) findViewById(R.id.button1);
        //secondButton = (Button) findViewById(R.id.button2);
        
        //---------aggiunge il generatore di evento
        //firstButton.setOnClickListener(new ButtonAction((TextView) findViewById(R.id.textView1), "bottone1"));
        //secondButton.setOnClickListener(new ButtonAction((TextView) findViewById(R.id.textView1), "bottone2"));
        
        Spinner spinner = (Spinner) findViewById(R.id.spinner1);
	    // Create an ArrayAdapter using the string array and a default spinner layout
	    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.upsampling_array, android.R.layout.simple_spinner_item);
	    // Specify the layout to use when the list of choices appears
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    // Apply the adapter to the spinner
	    spinner.setAdapter(adapter);
	    
	    CustomListSession adapter1 = new CustomListSession(FirstActivity.this, web, imageId);
	    list=(ListView)findViewById(R.id.list);
	    list.setAdapter(adapter1);
    }
}