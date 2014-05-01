package com.acceleraudio.activity;

import java.util.ArrayList;

import com.acceleraudio.database.DbAdapter;
import com.acceleraudio.design.VerticalProgressBar;
import com.acceleraudio.service.RecordTrack;
import com.acceleraudio.service.provaService;
import com.acceleraudio.util.Util;
import com.malunix.acceleraudio.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RecordActivityBeta extends Activity {
	
	private boolean initialized, insertComplete = false;
	private SensorManager sensorManager;
	private Sensor accelerometro;
	private Button startSession, stopSession, pauseSession;
	private EditText nameSession, rec_sample;
	private VerticalProgressBar progressBarX , progressBarY, progressBarZ;
	private ArrayList<Float> 
			data_x = new ArrayList<Float>(), 
			data_y = new ArrayList<Float>(), 
			data_z = new ArrayList<Float>();
	private DbAdapter dbAdapter;
	private int sample = 0, x = 0, y = 0, z = 0;
	public Intent intentRecord;
	private long sessionId;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_3l);
    	
    	// creo intent per avviare il servizio di registrazione
    	intentRecord = new Intent(this, RecordTrack.class);
    	
    	initialized = false;
    	sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		dbAdapter = new DbAdapter(this);
			
////////////////////////////////////////////////////////
///////////// collego widget con xml ///////////////////
///////////////////////////////////////////////////////

		nameSession = (EditText) findViewById(R.id.UI3editText1);
		rec_sample = (EditText) findViewById(R.id.UI3editText2);
    	startSession = (Button) findViewById(R.id.UI3button1);
    	stopSession = (Button) findViewById(R.id.UI3button2);
    	pauseSession = (Button) findViewById(R.id.UI3button3);
    	progressBarX = (VerticalProgressBar) findViewById(R.id.UI3verticalBarX);
    	progressBarY = (VerticalProgressBar) findViewById(R.id.UI3verticalBarY);
    	progressBarZ = (VerticalProgressBar) findViewById(R.id.UI3verticalBarZ);
    	progressBarX.setMax((int)accelerometro.getMaximumRange());
    	progressBarY.setMax((int)accelerometro.getMaximumRange());
    	progressBarZ.setMax((int)accelerometro.getMaximumRange());
    	
    	progressBarX.setProgress(0);
		progressBarY.setProgress(0);
		progressBarZ.setProgress(0);
		rec_sample.setText("" + sample);
		
		pauseSession.setEnabled(false);
		stopSession.setEnabled(false);
    	
		//////////////TODO: dati di prova da eliminare
		initialized = true;
		for(int xi = 0; xi < 1500; xi++) data_x.add(10f);
		for(int xi = 0; xi < 1500; xi++) data_y.add(10f);
		for(int xi = 0; xi < 1500; xi++) data_z.add(10f);
    }
    
    private BroadcastReceiver receiverRecord = new BroadcastReceiver() {
    	
    	@Override
        public void onReceive(Context context, Intent intent) {
    		Bundle bundle = intent.getExtras();
    		if (bundle != null) {
    			String action = intent.getAction();
    			if(action == RecordTrack.NOTIFICATION_RECORD)
    			{
	    			x = (int)bundle.getFloat(RecordTrack.AXIS_X_DATA, x);
	    			y = (int)bundle.getFloat(RecordTrack.AXIS_Y_DATA, y);
	    			z = (int)bundle.getFloat(RecordTrack.AXIS_Z_DATA, z);
	    			rec_sample.setText("" + bundle.getInt(RecordTrack.SAMPLE_N_DATA));
	    			progressBarX.setProgress(x);
	    			progressBarY.setProgress(y);
	    			progressBarZ.setProgress(z);
    			}
    		}
        }
    };
    private BroadcastReceiver receiverStop = new BroadcastReceiver() {
    	
    	@Override
        public void onReceive(Context context, Intent intent) {
    		Bundle bundle = intent.getExtras();
    		if (bundle != null) {
    			String action = intent.getAction();
				if(action == RecordTrack.NOTIFICATION_STOP)
				{
					data_x.addAll(Util.copyListFloat(bundle.getFloatArray(RecordTrack.AXIS_X_DATA)));
					data_y.addAll(Util.copyListFloat(bundle.getFloatArray(RecordTrack.AXIS_Y_DATA)));
					data_z.addAll(Util.copyListFloat(bundle.getFloatArray(RecordTrack.AXIS_Z_DATA)));
					initialized = true;
				}
    		}
        }
    };
    
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
				startService(intentRecord);
				startSession.setEnabled(false);
				pauseSession.setEnabled(true);
				stopSession.setEnabled(true);
			}
		});
		
		/**** PAUSA LA REGISTRAZIONE ****/
		pauseSession.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopService(intentRecord);
				startSession.setEnabled(true);
				pauseSession.setEnabled(false);
			}
		});
		
		/**** STOPPA LA REGISTRAZIONE ****/
		stopSession.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopService(intentRecord);
				startSession.setEnabled(false);
				pauseSession.setEnabled(false);
				
				// Verifica che siano stati presi dati dall'accelerometro
				if(initialized)
					if(nameSession.getText().toString().length() > 0)
					{
						saveAccelerometerData();
						// avvio la PlayerActivity
						Intent i = new Intent(v.getContext(), SessionInfoActivity.class);
						i.putExtra(DbAdapter.T_SESSION_SESSIONID, (int)sessionId);
						v.getContext().startActivity(i);
					}
					else Toast.makeText(v.getContext(), "INSERISCI NOME SESSIONE", Toast.LENGTH_SHORT).show();
				else finish(); // se non ci sono dati chiude l'activity
			}
		});
		
		registerReceiver(receiverRecord, new IntentFilter(RecordTrack.NOTIFICATION_RECORD));
		registerReceiver(receiverStop, new IntentFilter(RecordTrack.NOTIFICATION_STOP));
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	unregisterReceiver(receiverRecord);
    	unregisterReceiver(receiverStop);
    	if (insertComplete) finish();
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
	    	sessionId = dbAdapter.createSession( nameSession.getText().toString(), R.drawable.ic_launcher, 1, 1, 1, 48000, x_sb.toString(), y_sb.toString(), z_sb.toString(), n_x, n_y, n_z );
	    	insertComplete = true;
	    	
			// chiudo la connessione al db
			dbAdapter.close();
		}
    }
    
}