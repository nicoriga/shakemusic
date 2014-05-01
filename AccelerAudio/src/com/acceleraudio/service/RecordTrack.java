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
	public static final String NOTIFICATION_RECORD = "com.acceleraudio.service.recordtrack.record";
	public static final String NOTIFICATION_STOP = "com.acceleraudio.service.recordtrack.stop";
	public static final String SAMPLE_N_DATA = "recordtrack.sample_n_data";
	public static final String AXIS_X_DATA = "recordtrack.axis_x_data";
	public static final String AXIS_Y_DATA = "recordtrack.axis_y_data";
	public static final String AXIS_Z_DATA = "recordtrack.axis_z_data";
	private boolean initialized;
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private final float rumore = 0.3f;
	private float oldX, oldY, oldZ;
	private ArrayList<Float> 
			data_x = new ArrayList<Float>(), 
			data_y = new ArrayList<Float>(), 
			data_z = new ArrayList<Float>();
	private int sample = 0;
	private Intent intent;
	final SensorEventListener mySensorEventListener = new SensorEventListener() { 
        public void onSensorChanged(SensorEvent event) {
        	intent = new Intent(NOTIFICATION_RECORD);
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
	    				
	    		        intent.putExtra(RecordTrack.AXIS_X_DATA, deltaX);
	    		        intent.putExtra(RecordTrack.SAMPLE_N_DATA, sample);
	    		        sendBroadcast(intent);
	    			}
	    			
	    			if (deltaY < rumore)
	    				deltaY = (float) 0.0;
	    			else
	    			{
	    				data_y.add(deltaY);
	    				sample++;

	    				intent.putExtra(RecordTrack.AXIS_Y_DATA, deltaY);
	    				intent.putExtra(RecordTrack.SAMPLE_N_DATA, sample);
	    		        sendBroadcast(intent);
	    			}
	    			
	    			if (deltaZ < rumore)
	    				deltaZ = (float) 0.0;
	    			else
	    			{
	    				data_z.add(deltaZ);
	    				sample++;
	    				intent.putExtra(RecordTrack.AXIS_Z_DATA, deltaZ);
	    				intent.putExtra(RecordTrack.SAMPLE_N_DATA, sample);
	    		        sendBroadcast(intent);
	    			}
	    			
	    			oldX = x;
	    			oldY = y;
	    			oldZ = z;
	    			
	    		}
        	}
        }
        
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }
    };
    
    public RecordTrack (){
    	super("RecordTrack");
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
    	sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    	accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    	sensorManager.registerListener(mySensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    	initialized = false;
    	sample = 0;
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	sensorManager.unregisterListener(mySensorEventListener);
    	
    	intent = new Intent(NOTIFICATION_STOP);
        // TODO: inviare i dati registrati
    	intent.putExtra(RecordTrack.AXIS_X_DATA, data_x);
    	intent.putExtra(RecordTrack.AXIS_Y_DATA, data_y);
    	intent.putExtra(RecordTrack.AXIS_Z_DATA, data_z);
        sendBroadcast(intent);
    }
    
}