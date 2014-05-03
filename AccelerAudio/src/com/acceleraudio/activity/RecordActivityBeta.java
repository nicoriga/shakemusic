package com.acceleraudio.activity;

import java.util.ArrayList;

import com.acceleraudio.database.DbAdapter;
import com.acceleraudio.service.RecordTrack;
import com.malunix.acceleraudio.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class RecordActivityBeta extends Activity {
	
	private boolean initialized, insertComplete = false;
	private static Button startSession, stopSession, pauseSession;
	protected static EditText nameSession;
	protected static TextView  rec_sample, time_remaining;
	private static ProgressBar progressBarX , progressBarY, progressBarZ;
	public static ArrayList<Float> data_x, data_y, data_z;
	private DbAdapter dbAdapter;
	public static int sample, x, y, z;
	private Intent intentRecord;
	private long sessionId;
	private boolean axis_x, axis_y, axis_z;
	private int sample_rate;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_3);
    	
    	// creo intent per avviare il servizio di registrazione
    	intentRecord = new Intent(this, RecordTrack.class);
    	
    	initialized = false;
		
		dbAdapter = new DbAdapter(this);
		
		data_x = new ArrayList<Float>();
		data_y = new ArrayList<Float>();
		data_z = new ArrayList<Float>();
		sample = 0;
		x = 0;
		y = 0;
		z = 0;
		
////////////////////////////////////////////////////////
///////////// collego widget con xml ///////////////////
///////////////////////////////////////////////////////

		nameSession = (EditText) findViewById(R.id.UI3editText1);
		rec_sample = (TextView) findViewById(R.id.UI3textView3);
		time_remaining = (TextView) findViewById(R.id.UI3_timeRemaining);
    	startSession = (Button) findViewById(R.id.UI3button1);
    	stopSession = (Button) findViewById(R.id.UI3button2);
    	pauseSession = (Button) findViewById(R.id.UI3button3);
    	progressBarX = (ProgressBar) findViewById(R.id.UI3verticalBarX);
    	progressBarY = (ProgressBar) findViewById(R.id.UI3verticalBarY);
    	progressBarZ = (ProgressBar) findViewById(R.id.UI3verticalBarZ);
    	//progressBarX.setMax((int)accelerometro.getMaximumRange());
    	//progressBarY.setMax((int)accelerometro.getMaximumRange());
    	//progressBarZ.setMax((int)accelerometro.getMaximumRange());
    	progressBarX.setMax(15);
    	progressBarY.setMax(15);
    	progressBarZ.setMax(15);
    	
    	progressBarX.setProgress(0);
		progressBarY.setProgress(0);
		progressBarZ.setProgress(0);
		rec_sample.setText("" + sample);
		
		pauseSession.setEnabled(false);
		stopSession.setEnabled(false);
		
//////////////////////////////////////////////////////////
/// prelevo dati dal database, impostazioni predefinite///
//////////////////////////////////////////////////////////

		// apro la connessione al db
		dbAdapter = new DbAdapter(this);
		dbAdapter.open();
		
		// prelevo record by ID 
		Cursor cursor = dbAdapter.fetchAllPreferences();
		cursor.moveToFirst();
		
		// imposto preferenze
		axis_x = cursor.getString( cursor.getColumnIndex(DbAdapter.T_PREFERENCES_AXIS_X)).equals("1"); // asse x
		axis_y = cursor.getString( cursor.getColumnIndex(DbAdapter.T_PREFERENCES_AXIS_Y)).equals("1"); // asse y
		axis_z = cursor.getString( cursor.getColumnIndex(DbAdapter.T_PREFERENCES_AXIS_Z)).equals("1"); // asse z
		sample_rate = cursor.getInt( cursor.getColumnIndex(DbAdapter.T_SESSION_UPSAMPLING));
		
		// chiudo connessioni
		cursor.close();
		dbAdapter.close();
    	
		//////////////TODO: dati di prova da eliminare
		initialized = true;
		for(int xi = 0; xi < 500; xi++) data_x.add(10f);
		for(int xi = 0; xi < 500; xi++) data_y.add(10f);
		for(int xi = 0; xi < 500; xi++) data_z.add(10f);
		
    }

    @Override
	public void onResume() {
		super.onResume();
		
/////////////////////////////////////////////////////////
////////////aggiungo listener ai bottoni ///////////////
////////////////////////////////////////////////////////

		/**** AVVIA LA REGISTRAZIONE ****/
		startSession.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//TODO: impostazioni per accelerometro intentRecord.putExtra(...);
				startSession.setEnabled(false);
				pauseSession.setEnabled(true);
				stopSession.setEnabled(true);
				startService(intentRecord);
			}
		});
		
		/**** PAUSA LA REGISTRAZIONE ****/
		pauseSession.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startSession.setEnabled(true);
				pauseSession.setEnabled(false);
				stopService(intentRecord);
			}
		});
		
		/**** STOPPA LA REGISTRAZIONE ****/
		stopSession.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startSession.setEnabled(false);
				pauseSession.setEnabled(false);
				
				stopService(intentRecord);
				
				// Verifica che siano stati presi dati dall'accelerometro
				if(initialized)
					if(nameSession.getText().toString().length() > 0) // Verifica che si abbia scritto il nome della sessione
					{
						saveAccelerometerData();
						// avvio la SessionInfoActivity
						Intent i = new Intent(v.getContext(), SessionInfoActivity.class);
						i.putExtra(DbAdapter.T_SESSION_SESSIONID, (int)sessionId);
						v.getContext().startActivity(i);
					}
					else Toast.makeText(v.getContext(), "INSERISCI NOME SESSIONE", Toast.LENGTH_SHORT).show();
				else finish(); // se non ci sono dati chiude l'activity
			}
		});
		
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	if (insertComplete) finish();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	stopService(intentRecord);
    }
        
/////////////////////////////////////////////////////////    
////////////////Metodi Utili  //////////////////////////
////////////////////////////////////////////////////////

    public void saveAccelerometerData(){
    	if(initialized){

			// apro la connessione al db
	    	dbAdapter.open();

	    	int n_x = 0;
	    	int n_y = 0;
	    	int n_z = 0;

	    	StringBuilder x_sb = new StringBuilder();
	    	StringBuilder y_sb = new StringBuilder();
	    	StringBuilder z_sb = new StringBuilder();
	    	for (float value : data_x){   x_sb.append(" " + value); n_x++;}
	    	for (float value : data_y){   y_sb.append(" " + value); n_y++;}
	    	for (float value : data_z){   z_sb.append(" " + value); n_z++;}

	    	// inserisco i dati della sessione nel database
	    	//TODO: gestire nome vuoto se non viene inserito un nome per la sessione... con un messaggio che richiede l'inserimento del nome
	    	sessionId = dbAdapter.createSession( nameSession.getText().toString(), R.drawable.ic_launcher, (axis_x? 1:0), (axis_y? 1:0), (axis_z? 1:0), sample_rate, x_sb.toString(), y_sb.toString(), z_sb.toString(), n_x, n_y, n_z );
	    	insertComplete = true;
	    	
			// chiudo la connessione al db
			dbAdapter.close();
		}
    }
    public static void updateSample(){
		rec_sample.setText("" + sample);
		progressBarX.setProgress(x);
		progressBarY.setProgress(y);
		progressBarZ.setProgress(z);
    }
	public static void updateRemainingTime(String s){
		time_remaining.setText(s);
	}
	public static void finishTime(){
		startSession.setEnabled(false);
		pauseSession.setEnabled(false);
	}
}