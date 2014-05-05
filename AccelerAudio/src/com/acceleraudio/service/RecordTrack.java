package com.acceleraudio.service;

import java.util.ArrayList;

import com.acceleraudio.activity.RecordActivityBeta;

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
	private final float rumore = 0.3f;
	private float oldX, oldY, oldZ;
	private ArrayList<Float> 
			data_x = new ArrayList<Float>(), 
			data_y = new ArrayList<Float>(), 
			data_z = new ArrayList<Float>();
	private Thread t;
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
	    				RecordActivityBeta.sample++;
	    				RecordActivityBeta.x = (int)deltaX;
	    				RecordActivityBeta.data_x.add(deltaX);
	    				RecordActivityBeta.updateSample();
	    			}
	    			
	    			if (deltaY < rumore)
	    				deltaY = (float) 0.0;
	    			else
	    			{
	    				data_y.add(deltaY);

	    				RecordActivityBeta.sample++;
	    				RecordActivityBeta.y = (int)deltaY;
	    				RecordActivityBeta.data_y.add(deltaY);
	    				RecordActivityBeta.updateSample();
	    			}
	    			
	    			if (deltaZ < rumore)
	    				deltaZ = (float) 0.0;
	    			else
	    			{
	    				data_z.add(deltaZ);
	    				
	    				RecordActivityBeta.sample++;
	    				RecordActivityBeta.z = (int)deltaZ;
	    				RecordActivityBeta.data_z.add(deltaZ);
	    				RecordActivityBeta.updateSample();
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
   	  
    	t = new Thread() {
            public void run() {
            	// setta la priorità massia del thread
                setPriority(Thread.MAX_PRIORITY);
    	
		    	sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		    	accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		    	sensorManager.registerListener(mySensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		    	  
		    	while(isRecording){}
            }
    	};
    	t.run();
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
    	sensorManager.unregisterListener(mySensorEventListener);
    	isRecording = false;
    	try {
    		t.join();
    	} 
    	catch (InterruptedException e) {
    		e.printStackTrace();
    	}
    	
    	Toast.makeText(this, "stop recording", Toast.LENGTH_SHORT).show();
    	
    }
    
}