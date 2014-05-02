package com.acceleraudio.activity;

import java.util.ArrayList;

import com.acceleraudio.database.DbAdapter;
import com.acceleraudio.design.VerticalProgressBar;
import com.malunix.acceleraudio.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RecordActivity extends Activity {

	private boolean initialized, insertComplete = false;
	private SensorManager sensorManager;
	private Sensor accelerometro;
	private final float rumore = (float) 0.3;
	private float oldX, oldY, oldZ;
	private Button startSession, stopSession, pauseSession;
	private EditText nameSession, rec_sample;
	private VerticalProgressBar progressBarX , progressBarY, progressBarZ;
	private ArrayList<Float> 
			data_x = new ArrayList<Float>(), 
			data_y = new ArrayList<Float>(), 
			data_z = new ArrayList<Float>();
	private DbAdapter dbAdapter;
	private int sample = 0;
	public Intent intentRecord;
	private long sessionId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_3);
    	
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
		
		////////////// TODO: dati di prova da eliminare
		for(int xi = 0; xi < 1500; xi++) data_x.add(10f);
    	for(int xi = 0; xi < 1500; xi++) data_y.add(10f);
    	for(int xi = 0; xi < 1500; xi++) data_z.add(10f);
    	
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
				sensorManager.registerListener(mySensorEventListener, accelerometro, SensorManager.SENSOR_DELAY_NORMAL);
				//TODO: impostazioni per accelerometro intentRecord.putExtra(...);
				startSession.setEnabled(false);
				pauseSession.setEnabled(true);
				stopSession.setEnabled(true);
			}
		});
		
		/**** PAUSA LA REGISTRAZIONE ****/
		pauseSession.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sensorManager.unregisterListener(mySensorEventListener);
				startSession.setEnabled(true);
				pauseSession.setEnabled(false);
			}
		});
		
		/**** STOPPA LA REGISTRAZIONE ****/
		stopSession.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sensorManager.unregisterListener(mySensorEventListener);
				startSession.setEnabled(false);
				pauseSession.setEnabled(false);
				if(nameSession.getText().toString().length() > 0)
				{
					saveAccelerometerData();
					// avvio la PlayerActivity
					Intent i = new Intent(v.getContext(), SessionInfoActivity.class);
					i.putExtra(DbAdapter.T_SESSION_SESSIONID, (int)sessionId);
					v.getContext().startActivity(i);
					//finish();
				}
				else Toast.makeText(v.getContext(), "INSERISCI NOME SESSIONE", Toast.LENGTH_SHORT).show();
			}
		});
    }
    
    @Override
	public void onPause() {
		super.onPause();
		
		if (insertComplete) finish();
    }

/////////////////////////////////////////////////////////
//////////////// Gestione Accelerometro /////////////////
////////////////////////////////////////////////////////

	final SensorEventListener mySensorEventListener = new SensorEventListener() { 
        public void onSensorChanged(SensorEvent event) {
        	if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
	        	float x = event.values[0];
	    		float y = event.values[1];
	    		float z = event.values[2];
	    		if (!initialized) {

	    			oldX = x;
	    			oldY = y;
	    			oldZ = z;

	    			data_x.add(0f);
	    			data_y.add(0f);
	    			data_z.add(0f);

	    			progressBarX.setProgress(0);
	    			progressBarY.setProgress(0);
	    			progressBarZ.setProgress(0);

	    			rec_sample.setText("" + sample);

	    			initialized = true;

	    		} else {

	    			float deltaX = Math.abs(oldX - x);
	    			float deltaY = Math.abs(oldY - y);
	    			float deltaZ = Math.abs(oldZ - z);

	    			if (deltaX < rumore)
	    				deltaX = (float) 0.0;
	    			else
	    			{
	    				data_x.add(deltaX);
	    				sample++;
	    				rec_sample.setText("" + sample);
	    			}

	    			if (deltaY < rumore)
	    				deltaY = (float) 0.0;
	    			else
	    			{
	    				data_y.add(deltaY);
	    				sample++;
	    				rec_sample.setText("" + sample);
	    			}

	    			if (deltaZ < rumore)
	    				deltaZ = (float) 0.0;
	    			else
	    			{
	    				data_z.add(deltaZ);
	    				sample++;
	    				rec_sample.setText("" + sample);
	    			}

	    			oldX = x;
	    			oldY = y;
	    			oldZ = z;

	    			progressBarX.setProgress((int)deltaX);
	    			progressBarY.setProgress((int)deltaY);
	    			progressBarZ.setProgress((int)deltaZ);	
	    		}
        	}
        }
        
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }
    };
    
/////////////////////////////////////////////////////////    
//////////////// Metodi Utili  //////////////////////////
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