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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
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
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_5);
    	pref = PreferenceManager.getDefaultSharedPreferences(this);
	  
////////////////////////////////////////////////////////
///////////// collego widget con xml ///////////////////
///////////////////////////////////////////////////////
    	
    	axis_x = (CheckBox) findViewById(R.id.UI5checkBox1);
    	axis_y = (CheckBox) findViewById(R.id.UI5checkBox2);
    	axis_z = (CheckBox) findViewById(R.id.UI5checkBox3);
    	spinner1 = (Spinner) findViewById(R.id.UI5spinner1);
    	spinner2 = (Spinner) findViewById(R.id.UI5spinner2);
    	
    	
    	/*** Popolo lo spinner ***/
    	ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.sample_rate, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1); // applico adapter allo spinner
        
        /*** Popolo lo spinner ***/
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.upsampling_array, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2); // applico adapter allo spinner
        
////////////////////////////////////////////////////////
////////////////// carico le preferenze ////////////////
///////////////////////////////////////////////////////

        LoadPreferences();
		
    }
    
    @Override
	public void onResume() {
		super.onResume();
		
/////////////////////////////////////////////////////////
///////////  aggiungo listener  /////////////////////////
////////////////////////////////////////////////////////

// TODO: migliorare gestione aggiornamento preferenze predefinite

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
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				updateChange(arg1);
			}
		
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			// TODO: Auto-generated method stub
				
			}
		};
		spinner1.setOnItemSelectedListener(spinner_change);
		spinner2.setOnItemSelectedListener(spinner_change);
	}
    
/////////////////////////////////////////////////////////
///////////  metodi ausiliari  /////////////////////////
////////////////////////////////////////////////////////
    
    // aggiorna le impostazioni
    private void updateChange(View v){
    	if(v != null)
    	{
    		Editor prefEdit = pref.edit();
    		prefEdit.putBoolean(AXIS_X, axis_x.isChecked());
    		prefEdit.putBoolean(AXIS_Y, axis_y.isChecked());
    		prefEdit.putBoolean(AXIS_Z, axis_z.isChecked());
    		prefEdit.putInt(SAMPLE_RATE, Util.sensorRateByString(spinner1.getSelectedItem().toString()));
    		prefEdit.putInt(UPSAMPLING, Integer.parseInt(spinner2.getSelectedItem().toString()));
    		prefEdit.putInt(TIMER_MINUTES, 1);
    		prefEdit.putInt(TIMER_SECONDS, 0);
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
    		prefEdit.putInt(UPSAMPLING, 48000);
    		prefEdit.putInt(TIMER_MINUTES, 1);
    		prefEdit.putInt(TIMER_SECONDS, 0);
    		prefEdit.putBoolean(FIRST_START, false);
    		prefEdit.commit();
    	}
    	else
    	{
    		axis_x.setChecked(pref.getBoolean(AXIS_X, true)); // asse x
    		axis_y.setChecked(pref.getBoolean(AXIS_Y, true)); // asse y
    		axis_z.setChecked(pref.getBoolean(AXIS_Z, true)); // asse z
    		Util.SelectSpinnerItemByValue(spinner1, Util.sensorRateName(pref.getInt(SAMPLE_RATE, SensorManager.SENSOR_DELAY_NORMAL)));
    		Util.SelectSpinnerItemByValue(spinner2, "" + (pref.getInt(UPSAMPLING, 48000)));
    	}
    }
}