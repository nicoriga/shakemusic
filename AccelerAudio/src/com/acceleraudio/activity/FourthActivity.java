package com.acceleraudio.activity;

import java.util.ArrayList;

import com.acceleraudio.database.DbAdapter;
import com.example.acceleraudio.R;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FourthActivity extends Activity {
	
	private PlayerTrack player;
	private Button play, pause, stop;
	private ArrayList<Integer> sample;
	private int sessionId;
	private DbAdapter dbAdapter;
	private Cursor cursor;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_4);
	  
////////////////////////////////////////////////////////
///////////// Prelevo dati dall'intent /////////////////
///////////////////////////////////////////////////////  

		Bundle b = getIntent().getExtras();
		sessionId = b.getInt(DbAdapter.COLUMN_SESSIONID);
    	
////////////////////////////////////////////////////////
///////////// collego widget con xml ///////////////////
///////////////////////////////////////////////////////

		play = (Button) findViewById(R.id.UI4button2);
		pause = (Button) findViewById(R.id.UI4button3);
		stop = (Button) findViewById(R.id.UI4button1);
		
    	
    	player = new PlayerTrack(sample, 48000);
    	
////////////////////////////////////////////////////////
/// prelevo dati dal database e li carico nella vista///
///////////////////////////////////////////////////////

		// apro la connessione al db
		dbAdapter = new DbAdapter(this);
		dbAdapter.open();
		
		// prelevo record by ID 
		cursor = dbAdapter.fetchSessionById(sessionId);
		cursor.moveToFirst();
		
		// carico dati
		cursor.getString( cursor.getColumnIndex(DbAdapter.COLUMN_NAME));
		cursor.getString( cursor.getColumnIndex(DbAdapter.COLUMN_SENSOR_DATA_X));
		cursor.getString( cursor.getColumnIndex(DbAdapter.COLUMN_AXIS_X)).equals("1");
		cursor.getString( cursor.getColumnIndex(DbAdapter.COLUMN_AXIS_Y)).equals("1");
		cursor.getString( cursor.getColumnIndex(DbAdapter.COLUMN_AXIS_Z)).equals("1");
		cursor.getString( cursor.getColumnIndex(DbAdapter.COLUMN_UPSAMPLING));
		
		// chiudo connessioni
		cursor.close();
		dbAdapter.close();
    	
/////////////////////////////////////////////////////////
////////////aggiungo listener ai bottoni ///////////////
////////////////////////////////////////////////////////

    	/**** play music ****/
    	play.setOnClickListener(new View.OnClickListener() {
    		@Override
			public void onClick(View v) {
				player.startMusic();
			}
		});
    	
    	/**** stop music ****/
    	stop.setOnClickListener(new View.OnClickListener() {
    		@Override
			public void onClick(View v) {
				player.stopMusic();
			}
		});
    }
}