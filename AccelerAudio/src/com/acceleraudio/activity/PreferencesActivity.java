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

public class PreferencesActivity extends Activity {
	
	public static String FIRST_START = "first.start";
	public static String AXIS_X = "axis.x";
	public static String AXIS_Y = "axis.y";
	public static String AXIS_Z = "axis.z";
	public static String SAMPLE_RATE = "sample.rate";
	public static String UPSAMPLING = "upsampling";
	public static String TIMER_MINUTES = "timer.minutes";
	public static String TIMER_SECONDS = "timer.seconds";
	
	private SharedPreferences pref;
	private Spinner spinner1, spinner2;
	private CheckBox axis_x, axis_y, axis_z;
	private Button minutesUp, minutesDown, secondsUp, secondsDown;
	private EditText minutesET, secondsET;
	private int minutes, seconds;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_5);
    	pref = PreferenceManager.getDefaultSharedPreferences(this);
	  
////////////////////////////////////////////////////////
///////////// collego widget con xml ///////////////////
///////////////////////////////////////////////////////
    	
    	try {
			axis_x = (CheckBox) findViewById(R.id.UI5_CB_X);
			axis_y = (CheckBox) findViewById(R.id.UI5_CB_Y);
			axis_z = (CheckBox) findViewById(R.id.UI5_CB_Z);
			spinner1 = (Spinner) findViewById(R.id.UI5_SP_frequency);
			spinner2 = (Spinner) findViewById(R.id.UI5_SP_upsampling);
			minutesUp = (Button) findViewById(R.id.UI5_BT_minutePlus);
			minutesDown = (Button) findViewById(R.id.UI5_BT_minuteMinus);
			secondsUp = (Button) findViewById(R.id.UI5_BT_secondPlus);
			secondsDown = (Button) findViewById(R.id.UI5_BT_secondMinus);
			minutesET = (EditText) findViewById(R.id.UI5_ET_minute);
			secondsET = (EditText) findViewById(R.id.UI5_ET_second);
			
			/*** Popolo lo spinner ***/
			ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.sample_rate, android.R.layout.simple_spinner_item);
			adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner1.setAdapter(adapter1); // applico adapter allo spinner
			
			/*** Popolo lo spinner ***/
			ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.upsampling_array, android.R.layout.simple_spinner_item);
			adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner2.setAdapter(adapter2); // applico adapter allo spinner
			
			//carico le preferenze

			LoadPreferences();
		} catch (RuntimeException e) {
			Toast.makeText(this, "Errore caricamento Interfaccia", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			finish();
		}
		
    }
    
    @Override
	public void onResume() {
		super.onResume();
		
/////////////////////////////////////////////////////////
///////////  aggiungo listener  /////////////////////////
////////////////////////////////////////////////////////
			
		final OnCheckedChangeListener axis_change = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
		
		final OnItemSelectedListener spinner_change = new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Editor prefEdit = pref.edit();
				prefEdit.putInt(SAMPLE_RATE, Util.sensorRateByString(spinner1.getSelectedItem().toString()));
	    		prefEdit.putInt(UPSAMPLING, Util.getUpsamplingID(spinner2.getSelectedItem().toString()));
	    		prefEdit.commit();
			}
		
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		};
		spinner1.setOnItemSelectedListener(spinner_change);
		spinner2.setOnItemSelectedListener(spinner_change);
		
		/**** incrementa i minuti ****/
		minutesUp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (minutes < 60) {
					minutes++;
					minutesET.setText("" + minutes);
					updateMinutesSeconds(view);
				}
			}
		});
		
		/**** decrementa i minuti ****/
		minutesDown.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (minutes > 1 || (minutes >0 && seconds >0)) {
					minutes--;
					minutesET.setText("" + minutes);
					updateMinutesSeconds(view);
				}
			}
		});
		
		/**** incrementa i secondi ****/
		secondsUp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (seconds < 60) {
					seconds++;
					secondsET.setText("" + seconds);
					updateMinutesSeconds(view);
				}
			}
		});
		
		/**** decrementa i secondi ****/
		secondsDown.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (seconds > 1 || (seconds >0 && minutes >0)) {
					seconds--;
					secondsET.setText("" + seconds);
					updateMinutesSeconds(view);
				}
			}
		});
	}
    
/////////////////////////////////////////////////////////
///////////  metodi ausiliari  /////////////////////////
////////////////////////////////////////////////////////
    
    // aggiorna le impostazioni
    private void updateMinutesSeconds(View v){
    	if(v != null)
    	{
    		Editor prefEdit = pref.edit();
    		prefEdit.putInt(TIMER_MINUTES, Integer.parseInt(minutesET.getText().toString()));
    		prefEdit.putInt(TIMER_SECONDS, Integer.parseInt(secondsET.getText().toString()));
    		prefEdit.commit();
    	}
    }
    
    // carica le impostazioni dell'applicazione
    private void LoadPreferences()
    {
    	if(pref.getBoolean(FIRST_START, true))
    	{
    		Editor prefEdit = pref.edit();
    		prefEdit.putBoolean(AXIS_X, true);
    		prefEdit.putBoolean(AXIS_Y, true);
    		prefEdit.putBoolean(AXIS_Z, true);
    		prefEdit.putInt(SAMPLE_RATE, SensorManager.SENSOR_DELAY_NORMAL);
    		prefEdit.putInt(UPSAMPLING, Util.getUpsamplingID(getString(R.string.note)));
    		prefEdit.putInt(TIMER_MINUTES, 1);
    		prefEdit.putInt(TIMER_SECONDS, 0);
    		prefEdit.putBoolean(FIRST_START, false);
    		
    		axis_x.setChecked(true); // asse x
    		axis_y.setChecked(true); // asse y
    		axis_z.setChecked(true); // asse z
    		Util.SelectSpinnerItemByValue(spinner1, Util.sensorRateName(SensorManager.SENSOR_DELAY_NORMAL));
    		Util.SelectSpinnerItemByValue(spinner2, getString(R.string.note));
    		minutes = 1;
    		seconds = 0;
    		minutesET.setText("" + minutes);
    		secondsET.setText("" + seconds);
    		
    		prefEdit.commit();
    	}
    	else
    	{
    		axis_x.setChecked(pref.getBoolean(AXIS_X, true)); // asse x
    		axis_y.setChecked(pref.getBoolean(AXIS_Y, true)); // asse y
    		axis_z.setChecked(pref.getBoolean(AXIS_Z, true)); // asse z
    		Util.SelectSpinnerItemByValue(spinner1, Util.sensorRateName(pref.getInt(SAMPLE_RATE, SensorManager.SENSOR_DELAY_NORMAL)));
    		Util.SelectSpinnerItemByValue(spinner2, "" + Util.getUpsamplingName(pref.getInt(UPSAMPLING, Util.getUpsamplingID(getString(R.string.note)))));
    		minutes = pref.getInt(TIMER_MINUTES, 1);
    		seconds = pref.getInt(TIMER_SECONDS, 0);
    		minutesET.setText("" + minutes);
    		secondsET.setText("" + seconds);
    	}
    }

    @Override
	public void onBackPressed() {
	    if(!(axis_x.isChecked() || axis_y.isChecked() || axis_z.isChecked())) 
		{
				Toast.makeText(this, getString(R.string.error_no_axis), Toast.LENGTH_SHORT).show();
		}
	    else
	    	finish();
    }
}