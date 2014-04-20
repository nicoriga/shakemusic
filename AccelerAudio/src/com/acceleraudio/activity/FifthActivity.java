package com.acceleraudio.activity;

import com.acceleraudio.database.DbAdapter;
import com.acceleraudio.util.Util;
import com.malunix.acceleraudio.R;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;

public class FifthActivity extends Activity {
	
	private DbAdapter dbAdapter; 
    private Cursor cursor;
	private Spinner spinner1, spinner2;
	private CheckBox axis_x, axis_y, axis_z;
	private Boolean load = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_5);
	    
////////////////////////////////////////////////////////
///////////// collego widget con xml ///////////////////
///////////////////////////////////////////////////////
    	
    	axis_x = (CheckBox) findViewById(R.id.UI5checkBox1);
    	axis_y = (CheckBox) findViewById(R.id.UI5checkBox2);
    	axis_z = (CheckBox) findViewById(R.id.UI5checkBox3);
    	spinner1 = (Spinner) findViewById(R.id.UI5spinner1);
    	spinner2 = (Spinner) findViewById(R.id.UI5spinner2);
    	
    	
    	/*** Popolo lo spinner ***/
    	ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.upsampling_array, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1); // applico adapter allo spinner
        
        /*** Popolo lo spinner ***/
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.upsampling_array, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2); // applico adapter allo spinner
        
////////////////////////////////////////////////////////
/// prelevo dati dal database e li carico nella vista///
///////////////////////////////////////////////////////

		// apro la connessione al db
		dbAdapter = new DbAdapter(this);
		dbAdapter.open();
		
		// prelevo record by ID 
		cursor = dbAdapter.fetchAllPreferences();
		cursor.moveToFirst();
		
		axis_x.setChecked(cursor.getString( cursor.getColumnIndex(DbAdapter.T_PREFERENCES_AXIS_X)).equals("1")); // asse x
		axis_y.setChecked(cursor.getString( cursor.getColumnIndex(DbAdapter.T_PREFERENCES_AXIS_Y)).equals("1")); // asse y
		axis_z.setChecked(cursor.getString( cursor.getColumnIndex(DbAdapter.T_PREFERENCES_AXIS_Z)).equals("1")); // asse z
		String sample_rate = cursor.getString( cursor.getColumnIndex(DbAdapter.T_PREFERENCES_SAMPLE_RATE));
		String upsampling = cursor.getString( cursor.getColumnIndex(DbAdapter.T_PREFERENCES_UPSAMPLING));
		Util.SelectSpinnerItemByValue(spinner1, cursor.getString( cursor.getColumnIndex(DbAdapter.T_PREFERENCES_SAMPLE_RATE)));
		Util.SelectSpinnerItemByValue(spinner2, cursor.getString( cursor.getColumnIndex(DbAdapter.T_PREFERENCES_UPSAMPLING)));
		
		// chiudo connessioni
		cursor.close();
		dbAdapter.close();

		load = true;
		
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
				if(load) updateChange(arg1);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		};
		spinner1.setOnItemSelectedListener(spinner_change);
		spinner2.setOnItemSelectedListener(spinner_change);
    }
    
/////////////////////////////////////////////////////////
///////////  metodi ausiliari  /////////////////////////
////////////////////////////////////////////////////////
    
    private void updateChange(View v){
    	// apro la connessione al db
    	dbAdapter = new DbAdapter(v.getContext());
    	dbAdapter.open();
    	
    	// aggiorno i dati delle preferenze
		dbAdapter.updatePreferences( (axis_x.isChecked()? 1 : 0), (axis_y.isChecked()? 1 : 0), (axis_z.isChecked()? 1 : 0), Integer.parseInt(spinner2.getSelectedItem().toString()), Integer.parseInt(spinner1.getSelectedItem().toString()), 1, 0);
		
		// chiudo la connessione al db
		dbAdapter.close();
    }

}