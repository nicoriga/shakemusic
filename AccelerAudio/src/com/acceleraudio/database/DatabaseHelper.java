package com.acceleraudio.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Nicola Rigato
 * @author Luca Del Salvador
 * @author Marco Tessari
 * @author Gruppo: Malunix
 *
 * classe per la gestione del dabatase
 */
public class DatabaseHelper extends SQLiteOpenHelper {
         
	private static final String DATABASE_NAME = "AccelerAudio.db";
    private static final int DATABASE_VERSION = 1;
 
    // stringa creazione del database
    private static final String DATABASE_CREATE_T_SESSION = ""
        		+ "CREATE TABLE session ("
        		+ "_id integer primary key autoincrement, "
        		+ "name text not null, "
        		+ "image text not null,"
        		+ "axis_x int not null, "
        		+ "axis_y int not null, "
        		+ "axis_z int not null, "
        		+ "upsampling int not null, "
        		+ "creation_date text not null, "
        		+ "date_change text not null,"
        		+ "sensor_data_x text not null,"
        		+ "sensor_data_y text not null,"
        		+ "sensor_data_z text not null,"
        		+ "n_data_x int not null,"
        		+ "n_data_y int not null,"
        		+ "n_data_z int not null); ";
        
    // Costruttore
    public DatabaseHelper(Context context) {
    	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Metodo chiamato durante la creazione del database
    @Override
    public void onCreate(SQLiteDatabase db) {
    	db.execSQL(DATABASE_CREATE_T_SESSION);
    }
 
    // Metodo chiamato durante l'upgrade del database
    @Override
    public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) { 
    }
}