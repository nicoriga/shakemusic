package com.acceleraudio.design;

import java.util.ArrayList;

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

public class ListSessionAdapter extends ArrayAdapter<RecordedSession>
{
	private final Activity context;
	private ArrayList<RecordedSession> sessions;
	private int layout;
	private int totSample;
	
	// TODO eliminare sessionIdList
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
			// TODO da rimuovere... serve solo per eliminare quelle con problemi
			DbAdapter dbAdapter = new DbAdapter(context);
			dbAdapter.open();
			dbAdapter.deleteSession(sessions.get(position).getId());
			dbAdapter.close();
			
			e.printStackTrace();
		}
		
		if(layout == R.layout.list_session_select_layout)
		{
			holder.select.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked){
						sessions.get(position).select(true);
						totSample += sessions.get(position).getNumSample();
					}
					else{
						sessions.get(position).select(false);
						totSample -= sessions.get(position).getNumSample();
					}
					
				}
			});
			if(sessions.get(position).isSelected()) holder.select.setChecked(true);
			else holder.select.setChecked(false);
		}
		else totSample = 0;
		
		return rowView;
	}
	
	public void setLayout(int layout){
		this.layout = layout;
	}
	
	// restituisce un arraylist degli id delle sessioni selezionate
	public long[] getSelectedSession()
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
	
	private void resetTotSample()
	{
		totSample = 0;
	}
	
	public int getTotSample()
	{
		return totSample;
	}
	
	public int getSelectedSize()
	{
		int x = 0;
		for (int i=0; i<sessions.size(); i++) if(sessions.get(i).isSelected()) x++;
		return x;
	}
	
	// deseleziona tutte le sessioni
	public void resetSelectedSession(){
		for (int i=0; i<sessions.size(); i++) sessions.get(i).select(false);
		resetTotSample();
	}
	
	public void addRowAtPosition(int position, int id, String name, String dataMod, String image, int nSample){
		RecordedSession s = new RecordedSession(id, name, dataMod, image, nSample);
		sessions.add(position, s);
	}
	
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