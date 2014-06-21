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

/**
 * @author Nicola Rigato
 * @author Luca Del Salvador
 * @author Marco Tessari
 * @author Gruppo: Malunix
 *
 * file manager per scegliere la cartella su cui esportare la sessione in formato WAV
 */
public class FileExplorer extends FragmentActivity implements RenameDialogListener{
	
	private List<String> item = null, path = null;
	private String root, sessionName, savePath;
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
	private boolean isExporting = false, mounted = false;
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
			mounted = true;
			root = Environment.getExternalStorageDirectory().getPath();
			getDir(root);
			
			// visualizzo lo spazio disponibile
			Toast.makeText(getApplicationContext(), Float.toString(AvailableSpace.getExternalAvailableSpace(AvailableSpace.SIZE_MB))+ getString(R.string.notify_MB_Avaiable), Toast.LENGTH_LONG).show();
			
			/*** imposto azione quando viene premuto il tasto per iniziare l'esportazione del file wav ***/
			save.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try 
					{
						// mi ricavo la destinazione scelta
						savePath = myPath.getText().toString().substring(10);
						File f = new File(savePath);
						Log.w("permesso scrittura: ", "" + f.canWrite());
						
						// verifico di avere i permessi di scrittura
						if(f.canWrite()){
							isExporting = true;
							
							Bundle b = getIntent().getExtras();
							sessionId = b.getLong(DbAdapter.T_SESSION_SESSIONID);
	    	
							// prelevo i dati della sessione dal database
							dbAdapter.open();
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
								
								cursor.close();
								dbAdapter.close();
								
								// controllo che la sessione avesse almeno un asse selezionato
								if(!(axis_x || axis_y || axis_z)) 
								{
									Toast.makeText(getApplicationContext(), R.string.error_no_axis_selected, Toast.LENGTH_SHORT).show();
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
								
								// verifica che sia disponibile nella memory card lo spazio richiesto per l'esportazione
								if(AvailableSpace.getExternalAvailableSpaceInBytes()> totalDataLenght()){
									Log.w("Save Directory", savePath +"/"+ sessionName + ".wav");
				 			        
//									Util.lockOrientation(a, v.getRootView());
									
									// inizializzo la finestra di caricamento
									loadProgressDialog();

									myFile = new File(savePath +"/"+ sessionName + ".wav");
									
				 			        t = new Thread("wav_creation") {
										public void run() {
											setPriority(Thread.MIN_PRIORITY);
											
											try {
												myFile.createNewFile();
												
												FileOutputStream fOut = new FileOutputStream(myFile);
												long totalAudioLen = totalAudioLenght();
												int channels = 1;
												// scrivo header wav nel file
												Wav.WriteWaveFileHeader(totalAudioLen, soundRate, channels, fOut);
												// scrivo 
												MusicUpsampling.note(fOut, soundRate, upsampling, sample, pd);
												if(fOut != null) 
													fOut.close();
												pd.dismiss();
//												Util.unlockOrientation(a);
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
										// avvio un dialog per gestire il nome di salvataggio del file
										new AlertDialog.Builder(v.getContext()).setTitle(R.string.notify_file_exist)
										.setIcon(android.R.drawable.ic_dialog_alert)
										.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												pd.show();
												t.start();
											}
										})
										.setNegativeButton(getString(R.string.rename), new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												displayRenameDialog();
											}
										})
										.setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												isExporting = false;
											}
										}).show();
									}
										
								}
								// messaggio memoria disponibile non sufficente
								else
									new AlertDialog.Builder(v.getContext())
										.setIcon(android.R.drawable.ic_dialog_alert)
										.setTitle(R.string.error_memory_low)
										.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
											}
									}).show();
							}
							else
							{
								cursor.close();
								dbAdapter.close();
							}
						} else{ 
							// messaggio di notifica se non si hanno i privilegi di scrittura
							new AlertDialog.Builder(v.getContext())
								.setIcon(android.R.drawable.ic_dialog_alert)
								.setTitle(R.string.error_write_privileges)
								.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
									}
							}).show();
						}
					}
					catch(SQLException e){
						if(cursor != null & !cursor.isClosed())
							cursor.close();
						if(!dbAdapter.isOpen())
							dbAdapter.close();
						e.printStackTrace();
					}
					catch (Exception e) 
					{
					     Log.e("FileExplorer Save Error", "Could not create file",e);
					}
				}
			});
		}
		else
			// messaggio di notifica se non è presente la memoria su cui si deve esportare
			new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.error_no_external_memory)
				.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
			}).show();
		
		/*** imposto azione nel caso venga premuto un elemento della lista ***/
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
				File file = new File(path.get(position));
				// verifico se è una cartella
				if (file.isDirectory())
				{
					if(file.canRead())
						getDir(path.get(position));
					else
					{
						// messaggio di errore: permessi insufficenti per la lettura
						new AlertDialog.Builder(view.getContext())
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setTitle(file.getName() + getString(R.string.error_read_privileges))
							.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
								}
						}).show();
					}
				}
				else
				{
					// se è un file mostro un dialog con il nome del file
					new AlertDialog.Builder(view.getContext())
						.setIcon(android.R.drawable.ic_dialog_info)
						.setTitle(file.getName())
						.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
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
    	// permetto di tornare indietro solo a fine esportazione
    	if(!isExporting)
    		super.onBackPressed();
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	if(mounted)getDir(myPath.getText().toString().substring(10));
    }
	
    // compila la lista in base al percorso dato come parametro
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
	
	// lunghezza totale della musica in byte
	private long totalAudioLenght(){
		return (upsampling + buffsize) * sample.length * 2;
	}
	
	// lunghezza totale della musica in byte + lunghezza header del wav
	private long totalDataLenght(){
		return totalAudioLenght()+36;
	}

	/*** azione svolta quando viene chiuso il dialog per cambiare il nome del file di esportazione ***/
	@Override
	public void onFinishRenameDialog(int sessionId, String newName,	boolean confirm) {
		if(confirm){
			sessionName = newName;
			myFile = new File(savePath +"/"+ sessionName + ".wav");
			// verifico che non sia presente un file con lo stesso nome e che il nome non sia vuoto
			if(!myFile.exists() && sessionName.length()>0)
			{
				pd.show();
				t.start();
			}
			else
				// avvio un'altro dialog per rinominare 
				displayRenameDialog();
		}
		else
		{
			pd.dismiss();
			isExporting = false;
		}
	}
	
	/*** inizializzo il progress dialog ***/
	public void loadProgressDialog(){
		
		pd = new ProgressDialog(FileExplorer.this);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setCancelable(false);
		pd.setMax(sample.length);
	    pd.setMessage("Attendere Prego");
	    pd.setTitle("Salvataggio");
	    pd.setButton(DialogInterface.BUTTON_NEGATIVE, "Annulla", new DialogInterface.OnClickListener() {
	    	@Override
	        public void onClick(DialogInterface dialog, int which) {
	            dialog.dismiss();
           		MusicUpsampling.isRunning = false;
				File myFile = new File(savePath +"/"+ sessionName + ".wav");
				myFile.delete();
				Util.unlockOrientation(a);
				isExporting = false;
	        }
	    });
	    pd.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				getDir(savePath);
			}
		});
	}
	
	/*** visualizza il dialog per rinominare la sessione ***/
	public void displayRenameDialog(){
		// avvio un'altro dialog per rinominare 
		FragmentManager fm = getSupportFragmentManager();
	    RenameDialog rd = new RenameDialog();
	    rd.setSessionInfo(0, sessionName);
	    rd.show(fm, "rename_dialog");
	}
}