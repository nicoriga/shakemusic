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
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class PlayerTrack extends IntentService{
	
	public static final String NOTIFICATION = "com.acceleraudio.service.playertrack";
	public static final String DURATION = "com.acceleraudio.service.playertrack.duration";
	public static final String PAUSE = "com.acceleraudio.service.playertrack.pause";
	public static final String PLAY = "com.acceleraudio.service.playertrack.play";
	public static final String STOP = "com.acceleraudio.service.playertrack.stop";
	
	private Runnable runPlayer = new Runnable() {
        @Override
        public void run() {
	    	switch(upsampling){
		    	case MusicUpsampling.NOTE:
		    		// loop musicale con note
		    		
		    		int sizeBuff = buffsize;
		    		
		    		short samples1[] = new short[sizeBuff];
		    		
			        int amp = 10000;
			        double twophi = Math.PI*2; // 2pi grego
			        double fr; // frequenza
			        double phi = 0.0; // pi greco
			        
			        while(isRunning){
			        	// modifica la frequenza con i campioni prelevati dall'accelerometro
			        	fr =  262 + (Math.abs(sample[x])*10);
			        	for(int i=0; i < sizeBuff; i++){
			        		samples1[i] = (short) (amp*Math.sin(phi));
			        		phi += twophi*fr/sound_rate;
			        	}
			        	audioTrack.write(samples1, 0, sizeBuff);
			        	x++;
			        	if(x == sample.length) 
			        	{
			        			x = 0;
			        			Log.w("PlayerTrack", "restart loop " +sample.length);
			        	}
			        	
			        }	
		    		break;
		    		
		    	case MusicUpsampling.LINEAR: 
		    		// loop musicale con upscaling lineare
		    		short samples2[] = new short[buffsize+2000];
		    		
			        while(isRunning){
			        	
						for (int i = 0; i < buffsize+2000; i++) {
							samples2[i] = (short) (sample[x]*20000*Math.sin((Math.PI*2*600)/44100));
						}
						audioTrack.write(samples2, 0, buffsize+2000);
						
						x++;
			        	if(x == sample.length) x = 0;
			        }
			        break;
		    	default:
		    		break;
            }
        }
    };
	private Looper looper;
    private int sound_rate;
	private boolean isRunning ;
	private int[] sample;
	int x, upsampling; // x: indice del sample in riproduzione
	AudioTrack audioTrack;
	final int buffsize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
	private BroadcastReceiver receiver = new BroadcastReceiver() {
    	
    	@Override
        public void onReceive(Context context, Intent intent) {
    		Bundle bundle = intent.getExtras();
    		if (bundle != null) {
    			if(intent.hasExtra(PAUSE))
    				if(bundle.getInt(PlayerTrack.PAUSE) == 1)
    					pauseMusic();
    			if(intent.hasExtra(PLAY))
					if(bundle.getInt(PlayerTrack.PLAY) == 1)
						playMusic();
    			if(intent.hasExtra(STOP))
					if(bundle.getInt(PlayerTrack.STOP) == 1)
						stopMusic();
    		}
        }
    };
    
    public PlayerTrack (){
    	super("PlayerTrack");
    }
    
    @SuppressWarnings("static-access")
	@Override
    protected void onHandleIntent(Intent intent) {
    	registerReceiver(receiver, new IntentFilter(PlayerActivity.NOTIFICATION));
    	
		sample = intent.getIntArrayExtra(PlayerActivity.ACC_DATA);
		sound_rate = intent.getIntExtra(PlayerActivity.SOUND_RATE, 44100);
		upsampling = intent.getIntExtra(PlayerActivity.UPSAMPLING, 1);
		isRunning = true;
		
		Log.w("PlayerTrack", "sample: " +sample.length);
		
		//crea oggetto audiotrack
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT, buffsize, AudioTrack.MODE_STREAM);
        
        // avvia l'audio
        playMusic();
        
        // serve per mantenere il servizio attivo
        looper =  Looper.myLooper();
        Looper.loop();
			        
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

    /*** AVVIA LA RIPRODUZIONE AUDIO ***/
    public void playMusic(){
    	audioTrack.play();
    	isRunning = true;
    	// crea un nuovo thread in runTime che genera la musica dai sample
		new Thread(runPlayer, "Player Thread").start();
//    	t.start();
    }

    /*** METTE IN PAUSA LA RIPRODUZIONE AUDIO ***/
    public void pauseMusic(){
    	// settando isRunning false il thread finisce il suo lavoro e si autodistrugge
    	isRunning = false;
    	audioTrack.pause();
    }
    
    /*** STOPPA LA RIPRODUZIONE AUDIO ***/
    public void stopMusic(){
    	audioTrack.flush();
    	audioTrack.stop();
        audioTrack.release();
        
        isRunning = false;
        // sblocca il loop del servizio che si autochiude
    	looper.quit();
    }
}