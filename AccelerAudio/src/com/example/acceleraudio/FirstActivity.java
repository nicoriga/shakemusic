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
	  String[] sessionName = {
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
	  String[] sessionDataMod = {
			  "12/01/2014",
		      "05/01/2014",
		      "25/02/2014",
		      "12/01/2014",
		      "05/01/2014",
		      "25/02/2014",
		      "12/01/2014",
		      "05/01/2014",
		      "25/02/2014",
		      "12/01/2014",
		      "05/01/2014",
		      "25/02/2014",
		      "12/01/2014",
		      "05/01/2014",
		      "25/02/2014",
		      "12/01/2014",
		      "05/01/2014",
		      "25/02/2014",
		      "30/03/2014",
		      "12/03/2014"
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
	  String[] upsamplingRate = {
			  "44000",
		      "48000"
		  } ;
	  
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.landscape_main_layout);
    	
    	//TextView localText = new TextView(this);
        //localText.setText("Hello from a TextView");
        //firstButton = (Button) findViewById(R.id.button1);
        //secondButton = (Button) findViewById(R.id.button2);
        
        //---------aggiunge il generatore di evento
        //firstButton.setOnClickListener(new ButtonAction((TextView) findViewById(R.id.textView1), "bottone1"));
        //secondButton.setOnClickListener(new ButtonAction((TextView) findViewById(R.id.textView1), "bottone2"));
        
        Spinner spinner = (Spinner) findViewById(R.id.spinner1);
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.c_spinner_menu, R.id.upsamplingRate, upsamplingRate); // creo un custom adapter
        //UpsamplingAdapter adapter = new UpsamplingAdapter(this,android.R.layout.simple_spinner_item, upsamplingRate);
        //adapter.setDropDownViewResource(R.layout.c_spinner_menu);

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.upsampling_array, R.layout.custom_spinner_list);
        adapter.setDropDownViewResource(R.layout.customer_spinner);

        spinner.setAdapter(adapter); // applico adapter allo spinner
	    
	    CustomListSession adapter1 = new CustomListSession(FirstActivity.this, sessionName, sessionDataMod, imageId);
	    list=(ListView)findViewById(R.id.list);
	    list.setAdapter(adapter1);
	       
	    
    }
}