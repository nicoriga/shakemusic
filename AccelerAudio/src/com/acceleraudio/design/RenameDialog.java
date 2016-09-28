package com.acceleraudio.design;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.malunix.acceleraudio.R;

/**
 * @author Nicola Rigato
 * @author Luca Del Salvador
 * @author Marco Tessari
 * @author Gruppo: Malunix
 *
 * dialog personalizzato per rinominare il nome della sessione
 */
public class RenameDialog extends DialogFragment {

	public static String POSITION = "renameDialog.position";
	
	private int position;
	private String oldName;
    private EditText newName;
    private Button confirm, cancel;

    public RenameDialog() {
    }
    
    /**
     * imposta le informazioni della sessione
     * 
     * @param position posizione della listView
     * @param name nome della sessione
     */
    public void setSessionInfo(int position, String name)
    {
    	this.position = position;
    	this.oldName = name;
    }
    
    /**
     * listener della chiusura del dialog
     *
     */
    public interface RenameDialogListener {
        void onFinishRenameDialog(int sessionId, String newName, boolean confirm);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.rename_dialog, container);
        newName = (EditText) view.findViewById(R.id.rename_newName);
        confirm = (Button) view.findViewById(R.id.rename_confirm);
        cancel = (Button) view.findViewById(R.id.rename_cancel);
        getDialog().setTitle("Rinomina la sessione");
        
        if(oldName!= null)newName.setText(oldName);
        
        if (savedInstanceState != null)
		{
        	position = savedInstanceState.getInt(POSITION);
		}
        
        /**** confermo la rinomina ****/
        confirm.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				RenameDialogListener activity = (RenameDialogListener) getActivity();
		        activity.onFinishRenameDialog(position, newName.getText().toString(), true);
		        dismiss();
			}
		});
        
        /**** annullo la rinomina ****/
        cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				RenameDialogListener activity = (RenameDialogListener) getActivity();
		        activity.onFinishRenameDialog(position, oldName, false);
		        dismiss();
			}
		});
        
        return view;
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) 
    {
		savedInstanceState.putInt(POSITION, position);
    	super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * @param name il nome da scrivere nella textView
     */
    public void setText(String name) {
    	newName.setText(name);
	}
    
	/**
	 * @param title imposto il titolo del dialog
	 */
	public void setTitle(String title) {
		getDialog().setTitle(title);
	}
}