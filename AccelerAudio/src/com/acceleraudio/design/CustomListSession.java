package com.acceleraudio.design;

import java.util.ArrayList;

import com.example.acceleraudio.R;

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
	private final ArrayList<String> sessionNameList;
	private final String[] dateMod;
	private final Integer[] imageId;
	
	public CustomListSession(Activity context,String[] sessionName, String[] dateMod, Integer[] imageId) 
	{
		super(context, R.layout.list_session_layout, sessionName);
		this.context = context;
		this.sessionName = sessionName;
		this.dateMod = dateMod;
		this.imageId = imageId;
		this.sessionNameList = null;
	}
	
	public CustomListSession(Activity context,ArrayList<String> sessionNameList, String[] dateMod, Integer[] imageId) 
	{
		super(context, R.layout.list_session_layout, sessionNameList);
		this.context = context;
		this.sessionNameList = sessionNameList;
		this.dateMod = dateMod;
		this.imageId = imageId;
		this.sessionName = null;
	}
	
	@Override
	public View getView(int position, View view, ViewGroup parent) 
	{
		LayoutInflater inflater = context.getLayoutInflater();
		View rowView= inflater.inflate(R.layout.list_session_layout, null, true);
		
		TextView txtName = (TextView) rowView.findViewById(R.id.txtNomeSessione);
		TextView txtData = (TextView) rowView.findViewById(R.id.txtDataModifica);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
		
		if(sessionName != null) txtName.setText(sessionName[position]);
		if(sessionNameList != null) txtName.setText(sessionNameList.get(position));
			
		txtData.setText(dateMod[position]);
		imageView.setImageResource(imageId[position]);
		
		return rowView;
	}
}