package com.acceleraudio.service;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class RecordTrack extends IntentService{
	
	public static final String NOTIFICATION = "com.acceleraudio.service.recordtrack";
	private boolean initialized;
	private SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	private Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	private final float rumore = (float) 1.0;
	private float oldX, oldY, oldZ;
	private ArrayList<Float> 
			data_x = new ArrayList<Float>(), 
			data_y = new ArrayList<Float>(), 
			data_z = new ArrayList<Float>();
	private int sample;
    
    public RecordTrack (){
    	super("RecordTrack");
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
    	sensorManager.registerListener(mySensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    	initialized = false;
    	sample = 0;
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	Intent intent = new Intent(NOTIFICATION);
        // TODO: inviare i dati registrati
    	//intent.putExtra(FourthActivity.MUSIC_CURSOR, x);
        sendBroadcast(intent);
    }
    
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
	    			
	    			/*
	    			ThirdActivity.progressBarX.setProgress(0);
	    			progressBarY.setProgress(0);
	    			progressBarZ.setProgress(0);
	    			
	    			rec_sample.setText("" + sample);
	    			*/
	    			
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
	    				//rec_sample.setText("" + sample);
	    			}
	    			
	    			if (deltaY < rumore)
	    				deltaY = (float) 0.0;
	    			else
	    			{
	    				data_y.add(deltaY);
	    				sample++;
	    				//rec_sample.setText("" + sample);
	    			}
	    			
	    			if (deltaZ < rumore)
	    				deltaZ = (float) 0.0;
	    			else
	    			{
	    				data_z.add(deltaZ);
	    				sample++;
	    				//rec_sample.setText("" + sample);
	    			}
	    			
	    			oldX = x;
	    			oldY = y;
	    			oldZ = z;
	    			
	    			/*
	    			progressBarX.setProgress((int)deltaX);
	    			progressBarY.setProgress((int)deltaY);
	    			progressBarZ.setProgress((int)deltaZ);	
	    			*/
	    		}
        	}
        }
        
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }
    };
}