package com.acceleraudio.activity;

import com.acceleraudio.util.Util;
import com.malunix.acceleraudio.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * @author Nicola Rigato
 * @author Luca Del Salvador
 * @author Marco Tessari
 * @author Gruppo: Malunix
 *
 * permette di modificare le impostazioni predefinite della sessione che viene registrata 
 */
public class PreferencesActivity extends Activity {
	
	public static String FIRST_START = "first.start";
	public static String AXIS_X = "axis.x";
	public static String AXIS_Y = "axis.y";
	public static String AXIS_Z = "axis.z";
	public static String SAMPLE_RATE = "sample.rate";
	public static String UPSAMPLING = "upsampling";
	public static String TIMER_SECONDS = "timer.seconds";
	
	private SharedPreferences pref;
	private Spinner sp_sample_rate, sp_upsampling;
	private CheckBox axis_x, axis_y, axis_z;
	private Button secondsUp, secondsDown;
	private EditText secondsET;
	private int seconds;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_5);
    	pref = PreferenceManager.getDefaultSharedPreferences(this);
	  
    	try {
    		
////////////////////////////////////////////////////////
///////////// collego widget con xml ///////////////////
///////////////////////////////////////////////////////
    		
    		axis_x = (CheckBox) findViewById(R.id.UI5_CB_X);
			axis_y = (CheckBox) findViewById(R.id.UI5_CB_Y);
			axis_z = (CheckBox) findViewById(R.id.UI5_CB_Z);
			sp_sample_rate = (Spinner) findViewById(R.id.UI5_SP_frequency);
			sp_upsampling = (Spinner) findViewById(R.id.UI5_SP_upsampling);
			secondsUp = (Button) findViewById(R.id.UI5_BT_secondPlus);
			secondsDown = (Button) findViewById(R.id.UI5_BT_secondMinus);
			secondsET = (EditText) findViewById(R.id.UI5_ET_second);
    		
			/*** Popolo lo spinner: velocita campionamentov***/
			ArrayAdapter<CharSequence> adapterSampleRate = ArrayAdapter.createFromResource(this, R.array.sample_rate, android.R.layout.simple_spinner_item);
			adapterSampleRate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			sp_sample_rate.setAdapter(adapterSampleRate);
			
			/*** Popolo lo spinner: scelta algoritmo di sovracampionamento ***/
			ArrayAdapter<CharSequence> adapterUpsampling = ArrayAdapter.createFromResource(this, R.array.upsampling_array, android.R.layout.simple_spinner_item);
			adapterUpsampling.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			sp_upsampling.setAdapter(adapterUpsampling);
			
			//carico le preferenze
			LoadPreferences();
			
/////////////////////////////////////////////////////////
///////////  aggiungo listener  /////////////////////////
////////////////////////////////////////////////////////

			/*** aggiorno le impostazioni degl assi scelti ***/
			final OnCheckedChangeListener axis_change = new OnCheckedChangeListener() {
				@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// aggiorna le impostazioni solo se almeno un asse e selezionato
					if (axis_x.isChecked() || axis_y.isChecked() || axis_z.isChecked()) {
						Editor prefEdit = pref.edit();
						prefEdit.putBoolean(AXIS_X, axis_x.isChecked());
						prefEdit.putBoolean(AXIS_Y, axis_y.isChecked());
						prefEdit.putBoolean(AXIS_Z, axis_z.isChecked());
						prefEdit.commit();
					}
				}
			};
			axis_x.setOnCheckedChangeListener(axis_change);
			axis_y.setOnCheckedChangeListener(axis_change);
			axis_z.setOnCheckedChangeListener(axis_change);
			
			/*** aggiorno le impostazioni del campionamento e velocita sensore***/
			final OnItemSelectedListener spinner_change = new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					Editor prefEdit = pref.edit();
					prefEdit.putInt(SAMPLE_RATE, Util.sensorRateByString(sp_sample_rate.getSelectedItem().toString()));
					prefEdit.putInt(UPSAMPLING, Integer.parseInt(sp_upsampling.getSelectedItem().toString()));
					prefEdit.commit();
				}
			
				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			};
			sp_sample_rate.setOnItemSelectedListener(spinner_change);
			sp_upsampling.setOnItemSelectedListener(spinner_change);
			
			/**** incrementa i secondi ****/
			secondsUp.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					if (seconds < 60) {
						seconds++;
						secondsET.setText("" + seconds);
						Editor prefEdit = pref.edit();
			    		prefEdit.putInt(TIMER_SECONDS, Integer.parseInt(secondsET.getText().toString()));
			    		prefEdit.commit();
					}
				}
			});
			
			/**** decrementa i secondi ****/
			secondsDown.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					if (seconds > 5) {
						seconds--;
						secondsET.setText("" + seconds);
						Editor prefEdit = pref.edit();
			    		prefEdit.putInt(TIMER_SECONDS, Integer.parseInt(secondsET.getText().toString()));
			    		prefEdit.commit();
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
	public void onBackPressed() {
    	// impedisce di non selezionare nessun asse 
	    if(!(axis_x.isChecked() || axis_y.isChecked() || axis_z.isChecked())) 
		{
				Toast.makeText(this, getString(R.string.error_no_axis_selected), Toast.LENGTH_SHORT).show();
		}
	    else
	    	finish();
    }
    
/////////////////////////////////////////////////////////
///////////  metodi ausiliari  /////////////////////////
////////////////////////////////////////////////////////
    
    /*** carica le impostazioni dell'applicazione ***/
    private void LoadPreferences()
    {
    	if(pref.getBoolean(FIRST_START, true))
    	{
    		Editor prefEdit = pref.edit();
    		prefEdit.putBoolean(AXIS_X, true);
    		prefEdit.putBoolean(AXIS_Y, true);
    		prefEdit.putBoolean(AXIS_Z, true);
    		prefEdit.putInt(SAMPLE_RATE, SensorManager.SENSOR_DELAY_NORMAL);
    		prefEdit.putInt(UPSAMPLING, 0);
    		prefEdit.putInt(TIMER_SECONDS, 5);
    		prefEdit.putBoolean(FIRST_START, false);
    		
    		// carico i dati nella vista
    		axis_x.setChecked(true);
    		axis_y.setChecked(true);
    		axis_z.setChecked(true);
    		Util.selectSpinnerItemByValue(sp_sample_rate, Util.sensorRateName(SensorManager.SENSOR_DELAY_NORMAL));
    		Util.selectSpinnerItemByValue(sp_upsampling, ""+5);
    		seconds = 5;
    		secondsET.setText("" + seconds);
    		
    		prefEdit.commit();
    	}
    	else
    	{
    		// carico i dati nella vista
    		axis_x.setChecked(pref.getBoolean(AXIS_X, true));
    		axis_y.setChecked(pref.getBoolean(AXIS_Y, true));
    		axis_z.setChecked(pref.getBoolean(AXIS_Z, true));
    		Util.selectSpinnerItemByValue(sp_sample_rate, Util.sensorRateName(pref.getInt(SAMPLE_RATE, SensorManager.SENSOR_DELAY_NORMAL)));
    		Util.selectSpinnerItemByValue(sp_upsampling, "" + (pref.getInt(UPSAMPLING, 0 )));
    		seconds = pref.getInt(TIMER_SECONDS, 0);
    		secondsET.setText("" + seconds);
    	}
    }

}