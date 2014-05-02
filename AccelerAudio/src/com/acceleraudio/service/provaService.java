package com.acceleraudio.service;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.widget.Toast;

public class provaService extends IntentService{
	
	public static final String NOTIFICATION_RECORD = "com.acceleraudio.service.recordtrack.record";
	public static final String NOTIFICATION_STOP = "com.acceleraudio.service.recordtrack.stop";
	public static final String SAMPLE_N_DATA = "recordtrack.sample_n_data";
	public static final String AXIS_X_DATA = "recordtrack.axis_x_data";
	public static final String AXIS_Y_DATA = "recordtrack.axis_y_data";
	public static final String AXIS_Z_DATA = "recordtrack.axis_z_data";
	
	private SensorManager sensorManager;
    
    public provaService (){
    	super("RecordTrack");
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
    	sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent,flags,startId);
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	Toast.makeText(this, "service stop", Toast.LENGTH_SHORT).show();
    }
    
}