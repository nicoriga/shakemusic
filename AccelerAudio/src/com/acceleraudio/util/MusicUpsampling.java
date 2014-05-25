package com.acceleraudio.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import android.media.AudioFormat;
import android.media.AudioTrack;


public class MusicUpsampling
{
	public static short[] note(int sound_rate, int[] sample)
	{
		// setta dimensione buffer
        int buffsize = AudioTrack.getMinBufferSize(sound_rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        int musicSize = buffsize * sample.length;
        short sampleS[] = new short[musicSize];
        int amp = 10000;
        double twoph = Math.PI*2; // 2pi grego
        double fr; // frequenza
        double ph = 0.0; // pi greco
        int y = 0;
        
        for(int x=0; x < sample.length; x++){
        	// modifica la frequenza con i campioni prelevati dall'accelerometro
        	fr =  262 + (Math.abs(sample[x])*10);
        	for(int i=0; i < buffsize; i++){
        		sampleS[y] = (short) (amp*Math.sin(ph));
        		ph += twoph*fr/sound_rate;
        		y++;
        	}
        }
        
        return sampleS;
	}
	
	public static int note(OutputStream out, int sound_rate, int[] sample) throws IOException
	{
		// setta dimensione buffer
        int buffsize = AudioTrack.getMinBufferSize(sound_rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        short sampleS[] = new short[buffsize];
        int musicSize = buffsize * sample.length;
        int amp = 10000;
        double twoph = Math.PI*2; // 2pi grego
        double fr; // frequenza
        double ph = 0.0; // pi greco
        ByteBuffer byteBuff = ByteBuffer.allocate(buffsize*2);
        
        for(int x=0; x < sample.length; x++){
        	// modifica la frequenza con i campioni prelevati dall'accelerometro
        	fr =  262 + (Math.abs(sample[x])*10);
        	for(int i=0; i < buffsize; i++){
        		sampleS[i] = (short) (amp*Math.sin(ph));
        		ph += twoph*fr/sound_rate;
        		byteBuff.putShort(Short.reverseBytes(sampleS[i]));
        	}
        	out.write(byteBuff.array());
        	byteBuff.clear();
        }
        return musicSize;
	}
	
}