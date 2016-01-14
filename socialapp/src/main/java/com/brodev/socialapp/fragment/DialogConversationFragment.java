package com.brodev.socialapp.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;


import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.config.Config;

public class DialogConversationFragment extends DialogFragment {
	
	private PhraseManager phraseManager;
	private static String sUerId;
	private static boolean bIsBlock;
	
	public static DialogConversationFragment newInstance(String userId, boolean isBlock) {	
		Bundle b = new Bundle();
		sUerId = userId;
		bIsBlock = isBlock;
		DialogConversationFragment f = new DialogConversationFragment();
		f.setArguments(b);
		return f;
	}

	@Override
	public void onActivityCreated(Bundle arg0) {
		
		super.onActivityCreated(arg0);
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //set title
        builder.setTitle(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.conversation"));
        
        List<String> listItems = new ArrayList<String>();
        //if contact is blocked
        if (!bIsBlock) 
        	listItems.add(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.block"));
        else 
        	listItems.add(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.unblock"));
        
        listItems.add(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.delete"));
        final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
        
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// The 'which' argument contains the index position
				// of the selected item
				if (which == 0) {
					//block/unblock contact
					if (!bIsBlock)
						displayMessage(getActivity().getApplicationContext(), "block", sUerId, null);
					else 
						displayMessage(getActivity().getApplicationContext(), "unblock", sUerId, null);
				} else if (which == 1) {
					//action delete conversation
					displayMessage(getActivity().getApplicationContext(), "delete", sUerId, null);
				}
			}
		});
        return builder.create();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		phraseManager = new PhraseManager(getActivity().getApplicationContext());
		super.onCreate(savedInstanceState);
	}
	
	/**
	 * This method is defined in the common helper because it's used both by
     * the UI and the background service.
	 * @param context
	 * @param message
	 */
	public static void displayMessage(Context context, String type, String userId, String message) {
        Intent intent = new Intent(Config.DISPLAY_CHAT_ACTION);
        intent.putExtra("type", type);
        
        intent.putExtra("userId", userId);
        intent.putExtra("message", message);
        
        context.sendBroadcast(intent);
    }
}
