package com.acceleraudio.activity;

import java.util.ArrayList;

import com.acceleraudio.database.DbAdapter;
import com.acceleraudio.design.CustomListSession;
import com.acceleraudio.design.RenameDialog;
import com.acceleraudio.design.RenameDialog.RenameDialogListener;
import com.malunix.acceleraudio.R;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
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

	private Button newSession, preferences;
	private ListView list;
	private ArrayList<Integer> sessionIdList;
	private ArrayList<String> sessionNameList, sessionDataMod, imageId;
	private DbAdapter dbAdapter; 
    private Context context;
    private CustomListSession adaperList;
    private Thread t;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_1);
    	dbAdapter = new DbAdapter(this);
    	
////////////////////////////////////////////////////////
///////////// collego widget con xml ///////////////////
///////////////////////////////////////////////////////
    	
    	try {
			newSession = (Button)findViewById(R.id.UI1button1);
			preferences = (Button)findViewById(R.id.UI1button2);
			list = (ListView)findViewById(R.id.UI1listSession);
		} catch (Exception e) {
			Toast.makeText(this, "Errore Caricamento Interfaccia", Toast.LENGTH_SHORT).show();
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
			// apro la connessione al db
			dbAdapter.open();
			
			// prelevo tutti i record 
			Cursor cursor = dbAdapter.fetchAllSession();
			
			// istanzio array
			sessionIdList = new ArrayList<Integer>();
			sessionNameList = new ArrayList<String>();
			sessionDataMod = new ArrayList<String>();
			imageId = new ArrayList<String>();
			
			cursor.moveToFirst();
			while ( !cursor.isAfterLast() ) {
				sessionIdList.add(cursor.getInt( cursor.getColumnIndex(DbAdapter.T_SESSION_SESSIONID)));
				sessionNameList.add(cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_NAME)));
				sessionDataMod.add(cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_DATE_CHANGE)));
				imageId.add(cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_IMAGE)));
				cursor.moveToNext();
			}
			
			cursor.close();
			dbAdapter.close();
			
			adaperList = new CustomListSession(this, sessionIdList, sessionNameList, sessionDataMod, imageId);
			list.setAdapter(adaperList);
			
			registerForContextMenu(list);	
		
/////////////////////////////////////////////////////////
///////////  aggiungo listener  /////////////////////////
////////////////////////////////////////////////////////

			/**** Avvia l'activity per registrare una nuova session ****/
			newSession.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					Intent i = new Intent(view.getContext(), RecordActivity.class);
					view.getContext().startActivity(i);
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
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Intent i = new Intent(view.getContext(), SessionInfoActivity.class);
					i.putExtra(DbAdapter.T_SESSION_SESSIONID, sessionIdList.get(position));
					view.getContext().startActivity(i);
				}
			});
		
		} catch (SQLException e) {
			Toast.makeText(this, "Errore Database", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			finish();
		} catch (RuntimeException e) {
			Toast.makeText(this, "Errore caricamento interfaccia", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			finish();
		} 
	}
	
	/**** creazione del menu contestuale ****/
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
	{
		if (v.getId()==R.id.UI1listSession) 
		{
			context = v.getContext();
			menu.setHeaderTitle("Opzioni");
			String[] menuItems = {"play", "duplica", "esporta", "rinomina", "elimina"};
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
				i.putExtra(DbAdapter.T_SESSION_SESSIONID, sessionIdList.get(info.position));
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
								String[] s = dbAdapter.duplicateSessionById(sessionIdList.get(x));
								dbAdapter.close();
								sessionIdList.add(x, Integer.parseInt(s[0]));
								sessionNameList.add(x, s[1]);
								sessionDataMod.add(x, s[2]);
								imageId.add(x, s[3]);
							} catch (NumberFormatException e) {
								e.printStackTrace();
							} catch (SQLException e) {
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
				//TODO: esporta la sessione
				return true;
			
			case 3:
				//TODO: rinomina la sessione
				showRenameDialog(info.position, sessionNameList.get(info.position));
				return true;
			
			case 4:
				// Elimina la sessione
			try {
				dbAdapter.open();
				dbAdapter.deleteSession(sessionIdList.get(info.position));
				dbAdapter.close();
				sessionIdList.remove(info.position);
				sessionNameList.remove(info.position);
				sessionDataMod.remove(info.position);
				imageId.remove(info.position);
				adaperList.notifyDataSetChanged();
			} catch (SQLException e) {
				e.printStackTrace();
			}
				return true;	
			
			default:
				return super.onContextItemSelected(item);
		}
	}
	
	private void showRenameDialog(int position, String oldName) {
        FragmentManager fm = getSupportFragmentManager();
        RenameDialog rd = new RenameDialog();
        rd.setSessionInfo(position, oldName);
        rd.show(fm, "rename_dialog");
    }
	
	@Override
	public void onFinishRenameDialog(int position, String sessionName, boolean confirm) {
		if(confirm)
			{
				try {
					dbAdapter.open();
					if(dbAdapter.renameSession(sessionIdList.get(position), sessionName))
					{
						sessionNameList.set(position, sessionName);
						Toast.makeText(this, "Rinominato " + sessionName, Toast.LENGTH_SHORT).show();
					}
					dbAdapter.close();
					adaperList.notifyDataSetChanged();
				} catch (SQLException e) {
					Toast.makeText(this, "Errore database: rinomina sessione ", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
	}
}