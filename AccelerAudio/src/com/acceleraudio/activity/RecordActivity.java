package com.acceleraudio.activity;

import java.util.ArrayList;

import com.acceleraudio.database.DbAdapter;
import com.acceleraudio.service.RecordTrack;
import com.acceleraudio.util.ImageBitmap;
import com.acceleraudio.util.Util;
import com.malunix.acceleraudio.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class RecordActivity extends Activity {
	
	public static String SAMPLE_RATE = "recordActivity.sample_rate";
	public static String SAMPLE = "recordActivity.sample";
	public static String TIME_REMAINING = "recordActivity.time_remaining";
	public static String PAUSE = "recordActivity.pause";
	public static String DATA_X = "recordActivity.data_x";
	public static String DATA_Y = "recordActivity.data_y";
	public static String DATA_Z = "recordActivity.data_z";
	
	private static Button startSession, stopSession, pauseSession;
	private static EditText nameSession;
	public static TextView  rec_sample, time_remaining;
	private static ProgressBar progressBarX , progressBarY, progressBarZ;
	// essendo pubblici e statici, i dati non vengono persi durante la rotazione del display
	// TODO verificare che questo sia il metodo piu adatto di procedere
	public static ArrayList<Float> data_x, data_y, data_z;
	public static int sample, x, y, z;
	private DbAdapter dbAdapter;
	public Intent intentRecord;
	public static long sessionId, remaining_time;
	public static boolean pause;
	private int sample_rate, orientation;
	private CountDownTimer countDownTimer;
	private SharedPreferences pref;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_3);
    	pref = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	// creo intent per avviare il servizio di registrazione
    	intentRecord = new Intent(this, RecordTrack.class);	
		dbAdapter = new DbAdapter(this);	
		
		
////////////////////////////////////////////////////////
///////////// collego widget con xml ///////////////////
///////////////////////////////////////////////////////

		try {
			nameSession = (EditText) findViewById(R.id.UI3_ET_SessionName);
			rec_sample = (TextView) findViewById(R.id.UI3_TV_RecordedSamples);
			time_remaining = (TextView) findViewById(R.id.UI3_TV_timerRemaning);
			startSession = (Button) findViewById(R.id.UI3_BT_record);
			stopSession = (Button) findViewById(R.id.UI3_BT_stop);
			pauseSession = (Button) findViewById(R.id.UI3_BT_pause);
			progressBarX = (ProgressBar) findViewById(R.id.UI3_PB_X);
			progressBarY = (ProgressBar) findViewById(R.id.UI3_PB_Y);
			progressBarZ = (ProgressBar) findViewById(R.id.UI3_PB_Z);
			//setProgressBarMax((int)accelerometro.getMaximumRange());
			setProgressBarMax(20);
			resetProgressBar();
			rec_sample.setText("" + sample);
			
			pauseSession.setEnabled(false);
			stopSession.setEnabled(false);
			
//////////////////////////////////////////////////////////
///////////// prelevo le impostazioni predefinite ////////
//////////////////////////////////////////////////////////

			if (savedInstanceState != null)
			{
				resetProgressBar();
				sample_rate = savedInstanceState.getInt(SAMPLE_RATE);
				sample = savedInstanceState.getInt(SAMPLE);
				remaining_time = savedInstanceState.getLong(TIME_REMAINING);
				pause = savedInstanceState.getBoolean(PAUSE);
				data_x = Util.toArrayListFloat(savedInstanceState.getFloatArray(DATA_X));
				data_y = Util.toArrayListFloat(savedInstanceState.getFloatArray(DATA_Y));
				data_z = Util.toArrayListFloat(savedInstanceState.getFloatArray(DATA_Z));
				if(sample > 0 )startSession.setText(getString(R.string.resume));
				
				if(remaining_time == 0)
				{
					startSession.setEnabled(false);
					pauseSession.setEnabled(false);
					stopSession.setEnabled(false);
				}
				else
				{
					startSession.setEnabled(true);
					pauseSession.setEnabled(false);
					stopSession.setEnabled(true);
				}
			}
			else
			{	
				// imposto preferenze
				sample_rate = pref.getInt(PreferencesActivity.SAMPLE_RATE, SensorManager.SENSOR_DELAY_NORMAL);
				remaining_time = pref.getInt(PreferencesActivity.TIMER_SECONDS, 5)*1000;
				
				data_x = new ArrayList<Float>();
				data_y = new ArrayList<Float>();
				data_z = new ArrayList<Float>();
				sample = 0;
				rec_sample.setText("0");
				x = 0;
				y = 0;
				z = 0;
				pause = false;
			}
			
			time_remaining.setText("" + remaining_time / 1000);
			
		
/////////////////////////////////////////////////////////
////////////aggiungo listener ai bottoni ///////////////
////////////////////////////////////////////////////////
		
			/**** AVVIA LA REGISTRAZIONE ****/
			startSession.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					lockOrientation();
					pause = false;
					startSession.setText(getString(R.string.resume));
					startSession.setEnabled(false);
					pauseSession.setEnabled(true);
					stopSession.setEnabled(true);
					intentRecord.putExtra(RecordTrack.SENSOR_DELAY, sample_rate);
					startService(intentRecord);
					
					countDownTimer = new CountDownTimer(remaining_time, 1000) {
						public void onTick(long millisUntilFinished) {
							remaining_time = millisUntilFinished;
							time_remaining.setText("" + millisUntilFinished / 1000);
						}
						
						public void onFinish() {
							time_remaining.setText("0");
							if(!pause)
							{
								startSession.setEnabled(false);
								pauseSession.setEnabled(false);
								remaining_time = 0;
								stopSession.performClick();
							}
							try{
								resetProgressBar();
								stopService(intentRecord);
							}
							catch(NullPointerException ex)
							{
								ex.printStackTrace();
							}
						}
					};
					countDownTimer.start();
				}
			});
			
			/**** PAUSA LA REGISTRAZIONE ****/
			pauseSession.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					unlockOrientation();
					pause = true;
					startSession.setEnabled(true);
					pauseSession.setEnabled(false);
					stopService(intentRecord);
					if(countDownTimer != null) countDownTimer.cancel();
					resetProgressBar();
				}
			});
			
			/**** STOPPA E SALVA LA REGISTRAZIONE ****/
			stopSession.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					lockOrientation();
					pause = false;
					startSession.setEnabled(false);
					pauseSession.setEnabled(false);
					stopSession.setEnabled(false);
					
					stopService(intentRecord);
					if(countDownTimer != null) countDownTimer.cancel();
					resetProgressBar();
					
					// Verifica che siano stati presi dati dall'accelerometro
					if(data_x.size() > 0 || data_y.size() > 0 || data_z.size() > 0)
					{
						saveAccelerometerData();
						// avvio la SessionInfoActivity
						Intent i = new Intent(v.getContext(), SessionInfoActivity.class);
						i.putExtra(DbAdapter.T_SESSION_SESSIONID, sessionId);
						v.getContext().startActivity(i);
						finish();
					}
					else
					{
						Toast.makeText(v.getContext(), getString(R.string.error_no_recorded_data), Toast.LENGTH_SHORT).show();
						finish(); // se non ci sono dati da salvare quindi chiude l'activity
					}
				}
			});
			
		} catch (RuntimeException e) {
			Toast.makeText(this, getString(R.string.error_interface_load), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			finish();
		}
		
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
		stopService(intentRecord);
    	if(countDownTimer != null)countDownTimer.cancel();
    	// resetta i campi per risparmiare memoria
    	data_x = new ArrayList<Float>();
		data_y = new ArrayList<Float>();
		data_z = new ArrayList<Float>();
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) 
    {
    	savedInstanceState.putInt(SAMPLE_RATE, sample_rate);
    	savedInstanceState.putInt(SAMPLE, sample);
    	savedInstanceState.putLong(TIME_REMAINING, remaining_time);
    	savedInstanceState.putBoolean(PAUSE, pause);
    	savedInstanceState.putFloatArray(DATA_X, Util.toArrayFloat(data_x));
    	savedInstanceState.putFloatArray(DATA_Y, Util.toArrayFloat(data_y));
    	savedInstanceState.putFloatArray(DATA_Z, Util.toArrayFloat(data_z));
    	super.onSaveInstanceState(savedInstanceState);
    }
        

/////////////////////////////////////////////////////////    
////////////////Metodi Utili  //////////////////////////
////////////////////////////////////////////////////////

    public void saveAccelerometerData(){
		try {
			
			// inserisco i dati uno StringBuilder per trasformarli piu velocemente in una stringa da inserire nel database
			Log.w("save Session", "inzio..");
			final StringBuilder x_sb = new StringBuilder();
			final StringBuilder y_sb = new StringBuilder();
			final StringBuilder z_sb = new StringBuilder();
			for (float value : data_x) x_sb.append(value + " ");
			for (float value : data_y) y_sb.append(value + " ");
			for (float value : data_z) z_sb.append(value + " ");
			Log.w("save Session", "...dati preparati");
			Log.w("save Session", "creazione immagine...");
			
			// apro la connessione al db
			dbAdapter.open();
			
			String name;
			// Verifica che si abbia scritto il nome della sessione
			if(nameSession.getText().toString().length() > 0)
				name = nameSession.getText().toString();
			else
			{
				name = "Registrazione_" + (dbAdapter.getMaxId()+1);
			}
			
			// inserisco i dati della sessione nel database
			sessionId = dbAdapter.createSession( name, "", (pref.getBoolean(PreferencesActivity.AXIS_X, true)? 1:0), (pref.getBoolean(PreferencesActivity.AXIS_Y, true)? 1:0), (pref.getBoolean(PreferencesActivity.AXIS_Z, true)? 1:0), pref.getInt(PreferencesActivity.UPSAMPLING, 0), x_sb.toString(), y_sb.toString(), z_sb.toString(), data_x.size(), data_y.size(), data_z.size() );
			
			//costruzione immagine
	        Log.w("save Session", "...creata");
	        final Bitmap bmpT = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
			ImageBitmap.color(bmpT, data_x, data_y, data_z, (int)sessionId);	
			String encodedImage = ImageBitmap.encodeImage(bmpT);
			Log.w("save Session", "...codificata");
			dbAdapter.updateSessionImage(sessionId, encodedImage);
			Log.w("save Session", "sessione inserita");
			// chiudo la connessione al db
			dbAdapter.close();
			
		} catch (SQLException e) {
			Toast.makeText(this, getString(R.string.error_database_insert_new_session), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
    }

//////////////////////////////
///// METODI AUSILIARI //////
/////////////////////////////
    
    /*** aggiorna il numero di sample e le progressBar, nell'interfaccia ***/
    public static void updateSample(){
		rec_sample.setText("" + sample);
		progressBarX.setProgress(x);
		progressBarY.setProgress(y);
		progressBarZ.setProgress(z);
    }
    
    /*** imposta il valore massimo per tutte le progress bar ***/
    public static void setProgressBarMax(int max)
    {
    	progressBarX.setMax(max);
		progressBarY.setMax(max);
		progressBarZ.setMax(max);
    }
    
    /*** imposta tutte le progress bar a 0 ***/
    public static void resetProgressBar()
    {
    	progressBarX.setProgress(0);
		progressBarY.setProgress(0);
		progressBarZ.setProgress(0);
    }
    
    /*** blocca la rotazione dello schermo ***/
    public void lockOrientation()
    {
    	orientation = getResources().getConfiguration().orientation;
		
    	// se lo schermo si trova in una delle posizione conosciute allora si blocca in quella
    	switch(orientation){
    		case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
    		case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
    		case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
    		case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
    			setRequestedOrientation(orientation);
    			return;
    		default:
    			// nel caso il dispositivo si trovi in reverse-landscape viene bloccato in landscape
    			// qusto perchè il reverse non è disponibile nelle api 8
    			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    			return;
    	}
    }
    
    /*** sblocca la rotazione dello schermo ***/
    public void unlockOrientation()
    {
    	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
    
}