package com.acceleraudio.activity;

import java.util.ArrayList;

import com.acceleraudio.database.DbAdapter;
import com.acceleraudio.design.ListSessionAdapter;
import com.malunix.acceleraudio.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
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
	long rowId;
	private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;
    private int position;
    
    // Distanza minima richiesta sull'asse Y
    private static final int SWIPE_MIN_DISTANCE = 5;
    // Distanza massima consentita sull'asse X
    private static final int SWIPE_MAX_OFF_PATH = 400;
    // Velocità minima richiesta sull'asse Y
    private static final int SWIPE_THRESHOLD_VELOCITY = 5;
	
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

//    		/**** avvia l'activity per vedere le info sulla sessione ****/
//    		list.setOnItemClickListener(new OnItemClickListener() {
//    			public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
//    				Intent i = new Intent(view.getContext(), SessionInfoActivity.class);
//    				i.putExtra(DbAdapter.T_SESSION_SESSIONID, sessionIdList.get(position));
//    				view.getContext().startActivity(i);
//    			}
//    		});
            
            final GestureDetector gestureDetector = new GestureDetector(list.getContext(), new MyGestureDetector());
            View.OnTouchListener gestureListener = new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                	position = list.getPositionForView(v);
                    return gestureDetector.onTouchEvent(event); 
                }};
            list.setOnTouchListener(gestureListener);
    		
//    		/**** cambia l'ordine delle sessioni ****/
//    		list.setOnItemLongClickListener(new OnItemLongClickListener() {
//
//				@Override
//				public boolean onItemLongClick(AdapterView<?> adapter, View view,int position, long id) {
//					view.setBackgroundColor(Color.CYAN);
//					return false;
//				}
//			});
    		
    		/**** torna alla lista delle sessioni ****/
    		cancel.setOnClickListener(new View.OnClickListener() {
    			public void onClick(View view) {
    				finish();
    			}
    		});
    		
    		/**** unisce le sessioni nell'ordine impostato ****/
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

	class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                try {
                        if (Math.abs(e1.getX() - e2.getX()) > dp2px(SWIPE_MAX_OFF_PATH))
                                return false;
                        // Swipe alto e basso
                        if (e1.getY() - e2.getY() > dp2px(SWIPE_MIN_DISTANCE) && Math.abs(velocityX) > dp2px(SWIPE_THRESHOLD_VELOCITY)) {
//                                Toast.makeText(getApplicationContext(), "Swipe in alto", Toast.LENGTH_SHORT).show();
                                int start_position = list.pointToPosition((int)e1.getX(), (int)e1.getY());
                                int stop_position = list.pointToPosition((int)e2.getX(), (int)e2.getY());
                                switchListRow(start_position, stop_position);
                        } else 
                        	if (e2.getY() - e1.getY() > dp2px(SWIPE_MIN_DISTANCE) && Math.abs(velocityX) > dp2px(SWIPE_THRESHOLD_VELOCITY)) {
//                                Toast.makeText(getApplicationContext(), "Swipe in basso", Toast.LENGTH_SHORT).show();
                                int start_position = list.pointToPosition((int)e1.getX(), (int)e1.getY());
                                int stop_position = list.pointToPosition((int)e2.getX(), (int)e2.getY());
                                switchListRow(start_position, stop_position);
                        }
                } catch (Exception e) {
                	e.printStackTrace();
                }
                return false;
        }
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
	        if (gestureDetector.onTouchEvent(event))
	                return true;
	        else
	                return false;
	}
	
	public float dp2px(int dip) {
	        float scale = getResources().getDisplayMetrics().density;
	        return dip * scale + 0.5f;
	}
	
	public void switchListRow(int start_position, int stop_position)
	{
		// scambia id
		int tempId = sessionIdList.get(start_position);
		sessionIdList.set(start_position, sessionIdList.get(stop_position));
		sessionIdList.set(stop_position, tempId);
		
		// scambia il nome
		String tempName = sessionNameList.get(start_position);
        sessionNameList.set(start_position, sessionNameList.get(stop_position));
        sessionNameList.set(stop_position, tempName);
        
        // scambia la data di modifica
        String tempDate = sessionDataMod.get(start_position);
        sessionDataMod.set(start_position, sessionDataMod.get(stop_position));
        sessionDataMod.set(stop_position, tempDate);
        
        // scambia la data di modifica
        String tempImage = image.get(start_position);
        image.set(start_position, image.get(stop_position));
        image.set(stop_position, tempImage);
        
        adaperList.notifyDataSetChanged();
	}
}