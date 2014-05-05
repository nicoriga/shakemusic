package com.acceleraudio.activity;

import java.util.ArrayList;

import com.acceleraudio.database.DbAdapter;
import com.acceleraudio.design.CustomListSession;
import com.malunix.acceleraudio.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
 
public class ListSessionActivity extends Activity {

	private Button newSession, preferences;
	private ListView list;
	private ArrayList<Integer> sessionIdList;
	private ArrayList<String> sessionNameList;
	private String[] sessionDataMod;
	private Integer[] imageId;
	private DbAdapter dbAdapter; 
    private Cursor cursor;
    private Context context;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_1);
    	dbAdapter = new DbAdapter(this);
    	
////////////////////////////////////////////////////////
///////////// collego widget con xml ///////////////////
///////////////////////////////////////////////////////
    	
    	newSession = (Button)findViewById(R.id.UI1button1);
    	preferences = (Button)findViewById(R.id.UI1button3);
    	list = (ListView)findViewById(R.id.UI1listSession);

    }
	
	@Override
	public void onResume() {
		super.onResume();
	    
////////////////////////////////////////////////////////
///////////// Popolo la listview ///////////////////////
///////////////////////////////////////////////////////

		// apro la connessione al db
		dbAdapter.open();
		
		// prelevo tutti i record 
		cursor = dbAdapter.fetchAllSession();
		
		// istanzio array
		sessionIdList = new ArrayList<Integer>();
		sessionNameList = new ArrayList<String>();
		//sessionName = new String[cursor.getCount()];
		sessionDataMod = new String[cursor.getCount()];
		imageId = new Integer[cursor.getCount()];
		
		cursor.moveToFirst();
		int i = 0;
		while ( !cursor.isAfterLast() ) {
			sessionIdList.add(cursor.getInt( cursor.getColumnIndex(DbAdapter.T_SESSION_SESSIONID)));
			sessionNameList.add(cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_NAME)));
			sessionDataMod[i] = cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_DATE_CHANGE) );
			imageId[i] = cursor.getInt( cursor.getColumnIndex(DbAdapter.T_SESSION_IMAGE));
			cursor.moveToNext();
			i++;
		}
		
		cursor.close();
		dbAdapter.close();
		
		// carico i dati nella listView
		//if(i>0)
		//{
		CustomListSession adapter1 = new CustomListSession(this, sessionNameList, sessionDataMod, imageId);
		list.setAdapter(adapter1);
		registerForContextMenu(list);
		//}
		
/////////////////////////////////////////////////////////
///////////  aggiungo listener  /////////////////////////
////////////////////////////////////////////////////////

		/**** Avvia l'activity per registrare una nuova session ****/
		newSession.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// avvio la Recordactivity
				Intent i = new Intent(view.getContext(), RecordActivity.class);
				view.getContext().startActivity(i);
			}
		});
		
		/**** avvia l'activity per modificare le preferenze predefinite ****/
		preferences.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// avvio la PreferencesActivity
				Intent i = new Intent(view.getContext(), PreferencesActivity.class);
				view.getContext().startActivity(i);
			}
		});
		
		/**** avvia l'activity per vedere le info sulla sessione ****/
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// avvio la SessionInfoActivity activity
				Intent i = new Intent(view.getContext(), SessionInfoActivity.class);
				i.putExtra(DbAdapter.T_SESSION_SESSIONID, sessionIdList.get(position));
				view.getContext().startActivity(i);
			}
		});
		
	}
	
	//creazione del menu contestuale
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
	{
		if (v.getId()==R.id.UI1listSession) 
		{
			context = v.getContext();
			//AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
			menu.setHeaderTitle("Opzioni");
			String[] menuItems = {"play", "duplica", "esporta", "elimina"};
			for (int i = 0; i<menuItems.length; i++) {
				menu.add(Menu.NONE, i, i, menuItems[i]);
			}
		}
	}

	// svolge azione dal menu contestuale
	@Override
	public boolean onContextItemSelected(MenuItem item) 
	{
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		// esegue un azione diversa per le opzioni del menu
		switch(item.getItemId()) 
		{
			case 0:
				// avvio la PlayerActivity
				Intent i = new Intent(context, PlayerActivity.class);
				i.putExtra(DbAdapter.T_SESSION_SESSIONID, sessionIdList.get(info.position));
				context.startActivity(i);
				return true;
			case 1:
				//TODO: duplica la sessione
				return true;
			case 2:
				//TODO: esporta la sessione
				return true;	
			case 3:
				//TODO: elimina la sessione
				return true;	
			default:
				return super.onContextItemSelected(item);
	  }
	}
}