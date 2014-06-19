
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
	}
	
	@Override
	public void play(){
		super.play();
		if(duration > 0) {
			elapsed = remaining_millis;
			timer = new MyTimer(remaining_millis, 1);
			timer.start();
		}
	}
	
	public void pause(int sampleIndex){
		super.pause();
		if(timer != null) 
			timer.cancel();
		remaining_millis = (duration - MusicUpsampling.duration(upsampling, sampleIndex, getSampleRate()));
	}
	
	@Override
	public void stop(){
		super.stop();
		remaining_millis = duration;
		if(timer != null) 
			timer.cancel();
	}
	
	class MyTimer extends CountDownTimer{

		public MyTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			remaining_millis = duration;
		}

		@Override
		public void onTick(long millisUntilFinished) {
			long time_elapsed = duration - (millisUntilFinished + elapsed);
			PlayerActivity.currentTimeTV.setText("" + Util.millisecondsToMinutesSeconds(elapsed + time_elapsed));
//			PlayerActivity.currentTimeTV.setText("" + ((double)((elapsed + time_elapsed) / 1000)));
			PlayerActivity.sb_musicProgress.setProgress((int)(time_elapsed + elapsed));
			remaining_millis = millisUntilFinished;
		}
		
	}
}
