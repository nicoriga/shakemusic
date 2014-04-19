package com.acceleraudio.activity;

import java.util.ArrayList;

import com.acceleraudio.database.DbAdapter;
import com.acceleraudio.design.VerticalProgressBar;
import com.example.acceleraudio.R;

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
	private Button startSession, stopSession;
	private EditText nameSession;
	private VerticalProgressBar progressBarX , progressBarY, progressBarZ;
	private ArrayList<Integer> 
			data_x = new ArrayList<Integer>(), 
			data_y = new ArrayList<Integer>(), 
			data_z = new ArrayList<Integer>();
	private DbAdapter dbAdapter; 
	
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
    	startSession = (Button) findViewById(R.id.UI3button1);
    	stopSession = (Button) findViewById(R.id.UI3button2);
    	progressBarX = (VerticalProgressBar) findViewById(R.id.UI2verticalBarX);
    	progressBarY = (VerticalProgressBar) findViewById(R.id.UI2verticalBarY);
    	progressBarZ = (VerticalProgressBar) findViewById(R.id.UI2verticalBarZ);
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
    	
    	/**** STOPPA LA REGISTRAZIONE ****/
    	stopSession.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(inizializzato){
					
					sensorManager.unregisterListener(mySensorEventListener);
					
					// apro la connessione al db
			    	dbAdapter = new DbAdapter(v.getContext());
			    	dbAdapter.open();
			    	
			    	// inserisco i dati della sessione nel database
			    	//TODO: gestire nome vuoto se non viene inserito un nome per la sessione... con un messaggio che richiede l'inserimento del nome
					dbAdapter.createSession( nameSession.getText().toString(), R.drawable.ic_launcher, 1, 1, 1, 48000, "25/01/2014", "14/04/2014", data_x.toString(), data_y.toString(), data_z.toString() );
					
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
	    			inizializzato = true;
	    			
	    		} else {
	    			
	    			float deltaX = Math.abs(oldX - x);
	    			float deltaY = Math.abs(oldY - y);
	    			float deltaZ = Math.abs(oldZ - z);
	    			
	    			if (deltaX < rumore)
	    				deltaX = (float) 0.0;
	    			else
	    				data_x.add((int)deltaX);
	    			
	    			if (deltaY < rumore)
	    				deltaY = (float) 0.0;
	    			else
	    				data_y.add((int)deltaY);
	    			
	    			if (deltaZ < rumore)
	    				deltaZ = (float) 0.0;
	    			else
	    				data_z.add((int)deltaZ);
	    			
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