package com.brodev.socialapp.android;



import com.brodev.socialapp.android.manager.PrivacyManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import android.widget.TextView;

public final class Privacy {
	private PhraseManager phraseManager;
	private Context context;
	private TextView privacy;
	private String privacy_value = "0";
	private PrivacyManager privacyManager;
	
	public Privacy(Context context) {
		this.context = context;
		phraseManager = new PhraseManager(context);
		privacyManager = new PrivacyManager(context);
	}
	
	public void setTextView(TextView privacy) {
		this.privacy = privacy;
	}
	
	public String getValue() {
		return this.privacy_value;
	}
	
	
	
	public void showMessage() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		
		builder.setItems(privacyManager.getValue(),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// The 'which' argument contains the index position
						// of the selected item
						if (which == 0) {
							privacy.setText(phraseManager.getPhrase(context, "privacy.everyone"));
							privacy_value = "0";
						} else if (which == 1) {
							privacy.setText(phraseManager.getPhrase(context, "privacy.friends"));							
							privacy_value = "1";
						} else if (which == 2) {
							privacy.setText(phraseManager.getPhrase(context, "privacy.friends_of_friends"));
							privacy_value = "2";
						} else if (which == 3) {
							privacy.setText(phraseManager.getPhrase(context, "privacy.only_me"));
							privacy_value = "3";
						}
					}
				});
		builder.show();
	}
}
