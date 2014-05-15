package com.acceleraudio.service;

import com.acceleraudio.activity.RecordActivity;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

public class RecordTrack extends IntentService{
	
	public static final String NOTIFICATION = "com.acceleraudio.service.recordtrack";
	public static final String NOTIFICATION_RECORD = "com.acceleraudio.service.recordtrack.record";
	public static final String NOTIFICATION_STOP = "com.acceleraudio.service.recordtrack.stop";
	public static final String SAMPLE_N_DATA = "recordtrack.sample_n_data";
	public static final String SENSOR_DELAY = "recordtrack.sensorDelay";
	public static final String AXIS_X_DATA = "recordtrack.axis_x_data";
	public static final String AXIS_Y_DATA = "recordtrack.axis_y_data";
	public static final String AXIS_Z_DATA = "recordtrack.axis_z_data";
	public static final String TIME_REMAINING = "recordtrack.timeRemaining";
	private boolean initialized, isRecording;
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private final float rumore = 1.2f;
	private float oldX, oldY, oldZ;
	Intent intent = new Intent(NOTIFICATION_RECORD);
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
	    			
	    			initialized = true;
	    			
	    		} else {
	    			
	    			float deltaX = Math.abs(oldX - x);
	    			float deltaY = Math.abs(oldY - y);
	    			float deltaZ = Math.abs(oldZ - z);
	    			
	    			if (deltaX < rumore)
	    				deltaX = (float) 0.0;
	    			else
	    			{
	    				synchronized (this) {
							RecordActivity.sample++;
							RecordActivity.x = (int) deltaX;
							RecordActivity.data_x.add(deltaX);
							RecordActivity.updateSample();
						}
	    			}
	    			
	    			if (deltaY < rumore)
	    				deltaY = (float) 0.0;
	    			else
	    			{
	    				synchronized (this) {
							RecordActivity.sample++;
							RecordActivity.y = (int) deltaY;
							RecordActivity.data_y.add(deltaY);
							RecordActivity.updateSample();
						}
	    			}
	    			
	    			if (deltaZ < rumore)
	    				deltaZ = (float) 0.0;
	    			else
	    			{
	    				synchronized (this) {
							RecordActivity.sample++;
							RecordActivity.z = (int) deltaZ;
							RecordActivity.data_z.add(deltaZ);
							RecordActivity.updateSample();
						}
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
    	initialized = false;
    	isRecording = true;
    	intent.getIntExtra(SENSOR_DELAY, SensorManager.SENSOR_DELAY_NORMAL);
   	  
    	sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    	accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    	sensorManager.registerListener(mySensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    	
    	long endTime = System.currentTimeMillis() + RecordActivity.remaining_time;
    	while(isRecording && System.currentTimeMillis() < endTime)
        {
    		synchronized (this) {
    			try {
                    wait(endTime - System.currentTimeMillis());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
			}      
        }
    	try {
			sensorManager.unregisterListener(mySensorEventListener);
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
            
    	initialized = true;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "start recording", Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent,flags,startId);
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	isRecording = false;
    	synchronized (this) {
			this.notify();
		}
		Toast.makeText(this, "stop recording", Toast.LENGTH_SHORT).show();
    	
    }
    
}