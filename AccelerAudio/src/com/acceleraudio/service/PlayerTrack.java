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
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class PlayerTrack extends IntentService{
	
	public static final String NOTIFICATION = "com.acceleraudio.service.playertrack";
	public static final String DURATION = "com.acceleraudio.service.playertrack.duration";
	public static final String COMMAND = "com.acceleraudio.service.playertrack.command";
	public static final String PLAY = "com.acceleraudio.service.playertrack.play";
	public static final String PAUSE = "com.acceleraudio.service.playertrack.pause";
	public static final String STOP = "com.acceleraudio.service.playertrack.stop";
	public static final String PLAYBACK_POSITION = "com.acceleraudio.service.playertrack.playbackPosition";
	public static final int PLAY_MUSIC = 0;
	public static final int PAUSE_MUSIC = 1;
	public static final int STOP_MUSIC = 2;
//	private Thread t;
	private int sound_rate, playbackHeadPosition;
	private boolean isRunning ;
	private int[] sample;
	int x, upsampling;
	private long duration;
	private AudioTrackTimer audioTrackTimer;
	private BroadcastReceiver receiver = new BroadcastReceiver() {
    	
    	@Override
        public void onReceive(Context context, Intent intent) {
    		Bundle bundle = intent.getExtras();
    		if (bundle != null) {
    			if(intent.hasExtra(COMMAND)){
    				int command = bundle.getInt(PlayerTrack.COMMAND);
    					if(command == PAUSE_MUSIC)
    					{
    						audioTrackTimer.pause(x);
    						playbackHeadPosition = audioTrackTimer.getPlaybackHeadPosition();
//    						Intent intentPause = new Intent(NOTIFICATION);
//			        		intentPause.putExtra(PAUSE, PAUSE_MUSIC);
//			        		intentPause.putExtra(PLAYBACK_POSITION, playbackHeadPosition);
//					        sendBroadcast(intentPause);
    					} else if(command == PLAY_MUSIC){
    						resume();
    					} else if(command == STOP_MUSIC)
							stop();
    			}
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
//		if(sample == null) onDestroy();
		Log.w("PlayerTrack", "sample: " +sample.length);
		sound_rate = intent.getIntExtra(PlayerActivity.SOUND_RATE, 44100);
		upsampling = intent.getIntExtra(PlayerActivity.UPSAMPLING, 1);
		
		duration = MusicUpsampling.duration(upsampling, sample.length, sound_rate);
		Log.w("PlayerTrack", "duration " +duration);
		
		isRunning = true;
		
		 // setta dimensione buffer
        final int buffsize = AudioTrackTimer.getMinBufferSize(sound_rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        
        int sizeBuff = buffsize + upsampling;
        
    	// crea oggetto audiotrack
        audioTrackTimer = new AudioTrackTimer(AudioManager.STREAM_MUSIC, sound_rate, AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT, sizeBuff, AudioTrackTimer.MODE_STREAM, upsampling, sample.length);
        
        // avvia l'audio
        audioTrackTimer.play();
        audioTrackTimer.pause();
		// loop musicale 
		
		short samples1[] = new short[sizeBuff];
		
        int amp = 10000;
        double twophi = Math.PI*2; // 2pi grego
        double fr; // frequenza
        double phi = 0.0; // pi greco
        
//        Intent intentPlay = new Intent(NOTIFICATION);
//        intentPlay.putExtra(DURATION, duration);
//        intentPlay.putExtra(PLAY, PLAY_MUSIC);
//        sendBroadcast(intentPlay);
        
        while(isRunning){
        	if(audioTrackTimer.getPlayState() == AudioTrackTimer.PLAYSTATE_PAUSED)
				try {
					synchronized (this) {
						this.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	// modifica la frequenza con i campioni prelevati dall'accelerometro
        	fr =  362 + (Math.abs(sample[x])*100);
        	for(int i=0; i < sizeBuff; i++){
        		samples1[i] = (short) (amp*Math.sin(phi));
        		phi += twophi*fr/sound_rate;
        	}
        	audioTrackTimer.write(samples1, 0, sizeBuff);
        	x++;
        	if(x == sample.length) 
        		{
        		// stoppo e riavvio il coutdowntimer
//	        		Intent intentPause = new Intent(NOTIFICATION);
//	        		intentPause.putExtra(PAUSE, PAUSE_MUSIC);
//			        sendBroadcast(intentPause);
        			x = 0;
        			Log.w("PlayerTrack", "restart loop " +sample.length);
//        			intentPlay.putExtra(DURATION, duration);
//        			intentPlay.putExtra(PLAY, PLAY_MUSIC);
//			        sendBroadcast(intentPlay);
			        
        		}
        }	
        
        audioTrackTimer.stop();
        audioTrackTimer.release();
        		
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

    	Toast.makeText(this, "Play stop", Toast.LENGTH_SHORT).show();
    	
    }
    
    public void resume()
    {
    	synchronized (this) {
			this.notify();
//			audioTrack.setPlaybackHeadPosition(playbackHeadPosition);
			audioTrackTimer.play();
			
//			Intent intentPlay = new Intent(NOTIFICATION);
//			intentPlay.putExtra(PLAY, PLAY_MUSIC);
//	        sendBroadcast(intentPlay);
		}
    }
    
    public void stop()
    {
    	synchronized (this) {
			this.notify();
			isRunning = false;
		}
    }
}