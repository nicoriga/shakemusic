/**
 * 
 */
package com.acceleraudio.service;

import com.acceleraudio.activity.PlayerActivity;
import com.acceleraudio.util.MusicUpsampling;
import com.acceleraudio.util.Util;

import android.app.Activity;
import android.media.AudioTrack;
import android.os.CountDownTimer;
import android.util.Log;

public class AudioTrackTimer extends AudioTrack {

	private long elapsed, duration, remaining_millis;
	private int playbackHeadPosition, sampleLength, upsampling, sampleIndex;
	private CountDownTimer countDownTimer;
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
		if(duration > 0) {
//			loadCountDownTimer();
			elapsed = remaining_millis;
			timer = new MyTimer(remaining_millis, 1);
			timer.start();
		}
//		if(countDownTimer != null) 
//			countDownTimer.start();
	}
	
	public void pause(int sampleIndex){
		super.pause();
		this.sampleIndex = sampleIndex;
//		if(countDownTimer != null)
//			countDownTimer.cancel();
		if(timer != null) 
			timer.cancel();
		playbackHeadPosition = getPlaybackHeadPosition();
		// TODO: sistemare il calcolo del tempo rimanente quando viene messo in pausa
//		remaining_millis = (duration - (MusicUpsampling.duration(upsampling, sampleIndex, getSampleRate()) + (long)(( getPlaybackHeadPosition( ) / getSampleRate( ) ) * 1000.0)));
	}
	
	@Override
	public void stop(){
		super.stop();
		remaining_millis = duration;
//		if(countDownTimer != null)
//		{
//			countDownTimer.cancel();
//			Log.w("music timer", "stopped");
//			countDownTimer = null;
//		}
		if(timer != null) 
			timer.cancel();
	}
	
	@Override
	public int getPlaybackHeadPosition(){
		return super.getPlaybackHeadPosition();
	}
	
//	public void loadCountDownTimer()
//    {
//    	elapsed = remaining_millis;
//    	CountDownTimer countDownTimerT = new CountDownTimer(remaining_millis, 1) {
//			public void onTick(long millisUntilFinished) {
//				long time_elapsed = duration - (millisUntilFinished + elapsed);
//				PlayerActivity.currentTimeTV.setText("" + ((double)((elapsed + time_elapsed) / 1000)));
//				PlayerActivity.sb_musicProgress.setProgress((int)(time_elapsed + elapsed));
//				remaining_millis = millisUntilFinished;
//			}
//			
//			public void onFinish() {
////				PlayerActivity.currentTimeTV.setText("" + (remaining_millis/1000));
////				PlayerActivity.sb_musicProgress.setProgress((int)remaining_millis);
//				remaining_millis = duration;
//				
////				loadCountDownTimer();
////				countDownTimer.start();
//			}
//		}.start();
//		
//		countDownTimer = countDownTimerT;
//    }
	
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
