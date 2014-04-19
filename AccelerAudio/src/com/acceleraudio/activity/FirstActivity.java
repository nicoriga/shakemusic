package com.acceleraudio.activity;

import java.util.ArrayList;

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
	
	private Button newSession, preferences;
	private ListView list;
	//private String[] sessionName;
	private ArrayList<Integer> sessionIdList = new ArrayList<Integer>();
	private ArrayList<String> sessionNameList = new ArrayList<String>();
	private String[] sessionDataMod;
	private Integer[] imageId;
	private DbAdapter dbAdapter; 
    private Cursor cursor;
	  
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_1);
    	
////////////////////////////////////////////////////////
///////////// collego widget con xml ///////////////////
///////////////////////////////////////////////////////
    	
    	newSession = (Button)findViewById(R.id.UI1button1);
    	preferences = (Button)findViewById(R.id.UI1button3);
    	list = (ListView)findViewById(R.id.UI1listSession);
    		
////////////////////////////////////////////////////////
///////////// Popolo la listview ///////////////////////
///////////////////////////////////////////////////////
    	
    	// apro la connessione al db
    	dbAdapter = new DbAdapter(this);
    	dbAdapter.open();
    	
        // prelevo tutti i record 
        cursor = dbAdapter.fetchAllSession();
        
        // istanzio array
        //sessionName = new String[cursor.getCount()];
        sessionDataMod = new String[cursor.getCount()];
        imageId = new Integer[cursor.getCount()];
        
        cursor.moveToFirst();
        int i = 0;
		while ( !cursor.isAfterLast() ) {
			 sessionIdList.add(cursor.getInt( cursor.getColumnIndex(DbAdapter.COLUMN_SESSIONID)));
			 sessionNameList.add(cursor.getString( cursor.getColumnIndex(DbAdapter.COLUMN_NAME)));
			 sessionDataMod[i] = cursor.getString( cursor.getColumnIndex(DbAdapter.COLUMN_DATE_CHANGE) );
			 imageId[i] = cursor.getInt( cursor.getColumnIndex(DbAdapter.COLUMN_IMAGE));
			 cursor.moveToNext();
			 i++;
		}
		
		cursor.close();
		dbAdapter.close();
    	
/////////////////////////////////////////////////////////
///////////  aggiungo listener  /////////////////////////
////////////////////////////////////////////////////////
		
    	newSession.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// avvio la terza activity
		    	Intent i = new Intent(view.getContext(), ThirdActivity.class);
		    	view.getContext().startActivity(i);
			}
		});
    	
    	preferences.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// avvio la quinta activity
		    	Intent i = new Intent(view.getContext(), FifthActivity.class);
		    	view.getContext().startActivity(i);
			}
		});
    	
	    CustomListSession adapter1 = new CustomListSession(this, sessionNameList, sessionDataMod, imageId);
	    list.setAdapter(adapter1);
	    list.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    	// avvio la seconda activity
		    	Intent i = new Intent(view.getContext(), SecondActivity.class);
		    	i.putExtra(DbAdapter.COLUMN_SESSIONID, sessionIdList.get(position));
		    	
		    	// Toast.makeText(getApplicationContext(), "Click ListItem Number " + sessionIdList.get(position), Toast.LENGTH_LONG).show();
		    	
		    	view.getContext().startActivity(i);
		    }
	    });
	       
    }
}