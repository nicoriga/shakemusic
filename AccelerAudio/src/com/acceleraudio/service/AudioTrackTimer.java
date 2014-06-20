
package com.acceleraudio.service;

import com.acceleraudio.activity.PlayerActivity;
import com.acceleraudio.util.MusicUpsampling;
import com.acceleraudio.util.Util;

import android.media.AudioTrack;
import android.os.CountDownTimer;

public class AudioTrackTimer extends AudioTrack {

	private long elapsed, duration, remaining_millis;
	private int upsampling;
	private MyTimer timer;
	private boolean isPlaying;
	
	/**
	 * @param streamType
	 * @param sampleRateInHz
	 * @param channelConfig
	 * @param audioFormat
	 * @param bufferSizeInBytes
	 * @param mode
	 * @param sampleLength
	 * @throws IllegalArgumentException
	 */
	public AudioTrackTimer(int streamType, int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes, int mode, int upsampling, int sampleLength) throws IllegalArgumentException {
		super(streamType, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes, mode);
		this.upsampling = upsampling;
		duration = MusicUpsampling.duration(upsampling, sampleLength, sampleRateInHz);
		remaining_millis = duration;
		isPlaying = false;
	}
	
	@Override
	public void play(){
		isPlaying = true;
		if(duration > 0) {
			elapsed = remaining_millis;
			timer = new MyTimer(remaining_millis, 1);
			timer.start();
		}
		try {
			synchronized (this) {
				this.wait(3);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		super.play();
	}
	
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
	
	@Override
	public void stop(){
		super.stop();
		remaining_millis = duration;
		if(timer != null) 
			timer.cancel();
		isPlaying = false;
	}
	
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
//			PlayerActivity.currentTimeTV.setText("" + ((double)((elapsed + time_elapsed) / 1000)));
			PlayerActivity.sb_musicProgress.setProgress((int)(time_elapsed + elapsed));
			remaining_millis = millisUntilFinished;
			if(!isPlaying) onFinish();
		}
		
	}
}
