package com.acceleraudio.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
         
	private static final String DATABASE_NAME = "AccelerAudio.db";
    private static final int DATABASE_VERSION = 1;
 
    // stringa creazione del database
    private static final String DATABASE_CREATE_T_SESSION = ""
        		+ "CREATE TABLE session ("
        		+ "_id integer primary key autoincrement, "
        		+ "name text not null, "
        		+ "image int not null,"
        		+ "axis_x int not null, "
        		+ "axis_y int not null, "
        		+ "axis_z int not null, "
        		+ "upsampling int not null, "
        		+ "creation_date text not null, "
        		+ "date_change text not null,"
        		+ "sensor_data_x text not null,"
        		+ "sensor_data_y text not null,"
        		+ "sensor_data_z text not null); ";
    private static final String DATABASE_CREATE_T_PREFERENCES = ""
    			+ "CREATE TABLE preferences ("
        		+ "_id integer primary key autoincrement,"
        		+ "axis_x int not null,"
        		+ "axis_y int not null,"
        		+ "axis_z int not null,"
        		+ "upsampling int not null,"
        		+ "sample_rate int not null,"
        		+ "max_minutes,"
        		+ "max_seconds);";
    private static final String DATABASE_AFTER_CREATE_INSERT = ""
    			+ "INSERT INTO preferences (axis_x, axis_y, axis_z, upsampling, sample_rate, max_minutes, max_seconds) VALUES (1, 1, 1, 48000, 100, 1, 0); ";
        
    // Costruttore
    public DatabaseHelper(Context context) {
    	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Metodo chiamato durante la creazione del database
    @Override
    public void onCreate(SQLiteDatabase db) {
    	db.execSQL(DATABASE_CREATE_T_SESSION);
    	db.execSQL(DATABASE_CREATE_T_PREFERENCES);
    	db.execSQL(DATABASE_AFTER_CREATE_INSERT);
    }
 
    // Metodo chiamato durante l'upgrade del database
    @Override
    public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {             
    	db.execSQL("DROP TABLE IF EXISTS session; DROP TABLE IF EXISTS preferences");
        onCreate(db);         
    }
}