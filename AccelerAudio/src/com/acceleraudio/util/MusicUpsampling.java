package com.acceleraudio.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import android.media.AudioFormat;
import android.media.AudioTrack;

public class MusicUpsampling
{
	public final static int NOTE = 1;
	public final static int LINEAR = 2;
	
	/**
	 * scrive audio PCM nel file di output usando Upsampling: note 
	 * 
	 * @param out file su cui viene scritto audio PCM
	 * @param sample_rate sample rate espresso in Hz
	 * @param sample array di campioni
	 * @return 
	 * @throws IOException nel caso sussiste un problema nella scrittura sul file
	 */
	public static int note(OutputStream out, int sample_rate, int[] sample) throws IOException
	{
		// setta dimensione buffer
		int buffsize = AudioTrack.getMinBufferSize(sample_rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
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
        		ph += twoph*fr/sample_rate;
        		byteBuff.putShort(Short.reverseBytes(sampleS[i]));
        	}
        	out.write(byteBuff.array());
        	byteBuff.clear();
        }
        return musicSize;
	}
	
	/**
	 * restituisce la durata della musica in millisecondi
	 * 
	 * @param upsampling quantita di upsampling scelta
	 * @param num_sample numero di campioni
	 * @param sample_rate sample rate espresso in Hz
	 * @return durata della riproduzione in millisecondi
	 */
	public static long duration(int upsampling, int num_sample, int sample_rate)
	{
		int buffsize = AudioTrack.getMinBufferSize(sample_rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
		
		switch(upsampling)
		{
			case NOTE: 
				return ((num_sample* buffsize)/sample_rate)*1000;
			case LINEAR: 
				return (num_sample* buffsize)/sample_rate;
			default:
				return 0;
		}
		
	}
	
}