package com.acceleraudio.activity;

import java.util.ArrayList;

import com.acceleraudio.database.DbAdapter;
import com.acceleraudio.service.PlayerTrack;
import com.malunix.acceleraudio.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FourthActivity extends Activity {
	
	public static String MUSIC_CURSOR = "music_cursor"; // puntatore array della musica in riproduzione
	public static String SOUND_RATE = "soundRate";
	
	//private PlayerTrack player;
	private Button play, pause, stop;
	private ArrayList<Integer> sample = new ArrayList<Integer>();
	private int sessionId, upsampling, musicCursor = 0; // musicCursos: puntatore array della musica in riproduzione
	private DbAdapter dbAdapter;
	private Cursor cursor;
	public Intent intentPlayer;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_4);
    	
    	intentPlayer = new Intent(this, PlayerTrack.class);
    	
////////////////////////////////////////////////////////
///////////// Prelevo dati dall'intent /////////////////
///////////////////////////////////////////////////////  

		Bundle b = getIntent().getExtras();
		sessionId = b.getInt(DbAdapter.T_SESSION_SESSIONID);
    	
////////////////////////////////////////////////////////
///////////// collego widget con xml ///////////////////
///////////////////////////////////////////////////////

		play = (Button) findViewById(R.id.UI4button2);
		pause = (Button) findViewById(R.id.UI4button3);
		stop = (Button) findViewById(R.id.UI4button1);
    	
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
		cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_NAME));
		cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_SENSOR_DATA_X));
		cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_AXIS_X)).equals("1");
		cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_AXIS_Y)).equals("1");
		cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_AXIS_Z)).equals("1");
		upsampling = cursor.getInt(( cursor.getColumnIndex(DbAdapter.T_SESSION_UPSAMPLING)));
		
		// chiudo connessioni
		cursor.close();
		dbAdapter.close();
		
		// dati di prova
		sample.add(10);
		sample.add(50);
		sample.add(100);
		sample.add(150);
		sample.add(80);
		sample.add(30);
		sample.add(0);
		
    	//player = new PlayerTrack(sample, 48000);
    	
/////////////////////////////////////////////////////////
////////////aggiungo listener ai bottoni ///////////////
////////////////////////////////////////////////////////

    	/**** play music ****/
    	play.setOnClickListener(new View.OnClickListener() {
    		@Override
			public void onClick(View v) {
    			// TODO: caricare i dati e le impostazioni della sessione da riprodurre solo la prima volta, boolean inizializzato per la prima volta
    			intentPlayer.putExtra(MUSIC_CURSOR, musicCursor);
    			intentPlayer.putExtra(SOUND_RATE, upsampling);
    			startService(intentPlayer);
			}
		});
    	
    	/**** pause music ****/
    	pause.setOnClickListener(new View.OnClickListener() {
    		@Override
			public void onClick(View v) {
    			stopService(intentPlayer);
			}
		});
    	
    	/**** stop music ****/
    	stop.setOnClickListener(new View.OnClickListener() {
    		@Override
			public void onClick(View v) {
    			stopService(intentPlayer);
			}
		});
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	stopService(intentPlayer);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
    	
    	@Override
        public void onReceive(Context context, Intent intent) {
    		Bundle bundle = intent.getExtras();
    		if (bundle != null) {
    			musicCursor = bundle.getInt(MUSIC_CURSOR);
    		}
        }
    };
    
    @Override
    protected void onResume() {
    	super.onResume();
    	registerReceiver(receiver, new IntentFilter(PlayerTrack.NOTIFICATION));
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	unregisterReceiver(receiver);
    }

}