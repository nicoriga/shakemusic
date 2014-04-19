package com.acceleraudio.activity;

import com.acceleraudio.database.DbAdapter;
import com.acceleraudio.design.CustomListSession;
import com.example.acceleraudio.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
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
	private DbAdapter dbAdapter; 
    private Cursor cursor;
	  
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_1);
    	
    	// apro la connessione al db
    	dbAdapter = new DbAdapter(this);
    	dbAdapter.open();
         
        /* inserisco record di prova
    	dbAdapter.createSession("sessione db 1", R.drawable.ic_launcher, 1, 1, 1, 48000, "25/01/2014", "14/04/2014" );
    	dbAdapter.createSession("sessione db 2", R.drawable.ic_launcher, 1, 0, 1, 44000, "08/02/2014", "14/04/2014" );
    	dbAdapter.createSession("sessione db 3", R.drawable.ic_launcher, 0, 1, 1, 44000, "14/03/2014", "14/04/2014" );
    	dbAdapter.createSession("sessione db 4", R.drawable.ic_launcher, 1, 0, 0, 48000, "05/04/2014", "14/04/2014" );
        */ 
    	
        // prelevo tutti i record 
        cursor = dbAdapter.fetchAllSession();
        
  
        cursor.moveToFirst();
        int i = 0;
		while ( !cursor.isAfterLast() ) {
			 sessionName[i] = cursor.getString( cursor.getColumnIndex(DbAdapter.COLUMN_NAME) );
			 sessionDataMod[i] = cursor.getString( cursor.getColumnIndex(DbAdapter.COLUMN_DATE_CHANGE) );
			 cursor.moveToNext();
			 i++;
		}
		
		cursor.close();
		dbAdapter.close();
    	
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