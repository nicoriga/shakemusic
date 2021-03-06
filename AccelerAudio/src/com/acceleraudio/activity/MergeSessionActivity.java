package com.acceleraudio.activity;

import java.util.ArrayList;

import com.acceleraudio.database.DbAdapter;
import com.acceleraudio.design.ListSessionAdapter;
import com.acceleraudio.util.RecordedSession;
import com.malunix.acceleraudio.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**
 * @author Nicola Rigato
 * @author Luca Del Salvador
 * @author Marco Tessari
 * @author Gruppo: Malunix
 *
 * permette di riordinare un elenco di sessioni e unire i campioni per ogni asse
 * in modo da creare una nuova sessione 
 */
public class MergeSessionActivity extends Activity{
	
	public static final int MAX_SAMPLE = 11000;
	public static String SESSION_ID_LIST = "mergeSessionActivity.sessionIdList";
	
	private DbAdapter dbAdapter; 
	private Button merge, cancel;
	private EditText sessionName;
	private ListView list;
	private ListSessionAdapter adaperList;
	private long[] sessionIdList;
	private ArrayList<RecordedSession> sessions;
	long rowId;
	private SharedPreferences pref;
	private ListView.OnTouchListener gestureListener;
	private int start_position, previus_position, start_pointToPosition, stop_pointToPosition;
	private long sessionId; 
    
    // Distanza azione riordinamento touch
	// per telefono
//    private static final int SWIPE_OFFSET_PORTRAIT = 250;
//    private static final int SWIPE_OFFSET_LANDSCAPE = 440;
    // per tablet
    private static final int SWIPE_OFFSET_PORTRAIT = 420;
    private static final int SWIPE_OFFSET_LANDSCAPE = 700;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_1);
    	dbAdapter = new DbAdapter(this);
    	pref = PreferenceManager.getDefaultSharedPreferences(this);

    	Bundle b = getIntent().getExtras();
    	sessionIdList = b.getLongArray(DbAdapter.T_SESSION_SESSIONID);
    	
    	try {
    		setContentView(R.layout.merge_layout);
    		
    		merge = (Button)findViewById(R.id.mergeSession_BT_merge);
    		cancel = (Button)findViewById(R.id.mergeSession_BT_cancel);
    		sessionName = (EditText) findViewById(R.id.mergeSession_ET_sessionName);
    		list = (ListView)findViewById(R.id.merge_LV_listSession);
    		
    		if(savedInstanceState != null)
    			sessionIdList = savedInstanceState.getLongArray(SESSION_ID_LIST);
    		
    		// carica la lista delle sessioni usando la sessionIdList
    		loadSessionList();
    			
    		adaperList = new ListSessionAdapter(this, R.layout.list_session_merge_layout, sessions);
    		list.setAdapter(adaperList);
    		
    		// gestore eventi touch
    		gestureListener = new ListView.OnTouchListener() {
    			public boolean onTouch(View v, MotionEvent event) {
    				int action = event.getAction();
    				if(Math.abs(event.getX()) < convertDpToPixel(getOffsetSwipe())){
    					if(start_position>=0)list.getChildAt(start_position).setBackgroundColor(Color.WHITE);
    					if(previus_position>=0)list.getChildAt(previus_position).setBackgroundColor(Color.WHITE);
    					return false;
    				}
    					
		        	try {
						switch(action)
						{
							// quando viene messo il dito nello schermo viene salvata la posizione della
							// riga dove avviene la pressione
							case MotionEvent.ACTION_DOWN:
								start_pointToPosition = list.pointToPosition((int)event.getX(), (int)event.getY());
								start_position = getChildXy(event);
								if(start_position != ListView.INVALID_POSITION){
									list.getChildAt(start_position).setBackgroundColor(Color.YELLOW);
								}
								break;
							// quando si rimuove il dito dallo schermo avviene lo spostamento della sessione
							// nella posizione di arrivo
							case MotionEvent.ACTION_CANCEL: 
							case MotionEvent.ACTION_UP:
								if(start_position >= 0)
								{
						            stop_pointToPosition = list.pointToPosition((int)event.getX(), (int)event.getY());
//							            stop_position = getChildXy(event);
						            moveListRowTo(start_pointToPosition, stop_pointToPosition);
						            list.getChildAt(start_position).setBackgroundColor(Color.WHITE);
						            list.getChildAt(previus_position).setBackgroundColor(Color.WHITE);
								}
								break;
							case MotionEvent.ACTION_HOVER_ENTER:
							case MotionEvent.ACTION_MOVE:
								// coloro la riga dove si trova il dito
								int position = getChildXy(event);
								if(position >= 0 && position != start_position && start_position >= 0)
									{
										list.getChildAt(position).setBackgroundColor(Color.GREEN);
										if(previus_position >= 0 && previus_position != position)list.getChildAt(previus_position).setBackgroundColor(Color.WHITE);
										previus_position = position;
									}
								break;
							default:
								break;
						}
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
    		        	return true;
    		    }
    		};
    		list.setOnTouchListener(gestureListener);
    		
    /////////////////////////////////////////////////////////
    ///////////  aggiungo listener  /////////////////////////
    ////////////////////////////////////////////////////////
    		
    		/**** torna alla lista delle sessioni ****/
    		cancel.setOnClickListener(new View.OnClickListener() {
    			public void onClick(View view) {
    				finish();
    			}
    		});
    		
    		/**** unisce le sessioni nell'ordine impostato ****/
    		merge.setOnClickListener(new View.OnClickListener() {
    			public void onClick(View v) {
    				if (sessionName.getText().length()>0) {
						try {
							merge.setEnabled(false);
							cancel.setEnabled(false);
							
							// unisco le sessioni
							dbAdapter.open();
							sessionId = dbAdapter.mergeSession(
											adaperList.getSessionId(),
											sessionName.getText().toString(),
											(pref.getBoolean(PreferencesActivity.AXIS_X,true) ? 1 : 0),
											(pref.getBoolean(PreferencesActivity.AXIS_Y,true) ? 1 : 0),
											(pref.getBoolean(PreferencesActivity.AXIS_Z,true) ? 1 : 0),
											pref.getInt(PreferencesActivity.UPSAMPLING, 0));
							dbAdapter.close();
							
							// avvia activity con le info della sessione
							Intent i = new Intent(v.getContext(), SessionInfoActivity.class);
							i.putExtra(DbAdapter.T_SESSION_SESSIONID, sessionId);
							v.getContext().startActivity(i);
							finish();
							
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
    				else
    					Toast.makeText(v.getContext(), R.string.error_no_session_name, Toast.LENGTH_SHORT).show();
    			}
    		});
    		
		} catch (Exception e) {
			Toast.makeText(this, R.string.error_interface_load, Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			finish();
		}
	}
	
	@Override
    public void onSaveInstanceState(Bundle savedInstanceState) 
    {
    	savedInstanceState.putLongArray(SESSION_ID_LIST, adaperList.getSessionId());
    	super.onSaveInstanceState(savedInstanceState);
    }
	
///////////////////////////////////////
/////////// METODI UTILI /////////////
//////////////////////////////////////
	
	/*** carica le sessioni nell'interfaccia ***/
	public void loadSessionList()
	{
		// apro la connessione al db
		dbAdapter.open();
				
		// istanzio array
		sessions = new ArrayList<RecordedSession>();
		
		for(long sessionID: sessionIdList)
		{
			// prelevo i campi per ogni sessione
			Cursor cursor = dbAdapter.fetchSessionByIdMinimal(sessionID);
			cursor.moveToFirst();
			
			RecordedSession s = new RecordedSession(sessionID,
													cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_NAME)), 
													cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_DATE_CHANGE)), 
													cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_IMAGE)), 
													cursor.getInt( cursor.getColumnIndex(DbAdapter.T_SESSION_N_DATA_X)) +
													cursor.getInt( cursor.getColumnIndex(DbAdapter.T_SESSION_N_DATA_Y)) +
													cursor.getInt( cursor.getColumnIndex(DbAdapter.T_SESSION_N_DATA_Z)));
			sessions.add(s);
			cursor.close();
		}
		
		dbAdapter.close();
		
	}
	
	/*** ritorna posizione elemento della lista in base alla posizione del tocco ***/
	public int getChildXy(MotionEvent event)
	{
		int adapterIndex = list.pointToPosition((int) event.getX(),(int) event.getY());
		int firstViewItemIndex = list.getFirstVisiblePosition();
		int viewIndex = adapterIndex - firstViewItemIndex;
		return viewIndex;
	}
	
	/*** metodo per convertire i pixel in dp***/
	public float convertDpToPixel(int dip) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, getResources().getDisplayMetrics());
	}
	
	/*** sposta una riga nella posizione indicata ***/
	public void moveListRowTo(int start_position, int stop_position)
	{
		if (start_position >= 0 && stop_position >= 0) {
			
			// sposta la sessione
			sessions.add(stop_position,sessions.remove(start_position));
			adaperList.notifyDataSetChanged();
		}
	}
	
	/*** restituisce l'offset in base alla rotazione dello schermo ***/
	public int getOffsetSwipe()
	{
	    int rotation = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
	    switch (rotation) {
	    case Surface.ROTATION_0:
	    case Surface.ROTATION_180:
	    	return SWIPE_OFFSET_PORTRAIT;
	    case Surface.ROTATION_90:
	    default:
	    	return SWIPE_OFFSET_LANDSCAPE;
	    }
	}
}