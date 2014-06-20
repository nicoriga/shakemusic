package com.acceleraudio.design;

import java.util.ArrayList;

import com.acceleraudio.activity.ListSessionActivity;
import com.acceleraudio.database.DbAdapter;
import com.acceleraudio.util.ImageBitmap;
import com.acceleraudio.util.RecordedSession;
import com.malunix.acceleraudio.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * @author Nicola Rigato
 * @author Luca Del Salvador
 * @author Marco Tessari
 * @author Gruppo: Malunix
 *
 * adapter personalizzato per la lista delle sessioni
 */
public class ListSessionAdapter extends ArrayAdapter<RecordedSession>
{
	private final Activity context;
	private ArrayList<RecordedSession> sessions;
	private int layout;
	private int totSample;
	
	/**
	 * @param context il contesto corrente
	 * @param layout il tipo di layout
	 * @param sessions ArrayList di RecordedSession
	 */
	public ListSessionAdapter(Activity context, int layout, ArrayList<RecordedSession> sessions) 
	{
		super(context, layout, sessions);
		this.context = context;
		this.layout = layout;
		this.sessions = sessions;
	}
	
	@Override
	public View getView(final int position, View rowView, ViewGroup parent) throws IndexOutOfBoundsException
	{	
		ViewRowHolder holder = null;
		if (rowView == null) 
		{
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(layout, null, true);
			holder = new ViewRowHolder(rowView, layout);
			rowView.setTag(holder);
		}
		else
		{
			holder=(ViewRowHolder)rowView.getTag();
		}
		
		holder.txtName.setText(sessions.get(position).getName());
		holder.txtData.setText(sessions.get(position).getModifiedDate());

		try {
			// converto la stringa in una immagine bitmap
			Bitmap bmp = ImageBitmap.decodeImage(sessions.get(position).getImage());
			holder.imageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, 40,40, false));
		} catch (Exception e) {
			DbAdapter dbAdapter = new DbAdapter(context);
			dbAdapter.open();
			dbAdapter.deleteSession(sessions.get(position).getId());
			dbAdapter.close();
			
			e.printStackTrace();
		}
		
		// nel caso il layout sia quello con le checkBox per la selezione delle sessioni da unire
		if(layout == R.layout.list_session_select_layout)
		{
			holder.select.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked){
						if (!sessions.get(position).isSelected()) {
							sessions.get(position).select(true);
							totSample += sessions.get(position).getNumSample();
							ListSessionActivity.totSamplePB.setProgress(totSample);
						}
					}
					else {
						if (sessions.get(position).isSelected()) {
							sessions.get(position).select(false);
							totSample -= sessions.get(position).getNumSample();
							ListSessionActivity.totSamplePB.setProgress(totSample);
						}
					}
					
				}
			});
			if(sessions.get(position).isSelected()) holder.select.setChecked(true);
			else holder.select.setChecked(false);
		}
		else totSample = 0;
		
		return rowView;
	}
	
	/**
	 * @return restituisce un array degli id delle sessioni selezionate
	 */
	public long[] getSelectedSessionId()
	{
		ArrayList<Long> selectedSessionId = new  ArrayList<Long>();
		for (int i=0; i<sessions.size(); i++) if(sessions.get(i).isSelected()) selectedSessionId.add(sessions.get(i).getId());
		long[] selectedId = new long[selectedSessionId.size()];
		int x = 0;
		for(long id: selectedSessionId){
			selectedId[x] = id;
			x++;
		}
		return selectedId;
	}
	
	/**
	 * @return restituisce un array degli id di tutte le sessioni
	 */
	public long[] getSessionId()
	{
		long[] id = new long[sessions.size()];
		for(int i=0; i<sessions.size(); i++)
			id[i] = sessions.get(i).getId();
		return id;
	}
	
	/**
	 *  imposta il totale dei sample a 0
	 */
	private void resetTotSample()
	{
		totSample = 0;
	}

	/**
	 * @return il totale dei sample delle sessioni selezionate
	 */
	public int getTotSample()
	{
		return totSample;
	}
	
	/**
	 * @return la quantita di sessioni selezionate
	 */
	public int getSelectedSize()
	{
		int x = 0;
		for (int i=0; i<sessions.size(); i++) if(sessions.get(i).isSelected()) x++;
		return x;
	}
	
	/**
	 * @param totSample il totale dei sample delle sessioni da selezionare
	 * @param selectedSessionId array degli id delle sessioni da selezionare
	 */
	public void setSelectedSession(int totSample, long[] selectedSessionId){
		this.totSample = totSample;
		for(int x=0; x<selectedSessionId.length; x++)
			for (int i=0; i<sessions.size(); i++) 
				if(sessions.get(i).getId() == selectedSessionId[x]){
					sessions.get(i).select(true);
					break;
				}
	}
	
	/**
	 * deseleziona tutte le sessioni
	 */
	public void resetSelectedSession(){
		for (int i=0; i<sessions.size(); i++) sessions.get(i).select(false);
		resetTotSample();
	}
	
	/**
	 * aggiunge una sessione nella posizione impostata
	 * 
	 * @param position posizione di inserimento
	 * @param id id sessione
	 * @param name nome della sessione
	 * @param dataMod data di modifica
	 * @param image immagine in formato stringa
	 * @param nSample totale dei campioni della sessione
	 */
	public void addRowAtPosition(int position, int id, String name, String dataMod, String image, int nSample){
		RecordedSession s = new RecordedSession(id, name, dataMod, image, nSample);
		sessions.add(position, s);
	}
	
	/**
	 * classe interna per gestire le righe della listView
	 * serve per salvare le righe in memoria
	 */
	class ViewRowHolder {

		int layout = 0;
		boolean selected = false;
		TextView txtName = null;
		TextView txtData = null;
		ImageView imageView = null;
		CheckBox select = null;
		
		ViewRowHolder(View rowView, int layout) {
			this.layout = layout;
			this.txtName = (TextView) rowView.findViewById(R.id.txtNomeSessione);
			this.txtData = (TextView) rowView.findViewById(R.id.txtDataModifica);
			this.imageView = (ImageView) rowView.findViewById(R.id.img);
			if(layout == R.layout.list_session_select_layout)
				this.select = (CheckBox) rowView.findViewById(R.id.selectSession);
		}
	}
}