package com.acceleraudio;

import com.example.acceleraudio.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
 
public class FirstActivity extends Activity {
	
	Button newSession, preferences;
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

	  
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_1);
    	
    	newSession = (Button)findViewById(R.id.UI1button1);
    	newSession.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// avvio la terza activity
		    	Intent i = new Intent(view.getContext(), ThirdActivity.class);
		    	view.getContext().startActivity(i);
			}
		});
    	
    	preferences = (Button)findViewById(R.id.UI1button3);
    	preferences.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// avvio la quinta activity
		    	Intent i = new Intent(view.getContext(), FifthActivity.class);
		    	view.getContext().startActivity(i);
			}
		});
    	
	    CustomListSession adapter1 = new CustomListSession(this, sessionName, sessionDataMod, imageId);
	    list=(ListView)findViewById(R.id.UI1listSession);
	    list.setAdapter(adapter1);
	    list.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    	// avvio la seconda activity
		    	Intent i = new Intent(view.getContext(), SecondActivity.class);
		    	view.getContext().startActivity(i);
		    }
	    });
	       
    }
}