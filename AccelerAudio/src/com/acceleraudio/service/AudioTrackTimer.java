
package com.acceleraudio.service;

import com.acceleraudio.activity.PlayerActivity;
import com.acceleraudio.util.MusicUpsampling;
import com.acceleraudio.util.Util;

import android.media.AudioTrack;
import android.os.CountDownTimer;

/**
 * @author Nicola Rigato
 * @author Luca Del Salvador
 * @author Marco Tessari
 * @author Gruppo: Malunix
 *
 * audioTrack personalizzata, contenete un countDownTimer per lo scorrimento della sbarra in riproduzine
 * e per visualizzare il tempo trascorso nella PlayerActivity
 */
public class AudioTrackTimer extends AudioTrack {

	private long elapsed, duration, remaining_millis;
	private int upsampling;
	private MyTimer timer;
	private boolean isPlaying;
	
	/**
	 * @param streamType tipo di stream
	 * @param sampleRateInHz sound rate in Hz
	 * @param channelConfig numero di canali
	 * @param audioFormat formato audio
	 * @param bufferSizeInBytes lunghezza buffer in Bytes
	 * @param mode modalita di riproduzione
	 * @param sampleLength numero di campioni da riprodurre
	 * @throws IllegalArgumentException
	 */
	public AudioTrackTimer(int streamType, int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes, int mode, int upsampling, int sampleLength) throws IllegalArgumentException {
		super(streamType, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes, mode);
		this.upsampling = upsampling;
		duration = MusicUpsampling.duration(upsampling, sampleLength, sampleRateInHz);
		remaining_millis = duration;
		isPlaying = false;
	}
	
	/**
	 * Avvia la riproduzione della musica e la sbarra di riproduzione
	 * 
	 * @see android.media.AudioTrack#play()
	 */
	@Override
	public void play(){
		super.play();
		isPlaying = true;
		if(duration > 0) {
			elapsed = remaining_millis;
			timer = new MyTimer(remaining_millis, 1);
			timer.start();
		}
	}
	
	/**
	 * Mette in pausa la riproduzione e stoppa la sbarra di riproduzione
	 * 
	 * @param sampleIndex il campioni in riproduzione
	 * @see android.media.AudioTrack#pause()
	 */
	public void pause(int sampleIndex){
		super.pause();
		try {
			synchronized (this) {
				this.wait(100);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(timer != null) 
			timer.cancel();
		isPlaying = false;
		remaining_millis = (duration - MusicUpsampling.duration(upsampling, sampleIndex, getSampleRate()));
	}
	
	/**
	 * Stoppa la riproduzione audio, blocca la sbarra di riproduzione
	 * e resetta il tempo trascorso
	 * 
	 * @see android.media.AudioTrack#stop()
	 */
	@Override
	public void stop(){
		super.stop();
		remaining_millis = duration;
		if(timer != null) 
			timer.cancel();
		isPlaying = false;
	}
	//
	
	/**
	 * CountDownTimer personalizzato
	 *
	 */
	class MyTimer extends CountDownTimer{

		public MyTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			remaining_millis = duration;
			if(isPlaying){
				timer = new MyTimer(remaining_millis, 1);
				timer.start();
			}
		}

		@Override
		public void onTick(long millisUntilFinished) {
			long time_elapsed = duration - (millisUntilFinished + elapsed);
			PlayerActivity.currentTimeTV.setText("" + Util.millisecondsToMinutesSeconds(elapsed + time_elapsed));
			PlayerActivity.sb_musicProgress.setProgress((int)(time_elapsed + elapsed));
			remaining_millis = millisUntilFinished;
			if(!isPlaying) onFinish();
		}
		
	}
}
