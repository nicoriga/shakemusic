package com.example.acceleraudio;

import com.example.acceleraudio.R;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class UpsamplingAdapter extends ArrayAdapter<String>
{
        private final Activity context;
        private final String[] data;

        public UpsamplingAdapter(Activity context, int resource, String[] data)
        {
            super(context, resource, data);
            this.context = context;
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) 
        {   // Ordinary view in Spinner, we use android.R.layout.simple_spinner_item
            return super.getView(position, convertView, parent);   
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent)
        {   // This view starts when we click the spinner.
            View row = convertView;
            if(row == null)
            {
                LayoutInflater inflater = context.getLayoutInflater();
                row = inflater.inflate(R.layout.c_spinner_menu, parent, false);
            }

            String item = data[position];

            if(item != null)
            {   // Parse the data from each object and set it.
                TextView myCountry = (TextView) row.findViewById(R.id.upsamplingRate);
                /*if(myFlag != null)
                {
                    myFlag.setBackgroundDrawable(getResources().getDrawable(item.getCountryFlag()));
                }*/
                
                if(myCountry != null)
                    myCountry.setText(item);

            }
            
            row.setMinimumWidth(100);
            //ViewGroup group = parent;
            //group.setLayoutParams(new ListView.LayoutParams(100, LayoutParams.WRAP_CONTENT));
            
            return row;
        }
}