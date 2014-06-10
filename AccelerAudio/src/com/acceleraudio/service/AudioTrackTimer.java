/**
 * 
 */
package com.acceleraudio.service;

import com.acceleraudio.activity.PlayerActivity;
import com.acceleraudio.util.MusicUpsampling;

import android.app.Activity;
import android.media.AudioTrack;
import android.os.CountDownTimer;

public class AudioTrackTimer extends AudioTrack {

	private long elapsed, duration, remaining_millis;
	private int playbackHeadPosition, sampleLength, upsampling, sampleIndex;
	private CountDownTimer countDownTimer;
	
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
		this.sampleLength = sampleLength;
		this.upsampling = upsampling;
		duration = MusicUpsampling.duration(upsampling, sampleLength, sampleRateInHz);
		remaining_millis = duration;
		// TODO: non posso richiamare i metodi da un thread diverso
//		PlayerActivity.setMaxDuration(duration);
//    	PlayerActivity.durationTV.setText("" + duration/1000);
//		PlayerActivity.sb_musicProgress.setMax((int)duration);
	}
	
	@Override
	public void play(){
		super.play();
		loadCountDownTimer();
		if(countDownTimer != null) 
			countDownTimer.start();
	}
	
	public void pause(int sampleIndex){
		super.pause();
		this.sampleIndex = sampleIndex;
		if(countDownTimer != null)
			countDownTimer.cancel();
		playbackHeadPosition = getPlaybackHeadPosition();
//		remaining_millis = (duration - (MusicUpsampling.duration(upsampling, sampleIndex, getSampleRate()) + (long)(playbackHeadPosition/22.5)));
	}
	
	@Override
	public void stop(){
		super.stop();
		if(countDownTimer != null)
			countDownTimer.cancel();
	}
	
	@Override
	public int getPlaybackHeadPosition(){
		return super.getPlaybackHeadPosition();
	}
	
	public void loadCountDownTimer()
    {
    	elapsed = remaining_millis;
    	countDownTimer = new CountDownTimer(remaining_millis, 5) {
			public void onTick(long millisUntilFinished) {
				long time_elapsed = duration - (millisUntilFinished + elapsed);
				PlayerActivity.currentTimeTV.setText("" + ((double)((elapsed + time_elapsed) / 1000)));
				PlayerActivity.sb_musicProgress.setProgress((int)(time_elapsed + elapsed));
				remaining_millis = millisUntilFinished;
			}
			
			public void onFinish() {
				PlayerActivity.currentTimeTV.setText("" + (remaining_millis/1000));
				PlayerActivity.sb_musicProgress.setProgress((int)remaining_millis);
				remaining_millis = duration;
				
				loadCountDownTimer();
				countDownTimer.start();
			}
		};
    }
}
