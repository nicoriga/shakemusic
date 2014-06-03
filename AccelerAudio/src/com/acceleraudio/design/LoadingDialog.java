package com.acceleraudio.design;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.malunix.acceleraudio.R;

public class LoadingDialog extends DialogFragment {

	public static String POSITION = "loadingDialog.position";
	
	private int position;
	private ProgressBar progress;

    public LoadingDialog() {
    }
    
    public interface LoadingDialogListener {
        void onFinishLoadingDialog(int sessionId, String newName, boolean confirm);
    }
    
    public void setProgress(int position){
    	this.position = position;
    	progress.setProgress(position);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.rename_dialog, container);
        progress = (ProgressBar) view.findViewById(R.id.PB_loading);
        getDialog().setTitle("Caricamento");
        
        if (savedInstanceState != null)
		{
        	position = savedInstanceState.getInt(POSITION);
		}
        
        return view;
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) 
    {
		savedInstanceState.putInt(POSITION, position);
    	super.onSaveInstanceState(savedInstanceState);
    }
}