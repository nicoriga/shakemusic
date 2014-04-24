package com.acceleraudio.service;

import com.acceleraudio.activity.PlayerActivity;
import com.acceleraudio.util.Util;

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
	private int[] sample = {50, 100, 400, 500, 150, 100 , 80};
	private Object[] sampleList;
	int x;
    
    public PlayerTrack (){
    	super("PlayerTrack");
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
    	sampleList = intent.getIntegerArrayListExtra(PlayerActivity.ACC_DATA).toArray();
    	sample = Util.copyArrayInt(sampleList);
    	x = intent.getIntExtra(PlayerActivity.MUSIC_CURSOR, 0);
    	sound_rate = intent.getIntExtra(PlayerActivity.SOUND_RATE, 44100);
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
		        double twoph = Math.PI*2; // 2pi grego
		        double fr = 440.f; // frequenza
		        double ph = 0.0; // pi greco
		
		        // avvia l'audio
		        audioTrack.play();
		  
		        // loop musicale
		        while(isRunning){
		        	//amp = amp;
		        	// TODO: modificare la frequenza con i campioni prelevati dall'accelerometro
		        	fr =  262 + sample[x];
		        	for(int i=0; i < buffsize; i++){
		        		samples[i] = (short) (amp*Math.sin(ph));
		        		ph += twoph*fr/sound_rate;
		        	}
		        	audioTrack.write(samples, 0, buffsize);
		        	x++;
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
        intent.putExtra(PlayerActivity.MUSIC_CURSOR, x);
        sendBroadcast(intent);
    }

}