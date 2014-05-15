package com.acceleraudio.design;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.malunix.acceleraudio.R;

public class RenameDialog extends DialogFragment {

	private int position;
	private String oldName;
    private EditText newName;
    private Button confirm, cancel;

    public RenameDialog() {
    }
    
    public void setSessionInfo(int position, String name)
    {
    	this.position = position;
    	oldName = name;
    }
    
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
}