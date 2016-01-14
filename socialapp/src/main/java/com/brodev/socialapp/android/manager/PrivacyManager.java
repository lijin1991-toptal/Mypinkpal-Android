package com.brodev.socialapp.android.manager;

import java.util.ArrayList;
import java.util.List;

import com.brodev.socialapp.android.PhraseManager;

import android.content.Context;

public class PrivacyManager {
	
	
	//parent layout
	private Context context;

	private PhraseManager phraseManager; 
	
	
	private List<String> listPrivacy = new ArrayList<String>();	
	
	
	public PrivacyManager(Context context) {
		this.context = context;
		phraseManager = new PhraseManager(this.context);
		
		listPrivacy.add(phraseManager.getPhrase(this.context, "privacy.everyone"));
		listPrivacy.add(phraseManager.getPhrase(this.context, "privacy.friends"));
		listPrivacy.add(phraseManager.getPhrase(this.context, "privacy.friends_of_friends"));
		listPrivacy.add(phraseManager.getPhrase(this.context, "privacy.only_me"));
	}
	
	public CharSequence[] getValue() {
		CharSequence[] itemPrivacy = listPrivacy.toArray(new CharSequence[listPrivacy.size()]);
		return itemPrivacy;
	}
	
	
}
