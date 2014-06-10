package com.acceleraudio.service;

import android.os.Environment;
import android.os.StatFs;


public class AvailableSpaceHandler {

    //Variabili
    public final static long SIZE_KB = 1024L;
    final static long SIZE_MB = SIZE_KB * SIZE_KB;
    public final static long SIZE_GB = SIZE_KB * SIZE_KB * SIZE_KB;

    // Metodi
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
     * KB disponibili nella SD
     */
    public static long getExternalAvailableSpaceInKB(){
        return getExternalAvailableSpaceInBytes()/SIZE_KB;
    }
    /**
     * MB disponibili nella SD
     */
    public static long getExternalAvailableSpaceInMB(){
        return getExternalAvailableSpaceInBytes()/SIZE_MB;
    }

    /**
     * GB disponibili nella SD
     */
    public static long getExternalAvailableSpaceInGB(){
        return getExternalAvailableSpaceInBytes()/SIZE_GB;
    }

    /**
     * Numero di "blocchi" disponibili nella SD
     */
    public static long getExternalStorageAvailableBlocks() {
        long availableBlocks = -1L;
        try {
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            availableBlocks = stat.getAvailableBlocks();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return availableBlocks;
    }
}
