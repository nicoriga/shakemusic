package com.example.acceleraudio;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomListSession extends ArrayAdapter<String>
{
	private final Activity context;
	private final String[] sessionName;
	private final String[] dateMod;
	private final Integer[] imageId;
	
	public CustomListSession(Activity context,String[] sessionName, String[] dateMod, Integer[] imageId) 
	{
		super(context, R.layout.list_session_layout, sessionName);
		this.context = context;
		this.sessionName = sessionName;
		this.dateMod = dateMod;
		this.imageId = imageId;
	}
	
	@Override
	public View getView(int position, View view, ViewGroup parent) 
	{
		LayoutInflater inflater = context.getLayoutInflater();
		View rowView= inflater.inflate(R.layout.list_session_layout, null, true);
		TextView txtTitle = (TextView) rowView.findViewById(R.id.txtNomeSessione);
		TextView txtData = (TextView) rowView.findViewById(R.id.txtDataModifica);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
		txtTitle.setText(sessionName[position]);
		txtData.setText(dateMod[position]);
		imageView.setImageResource(imageId[position]);
		
		return rowView;
	}
}