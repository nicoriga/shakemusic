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

public class ThirdActivity extends Activity {
	
	private boolean inizializzato;
	private SensorManager sensorManager;
	private Sensor accelerometro;
	private final float rumore = (float) 1.0;
	private float oldX, oldY, oldZ;
	private Button startSession, stopSession, pauseSession;
	private EditText nameSession, rec_sample;
	private VerticalProgressBar progressBarX , progressBarY, progressBarZ;
	private ArrayList<Integer> 
			data_x = new ArrayList<Integer>(), 
			data_y = new ArrayList<Integer>(), 
			data_z = new ArrayList<Integer>();
	private DbAdapter dbAdapter;
	private int sample = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_3);
    	
    	inizializzato = false;
    	sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		
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
    	
/////////////////////////////////////////////////////////
////////////aggiungo listener ai bottoni ///////////////
////////////////////////////////////////////////////////

		/**** AVVIA LA REGISTRAZIONE ****/
    	startSession.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sensorManager.registerListener(mySensorEventListener, accelerometro, SensorManager.SENSOR_DELAY_NORMAL);
			}
		});
    	
    	/**** PAUSA LA REGISTRAZIONE ****/
    	pauseSession.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sensorManager.unregisterListener(mySensorEventListener);
			}
		});
    	
    	/**** STOPPA LA REGISTRAZIONE ****/
    	stopSession.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(inizializzato){
					
					sensorManager.unregisterListener(mySensorEventListener);
					
					// apro la connessione al db
			    	dbAdapter = new DbAdapter(v.getContext());
			    	dbAdapter.open();
			    	
			    	String x = "";
			    	String y = "";
			    	String z = "";
			    	
			    	for (int value : data_x)   x += " " + value;
			    	for (int value : data_y)   y += " " + value;
			    	for (int value : data_z)   z += " " + value;
			    	
			    	// inserisco i dati della sessione nel database
			    	//TODO: gestire nome vuoto se non viene inserito un nome per la sessione... con un messaggio che richiede l'inserimento del nome
					dbAdapter.createSession( nameSession.getText().toString(), R.drawable.ic_launcher, 1, 1, 1, 48000, "25/01/2014", "14/04/2014", x, y, z );
					
					// chiudo la connessione al db
					dbAdapter.close();
				}
				
				Intent i = new Intent(v.getContext(), FirstActivity.class);
				v.getContext().startActivity(i);
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
	    		if (!inizializzato) {
	    			
	    			oldX = x;
	    			oldY = y;
	    			oldZ = z;
	    			
	    			data_x.add(0);
	    			data_y.add(0);
	    			data_z.add(0);
	    			
	    			progressBarX.setProgress(0);
	    			progressBarY.setProgress(0);
	    			progressBarZ.setProgress(0);
	    			
	    			rec_sample.setText("" + sample);
	    			
	    			inizializzato = true;
	    			
	    		} else {
	    			
	    			float deltaX = Math.abs(oldX - x);
	    			float deltaY = Math.abs(oldY - y);
	    			float deltaZ = Math.abs(oldZ - z);
	    			
	    			if (deltaX < rumore)
	    				deltaX = (float) 0.0;
	    			else
	    			{
	    				data_x.add((int)deltaX);
	    				sample++;
	    				rec_sample.setText("" + sample);
	    			}
	    			
	    			if (deltaY < rumore)
	    				deltaY = (float) 0.0;
	    			else
	    			{
	    				data_y.add((int)deltaY);
	    				sample++;
	    				rec_sample.setText("" + sample);
	    			}
	    			
	    			if (deltaZ < rumore)
	    				deltaZ = (float) 0.0;
	    			else
	    			{
	    				data_z.add((int)deltaZ);
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
    
}