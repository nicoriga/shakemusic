package com.acceleraudio.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.acceleraudio.database.DbAdapter;
import com.acceleraudio.util.AvailableSpaceHandler;
import com.acceleraudio.util.MusicUpsampling;
import com.acceleraudio.util.Util;
import com.acceleraudio.util.Wav;
import com.malunix.acceleraudio.R;

public class FileExplore extends ListActivity {
private List<String> item = null, path = null;
private String root="/", sessionName;
private TextView myPath;
private Button save;
private DbAdapter dbAdapter;
private Cursor cursor;
public static String[] data_x, data_y, data_z;
private long sessionId;
private int upsampling;
private int[] sample;
private Thread t;
private ProgressDialog pd;
private Activity a;
private boolean isExporting = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbAdapter = new DbAdapter(this);
		a = this;
		// TODO da sistemare le viste
		setContentView(R.layout.file_manager_layout);
		myPath = (TextView)findViewById(R.id.path);
		save = (Button) findViewById(R.id.UI_fileManager_BT_save);
		getDir(root);
		
		Toast.makeText(getApplicationContext(), (String) Float.toString(AvailableSpaceHandler.getExternalAvailableSpaceInMB())+ " MB disponibili", Toast.LENGTH_LONG).show();
		
		save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try 
				{
					isExporting = true;
					
					Bundle b = getIntent().getExtras();
					sessionId = b.getLong(DbAdapter.T_SESSION_SESSIONID);
	    	
////////////////////////////////////////////////////////
/// prelevo dati dal database e li carico nella vista///
///////////////////////////////////////////////////////

					// apro la connessione al db
					dbAdapter.open();
					
					// prelevo record by ID 
					cursor = dbAdapter.fetchSessionById(sessionId);
					cursor.moveToFirst();
					
					if(cursor.getCount()>0){
						
						// carico dati
						sessionName = (cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_NAME)));
						data_x = (cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_SENSOR_DATA_X))).split(" ");
						data_y = (cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_SENSOR_DATA_Y))).split(" ");
						data_z = (cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_SENSOR_DATA_Z))).split(" ");
						//TODO: si potrebbe togliere il numero dei sample presente nel database.
						boolean axis_x = cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_AXIS_X)).equals("1");
						boolean axis_y = cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_AXIS_Y)).equals("1");
						boolean axis_z = cursor.getString( cursor.getColumnIndex(DbAdapter.T_SESSION_AXIS_Z)).equals("1");
						upsampling = cursor.getInt(( cursor.getColumnIndex(DbAdapter.T_SESSION_UPSAMPLING)));
						
						// chiudo connessioni
						cursor.close();
						dbAdapter.close();
						
						if(!(axis_x || axis_y || axis_z)) 
						{
							Toast.makeText(getApplicationContext(), getString(R.string.error_no_axis_selected), Toast.LENGTH_SHORT).show();
							finish();
						}
						
						int nSample = (axis_x ? data_x.length : 0) + (axis_y ? data_y.length : 0) + (axis_z ? data_z.length : 0);
						sample = new int[(nSample > 0 ? nSample : 1)];
						
						int z=0;
						if(axis_x)
							for(int i = 0; i<data_x.length; i++)
								if(data_x[i].length()>0)
								{
									sample[z] = ((int)(Float.parseFloat(data_x[i])*10)); 
									z++;
								}
						if(axis_y)
							for(int i = 0; i<data_y.length; i++)
								if(data_y[i].length()>0)
								{ 
									sample[z] = ((int)(Float.parseFloat(data_y[i])*10)); 
									z++;
								}
						if(axis_z)
							for(int i = 0; i<data_z.length; i++)
								if(data_z[i].length()>0)
								{ 
									sample[z] = ((int)(Float.parseFloat(data_z[i])*10)); 
									z++;
								}
						
						Log.w("Save Directory", myPath.getText().toString().substring(10) +"/"+ sessionName + ".wav");
	 			        
						Util.lockOrientation(a, v.getRootView());
						
						pd = new ProgressDialog(FileExplore.this);
						pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						pd.setCancelable(false);
						pd.setMax(sample.length);
					    pd.setMessage("Attendere Prego");
					    pd.setTitle("Salvataggio");
					    pd.setButton(DialogInterface.BUTTON_NEGATIVE, "Annulla", new DialogInterface.OnClickListener() {@Override
					        public void onClick(DialogInterface dialog, int which) {
					            dialog.dismiss();
				               	synchronized (t) {
									t.interrupt();
									File myFile = new File(myPath.getText().toString().substring(10) +"/"+ sessionName + ".wav");
									myFile.delete();
									Util.unlockOrientation(a);
									isExporting = false;
								}
					        }
					    });
						pd.show();
						
	 			        t = new Thread("wav_creation") {
							public void run() {
								setPriority(Thread.MIN_PRIORITY);
								
								File myFile = new File(myPath.getText().toString().substring(10) +"/"+ sessionName + ".wav");
	//							File myFile = new File("/sdcard/" + sessionName	+ ".wav");
								try {
									myFile.createNewFile();
								
									FileOutputStream fOut = new FileOutputStream(myFile);
									int buffsize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_MONO,	AudioFormat.ENCODING_PCM_16BIT);
									long totalAudioLen = buffsize * sample.length * 2;
									long totalDataLen = totalAudioLen + 36;
									long longSampleRate = 44100;
									int channels = 1;
									Wav.WriteWaveFileHeader(totalAudioLen,totalDataLen, longSampleRate, channels,fOut);
	// 			        			fOut.write(byteBuff.array());
									MusicUpsampling.note(fOut, 44100, upsampling, sample, pd);
									fOut.close();
									pd.dismiss();
									Util.unlockOrientation(a);
									isExporting = false;
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						};
						t.start();
					}
				}
				catch(SQLException e){
					if(cursor != null & !cursor.isClosed())cursor.close();
					if(!dbAdapter.isOpen())dbAdapter.close();
					e.printStackTrace();
				}
				catch (Exception e) 
				{
				     Log.e("FileExplorer Save Error", "Could not create file",e);
				}
			}
		});
	}

    @Override
    public void onBackPressed(){
    	if(!isExporting)
    		super.onBackPressed();
    }
	
	private void getDir(String dirPath)
	{
		 myPath.setText("Location: " + dirPath);
		 item = new ArrayList<String>();
		 path = new ArrayList<String>();
		 File f = new File(dirPath);
		 File[] files = f.listFiles();
		 if(!dirPath.equals(root))
		 {
			 item.add(root);
			 path.add(root);
			 item.add("../");
			 path.add(f.getParent());
		 }
		
		 for(int i=0; i < files.length; i++)
		 {
			 File file = files[i];
			 if (file.canRead()) {
				path.add(file.getPath());
				if (file.isDirectory())
					item.add(file.getName() + "/");
				else
					item.add(file.getName());
			}
		 }
		
		 ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, R.layout.row, item);
		 setListAdapter(fileList);
    }

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		File file = new File(path.get(position));
		if (file.isDirectory())
		{
			if(file.canRead())
				getDir(path.get(position));
			else
			{
				new AlertDialog.Builder(this).setIcon(R.drawable.icon).setTitle("[" + file.getName() + "] folder can't be read!").setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				}).show();
			}
		}
		else
		{
			new AlertDialog.Builder(this).setIcon(R.drawable.icon).setTitle("[" + file.getName() + "]").setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub    	   
				}
			}).show();
		}
	}
}