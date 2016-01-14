package com.brodev.socialapp.android.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class CoreManager {
	
	// Shared Preferences
	SharedPreferences pref;

	// Editor for Shared preferences
	Editor editor;

	// Context
	Context _context;

	// SharedPreferences mode
	int PRIVATE_MODE = 0;

	// SharedPreferences file name
	private static final String PREF_NAME = "socialapp_core";

	// key for url
	private static final String KEY_URL = "url_core";
	
	//key for sender id
	private static final String KEY_GCM = "gcm_core";

	// Constructor
	public CoreManager(Context context) {
		this._context = context;
		pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}
	
	/**
	 * Create core url
	 * @param url
	 */
	public void createUrl(String url) {
		// Storing url in pref
		editor.putString(KEY_URL, url);

		// commit changes
		editor.commit();
	}
	
	/**
	 * create gcm key
	 * @param keyGCM
	 */
	public void createGCMKey(String keyGCM) {
		//Storing gcm key in pref
		editor.putString(KEY_GCM, keyGCM);
		
		//commit changes
		editor.commit();
	}
	
	/**
	 * get core url
	 * @return
	 */
	public String getCoreUrl() {
		String coreUrl = null;
		coreUrl = pref.getString(KEY_URL, "");
		return coreUrl;
	}
	
	/**
	 * Get gcm key
	 * @return gcm key
	 */
	public String getGCMKey() {
		String gcmKey = null;
		gcmKey = pref.getString(KEY_GCM, "");
		return gcmKey;
	}
}
