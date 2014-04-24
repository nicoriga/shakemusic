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

public class RecordActivity extends Activity {

	private boolean initialized;
	private SensorManager sensorManager;
	private Sensor accelerometro;
	private final float rumore = (float) 1.0;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_3l);
    	
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
    	
/////////////////////////////////////////////////////////
////////////aggiungo listener ai bottoni ///////////////
////////////////////////////////////////////////////////

		/**** AVVIA LA REGISTRAZIONE ****/
    	startSession.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sensorManager.registerListener(mySensorEventListener, accelerometro, SensorManager.SENSOR_DELAY_NORMAL);
				//TODO: impostazioni per accelerometro intentRecord.putExtra(...);
    			//startService(intentRecord);
			}
		});
    	
    	/**** PAUSA LA REGISTRAZIONE ****/
    	pauseSession.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sensorManager.unregisterListener(mySensorEventListener);
				//stopService(intentRecord);
			}
		});
    	
    	/**** STOPPA LA REGISTRAZIONE ****/
    	stopSession.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveAccelerometerData();
				//Intent i = new Intent(v.getContext(), ListSessionActivity.class);
				//v.getContext().startActivity(i);
				finish();
			}
		});
    	
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

			//sensorManager.unregisterListener(mySensorEventListener);
			//stopService(intentRecord);

			// apro la connessione al db
	    	dbAdapter.open();

	    	String x = "";
	    	String y = "";
	    	String z = "";

	    	for (float value : data_x)   x += " " + value;
	    	for (float value : data_y)   y += " " + value;
	    	for (float value : data_z)   z += " " + value;

	    	// inserisco i dati della sessione nel database
	    	//TODO: gestire nome vuoto se non viene inserito un nome per la sessione... con un messaggio che richiede l'inserimento del nome
			dbAdapter.createSession( nameSession.getText().toString(), R.drawable.ic_launcher, 1, 1, 1, 48000, "25/01/2014", "14/04/2014", x, y, z );

			// chiudo la connessione al db
			dbAdapter.close();
		}
    }
    
}