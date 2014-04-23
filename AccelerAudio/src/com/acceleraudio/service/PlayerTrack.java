package com.acceleraudio.service;

import com.acceleraudio.activity.FourthActivity;

import android.app.IntentService;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.widget.Toast;

public class PlayerTrack extends IntentService{
	
	public static final String NOTIFICATION = "com.acceleraudio.service.playertrack";
	private Thread t;
	private int sound_rate;
	private boolean isRunning ;
	private Integer[] sample = {10,500,1000,-1500,-1800,2100,2200};
	int x;
    
    public PlayerTrack (){
    	super("PlayerTrack");
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
    	x = intent.getIntExtra(FourthActivity.MUSIC_CURSOR, 0);
    	sound_rate = intent.getIntExtra(FourthActivity.SOUND_RATE, 44100);
    	isRunning = true;
    	
    	t = new Thread() {
            public void run() {
            	// setta la priorità massia del thread
                setPriority(Thread.MAX_PRIORITY);
                
		    	 // setta dimensione buffer
		        int buffsize = AudioTrack.getMinBufferSize(sound_rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
		        // crea oggetto audiotrack
		        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sound_rate, AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT, buffsize, AudioTrack.MODE_STREAM);
		
		        short samples[] = new short[buffsize];
		        int amp = 4000;
		        double twopi = 8.*Math.atan(1.);
		        double fr = 440.f;
		        double ph = 0.0;
		
		        // start audio
		        audioTrack.play();
		  
		        // synthesis loop
		        while(isRunning){
		        	amp = amp + sample[x];
		        	fr =  440 + 440*sample[x];
		        	for(int i=0; i < buffsize; i++){
		        		samples[i] = (short) (amp*Math.sin(ph));
		        		ph += twopi*fr/sound_rate;
		        	}
		        	audioTrack.write(samples, 0, buffsize);
		        	
		        	if(x == sample.length) x = 0;
		        }
		        
		        audioTrack.stop();
		        audioTrack.release();
            }
        };
        t.run();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent,flags,startId);
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	isRunning = false;
    	
    	try {
    		t.join();
    	} 
    	catch (InterruptedException e) {
    		e.printStackTrace();
    	}
    	
    	Toast.makeText(this, "service stop", Toast.LENGTH_SHORT).show();
    	
    	Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(FourthActivity.MUSIC_CURSOR, x);
        sendBroadcast(intent);
    }
}