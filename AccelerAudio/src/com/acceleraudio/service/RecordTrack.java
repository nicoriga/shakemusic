package com.acceleraudio.service;

import com.acceleraudio.activity.RecordActivity;
import com.malunix.acceleraudio.R;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

/**
 * @author Nicola Rigato
 * @author Luca Del Salvador
 * @author Marco Tessari
 * @author Gruppo: Malunix
 *
 * servizio per registrare i campioni dall'accelerometro
 */
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
	public static final String NOISE = "recordtrack.noise";
	private boolean initialized, isRecording;
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private float noise = 1.0f;
	private float oldX, oldY, oldZ;
	Intent intent = new Intent(NOTIFICATION_RECORD);
	
	/*** gestore evento registrazione dati accelerometro ***/
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
	    			
	    			float deltaX = oldX - x;
	    			float deltaY = oldY - y;
	    			float deltaZ = oldZ - z;
	    			
	    			// verifico se il nuovo valore supera il rumore
	    			if (Math.abs(deltaX) < noise)
	    			{
	    				deltaX = (float) 0.0;
	    				RecordActivity.x = (int) deltaX;
	    				RecordActivity.updateSample();
	    			}
	    			else
	    			{
	    				synchronized (this) {
							RecordActivity.sample++;
							RecordActivity.x = (int) Math.abs(deltaX);
							RecordActivity.data_x.add(deltaX); // aggiungo il valore registrato
							RecordActivity.updateSample();
						}
	    			}
	    			
	    			// verifico se il nuovo valore supera il rumore
	    			if (Math.abs(deltaY) < noise)
	    			{
	    				deltaY = (float) 0.0;
	    				RecordActivity.y = (int) deltaY;
	    				RecordActivity.updateSample();
	    			}
	    			else
	    			{
	    				synchronized (this) {
							RecordActivity.sample++;
							RecordActivity.y = (int) Math.abs(deltaY);
							RecordActivity.data_y.add(deltaY); // aggiungo il valore registrato
							RecordActivity.updateSample();
						}
	    			}
	    			
	    			// verifico se il nuovo valore supera il rumore
	    			if (Math.abs(deltaZ) < noise)
	    			{
	    				deltaZ = (float) 0.0;
	    				RecordActivity.z = (int) deltaZ;
	    				RecordActivity.updateSample();
	    			}
	    			else
	    			{
	    				synchronized (this) {
							RecordActivity.sample++;
							RecordActivity.z = (int) Math.abs(deltaZ);
							RecordActivity.data_z.add(deltaZ); // aggiungo il valore registrato
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
        }
    };

    public RecordTrack (){
    	super("RecordTrack");
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
    	try {
			initialized = false;
			isRecording = true;
			noise = intent.getFloatExtra(NOISE, 1.0f);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
   	  
    	try {
			sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			
			// verifica presenza accelerometro
			if (accelerometer != null) 
			{
				sensorManager.registerListener(mySensorEventListener, accelerometer, intent.getIntExtra(SENSOR_DELAY, SensorManager.SENSOR_DELAY_NORMAL));
				
				/* imposto che il servizio di registrazione si autochiuda allo scadere del tempo prefissato
				 * nelle preferenze
				 * */
				long endTime = System.currentTimeMillis() + RecordActivity.remaining_time;
				while (isRecording && System.currentTimeMillis() < endTime) {
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
			else
				initialized = false;
		} catch (Exception e) {
			Toast.makeText(this, getString(R.string.error_no_accelerometer), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			initialized = false;
		}
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, getString(R.string.notify_start_recording), Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent,flags,startId);
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	isRecording = false;
    	synchronized (this) {
			this.notify();
		}
		Toast.makeText(this, getString(R.string.notify_stop_recording), Toast.LENGTH_SHORT).show();
    	
    }
    
}