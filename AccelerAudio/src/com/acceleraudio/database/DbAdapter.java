package com.acceleraudio.database;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.acceleraudio.util.ImageBitmap;
import com.acceleraudio.util.RecordedSession;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
 
public class DbAdapter {
	@SuppressWarnings("unused")
	private static final String LOG_TAG = DbAdapter.class.getSimpleName();
         
	private Context context;
	private SQLiteDatabase database;
	private DatabaseHelper dbHelper;
	
	// nome tabelle
	private static final String DATABASE_TABLE_SESSION = "session";
	
	// lista colonne tabella session
	public static final String T_SESSION_SESSIONID = "_id";
	public static final String T_SESSION_NAME = "name";
	public static final String T_SESSION_IMAGE = "image";
	public static final String T_SESSION_AXIS_X = "axis_x";
	public static final String T_SESSION_AXIS_Y = "axis_y";
	public static final String T_SESSION_AXIS_Z = "axis_z";
	public static final String T_SESSION_UPSAMPLING = "upsampling";
	public static final String T_SESSION_CREATION_DATE = "creation_date";
	public static final String T_SESSION_DATE_CHANGE = "date_change";
	public static final String T_SESSION_SENSOR_DATA_X = "sensor_data_x";
	public static final String T_SESSION_SENSOR_DATA_Y = "sensor_data_y";
	public static final String T_SESSION_SENSOR_DATA_Z = "sensor_data_z";
	public static final String T_SESSION_N_DATA_X = "n_data_x";
	public static final String T_SESSION_N_DATA_Y = "n_data_y";
	public static final String T_SESSION_N_DATA_Z = "n_data_z";
	
	
	/**
	 * @param context il context corrente
	 */
	public DbAdapter(Context context) 
	{
		this.context = context;
	}
	
	/**
	 * apre la connessione al database
	 * 
	 * @return DbAdapter per eseguire le query nel database
	 * @throws SQLException
	 */
	public DbAdapter open() throws SQLException 
	{
		dbHelper = new DatabaseHelper(context);
		database = dbHelper.getWritableDatabase();
		return this;
	}
	
	/**
	 * chiude la connessione al database
	 */
	public void close() 
	{
		database.close();
		dbHelper.close();
	}
	
	/**
	 * verificare lo stato della connessione al database
	 * 
	 * @return true se la connessione al database è aperta
	 */
	public boolean isOpen() 
	{
		return database.isOpen();
	}
	
	/**
	 * preleva la data corrente del sistema
	 * 
	 * @return la data nel formato dd-MM-yyyy
	 */
	public String getDate() 
	{
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
	}
	
	// creazione valori nuova sessione: con immagine sottoforma di stringa
	private ContentValues createContentValuesSession(String name, String image, int axis_x, int axis_y, int axis_z, int upsampling, String creation_date, String date_change, String sensor_data_x, String sensor_data_y, String sensor_data_z, int n_data_x, int n_data_y, int n_data_z ) 
	{
		ContentValues values = new ContentValues();
		values.put( T_SESSION_NAME, name );
	    values.put( T_SESSION_IMAGE, image );
	    values.put( T_SESSION_AXIS_X, axis_x );
	    values.put( T_SESSION_AXIS_Y, axis_y );
	    values.put( T_SESSION_AXIS_Z, axis_z );
	    values.put( T_SESSION_UPSAMPLING, upsampling );
	    values.put( T_SESSION_CREATION_DATE, creation_date );
	    values.put( T_SESSION_DATE_CHANGE, date_change );
	    values.put( T_SESSION_SENSOR_DATA_X, sensor_data_x );
	    values.put( T_SESSION_SENSOR_DATA_Y, sensor_data_y );
	    values.put( T_SESSION_SENSOR_DATA_Z, sensor_data_z );
	    values.put( T_SESSION_N_DATA_X, n_data_x );
	    values.put( T_SESSION_N_DATA_Y, n_data_y );
	    values.put( T_SESSION_N_DATA_Z, n_data_z );
     
	    return values;
	}
	
	// usata per l'aggiornamento delle info della sessione
	private ContentValues createContentValuesSession(String name, int axis_x, int axis_y, int axis_z, int upsampling, String date_change) 
	{
		ContentValues values = new ContentValues();
		values.put( T_SESSION_NAME, name );
	    values.put( T_SESSION_AXIS_X, axis_x );
	    values.put( T_SESSION_AXIS_Y, axis_y );
	    values.put( T_SESSION_AXIS_Z, axis_z );
	    values.put( T_SESSION_UPSAMPLING, upsampling );
	    values.put( T_SESSION_DATE_CHANGE, date_change );
     
	    return values;
	}
    
	// inserisci nuova sessione: versione con immagine sottoforma di stringa base64
	public long createSession(String name, String image, int axis_x, int axis_y, int axis_z, int upsampling,  String sensor_data_x, String sensor_data_y, String sensor_data_z, int n_data_x, int n_data_y, int n_data_z ) 
	{
		ContentValues values = createContentValuesSession(name, image, axis_x, axis_y, axis_z, upsampling, getDate(), getDate(), sensor_data_x, sensor_data_y, sensor_data_z, n_data_x, n_data_y, n_data_z );
		return database.insertOrThrow(DATABASE_TABLE_SESSION, null, values);
	}
		
	// aggiorna info sessione 
	public boolean updateSession( long sessionID, String name, int axis_x, int axis_y, int axis_z, int upsampling) 
	{
		ContentValues updateValues = createContentValuesSession(name, axis_x, axis_y, axis_z, upsampling, getDate());
		return database.update(DATABASE_TABLE_SESSION, updateValues, T_SESSION_SESSIONID + "=" + sessionID, null) > 0;
	}
	
	// aggiorna nome sessione 
	public boolean updateSessionName( long sessionID, String name) 
	{
		ContentValues updateValues = new ContentValues();
		updateValues.put( T_SESSION_NAME, name );
		return database.update(DATABASE_TABLE_SESSION, updateValues, T_SESSION_SESSIONID + "=" + sessionID, null) > 0;
	}
	
	// aggiorna immagine sessione 
	public boolean updateSessionImage( long sessionID, String image) 
	{
		ContentValues updateValues = new ContentValues();
		updateValues.put( T_SESSION_IMAGE, image );
		return database.update(DATABASE_TABLE_SESSION, updateValues, T_SESSION_SESSIONID + "=" + sessionID, null) > 0;
	}
	
	// elimina sessione 
	public boolean deleteSession(long Id) 
	{
		return database.delete(DATABASE_TABLE_SESSION, T_SESSION_SESSIONID + "=" + Id, null) > 0;
	}
		
	// preleva tutte le sessioni
	public Cursor fetchAllSession() 
	{
		return database.query(DATABASE_TABLE_SESSION, new String[] { T_SESSION_SESSIONID, T_SESSION_NAME, T_SESSION_IMAGE, T_SESSION_CREATION_DATE, T_SESSION_DATE_CHANGE, T_SESSION_N_DATA_X, T_SESSION_N_DATA_Y, T_SESSION_N_DATA_Z}, null, null, null, null, T_SESSION_SESSIONID + " DESC");
	}
   
	// preleva sessione per ID
	public Cursor fetchSessionById(long id) 
	{
		Cursor mCursor = database.query(true, DATABASE_TABLE_SESSION, new String[] {T_SESSION_SESSIONID, T_SESSION_NAME, T_SESSION_IMAGE, T_SESSION_AXIS_X, T_SESSION_AXIS_Y, T_SESSION_AXIS_Z, T_SESSION_UPSAMPLING, T_SESSION_CREATION_DATE, T_SESSION_DATE_CHANGE, T_SESSION_SENSOR_DATA_X, T_SESSION_SENSOR_DATA_Y, T_SESSION_SENSOR_DATA_Z, T_SESSION_N_DATA_X, T_SESSION_N_DATA_Y, T_SESSION_N_DATA_Z }, T_SESSION_SESSIONID + "=" + id, null, null, null, null, null);     
		return mCursor;
	}
	
	// preleva sessione per ID senza i dati dell'accelerometro
	public Cursor fetchSessionByIdMinimal(long sessionId) 
	{
		Cursor mCursor = database.query(true, DATABASE_TABLE_SESSION, new String[] {T_SESSION_SESSIONID, T_SESSION_NAME, T_SESSION_IMAGE, T_SESSION_AXIS_X, T_SESSION_AXIS_Y, T_SESSION_AXIS_Z, T_SESSION_UPSAMPLING, T_SESSION_CREATION_DATE, T_SESSION_DATE_CHANGE, T_SESSION_N_DATA_X, T_SESSION_N_DATA_Y, T_SESSION_N_DATA_Z }, T_SESSION_SESSIONID + "=" + sessionId, null, null, null, null, null);     
		return mCursor;
	}
	
	/**
	 * @return il massimo id presente nel database
	 */
	public long getMaxId() 
	{
		Cursor mCursor = database.rawQuery(" SELECT MAX("+T_SESSION_SESSIONID+") FROM "+DATABASE_TABLE_SESSION, null);
		mCursor.moveToFirst();
		long id = mCursor.getLong(0);
		mCursor.close();
		return id;
	}
	
	/**
	 * duplica sessioni per ID
	 * 
	 * @param id id della sessione
	 * @return un oggetto di tipo RecordedSession
	 */
	public RecordedSession duplicateSessionById(long id) 
	{
		Cursor mCursor = fetchSessionById(id);
		mCursor.moveToFirst();
		if(mCursor.getCount()>0)
		{
			String name = mCursor.getString( mCursor.getColumnIndex(DbAdapter.T_SESSION_NAME));
			String image = mCursor.getString( mCursor.getColumnIndex(DbAdapter.T_SESSION_IMAGE));
			String creation_date = mCursor.getString( mCursor.getColumnIndex(DbAdapter.T_SESSION_CREATION_DATE));
			int axis_x = mCursor.getInt( mCursor.getColumnIndex(DbAdapter.T_SESSION_AXIS_X));
			int axis_y = mCursor.getInt( mCursor.getColumnIndex(DbAdapter.T_SESSION_AXIS_Y));
			int axis_z = mCursor.getInt( mCursor.getColumnIndex(DbAdapter.T_SESSION_AXIS_Z));
			int upsampling = mCursor.getInt( mCursor.getColumnIndex(DbAdapter.T_SESSION_UPSAMPLING));
			String sensor_data_x = mCursor.getString( mCursor.getColumnIndex(DbAdapter.T_SESSION_SENSOR_DATA_X));
			String sensor_data_y = mCursor.getString( mCursor.getColumnIndex(DbAdapter.T_SESSION_SENSOR_DATA_Y));
			String sensor_data_z = mCursor.getString( mCursor.getColumnIndex(DbAdapter.T_SESSION_SENSOR_DATA_Z));
			int n_data_x = mCursor.getInt( mCursor.getColumnIndex(DbAdapter.T_SESSION_N_DATA_X));
			int n_data_y = mCursor.getInt( mCursor.getColumnIndex(DbAdapter.T_SESSION_N_DATA_Y));
			int n_data_z = mCursor.getInt( mCursor.getColumnIndex(DbAdapter.T_SESSION_N_DATA_Z));
			int totSample = n_data_x + n_data_y + n_data_z;
			mCursor.close();
			
			name = name + "_" + (getMaxId()+1);
			ContentValues values = createContentValuesSession( name, image, axis_x, axis_y, axis_z, upsampling, creation_date, getDate(), sensor_data_x, sensor_data_y, sensor_data_z, n_data_x, n_data_y, n_data_z );
			long sessionId = database.insertOrThrow(DATABASE_TABLE_SESSION, null, values);
			
			Bitmap bmp = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
			//costruzione immagine
			ImageBitmap.color(bmp, sensor_data_x.split(" ", 200), sensor_data_y.split(" ", 200), sensor_data_z.split(" ", 200), (int)sessionId);	
			image = ImageBitmap.encodeImage(bmp);
			updateSessionImage(sessionId, image);
			RecordedSession s=new RecordedSession(sessionId, name, getDate(), image, totSample);
			
			return s;
		}
		else
		{
			return null;
		}	
	}
  
	// unisce sessioni per ID
	public long mergeSession(long[] sessionIdList, String name, int axis_x, int axis_y, int axis_z, int upsampling) 
	{
		StringBuilder sensor_data_x = new StringBuilder();
		StringBuilder sensor_data_y = new StringBuilder();
		StringBuilder sensor_data_z = new StringBuilder();
		int n_data_x = 0, n_data_y = 0, n_data_z = 0;
		
		for(long sessionID: sessionIdList)
		{
			// prelevo i campi per ogni sessione
			Cursor cursor = fetchSessionById(sessionID);
			cursor.moveToFirst();
			
			if(cursor.getCount()>0)
			{
				sensor_data_x.append(cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_SENSOR_DATA_X)));
				sensor_data_y.append(cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_SENSOR_DATA_Y)));
				sensor_data_z.append(cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_SENSOR_DATA_Z)));
				n_data_x += cursor.getInt( cursor.getColumnIndex(DbAdapter.T_SESSION_N_DATA_X));
				n_data_y += cursor.getInt( cursor.getColumnIndex(DbAdapter.T_SESSION_N_DATA_Y));
				n_data_z += cursor.getInt( cursor.getColumnIndex(DbAdapter.T_SESSION_N_DATA_Z));
			}
			
			cursor.close();
		}
			
		ContentValues values = createContentValuesSession(name, "", axis_x, axis_y, axis_z, upsampling, getDate(), getDate(), sensor_data_x.toString(), sensor_data_y.toString(), sensor_data_z.toString(), n_data_x, n_data_y, n_data_z );
		long sessionId = database.insertOrThrow(DATABASE_TABLE_SESSION, null, values);
		
		Bitmap bmp = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
		//costruzione immagine
		ImageBitmap.color(bmp, sensor_data_x.toString().split(" ", 200), sensor_data_y.toString().split(" ", 200), sensor_data_z.toString().split(" ", 200), (int)sessionId);	
		String image = ImageBitmap.encodeImage(bmp);
		updateSessionImage(sessionId, image);
		
		return sessionId;
	}
}