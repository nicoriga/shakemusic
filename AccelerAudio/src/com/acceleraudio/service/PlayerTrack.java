package com.acceleraudio.service;

import com.acceleraudio.activity.PlayerActivity;
import android.app.IntentService;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import android.widget.Toast;

public class PlayerTrack extends IntentService{
	
	public static final String NOTIFICATION = "com.acceleraudio.service.playertrack";
	private Thread t;
	private int sound_rate;
	private boolean isRunning ;
	private int[] sample = {10};
	int x, upsampling;
    
    public PlayerTrack (){
    	super("PlayerTrack");
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
    	try {
    		
    		sample = intent.getIntArrayExtra(PlayerActivity.ACC_DATA);
			Log.w("PlayerTrack", "sample: " +sample.length);
			x = intent.getIntExtra(PlayerActivity.MUSIC_CURSOR, 0);
			sound_rate = intent.getIntExtra(PlayerActivity.SOUND_RATE, 44100);
			upsampling = intent.getIntExtra(PlayerActivity.UPSAMPLING, 1);
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
			        int amp = 10000;
			        double twoph = Math.PI*2; // 2pi grego
			        double fr; // frequenza
			        double ph = 0.0; // pi greco
			
			        // avvia l'audio
			        audioTrack.play();
			  
			        switch(upsampling){
			        	case 1:
			        		// loop musicale con note
					        while(isRunning){
					        	// modifica la frequenza con i campioni prelevati dall'accelerometro
					        	fr =  262 + sample[x];
					        	for(int i=0; i < buffsize; i++){
					        		samples[i] = (short) (amp*Math.sin(ph));
					        		ph += twoph*fr/sound_rate;
					        	}
					        	audioTrack.write(samples, 0, buffsize);
					        	x++;
					        	if(x == sample.length) x = 0;
					        	
					        }	
			        		break;
			        	case 2: 
			        		// loop musicale con upscaling lineare
					        while(isRunning){
					        	for (int z = 0; z < 10000; z++) {
									// modifica la frequenza con i campioni prelevati dall'accelerometro
									for (int i = 0; i < buffsize; i++) {
										samples[i] = (short) (sample[x+i]*100);
									}
									audioTrack.write(samples, 0, buffsize);
								}
								x++;
					        	if(x == sample.length) x = 0;
					        }
					        break;
			        	default:
			        		break;
			        }
			        
			        audioTrack.stop();
			        audioTrack.release();
			    }
			};
			t.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Play started", Toast.LENGTH_SHORT).show();
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
    	
    	Toast.makeText(this, "Play stop", Toast.LENGTH_SHORT).show();
    	
    	Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(PlayerActivity.MUSIC_CURSOR, x);
        sendBroadcast(intent);
    }

}