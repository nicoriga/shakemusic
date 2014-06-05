package com.acceleraudio.activity;

import java.util.ArrayList;

import com.acceleraudio.database.DbAdapter;
import com.acceleraudio.design.ListSessionAdapter;
import com.acceleraudio.design.RenameDialog;
import com.acceleraudio.design.RenameDialog.RenameDialogListener;
import com.acceleraudio.util.RecordedSession;
import com.malunix.acceleraudio.R;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
 
public class ListSessionActivity extends FragmentActivity  implements RenameDialogListener{

	private Button newSession, preferences, merge, next, cancel;
	private ListView list;
	private ArrayList<RecordedSession> sessions;
	private DbAdapter dbAdapter; 
    private Context context;
    private ListSessionAdapter adaperList, adaperListCheck;  
    private Thread t;
    private boolean select_mode = false;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_1);
    	dbAdapter = new DbAdapter(this);
    	
////////////////////////////////////////////////////////
///////////// collego widget con xml ///////////////////
///////////////////////////////////////////////////////
    	
    	try {
    		loadInterface();
    		
		} catch (Exception e) {
			Toast.makeText(this, getString(R.string.error_interface_load), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			finish();
		}

    }
	
	@Override
	public void onResume() {
		super.onResume();
	    
////////////////////////////////////////////////////////
///////////// Popolo la listview ///////////////////////
///////////////////////////////////////////////////////

		try {
			loadInterface();
			
			// TODO: ricaricare i dati dal database solo se viene registrata una nuova
			// session, attraverso un contatore delle sessioni che verifica ogni volta
			// quando sono presenti nel db.
			// alternativamente vedere di passare i nuovi dati da aggiungere alla lista
			// e aggiornarla
			
			// apro la connessione al db
			dbAdapter.open();
			
			// prelevo tutti i record 
			Cursor cursor = dbAdapter.fetchAllSession();
			
			// istanzio array
			sessions = new ArrayList<RecordedSession>();
			
			cursor.moveToFirst();
			while ( !cursor.isAfterLast() ) {
				RecordedSession s = new RecordedSession(cursor.getLong( cursor.getColumnIndex(DbAdapter.T_SESSION_SESSIONID)),
														cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_NAME)),
														cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_DATE_CHANGE)),
														cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_IMAGE)),
														cursor.getInt( cursor.getColumnIndex(DbAdapter.T_SESSION_N_DATA_X)) +
														cursor.getInt( cursor.getColumnIndex(DbAdapter.T_SESSION_N_DATA_Y)) +
														cursor.getInt( cursor.getColumnIndex(DbAdapter.T_SESSION_N_DATA_Z)));
				sessions.add(s);
				cursor.moveToNext();
			}
			
			cursor.close();
			dbAdapter.close();
			
			adaperListCheck = new ListSessionAdapter(this, R.layout.list_session_select_layout, sessions);
			adaperList = new ListSessionAdapter(this, R.layout.list_session_layout, sessions);
			list.setAdapter(adaperList);
			
			registerForContextMenu(list);	
		
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
	public void onPause(){
		super.onPause();
		select_mode = false;
	}
	
	@Override
	public void onBackPressed(){
		if(select_mode)
			cancel.performClick();
		else
			super.onBackPressed();
	}
	
	/**** creazione del menu contestuale ****/
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
	{
		if (v.getId()==R.id.UI1_LV_listSession) 
		{
			context = v.getContext();
			menu.setHeaderTitle(getString(R.string.option));
			String[] menuItems = {getString(R.string.play), getString(R.string.duplicate), getString(R.string.export), getString(R.string.rename), getString(R.string.delete)};
			for (int i = 0; i<menuItems.length; i++) {
				menu.add(Menu.NONE, i, i, menuItems[i]);
			}
		}
	}

	/**** svolge azione dal menu contestuale ****/
	@Override
	public boolean onContextItemSelected(MenuItem item) 
	{
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		
		// esegue un azione diversa per le opzioni del menu
		switch(item.getItemId()) 
		{
			case 0:
				// Avvio la PlayerActivity
				Intent i = new Intent(context, PlayerActivity.class);
				i.putExtra(DbAdapter.T_SESSION_SESSIONID, sessions.get(info.position).getId());
				context.startActivity(i);
				return true;
			
			case 1:
				// Duplica la sessione
				final int x = info.position;
				t = new Thread("duplicate_session"){
					public void run() {
						setPriority(Thread.MAX_PRIORITY);
						synchronized (t) {
							try {
								// utilizzo un adapter locale per non aver problemi con l'apertura e chiusura della connessione
								DbAdapter dbAdapter = new DbAdapter(context);
								dbAdapter.open();
								RecordedSession s = dbAdapter.duplicateSessionById(sessions.get(x).getId());
								dbAdapter.close();
								sessions.add(x, s);
							} catch (NumberFormatException e) {
								e.printStackTrace();
							} catch (SQLException e) {
								e.printStackTrace();
							} catch (NullPointerException e) {
								e.printStackTrace();
							}
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									
									adaperList.notifyDataSetChanged();
								}
							});
						}
					}
				};
				t.start();
				return true;
			
			case 2:
				// esporta la sessione
				// Avvio il File Manager
				 Intent i1 = new Intent(context, FileExplore.class);
				 context.startActivity(i1);
				return true;
			
			case 3:
				// rinomina la sessione
				showRenameDialog(info.position, sessions.get(info.position).getName());
				return true;
			
			case 4:
				// Elimina la sessione
			try {
				dbAdapter.open();
				dbAdapter.deleteSession(sessions.get(info.position).getId());
				dbAdapter.close();
				sessions.remove(info.position);
				adaperList.notifyDataSetChanged();
			} catch (SQLException e) {
				e.printStackTrace();
			}
				return true;	
			
			default:
				return super.onContextItemSelected(item);
		}
	}
	
	/**** visualizza dialog per rinomina sessione ****/
	private void showRenameDialog(int position, String oldName) {
        FragmentManager fm = getSupportFragmentManager();
        RenameDialog rd = new RenameDialog();
        rd.setSessionInfo(position, oldName);
        rd.show(fm, "rename_dialog");
    }
	
	/**** rinomina la sessione dopo la conferma del dialog ****/
	@Override
	public void onFinishRenameDialog(int position, String sessionName, boolean confirm) {
		if(confirm)
			{
				try {
					dbAdapter.open();
					if(dbAdapter.updateSessionName(sessions.get(position).getId(), sessionName))
					{
						sessions.get(position).setName(sessionName);
					}
					dbAdapter.close();
					adaperList.notifyDataSetChanged();
				} catch (SQLException e) {
					Toast.makeText(this, getString(R.string.error_database_rename_session), Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
	}

	/**** carica l'interfaccia grafica ****/
	public void loadInterface()
	{
		if(select_mode)
			setContentView(R.layout.ui_1_select);
		else 
			setContentView(R.layout.ui_1);
		
		newSession = (Button)findViewById(R.id.UI1_BT_newSession);
		preferences = (Button)findViewById(R.id.UI1_BT_preferences);
		merge = (Button)findViewById(R.id.UI1_BT_mergeSession);
		list = (ListView)findViewById(R.id.UI1_LV_listSession);
		
		if(select_mode)
		{
			next = (Button)findViewById(R.id.UI1_BT_next);
			cancel = (Button)findViewById(R.id.UI1_BT_cancel);
			merge.setEnabled(false);
			list.setAdapter(adaperListCheck);
		}
		else
		{
			merge.setEnabled(true);
			list.setAdapter(adaperList);
			registerForContextMenu(list);
		}
		
		
/////////////////////////////////////////////////////////
///////////  aggiungo listener  /////////////////////////
////////////////////////////////////////////////////////

		/**** Avvia l'activity per registrare una nuova session ****/
		newSession.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// verifico presenza accelerometro
				SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
				Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				if (accelerometer == null)
				{
					newSession.setEnabled(false);
					Toast.makeText(v.getContext(), getString(R.string.error_no_accelerometer), Toast.LENGTH_SHORT).show();
				}
				else
				{
					Intent i = new Intent(v.getContext(), RecordActivity.class);
					v.getContext().startActivity(i);
				}
			}
		});
		
		/**** avvia l'activity per modificare le preferenze di registrazione ****/
		preferences.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent i = new Intent(view.getContext(), PreferencesActivity.class);
				view.getContext().startActivity(i);
			}
		});
		
		/**** avvia l'activity per vedere le info sulla sessione ****/
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
				Intent i = new Intent(view.getContext(), SessionInfoActivity.class);
				i.putExtra(DbAdapter.T_SESSION_SESSIONID, sessions.get(position).getId());
				view.getContext().startActivity(i);
			}
		});
		
		/**** visualizza checkbox per selezionare le sessioni da unire ****/
		merge.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				select_mode = true;
				setContentView(R.layout.ui_1_select);
				loadInterface();
//				merge.setEnabled(false);
//				list.setAdapter(adaperListCheck);
			}
		});
		
		if (select_mode) {
			
			/**** avvia activity per riordinare le sessioni da unire ****/
			next.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					Intent i = new Intent(view.getContext(), MergeSessionActivity.class);
					i.putExtra(DbAdapter.T_SESSION_SESSIONID, adaperListCheck.getSelectedSession());
					
					if(adaperListCheck.getSelectedSize() > 1)
					{
						view.getContext().startActivity(i);
					}
					else
						Toast.makeText(view.getContext(), getString(R.string.error_no_selected_session), Toast.LENGTH_SHORT).show();
				}
			});
			
			/**** annulla unione sessioni ****/
			cancel.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					select_mode = false;
					adaperListCheck.resetSelectedSession();
					setContentView(R.layout.ui_1);
					loadInterface();
//					merge.setEnabled(true);
//					list.setAdapter(adaperList);
				}
			});
		}
	}
}