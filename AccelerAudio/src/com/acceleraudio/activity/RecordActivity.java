package com.acceleraudio.activity;

import java.util.ArrayList;

import com.acceleraudio.database.DbAdapter;
import com.acceleraudio.service.RecordTrack;
import com.acceleraudio.util.ImageBitmap;
import com.acceleraudio.util.Util;
import com.malunix.acceleraudio.R;

import android.app.Activity;
import android.content.Context;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class RecordActivity extends Activity {
	
	public static String SESSION_ID = "recordActivity.session_id";
	public static String TIME_REMAINING = "recordActivity.time_remaining";
	public static String ORIENTATION = "recordActivity.orientation";
	public static String PAUSE = "recordActivity.pause";
	public static String STOP = "recordActivity.stop";
	public static String INIZIALIZED = "recordActivity.inizialized";
	
	private boolean insertComplete = false;
	private static boolean initialized;
	private Button startSession, stopSession, pauseSession, saveSession;
	private EditText nameSession;
	private static TextView  rec_sample, time_remaining;
	private static ProgressBar progressBarX , progressBarY, progressBarZ;
	private RadioGroup radioGroup;
	private RadioButton radioOrientationButton;
	// essendo pubblici e statici riesco a inserire i dati direttamente dal servizio, inoltre non vengono persi
	// quando viene stoppato il servizio e si ruota lo schermo
	public static ArrayList<Float> data_x, data_y, data_z;
	public static int sample, x, y, z;
	private DbAdapter dbAdapter;
	private Intent intentRecord;
	private long sessionId, remaining_time;
	private boolean axis_x, axis_y, axis_z;
	private boolean pause, stop;
	private int upsampling, sample_rate, orientation;
	private CountDownTimer countDownTimer;
	private SharedPreferences pref;
	static final Bitmap bmp = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
	private Context context;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_3);
    	pref = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	// creo intent per avviare il servizio di registrazione
    	intentRecord = new Intent(this, RecordTrack.class);
    	initialized = false;
		dbAdapter = new DbAdapter(this);
		context = this;
		
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
			saveSession = (Button) findViewById(R.id.UI3_BT_save);
			progressBarX = (ProgressBar) findViewById(R.id.UI3_PB_X);
			progressBarY = (ProgressBar) findViewById(R.id.UI3_PB_Y);
			progressBarZ = (ProgressBar) findViewById(R.id.UI3_PB_Z);
			radioGroup = (RadioGroup) findViewById(R.id.UI3_orientation);
			//setProgressBarMax((int)accelerometro.getMaximumRange());
			setProgressBarMax(20);
			resetProgressBar();
			rec_sample.setText("" + sample);
			
			pauseSession.setEnabled(false);
			stopSession.setEnabled(false);
			saveSession.setEnabled(false);
			
//////////////////////////////////////////////////////////
///////////// prelevo le impostazioni predefinite ////////
//////////////////////////////////////////////////////////

			// imposto preferenze
			axis_x = pref.getBoolean(PreferencesActivity.AXIS_X, true); // asse x
			axis_y = pref.getBoolean(PreferencesActivity.AXIS_Y, true); // asse y
			axis_z = pref.getBoolean(PreferencesActivity.AXIS_Z, true); // asse z
			sample_rate = pref.getInt(PreferencesActivity.SAMPLE_RATE, SensorManager.SENSOR_DELAY_NORMAL);
			upsampling = pref.getInt(PreferencesActivity.UPSAMPLING, Util.getUpsamplingID(getString(R.string.note)));
			remaining_time = pref.getInt(PreferencesActivity.TIMER_MINUTES, 1)*60000 + pref.getInt(PreferencesActivity.TIMER_SECONDS, 0)*1000;
			
			if (savedInstanceState != null)
			{
				resetProgressBar();
				sessionId = savedInstanceState.getLong(SESSION_ID);
				remaining_time = savedInstanceState.getLong(TIME_REMAINING);
				orientation = savedInstanceState.getInt(ORIENTATION);
				pause = savedInstanceState.getBoolean(PAUSE);
				stop = savedInstanceState.getBoolean(STOP);
				initialized = savedInstanceState.getBoolean(INIZIALIZED);
				setRequestedOrientation(orientation);
				
				if(remaining_time == 0 || stop)
				{
					startSession.setEnabled(false);
					pauseSession.setEnabled(false);
					stopSession.setEnabled(false);
					saveSession.setEnabled(true);
				}
				else
				{
					startSession.setEnabled(true);
					pauseSession.setEnabled(false);
					stopSession.setEnabled(true);
					saveSession.setEnabled(false);
				}
			}
			else
			{	
				orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				radioOrientationButton = (RadioButton) findViewById(R.id.UI3_RB_portrait);
				radioOrientationButton.setChecked(true);
				
				data_x = new ArrayList<Float>();
				data_y = new ArrayList<Float>();
				data_z = new ArrayList<Float>();
				sample = 0;
				x = 0;
				y = 0;
				z = 0;
				pause = false;
				stop = false;
			}
			
			rec_sample.setText("" + sample);
			time_remaining.setText("" + remaining_time / 1000);
			
/////////////////////////////////////////////////////////
////////////aggiungo listener ai bottoni ///////////////
////////////////////////////////////////////////////////

			/**** ROTAZIONE SCHERMO ****/
			radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					int orientationID = radioGroup.getCheckedRadioButtonId();
					radioOrientationButton = (RadioButton) findViewById(orientationID);
					
					if(radioOrientationButton.getText().toString().equalsIgnoreCase("portrait"))
					{
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
						orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
					}
					else
					{
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
						orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
					}
				}
			});
			
			/**** AVVIA LA REGISTRAZIONE ****/
			startSession.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					pause = false;
					stop = false;
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
								stopSession.setEnabled(false);
								saveSession.setEnabled(true);
								remaining_time = 0;
							}
							try{
								resetProgressBar();
								stopService(intentRecord);
								stop = true;
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
					pause = true;
					stop = false;
					startSession.setEnabled(true);
					pauseSession.setEnabled(false);
					stopService(intentRecord);
					if(countDownTimer != null) countDownTimer.cancel();
					resetProgressBar();
				}
			});
			
			/**** STOPPA LA REGISTRAZIONE ****/
			stopSession.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					pause = false;
					stop = true;
					startSession.setEnabled(false);
					pauseSession.setEnabled(false);
					stopSession.setEnabled(false);
					saveSession.setEnabled(true);
					
					stopService(intentRecord);
					if(countDownTimer != null) countDownTimer.cancel();
					resetProgressBar();
				}
			});
			
			/**** SALVA LA REGISTRAZIONE ****/
			saveSession.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// Verifica che siano stati presi dati dall'accelerometro
					if(initialized)
						if(nameSession.getText().toString().length() > 0) // Verifica che si abbia scritto il nome della sessione
						{
							radioGroup.setEnabled(false);
							saveSession.setEnabled(false);
							saveAccelerometerData();
							// avvio la SessionInfoActivity
							Intent i = new Intent(v.getContext(), SessionInfoActivity.class);
							i.putExtra(DbAdapter.T_SESSION_SESSIONID, (int)sessionId);
							v.getContext().startActivity(i);
						}
						else Toast.makeText(v.getContext(), "INSERISCI NOME SESSIONE", Toast.LENGTH_SHORT).show();
					else finish(); // se non ci sono dati chiude l'activity
				}
			});

			
		} catch (RuntimeException e) {
			Toast.makeText(this, getString(R.string.error_interface_load), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			finish();
		}
		
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	// nel caso vada in background non deve chiudersi activity, mentre se ha salvato i dati
    	// e passa nella infoActivity deve chiudersi in modo premendo back
    	// si ritorna alla lista delle sessioni
    	if (insertComplete) finish();
    }
    
   
    @Override
    protected void onDestroy() {
    	super.onDestroy();
		stopService(intentRecord);
    	if(countDownTimer != null)countDownTimer.cancel();
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) 
    {
    	savedInstanceState.putLong(SESSION_ID, sessionId);
    	savedInstanceState.putLong(TIME_REMAINING, remaining_time);
    	savedInstanceState.putInt(ORIENTATION, orientation);
    	savedInstanceState.putBoolean(PAUSE, pause);
    	savedInstanceState.putBoolean(STOP, stop);
    	savedInstanceState.putBoolean(INIZIALIZED, initialized);
    	super.onSaveInstanceState(savedInstanceState);
    }
        

/////////////////////////////////////////////////////////    
/////////////// Metodi AUSILIARI  ///////////////////////
////////////////////////////////////////////////////////

    public void saveAccelerometerData(){
    	if(initialized){

			try {
				
				Log.w("save Session", "inzio..");
				final StringBuilder x_sb = new StringBuilder();
				final StringBuilder y_sb = new StringBuilder();
				final StringBuilder z_sb = new StringBuilder();
				for (float value : data_x) x_sb.append(" " + value);
				for (float value : data_y) y_sb.append(" " + value);
				for (float value : data_z) z_sb.append(" " + value);
				Log.w("save Session", "...dati preparati");
				Log.w("save Session", "creazione immagine...");
				
				Thread t = new Thread("Thumbnail_Creation"){
					public void run() {
						// setta la priorità massia del thread
				        setPriority(Thread.MAX_PRIORITY);
				        
				        //costruzione immagine
				        Log.w("save Session", "...creata");
						ImageBitmap.color(bmp, x_sb.toString().split(" "), y_sb.toString().split(" "), z_sb.toString().split(" "), (int)sessionId);	
						String encodedImage = ImageBitmap.encodeImage(bmp);
						Log.w("save Session", "...codificata");
						
						DbAdapter dba = new  DbAdapter(context);
						dba.open();
						dba.updateSessionImage(sessionId, encodedImage);
						dba.close();
						
						runOnUiThread(new Runnable() {
			                @Override
			                public void run() {
			                	try {
									SessionInfoActivity.thumbnail.setImageBitmap(bmp);
								} catch (Exception e) {
									e.printStackTrace();
								}
			                }
			            });	
					}
				};
				t.start();  
				
				// inserisce un immagine vuota nel database mentre il thread elabora quella vera
				String encodedImage = ImageBitmap.encodeImage(bmp);

				// apro la connessione al db
				dbAdapter.open();
				// inserisco i dati della sessione nel database
				sessionId = dbAdapter.createSession( nameSession.getText().toString(), encodedImage, (axis_x? 1:0), (axis_y? 1:0), (axis_z? 1:0), upsampling, x_sb.toString(), y_sb.toString(), z_sb.toString(), x_sb.length(), y_sb.length(), z_sb.length() );
				insertComplete = true;
				Log.w("save Session", "sessione inserita");
				// chiudo la connessione al db
				dbAdapter.close();
				
			} catch (SQLException e) {
				Toast.makeText(this, "Errore salvataggio nel database", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
    }
    
    public static void updateSample(){
		rec_sample.setText("" + sample);
		progressBarX.setProgress(x);
		progressBarY.setProgress(y);
		progressBarZ.setProgress(z);
		initialized = true;
    }
    public static void setProgressBarMax(int max)
    {
    	progressBarX.setMax(max);
		progressBarY.setMax(max);
		progressBarZ.setMax(max);
    }
    public static void resetProgressBar()
    {
    	progressBarX.setProgress(0);
		progressBarY.setProgress(0);
		progressBarZ.setProgress(0);
    }
}