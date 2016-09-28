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

/**
 * @author Nicola Rigato
 * @author Luca Del Salvador
 * @author Marco Tessari
 * @author Gruppo: Malunix
 *
 * servizio per la riproduzione dei campioni dell'acceleremetro
 * effettua la creazione delle note da riprodurre in runtime
 */
public class PlayerTrack extends IntentService{
	
	public static final String NOTIFICATION = "com.acceleraudio.service.playertrack";
	public static final String COMMAND = "com.acceleraudio.service.playertrack.command";
	public static final int PLAY_MUSIC = 0;
	public static final int PAUSE_MUSIC = 1;
	public static final int STOP_MUSIC = 2;
	public static final int SOUND_RATE_48000 = 48000;
	
	private int sound_rate;
	private boolean isRunning, inizialized ;
	private int[] sample;
	int x, upsampling;
	private long duration;
	private AudioTrackTimer audioTrackTimer;
	
	/*** ricevitore dei comandi di riproduzione ***/
	private BroadcastReceiver receiver = new BroadcastReceiver() {
    	
    	@Override
        public void onReceive(Context context, Intent intent) {
    		Bundle bundle = intent.getExtras();
    		if (bundle != null) {
    			if(intent.hasExtra(COMMAND)){
    				int command = bundle.getInt(PlayerTrack.COMMAND);
    					if(command == PAUSE_MUSIC)
    						pause();
    					else if(command == PLAY_MUSIC)
    						resume();
    					else if(command == STOP_MUSIC)
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
		sound_rate = intent.getIntExtra(PlayerActivity.SOUND_RATE, SOUND_RATE_48000);
		upsampling = intent.getIntExtra(PlayerActivity.UPSAMPLING, 0);
		
		duration = MusicUpsampling.duration(upsampling, sample.length, sound_rate);
		Log.w("PlayerTrack", "duration " +duration);
		
		isRunning = true;
		
		// setta dimensione buffer
        final int buffsize = AudioTrackTimer.getMinBufferSize(sound_rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        
        // imposta la quantita di upsampling
        int sizeBuff = buffsize + upsampling;
        
    	// crea oggetto audiotrack
        audioTrackTimer = new AudioTrackTimer(AudioManager.STREAM_MUSIC, sound_rate, AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT, sizeBuff, AudioTrackTimer.MODE_STREAM, upsampling, sample.length);
        
        // avvia l'audio
        audioTrackTimer.play();
        audioTrackTimer.stop();
        Intent intentS = new Intent(PlayerActivity.NOTIFICATION);
		intentS.putExtra(PlayerTrack.COMMAND, PlayerTrack.PLAY_MUSIC);
		sendBroadcast(intentS);
		
		// array dei campioni in riproduzione
		short samples[] = new short[sizeBuff];
		
        int amp = 10000;
        double twophi = Math.PI*2; // 2pi grego
        double fr, increment, angle = 0.0;
        
        inizialized = true;
        
        // loop musicale
        while(isRunning){
        	// nel caso venga messa in pausa la riproduzione, viene fermato anche algoritmo di creazione campioni
        	if(audioTrackTimer.getPlayState() == AudioTrackTimer.PLAYSTATE_PAUSED)
				try {
					synchronized (this) {
						this.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	// creazione campioni
        	fr =  362 + (Math.abs(sample[x])*100); // modifica la frequenza con i campioni prelevati dall'accelerometro
        	increment = twophi*fr/sound_rate;
        	for(int i=0; i < sizeBuff; i++){
        		samples[i] = (short) (amp*Math.sin(angle));
        		angle += increment;
        	}
        	audioTrackTimer.write(samples, 0, sizeBuff);
        	x++;
        	
        	// se arrivato all'ultimo campione
        	if(x == sample.length) 
        	{
        		// mette il puntatore al primo campione
    			x = 0;
    			
    			/* questo messaggio compare un attimo prima della fine della musica
    			 * perchè si entra in questo if prima che audioTrack finisca di riprodurre la musica
    			*/
    			Log.w("PlayerTrack", "restart loop " +sample.length);
        	}
        }	
        
        audioTrackTimer.stop();
        audioTrackTimer.release();
        inizialized = false;
        		
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Toast.makeText(this, "Play started", Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent,flags,startId);
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	unregisterReceiver(receiver);
//    	Toast.makeText(this, "Play stop", Toast.LENGTH_SHORT).show();
    	
    }
    
    /*** riprende la riproduzione della musica ***/
    public void resume()
    {
    	synchronized (this) {
			this.notify();
			if(inizialized)audioTrackTimer.play();
		}
    }
    
    /*** mette in pausa la riproduzione ***/
    public void pause()
    {
    	audioTrackTimer.pause(x);
    }
    
    /*** stoppa la riproduzione della musica e induce l'autochiusura del servizio ***/
    public void stop()
    {
    	synchronized (this) {
			this.notify();
			isRunning = false;
		}
    }
}