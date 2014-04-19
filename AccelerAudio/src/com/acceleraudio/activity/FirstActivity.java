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
    	
    	// apro la connessione al db
    	dbAdapter = new DbAdapter(this);
    	dbAdapter.open();
         
        /* inserisco record di prova 
    	dbAdapter.createSession("sessione db 1", R.drawable.ic_launcher, 1, 1, 1, 48000, "25/01/2014", "14/04/2014", "6214562621969612565452189764845664515" );
    	dbAdapter.createSession("sessione db 2", R.drawable.ic_launcher, 1, 0, 1, 44000, "08/02/2014", "14/04/2014", "154351864154181428548341685431568" );
    	dbAdapter.createSession("sessione db 3", R.drawable.ic_launcher, 0, 1, 1, 44000, "14/03/2014", "14/04/2014", "546518458435843818344215187431" );
    	dbAdapter.createSession("sessione db 4", R.drawable.ic_launcher, 1, 0, 0, 48000, "05/04/2014", "14/04/2014", "8486468468738515615695492519*7/97545856451556186/48132" );
    	dbAdapter.createSession("sessione db 5", R.drawable.ic_launcher, 1, 0, 0, 48000, "05/04/2014", "14/04/2014", "656516541856845151784754154512." );
    	dbAdapter.createSession("sessione db 6", R.drawable.ic_launcher, 1, 0, 0, 48000, "05/04/2014", "14/04/2014", "2512315645487453145156512534515314" );
    	dbAdapter.createSession("sessione db 7", R.drawable.ic_launcher, 1, 0, 0, 48000, "15/04/2014", "14/04/2014", "3412564512334515531455612345351" );
    	dbAdapter.createSession("sessione db 8", R.drawable.ic_launcher, 1, 0, 0, 44000, "05/04/2014", "14/04/2014", "2345516234551263844515623145862123453512341525643464" );
    	dbAdapter.createSession("sessione db 9", R.drawable.ic_launcher, 1, 0, 0, 48000, "05/04/2014", "14/04/2014", "1263545152345212345512384551253344552134682324551234521" );
    	dbAdapter.createSession("sessione db 10", R.drawable.ic_launcher, 1, 0, 0, 48000, "05/04/2014", "14/04/2014", "5213465123465516253421565213455123454652534315" );
        */
    	
        // prelevo tutti i record 
        cursor = dbAdapter.fetchAllSession();
        
        // istanzio array
        //sessionName = new String[cursor.getCount()];
        sessionDataMod = new String[cursor.getCount()];
        imageId = new Integer[cursor.getCount()];
        
        cursor.moveToFirst();
        int i = 0;
		while ( !cursor.isAfterLast() ) {
			 //sessionName[i] = cursor.getString( cursor.getColumnIndex(DbAdapter.COLUMN_NAME) );
			 sessionIdList.add(cursor.getInt( cursor.getColumnIndex(DbAdapter.COLUMN_SESSIONID)));
			 sessionNameList.add(cursor.getString( cursor.getColumnIndex(DbAdapter.COLUMN_NAME)));
			 sessionDataMod[i] = cursor.getString( cursor.getColumnIndex(DbAdapter.COLUMN_DATE_CHANGE) );
			 imageId[i] = cursor.getInt( cursor.getColumnIndex(DbAdapter.COLUMN_IMAGE));
			 // per ripulire il db dalle sessioni
			 //dbAdapter.deleteSession(cursor.getInt( cursor.getColumnIndex(DbAdapter.COLUMN_SESSIONID)));
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
    	
	    CustomListSession adapter1 = new CustomListSession(this, sessionNameList, sessionDataMod, imageId);
	    list=(ListView)findViewById(R.id.UI1listSession);
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