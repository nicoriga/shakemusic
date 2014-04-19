package com.acceleraudio.database;

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
	
	// nome database
	private static final String DATABASE_TABLE = "session";
	
	// lista colonne database
	public static final String COLUMN_SESSIONID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_IMAGE = "image";
	public static final String COLUMN_AXIS_X = "axis_x";
	public static final String COLUMN_AXIS_Y = "axis_y";
	public static final String COLUMN_AXIS_Z = "axis_z";
	public static final String COLUMN_UPSAMPLING = "upsampling";
	public static final String COLUMN_CREATION_DATE = "creation_date";
	public static final String COLUMN_DATE_CHANGE = "date_change";
	public static final String COLUMN_SENSOR_DATA = "sensor_data";
	 
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
 
	private ContentValues createContentValues(String name, int image, int axis_x, int axis_y, int axis_z, int upsampling, String creation_date, String date_change, String sensor_data ) {
		ContentValues values = new ContentValues();
		values.put( COLUMN_NAME, name );
	    values.put( COLUMN_IMAGE, image );
	    values.put( COLUMN_AXIS_X, axis_x );
	    values.put( COLUMN_AXIS_Y, axis_y );
	    values.put( COLUMN_AXIS_Z, axis_z );
	    values.put( COLUMN_UPSAMPLING, upsampling );
	    values.put( COLUMN_CREATION_DATE, creation_date );
	    values.put( COLUMN_DATE_CHANGE, date_change );
	    values.put( COLUMN_SENSOR_DATA, sensor_data );
     
	    return values;
	}
         
	// inserisci nuova sessione
	public long createSession(String name, int image, int axis_x, int axis_y, int axis_z, int upsampling, String creation_date, String date_change, String sensor_data ) {
		ContentValues values = createContentValues(name, image, axis_x, axis_y, axis_z, upsampling, creation_date, date_change, sensor_data );
		return database.insertOrThrow(DATABASE_TABLE, null, values);
	}
 
	// aggionra record sessione
	public boolean updateSession( long sessionID, String name, int image, int axis_x, int axis_y, int axis_z, int upsampling, String creation_date, String date_change, String sensor_data ) {
		ContentValues updateValues = createContentValues(name, image, axis_x, axis_y, axis_z, upsampling, creation_date, date_change,  sensor_data);
		return database.update(DATABASE_TABLE, updateValues, COLUMN_SESSIONID + "=" + sessionID, null) > 0;
	}
                 
	// elimina sessione    
	public boolean deleteSession(long contactID) {
		return database.delete(DATABASE_TABLE, COLUMN_SESSIONID + "=" + contactID, null) > 0;
	}
 
	// preleva tutte le sessioni
	public Cursor fetchAllSession() {
		return database.query(DATABASE_TABLE, new String[] { COLUMN_SESSIONID, COLUMN_NAME, COLUMN_IMAGE, COLUMN_AXIS_X, COLUMN_AXIS_Y, COLUMN_AXIS_Z, COLUMN_UPSAMPLING, COLUMN_CREATION_DATE, COLUMN_DATE_CHANGE, COLUMN_SENSOR_DATA}, null, null, null, null, null);
	}
   
	// preleva sessioni per ID
		public Cursor fetchSessionById(int filter) {
			Cursor mCursor = database.query(true, DATABASE_TABLE, new String[] {COLUMN_SESSIONID, COLUMN_NAME, COLUMN_IMAGE, COLUMN_AXIS_X, COLUMN_AXIS_Y, COLUMN_AXIS_Z, COLUMN_UPSAMPLING, COLUMN_CREATION_DATE, COLUMN_DATE_CHANGE, COLUMN_SENSOR_DATA }, COLUMN_SESSIONID + "=" + filter, null, null, null, null, null);     
			return mCursor;
		}
	
	// preleva sessioni filtrate per nome
	public Cursor fetchSessionByFilter(String filter) {
		Cursor mCursor = database.query(true, DATABASE_TABLE, new String[] {COLUMN_SESSIONID, COLUMN_NAME, COLUMN_IMAGE, COLUMN_AXIS_X, COLUMN_AXIS_Y, COLUMN_AXIS_Z, COLUMN_UPSAMPLING, COLUMN_CREATION_DATE, COLUMN_DATE_CHANGE, COLUMN_SENSOR_DATA }, COLUMN_NAME + " like '%"+ filter + "%'", null, null, null, null, null);     
		return mCursor;
	}	
  
}