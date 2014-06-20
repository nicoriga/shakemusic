package com.acceleraudio.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import android.app.ProgressDialog;
import android.media.AudioFormat;
import android.media.AudioTrack;

/**
 * @author Nicola Rigato
 * @author Luca Del Salvador
 * @author Marco Tessari
 * @author Gruppo: Malunix
 *
 * contiene algoritmo di upsampling e metodo per la calcolare la durata
 */
public class MusicUpsampling
{
	// serve per bloccare la scrittura delle note nel file
	public static boolean isRunning;
	
	/**
	 * scrive audio PCM nel file di output
	 * 
	 * @param out file su cui viene scritto audio PCM
	 * @param sound_rate sound rate espresso in Hz
	 * @param upsampling la quantità di upsampling scelta
	 * @param sample array di campioni
	 * @param pd progressDialog su cui mostrare il progresso di salvataggio
	 * @return 
	 * @throws IOException nel caso sussistesse un problema nella scrittura sul file
	 */
	public static int note(OutputStream out, int sound_rate, int upsampling, int[] sample, ProgressDialog pd, RandomAccessFile raf) throws IOException
	{
		// setta dimensione buffer
		int buffsize = upsampling + AudioTrack.getMinBufferSize(sound_rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        short sampleS[] = new short[buffsize];
        int musicSize = buffsize * sample.length;
        int amp = 10000;
        double twophi = Math.PI*2; // 2pi grego
        double fr, increment, angle = 0.0;
        ByteBuffer byteBuff = ByteBuffer.allocate(buffsize*2);
        isRunning = true;
        
        for(int x=0; x < sample.length && isRunning; x++){
        	fr =  362 + (Math.abs(sample[x])*100); // modifica la frequenza con i campioni prelevati dall'accelerometro
        	increment = twophi*fr/sound_rate;
        	for(int i=0; i < buffsize; i++){
        		sampleS[i] = (short) (amp*Math.sin(angle));
        		angle += increment;
        		byteBuff.putShort(Short.reverseBytes(sampleS[i]));
        	}
        	raf.write(byteBuff.array());
        	byteBuff.clear();
        	if(pd!=null)pd.setProgress(x);
        }
        return musicSize;
	}
	
	/**
	 * restituisce la durata della musica in millisecondi
	 * 
	 * @param upsampling quantita di upsampling scelta
	 * @param num_sample numero di campioni
	 * @param sound_rate sound rate espresso in Hz
	 * @return durata della riproduzione in millisecondi
	 */
	public static long duration(int upsampling, int num_sample, int sound_rate)
	{
		int buffsize = AudioTrack.getMinBufferSize(sound_rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
		buffsize += upsampling;
		double length = ((double)(num_sample * buffsize)/sound_rate)*1000;
		return (long)length;
		
	}
	
}