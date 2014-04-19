package com.acceleraudio.activity;

import com.acceleraudio.database.DbAdapter;
import com.example.acceleraudio.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class SecondActivity extends Activity {
	
	private DbAdapter dbAdapter; 
    private Cursor cursor;
    private Button listSession, playSession;
    private EditText et_sessionName, et_result;
    private TextView creation_date, date_change;
    private CheckBox axis_x, axis_y, axis_z;
    private Spinner spinner;
    private int sessionId;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_2);

////////////////////////////////////////////////////////
///////////// Prelevo dati dall'intent /////////////////
///////////////////////////////////////////////////////  
    	
    	Bundle b = getIntent().getExtras();
    	sessionId = b.getInt(DbAdapter.COLUMN_SESSIONID);
    	 	
////////////////////////////////////////////////////////
///////////// collego widget con xml ///////////////////
///////////////////////////////////////////////////////
    	
    	listSession = (Button) findViewById(R.id.UI2button1M);
    	playSession = (Button) findViewById(R.id.UI2button1);
    	et_sessionName = (EditText)findViewById(R.id.UI2editText1);
    	et_result = (EditText)findViewById(R.id.UI2editText2);
    	creation_date = (TextView)findViewById(R.id.UI2textView2);
    	date_change = (TextView)findViewById(R.id.UI2textView3);
    	axis_x = (CheckBox)findViewById(R.id.UI2checkBox1);
    	axis_y = (CheckBox)findViewById(R.id.UI2checkBox2);
    	axis_z = (CheckBox)findViewById(R.id.UI2checkBox3);
    	spinner = (Spinner) findViewById(R.id.UI2spinner1);
    	
    	/**** POPOLA LA LISTA DELLO SPINNER ****/
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.upsampling_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    	
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
        et_sessionName.setText(cursor.getString( cursor.getColumnIndex(DbAdapter.COLUMN_NAME))); // carico il nome della sessione
        et_result.setText(cursor.getString( cursor.getColumnIndex(DbAdapter.COLUMN_SENSOR_DATA_X))); // TODO: da eliminare server solo per prova carico i dati registraati PROVA
        creation_date.setText(creation_date.getText() + " " + cursor.getString( cursor.getColumnIndex(DbAdapter.COLUMN_CREATION_DATE))); // carico data creazione
        date_change.setText(date_change.getText() + " " + cursor.getString( cursor.getColumnIndex(DbAdapter.COLUMN_DATE_CHANGE))); // carico data modifica
        axis_x.setChecked(cursor.getString( cursor.getColumnIndex(DbAdapter.COLUMN_AXIS_X)).equals("1")); // asse x
        axis_y.setChecked(cursor.getString( cursor.getColumnIndex(DbAdapter.COLUMN_AXIS_Y)).equals("1")); // asse y
        axis_z.setChecked(cursor.getString( cursor.getColumnIndex(DbAdapter.COLUMN_AXIS_Z)).equals("1")); // asse z
        SelectSpinnerItemByValue(spinner, cursor.getString( cursor.getColumnIndex(DbAdapter.COLUMN_UPSAMPLING)));
        
        // chiudo connessioni
        cursor.close();
		dbAdapter.close();
             	    
/////////////////////////////////////////////////////////
//////////// aggiungo listener ai bottoni ///////////////
////////////////////////////////////////////////////////
        
        /**** TORNA ALLA LISTA DELLE SESSIONI ****/
        listSession.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// avvio la prima activity
		    	Intent i = new Intent(v.getContext(), FirstActivity.class);
		    	v.getContext().startActivity(i);
			}
		});
        
        /**** AVVIA IL PLAYER MUSICALE ****/
	    playSession.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// avvio la quarta activity
		    	Intent i = new Intent(v.getContext(), FourthActivity.class);
		    	i.putExtra(DbAdapter.COLUMN_SESSIONID, sessionId);
		    	v.getContext().startActivity(i);
			}
		});
    }
    
    /**** Seleziona item dello spinner in base al valore passato ****/
    public static void SelectSpinnerItemByValue(Spinner spinner, String value)
    {
    	SpinnerAdapter adapter = spinner.getAdapter();
        for (int position = 0; position < adapter.getCount(); position++)
        {
        	String v = adapter.getItem(position).toString();
            if(v.equals(value))
            {
            	spinner.setSelection(position);
                return;
            }
        }
        
    }
}