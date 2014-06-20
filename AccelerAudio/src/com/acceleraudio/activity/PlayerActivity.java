package com.acceleraudio.activity;

import com.acceleraudio.database.DbAdapter;
import com.acceleraudio.service.PlayerTrack;
import com.acceleraudio.util.MusicUpsampling;
import com.acceleraudio.util.Util;
import com.malunix.acceleraudio.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Nicola Rigato
 * @author Luca Del Salvador
 * @author Marco Tessari
 * @author Gruppo: Malunix
 *
 * permette di riprodure la musica dai dati dell'accelerometro della sessione
 */
public class PlayerActivity extends Activity {
	
	public static final String NOTIFICATION = "com.acceleraudio.activity.playerActivity";
	public static String SESSION_ID = "playerActivity.session_id";
	public static String SESSION_NAME = "playerActivity.session_Name";
	public static String ACC_DATA = "playerActivity.accelerotemer_data"; // dati accelerometro
	public static String SOUND_RATE = "playerActivity.soundRate";
	public static String UPSAMPLING = "playerActivity.upsampling";
	public static String SAMPLE = "playerActivity.sample";
	public static String IMAGE = "playerActivity.image";
	public static String INTENT_PLAYER = "playerActivity.intentPlayer";
	public static String PROGRESS_TIME = "playerActivity.progressTime";
	public static final String DURATION = "playertrack.duration";
	public static final String PAUSE = "playertrack.pause";
	
	private Boolean axis_x, axis_y, axis_z;
	private TextView sessionName;
	public static TextView currentTimeTV, durationTV;
	private ImageButton play, pause, stop;
	private Button export;
	private ImageView thumbnail;
	public static SeekBar sb_musicProgress;
	private int[] sample;
	private long sessionId;
	private int upsampling;
	private static long duration;
	private DbAdapter dbAdapter;
	private Cursor cursor;
	public Intent intentPlayer;
	public static String[] data_x, data_y, data_z;
	private String image;
	private Thread t;
	private static Bitmap bmp = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
	public boolean isPause;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_4);
    	
/////////////////////////////////////////////////////////
///////////// collego widget con xml ///////////////////
///////////////////////////////////////////////////////

		try {
			sessionName = (TextView) findViewById(R.id.UI4_TV_sessionName);
			play = (ImageButton) findViewById(R.id.UI4_BT_play);
			pause = (ImageButton) findViewById(R.id.UI4_BT_pause);
			stop = (ImageButton) findViewById(R.id.UI4_BT_stop);
			export = (Button) findViewById(R.id.UI4_BT_export);
			thumbnail = (ImageView) findViewById(R.id.UI4_IV_thumbnail);
			currentTimeTV = (TextView) findViewById(R.id.UI4_TV_initialTimer);
			durationTV = (TextView) findViewById(R.id.UI4_TV_finalTimer);
			sb_musicProgress = (SeekBar) findViewById(R.id.UI4_SB_musicProgress);
			
			play.setEnabled(true);
			pause.setEnabled(false);
			
			if (savedInstanceState != null)
			{
				sessionId = savedInstanceState.getLong(SESSION_ID);
				sessionName.setText(savedInstanceState.getString(SESSION_NAME));
				isPause = savedInstanceState.getBoolean(PAUSE);
				sample = savedInstanceState.getIntArray(SAMPLE);
				upsampling = savedInstanceState.getInt(UPSAMPLING);
				image = savedInstanceState.getString(IMAGE);
				duration = savedInstanceState.getLong(DURATION);
				intentPlayer = savedInstanceState.getParcelable(INTENT_PLAYER);
				currentTimeTV.setText(savedInstanceState.getString(PROGRESS_TIME));
				
				durationTV.setText("" + Util.millisecondsToMinutesSeconds(duration));
				sb_musicProgress.setMax((int)duration);
				
				if(isPause)
				{
					play.setEnabled(true);
					pause.setEnabled(false);
				}
				else
				{
					play.setEnabled(false);
					pause.setEnabled(true);
				}
			}
			else
			{
				// creo intent per avviare il servizio di riproduzione audio
		    	intentPlayer = new Intent(this, PlayerTrack.class);
		    	
				isPause = false;
				
////////////////////////////////////////////////////////
///////////// Prelevo dati dall'intent /////////////////
///////////////////////////////////////////////////////  

				Bundle b = getIntent().getExtras();
				sessionId = b.getLong(DbAdapter.T_SESSION_SESSIONID);
    	
////////////////////////////////////////////////////////
////////// prelevo dati dal database //////////////////
///////////////////////////////////////////////////////

				// apro la connessione al db
				dbAdapter = new DbAdapter(this);
				dbAdapter.open();
				
				// prelevo record per ID 
				cursor = dbAdapter.fetchSessionById(sessionId);
				cursor.moveToFirst();
				
				// carico dati
				sessionName.setText(cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_NAME)));
				image = (cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_IMAGE)));
				data_x = (cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_SENSOR_DATA_X))).split(" ");
				data_y = (cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_SENSOR_DATA_Y))).split(" ");
				data_z = (cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_SENSOR_DATA_Z))).split(" ");
				axis_x = cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_AXIS_X)).equals("1");
				axis_y = cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_AXIS_Y)).equals("1");
				axis_z = cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_AXIS_Z)).equals("1");
				upsampling = cursor.getInt(( cursor.getColumnIndex(DbAdapter.T_SESSION_UPSAMPLING)));
				
				// chiudo connessioni
				cursor.close();
				dbAdapter.close();
				
				// se la sessione non ha almeno un asse selezionato la riproduzione non può avvenire
				if(!(axis_x || axis_y || axis_z)) 
				{
						Toast.makeText(this, getString(R.string.error_no_axis_selected), Toast.LENGTH_SHORT).show();
						finish();
				}
				else
				{
					// creo array con la lunghezza degli assi selezionati
					int nSample = (axis_x ? data_x.length : 0) + (axis_y ? data_y.length : 0) + (axis_z ? data_z.length : 0);
					sample = new int[(nSample > 0 ? nSample : 1)];
					
					// creo array sample da riprodurre
					int z=0;
					if(axis_x)
						for(int i = 0; i<data_x.length; i++)
							if(data_x[i].length()>0)
							{
								sample[z] = ((int)(Float.parseFloat(data_x[i]))); 
								z++;
							}
					if(axis_y)
						for(int i = 0; i<data_y.length; i++)
							if(data_y[i].length()>0)
							{ 
								sample[z] = ((int)(Float.parseFloat(data_y[i]))); 
								z++;
							}
					if(axis_z)
						for(int i = 0; i<data_z.length; i++)
							if(data_z[i].length()>0)
							{ 
								sample[z] = ((int)(Float.parseFloat(data_z[i]))); 
								z++;
							}
					
					Log.w("PlayerActivity", "sample tot: "+z);
					
					play.setEnabled(false);
					pause.setEnabled(true);
					
					t = new Thread("Thumbnail_Decoding"){
						public void run() {
							// setta la priorità massima del thread
			                setPriority(Thread.MAX_PRIORITY);
			                
			                // converto la stringa in una immagine bitmap
			        		byte[] decodedImgByteArray = Base64.decode(image, Base64.DEFAULT);
			        		bmp = BitmapFactory.decodeByteArray(decodedImgByteArray, 0, decodedImgByteArray.length);
							
							runOnUiThread(new Runnable() {
		                        @Override
		                        public void run() {
		                        	thumbnail.setImageBitmap(bmp);
		                        }
		                    });
						}
					};
					t.start();
				
					duration = MusicUpsampling.duration(upsampling, sample.length, PlayerTrack.SOUND_RATE_48000);
					
					// avvio subito la riproduzione della musica
					intentPlayer.putExtra(ACC_DATA, sample);
					intentPlayer.putExtra(UPSAMPLING, upsampling);
					startService(intentPlayer);
				}
			}
			
			// blocco lo spostamento della seekBar
			sb_musicProgress.setOnTouchListener(new OnTouchListener(){
		        @Override
		        public boolean onTouch(View v, MotionEvent event) {
		            return true;
		        }
		    });
			
			thumbnail.setImageBitmap(bmp);
			
/////////////////////////////////////////////////////////
////////////aggiungo listener ai bottoni ///////////////
////////////////////////////////////////////////////////

			/**** play music ****/
			play.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					/* delay per permettere al servizio di mettersi in pausa correttamente
					 * previene problemi nel caso venga premuto pausa e play velocemente
					*/
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					// verifica che lo speacker non stia riproducendo altra musica
					if(!((AudioManager)getSystemService(Context.AUDIO_SERVICE)).isMusicActive())
					{
						if(isPause){
							isPause = false;
							
							// invio comando di pausa
							Intent intent = new Intent(NOTIFICATION);
							intent.putExtra(PlayerTrack.COMMAND, PlayerTrack.PLAY_MUSIC);
							sendBroadcast(intent);
							
							play.setEnabled(false);
							pause.setEnabled(true);
						}
					}
					else{
						Toast.makeText(v.getContext(), getString(R.string.notify_speaker_occuped), Toast.LENGTH_SHORT).show();
					}
				}
			});
			
			/**** pause music ****/
			pause.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					isPause = true;
					
					// invio comando di pausa
					Intent intent = new Intent(NOTIFICATION);
					intent.putExtra(PlayerTrack.COMMAND, PlayerTrack.PAUSE_MUSIC);
					sendBroadcast(intent);
					
					play.setEnabled(true);
					pause.setEnabled(false);
				}
			});
			
			/**** stop music ****/
			stop.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// stoppo la musica e chiudi activity
					onBackPressed();
				}
			});
			
			/**** Avvia l'activity per esportare session ****/
			export.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					pause.performClick();
					// avvio il fileExplorer per esportazione
					Intent i = new Intent(view.getContext(), FileExplorer.class);
					i.putExtra(DbAdapter.T_SESSION_SESSIONID, sessionId);
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
    	setMaxDuration();
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    }
    
    @Override
	public void onBackPressed() {
	    stopService(); // stoppa il servizio della musica
	    super.onBackPressed();
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) 
    {
    	savedInstanceState.putLong(SESSION_ID, sessionId);
    	savedInstanceState.putString(SESSION_NAME, sessionName.getText().toString());
    	savedInstanceState.putBoolean(PAUSE, isPause);
    	savedInstanceState.putInt(UPSAMPLING, upsampling);
    	savedInstanceState.putIntArray(SAMPLE, sample);
    	savedInstanceState.putString(IMAGE, image);
    	savedInstanceState.putLong(DURATION, duration);
    	savedInstanceState.putParcelable(INTENT_PLAYER, intentPlayer);
    	savedInstanceState.putString(PROGRESS_TIME, currentTimeTV.getText().toString());
    	super.onSaveInstanceState(savedInstanceState);
    }
    
    /*** imposta la durata massima della seekBar e della TextView***/
    public void setMaxDuration(){
    	durationTV.setText("" + Util.millisecondsToMinutesSeconds(duration));
    	sb_musicProgress.setMax((int)duration);
    }
    
    /*** stoppa il servizio in modo automatico ***/
    public void stopService(){
    	Intent intent = new Intent(NOTIFICATION);
		intent.putExtra(PlayerTrack.COMMAND, PlayerTrack.STOP_MUSIC);
		sendBroadcast(intent);
		// un piccolo delay per permettere al servizio di stopparsi correttamente
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
}