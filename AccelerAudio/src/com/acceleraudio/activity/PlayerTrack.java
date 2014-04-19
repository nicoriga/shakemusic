package com.acceleraudio.activity;

import java.util.ArrayList;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;


public class PlayerTrack {
	
	private Thread t;
	private int sound_rate;
	private boolean isRunning ;
	private ArrayList<Integer> sample;
    
    public PlayerTrack (ArrayList<Integer> s, int rate){
    	sound_rate = rate;
    	sample = s;
    	
    	t = new Thread() {
    		public void run() {
            
    			// set process priority
	            setPriority(Thread.MAX_PRIORITY);
            
	            // set the buffer size
	            int buffsize = AudioTrack.getMinBufferSize(sound_rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
	            // create an audiotrack object
	            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sound_rate, AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT, buffsize, AudioTrack.MODE_STREAM);

	            short samples[] = new short[buffsize];
	            int amp = 10000;
	            double twopi = 8.*Math.atan(1.);
	            double fr = 440.f;
	            double ph = 0.0;

	            // start audio
	            audioTrack.play();

	            // synthesis loop
	            while(isRunning){
	            	fr =  440 + 440*sample.listIterator().next();
	            	for(int i=0; i < buffsize; i++){
	            		samples[i] = (short) (amp*Math.sin(ph));
	            		ph += twopi*fr/sound_rate;
	            	}
	            	audioTrack.write(samples, 0, buffsize);
	            }
	            
	            audioTrack.stop();
	            audioTrack.release();
    		}
    		
    	};
	
    }
    
    public void startMusic(){
    	isRunning = true;
    	t.run();
    }
    
    public void stopMusic(){
    	isRunning = false;
    	try {
    		t.join();
    	} 
    	catch (InterruptedException e) {
    		e.printStackTrace();
    	}
    	
    }
    
}