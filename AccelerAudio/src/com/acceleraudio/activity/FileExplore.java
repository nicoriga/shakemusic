package com.acceleraudio.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.database.Cursor;
import android.database.SQLException;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.acceleraudio.database.DbAdapter;
import com.acceleraudio.design.RenameDialog;
import com.acceleraudio.design.RenameDialog.RenameDialogListener;
import com.acceleraudio.util.AvailableSpace;
import com.acceleraudio.util.MusicUpsampling;
import com.acceleraudio.util.Util;
import com.acceleraudio.util.Wav;
import com.malunix.acceleraudio.R;

public class FileExplore extends FragmentActivity implements RenameDialogListener{
	
private List<String> item = null, path = null;
private String root, sessionName;
private ListView list;
private TextView myPath;
private Button save;
private File myFile;
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
private final int soundRate = 48000 ;
private final int buffsize = AudioTrack.getMinBufferSize(soundRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbAdapter = new DbAdapter(this);
		a = this;

		setContentView(R.layout.file_manager_layout);
		list = (ListView)findViewById(R.id.list_file_manager);
		myPath = (TextView)findViewById(R.id.path);
		save = (Button) findViewById(R.id.UI_fileManager_BT_save);
		
		// verifico che la memoria sia caricata
		String state = Environment.getExternalStorageState();
		if( state.equals(Environment.MEDIA_MOUNTED) ){
			root = Environment.getExternalStorageDirectory().getPath();
			getDir(root);
			
			Toast.makeText(getApplicationContext(), Float.toString(AvailableSpace.getExternalAvailableSpace(AvailableSpace.SIZE_MB))+ " MB disponibili", Toast.LENGTH_LONG).show();
			
			save.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try 
					{
						final String savePath = myPath.getText().toString().substring(10);
						File f = new File(savePath);
						Log.w("permesso scrittura: ", "" + f.canWrite());
						
						if(f.canWrite()){
							isExporting = true;
							
							Bundle b = getIntent().getExtras();
							sessionId = b.getLong(DbAdapter.T_SESSION_SESSIONID);
	    	
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
											sample[z] = ((int)(Float.parseFloat(data_x[i]))); 
											z++;
										}
								if(axis_y)
									for(int i = 0; i<data_y.length; i++)
										if(data_y[i].length()>0)
										{ 
											sample[z] = ((int)(Float.parseFloat(data_y[i]))); 
											z++;
										}
								if(axis_z)
									for(int i = 0; i<data_z.length; i++)
										if(data_z[i].length()>0)
										{ 
											sample[z] = ((int)(Float.parseFloat(data_z[i]))); 
											z++;
										}
								
								// verifica che ci sia lo spazio disponibile nella memory card
								if(AvailableSpace.getExternalAvailableSpaceInBytes()> totalDataLenght()){
									Log.w("Save Directory", savePath +"/"+ sessionName + ".wav");
				 			        
									Util.lockOrientation(a, v.getRootView());
									
									pd = new ProgressDialog(FileExplore.this);
									pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
									pd.setCancelable(false);
									pd.setMax(sample.length);
								    pd.setMessage("Attendere Prego");
								    pd.setTitle("Salvataggio");
								    pd.setButton(DialogInterface.BUTTON_NEGATIVE, "Annulla", new DialogInterface.OnClickListener() {
								    	@Override
								        public void onClick(DialogInterface dialog, int which) {
								            dialog.dismiss();
							               	synchronized (t) {
							               		MusicUpsampling.isRunning = false;
//												t.interrupt();
												File myFile = new File(savePath +"/"+ sessionName + ".wav");
												myFile.delete();
												Util.unlockOrientation(a);
												isExporting = false;
											}
								        }
								    });
								    pd.setOnDismissListener(new OnDismissListener() {
										@Override
										public void onDismiss(DialogInterface dialog) {
											getDir(savePath);
										}
									});
								    
									myFile = new File(myPath.getText().toString().substring(10) +"/"+ sessionName + ".wav");
									
				 			        t = new Thread("wav_creation") {
										public void run() {
											setPriority(Thread.MIN_PRIORITY);
											
											try {
												myFile.createNewFile();
											
												FileOutputStream fOut = new FileOutputStream(myFile);
												long totalAudioLen = totalAudioLenght();
												long totalDataLen = totalDataLenght();
												int channels = 1;
												Wav.WriteWaveFileHeader(totalAudioLen,totalDataLen, soundRate, channels,fOut);
												MusicUpsampling.note(fOut, soundRate, upsampling, sample, pd);
												fOut.close();
												pd.dismiss();
												Util.unlockOrientation(a);
												isExporting = false;
											} catch (IOException e) {
												e.printStackTrace();
											}
										}
									};
									
									// verifico se esiste un file con lo stesso nome
									if(!myFile.exists())
									{
										pd.show();
										t.start();
									}
									else
									{
										new AlertDialog.Builder(v.getContext()).setTitle("File già presente: Vuoi sovrascrivere?")
										.setPositiveButton("SI", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												pd.show();
												t.start();
											}
										})
										.setNegativeButton("NO", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												FragmentManager fm = getSupportFragmentManager();
											    RenameDialog rd = new RenameDialog();
											    rd.setSessionInfo(0, sessionName + ".wav");
											    rd.show(fm, "rename_dialog");
											}
										})
										.setNeutralButton("Annulla", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												
											}
										}).show();
									}
										
								}
								else
									Toast.makeText(v.getContext(), getString(R.string.error_memory_low), Toast.LENGTH_SHORT).show();
						}
						else
						{
							// chiudo connessioni
							cursor.close();
							dbAdapter.close();
						}
					} else{ 
						new AlertDialog.Builder(v.getContext()).setTitle(getString(R.string.error_write_privileges)).setPositiveButton("OK", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						}).show();
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
		
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
				File file = new File(path.get(position));
				if (file.isDirectory())
				{
					if(file.canRead())
						getDir(path.get(position));
					else
					{
						// TODO: sistemare messaggio stringa
						new AlertDialog.Builder(view.getContext()).setIcon(R.drawable.icon).setTitle("[" + file.getName() + "] folder can't be read!").setPositiveButton("OK", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						}).show();
					}
				}
				else
				{
					new AlertDialog.Builder(view.getContext()).setIcon(R.drawable.icon).setTitle("[" + file.getName() + "]").setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					}).show();
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
		 list.setAdapter(fileList);
    }
	
	private long totalAudioLenght(){
		return buffsize * sample.length * 2;
	}
	
	private long totalDataLenght(){
		return totalAudioLenght()+36;
	}

	@Override
	public void onFinishRenameDialog(int sessionId, String newName,	boolean confirm) {
		if(confirm){
			sessionName = newName;
			myFile = new File(myPath.getText().toString().substring(10) +"/"+ sessionName + ".wav");
			if(!myFile.exists())
			{
				pd.show();
				t.start();
			}
			else
			{
				FragmentManager fm = getSupportFragmentManager();
			    RenameDialog rd = new RenameDialog();
			    rd.setSessionInfo(0, sessionName + ".wav");
			    rd.show(fm, "rename_dialog");
			}
		}
		else
		{
			pd.dismiss();
			isExporting = false;
		}
	}
}