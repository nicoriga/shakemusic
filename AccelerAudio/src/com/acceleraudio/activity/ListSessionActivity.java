package com.acceleraudio.activity;

import java.util.ArrayList;

import com.acceleraudio.database.DbAdapter;
import com.acceleraudio.design.ListSessionAdapter;
import com.acceleraudio.design.RenameDialog;
import com.acceleraudio.design.RenameDialog.RenameDialogListener;
import com.acceleraudio.util.AvailableSpace;
import com.acceleraudio.util.RecordedSession;
import com.malunix.acceleraudio.R;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
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
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * @author Nicola Rigato
 * @author Luca Del Salvador
 * @author Marco Tessari
 * @author Gruppo: Malunix
 *
 * visualizza elenco sessioni registrate, permette la registrazione di nuove sessioni,
 * la modifica delle impostazioni predefinite.
 * permette inoltre di esportare, unire, rinominare, eliminare le sessioni
 * ed avviare il player musicale
 */
public class ListSessionActivity extends FragmentActivity  implements RenameDialogListener{
	
	public static String SELECTED_MODE = "listSessionActivity.selectedMode";
	public static String SESSION_SELECTED_LIST = "listSessionActivity.sessionSelectedList";
	public static String SESSION_SELECTED_LIST_SIZE = "listSessionActivity.sessionSelectedListSize";
	public static String TOT_SAMPLE = "listSessionActivity.totSample";
	
	private Button newSession, preferences, merge, next, cancel;
	private ListView list;
	private ArrayList<RecordedSession> sessions;
	private DbAdapter dbAdapter; 
    private Context context;
    private ListSessionAdapter adaperList, adaperListCheck;  
    private Thread t;
    private boolean select_mode = false;
    private long lastId = -1;
    private int focusPosition, totSample;
    public static ProgressBar totSamplePB;
    private long[] selectedSessionId;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_1);
    	dbAdapter = new DbAdapter(this);
    	Cursor cursor = null;
    	try {
    		
    		if(savedInstanceState != null)
    			select_mode = savedInstanceState.getBoolean(SELECTED_MODE);
    		
    		// carico l'interfaccia
    		loadInterface();
    		
    		///////// prelevo i dati dal database ///////////
			dbAdapter.open();
			
			/* mi salvo l'ultimo id per verificare se viene registrata una nuova sessione
			 * e in tal caso aggiungerla alla lista
			 * */
			lastId = dbAdapter.getMaxId();
			
			// prelevo tutti i record 
			cursor = dbAdapter.fetchAllSession();
			
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
			
			// adapter con elenco sessioni e checkBox per la selezione delle sessioni da unire
			adaperListCheck = new ListSessionAdapter(this, R.layout.list_session_select_layout, sessions);
			// adapter con solo elenco sessioni
			adaperList = new ListSessionAdapter(this, (R.layout.list_session_layout), sessions);
			list.setAdapter(adaperList);
			
			if(savedInstanceState != null){
				// gestisto la rotazione nel caso si fosse nelle modalit� di selezione delle sessioni da unire
				if(savedInstanceState.getInt(SESSION_SELECTED_LIST_SIZE)>0){
					totSample = savedInstanceState.getInt(TOT_SAMPLE);
					selectedSessionId = savedInstanceState.getLongArray(SESSION_SELECTED_LIST);
					adaperListCheck.setSelectedSession(totSample,selectedSessionId);
				}
			}
			
		} catch (Exception e) {
			Toast.makeText(this, R.string.error_interface_load, Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			finish();
		}

    }
	
	@Override
	public void onResume() {
		super.onResume();
		
		Cursor cursor = null;
		
		try {
			// carico interfaccia
			loadInterface();
			
			// apro la connessione al db
			dbAdapter.open();
			
			long maxId = dbAdapter.getMaxId();
			
			// Verifico se si � registrata una nuova sessione
			// in quel caso aggiungo la nuova sessione alla lista
			if(lastId < maxId)
			{
				lastId = maxId;
				
				// prelevo dati della nuova sessione
				cursor = dbAdapter.fetchSessionByIdMinimal(lastId);
				cursor.moveToFirst();
				if ( !cursor.isAfterLast() ) {
					RecordedSession s = new RecordedSession(cursor.getLong( cursor.getColumnIndex(DbAdapter.T_SESSION_SESSIONID)),
															cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_NAME)),
															cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_DATE_CHANGE)),
															cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_IMAGE)),
															cursor.getInt( cursor.getColumnIndex(DbAdapter.T_SESSION_N_DATA_X)) +
															cursor.getInt( cursor.getColumnIndex(DbAdapter.T_SESSION_N_DATA_Y)) +
															cursor.getInt( cursor.getColumnIndex(DbAdapter.T_SESSION_N_DATA_Z)));
					sessions.add(0,s);
				}
				cursor.close();
			}
			
			// aggiorno il nome e la data modifica della sessione.. potrebbe essere stata rinominata o modificati i parametri
			if(sessions.size()>0){
				cursor = dbAdapter.fetchSessionByIdMinimal(sessions.get(focusPosition).getId());
				cursor.moveToFirst();
				RecordedSession x;
				if ( !cursor.isAfterLast() )
				{
					x = sessions.get(focusPosition);
					x.setName(cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_NAME)));
					x.setModifiedDate(cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_DATE_CHANGE)));
				}
			cursor.close();
			}
			adaperList.notifyDataSetChanged();
			
			dbAdapter.close();
			
			list.setSelection(focusPosition);
			registerForContextMenu(list);	
		
		} catch (SQLException e) {
			Toast.makeText(this, R.string.error_database, Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			if(cursor != null & !cursor.isClosed())
				cursor.close();
			if(!dbAdapter.isOpen())
				dbAdapter.close();
			finish();
		} catch (RuntimeException e) {
			Toast.makeText(this, R.string.error_interface_load, Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			finish();
		} 
	}
	
	@Override
	public void onPause(){
		super.onPause();
	}
	
	@Override
	public void onBackPressed(){
		if(select_mode)
			cancel.performClick();
		else
			super.onBackPressed();
	}
	
	@Override
    public void onSaveInstanceState(Bundle savedInstanceState) 
    {
		savedInstanceState.putBoolean(SELECTED_MODE, select_mode);
		savedInstanceState.putInt(SESSION_SELECTED_LIST_SIZE, adaperListCheck.getSelectedSize());
		if(adaperListCheck.getSelectedSize() > 0){
			savedInstanceState.putLongArray(SESSION_SELECTED_LIST, adaperListCheck.getSelectedSessionId());
			savedInstanceState.putInt(TOT_SAMPLE, adaperListCheck.getTotSample());
		}
    	super.onSaveInstanceState(savedInstanceState);
    }
	
	/**** creazione del menu contestuale ****/
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
	{
		if (v.getId()==R.id.UI1_LV_listSession) 
		{
			context = v.getContext();
			menu.setHeaderTitle(R.string.option);
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
				focusPosition = info.position;
				// verifico che lo speacker non sia occupato
				if(!((AudioManager)getSystemService(Context.AUDIO_SERVICE)).isMusicActive()){
					// Avvio la PlayerActivity
					Intent i = new Intent(context, PlayerActivity.class);
					i.putExtra(DbAdapter.T_SESSION_SESSIONID, sessions.get(info.position).getId());
					context.startActivity(i);
				}
				else
					Toast.makeText(this, R.string.notify_speaker_occuped, Toast.LENGTH_SHORT).show();
				return true;
			
			case 1:
				// Duplica la sessione
				
				// verifica che ci sia almeno 1 MB di spazio disponibile
				if(AvailableSpace.getinternalAvailableSpace(AvailableSpace.SIZE_MB)>1){
					final int x = info.position;
					t = new Thread("duplicate_session"){
						public void run() {
							setPriority(Thread.MAX_PRIORITY);
							synchronized (t) {
								try {
									// utilizzo un adapter locale per non aver problemi con l'apertura e chiusura della connessione
									// nel caso si duplichino velocemente pi� sessioni
									DbAdapter dbAdapter = new DbAdapter(context);
									dbAdapter.open();
									RecordedSession s = dbAdapter.duplicateSessionById(sessions.get(x).getId());
									dbAdapter.close();
									if(s !=null){
										sessions.add(x, s);
										lastId = s.getId();
									}
									else
										Toast.makeText(context, R.string.error_database_duplicate_session, Toast.LENGTH_SHORT).show();
										
								} catch (SQLException e) {
									e.printStackTrace();
									if(!dbAdapter.isOpen())
										dbAdapter.close();
									Toast.makeText(context, R.string.error_database_duplicate_session, Toast.LENGTH_SHORT).show();
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
				}
				else
					Toast.makeText(this, R.string.error_memory_low, Toast.LENGTH_SHORT).show();
				return true;
			
			case 2:
				// esporta la sessione
				// Avvio il File Manager
				focusPosition = info.position;
				Intent i1 = new Intent(context, FileExplorer.class);
				i1.putExtra(DbAdapter.T_SESSION_SESSIONID, sessions.get(info.position).getId());
				context.startActivity(i1);
				return true;
			
			case 3:
				// rinomina la sessione
				showRenameDialog(info.position, sessions.get(info.position).getName());
				return true;
			
			case 4:
				// Elimina la sessione
			try {
				focusPosition = (info.position == 0 ? 0 : info.position-1);
				dbAdapter.open();
				if(!dbAdapter.deleteSession(sessions.get(info.position).getId()))
					Toast.makeText(context, R.string.error_database_delete_session, Toast.LENGTH_SHORT).show();
				dbAdapter.close();
				sessions.remove(info.position);
				adaperList.notifyDataSetChanged();
			} catch (SQLException e) {
				e.printStackTrace();
				if(!dbAdapter.isOpen())
					dbAdapter.close();
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
					else
						Toast.makeText(this, R.string.error_database_rename_session, Toast.LENGTH_SHORT).show();
					dbAdapter.close();
					adaperList.notifyDataSetChanged();
				} catch (SQLException e) {
					Toast.makeText(this, R.string.error_database_rename_session, Toast.LENGTH_SHORT).show();
					e.printStackTrace();
					if(!dbAdapter.isOpen())
						dbAdapter.close();
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
			totSamplePB = (ProgressBar)findViewById(R.id.UI1S_PB_totSample);
			totSamplePB.setMax(MergeSessionActivity.MAX_SAMPLE);
			merge.setEnabled(false);
			list.setAdapter(adaperListCheck);
			if(adaperListCheck!=null && adaperListCheck.getTotSample()>0)
				totSamplePB.setProgress(adaperListCheck.getTotSample());
		}
		else
		{
			merge.setEnabled(true);
			list.setAdapter(adaperList);
			registerForContextMenu(list);
			totSample = 0;
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
					Toast.makeText(v.getContext(), R.string.error_no_accelerometer, Toast.LENGTH_SHORT).show();
				}
				// verifico spazio libero nella memoria interna
				else if(AvailableSpace.getinternalAvailableSpace(AvailableSpace.SIZE_MB)>1)
				{
					focusPosition = 0;
					if(select_mode) adaperListCheck.resetSelectedSession();
					select_mode = false;
					Intent i = new Intent(v.getContext(), RecordActivity.class);
					v.getContext().startActivity(i);
				}
				else
					Toast.makeText(v.getContext(), R.string.error_memory_low, Toast.LENGTH_SHORT).show();
			}
		});
		
		/**** avvia l'activity per modificare le preferenze di registrazione ****/
		preferences.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if(select_mode) adaperListCheck.resetSelectedSession();
				select_mode = false;
				Intent i = new Intent(view.getContext(), PreferencesActivity.class);
				view.getContext().startActivity(i);
			}
		});
		
		/**** avvia l'activity per vedere le info sulla sessione ****/
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
				focusPosition = position;
				if(select_mode) adaperListCheck.resetSelectedSession();
				select_mode = false;
				Intent i = new Intent(view.getContext(), SessionInfoActivity.class);
				i.putExtra(DbAdapter.T_SESSION_SESSIONID, sessions.get(position).getId());
				view.getContext().startActivity(i);
			}
		});
		
		/**** visualizza checkbox per selezionare le sessioni da unire ****/
		merge.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if(select_mode) adaperListCheck.resetSelectedSession();
				select_mode = true;
				loadInterface();
			}
		});
		
		/* nel caso si � nella modalit� di selezione
		  si aggiungono i listener per i bottoni in pi�*/
		if (select_mode) {
			
			/**** avvia activity per riordinare le sessioni da unire ****/
			next.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					Intent i = new Intent(view.getContext(), MergeSessionActivity.class);
					i.putExtra(DbAdapter.T_SESSION_SESSIONID, adaperListCheck.getSelectedSessionId());
					
					if(adaperListCheck.getSelectedSize() > 1)
					{
						int nSample = adaperListCheck.getTotSample();
						if(nSample < MergeSessionActivity.MAX_SAMPLE){
							select_mode = false;
							totSample = 0;
							view.getContext().startActivity(i);
							adaperListCheck.resetSelectedSession();
						}
						else
							Toast.makeText(view.getContext(), R.string.error_too_sample, Toast.LENGTH_SHORT).show();
					}
					else
						Toast.makeText(view.getContext(), R.string.error_no_selected_session, Toast.LENGTH_SHORT).show();
				}
			});
			
			/**** annulla unione sessioni ****/
			cancel.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					select_mode = false;
					totSample = 0;
					adaperListCheck.resetSelectedSession();
					setContentView(R.layout.ui_1);
					loadInterface();
				}
			});
		}
	}
}