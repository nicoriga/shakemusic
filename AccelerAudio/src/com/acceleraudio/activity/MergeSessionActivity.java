package com.acceleraudio.activity;

import java.util.ArrayList;

import com.acceleraudio.database.DbAdapter;
import com.acceleraudio.design.ListSessionAdapter;
import com.malunix.acceleraudio.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MergeSessionActivity extends Activity{
	
	private DbAdapter dbAdapter; 
	private Button merge, cancel;
	private EditText sessionName;
	private ListView list;
	private ListSessionAdapter adaperList;
	private ArrayList<Integer> sessionIdList;
	private ArrayList<String> sessionNameList, sessionDataMod, image;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_1);
    	dbAdapter = new DbAdapter(this);
    	
    	Bundle b = getIntent().getExtras();
    	sessionIdList = b.getIntegerArrayList(DbAdapter.T_SESSION_SESSIONID);
    	
////////////////////////////////////////////////////////
///////////// collego widget con xml ///////////////////
///////////////////////////////////////////////////////
    	
    	try {
    		setContentView(R.layout.merge_layout);
    		
    		merge = (Button)findViewById(R.id.mergeSession_BT_merge);
    		cancel = (Button)findViewById(R.id.mergeSession_BT_cancel);
    		sessionName = (EditText) findViewById(R.id.mergeSession_ET_sessionName);
    		list = (ListView)findViewById(R.id.merge_LV_listSession);
    		
    		loadSession();
    		adaperList = new ListSessionAdapter(this, R.layout.list_session_layout, sessionIdList, sessionNameList, sessionDataMod, image);
    		list.setAdapter(adaperList);
    		
    /////////////////////////////////////////////////////////
    ///////////  aggiungo listener  /////////////////////////
    ////////////////////////////////////////////////////////

    		/**** avvia l'activity per vedere le info sulla sessione ****/
    		list.setOnItemClickListener(new OnItemClickListener() {
    			public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
    				Intent i = new Intent(view.getContext(), SessionInfoActivity.class);
    				i.putExtra(DbAdapter.T_SESSION_SESSIONID, sessionIdList.get(position));
    				view.getContext().startActivity(i);
    			}
    		});
    		
    		/**** torna alla lista delle sessioni ****/
    		cancel.setOnClickListener(new View.OnClickListener() {
    			public void onClick(View view) {
    				finish();
    			}
    		});
    		
    		/**** visualizza checkbox per selezionare le sessioni da unire ****/
    		merge.setOnClickListener(new View.OnClickListener() {
    			public void onClick(View view) {
    			}
    		});
    		
		} catch (Exception e) {
			Toast.makeText(this, getString(R.string.error_interface_load), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			finish();
		}
	}
	
	public void loadSession()
	{
		// apro la connessione al db
		dbAdapter.open();
				
		// istanzio array
		sessionNameList = new ArrayList<String>();
		sessionDataMod = new ArrayList<String>();
		image = new ArrayList<String>();
		
		for(int sessionID: sessionIdList)
		{
			// prelevo i campi per ogni sessione
			Cursor cursor = dbAdapter.fetchSessionById(sessionID);
			cursor.moveToFirst();
			
			sessionNameList.add(cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_NAME)));
			sessionDataMod.add(cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_DATE_CHANGE)));
			image.add(cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_IMAGE)));
			
			cursor.close();
		}
		
		dbAdapter.close();
		
	}
}