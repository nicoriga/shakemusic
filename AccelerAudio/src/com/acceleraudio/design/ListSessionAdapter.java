package com.acceleraudio.design;

import java.util.ArrayList;

import com.acceleraudio.database.DbAdapter;
import com.acceleraudio.util.ImageBitmap;
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

public class ListSessionAdapter extends ArrayAdapter<String>
{
	private final Activity context;
	private final ArrayList<Integer> sessionIdList;
	private ArrayList<Integer> selectedSessionId = null;
	private final ArrayList<String> sessionNameList, dateMod, image;
	private final ArrayList<Boolean> isSelected;
	private final int layout;
	
	// TODO eliminare sessionIdList
	public ListSessionAdapter(Activity context, int layout, ArrayList<Integer> sessionIdList, ArrayList<String> sessionNameList, ArrayList<String> dateMod, ArrayList<String> imageId) 
	{
		super(context, layout, sessionNameList);
		this.context = context;
		this.sessionIdList = sessionIdList;
		this.sessionNameList = sessionNameList;
		this.dateMod = dateMod;
		this.image = imageId;
		this.layout = layout;
		if(layout == R.layout.list_session_select_layout)
		{
			this.isSelected = new ArrayList<Boolean>();
			for(int i=0; i<sessionIdList.size(); i++) this.isSelected.add(false);
		}
		else
			this.isSelected = null;
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
		
		if(sessionNameList != null) holder.txtName.setText(sessionNameList.get(position));
		else holder.txtName.setText("");
		
		holder.txtData.setText(dateMod.get(position));

		try {
			// converto la stringa in una immagine bitmap
			Bitmap bmp = ImageBitmap.decodeImage(image.get(position));
			holder.imageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, 40,40, false));
		} catch (Exception e) {
			// TODO da rimuovere... serve solo per eliminare quelle con problemi
			DbAdapter dbAdapter = new DbAdapter(context);
			dbAdapter.open();
			dbAdapter.deleteSession(sessionIdList.get(position));
			dbAdapter.close();
			
			e.printStackTrace();
		}
		
		if(layout == R.layout.list_session_select_layout)
		{
			holder.select.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked) isSelected.set(position, true);
					else isSelected.set(position, false);
					
				}
			});
			if(isSelected.get(position)) holder.select.setChecked(true);
			else holder.select.setChecked(false);
		}
		return rowView;
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
	
	// restituisce un arraylist degli id delle sessioni selezionate
	public ArrayList<Integer>  getSelectedSession()
	{
		selectedSessionId = new  ArrayList<Integer>();
		for (int i=0; i<isSelected.size(); i++) if(isSelected.get(i)) selectedSessionId.add(sessionIdList.get(i));
		return selectedSessionId;
	}
	
	public int getSelectedSize()
	{
		if(selectedSessionId !=null)
			return selectedSessionId.size();
		else return 0;
	}
	
	// deseleziona tutte le sessioni
	public void resetSelectedSession(){
		for (int i=0; i<isSelected.size(); i++) isSelected.set(i,false);
	}
	
	public void addRowAtPosition(int position, int id, String name, String dataMod, String image){
		sessionIdList.add(position, id);
		sessionNameList.add(position, name);
		dateMod.add(position, dataMod);
		this.image.add(position, image);
		if(layout == R.layout.list_session_select_layout)
			this.isSelected.add(position, false);
	}
}