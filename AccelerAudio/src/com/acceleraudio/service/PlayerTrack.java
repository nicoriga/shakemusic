package com.acceleraudio.service;

import com.acceleraudio.activity.PlayerActivity;
import com.acceleraudio.util.MusicUpsampling;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class PlayerTrack extends IntentService{
	
	public static final String NOTIFICATION = "com.acceleraudio.service.playertrack";
	public static final String DURATION = "com.acceleraudio.service.playertrack.duration";
	public static final String PAUSE = "com.acceleraudio.service.playertrack.pause";
	public static final String PLAY = "com.acceleraudio.service.playertrack.play";
	public static final int PLAY_MUSIC = 1;
	public static final int PAUSE_MUSIC = 1;
//	private Thread t;
	private int sample_rate;
	private boolean isRunning ;
	private int[] sample;
	int x, upsampling;
	private long duration;
	private AudioTrack audioTrack;
	private BroadcastReceiver receiver = new BroadcastReceiver() {
    	
    	@Override
        public void onReceive(Context context, Intent intent) {
    		Bundle bundle = intent.getExtras();
    		if (bundle != null) {
    			if(intent.hasExtra(PAUSE))
    					if(bundle.getInt(PlayerTrack.PAUSE) == PAUSE_MUSIC)
    					{
    						Intent intentPause = new Intent(NOTIFICATION);
			        		intentPause.putExtra(PAUSE, PAUSE_MUSIC);
					        sendBroadcast(intentPause);
    						audioTrack.pause();
    					}
    			if(intent.hasExtra(PLAY))
					if(bundle.getInt(PlayerTrack.PLAY) == PLAY_MUSIC)
						resume();
    		}
        }
    };
    
    public PlayerTrack (){
    	super("PlayerTrack");
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
    	registerReceiver(receiver, new IntentFilter(PlayerActivity.NOTIFICATION));
    	
		sample = intent.getIntArrayExtra(PlayerActivity.ACC_DATA);
		Log.w("PlayerTrack", "sample: " +sample.length);
		sample_rate = intent.getIntExtra(PlayerActivity.SOUND_RATE, 44100);
		upsampling = intent.getIntExtra(PlayerActivity.UPSAMPLING, 1);
		
		duration = MusicUpsampling.duration(upsampling, sample.length, sample_rate);
		Log.w("PlayerTrack", "duration " +duration);
		
		isRunning = true;
		
		 // setta dimensione buffer
        final int buffsize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        
    	// crea oggetto audiotrack
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT, buffsize, AudioTrack.MODE_STREAM);
        
        // avvia l'audio
        audioTrack.play();
  
		// loop musicale 
		
		int sizeBuff = buffsize + upsampling;
		
		short samples1[] = new short[sizeBuff];
		
        int amp = 10000;
        double twophi = Math.PI*2; // 2pi grego
        double fr; // frequenza
        double phi = 0.0; // pi greco
        
        Intent intentPlay = new Intent(NOTIFICATION);
        intentPlay.putExtra(DURATION, duration);
        intentPlay.putExtra(PLAY, PLAY_MUSIC);
        sendBroadcast(intentPlay);
        
        while(isRunning){
        	if(audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED)
				try {
					synchronized (this) {
						this.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	// modifica la frequenza con i campioni prelevati dall'accelerometro
        	fr =  262 + (Math.abs(sample[x])*10);
        	for(int i=0; i < sizeBuff; i++){
        		samples1[i] = (short) (amp*Math.sin(phi));
        		phi += twophi*fr/sample_rate;
        	}
        	audioTrack.write(samples1, 0, sizeBuff);
        	x++;
        	if(x == sample.length) 
        		{
        		// stoppo e riavvio il coutdowntimer
	        		Intent intentPause = new Intent(NOTIFICATION);
	        		intentPause.putExtra(PAUSE, PAUSE_MUSIC);
			        sendBroadcast(intentPause);
        			x = 0;
        			Log.w("PlayerTrack", "restart loop " +sample.length);
        			intentPlay.putExtra(DURATION, duration);
        			intentPlay.putExtra(PLAY, PLAY_MUSIC);
			        sendBroadcast(intentPlay);
        		}
        	
        }	
        		
        
        audioTrack.stop();
        audioTrack.release();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Play started", Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent,flags,startId);
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	unregisterReceiver(receiver);
    	
    	isRunning = false;
    	Toast.makeText(this, "Play stop", Toast.LENGTH_SHORT).show();
    	
    }
    
    public void resume()
    {
    	synchronized (this) {
			this.notify();
			audioTrack.play();
			
			Intent intentPlay = new Intent(NOTIFICATION);
			intentPlay.putExtra(PLAY, PLAY_MUSIC);
	        sendBroadcast(intentPlay);
		}
    }
}