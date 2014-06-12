package com.acceleraudio.util;

import android.os.Environment;
import android.os.StatFs;

/**
 * @author Malunix
 * 
 * classe per gestire la memoria disponibile
 *
 * sono stati utilizzati dei metodi deprecati perché gli unici disponibili per le API 8
 */
public class AvailableSpace {

    //Variabili
    public final static long SIZE_KB = 1024L;
    public final static long SIZE_MB = SIZE_KB * SIZE_KB;
    public final static long SIZE_GB = SIZE_KB * SIZE_KB * SIZE_KB;

    /**
     * memoria disponibile nella memory card
     * 
	 * @return memoria disponibile espressa in byte
	 */
    @SuppressWarnings("deprecation")
	public static long getExternalAvailableSpaceInBytes() {
    	long availableSpace = -1L;
    	try {
    		StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
    		availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return availableSpace;
	}
    
    /**
     * memoria disponibile nel telefono
     * 
	 * @return memoria disponibile espressa in byte
	 */
	@SuppressWarnings("deprecation")
	public static long getInternalAvailableSpaceInBytes() {
		long availableSpace = -1L;
		try {
			StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
			availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return availableSpace;
	}

    /**
     * memoria disponibile nella memory card espressa nell'unita di misura richiesta
     * 
     * @param type l'unita di misura richiesta
     * <ul>
     * 	<li>{@code SIZE_KB}</li>
     * 	<li>{@code SIZE_MB}</li>
     * 	<li>{@code SIZE_GB}</li>
     * </ul>
     * @return
     */
    public static long getExternalAvailableSpace(long type){
        return getExternalAvailableSpaceInBytes()/type;
    }
    
    /**
     * memoria disponibile nel telefono espressa nell'unita di misura richiesta
     * 
     * @param type l'unita di misura richiesta
     * <ul>
     * 	<li>{@code SIZE_KB}</li>
     * 	<li>{@code SIZE_MB}</li>
     * 	<li>{@code SIZE_GB}</li>
     * </ul>
     * @return
     */
    public static long getinternalAvailableSpace(long type){
        return getInternalAvailableSpaceInBytes()/type;
    }
	
}
