package com.acceleraudio.util;

import android.media.AudioFormat;
import android.media.AudioTrack;


public class MusicUpsampling{
	
	public static short[] note(int sound_rate, int[] sample)
	{
		// setta dimensione buffer
        int buffsize = AudioTrack.getMinBufferSize(sound_rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        int musicSize = buffsize * sample.length;
        short sampleS[] = new short[musicSize];
        int amp = 20000;
        double twoph = Math.PI*2; // 2pi grego
        double fr; // frequenza
        double ph = 0.0; // pi greco
        int y = 0;
        
        for(int x=0; x < sample.length; x++){
        	// modifica la frequenza con i campioni prelevati dall'accelerometro
        	fr =  262 + sample[x];
        	for(int i=0; i < buffsize; i++){
        		sampleS[y] = (short) (amp*Math.sin(ph));
        		ph += twoph*fr/sound_rate;
        		y++;
        	}
        }
        
        return sampleS;
	}
	
}