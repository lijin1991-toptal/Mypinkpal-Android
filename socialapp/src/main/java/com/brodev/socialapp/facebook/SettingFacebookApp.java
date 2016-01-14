package com.brodev.socialapp.facebook;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SettingFacebookApp {

	// Shared Preferences
	SharedPreferences pref;

	// Editor for Shared preferences
	Editor editor;

	// Context
	Context _context;

	// Shared pref mode
	int PRIVATE_MODE = 0;

	// Sharedpref file name
	private static final String PREF_NAME = "Facebook_Social_App";
	private static final String APP_ID = "facebook_app_id";
	private static final String DISPLAY_FACEBOOK = "display_facebook";
	
	// Constructor
	public SettingFacebookApp(Context context) {
		this._context = context;
		pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}
	
	
	/**
	 * Function store app id, display facebook
	 * @param appId
	 */
	public void storeAppId(String appId, boolean displayFb) {
		editor.putString(APP_ID, appId);
		editor.putBoolean(DISPLAY_FACEBOOK, displayFb);
		editor.commit();
	}
	
	public String getAppId() {
		return pref.getString(APP_ID, null);
	}
	
	public boolean getDisplayFb() {
		return pref.getBoolean(DISPLAY_FACEBOOK, false);
	}
}
