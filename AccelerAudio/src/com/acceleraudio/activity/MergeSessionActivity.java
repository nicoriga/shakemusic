package com.acceleraudio.activity;

import java.util.ArrayList;

import com.acceleraudio.database.DbAdapter;
import com.acceleraudio.design.ListSessionAdapter;
import com.acceleraudio.util.Util;
import com.malunix.acceleraudio.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MergeSessionActivity extends Activity{
	
	private DbAdapter dbAdapter; 
	private Button merge, cancel;
	private EditText sessionName;
	private ListView list;
	private ListSessionAdapter adaperList;
	private ArrayList<Integer> sessionIdList;
	private ArrayList<String> sessionNameList, sessionDataMod, image;
	long rowId;
	private SharedPreferences pref;
	private GestureDetector gestureDetector;
	private View.OnTouchListener gestureListener;
	private int start_position, stop_position, previus_position;
	private long sessionId; 
    
    // Distanza minima richiesta sull'asse Y
    private static final int SWIPE_MIN_DISTANCE = 5;
    // Distanza massima consentita sull'asse X
    private static final int SWIPE_MAX_OFF_PATH = 400;
    // Velocit� minima richiesta sull'asse Y
    private static final int SWIPE_THRESHOLD_VELOCITY = 0;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.ui_1);
    	dbAdapter = new DbAdapter(this);
    	pref = PreferenceManager.getDefaultSharedPreferences(this);
    	
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
    		
//    		gestureDetector = new GestureDetector(list.getContext(), new MyGestureDetector());
    		gestureListener = new View.OnTouchListener() {
    		        public boolean onTouch(View v, MotionEvent event) {
//    		            return gestureDetector.onTouchEvent(event); 
    		        	int action = event.getAction();
    		        	switch(action)
    		        	{
    		        		// quando viene messo il dito nello schermo viene salvata la posizione della
    		        		// riga dove avviene la pressione
    		        		case MotionEvent.ACTION_DOWN:
    		        			start_position = list.pointToPosition((int)event.getX(), (int)event.getY());
    		        			if(start_position >= 0) list.getChildAt(start_position).setBackgroundColor(Color.YELLOW);
    		        			break;
    		        		// quando si rimuove il dito dallo schermo avviene lo spostamento della sessione
    		        		// nella posizione di arrivo
    		        		case MotionEvent.ACTION_CANCEL: 
    		        		case MotionEvent.ACTION_UP:
    		        			if(start_position >= 0)
    		        			{
	    	                        stop_position = list.pointToPosition((int)event.getX(), (int)event.getY());
//	    	                        moveListRowTo(start_position, stop_position);
	    	                        if(stop_position >= 0) list.getChildAt(stop_position).setBackgroundColor(Color.WHITE);
//	    	                        list.getChildAt(previus_position).setBackgroundColor(Color.WHITE);
    		        			}
    		        			break;
    		        		case MotionEvent.ACTION_HOVER_ENTER:
    		        		case MotionEvent.ACTION_MOVE:
    		        			// TODO: coloro la riga dove si trova il dito
    		        			int position = list.pointToPosition((int)event.getX(), (int)event.getY());
    		        			if(position >= 0 && position != start_position && start_position >= 0)
    		        				{
//    		        					list.getChildAt(position).setBackgroundColor(Color.GREEN);
//    		        					if(previus_position >= 0 && previus_position != position)list.getChildAt(previus_position).setBackgroundColor(Color.WHITE);
//    		        					previus_position = position;
    		        					list.getChildAt(position).setBackgroundColor(Color.WHITE);
    		        					list.getChildAt(start_position).setBackgroundColor(Color.YELLOW);
    		        					moveListRowTo(start_position, position);
    		        					start_position = position;
    		        				}
    		        			break;
    		        		default:
    		        			break;
    		        	}
    		        	return true;
    		}};
    		list.setOnTouchListener(gestureListener);
    		
    /////////////////////////////////////////////////////////
    ///////////  aggiungo listener  /////////////////////////
    ////////////////////////////////////////////////////////

//    		/**** avvia l'activity per vedere le info sulla sessione ****/
//    		list.setOnItemClickListener(new OnItemClickListener() {
//    			public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
//    				Intent i = new Intent(view.getContext(), SessionInfoActivity.class);
//    				i.putExtra(DbAdapter.T_SESSION_SESSIONID, sessionIdList.get(position));
//    				view.getContext().startActivity(i);
//    			}
//    		});
            
    		
    		/**** torna alla lista delle sessioni ****/
    		cancel.setOnClickListener(new View.OnClickListener() {
    			public void onClick(View view) {
    				finish();
    			}
    		});
    		
    		final Toast toast = Toast.makeText(this, getString(R.string.error_no_session_name), Toast.LENGTH_SHORT);
    		
    		/**** unisce le sessioni nell'ordine impostato ****/
    		merge.setOnClickListener(new View.OnClickListener() {
    			public void onClick(View v) {
    				if (sessionName.getText().length()>0) {
						try {
							merge.setEnabled(false);
							cancel.setEnabled(false);
							
							// unisco le sessioni
									// apro la connessione al db
									dbAdapter.open();
									sessionId = dbAdapter
											.mergeSession(
													sessionIdList,
													sessionName.getText().toString(),
													(pref.getBoolean(PreferencesActivity.AXIS_X,true) ? 1 : 0),
													(pref.getBoolean(PreferencesActivity.AXIS_Y,true) ? 1 : 0),
													(pref.getBoolean(PreferencesActivity.AXIS_Z,true) ? 1 : 0),
													pref.getInt(PreferencesActivity.UPSAMPLING,
													Util.getUpsamplingID(getString(R.string.note))));
									// chiudo la connessione
									dbAdapter.close();
							
							// avvia activity con le info della sessione
							Intent i = new Intent(v.getContext(), SessionInfoActivity.class);
							i.putExtra(DbAdapter.T_SESSION_SESSIONID, (int)sessionId);
							v.getContext().startActivity(i);
							finish();
							
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
    				else
    					toast.show();
    			}
    		});
    		
		} catch (Exception e) {
			Toast.makeText(this, getString(R.string.error_interface_load), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			finish();
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
//	        if (gestureDetector.onTouchEvent(event))
//	        {
	        	int action = event.getAction();
	        	switch(action)
	        	{
	        		case MotionEvent.ACTION_DOWN:
	        			start_position = list.pointToPosition((int)event.getX(), (int)event.getY());
	        			break;
	        		case MotionEvent.ACTION_CANCEL:
	        		case MotionEvent.ACTION_UP:
                        stop_position = list.pointToPosition((int)event.getX(), (int)event.getY());
                        moveListRowTo(start_position, stop_position);
	        			break;
	        		default:
	        			break;
	        	}
	        	return true;
//	        }
//	        else
//	        	return false;
	}
	
	/*** classe per la gestione dello swipe ***/
	class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                try {
                        if (Math.abs(e1.getX() - e2.getX()) > dpTopx(SWIPE_MAX_OFF_PATH))
                                return false;
                        
                        // Swipe in alto
                        if (e1.getY() - e2.getY() > dpTopx(SWIPE_MIN_DISTANCE) && Math.abs(velocityX) > dpTopx(SWIPE_THRESHOLD_VELOCITY)) {
                                int start_position = list.pointToPosition((int)e1.getX(), (int)e1.getY());
                                int stop_position = list.pointToPosition((int)e2.getX(), (int)e2.getY());
                                moveListRowTo(start_position, stop_position);
                        } else 
                        // swipe in basso
                        	if (e2.getY() - e1.getY() > dpTopx(SWIPE_MIN_DISTANCE) && Math.abs(velocityX) > dpTopx(SWIPE_THRESHOLD_VELOCITY)) {
                                int start_position = list.pointToPosition((int)e1.getX(), (int)e1.getY());
                                int stop_position = list.pointToPosition((int)e2.getX(), (int)e2.getY());
                                moveListRowTo(start_position, stop_position);
                        }
                } catch (Exception e) {
                	e.printStackTrace();
                }
                return true;
        }
	}

	
///////////////////////////////////////
/////////// METODI UTILI /////////////
//////////////////////////////////////
	
	/*** carica le sessioni nell'interfaccia ***/
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
			Cursor cursor = dbAdapter.fetchSessionByIdMinimal(sessionID);
			cursor.moveToFirst();
			
			sessionNameList.add(cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_NAME)));
			sessionDataMod.add(cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_DATE_CHANGE)));
			image.add(cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_IMAGE)));
			
			cursor.close();
		}
		
		dbAdapter.close();
		
	}
	
	/*** metodo per convertire i pixel in dp***/
	public float dpTopx(int dip) {
		float scale = getResources().getDisplayMetrics().density;
		return dip * scale + 0.5f;
	}
	
	/*** scambia due righe nelle posizioni indicate ***/
	public void switchListRow(int start_position, int stop_position)
	{
		if (start_position >= 0 && stop_position >= 0) {
			
			// scambia id
			int tempId = sessionIdList.get(start_position);
			sessionIdList.set(start_position, sessionIdList.get(stop_position));
			sessionIdList.set(stop_position, tempId);
			
			// scambia il nome
			String tempName = sessionNameList.get(start_position);
			sessionNameList.set(start_position,sessionNameList.get(stop_position));
			sessionNameList.set(stop_position, tempName);
			
			// scambia la data di modifica
			String tempDate = sessionDataMod.get(start_position);
			sessionDataMod.set(start_position,sessionDataMod.get(stop_position));
			sessionDataMod.set(stop_position, tempDate);
			
			// scambia l'immagine
			String tempImage = image.get(start_position);
			image.set(start_position, image.get(stop_position));
			image.set(stop_position, tempImage);
			adaperList.notifyDataSetChanged();
		}
	}
	
	/*** sposta una riga nella posizione indicata ***/
	public void moveListRowTo(int start_position, int stop_position)
	{
		if (start_position >= 0 && stop_position >= 0) {
			
			// sposta id
			sessionIdList.add(stop_position, sessionIdList.remove(start_position));
			
			// sposta il nome
			sessionNameList.add(stop_position,sessionNameList.remove(start_position));
			
			// sposta la data di modifica
			sessionDataMod.add(stop_position,sessionDataMod.remove(start_position));
			
			// sposta l'immagine
			image.add(stop_position, image.remove(start_position));
			
			adaperList.notifyDataSetChanged();
		}
	}
}