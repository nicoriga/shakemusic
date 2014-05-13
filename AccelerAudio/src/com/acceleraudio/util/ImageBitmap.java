package com.acceleraudio.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

public class ImageBitmap 
{
	
	/*
	 * Metodo per colorare una bitmap in modo standard
	 * Bitmap bmp: la bitmap da colorare
	 * data_x: valori dall'asse x
	 * data_y: valori dall'asse y
	 * data_z: valori dall'asse z
	 */
	public static void color(Bitmap bmp, String[] data_x, String[] data_y, String[] data_z, int id)
	{
		int _x = 1, _y = 1, _z = 1;
		double val;
		//algoritmo di colorazione
		for (int y = 0; y < bmp.getHeight(); y++) 			//selezione la riga
        {
			for (int x = 0; x < bmp.getWidth(); x++) 		//seleziona la colonna e quindi il pixel
			{
				int p = x/10;
				int r = y/10;
				switch ((p+r)%4) 
				{
					case 0:		//pesco da x	attenzione il primo valore 
						try {
							//Log.w("valore x:", "-"+data_x[_x]+"-");
							if(data_x[_x].length()>0) val = (double)Float.parseFloat(data_x[_x]);
							else val = id;
						} catch (NumberFormatException e) {
							val = id;
							e.printStackTrace();
						}
						if(_x+1 >= data_x.length)
							_x = 0;
						else
							_x++;
						break;
					case 1: 	//pesco da y
						try {
							if(data_y[_y].length()>0) val = (double)Float.parseFloat(data_y[_y]);
							else val = id;
						} catch (NumberFormatException e) {
							val = id;
							e.printStackTrace();
						}
						if(_y+1 >= data_y.length)
							_y = 0;
						else
							_y++;
						
						break;
					
					case 2:		//pesco da z
						try {
							if(data_z[_z].length()>0) val = (double)Float.parseFloat(data_z[_z]);
							else val = id;
						} catch (NumberFormatException e) {
							val = id;
							e.printStackTrace();
						}
						if(_z+1 >= data_z.length)
							_z = 0;
						else
							_z++;
					break;
					
					case 3:
						val = id;
					break;

					default:
					val = 0;
				}
				
				
				int v = (int)(val*100);
				bmp.setPixel(x, y, colorSelect(v%12));
			}
			_x = 1;
			_y = 1;
			_z = 1;
        }
	}
	
	/*
	 * Metodo per colorare una bitmap in modo standard
	 * Bitmap bmp: la bitmap da colorare
	 * data_x: valori dall'asse x
	 * data_y: valori dall'asse y
	 * data_z: valori dall'asse z
	 */
	public static void colorStandard(Bitmap bmp, String[] data_x, String[] data_y, String[] data_z)
	{
		//algoritmo di colorazione
		for (int y = 0; y < bmp.getHeight(); y++) 			//selezione la riga
        {
			if(y < bmp.getHeight()/2)
			{
				for (int x = 0; x < bmp.getWidth(); x++) 		//seleziona la colonna e quindi il pixel
				{
					if(x < bmp.getWidth()/2)
					{
						bmp.setPixel(x, y, Color.BLACK);
						Log.d("myApp", "colore nero: " + Color.BLACK);
					}
					else
					{
						bmp.setPixel(x, y, Color.RED);
						Log.d("myApp", "colore rosso: " + Color.RED);
					}
					
				}
			}
			else
			{
				for (int x = 0; x < bmp.getWidth(); x++) 		//seleziona la colonna e quindi il pixel
				{
					if(x < bmp.getWidth()/2)
					{
						bmp.setPixel(x, y, Color.GREEN);
						Log.d("myApp", "colore verde: " + Color.GREEN);
					}
					else
					{
						bmp.setPixel(x, y, Color.BLUE);
						Log.d("myApp", "colore blue: " + Color.BLUE);
					}
					
				}
			}			
		}
	}
	
	private static int colorSelect(int val)
	{
		switch(val)
		{
			case 0: return Color.BLACK; 
			case 1: return Color.BLUE;
			case 2: return Color.CYAN;
			case 3: return Color.DKGRAY;
			case 4: return Color.GRAY;
			case 5: return Color.GREEN;
			case 6: return Color.LTGRAY;
			case 7: return Color.MAGENTA;
			case 8: return Color.RED;
			case 9: return Color.WHITE;
			case 10: return Color.YELLOW;
			case 11: return Color.TRANSPARENT;
			default: return Color.TRANSPARENT;
		}
		 
	}
}
