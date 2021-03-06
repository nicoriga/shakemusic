package com.acceleraudio.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Nicola Rigato
 * @author Luca Del Salvador
 * @author Marco Tessari
 * @author Gruppo: Malunix
 *
 * classe per scrivere intestazione file wav
 */
public class Wav
{	
	private static final int bitsForSample = 16; 
    
	/**
	 * scrive header wav sull'OutputStream passato
	 * 
	 * @param totalAudioLen lunghezza musica espressa in byte
	 * @param soundRate sound rate espresso in Hz
	 * @param channels numero di canali
	 * @param out file su cui scivere
	 * @throws IOException
	 */
	public static void WriteWaveFileHeader(long totalAudioLen, long soundRate, int channels, OutputStream out) throws IOException 
	{
		int byteRate = (int) (channels * soundRate * bitsForSample / 8);
		long totalDataLen = totalAudioLen + 36;
		
		byte[] header = new byte[44];
        
        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // PCM format = 1 
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (soundRate & 0xff);
        header[25] = (byte) ((soundRate >> 8) & 0xff);
        header[26] = (byte) ((soundRate >> 16) & 0xff);
        header[27] = (byte) ((soundRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (channels * bitsForSample / 8);  // block align
        header[33] = 0;
        header[34] = bitsForSample;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header);

	}

}