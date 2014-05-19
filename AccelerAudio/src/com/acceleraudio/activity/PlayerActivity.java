package com.acceleraudio.activity;

import com.acceleraudio.database.DbAdapter;
import com.acceleraudio.service.PlayerTrack;
import com.malunix.acceleraudio.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class PlayerActivity extends Activity {
	
	public static String ACC_DATA = "playerActivity.accelerotemer_data"; // dati accelerometro
	public static String MUSIC_CURSOR = "playerActivity.music_cursor"; // puntatore array della musica in riproduzione
	public static String SOUND_RATE = "playerActivity.soundRate";
	public static String UPSAMPLING = "playerActivity.upsampling";
	public static String INIZIALIZED = "playerActivity.inizialied";
	public static String SAMPLE = "playerActivity.sample";
	public static String IMAGE = "playerActivity.image";
	
	private Boolean inizialized = false, axis_x, axis_y, axis_z;
	private TextView sessionName, currentTimeTV, durationTV;
	private Button play, pause, stop, export;
	private ImageView thumbnail;
	private SeekBar sb_musicProgress;
	private int[] sample;
	private int sessionId, upsampling, musicCursor = 0; // musicCursos: puntatore array della musica in riproduzione
	private static long duration;
	private DbAdapter dbAdapter;
	private Cursor cursor;
	public Intent intentPlayer;
	public static String[] data_x, data_y, data_z;
	private String image;
	private Thread t;
	private CountDownTimer countDownTimer;
	private BroadcastReceiver receiver = new BroadcastReceiver() {
    	
    	@Override
        public void onReceive(Context context, Intent intent) {
    		Bundle bundle = intent.getExtras();
    		if (bundle != null) {
    			if(intent.hasExtra(MUSIC_CURSOR)) musicCursor = bundle.getInt(MUSIC_CURSOR);
    			//if(intent.hasExtra(PlayerTrack.DURATION)) duration = bundle.getLong(PlayerTrack.DURATION);
    		}
        }
    };
	
    @Override
    
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_4);
    	
    	// creo intent per avviare il servizio di riproduzione audio
    	intentPlayer = new Intent(this, PlayerTrack.class);
    	
////////////////////////////////////////////////////////
///////////// collego widget con xml ///////////////////
///////////////////////////////////////////////////////

		try {
			sessionName = (TextView) findViewById(R.id.UI4_TV_sessionName);
			play = (Button) findViewById(R.id.UI4_BT_play);
			pause = (Button) findViewById(R.id.UI4_BT_pause);
			stop = (Button) findViewById(R.id.UI4_BT_stop);
			export = (Button) findViewById(R.id.UI4_BT_export);
			thumbnail = (ImageView) findViewById(R.id.UI4_IV_thumbnail);
			currentTimeTV = (TextView) findViewById(R.id.UI4_TV_initialTimer);
			durationTV = (TextView) findViewById(R.id.UI4_TV_finalTimer);
			sb_musicProgress = (SeekBar) findViewById(R.id.UI4_SB_musicProgress);
			
			play.setEnabled(true);
			pause.setEnabled(false);
			
			if (savedInstanceState != null)
			{
				inizialized = savedInstanceState.getBoolean(INIZIALIZED);
				musicCursor = savedInstanceState.getInt(MUSIC_CURSOR);
				sample = savedInstanceState.getIntArray(SAMPLE);
				upsampling = savedInstanceState.getInt(UPSAMPLING);
				image = savedInstanceState.getString(IMAGE);
				duration = savedInstanceState.getLong(PlayerTrack.DURATION);
				
				if(inizialized)
				{
					play.setEnabled(false);
					pause.setEnabled(true);
				}
			}
			else
			{
				
////////////////////////////////////////////////////////
///////////// Prelevo dati dall'intent /////////////////
///////////////////////////////////////////////////////  

				Bundle b = getIntent().getExtras();
				sessionId = b.getInt(DbAdapter.T_SESSION_SESSIONID);
    	
////////////////////////////////////////////////////////
/// prelevo dati dal database e li carico nella vista///
///////////////////////////////////////////////////////

				// apro la connessione al db
				dbAdapter = new DbAdapter(this);
				dbAdapter.open();
				
				// prelevo record by ID 
				cursor = dbAdapter.fetchSessionById(sessionId);
				cursor.moveToFirst();
				
				// carico dati
				sessionName.setText(cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_NAME)));
				image = (cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_IMAGE)));
				data_x = (cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_SENSOR_DATA_X))).split(" ");
				data_y = (cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_SENSOR_DATA_Y))).split(" ");
				data_z = (cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_SENSOR_DATA_Z))).split(" ");
				//TODO: si potrebbe togliere il numero dei sample presente nel database.
				axis_x = cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_AXIS_X)).equals("1");
				axis_y = cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_AXIS_Y)).equals("1");
				axis_z = cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_AXIS_Z)).equals("1");
				upsampling = cursor.getInt(( cursor.getColumnIndex(DbAdapter.T_SESSION_UPSAMPLING)));
				
				// chiudo connessioni
				cursor.close();
				dbAdapter.close();
				
				if(!(axis_x || axis_y || axis_z)) 
				{
						Toast.makeText(this, "Selezionare almeno un asse", Toast.LENGTH_SHORT).show();
						finish();
				}
				
				int nSample = (axis_x ? data_x.length : 0) + (axis_y ? data_y.length : 0) + (axis_z ? data_z.length : 0);
				sample = new int[(nSample > 0 ? nSample : 1)];
				
				if(axis_x)
					for(int i = 0; i<data_x.length; i++)
						if(data_x[i].length()>0)sample[i] = ((int)(Float.parseFloat(data_x[i])*10));
				if(axis_y)
					for(int i = 0; i<data_y.length; i++)
						if(data_y[i].length()>0)sample[i] = ((int)(Float.parseFloat(data_y[i])*10));
				if(axis_z)
					for(int i = 0; i<data_z.length; i++)
						if(data_z[i].length()>0)sample[i] = ((int)(Float.parseFloat(data_z[i])*10));
				
			}
			
			// TODO se possibile calcolare l'immagine solo la prima volta
			t = new Thread("Thumbnail_Decoding"){
				public void run() {
					// setta la priorità massia del thread
	                setPriority(Thread.MAX_PRIORITY);
	                
	                // converto la stringa in una immagine bitmap
	        		byte[] decodedImgByteArray = Base64.decode(image, Base64.DEFAULT);
	        		final Bitmap bmp = BitmapFactory.decodeByteArray(decodedImgByteArray, 0, decodedImgByteArray.length);
					
					runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        	thumbnail.setImageBitmap(bmp);
                        }
                    });
				}
			};
			t.start();
			
    	
/////////////////////////////////////////////////////////
////////////aggiungo listener ai bottoni ///////////////
////////////////////////////////////////////////////////

			/**** play music ****/
			play.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO: caricare i dati e le impostazioni della sessione da riprodurre solo la prima volta, boolean inizializzato per la prima volta
					if(!inizialized)
					{
						// nel caso non ci siano sample ne aggiunge uno standard
						if(sample.length == 0) sample[0] = 100;
						intentPlayer.putExtra(ACC_DATA, sample);
						intentPlayer.putExtra(MUSIC_CURSOR, musicCursor);
						intentPlayer.putExtra(UPSAMPLING, upsampling);
						startService(intentPlayer);
						inizialized = true;
						play.setEnabled(false);
						pause.setEnabled(true);
						
						// TODO valori di prova
						duration = ((sample.length*3072)/44100)*1000;
						durationTV.setText("" + duration/1000);
						sb_musicProgress.setMax((int)duration/1000);
						
						
						// TODO sistemare timer durata musica
						countDownTimer = new CountDownTimer(duration, 1000) {
							public void onTick(long millisUntilFinished) {
								long time_elapsed = duration - millisUntilFinished;
								currentTimeTV.setText("" + time_elapsed / 1000);
								sb_musicProgress.setProgress((int)time_elapsed / 1000);
							}
							
							public void onFinish() {
								countDownTimer.start();
							}
						};
						countDownTimer.start();
					}
				}
			});
			
			/**** pause music ****/
			pause.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					stopService(intentPlayer);
					inizialized = false;
					play.setEnabled(true);
					pause.setEnabled(false);
					
				}
			});
			
			/**** stop music ****/
			stop.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					stopService(intentPlayer);
					finish();
				}
			});
			
			/**** Avvia l'activity per esportare session ****/
			 export.setOnClickListener(new View.OnClickListener() {
				 @Override
				 public void onClick(View view) {
					 Intent i = new Intent(view.getContext(), FileExplore.class);
					 view.getContext().startActivity(i);
				 }
			 });
	    	
	    	
		} catch (NumberFormatException e) {
			Toast.makeText(this, getString(R.string.error_number_format), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			finish();
		} catch (SQLException e) {
			Toast.makeText(this, getString(R.string.error_database), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			finish();
		} catch (RuntimeException e) {
			Toast.makeText(this, getString(R.string.error_interface_load), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			finish();
		}

    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	registerReceiver(receiver, new IntentFilter(PlayerTrack.NOTIFICATION));
    }
	    
    @Override
    protected void onPause() {
    	super.onPause();
    	unregisterReceiver(receiver);
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	//stopService(intentPlayer);
    	inizialized = false;
    }
    
    @Override
	public void onBackPressed() {
	    super.onBackPressed();
	    stopService(intentPlayer); // stoppa il servizio della musica
	    finish();
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) 
    {
    	savedInstanceState.putBoolean(INIZIALIZED, inizialized);
    	savedInstanceState.putInt(MUSIC_CURSOR, musicCursor);
    	savedInstanceState.putInt(UPSAMPLING, upsampling);
    	savedInstanceState.putIntArray(SAMPLE, sample);
    	savedInstanceState.putString(IMAGE, image);
    	savedInstanceState.putLong(PlayerTrack.DURATION, duration);
    	super.onSaveInstanceState(savedInstanceState);
    }

}