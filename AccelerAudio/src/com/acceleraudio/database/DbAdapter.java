package com.acceleraudio.database;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
 
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
	
	 
	public DbAdapter(Context context) {
		this.context = context;
	}
 
	public DbAdapter open() throws SQLException {
		dbHelper = new DatabaseHelper(context);
		database = dbHelper.getWritableDatabase();
		return this;
	}
 
	public void close() {
		database.close();
		dbHelper.close();
	}
 
	private String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
	}
	
	private ContentValues createContentValuesSession(String name, int image, int axis_x, int axis_y, int axis_z, int upsampling, String creation_date, String date_change, String sensor_data_x, String sensor_data_y, String sensor_data_z, int n_data_x, int n_data_y, int n_data_z ) {
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
	
	private ContentValues createContentValuesSession(String name, int axis_x, int axis_y, int axis_z, int upsampling, String date_change) {
		ContentValues values = new ContentValues();
		values.put( T_SESSION_NAME, name );
	    values.put( T_SESSION_AXIS_X, axis_x );
	    values.put( T_SESSION_AXIS_Y, axis_y );
	    values.put( T_SESSION_AXIS_Z, axis_z );
	    values.put( T_SESSION_UPSAMPLING, upsampling );
	    values.put( T_SESSION_DATE_CHANGE, date_change );
     
	    return values;
	}
         
	// inserisci nuova sessione
	public long createSession(String name, int image, int axis_x, int axis_y, int axis_z, int upsampling,  String sensor_data_x, String sensor_data_y, String sensor_data_z, int n_data_x, int n_data_y, int n_data_z ) {
		ContentValues values = createContentValuesSession(name, image, axis_x, axis_y, axis_z, upsampling, getDate(), getDate(), sensor_data_x, sensor_data_y, sensor_data_z, n_data_x, n_data_y, n_data_z );
		return database.insertOrThrow(DATABASE_TABLE_SESSION, null, values);
	}
		
	// aggiorna record info sessione 
	public boolean updateSession( long sessionID, String name, int axis_x, int axis_y, int axis_z, int upsampling) {
		ContentValues updateValues = createContentValuesSession(name, axis_x, axis_y, axis_z, upsampling, getDate());
		return database.update(DATABASE_TABLE_SESSION, updateValues, T_SESSION_SESSIONID + "=" + sessionID, null) > 0;
	}
	
	// aggiorna record sessione
	public boolean updateSession( long sessionID, String name, int image, int axis_x, int axis_y, int axis_z, int upsampling, String creation_date, String date_change, String sensor_data_x, String sensor_data_y, String sensor_data_z, int n_data_x, int n_data_y, int n_data_z ) {
		ContentValues updateValues = createContentValuesSession(name, image, axis_x, axis_y, axis_z, upsampling, creation_date, date_change,  sensor_data_x, sensor_data_y, sensor_data_z, n_data_x, n_data_y, n_data_z);
		return database.update(DATABASE_TABLE_SESSION, updateValues, T_SESSION_SESSIONID + "=" + sessionID, null) > 0;
	}
                 
	// elimina sessione    
	public boolean deleteSession(long contactID) {
		return database.delete(DATABASE_TABLE_SESSION, T_SESSION_SESSIONID + "=" + contactID, null) > 0;
	}
		
	// preleva tutte le sessioni
	public Cursor fetchAllSession() {
		return database.query(DATABASE_TABLE_SESSION, new String[] { T_SESSION_SESSIONID, T_SESSION_NAME, T_SESSION_IMAGE, T_SESSION_AXIS_X, T_SESSION_AXIS_Y, T_SESSION_AXIS_Z, T_SESSION_UPSAMPLING, T_SESSION_CREATION_DATE, T_SESSION_DATE_CHANGE, T_SESSION_SENSOR_DATA_X, T_SESSION_SENSOR_DATA_Y, T_SESSION_SENSOR_DATA_Z}, null, null, null, null, null);
	}
   
	// preleva sessioni per ID
	public Cursor fetchSessionById(int filter) {
		Cursor mCursor = database.query(true, DATABASE_TABLE_SESSION, new String[] {T_SESSION_SESSIONID, T_SESSION_NAME, T_SESSION_IMAGE, T_SESSION_AXIS_X, T_SESSION_AXIS_Y, T_SESSION_AXIS_Z, T_SESSION_UPSAMPLING, T_SESSION_CREATION_DATE, T_SESSION_DATE_CHANGE, T_SESSION_SENSOR_DATA_X, T_SESSION_SENSOR_DATA_Y, T_SESSION_SENSOR_DATA_Z, T_SESSION_N_DATA_X, T_SESSION_N_DATA_Y, T_SESSION_N_DATA_Z }, T_SESSION_SESSIONID + "=" + filter, null, null, null, null, null);     
		return mCursor;
	}
  
}