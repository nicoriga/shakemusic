package com.acceleraudio.activity;

import com.acceleraudio.database.DbAdapter;
import com.acceleraudio.util.Util;
import com.malunix.acceleraudio.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SessionInfoActivity extends Activity {
	
	private static String SESSION_ID = "sessionInfoActivity.session_id";
	private static String CREATION_DATE = "sessionInfoActivity.creation_date";
	private static String DATE_CHANGE = "sessionInfoActivity.date_change";
	private static String IMAGE = "sessionInfoActivity.image";
	
	private DbAdapter dbAdapter; 
    private Cursor cursor;
    private Button listSession, playSession;
    public static ImageView thumbnail;
    private EditText et_sessionName;
    private TextView creation_date, date_change;
    private CheckBox axis_x, axis_y, axis_z;
    private Spinner sp_upsampling;
    private int sessionId;
    private String image;
    private Thread t;
    static final Bitmap bmp = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_2);
    	
    	try {
    		
////////////////////////////////////////////////////////
///////////// collego widget con xml ///////////////////
///////////////////////////////////////////////////////
    		
			listSession = (Button) findViewById(R.id.UI2_BT_listsession);
			playSession = (Button) findViewById(R.id.UI2_button_play);
			et_sessionName = (EditText)findViewById(R.id.UI2_ET_sessionTitle);
			thumbnail = (ImageView) findViewById(R.id.UI2_IV_thumbnail);
			thumbnail.setImageResource(R.drawable.ic_launcher);
			creation_date = (TextView)findViewById(R.id.UI2_TV_creationDate);
			date_change = (TextView)findViewById(R.id.UI2_TV_ModifiedDate);
			axis_x = (CheckBox)findViewById(R.id.UI2_CB_X);
			axis_y = (CheckBox)findViewById(R.id.UI2_CB_Y);
			axis_z = (CheckBox)findViewById(R.id.UI2_CB_Z);
			sp_upsampling = (Spinner) findViewById(R.id.UI2_SP_upsampling);
			
			/**** POPOLA LA LISTA DELLO SPINNER ****/
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.upsampling_array, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			sp_upsampling.setAdapter(adapter);
			
			if (savedInstanceState != null)
			{
				sessionId = savedInstanceState.getInt(SESSION_ID);
				creation_date.setText(savedInstanceState.getString(CREATION_DATE));
				date_change.setText(savedInstanceState.getString(DATE_CHANGE));
				image = savedInstanceState.getString(IMAGE);
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
				et_sessionName.setText(cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_NAME))); // carico il nome della sessione
				image = (cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_IMAGE)));
				creation_date.setText(creation_date.getText() + " " + cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_CREATION_DATE))); // carico data creazione
				date_change.setText(date_change.getText() + " " + cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_DATE_CHANGE))); // carico data modifica
				axis_x.setChecked(cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_AXIS_X)).equals("1")); // asse x
				axis_y.setChecked(cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_AXIS_Y)).equals("1")); // asse y
				axis_z.setChecked(cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_AXIS_Z)).equals("1")); // asse z
				Util.SelectSpinnerItemByValue(sp_upsampling, Util.getUpsamplingName(Integer.parseInt(cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_UPSAMPLING)))));
				
				// chiudo connessioni
				cursor.close();
				dbAdapter.close();
				
				
			}
			
			// TODO se possibile decodificare solo la prima volta il thumbnail
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
////////////aggiungo listener cambio info ///////////////
////////////////////////////////////////////////////////

			et_sessionName.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}
				
				@Override
				public void afterTextChanged(Editable s) {
					updateChange(et_sessionName);
				}
			});
			
			final OnCheckedChangeListener axis_change = new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					updateChange(buttonView);
				}
			};
			axis_x.setOnCheckedChangeListener(axis_change);
			axis_y.setOnCheckedChangeListener(axis_change);
			axis_z.setOnCheckedChangeListener(axis_change);
			
			final OnItemSelectedListener spinner_change = new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> arg0, View v, int arg2, long arg3) {
					updateChange(v);
				}
				
				@Override
				public void onNothingSelected(AdapterView<?> arg0) {				
				}
			};
			
			sp_upsampling.setOnItemSelectedListener(spinner_change);
			
/////////////////////////////////////////////////////////
////////////aggiungo listener ai bottoni ///////////////
////////////////////////////////////////////////////////
			
			final Toast toast = Toast.makeText(this, getString(R.string.error_no_axis_selected), Toast.LENGTH_SHORT);
			
			/**** TORNA ALLA LISTA DELLE SESSIONI ****/
			listSession.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// impedisco di tornare indietro se non viene selezionato almeno un asse
					if(!(axis_x.isChecked() || axis_y.isChecked() || axis_z.isChecked())) 
					{
						toast.show();
					}
					else
						finish();
				}
			});
			
			/**** AVVIA IL PLAYER MUSICALE ****/
			playSession.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// avvio la PlayerActivity
					Intent i = new Intent(v.getContext(), PlayerActivity.class);
					i.putExtra(DbAdapter.T_SESSION_SESSIONID, sessionId);
					v.getContext().startActivity(i);
				}
			});
		
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
	public void onBackPressed() {
		// impedisce di tornare indietro se non viene selezionato nessun asse
	    if(!(axis_x.isChecked() || axis_y.isChecked() || axis_z.isChecked())) 
		{
				Toast.makeText(this, getString(R.string.error_no_axis_selected), Toast.LENGTH_SHORT).show();
		}
	    else
	    	finish();
    }
	
	@Override
    public void onSaveInstanceState(Bundle savedInstanceState) 
    {
		savedInstanceState.putInt(SESSION_ID, sessionId);
    	savedInstanceState.putString(CREATION_DATE, creation_date.getText().toString());
    	savedInstanceState.putString(DATE_CHANGE, date_change.getText().toString());
      	savedInstanceState.putString(IMAGE, image);
    	super.onSaveInstanceState(savedInstanceState);
    }
	
/////////////////////////////////////////////////////////
///////////  metodi ausiliari  /////////////////////////
////////////////////////////////////////////////////////

    // aggiorna le impostazioni nel database
	private void updateChange(View v){
		if(v != null)
		{
			try {
				// apro la connessione al db
				dbAdapter = new DbAdapter(v.getContext());
				dbAdapter.open();
				
				// aggiorno i dati delle preferenze
				dbAdapter.updateSession(sessionId, et_sessionName.getText().toString(), (axis_x.isChecked()? 1 : 0), (axis_y.isChecked()? 1 : 0), (axis_z.isChecked()? 1 : 0), Util.getUpsamplingID(sp_upsampling.getSelectedItem().toString()));
				
				// chiudo la connessione al db
				dbAdapter.close();
				
			} catch (NumberFormatException e) {
				Toast.makeText(this, getString(R.string.error_number_format), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (SQLException e) {
				if(dbAdapter.isOpen()) dbAdapter.close();
				Toast.makeText(this, getString(R.string.error_database_update_change), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
	}
	
}